package mp_pprl.encoding;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class BloomFilter {
    private final int length;
    private final byte[] cells;
    private final int numberOfHashFunctions;

    public BloomFilter(int length, int numberOfHashFunctions) {
        this.length = length;
        cells = new byte[length];
        this.numberOfHashFunctions = numberOfHashFunctions;
    }

    /*addElement() takes a string and splits it in 2 character substrings(ex. word -> wo,or,rd).
    Then it hashes those substrings numberOfHashFunctions times, each time with a different variation(i.e. +j)
    and turns the according cells from 0 to 1.
     */
    public void addElement(String element) {
        for (int i = 0; i < element.length() - 1; i++) {
            for (int j = 0; j < numberOfHashFunctions; j++) {
                String data = String.valueOf(element.charAt(i + 1)) + element.charAt(i);
                cells[hash(data + j)] = 1;
            }
        }
    }

    private int hash(String data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(data.getBytes());
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

    public byte[] getCells() {
        return cells;
    }
}
