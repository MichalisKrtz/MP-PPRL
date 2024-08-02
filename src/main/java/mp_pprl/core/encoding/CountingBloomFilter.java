package mp_pprl.core.encoding;

import java.util.Arrays;

public class CountingBloomFilter {
    private final int[] vector;
    private int numberOfBloomFilters;

    public CountingBloomFilter(int[] vector) {
        this.vector = Arrays.copyOf(vector, vector.length);
        numberOfBloomFilters = 1;
    }

    public void addVector(int[] inputVector) {
        if (inputVector.length != vector.length) {
            System.out.println("Bloom filter length is different than the counting bloom filter length.");
            return;
        }

        for (int i = 0; i < vector.length; i++) {
            vector[i] += inputVector[i];
        }

        numberOfBloomFilters++;
    }

    public void addVector(byte[] inputVector) {
        if (inputVector.length != vector.length) {
            System.out.println("Input vector has different length than the counting bloom filter's vector length.");
            return;
        }

        for (int i = 0; i < vector.length; i++) {
            vector[i] += inputVector[i];
        }

        numberOfBloomFilters++;
    }

    public void subtractVector(int[] inputVector) {
        if (inputVector.length != vector.length) {
            System.out.println("Bloom filter length is different than the counting bloom filter length.");
            return;
        }

        for (int i = 0; i < vector.length; i++) {
            vector[i] -= inputVector[i];
        }

        numberOfBloomFilters--;
    }

    public int[] getVector() {
        return vector;
    }

    public int getNumberOfBloomFilters() {
        return numberOfBloomFilters;
    }

    public void printVector() {
        System.out.print("[");
        for (int c : vector) {
            System.out.print(c + ", ");
        }
        System.out.println("]");
    }

}
