package mp_pprl.core.encoding;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class EncodingHandler {
    private final byte[] salt;

    public EncodingHandler() {
        SecureRandom random = new SecureRandom();
        this.salt = new byte[16];
        //TODO
        //Having a random salt, interferes with the selection of the first pivots
//        random.nextBytes(salt);
        for (int i = 0; i < 16; i++) {
            this.salt[i] = 0;
        }
    }

    public int hash(String data, int bloomFilterLength) {
        try {
            byte[] message = new byte[data.getBytes().length + salt.length];
            System.arraycopy(data.getBytes(), 0, message, 0, data.getBytes().length);
            System.arraycopy(salt, 0, message, data.getBytes().length, salt.length);


            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(message);
            byte[] hash = md.digest();

            int result = 0;
            for (byte b : hash) {
                result += b;
            }
            if (result < 0) result *= -1;

            return result % bloomFilterLength;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
