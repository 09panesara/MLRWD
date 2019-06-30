package uk.ac.cam.cl.ap949.exercises;
import edu.stanford.nlp.util.ArraySet;

import java.io.BufferedReader;
import java.nio.file.Files;
import uk.ac.cam.cl.mlrd.exercises.social_networks.IExercise11;
import uk.ac.cam.cl.mlrd.exercises.social_networks.*;

import java.io.IOException;
import java.nio.file.Path;
import java.io.BufferedReader;
import java.nio.file.Files;
import java.util.*;

public class Exercise11 implements IExercise11 {
    /**
     * Load the graph file. Use Brandes' algorithm to calculate the betweenness
     * centrality for each node in the graph.
     *
     * @param graphFile
     *            {@link Path} the path to the network specification
     * @return {@link Map}<{@link Integer}, {@link Double}> For
     *         each node, its betweenness centrality
     */

    public Map<Integer, Double> getNodeBetweenness(Path graphFile) throws IOException {
        Exercise10 implementation = new Exercise10();
        Map<Integer, Set<Integer>> neighbours = implementation.loadGraph(graphFile);
        Map<Integer, Double> nodeBetweeness = new HashMap<>();
        for (Integer s: neighbours.keySet()) {
            nodeBetweeness.put(s, 0.0);
        }
        for (Integer s: neighbours.keySet()) {
            LinkedList<Integer> stack = new LinkedList<>();
            LinkedList<Integer>[] pred = new LinkedList[neighbours.size()+1]; // predecessors
            LinkedList<Integer> queue = new LinkedList<>();
            double[] sigma = new double[neighbours.size()+1]; // +1? no. shortest paths from source to vertices in graph
            int[] dist = new int[neighbours.size()+1];
            sigma[s] = 1;
            for (int i=0; i<dist.length; i++) {
                dist[i] = -1;
            }
            dist[s]= 0;
            queue.add(s);

            while (queue.size() != 0) {
                int v = queue.poll();
                stack.push(v);
                for(int w: neighbours.get(v)) {
                    // w found for the first time? path discovery
                    if (dist[w] == -1) {
                        dist[w] = dist[v] + 1;
                        queue.add(w);
                    }
                    // shortest path to w via v? path counting
                    if (dist[w] == dist[v] + 1) { // check
                        sigma[w] = sigma[w] + sigma[v];
                        if (pred[w] == null)
                            pred[w] = new LinkedList<>();
                        pred[w].add(v);
                    }
                }

            }
            // backpropagation of dependencies
            double[] delta = new double[neighbours.size()+1]; // +1?

            for (Integer v: neighbours.keySet()) {
                delta[v] = 0;
            }

            // S returns vertices in order of non-increasing distance from s
            while (stack.size() != 0) {
                int w = stack.pop();
                if (pred[w] != null) { // if don't have empty list
                    for (int v : pred[w]) {
                        if (sigma[w] == 0) {
                            System.out.println("Here");
                        }
                        delta[v] = delta[v] + (sigma[v] / sigma[w]) * (1 + delta[w]);

                    }
                    if (w != s) {
                        nodeBetweeness.put(w, (nodeBetweeness.get(w) + delta[w]));
                    }
                }
            }



        }
        for (Integer w: nodeBetweeness.keySet()) {
            nodeBetweeness.put(w, nodeBetweeness.get(w)/2);
        }
        return nodeBetweeness;
    }

}



