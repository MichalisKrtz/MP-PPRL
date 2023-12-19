package protocols;

import other.Party;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class EarlyMappingClusteringProtocol {
    private List<Party> parties;
    private Set<String> unionOfBlocks;

    public EarlyMappingClusteringProtocol(List<Party> parties, Set<String> unionOfBlocks) {
        this.parties = parties;
        this.unionOfBlocks = unionOfBlocks;
    }

//    public List<Map<String, DynamicTypeValue>> run(double similarityThreshold, int minimumSubsetSize) {
//        int clusterId = 0;
//
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
