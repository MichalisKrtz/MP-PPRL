package mp_pprl.protocols;

import mp_pprl.domain.RecordIdentifier;
import mp_pprl.encoding.CountingBloomFilter;

import java.util.List;
import java.util.Random;

public class SummationProtocol {
    public static CountingBloomFilter execute(List<RecordIdentifier> recordIdentifiers, int vectorSize) {
        int[] startingVector = generateRandomVector(vectorSize);
        CountingBloomFilter countingBloomFilter = new CountingBloomFilter(startingVector);

        for (RecordIdentifier recordIdentifier : recordIdentifiers) {
            Party party = recordIdentifier.getParty();
            int recordId = recordIdentifier.getId();
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
