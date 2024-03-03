package mp_pprl.encoding;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class BloomFilter {
    private final int length;
    private final byte[] vector;
    private final int numberOfHashFunctions;

    public BloomFilter(int length, int numberOfHashFunctions) {
        this.length = length;
        vector = new byte[length];
        this.numberOfHashFunctions = numberOfHashFunctions;
    }

    /*addElement() takes a string and splits it in 2 character substrings(ex. word -> wo,or,rd).
    Then it hashes those substrings 'numberOfHashFunctions' times, each time with a different
    variation(i.e. subString + j) and turns the according cells from 0 to 1.
     */
    public void addElement(String element) {
        for (int i = 0; i < numberOfHashFunctions; i++) {
            for (int j = 0; j < element.length() - 1; j++) {
                String subString = String.valueOf(element.charAt(j)) + element.charAt(j + 1);
                vector[hash(subString + i)] = 1;
            }
        }
    }

    private int hash(String data) {
        try {
            byte[] message = data.getBytes();
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(message);
            byte[] hash = md.digest();

            int result = 0;
            for (byte b : hash) {
                result += b;
            }
            if (result < 0) result *= -1;

            return result % length;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] getVector() {
        return vector;
    }

    @Override
    public String toString() {
        return "BloomFilter{" +
                "length=" + length +
                ", vector=" + Arrays.toString(vector) +
                ", numberOfHashFunctions=" + numberOfHashFunctions +
                '}';
    }
}
