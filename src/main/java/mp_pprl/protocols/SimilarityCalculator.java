package mp_pprl.protocols;

import mp_pprl.domain.RecordIdentifier;
import mp_pprl.encoding.CountingBloomFilter;
import mp_pprl.graph.Cluster;

import java.util.ArrayList;
import java.util.List;

public class SimilarityCalculator {
    /*This method uses the secure summation protocol to create counting bloom filters and then calculates the similarity.*/
    public static double averageSimilaritySecure(Cluster cluster, RecordIdentifier recordIdentifier, int bloomFilterLength) {
        List<CountingBloomFilter> countingBloomFilterList = new ArrayList<>();
        for (RecordIdentifier clusteredRecordIdentifier : cluster.recordIdentifierList()) {
            List<RecordIdentifier> recordIdentifiersForSummation = new ArrayList<>();
            // Add one clustered record.
            recordIdentifiersForSummation.add(clusteredRecordIdentifier);
            // Add the record from the new singleton cluster.
            recordIdentifiersForSummation.add(recordIdentifier);
            CountingBloomFilter cbf = SummationProtocol.execute(recordIdentifiersForSummation, bloomFilterLength);
            countingBloomFilterList.add(cbf);
        }
        double sumSimilarity = 0;
        for (CountingBloomFilter cbf : countingBloomFilterList) {
            sumSimilarity += calculateDiceCoefficient(cbf);
        }

        return sumSimilarity / countingBloomFilterList.size();
    }

    public static double averageSimilarity(Cluster cluster, RecordIdentifier recordIdentifier) {
        double sumSimilarity = 0;
        for (RecordIdentifier clusteredRecord : cluster.recordIdentifierList()) {

            sumSimilarity += calculateSimilarity(recordIdentifier, clusteredRecord);
        }

        return sumSimilarity / cluster.recordIdentifierList().size();
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

    private static double calculateSimilarity(RecordIdentifier r1, RecordIdentifier r2) {
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
