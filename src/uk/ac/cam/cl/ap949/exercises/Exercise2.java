package uk.ac.cam.cl.ap949.exercises;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise2;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Sentiment;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import java.util.Set;

import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Tokenizer;


public class Exercise2 implements IExercise2{

    public Map<Sentiment, Double> calculateClassProbabilities(Map<Path, Sentiment> trainingSet) throws IOException {
        // calculates P(Pos), P(Neg)
        Map<Sentiment, Double> classProbs = new HashMap<Sentiment, Double>();
        double noPosClasses = 0;
        double noNegClasses = 0;

        for(Path p: trainingSet.keySet()) {
            if(trainingSet.get(p) == Sentiment.POSITIVE) { // need to check this works
                noPosClasses++;
            } else {
                noNegClasses++;
            }

        }

        classProbs.put(Sentiment.POSITIVE, (noPosClasses/(noPosClasses + noNegClasses)));
        classProbs.put(Sentiment.NEGATIVE, (noNegClasses/(noPosClasses + noNegClasses)));
        return classProbs;
    }

    public Map<String, Map<Sentiment, Integer>> calculateNumerator(boolean smoothed, Map<Path, Sentiment> trainingSet) throws IOException {
        Map<String, Map<Sentiment, Integer>> wordCount = new HashMap<>();
        for (Path p : trainingSet.keySet()) {
            List<String> reviewWords = Tokenizer.tokenize(p);
            Map<Sentiment, Integer> count;
            Sentiment sentiment = trainingSet.get(p);
            for(String word: reviewWords) {

                if(wordCount.containsKey(word)) {
                    count = wordCount.get(word);
                    count.put(sentiment, count.get(sentiment)+1);
                    wordCount.put(word, count);
                } else {
                    count = new HashMap<>();
                    count.put(sentiment, smoothed ? 2 : 1);
                    count.put(sentiment == Sentiment.NEGATIVE ? Sentiment.POSITIVE : Sentiment.NEGATIVE, smoothed ? 1 : 0); // makes sure each word has a count for both sentiments
                    wordCount.put(word, count);
                }
            }

        }
        return wordCount;
    }

    public Map<Sentiment, Integer> calculateDenominator(Map<String, Map<Sentiment, Integer>> wordCount) throws IOException {
        Map<Sentiment, Integer> denom = new HashMap<>();
        int countPos = 0;
        int countNeg = 0;
        for (String word: wordCount.keySet()) {
            countPos += wordCount.get(word).get(Sentiment.POSITIVE);
            countNeg += wordCount.get(word).get(Sentiment.NEGATIVE);
        }
        denom.put(Sentiment.POSITIVE, countPos);
        denom.put(Sentiment.NEGATIVE, countNeg);
        return denom;
    }

    public Map<String, Map<Sentiment, Double>> calcLogProbsHelper(Map<String, Map<Sentiment, Integer>> wordCount) throws IOException {
        double posProb;
        double negProb;
        Map<Sentiment, Integer> denominator = calculateDenominator(wordCount);

        Map<String, Map<Sentiment, Double>> logProbs= new HashMap<>();
        Map<Sentiment, Double> logProbsVal;

        // am I looping through each word and checking no. times it appears in positive
        for (String word: wordCount.keySet()) {
            // calculate P(word | POS)
            posProb = wordCount.get(word).get(Sentiment.POSITIVE);
            posProb = posProb/denominator.get(Sentiment.POSITIVE);
            posProb = Math.log(posProb);
            // calculate P(word | NEG)
            negProb = wordCount.get(word).get(Sentiment.NEGATIVE);
            negProb = negProb/denominator.get(Sentiment.NEGATIVE);
            negProb = Math.log(negProb);
            logProbsVal = new HashMap<>();
            logProbsVal.put(Sentiment.POSITIVE, posProb);
            logProbsVal.put(Sentiment.NEGATIVE, negProb);
            logProbs.put(word, logProbsVal);

        }
        return logProbs;
    }

    public Map<String, Map<Sentiment, Double>> calculateUnsmoothedLogProbs(Map<Path, Sentiment> trainingSet) throws IOException {
        // count no. words in class/total number of words in the class
        // get dataset
        Map<String, Map<Sentiment, Integer>> wordCount = calculateNumerator(false, trainingSet);
        return calcLogProbsHelper(wordCount);
    }

    public Map<String, Map<Sentiment, Double>> calculateSmoothedLogProbs(Map<Path, Sentiment> trainingSet) throws IOException {
        // count no. words in class/total number of words in the class
        // get dataset
        Map<String, Map<Sentiment, Integer>> wordCount = calculateNumerator(true, trainingSet);
        return calcLogProbsHelper(wordCount);
    }

    public Map<Path, Sentiment> naiveBayes(Set<Path> testSet, Map<String, Map<Sentiment, Double>> tokenLogProbs,
                                           Map<Sentiment, Double> classProbabilities) throws IOException {
        Map<Path, Sentiment> results = new HashMap<>();
        double p_pos;
        double p_neg;
        List<String> reviewWords;
        for(Path p: testSet) {
            // loop over words in review at path p and find argmax [ (logP(c) + sum_across_words_w__i(logP(w__i|c) ]
            reviewWords = Tokenizer.tokenize(p);
            p_pos = Math.log(classProbabilities.get(Sentiment.POSITIVE));
            p_neg = Math.log(classProbabilities.get(Sentiment.NEGATIVE));
            for(String word: reviewWords) {
                if(tokenLogProbs.get(word) != null) {
                    p_pos += tokenLogProbs.get(word).get(Sentiment.POSITIVE);
                    p_neg += tokenLogProbs.get(word).get(Sentiment.NEGATIVE);
                }
            }
            results.put(p, (p_pos >= p_neg) ? Sentiment.POSITIVE : Sentiment.NEGATIVE);
        }
        return results;
    }


}
