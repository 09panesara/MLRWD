package uk.ac.cam.cl.ap949.exercises;

import uk.ac.cam.cl.mlrd.exercises.markov_models.*;

import java.io.IOException;
import java.util.*;

public class Exercise9 implements IExercise9 {
//    public HiddenMarkovModel<AminoAcid, Feature> estimateHMM(Collection<Path> sequenceFiles) throws IOException {
    public HiddenMarkovModel<AminoAcid, Feature> estimateHMM(List<HMMDataStore<AminoAcid, Feature>> sequencePairs)
        throws IOException {

        List<Feature> hiddenStates = Arrays.asList(Feature.values());
        List<AminoAcid> observedStates = Arrays.asList(AminoAcid.values());


        // count no. transitions from state si to state sj / count no. transitions (s = {loaded, fair})

        // initialise Sij counts matrix
        Map<Feature, java.util.Map<Feature, Double>> transitionMatrix = new HashMap<>();
        for (Feature dt: hiddenStates) {
            HashMap<Feature, Double> counts = new HashMap<>();
            for(Feature dt2: hiddenStates) {
                counts.put(dt2, 0.0); // add-one smoothing
            }
            transitionMatrix.put(dt, counts);
        }
        Map<Feature, Double> countSiTrans = new HashMap<>();
        for (Feature dt: hiddenStates) {
            countSiTrans.put(dt, 0.0);
        }

        // initialise emission counts matrix
        Map<Feature, java.util.Map<AminoAcid, Double>> emissionMatrix = new HashMap<>();
        for (Feature dt: hiddenStates) {
            HashMap<AminoAcid, Double> counts = new HashMap<>();
            for(AminoAcid dr: observedStates) {
                counts.put(dr, 0.0);
            }
            emissionMatrix.put(dt, counts);
        }
        Map<Feature, Double> countSiEmiss = new HashMap<>();
        for (Feature dt: hiddenStates) {
            countSiEmiss.put(dt, 0.0);
        }

        for (HMMDataStore ds: sequencePairs) {

            List<AminoAcid> observedSequence = ds.observedSequence;
            List<Feature> hiddenSequence = ds.hiddenSequence;
            Feature si;
            Feature sj;
            AminoAcid oi;

            for (int i=0; i<hiddenSequence.size(); i++) { // i = -1
                si = hiddenSequence.get(i);
                if (i!=hiddenSequence.size()-1) { // not in END State
                    sj = hiddenSequence.get(i + 1);
                    countSiTrans.put(si, countSiTrans.get(si)+1); // denom
                    Map<Feature, Double> s = transitionMatrix.get(si);
                    s.put(sj, s.get(sj)+1);
                    transitionMatrix.put(si, s);

                }
                oi = observedSequence.get(i); // emission matrix
                emissionMatrix.get(si).put(oi, emissionMatrix.get(si).get(oi)+1);
                countSiEmiss.put(si, countSiEmiss.get(si)+1);
            }
        }

        // calculate probabilities for transition matrix
        Map<Feature, Map<Feature, Double>> nTransitionMatrix = new HashMap<>();
        for(Feature dt: transitionMatrix.keySet()) {
            // calculate
            Map<Feature, Double> currTransitions = new HashMap<Feature, Double>(transitionMatrix.get(dt));
            for(Feature dt2: currTransitions.keySet()) {
                currTransitions.put(dt2, (countSiTrans.get(dt) != 0) ? (currTransitions.get(dt2)/countSiTrans.get(dt)) : currTransitions.get(dt2));
            }
            nTransitionMatrix.put(dt, currTransitions);
        }

        // calculate probabilities for emission matrix
        Map<Feature, Map<AminoAcid, Double>> nEmissionMatrix = new HashMap<>();
        for(Feature dt: emissionMatrix.keySet()) {
            Map<AminoAcid, Double> currEmissions = new HashMap<AminoAcid, Double>(emissionMatrix.get(dt));
            for(AminoAcid dr: currEmissions.keySet()) {
                currEmissions.put(dr, (countSiEmiss.get(dt) != 0) ? currEmissions.get(dr)/countSiEmiss.get(dt) : currEmissions.get(dr));
            }
            nEmissionMatrix.put(dt, currEmissions);
        }


        HiddenMarkovModel<AminoAcid, Feature> hmm = new HiddenMarkovModel(nTransitionMatrix, nEmissionMatrix);
        return hmm;
    }
    public List<Feature> viterbi(HiddenMarkovModel<AminoAcid, Feature> model, List<AminoAcid> observedSequence) {
        List<Map<Feature, Feature>> phi = new ArrayList<>(); // most probable previous hidden state for each of possible current hidden states
        List<Map<Feature, Double>> delta = new ArrayList<>(); // path probabilities
        Map<Feature, Map<Feature, Double>> transitionMatrix = model.getTransitionMatrix();
        Map<Feature, Map<AminoAcid, Double>> emissionMatrix = model.getEmissionMatrix();
        List<Feature> hiddenStates = new ArrayList(Arrays.asList(Feature.values()));
        hiddenStates.remove(Feature.START);
        hiddenStates.remove(Feature.END);
        List<Feature> endStates = new ArrayList<>();
        endStates.add(Feature.END);

        // start state - timestep 0
        Map<Feature, Double> tempMap = new HashMap<>();
        Double delta_0 = emissionMatrix.get(Feature.START).get(observedSequence.get(0));
        tempMap.put(Feature.START, delta_0);
        delta.add(0, tempMap);
        phi.add(0, null);

        // timestep 1
        Map<Feature, Double> delta_1 = new HashMap<>();
        Map<Feature, Feature> phi_1 = new HashMap<>();
        for(Feature currDelta: hiddenStates) {  // timestep 1 - no max to calculate
            delta_1.put(currDelta, delta_0  * transitionMatrix.get(Feature.START).get(currDelta) * emissionMatrix.get(currDelta).get(observedSequence.get(1)));
            phi_1.put(currDelta, Feature.START);
        }
        delta.add(1, delta_1);
        phi.add(1, phi_1);

        // rest of time steps from 2 onwards
        Feature argmax = null;
        Double currPathProb;
        Double max; // max(prob reaching state from different possible previous states)
        for(int n=2; n<observedSequence.size(); n++) {
            // for i = 1 onwards
            AminoAcid currAminoAcid = observedSequence.get(n);
            Map<Feature, Double> delta_n = new HashMap<>(); // delta_F, delta_L for current timestep
            Map<Feature, Feature> phi_n = new HashMap<>(); // from F at timestep n, best previous hidden state which maximises probability of getting F, same for L.
            for (Feature currDelta : (n != observedSequence.size()-1 ? hiddenStates : endStates)) { // calc delta_F(observation at timestep n), delta_L(observation at timestep n)
                max = Math.log(0);
                for (Feature prevHiddState : hiddenStates) { // each i previous state
                    if (delta.get(n - 1).get(prevHiddState) != null && delta.get(n - 1).get(prevHiddState) != 0) {
                        Double d = delta.get(n - 1).get(prevHiddState);
                        Double a = transitionMatrix.get(prevHiddState).get(currDelta);
                        Double b = (n != observedSequence.size() - 1) ? emissionMatrix.get(currDelta).get(currAminoAcid) : emissionMatrix.get(Feature.END).get(AminoAcid.END);
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

        Feature[] finalSequence = new Feature[observedSequence.size()];
        finalSequence[observedSequence.size()-1] = Feature.END;
        for(int n=observedSequence.size()-1; n>=1; n--) { // backtrack
            finalSequence[n-1] =  phi.get(n).get(finalSequence[n]);
        }

        return Arrays.asList(finalSequence);
    }


    public Map<List<Feature>, List<Feature>> predictAll(HiddenMarkovModel<AminoAcid, Feature> model, List<HMMDataStore<AminoAcid, Feature>> testSequencePairs) throws IOException {
        Map<List<Feature>, List<Feature>> predictions = new HashMap<>();
        for (HMMDataStore ds: testSequencePairs) {
            List<AminoAcid> observedSeq = ds.observedSequence;
            List<Feature> hiddenSeq = ds.hiddenSequence;
            List<Feature> prediction = viterbi(model, observedSeq);
            predictions.put(hiddenSeq, prediction);
        }
        return predictions;
    }



    public double precision(Map<List<Feature>, List<Feature>> true2PredictedMap) {
        double noCorrectlyPredicted = 0;
        double noPredictedL = 0; // no. predicted Loaded (i.e. WEIGHTED) states
        for (List<Feature> realSeq: true2PredictedMap.keySet()) {
            List<Feature> estimateSeq = true2PredictedMap.get(realSeq);
            for (int i=0; i<realSeq.size(); i++) {
                if(estimateSeq.get(i) == Feature.MEMBRANE) {
                    noPredictedL++;
                    if(realSeq.get(i) == Feature.MEMBRANE) {
                        noCorrectlyPredicted++;
                    }
                }
            }
        }
        double precision = noCorrectlyPredicted / noPredictedL;
        return precision;
    }


    public double recall(Map<List<Feature>, List<Feature>> true2PredictedMap) { // measures recall of L state
        double noCorrectlyPredicted = 0.0;
        double noTrueL = 0.0; // no. predicted Loaded (i.e. WEIGHTED) states
        for (List<Feature> realSeq: true2PredictedMap.keySet()) {
            List<Feature> estimateSeq = true2PredictedMap.get(realSeq);
            for (int i=0; i<realSeq.size(); i++) {
                if(realSeq.get(i) == Feature.MEMBRANE) {
                    noTrueL++;
                    if(estimateSeq.get(i) == Feature.MEMBRANE) {
                        noCorrectlyPredicted++;
                    }
                }
            }
        }
        Double recall = noCorrectlyPredicted / noTrueL;
        return recall;
    }



    public double fOneMeasure(Map<List<Feature>, List<Feature>> true2PredictedMap) {
        double precision = precision(true2PredictedMap);
        double recall = recall(true2PredictedMap);
        return ((2 * precision * recall) / (precision + recall));
    }
}
