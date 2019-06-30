package uk.ac.cam.cl.mlrd.testing;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import uk.ac.cam.cl.ap949.exercises.Exercise7;
import uk.ac.cam.cl.ap949.exercises.Exercise8;
import uk.ac.cam.cl.mlrd.exercises.markov_models.DiceRoll;
import uk.ac.cam.cl.mlrd.exercises.markov_models.DiceType;
import uk.ac.cam.cl.mlrd.exercises.markov_models.HMMDataStore;
import uk.ac.cam.cl.mlrd.exercises.markov_models.HiddenMarkovModel;
import uk.ac.cam.cl.mlrd.exercises.markov_models.IExercise7;
import uk.ac.cam.cl.mlrd.exercises.markov_models.IExercise8;


public class Exercise8Tester {

    static final Path dataDirectory = Paths.get("data/dice_dataset");

    public double cv(double[] scores) {
        double avg = 0;
        for (int i=0; i<scores.length; i++) {
            avg += scores[i];
        }
        return (avg/scores.length);
    }

    public static void main(String[] args)
            throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {

        List<Path> sequenceFiles = new ArrayList<>();
        try (DirectoryStream<Path> files = Files.newDirectoryStream(dataDirectory)) {
            for (Path item : files) {
                sequenceFiles.add(item);
            }
        } catch (IOException e) {
            throw new IOException("Cant access the dataset.", e);
        }

        // Use for testing the code
        IExercise8 implementation = (IExercise8) new Exercise8();

        IExercise7 implementation7 = (IExercise7) new Exercise7();
        Exercise8Tester implementation8T = new Exercise8Tester();
        Collections.shuffle(sequenceFiles, new Random(0));

        List<Path> validationSet = new ArrayList<Path>();
        int testSize = sequenceFiles.size() / 10;
        double[] cvPrecision = new double[10];
        double[] cvRecall = new double[10];
        double[] cvF1 = new double[10];

        for (int i=0; i<10; i++) {
            // loop through folds, set ith index to validationSet, rest to trainingSet
            List<Path> devSet = new ArrayList<>();
            for(int j=0; j<10; j++) {
                if (i == j) {
                    validationSet = sequenceFiles.subList(j*testSize, testSize*(j+1));
                } else {
                    List<Path> tmp = sequenceFiles.subList(j*testSize, testSize*(j+1));
                    for (Path p: tmp) {
                        devSet.add(p);
                    }
                }
            }
            HiddenMarkovModel<DiceRoll, DiceType> model = implementation7.estimateHMM(devSet);


            HMMDataStore<DiceRoll, DiceType> data = HMMDataStore.loadDiceFile(devSet.get(0));
            List<DiceType> predicted = implementation.viterbi(model, data.observedSequence);

            Map<List<DiceType>, List<DiceType>> true2PredictedMap = implementation.predictAll(model, devSet);
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