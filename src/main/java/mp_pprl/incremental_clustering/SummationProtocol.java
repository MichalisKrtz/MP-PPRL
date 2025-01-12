package mp_pprl.incremental_clustering;

import mp_pprl.core.Party;
import mp_pprl.core.BloomFilterEncodedRecord;
import mp_pprl.core.encoding.CountingBloomFilter;

import java.util.List;
import java.util.Random;

public class SummationProtocol {
    public static CountingBloomFilter execute(List<BloomFilterEncodedRecord> bloomFilterEncodedRecords, int vectorSize) {
        int[] startingVector = generateRandomVector(vectorSize);
        CountingBloomFilter countingBloomFilter = new CountingBloomFilter(startingVector);

        for (BloomFilterEncodedRecord bloomFilterEncodedRecord : bloomFilterEncodedRecords) {
            Party party = bloomFilterEncodedRecord.getParty();
            String recordId = bloomFilterEncodedRecord.getId();
            party.addToCountingBloomFilter(countingBloomFilter, recordId);
        }

        countingBloomFilter.subtractVector(startingVector);

        return countingBloomFilter;
    }

    private static int[] generateRandomVector(int size) {
        int[] vector = new int[size];
        Random random = new Random();
        for (int i = 0; i < vector.length; i++) {
            vector[i] = random.nextInt(10);
        }

        return vector;
    }

}
