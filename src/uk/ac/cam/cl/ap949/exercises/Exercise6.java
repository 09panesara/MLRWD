package uk.ac.cam.cl.ap949.exercises;

import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise6;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Sentiment;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.NuancedSentiment;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Tokenizer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class Exercise6 implements IExercise6 {

    public Map<NuancedSentiment, Double> calculateClassProbabilities(Map<Path, NuancedSentiment> trainingSet) throws IOException {
        // calculates P(Pos), P(Neg)
        Map<NuancedSentiment, Double> classProbs = new HashMap<NuancedSentiment, Double>();
        double noPosClasses = 0;
        double noNegClasses = 0;
        double noNeutralClasses = 0;
        NuancedSentiment currSentiment;

        for(Path p: trainingSet.keySet()) {
            currSentiment = trainingSet.get(p);
            if(currSentiment == NuancedSentiment.POSITIVE) { // need to check this works
                noPosClasses++;
            } else if (currSentiment == NuancedSentiment.NEGATIVE){
                noNegClasses++;
            } else { // neutral
                noNeutralClasses++;
            }
        }

        double total = noPosClasses + noNegClasses + noNeutralClasses;
        classProbs.put(NuancedSentiment.POSITIVE, (noPosClasses/total));
        classProbs.put(NuancedSentiment.NEGATIVE, (noNegClasses/total));
        classProbs.put(NuancedSentiment.NEUTRAL, (noNeutralClasses/total));

        return classProbs;
    }

    public Map<String, Map<NuancedSentiment, Integer>> calculateNumerator(Map<Path, NuancedSentiment> trainingSet) throws IOException {
        Map<String, Map<NuancedSentiment, Integer>> wordCount = new HashMap<>();
        List<NuancedSentiment> sentiments = new ArrayList<>();
        sentiments.add(NuancedSentiment.POSITIVE);
        sentiments.add(NuancedSentiment.NEGATIVE);
        sentiments.add(NuancedSentiment.NEUTRAL);


        for (Path p : trainingSet.keySet()) {
            List<String> reviewWords = Tokenizer.tokenize(p);
            Map<NuancedSentiment, Integer> count;
            NuancedSentiment currSentiment = trainingSet.get(p);
            for(String word: reviewWords) {
                if(wordCount.containsKey(word)) {
                    count = wordCount.get(word);
                    count.put(currSentiment, count.get(currSentiment)+1);
                    wordCount.put(word, count);
                } else {
                    count = new HashMap<>();
                    count.put(currSentiment, 2);
                    for (int i=0; i<3; i++) {
                        if (sentiments.get(i) != currSentiment) {
                            count.put(sentiments.get(i), 1);
                        }
                    }
                    wordCount.put(word, count);
                }
            }

        }
        return wordCount;
    }



    public Map<NuancedSentiment, Integer> calculateDenominator(Map<String, Map<NuancedSentiment, Integer>> wordCount) throws IOException {
        Map<NuancedSentiment, Integer> denom = new HashMap<>();
        int countPos = 0;
        int countNeg = 0;
        int countNeut = 0;
        for (String word: wordCount.keySet()) {
            countPos += wordCount.get(word).get(NuancedSentiment.POSITIVE);
            countNeg += wordCount.get(word).get(NuancedSentiment.NEGATIVE);
            countNeut += wordCount.get(word).get(NuancedSentiment.NEUTRAL);
        }
        denom.put(NuancedSentiment.POSITIVE, countPos);
        denom.put(NuancedSentiment.NEGATIVE, countNeg);
        denom.put(NuancedSentiment.NEUTRAL, countNeut);
        return denom;
    }

    public Map<String, Map<NuancedSentiment, Double>> calculateNuancedLogProbs(Map<Path, NuancedSentiment> trainingSet)
            throws IOException {

        Map<String, Map<NuancedSentiment, Integer>> wordCount = calculateNumerator(trainingSet);

        double posProb;
        double negProb;
        double neutProb;

        Map<NuancedSentiment, Integer> denominator = calculateDenominator(wordCount);

        Map<String, Map<NuancedSentiment, Double>> logProbs= new HashMap<>();
        Map<NuancedSentiment, Double> logProbsVal;

        // am I looping through each word and checking no. times it appears in positive
        for (String word: wordCount.keySet()) {
            // calculate P(word | POS)
            posProb = wordCount.get(word).get(NuancedSentiment.POSITIVE);
            posProb = posProb/denominator.get(NuancedSentiment.POSITIVE);
            posProb = Math.log(posProb);
            // calculate P(word | NEG)
            negProb = wordCount.get(word).get(NuancedSentiment.NEGATIVE);
            negProb = negProb/denominator.get(NuancedSentiment.NEGATIVE);
            negProb = Math.log(negProb);
            // calculate P(word | NEUTRAL)
            neutProb = wordCount.get(word).get(NuancedSentiment.NEUTRAL);
            neutProb = neutProb/denominator.get(NuancedSentiment.NEUTRAL);
            neutProb = Math.log(neutProb);

            logProbsVal = new HashMap<>();
            logProbsVal.put(NuancedSentiment.POSITIVE, posProb);
            logProbsVal.put(NuancedSentiment.NEGATIVE, negProb);
            logProbsVal.put(NuancedSentiment.NEUTRAL, neutProb);
            logProbs.put(word, logProbsVal);

        }
        return logProbs;
    }


    public 	Map<Path, NuancedSentiment> nuancedClassifier(Set<Path> testSet, Map<String, Map<NuancedSentiment, Double>> tokenLogProbs, Map<NuancedSentiment, Double> classProbabilities)
            throws IOException {
        Map<Path, NuancedSentiment> results = new HashMap<>();
        double p_pos;
        double p_neg;
        double p_neut;

        List<String> reviewWords;
        for(Path p: testSet) {
            // loop over words in review at path p and find argmax [ (logP(c) + sum_across_words_w__i(logP(w__i|c) ]
            reviewWords = Tokenizer.tokenize(p);
            p_pos = Math.log(classProbabilities.get(NuancedSentiment.POSITIVE));
            p_neg = Math.log(classProbabilities.get(NuancedSentiment.NEGATIVE));
            p_neut = Math.log(classProbabilities.get(NuancedSentiment.NEUTRAL));
            for(String word: reviewWords) {
                if(tokenLogProbs.get(word) != null) {
                    p_pos += tokenLogProbs.get(word).get(NuancedSentiment.POSITIVE);
                    p_neg += tokenLogProbs.get(word).get(NuancedSentiment.NEGATIVE);
                    p_neut += tokenLogProbs.get(word).get(NuancedSentiment.NEUTRAL);
                }
            }
            if (p_pos > p_neg) { // pick between p_pos and p_neut
                results.put(p, (p_pos > p_neut ? NuancedSentiment.POSITIVE : NuancedSentiment.NEUTRAL));
            } else { // pick between p_neg and p_neut
                results.put(p, (p_neg > p_neut ? NuancedSentiment.NEGATIVE : NuancedSentiment.NEUTRAL));
            }
        }
        return results;
    }

    public double nuancedAccuracy(Map<Path, NuancedSentiment> trueSentiments,
                                  Map<Path, NuancedSentiment> predictedSentiments) {
        double accuracy = 0;
        int noFiles = 0;
        for(Map.Entry<Path, NuancedSentiment> entry : predictedSentiments.entrySet()) {
            Path key = entry.getKey();
            NuancedSentiment value = entry.getValue();
            if(value.equals(trueSentiments.get(key))) {
                accuracy++;
            }
            noFiles++;
        }
        accuracy = accuracy/noFiles;
        return accuracy;
    }



    // input - review1, review2, review3, review4
    public Map<Integer, Map<Sentiment, Integer>> agreementTable(Collection<Map<Integer, Sentiment>> predictedSentiments) {
        Map<Integer, Map<Sentiment, Integer>> agreementTable = new HashMap<>();
        for (Map<Integer, Sentiment> m: predictedSentiments) {
            for (Integer reviewNo: m.keySet()) {
                Sentiment sentiment = m.get(reviewNo);
                if (agreementTable.get(reviewNo) == null) {
                    Map<Sentiment, Integer> currReview = new HashMap<>();
                    currReview.put(Sentiment.NEGATIVE, (sentiment==Sentiment.NEGATIVE) ? 1: 0);
                    currReview.put(Sentiment.POSITIVE, (sentiment==Sentiment.POSITIVE) ? 1: 0);
                    agreementTable.put(reviewNo, currReview);
                } else {
                    Map<Sentiment, Integer> currReview = new HashMap<>(agreementTable.get(reviewNo));
                    currReview.put(sentiment, currReview.get(sentiment)+1);
                    agreementTable.put(reviewNo, currReview);
                }
            }

        }
        return agreementTable;
    }

    public double kappa(Map<Integer, Map<Sentiment, Integer>> agreementTable) {
        double kappa = 0;
        // calc P(A)
        double n = 0;
        double observedPairAgr = 0;
        double possiblePairAgr = 0;
        double noPos = 0;
        double noNeg = 0;
        double prob_a = 0;
        for (Integer i: agreementTable.keySet()) {
            noNeg = (agreementTable.get(i).get(Sentiment.NEGATIVE) != null) ? (agreementTable.get(i).get(Sentiment.NEGATIVE)) : 0;
            noPos = (agreementTable.get(i).get(Sentiment.POSITIVE) != null) ? (agreementTable.get(i).get(Sentiment.POSITIVE)) : 0;
            n = noNeg + noPos;
            observedPairAgr = (noNeg*(noNeg-1))/2 + (noPos*(noPos-1))/2;
            possiblePairAgr = (n*(n-1))/2;
            prob_a += (observedPairAgr/possiblePairAgr);
        }
        prob_a = prob_a/agreementTable.size();

        // calc P(E)
        noPos = 0;
        noNeg = 0;
        for (Integer i: agreementTable.keySet()) {
            noPos += ((agreementTable.get(i).get(Sentiment.POSITIVE) != null) ? (agreementTable.get(i).get(Sentiment.POSITIVE)) : 0);
            noNeg += ((agreementTable.get(i).get(Sentiment.NEGATIVE) != null) ? (agreementTable.get(i).get(Sentiment.NEGATIVE)) : 0);
        }
        double prop_pos = noPos/(noPos+noNeg);
        double neg_pos = noNeg/(noPos+noNeg);
        double prob_e = (Math.pow(prop_pos, 2)) + (Math.pow(neg_pos, 2));
        kappa = (prob_a - prob_e) / (1- prob_e);
        return kappa;

    }
}
