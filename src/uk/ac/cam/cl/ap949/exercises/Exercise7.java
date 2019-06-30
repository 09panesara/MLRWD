package uk.ac.cam.cl.ap949.exercises;
import uk.ac.cam.cl.mlrd.exercises.markov_models.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class Exercise7 implements IExercise7 {

    public HiddenMarkovModel<DiceRoll, DiceType> estimateHMM(Collection<Path> sequenceFiles) throws IOException {

        List<HMMDataStore<DiceRoll, DiceType>> data = HMMDataStore.loadDiceFiles(sequenceFiles);

        List<DiceType> hiddenStates = Arrays.asList(DiceType.values()); // S, F, W, E
        List<DiceRoll> observedStates = Arrays.asList(DiceRoll.values()); // S, 1, 2, 3, 4, 5, 6, E

        int noHiddenStates = hiddenStates.size(); // 4
        int noObservStates = observedStates.size(); // 8

        // count no. transitions from state si to state sj / count no. transitions (s = {loaded, fair})

        // initialise Sij counts matrix
        Map<DiceType, java.util.Map<DiceType, Double>> transitionMatrix = new HashMap<>();
        for (DiceType dt: hiddenStates) {
            HashMap<DiceType, Double> counts = new HashMap<>();
            for(DiceType dt2: hiddenStates) {
                counts.put(dt2, 0.0); // add-one smoothing
            }
            transitionMatrix.put(dt, counts);
        }
        Map<DiceType, Double> countSiTrans = new HashMap<>();
        for (DiceType dt: hiddenStates) {
            countSiTrans.put(dt, 0.0);
        }

        // initialise emission counts matrix
        Map<DiceType, java.util.Map<DiceRoll, Double>> emissionMatrix = new HashMap<>();
        for (DiceType dt: hiddenStates) {
            HashMap<DiceRoll, Double> counts = new HashMap<>();
            for(DiceRoll dr: observedStates) {
                counts.put(dr, 0.0);
            }
            emissionMatrix.put(dt, counts);
        }
        Map<DiceType, Double> countSiEmiss = new HashMap<>();
        for (DiceType dt: hiddenStates) {
            countSiEmiss.put(dt, 0.0);
        }

        for (HMMDataStore ds: data) {

            List<DiceRoll> observedSequence = ds.observedSequence;
            List<DiceType> hiddenSequence = ds.hiddenSequence;
            DiceType si;
            DiceType sj;
            DiceRoll oi;

            for (int i=0; i<hiddenSequence.size(); i++) { // i = -1
                si = hiddenSequence.get(i);
                if (i!=hiddenSequence.size()-1) { // not in END State
                    sj = hiddenSequence.get(i + 1);
                    countSiTrans.put(si, countSiTrans.get(si)+1); // denom
                    Map<DiceType, Double> s = transitionMatrix.get(si);
                    s.put(sj, s.get(sj)+1);
                    transitionMatrix.put(si, s);

                }
                oi = observedSequence.get(i); // emission matrix
                emissionMatrix.get(si).put(oi, emissionMatrix.get(si).get(oi)+1);
                countSiEmiss.put(si, countSiEmiss.get(si)+1);
            }
        }

        // calculate probabilities for transition matrix
        Map<DiceType, Map<DiceType, Double>> nTransitionMatrix = new HashMap<>();
        for(DiceType dt: transitionMatrix.keySet()) {
            // calculate
            Map<DiceType, Double> currTransitions = new HashMap<DiceType, Double>(transitionMatrix.get(dt));
            for(DiceType dt2: currTransitions.keySet()) {
                currTransitions.put(dt2, (countSiTrans.get(dt) != 0) ? (currTransitions.get(dt2)/countSiTrans.get(dt)) : currTransitions.get(dt2));
            }
            nTransitionMatrix.put(dt, currTransitions);
        }

        // calculate probabilities for emission matrix
        Map<DiceType, Map<DiceRoll, Double>> nEmissionMatrix = new HashMap<>();
        for(DiceType dt: emissionMatrix.keySet()) {
            Map<DiceRoll, Double> currEmissions = new HashMap<DiceRoll, Double>(emissionMatrix.get(dt));
            for(DiceRoll dr: currEmissions.keySet()) {
                currEmissions.put(dr, (countSiEmiss.get(dt) != 0) ? currEmissions.get(dr)/countSiEmiss.get(dt) : currEmissions.get(dr));
            }
            nEmissionMatrix.put(dt, currEmissions);
        }


        HiddenMarkovModel<DiceRoll, DiceType> hmm = new HiddenMarkovModel(nTransitionMatrix, nEmissionMatrix);
        return hmm;
    }
}
