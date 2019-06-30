package uk.ac.cam.cl.ap949.exercises;

import edu.stanford.nlp.util.ArraySet;
import uk.ac.cam.cl.mlrd.exercises.social_networks.IExercise12;

import java.util.*;

public class Exercise12 implements IExercise12 {
    public List<Set<Integer>> GirvanNewman(Map<Integer, Set<Integer>> graph, int minimumComponents) {
        double currBtwness;
        List<Set<Integer>> components = new ArrayList<>();
        while (getComponents(graph).size() < minimumComponents && getNumberOfEdges(graph) > 0) {
            Map<Integer, Map<Integer, Double>> edgeBtwness = getEdgeBetweenness(graph);
            // in case of ties, remv all highest betweeness edges
            List<Map<Integer, Integer>> highestBetweenessL = new ArrayList<>();
            double highestBetweeness = -1;
            for(Integer u: edgeBtwness.keySet()) {
                Map<Integer, Double> m = edgeBtwness.get(u);
                for(Integer v: edgeBtwness.get(u).keySet()) {
                    currBtwness = m.get(v);
                    if(currBtwness > (highestBetweeness - 10e-6)) {
                        highestBetweeness = currBtwness;
                        highestBetweenessL = new ArrayList<Map<Integer, Integer>>();
                        Map<Integer, Integer> edge = new HashMap<>();
                        edge.put(u, v);
                        highestBetweenessL.add(edge);
                    } else if (currBtwness == (highestBetweeness - 10e-6)) {
                        Map<Integer, Integer> edge = new HashMap<>();
                        edge.put(u, v);
                        highestBetweenessL.add(edge);
                    }

                }
            }
            // rmv edges with highest betweeness
            for(Map<Integer, Integer> edge: highestBetweenessL) {
                for(Integer u: edge.keySet()) {
                    graph.get(u).remove(edge.get(u));
                }
            }
            components = getComponents(graph);
        }
        return components;
    }

    public int getNumberOfEdges(Map<Integer, Set<Integer>> graph) {
        int noEdges = 0;
        for(Integer i: graph.keySet()) {
            noEdges += graph.get(i).size();
        }
        return noEdges/2;
    }

    public List<Set<Integer>> getComponents(Map<Integer, Set<Integer>> graph) {
        Set<Integer> vertices = graph.keySet();
        List<Set<Integer>> components = new ArrayList<>();
        Set<Integer> component;
        Set<Integer> visited = new ArraySet<Integer>();


        for (int v: vertices) {
            component = new ArraySet<>();
            if(!visited.contains(v)) {
                LinkedList<Integer> queue = new LinkedList<>();
                queue.add(v);
                int currVertex;
                while (queue.size() != 0) {
                    currVertex = queue.poll();
                    //                visited.add(currVertex);
                    component.add(currVertex);
                    for (Integer neighbour : graph.get(currVertex)) {
                        if (!component.contains(neighbour)) {
                            queue.push(neighbour);
                        }
                    }
                }
                if (component.size() != 0) {
                    components.add(component);
                    visited.addAll(component);
                }
            }
        }
        return components;

    }

    public Map<Integer, Map<Integer, Double>> getEdgeBetweenness(Map<Integer, Set<Integer>> graph) {
        Set<Integer> vertices = graph.keySet();
        Map<Integer, Map<Integer, Double>> edgeBetweeness = new HashMap<>();
        for (Integer s: vertices) {
            Map<Integer, Double> m = new HashMap<>();
            for(Integer n: graph.get(s)) {
                m.put(n, 0.0);
            }
            edgeBetweeness.put(s, m);
        }
        for (Integer s: graph.keySet()) {
            LinkedList<Integer> stack = new LinkedList<>();
            Map<Integer, LinkedList<Integer>> pred = new HashMap<>(); // predecessors
            LinkedList<Integer> queue = new LinkedList<>();
            Map<Integer, Double> sigma = new HashMap<>();
            Map<Integer, Integer> dist = new HashMap<>();
            for (int v: vertices) {
                sigma.put(v, 0.0);
                dist.put(v, -1);
            }
            sigma.put(s, 1.0);
            dist.put(s, 0);
            queue.add(s);

            while (queue.size() != 0) {
                int v = queue.poll();
                stack.push(v);
                for(int w: graph.get(v)) {
                    // w found for the first time? path discovery
                    if (dist.get(w) == -1) {
                        dist.put(w, dist.get(v) + 1);
                        queue.add(w);
                    }
                    // shortest path to w via v? path counting
                    if (dist.get(w) == dist.get(v) + 1) { // check
                        sigma.put(w, sigma.get(w) + sigma.get(v));
                        if (pred.get(w) == null)
                            pred.put(w, new LinkedList<>());
                        pred.get(w).add(v);
                    }
                }

            }
            // backpropagation of dependencies
            Map<Integer, Double> delta = new HashMap<>();

            for (Integer v: vertices) {
                delta.put(v, 0.0);
            }

            // S returns vertices in order of non-increasing distance from s
            while (stack.size() != 0) {
                int w = stack.pop();
                if (pred.get(w) != null) { // if don't have empty list
                    for (int v : pred.get(w)) {
                        double c = (sigma.get(v)/sigma.get(w)) * (1 + delta.get(w));
                        edgeBetweeness.get(v).put(w, edgeBetweeness.get(v).get(w) + c);
                        delta.put(v, delta.get(v) + c);
                    }
                }
            }

        }

        return edgeBetweeness;
    }
}


