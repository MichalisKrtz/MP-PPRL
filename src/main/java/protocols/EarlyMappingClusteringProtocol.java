package protocols;

import db.Record;
import other.Party;

import java.util.*;

public class EarlyMappingClusteringProtocol {
    private final List<Map<String, List<Record>>> sharedRecords;

    public EarlyMappingClusteringProtocol(List<Map<String, List<Record>>> sharedRecords) {
        this.sharedRecords = sharedRecords;
    }

    public void run(double similarityThreshold, int minimumSubsetSize) {
        // Initialization
        int clusterId = 0;
        // Order databases
//        System.out.println(parties.getFirst().getRecordsSize());
//        sharedRecords.sort(Comparator.comparing(Map<String, List<Record>>::).reversed());
//        System.out.println(parties.getFirst().getRecordsSize());
        // Iterate blocks

    }

    private Set<String> unionOfBlocks() {
        Set<String> blocks = new HashSet<>();
        for (Map<String, List<Record>> partyRecords : sharedRecords) {
            blocks.addAll(partyRecords.keySet());
        }
        return blocks;
    }

//    private void orderDatabases() {
//        sharedRecords.sort(Comparator.comparing(Map<String, List<Record>>::));
//    }


}
//public class EarlyMappingClusteringProtocol {
//    public static ArrayList<ArrayList<M>>() {
//        //input
//        int minimumSubsetSize = 2;
//        double similarityThreshold = 0.85;
//
//        int clusterId = 0;
//        Graph G = {};
//        MatchingSets M = {};
//        for (Block B : blocks) {
//            Graph Gb = {};
//            for (int i; i < numberOfParties; i++) {
//                if (i == 1) {
//                    for (Record rec : databases[i]) {
//                        clusterId++;
//                        Gb[clusterId] = rec;
//                    }
//                    continue;
//                }
//                for (Record rec : databases[i]) {
//                    for (Vertice c : Gb) {
//                        double sim_val = sim(rec, c);
//                        if (sim_val >= similarityThreshold) {
//                            Gb.add_edge(c, rec);
//                        }
//                    }
//                }
//                optimalEdges = map(Gb.E);
//                for (Edge e : Gb.E) {
//                    if (!optimalEdges.contains(e)) {
//                        Gb.remove(e);
//                    }
//                }
//                for (Edge e : Gb.E) {
//                    Gb.merge(get_vertices(e));
//                }
//            }
//            G.add(Gb);
//        }
//
//
//        for (Cluster c : G) {
//            if (c.size() >= minimumSubsetSize) {
//                M.add(c);
//            }
//        }
//        return M;
//    }
//}
