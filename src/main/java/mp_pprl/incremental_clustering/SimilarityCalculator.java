package mp_pprl.incremental_clustering;

import mp_pprl.core.BloomFilterEncodedRecord;
import mp_pprl.core.encoding.CountingBloomFilter;
import mp_pprl.core.graph.Cluster;

import java.util.ArrayList;
import java.util.List;

public class SimilarityCalculator {
    /*This method uses the secure summation protocol to create counting bloom filters and then calculates the metric.*/
    public static double averageSimilaritySecure(Cluster cluster, BloomFilterEncodedRecord bloomFilterEncodedRecord, int bloomFilterLength) {
        List<CountingBloomFilter> countingBloomFilterList = new ArrayList<>();
        for (BloomFilterEncodedRecord clusteredBloomFilterEncodedRecord : cluster.bloomFilterEncodedRecordsSet()) {
            List<BloomFilterEncodedRecord> bloomFilterEncodedRecordsForSummation = new ArrayList<>();
            // Add one clustered record.
            bloomFilterEncodedRecordsForSummation.add(clusteredBloomFilterEncodedRecord);
            // Add the record from the new singleton cluster.
            bloomFilterEncodedRecordsForSummation.add(bloomFilterEncodedRecord);
            CountingBloomFilter cbf = SummationProtocol.execute(bloomFilterEncodedRecordsForSummation, bloomFilterLength);
            countingBloomFilterList.add(cbf);
        }
        double sumSimilarity = 0;
        for (CountingBloomFilter cbf : countingBloomFilterList) {
            sumSimilarity += calculateDiceCoefficient(cbf);
        }

        return sumSimilarity / countingBloomFilterList.size();
    }

    public static double averageSimilarity(Cluster cluster, BloomFilterEncodedRecord bloomFilterEncodedRecord) {
        double sumSimilarity = 0;
        for (BloomFilterEncodedRecord clusteredRecord : cluster.bloomFilterEncodedRecordsSet()) {

            sumSimilarity += calculateSimilarity(bloomFilterEncodedRecord, clusteredRecord);
        }

        return sumSimilarity / cluster.bloomFilterEncodedRecordsSet().size();
    }

    private static double calculateDiceCoefficient(CountingBloomFilter countingBloomFilter) {
        int[] vector = countingBloomFilter.getVector();

        int numberOfMatches = 0;
        int cbfSum = 0;
        for (int counter : vector) {
            if (counter == countingBloomFilter.getNumberOfBloomFilters()) {
                numberOfMatches++;
            }
            cbfSum += counter;
        }

        return (double) (countingBloomFilter.getNumberOfBloomFilters() * numberOfMatches) / cbfSum;
    }

    private static double calculateSimilarity(BloomFilterEncodedRecord r1, BloomFilterEncodedRecord r2) {
        byte[] bf1 = r1.getBloomFilter().getVector();
        byte[] bf2 = r2.getBloomFilter().getVector();
        int[] cbf = new int[bf1.length];
        for (int i = 0; i < cbf.length; i++) {
            cbf[i] = bf1[i] + bf2[i];
        }
        int numberOfMatches = 0;
        int cbfSum = 0;
        for (int cell : cbf) {
            if (cell == 1) {
                cbfSum++;
            } else if (cell == 2) {
                cbfSum += 2;
                numberOfMatches++;
            }
        }

        return (double) (2 * numberOfMatches) / cbfSum;
    }
}
