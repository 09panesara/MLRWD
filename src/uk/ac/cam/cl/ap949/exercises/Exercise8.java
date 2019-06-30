package uk.ac.cam.cl.ap949.exercises;
import uk.ac.cam.cl.mlrd.exercises.markov_models.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class Exercise8 implements IExercise8 {
    // TODO Modify to use generics instead of these specific types

    public List<DiceType> viterbi(HiddenMarkovModel<DiceRoll, DiceType> model, List<DiceRoll> observedSequence) {
        List<Map<DiceType, DiceType>> phi = new ArrayList<>(); // most probable previous hidden state for each of possible current hidden states
        List<Map<DiceType, Double>> delta = new ArrayList<>(); // path probabilities
        Map<DiceType, Map<DiceType, Double>> transitionMatrix = model.getTransitionMatrix();
        Map<DiceType, Map<DiceRoll, Double>> emissionMatrix = model.getEmissionMatrix();
        List<DiceType> hiddenStates = new ArrayList(Arrays.asList(DiceType.values()));
        hiddenStates.remove(DiceType.START);
        hiddenStates.remove(DiceType.END);
        List<DiceType> endStates = new ArrayList<>();
        endStates.add(DiceType.END);

        // start state - timestep 0
        Map<DiceType, Double> tempMap = new HashMap<>();
        Double delta_0 = emissionMatrix.get(DiceType.START).get(observedSequence.get(0));
        tempMap.put(DiceType.START, delta_0);
        delta.add(0, tempMap);
        phi.add(0, null);

        // timestep 1
        Map<DiceType, Double> delta_1 = new HashMap<>();
        Map<DiceType, DiceType> phi_1 = new HashMap<>();
        for(DiceType currDelta: hiddenStates) {  // timestep 1 - no max to calculate
            delta_1.put(currDelta, delta_0  * transitionMatrix.get(DiceType.START).get(currDelta) * emissionMatrix.get(currDelta).get(observedSequence.get(1)));
            phi_1.put(currDelta, DiceType.START);
        }
        delta.add(1, delta_1);
        phi.add(1, phi_1);

        // rest of time steps from 2 onwards
        DiceType argmax = null;
        Double currPathProb;
        Double max; // max(prob reaching state from different possible previous states)
        for(int n=2; n<observedSequence.size(); n++) {
            // for i = 1 onwards
            DiceRoll currRoll = observedSequence.get(n);
            Map<DiceType, Double> delta_n = new HashMap<>(); // delta_F, delta_L for current timestep
            Map<DiceType, DiceType> phi_n = new HashMap<>(); // from F at timestep n, best previous hidden state which maximises probability of getting F, same for L.
            for (DiceType currDelta : (n != observedSequence.size()-1 ? hiddenStates : endStates)) { // calc delta_F(observation at timestep n), delta_L(observation at timestep n)
                max = Math.log(0);
                for (DiceType prevHiddState : hiddenStates) { // each i previous state
                    if (delta.get(n - 1).get(prevHiddState) != null && delta.get(n - 1).get(prevHiddState) != 0) {
                        Double d = delta.get(n - 1).get(prevHiddState);
                        Double a = transitionMatrix.get(prevHiddState).get(currDelta);
                        Double b = (n != observedSequence.size() - 1) ? emissionMatrix.get(currDelta).get(currRoll) : emissionMatrix.get(DiceType.END).get(DiceRoll.END);
                        currPathProb = d + Math.log(a) + Math.log(b);
                        if (currPathProb > max) { // update
                            max = currPathProb;
                            argmax = prevHiddState;
                        }
                    }
                }
                // we have our argmax now
                delta_n.put(currDelta, max);
                phi_n.put(currDelta, argmax); // for delta(observed state), argmax is the hidden state at prev timestep that maximises getting to current hidden state currDelta

            }
            delta.add(n, delta_n);
            phi.add(n, phi_n);
        }

        DiceType[] finalSequence = new DiceType[observedSequence.size()];
        finalSequence[observedSequence.size()-1] = DiceType.END;
        for(int n=observedSequence.size()-1; n>=1; n--) { // backtrack
            finalSequence[n-1] =  phi.get(n).get(finalSequence[n]);
        }

        return Arrays.asList(finalSequence);
    }


    public Map<List<DiceType>, List<DiceType>> predictAll(HiddenMarkovModel<DiceRoll, DiceType> model, List<Path> testFiles) throws IOException { // TODO: Check
        Map<List<DiceType>, List<DiceType>> predictions = new HashMap<>();
        List<HMMDataStore<DiceRoll, DiceType>> data = HMMDataStore.loadDiceFiles(testFiles);
        for (HMMDataStore ds: data) {

            List<DiceRoll> observedSeq = ds.observedSequence;
            List<DiceType> hiddenSeq = ds.hiddenSequence;
            List<DiceType> prediction = viterbi(model, observedSeq);
            predictions.put(hiddenSeq, prediction);
        }
        return predictions;
    }



    public double precision(Map<List<DiceType>, List<DiceType>> true2PredictedMap) {
        double noCorrectlyPredicted = 0;
        double noPredictedL = 0; // no. predicted Loaded (i.e. WEIGHTED) states
        for (List<DiceType> realSeq: true2PredictedMap.keySet()) {
            List<DiceType> estimateSeq = true2PredictedMap.get(realSeq);
            for (int i=0; i<realSeq.size(); i++) {
                if(estimateSeq.get(i) == DiceType.WEIGHTED) {
                    noPredictedL++;
                    if(realSeq.get(i) == DiceType.WEIGHTED) {
                        noCorrectlyPredicted++;
                    }
                }
            }
        }
        double precision = noCorrectlyPredicted / noPredictedL;
        return precision;
    }


    public double recall(Map<List<DiceType>, List<DiceType>> true2PredictedMap) { // measures recall of L state
        double noCorrectlyPredicted = 0.0;
        double noTrueL = 0.0; // no. predicted Loaded (i.e. WEIGHTED) states
        for (List<DiceType> realSeq: true2PredictedMap.keySet()) {
            List<DiceType> estimateSeq = true2PredictedMap.get(realSeq);
            for (int i=0; i<realSeq.size(); i++) {
                if(realSeq.get(i) == DiceType.WEIGHTED) {
                    noTrueL++;
                    if(estimateSeq.get(i) == DiceType.WEIGHTED) {
                        noCorrectlyPredicted++;
                    }
                }
            }
        }
        Double recall = noCorrectlyPredicted / noTrueL;
        return recall;
    }



    public double fOneMeasure(Map<List<DiceType>, List<DiceType>> true2PredictedMap) {
        double precision = precision(true2PredictedMap);
        double recall = recall(true2PredictedMap);
        return ((2 * precision * recall) / (precision + recall));
    }





}