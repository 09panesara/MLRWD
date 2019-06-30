package uk.ac.cam.cl.ap949.exercises;

import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise1;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.List;

import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Tokenizer;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Sentiment;


public class Exercise1 implements IExercise1{

        /**
         * Read the lexicon and determine whether the sentiment of each review in
         * the test set is positive or negative based on whether there are more
         * positive or negative words.
         *
         * @param testSet
         *            {@link Set}<{@link Path}> Paths to reviews to classify
         * @param lexiconFile
         *            {@link Path} Path to the lexicon file
         * @return {@link Map}<{@link Path}, {@link Sentiment}> Map of calculated
         *         sentiment for each review
         * @throws IOException
         */
        Map lexicon = null;
        Map intensity = null;
        public void loadSentiment(Path lexPath) {
            if (lexicon == null) {
                lexicon = new HashMap<Path, Sentiment>();
                try (BufferedReader reader = Files.newBufferedReader(lexPath)) {
                    reader.lines().forEach(line -> {
                        String[] tokens = line.split("\\s+");
                        String word = tokens[0].split("=")[1];
                        String polarity = tokens[2].split("=")[1];
                        lexicon.put(word, ((polarity.equals("positive")) ? Sentiment.POSITIVE : Sentiment.NEGATIVE));
                    });
                } catch (IOException e) {
                    System.out.println("Can't access the file " + lexPath);
                }
            }
        }

        public void loadIntensity(Path lexPath) {
            if (intensity == null) {
                intensity = new HashMap<Path, Integer>(); // 1 = strong, 0 = weak
                try (BufferedReader reader = Files.newBufferedReader(lexPath)) {
                    reader.lines().forEach(line -> {
                        String[] tokens = line.split("\\s+");
                        String word = tokens[0].split("=")[1];
                        String intens = tokens[1].split("=")[1];
                        intensity.put(word, ((intens.equals("strong")) ? 1 : 0));

//                        System.out.format("%s %s \n", word, intens);
                    });
                } catch (IOException e) {
                    System.out.println("Can't access the file " + lexPath);
                }
            }
        }


        public Map<Path, Sentiment> simpleClassifier(Set<Path> testSet, Path lexiconFile) throws IOException {
            Map sentiments = new HashMap<Path, Sentiment>();
            loadSentiment(lexiconFile);
            System.out.println(lexicon);
            int noPositive;
            int noNegative;
            for(Path p: testSet) {
                List<String> words = Tokenizer.tokenize(p);
                noPositive = 0;
                noNegative = 0;
                int count = 0;
                for(String w: words) {
                    count++;
                    if(lexicon.containsKey(w)) {
                        if (lexicon.get(w) == Sentiment.POSITIVE) {
                            noPositive++;
                        } else if (lexicon.get(w) == Sentiment.NEGATIVE) {
                            noNegative++;
                        }
                    }
                }
                sentiments.put(p, (noPositive >= noNegative) ? Sentiment.POSITIVE: Sentiment.NEGATIVE );
            }
            System.out.println(sentiments);

            return sentiments;

        }



        /**
         * Calculate the proportion of predicted sentiments that were correct.
         *
         * @param trueSentiments
         *            {@link Map}<{@link Path}, {@link Sentiment}> Map of correct
         *            sentiment for each review
         *
         * @param predictedSentiments
         *            {@link Map}<{@link Path}, {@link Sentiment}> Map of calculated
         *            sentiment for each review
         * @return <code>double</code> The overall accuracy of the predictions
         */
        public double calculateAccuracy(Map<Path, Sentiment> trueSentiments, Map<Path, Sentiment> predictedSentiments){
            double accuracy = 0;
            int noFiles = 0;
            for(Map.Entry<Path, Sentiment> entry : predictedSentiments.entrySet()) {
                Path key = entry.getKey();
                Sentiment value = entry.getValue();
                if(value.equals(trueSentiments.get(key))) {
                    accuracy++;
                }
                noFiles++;
            }
            accuracy = accuracy/noFiles;
            return accuracy;

        }

        /**
         * Use the training data to improve your classifier, perhaps by choosing an
         * offset for the classifier cutoff which works better than 0.
         *
         * @param testSet
         *            {@link Set}<{@link Path}> Paths to reviews to classify
         * @param lexiconFile
         *            {@link Path} Path to the lexicon file
         * @return {@link Map}<{@link Path}, {@link Sentiment}> Map of calculated
         *         sentiment for each review
         * @throws IOException
         */
        public Map<Path, Sentiment> improvedClassifier(Set<Path> testSet, Path lexiconFile) throws IOException{
            Map sentiments = new HashMap<Path, Sentiment>();
            loadSentiment(lexiconFile);
            loadIntensity(lexiconFile);
            int noPositive;
            int noNegative;
            for(Path p: testSet) {
                List<String> words = Tokenizer.tokenize(p);
                noPositive = 0;
                noNegative = 0;
                for(String w: words) {
                    if(lexicon.containsKey(w)) {
                        if (lexicon.get(w) == Sentiment.POSITIVE) {
                            noPositive += ((intensity.get(w).equals(1)) ? 10: 1);

                        } else if (lexicon.get(w) == Sentiment.NEGATIVE) {
                            noNegative += ((intensity.get(w).equals(1)) ? 10: 1);

                        }
                    }
                }
                int threshold = 8;
                sentiments.put(p, ((noPositive - noNegative) >= threshold) ? Sentiment.POSITIVE: Sentiment.NEGATIVE );
            }
            return sentiments;

        }




}
