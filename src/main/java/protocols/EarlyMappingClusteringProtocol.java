package protocols;

import db.Record;
import other.HungarianAlgorithm;
import other.SimilarityCalculator;

import java.util.*;

public class EarlyMappingClusteringProtocol {
    private final List<Map<String, List<Record>>> sharedRecords;

    public EarlyMappingClusteringProtocol(List<Map<String, List<Record>>> sharedRecords) {
        this.sharedRecords = sharedRecords;
    }

    public void run(double similarityThreshold, int minimumSubsetSize) {
        // Initialization
        int clusterId = 0;
        WeightedGraph graph = new WeightedGraph();
        WeightedGraph finalGraph = new WeightedGraph();
        // Order databases
        orderDatabasesDesc();
        // Iterate blocks
        for (String blockKey : unionOfBlocks()) {
            WeightedGraph blockGraph = new WeightedGraph();
            for (Map<String, List<Record>> partyRecords : sharedRecords) {
                if (!partyRecords.containsKey(blockKey)) {
                    continue;
                }
                if (blockGraph.getVertices().isEmpty()) {
                    for (Record rec : partyRecords.get(blockKey)) {
                        clusterId++;
                        Vertex v = new Vertex(rec);
                        blockGraph.addVertex(v);
                    }
                }
                if (!blockGraph.getVertices().isEmpty()) {
                    for (Record rec : partyRecords.get(blockKey)) {
                        for (Vertex v : blockGraph.getVertices()) {
                            double similarity = SimilarityCalculator.calculateAverageSimilarity(v, rec);
                            if (similarity > similarityThreshold) {
                                blockGraph.addEdge(v, rec);
                            }
                        }
                    }
                    Set<Edge> optimalEdges = HungarianAlgorithm.findOptimalEdges(blockGraph.getEdges());
                }
            }
        }

    }

    private Set<String> unionOfBlocks() {
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
