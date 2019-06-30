package uk.ac.cam.cl.mlrd.testing;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

//TODO: Replace with your package.
import uk.ac.cam.cl.ap949.exercises.Exercise7;
import uk.ac.cam.cl.ap949.exercises.Exercise9;
import uk.ac.cam.cl.mlrd.exercises.markov_models.*;

public class Exercise9Tester {

    static final Path dataFile = Paths.get("data/bio_dataset.txt");

    public static void main(String[] args) throws IOException {

        List<HMMDataStore<AminoAcid, Feature>> sequencePairs = HMMDataStore.loadBioFile(dataFile);

        // Use for testing the code
//        Collections.shuffle(sequencePairs, new Random(5));
//        int testSize = sequencePairs.size() / 10;
//        List<HMMDataStore<AminoAcid, Feature>> devSet = sequencePairs.subList(0, testSize);
//        List<HMMDataStore<AminoAcid, Feature>> testSet = sequencePairs.subList(testSize, 2 * testSize);
//        List<HMMDataStore<AminoAcid, Feature>> trainingSet = sequencePairs.subList(testSize * 2, sequencePairs.size());
//        // But:
//        // TODO: Replace with cross-validation for the tick.
//
        IExercise9 implementation = (IExercise9) new Exercise9();
//
//        HiddenMarkovModel<AminoAcid, Feature> model = implementation.estimateHMM(trainingSet);
//        System.out.println("Predicted transitions:");
//        System.out.println(model.getTransitionMatrix());
//        System.out.println();
//        System.out.println("Predicted emissions:");
//        System.out.println(model.getEmissionMatrix());
//        System.out.println();
//
//        HMMDataStore<AminoAcid, Feature> data = devSet.get(0);
//        List<Feature> predicted = implementation.viterbi(model, data.observedSequence);
//        System.out.println("True hidden sequence:");
//        System.out.println(data.hiddenSequence);
//        System.out.println();
//
//        System.out.println("Predicted hidden sequence:");
//        System.out.println(predicted);
//        System.out.println();
//
//        Map<List<Feature>, List<Feature>> true2PredictedSequences = implementation.predictAll(model, devSet);
//        double accuracy = implementation.precision(true2PredictedSequences);
//        System.out.println("Prediction precision:");
//        System.out.println(accuracy);
//        System.out.println();
//
//        double recall = implementation.recall(true2PredictedSequences);
//        System.out.println("Prediction recall:");
//        System.out.println(recall);
//        System.out.println();
//
//        double f1Score = implementation.fOneMeasure(true2PredictedSequences);
//        System.out.println("Prediction F1 score:");
//        System.out.println(f1Score);
//        System.out.println();
//
//
//
        //////
        // Use for testing the code

        IExercise7 implementation7 = (IExercise7) new Exercise7();
        Exercise8Tester implementation8T = new Exercise8Tester();
        Collections.shuffle(sequencePairs, new Random(0));

        List<HMMDataStore<AminoAcid, Feature>> validationSet = new ArrayList<HMMDataStore<AminoAcid, Feature>>();
        int testSize = sequencePairs.size() / 10;
        double[] cvPrecision = new double[10];
        double[] cvRecall = new double[10];
        double[] cvF1 = new double[10];

        for (int i=0; i<10; i++) {
            // loop through folds, set ith index to validationSet, rest to trainingSet
            List<HMMDataStore<AminoAcid, Feature>> devSet = new ArrayList<>();
            for(int j=0; j<10; j++) {
                if (i == j) {
                    validationSet = sequencePairs.subList(j*testSize, testSize*(j+1));
                } else {
                    List<HMMDataStore<AminoAcid, Feature>> tmp = sequencePairs.subList(j*testSize, testSize*(j+1));
                    for (HMMDataStore hds: tmp) {
                        devSet.add(hds);
                    }
                }
            }
            HiddenMarkovModel<AminoAcid, Feature> model = implementation.estimateHMM(devSet);


            HMMDataStore<AminoAcid, Feature> data = devSet.get(0);
            List<Feature> predicted = implementation.viterbi(model, data.observedSequence);
            Map<List<Feature>, List<Feature>> true2PredictedMap = implementation.predictAll(model, devSet);
            double precision = implementation.precision(true2PredictedMap);
            cvPrecision[i] = precision;


            double recall = implementation.recall(true2PredictedMap);
            cvRecall[i] = recall;

            double fOneMeasure = implementation.fOneMeasure(true2PredictedMap);
            cvF1[i] = fOneMeasure;

        }

        double avgPrecision = implementation8T.cv(cvPrecision);
        double avgRecall = implementation8T.cv(cvRecall);
        double cvFOneMeasure = ((2 * avgPrecision * avgRecall) / (avgPrecision + avgRecall));
        System.out.println("Prediction precision:");
        System.out.println(avgPrecision);
        System.out.println();
        System.out.println("Prediction recall:");
        System.out.println(avgRecall);
        System.out.println();
        System.out.println("Prediction fOneMeasure using set of precision and recalls:");
        System.out.println(cvFOneMeasure);
        System.out.println();
        System.out.println("Prediction fOneMeasure using average of fOneMeasures for individual test sets");
        System.out.println(implementation8T.cv(cvF1));
        System.out.println();




    }
}