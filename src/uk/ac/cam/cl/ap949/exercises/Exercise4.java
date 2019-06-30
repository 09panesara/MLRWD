package uk.ac.cam.cl.ap949.exercises;

import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise4;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Sentiment;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Tokenizer;


import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.math.BigInteger;
import java.math.BigDecimal;

public class Exercise4 implements IExercise4 {
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

    public Map<Path, Sentiment> magnitudeClassifier(Set<Path> testSet, Path lexiconFile) throws IOException {
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
                        noPositive += ((intensity.get(w).equals(1)) ? 2: 1); // 10

                    } else if (lexicon.get(w) == Sentiment.NEGATIVE) {
                        noNegative += ((intensity.get(w).equals(1)) ? 2: 1); // 10

                    }
                }
            }
            int threshold = 0;
            sentiments.put(p, ((noPositive - noNegative) >= threshold) ? Sentiment.POSITIVE: Sentiment.NEGATIVE );
        }
        return sentiments;
    }

    public static BigDecimal getFactorial(BigDecimal num) {
        if (num.intValue() == 0) return BigDecimal.valueOf(1);

        if (num.intValue() == 1) return BigDecimal.valueOf(1);

        return num.multiply(getFactorial(num.subtract(BigDecimal.valueOf(1))));
    }


    public double signTest(Map<Path, Sentiment> actualSentiments, Map<Path, Sentiment> classificationA,
                           Map<Path, Sentiment> classificationB) {
//        BigInteger Plus = BigInteger.ZERO; // no. times System 1 is better than System 2
//        BigInteger Minus = BigInteger.ZERO; // no. times System 2 is better than System 1
//        BigInteger Null = BigInteger.ZERO;
        double Plus = 0;
        double Minus = 0;
        double Null = 0;
        Sentiment correctSentiment;
        int count = 0;
        int no_samples = 500;
        for(Path p: actualSentiments.keySet()) {
            if (count == no_samples)
                break;
            else
                count++;
            correctSentiment = actualSentiments.get(p);
            if(classificationA.get(p) == correctSentiment) {
                if(classificationB.get(p) != correctSentiment) { // A is better than B
                    Plus++;
                } else { // both are correct
                    Null++;
                }
            } else { // A is incorrect
                if (classificationB.get(p) == correctSentiment) { // B is better than A
                    Minus++;
                } else { // A and B are both incorrect
                    Null++;
                }
            }
        }

        double n = 2*Math.ceil(Null/2) + Plus + Minus;
        BigDecimal nBigDec = BigDecimal.valueOf((long) n);
        double k = Math.ceil(Null/2) + (Plus > Minus ? Minus : Plus);

        BigDecimal iBigInt = BigDecimal.ZERO;
        BigDecimal factorial = BigDecimal.ZERO;
        BigDecimal sum = BigDecimal.ZERO;
        double q = 0.5;

        for(int i=0; i<k+1; i++) {
            iBigInt = BigDecimal.valueOf((long) i);
            factorial = getFactorial(nBigDec).divide(getFactorial(iBigInt).multiply(getFactorial(nBigDec.subtract(iBigInt))));
            BigDecimal firstPart = BigDecimal.valueOf(Math.pow(q, i));
            BigDecimal secondPart = BigDecimal.valueOf(Math.pow(1-q, n-i));
            BigDecimal temp = factorial.multiply(firstPart.multiply(secondPart));
            sum = sum.add(temp);
        }
        sum = sum.multiply(BigDecimal.valueOf(2));
        return sum.doubleValue();

    }
}
