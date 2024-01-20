package mp_pprl.protocols;

import mp_pprl.graph.Edge;
import mp_pprl.graph.Vertex;
import mp_pprl.graph.WeightedGraph;
import mp_pprl.db.Record;
import mp_pprl.optimization.HungarianAlgorithm;

import java.util.*;

public class EarlyMappingClusteringProtocol {
    private final List<Map<String, List<Record>>> sharedRecords;

    public EarlyMappingClusteringProtocol(List<Map<String, List<Record>>> sharedRecords) {
        this.sharedRecords = sharedRecords;
    }

    public Set<Vertex> generateClusters(double similarityThreshold, int minimumSubsetSize) {
        // Initialization
        WeightedGraph graph = new WeightedGraph();
        Set<Vertex> finalClusters = new HashSet<>();
        // Order databases
        orderDatabasesDesc();
        // Iterate blocks
        for (String blockKey : getUnionOfBlocks()) {
            WeightedGraph blockGraph = new WeightedGraph();
            for (Map<String, List<Record>> partyRecords : sharedRecords) {
                if (!partyRecords.containsKey(blockKey)) {
                    continue;
                }

                if (blockGraph.getVertices().isEmpty()) {
                    for (Record rec : partyRecords.get(blockKey)) {
                        Vertex v = new Vertex(rec);
                        blockGraph.addVertex(v);
                    }
                    continue;
                }

                for (Record rec : partyRecords.get(blockKey)) {
                    for (Vertex cluster : blockGraph.getVertices()) {
                        double similarity = SimilarityCalculator.calculateAverageSimilarity(cluster, rec);
                        if (similarity > similarityThreshold) {
                            blockGraph.addEdge(cluster, rec);
                        }
                    }
                }
                Set<Edge> optimalEdges = HungarianAlgorithm.findOptimalEdges(blockGraph.getEdges());

                //  Use a copy of the blocks edges to avoid ConcurrentModificationException when removing the edge.
                Set<Edge> blockGraphEdges = new HashSet<>(blockGraph.getEdges());
                for (Edge e : blockGraphEdges) {
                    if (!optimalEdges.contains(e)) {
                        blockGraph.removeEdge(e);
                    }
                }

                // Merge each edge's record to the edge's vertex.
                Set<Record> assignedRecords = new HashSet<>();
                for (Edge e : blockGraph.getEdges()) {
                    blockGraph.mergeClusterVertices(e);
                    assignedRecords.add(e.record());
                }
                // If a record doesn't belong to a vertex(cluster) create a new singleton vertex.
                for (Record rec : partyRecords.get(blockKey)) {
                    if (!assignedRecords.contains(rec)) {
                        blockGraph.addVertex(new Vertex(rec));
                    }
                }

                blockGraph.clearEdges();
            }
            graph.addVertices(blockGraph.getVertices());

        }

        for (Vertex cluster : graph.getVertices()) {
            if (cluster.records().size() >= minimumSubsetSize) {
                finalClusters.add(cluster);
            }
        }

        return finalClusters;
    }

    private Set<String> getUnionOfBlocks() {
        Set<String> blocks = new HashSet<>();
        for (Map<String, List<Record>> partyRecords : sharedRecords) {
            blocks.addAll(partyRecords.keySet());
        }
        return blocks;
    }

    private void orderDatabasesDesc() {
        Comparator<Map<String, List<Record>>> comp = new Comparator<Map<String, List<Record>>>() {
            @Override
            public int compare(Map<String, List<Record>> db1, Map<String, List<Record>> db2) {
                int db1NumberOfRecords = 0;
                int db2NumberOfRecords = 0;
                for (List<Record> group : db1.values()) {
                    db1NumberOfRecords += group.size();
                }
                for (List<Record> group : db2.values()) {
                    db2NumberOfRecords += group.size();
                }
                return db1NumberOfRecords - db2NumberOfRecords;
            }
        };

        sharedRecords.sort(comp.reversed());
    }
}
