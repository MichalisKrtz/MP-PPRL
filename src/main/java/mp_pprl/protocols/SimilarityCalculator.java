package mp_pprl.protocols;

import mp_pprl.encoding.CountingBloomFilter;

import java.util.List;

public class SimilarityCalculator {
    public static double calculateAverageSimilarity(List<CountingBloomFilter> countingBloomFilterList) {
        double sumSimilarity = 0;
        for (CountingBloomFilter cbf : countingBloomFilterList) {
            sumSimilarity += calculateDiceCoefficient(cbf);
        }

        return sumSimilarity / countingBloomFilterList.size();
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
//    public static double calculateAverageSimilarity(Vertex v1, Record c2) {
//        double sumSimilarity = 0;
//        for (Record clusterRecord : v1.partyRecordMap()) {
//
//            sumSimilarity += calculateSimilarity(clusterRecord, c2);
//        }
//
//        return sumSimilarity / v1.partyRecordMap().size();
//    }
//
//    private static double calculateSimilarity(Record r1, Record r2) {
//        byte[] bf1 = (byte[]) r1.get("bloom_filter").getValue();
//        byte[] bf2 = (byte[]) r2.get("bloom_filter").getValue();
//        int[] cbf = new int[bf1.length];
//        for (int i = 0; i < cbf.length; i++) {
//            cbf[i] = bf1[i] + bf2[i];
//        }
//        int numberOfMatches = 0;
//        int cbfSum = 0;
//        for (int cell : cbf) {
//            if (cell == 1) {
//                cbfSum++;
//            } else if (cell == 2) {
//                cbfSum += 2;
//                numberOfMatches++;
//            }
//        }
//
//        return (double) (2 * numberOfMatches) / cbfSum;
//    }
}
