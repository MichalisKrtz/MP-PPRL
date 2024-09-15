package mp_pprl.dynamic_metric_space;

import mp_pprl.core.domain.RecordIdentifier;
import mp_pprl.core.graph.Cluster;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetricSpace {
    public final Map<Pivot, List<Cluster>> pivotElementsMap;
    public final Map<Pivot, List<Double>> pivotElementsDistanceMap;

    public MetricSpace() {
        pivotElementsMap = new HashMap<>();
        pivotElementsDistanceMap = new HashMap<>();
    }

    public static int distance(RecordIdentifier r1, RecordIdentifier r2) {
        int hammingDistance = 0;
        int bloomFilterLength = r1.getBloomFilter().getVector().length;
        for (int i = 0; i < bloomFilterLength; i++) {
            if (r1.getBloomFilter().getVector()[i] != r2.getBloomFilter().getVector()[i]) {
                hammingDistance++;
            }
        }
        return hammingDistance;
    }

    public static double distance(RecordIdentifier r, Cluster c) {
        int hammingDistanceSum = 0;
        for (RecordIdentifier clusterRecord : c.recordIdentifiersSet()) {
            for (int i = 0; i < r.getBloomFilter().getVector().length; i++) {
                if (r.getBloomFilter().getVector()[i] != clusterRecord.getBloomFilter().getVector()[i]) {
                    hammingDistanceSum++;
                }
            }
        }

        return (double) hammingDistanceSum / c.recordIdentifiersSet().size();
    }

    public static double distance(Cluster c1, Cluster c2) {
        int hammingDistanceSum = 0;
        for (RecordIdentifier r1 : c1.recordIdentifiersSet()) {
            for (RecordIdentifier r2 : c2.recordIdentifiersSet()) {
                for (int i = 0; i < r2.getBloomFilter().getVector().length; i++) {
                    if (r1.getBloomFilter().getVector()[i] != r2.getBloomFilter().getVector()[i]) {
                        hammingDistanceSum++;
                    }
                }
            }
        }

        return (double) hammingDistanceSum / (c1.recordIdentifiersSet().size() * c2.recordIdentifiersSet().size());
    }

    public void printMetricSpace() {
        System.out.println("PIVOTS = " + pivotElementsMap.size());
//        for (Pivot p : pivotElementsMap.keySet()) {
//            System.out.print(p.getCluster().recordIdentifiersSet().iterator().next().getId() + ", ");
//        }
//        System.out.println();
//
//        for (Map.Entry<Pivot, List<Cluster>> entry: pivotElementsMap.entrySet()) {
//            System.out.print(entry.getKey().getCluster().recordIdentifiersSet().iterator().next().getId() + ": ");
//            for (Cluster elementsCluster : entry.getValue()) {
//                System.out.print("(");
//                for (RecordIdentifier element : elementsCluster.recordIdentifiersSet()) {
//                    System.out.print(element.getId() + ", ");
//                }
//                System.out.print("), ");
//            }
//            System.out.println();
//        }
    }

    public void printClusters() {
        System.out.println("MP-PPRL with dynamic pivots in metric space results");
        for (Pivot p : pivotElementsMap.keySet()) {
            System.out.print("Pivot's cluster: " );
            for (RecordIdentifier r : p.getCluster().recordIdentifiersSet()) {
                System.out.print(r.getId() + " - ");
            }
            System.out.println();
            System.out.println("Clusters");
            for (Cluster c : pivotElementsMap.get(p)) {
                System.out.print("Cluster: ");
                for (RecordIdentifier r : c.recordIdentifiersSet()) {
                    System.out.print(r.getId() + " - ");
                }
                System.out.println();
            }
        }
    }

}
