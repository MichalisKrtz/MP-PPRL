package mp_pprl.core.encoding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

public class BloomFilter {
    private final int length;
    private final byte[] vector;
    private final int numberOfHashFunctions;
    private final EncodingHandler encodingHandler;

    public BloomFilter(int length, int numberOfHashFunctions, EncodingHandler encodingHandler) {
        this.length = length;
        vector = new byte[length];
        this.numberOfHashFunctions = numberOfHashFunctions;
        this.encodingHandler = encodingHandler;
    }

    /*addElement() takes a string and splits it in 2 character substrings(ex. word -> wo,or,rd).
    Then it hashes those substrings 'numberOfHashFunctions' times, each time with a different
    variation(i.e. subString + j) and turns the according cells from 0 to 1.
     */
    public void addElement(String element) {
        for (int i = 0; i < numberOfHashFunctions; i++) {
            for (int j = 0; j < element.length() - 1; j++) {
                String subString = String.valueOf(element.charAt(j)) + element.charAt(j + 1);
                vector[encodingHandler.hash(subString + i, length)] = 1;
            }
        }
    }

    // produce m splits (equal or nearly equal)
    public List<byte[]> split(int m) {
        List<byte[]> res = new ArrayList<>(m);

        int base = length / m;
        int rem = length % m;
        int idx = 0;

        for (int i = 0; i < m; i++) {

            int len = base + (i < rem ? 1 : 0);
            byte[] slice = new byte[len];

            System.arraycopy(vector, idx, slice, 0, len);

            res.add(slice);
            idx += len;
        }

        return res;
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
