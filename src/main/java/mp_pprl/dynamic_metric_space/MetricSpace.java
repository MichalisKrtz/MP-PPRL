package mp_pprl.dynamic_metric_space;

import mp_pprl.core.BloomFilterEncodedRecord;
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

    public static int distance(BloomFilterEncodedRecord r1, BloomFilterEncodedRecord r2) {
        int hammingDistance = 0;
        int bloomFilterLength = r1.getBloomFilter().getVector().length;
        for (int i = 0; i < bloomFilterLength; i++) {
            if (r1.getBloomFilter().getVector()[i] != r2.getBloomFilter().getVector()[i]) {
                hammingDistance++;
            }
        }
        return hammingDistance;
    }

    public static double distance(BloomFilterEncodedRecord r, Cluster c) {
        int hammingDistanceSum = 0;
        for (BloomFilterEncodedRecord clusterRecord : c.bloomFilterEncodedRecordsSet()) {
            for (int i = 0; i < r.getBloomFilter().getVector().length; i++) {
                if (r.getBloomFilter().getVector()[i] != clusterRecord.getBloomFilter().getVector()[i]) {
                    hammingDistanceSum++;
                }
            }
        }

        return (double) hammingDistanceSum / c.bloomFilterEncodedRecordsSet().size();
    }

    public static double distance(Cluster c1, Cluster c2) {
        int hammingDistanceSum = 0;
        for (BloomFilterEncodedRecord r1 : c1.bloomFilterEncodedRecordsSet()) {
            for (BloomFilterEncodedRecord r2 : c2.bloomFilterEncodedRecordsSet()) {
                hammingDistanceSum += calculateHammingDistance(r1.getBloomFilter().getVector(), r2.getBloomFilter().getVector());
            }
        }

        return (double) hammingDistanceSum / (c1.bloomFilterEncodedRecordsSet().size() * c2.bloomFilterEncodedRecordsSet().size());
    }

    private static int calculateHammingDistance(byte[] bf1, byte[] bf2) {
        int hammingDistance = 0;
        for (int i = 0; i < bf1.length; i++) {
            if (bf1[i] != bf2[i]) {
                hammingDistance++;
            }
        }
        return hammingDistance;
    }

    public void printMetricSpace() {
        System.out.println("PIVOTS = " + pivotElementsMap.size());
        for (Pivot p : pivotElementsMap.keySet()) {
            System.out.print(p.getCluster().bloomFilterEncodedRecordsSet().iterator().next().getId() + ", ");
        }
        System.out.println();

        for (Map.Entry<Pivot, List<Cluster>> entry: pivotElementsMap.entrySet()) {
            System.out.print(entry.getKey().getCluster().bloomFilterEncodedRecordsSet().iterator().next().getId() + ": ");
            for (Cluster elementsCluster : entry.getValue()) {
                System.out.print("(");
                for (BloomFilterEncodedRecord element : elementsCluster.bloomFilterEncodedRecordsSet()) {
                    System.out.print(element.getId() + ", ");
                }
                System.out.print("), ");
            }
            System.out.println();
        }
    }

    public void printClusters() {
        System.out.println("MP-PPRL with dynamic pivots in metric space results");
        for (Pivot p : pivotElementsMap.keySet()) {
            System.out.print("Pivot's cluster: " );
            for (BloomFilterEncodedRecord r : p.getCluster().bloomFilterEncodedRecordsSet()) {
                System.out.print(r.getId() + "(p: " + r.getParty().getRecordsSize() + ")" + " - ");
            }
            System.out.println();
            System.out.println("Clusters");
            for (Cluster c : pivotElementsMap.get(p)) {
                System.out.print("Cluster: ");
                for (BloomFilterEncodedRecord r : c.bloomFilterEncodedRecordsSet()) {
                    System.out.print(r.getId() + "(p: " + r.getParty().getRecordsSize() + ")" + " - ");
                }
                System.out.println();
            }
        }
    }

}
