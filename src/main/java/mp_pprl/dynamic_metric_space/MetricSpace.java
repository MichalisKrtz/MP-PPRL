package mp_pprl.dynamic_metric_space;

import mp_pprl.core.domain.RecordIdentifier;

import java.util.*;

public class MetricSpace {
    public final Map<Pivot, List<RecordIdentifier>> pivotElementsMap;
    public final Map<Pivot, List<Integer>> pivotElementsDistanceMap;

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

    public void printMetricSpace() {
        for (Map.Entry<Pivot, List<RecordIdentifier>> entry: pivotElementsMap.entrySet()) {
            System.out.print(entry.getKey().getRecordIdentifier().getId() + ": ");
            for (RecordIdentifier element : entry.getValue()) {
                System.out.print(element.getId() + ", ");
            }
            System.out.println();
        }
    }

}
