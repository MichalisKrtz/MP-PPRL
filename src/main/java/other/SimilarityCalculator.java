package other;

import db.Record;
import protocols.Vertex;

public class SimilarityCalculator {
    public static double calculateAverageSimilarity(Vertex v1, Record record) {
        double sumSimilarity = 0;
        for (Record clusterRecord : v1.records()) {

            sumSimilarity += calculateSimilarity(clusterRecord, record);
        }

        return sumSimilarity / v1.records().size();
    }

    private static double calculateSimilarity(Record r1, Record r2) {
        byte[] bf1 = (byte[])r1.get("bloomFilter").getValue();
        byte[] bf2 = (byte[])r2.get("bloomFilter").getValue();
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
