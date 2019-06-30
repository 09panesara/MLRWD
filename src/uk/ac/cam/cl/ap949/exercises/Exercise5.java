package uk.ac.cam.cl.ap949.exercises;

import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Collections;
import java.util.Random;


public class Exercise5 implements IExercise5 {

    public List<Map<Path, Sentiment>> splitCVRandom(Map<Path, Sentiment> dataSet, int seed) {
        List<Path> keys = new ArrayList<>(dataSet.keySet());
        Collections.shuffle(keys, new Random(seed));
        List<Map<Path, Sentiment>> folds = new ArrayList<>();
        for (int i=0; i<10; i++) {
            Map<Path, Sentiment> fold = new HashMap<>();
            for (int j=i; j<keys.size(); j+=10) {
                fold.put(keys.get(j), dataSet.get(keys.get(j)));
            }
            folds.add(fold);
        }

        return folds;

    }

    public List<Map<Path, Sentiment>> splitCVStratifiedRandom(Map<Path, Sentiment> dataSet, int seed) {
        List<Path> posReviews = new ArrayList<>();
        List<Path> negReviews = new ArrayList<>();

        for (Path p: dataSet.keySet()) { // split reviews into pos and neg
            if (dataSet.get(p) == Sentiment.POSITIVE) {
                posReviews.add(p);
            } else {
                negReviews.add(p);
            }
        }

        // randomly shuffle positive and negative reviews
        Collections.shuffle(posReviews, new Random(seed));
        Collections.shuffle(negReviews, new Random(seed));

        List<Map<Path, Sentiment>> folds = new ArrayList<>();
        for (int i=0; i<10; i++) {
            Map<Path, Sentiment> fold = new HashMap<>();
            for (int j=i; j<posReviews.size(); j+=10) {
                fold.put(posReviews.get(j), dataSet.get(posReviews.get(j)));
            }
            for (int j=i; j<negReviews.size(); j+=10) {
                fold.put(negReviews.get(j), dataSet.get(negReviews.get(j)));
            }
            folds.add(fold);
            int countPos = 0;
            int countNeg = 0;

            for (Path p: fold.keySet()) {
                if (fold.get(p) == Sentiment.POSITIVE)
                    countPos++;
                else
                    countNeg++;
            }
        }

        return folds;

    }

    public double[] crossValidate(List<Map<Path, Sentiment>> folds) throws IOException {
        double[] crossVal = new double[10];

        Exercise1 ex1 = new Exercise1(); // for calculating accuracy
        Exercise2 ex2 = new Exercise2(); // for training Naive Bayes classifier

        Map<Path, Sentiment> validationSet = new HashMap<>();

        for (int i=0; i<10; i++) {
            // loop through folds, set ith index to validationSet, rest to trainingSet
            Map<Path, Sentiment> trainingSet = new HashMap<>();
            for(int j=0; j<10; j++) {
                if (i == j) {
                    validationSet = new HashMap(folds.get(j));
                } else {
                    Map<Path, Sentiment> tmp = new HashMap(folds.get(j));
                    for (Path p: tmp.keySet()) {
                        trainingSet.put(p, tmp.get(p));
                    }
                }
            }

            Map<Sentiment, Double> classProbabilities = ex2.calculateClassProbabilities(trainingSet);

            // Without smoothing
            Map<String, Map<Sentiment, Double>> logProbs = ex2.calculateSmoothedLogProbs(trainingSet);
            Map<Path, Sentiment> NBPredictions = ex2.naiveBayes(validationSet.keySet(), logProbs,
                    classProbabilities);

            double NBAccuracy = ex1.calculateAccuracy(validationSet, NBPredictions);
            crossVal[i] = NBAccuracy;
        }

        return crossVal;
    }

    public double cvAccuracy(double[] scores) {
        double avg = 0;
        for (int i=0; i<scores.length; i++) {
            avg += scores[i];
        }
        return (avg/scores.length);
    }


    public double cvVariance(double[] scores) {
        double var = 0;
        double mean = cvAccuracy(scores);

        for (int i=0; i<scores.length; i++) {
            var += Math.pow((scores[i]-mean), 2);
        }
        var = var/scores.length;
        return var;
    }

}
