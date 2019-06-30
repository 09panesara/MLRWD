package uk.ac.cam.cl.ap949.exercises;

import edu.stanford.nlp.util.ArraySet;
import uk.ac.cam.cl.mlrd.exercises.social_networks.IExercise10;

import java.io.IOException;
import java.nio.file.Path;
import java.io.BufferedReader;
import java.nio.file.Files;
import java.util.*;


public class Exercise10 implements IExercise10 {
    /**
     * Load the graph file. Each line in the file corresponds to an edge; the
     * first column is the source node and the second column is the target. As
     * the graph is undirected, your program should add the source as a
     * neighbour of the target as well as the target a neighbour of the source.
     *
     * @param graphFile
     *            {@link Path} the path to the network specification
     * @return {@link Map}<{@link Integer}, {@link Set}<{@link Integer}>> For
     *         each node, all the nodes neighbouring that node
     */
    public Map<Integer, Set<Integer>> loadGraph(Path graphFile) throws IOException {
        Map<Integer, Set<Integer>> neighbours = new HashMap<>();
        try (BufferedReader reader = Files.newBufferedReader(graphFile)) {
            reader.lines().forEach(line -> {
                String[] tokens = line.split("\\s+");
                int source = Integer.parseInt(tokens[0]);
                int target = Integer.parseInt(tokens[1]);
                if(!neighbours.containsKey(source)) {
                    neighbours.put(source, new ArraySet<>());
                }
                neighbours.get(source).add(target);
                if(!neighbours.containsKey(target)) {
                    neighbours.put(target, new ArraySet<>());
                }
                neighbours.get(target).add(source);
            });
        } catch (IOException e) {
            throw new IOException("Can't load the graph file.", e);
        }
        return neighbours;
    }

    /**
     * Find the number of neighbours for each point in the graph.
     *
     * @param graph
     *            {@link Map}<{@link Integer}, {@link Set}<{@link Integer}>> The
     *            loaded graph
     * @return {@link Map}<{@link Integer}, {@link Integer}> For each node, the
     *         number of neighbours it has
     */
    public Map<Integer, Integer> getConnectivities(Map<Integer, Set<Integer>> graph) {
        Map<Integer, Integer> connectivities = new HashMap<>();
        for (Integer node: graph.keySet()) {
            int countNeighbours = graph.get(node).size();
            connectivities.put(node, countNeighbours);
        }

        return connectivities;
    }

    /**
     * Find the maximal shortest distance between any two nodes in the network
     * using a breadth-first search.
     *
     * @param graph
     *            {@link Map}<{@link Integer}, {@link Set}<{@link Integer}>> The
     *            loaded graph
     * @return <code>int</code> The diameter of the network
     */
    public int getDiameter(Map<Integer, Set<Integer>> graph) {
        int diameter = 0;
        for (Integer node: graph.keySet()) {
            boolean visited[] = new boolean[graph.size()];
            LinkedList<Integer> toVisit = new LinkedList<>(); // list of node - diameter
            int currNode = node;
            int currDiameter = 0;

            visited[currNode] = true;
            toVisit.add(currNode);
            while(toVisit.size() != 0) {
                LinkedList<Integer> tempVisit = new LinkedList<>();
                for(Integer n: toVisit) { // loop through nodes in toVisit and get neighbours, check if in visited and if not add to tempVisit
                    Set<Integer> neighbours = graph.get(n);
                    for(Integer neighbour: neighbours) {
                        if (!visited[neighbour]) {
                            visited[neighbour] = true;
                            tempVisit.add(neighbour);
                        }
                    }
                }
                if(tempVisit.size() != 0) currDiameter++;
                toVisit = tempVisit;


            }

            diameter = currDiameter > diameter ? currDiameter : diameter;

        }
        return diameter;
    }
}
