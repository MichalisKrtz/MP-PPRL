import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class BloomFilter {
    int length;
    byte[] cells;
    int numberOfHashFunctions;

    public BloomFilter(int length, int numberOfHashFunctions) {
        this.length = length;
        cells = new byte[length];
        this.numberOfHashFunctions = numberOfHashFunctions;
    }

    public void addElement(String element) {
        for (int i = 0; i < element.length() - 1; i++) {
            for (int j = 0; j < numberOfHashFunctions; j++) {
                String data = String.valueOf(element.charAt(i + 1)) + element.charAt(i);
                cells[hash(data + j)] = 1;
            }
        }
    }

    public void printCells() {
        for (int c : cells) {
            System.out.print(c);
        }
        System.out.println();
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

}
