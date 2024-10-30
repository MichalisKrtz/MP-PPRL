package mp_pprl.soundex_based;

import mp_pprl.core.encoding.EncodingHandler;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class NoiseDataGenerator {

    public static List<List<String>> generateNoiseData(int originalSize, double noisePercentage) {
        int numberOfNoiseData = rowsToGenerate(originalSize, noisePercentage);
        List<List<String>> noiseData = new ArrayList<>(numberOfNoiseData);
        for (int i = 0; i < numberOfNoiseData; i++) {
            noiseData.add(createNoiseRow());
        }
        return noiseData;
    }

    private static String generateRandomSoundex() {
        SecureRandom r = new SecureRandom();
        r.setSeed(Instant.now().toEpochMilli());
        String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder randomCode = new StringBuilder();
        randomCode.append(letters.charAt(r.nextInt(letters.length())));

        EncodingHandler encodingHandler = new EncodingHandler();
        for (int i = 0; i < 3; i++) {
            int nextRand = r.nextInt(6);
            if (nextRand == 0) {
                randomCode.append("0".repeat(3 - i));
                return encodingHandler.hash(randomCode.toString());
            } else {
                randomCode.append(nextRand);
            }
        }

        return randomCode.toString();
    }

    private static int rowsToGenerate(int originalSize, double noisePercentage) {
        return (int) (originalSize * noisePercentage );
    }

    private static List<String> createNoiseRow() {
        SecureRandom r = new SecureRandom();
        r.setSeed(Instant.now().toEpochMilli());
        List<String> row = new ArrayList<>();
        row.add(null);  // First element is null

        for (int x = 0; x < 5; x++) {
            row.add(generateRandomSoundex());
        }
        row.add(String.valueOf(r.nextDouble()));
        return row;
    }

}
