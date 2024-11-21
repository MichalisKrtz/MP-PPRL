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

    public static double distance(Cluster c1, Cluster c2) {
        int hammingDistanceSum = 0;
        for (BloomFilterEncodedRecord r1 : c1.bloomFilterEncodedRecordsSet()) {
            for (BloomFilterEncodedRecord r2 : c2.bloomFilterEncodedRecordsSet()) {
                hammingDistanceSum += calculateHammingDistance(r1.bloomFilter().getVector(), r2.bloomFilter().getVector());
            }
        }

        return (double) hammingDistanceSum / (c1.bloomFilterEncodedRecordsSet().size() * c2.bloomFilterEncodedRecordsSet().size());
    }

    public static double distance(BloomFilterEncodedRecord r1, BloomFilterEncodedRecord r2) {

        return calculateHammingDistance(r1.bloomFilter().getVector(), r2.bloomFilter().getVector());
    }

    private static int calculateHammingDistance(byte[] bf1, byte[] bf2) {
        int hammingDistance = 0;
        for (int i = 0; i < bf1.length; i++) {
            int differingBits = bf1[i] ^ bf2[i];
            hammingDistance += Integer.bitCount(differingBits & 0xFF);
        }
        return hammingDistance;
    }

    public void printMetricSpace() {
        System.out.println("PIVOTS = " + pivotElementsMap.size());
        for (Pivot p : pivotElementsMap.keySet()) {
            System.out.print(p.getCluster().bloomFilterEncodedRecordsSet().iterator().next().id() + ", ");
        }
        System.out.println();

        for (Map.Entry<Pivot, List<Cluster>> entry: pivotElementsMap.entrySet()) {
            System.out.print(entry.getKey().getCluster().bloomFilterEncodedRecordsSet().iterator().next().id() + ": ");
            for (Cluster elementsCluster : entry.getValue()) {
                System.out.print("(");
                for (BloomFilterEncodedRecord element : elementsCluster.bloomFilterEncodedRecordsSet()) {
                    System.out.print(element.id() + ", ");
                }
                System.out.print("), ");
            }
            System.out.println();
        }
    }

    public void printClusters() {
        System.out.println("MP-PPRL with dynamic pivots in metric space results");
        int counter = 0;
        for (Pivot p : pivotElementsMap.keySet()) {
            counter++;
            if (counter > 200) break;
            System.out.print("Pivot's cluster: ");
            for (BloomFilterEncodedRecord r : p.getCluster().bloomFilterEncodedRecordsSet()) {
                System.out.print("P" + r.party().id + " id " + r.id() + " | ");
            }
            System.out.println();
            for (Cluster c : pivotElementsMap.get(p)) {
                System.out.print("----Cluster: ");
                for (BloomFilterEncodedRecord r : c.bloomFilterEncodedRecordsSet()) {
                    System.out.print("P" + r.party().id + " id " + r.id() + " | ");
                }
                System.out.println();
            }
        }
        int threeRecordClusters = 0;
        for (Pivot p : pivotElementsMap.keySet()) {
            if (p.getCluster().bloomFilterEncodedRecordsSet().size() == 3) {
                threeRecordClusters++;
            }
        }
        for (List<Cluster> clusters : pivotElementsMap.values()) {
            for (Cluster c : clusters) {
                if (c.bloomFilterEncodedRecordsSet().size() == 3) {
                    threeRecordClusters++;
                }
            }
        }
        System.out.println("Three record clusters: " + threeRecordClusters);

    }

}
