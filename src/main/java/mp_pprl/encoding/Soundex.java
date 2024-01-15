package mp_pprl.encoding;

import java.util.Arrays;

public class Soundex {
    private static final int[] charsToDelete = {'a', 'e', 'h', 'i', 'o', 'u', 'w', 'y'};
    private static final int[] charsEqualToOne = {'b', 'f', 'p', 'v'};
    private static final int[] charsEqualToTwo = {'c', 'g', 'j', 'k', 'q', 's', 'x', 'z'};
    private static final int[] charsEqualToThree = {'d', 't'};
    private static final int charEqualToFour = 'l';
    private static final int[] charsEqualToFive = {'m', 'n'};
    private static final int charEqualToSix = 'r';

    public static String encode(String name) {
        StringBuilder encodedName = new StringBuilder();
        encodedName.append(Character.toUpperCase(name.charAt(0)));
        for (int i = 1; i < name.length(); i++) {
            int character = Character.toLowerCase(name.charAt(i));

            boolean isCharToDelete = Arrays.stream(charsToDelete).anyMatch(value -> value == character);
            if (isCharToDelete) {
                continue;
            }
            boolean isCharEqualToOne = Arrays.stream(charsEqualToOne).anyMatch(value -> value == character);
            if (isCharEqualToOne) {
                encodedName.append('1');
                continue;
            }
            boolean isCharEqualToTwo = Arrays.stream(charsEqualToTwo).anyMatch(value -> value == character);
            if (isCharEqualToTwo) {
                encodedName.append('2');
                continue;
            }
            boolean isCharEqualToThree = Arrays.stream(charsEqualToThree).anyMatch(value -> value == character);
            if (isCharEqualToThree) {
                encodedName.append('3');
                continue;
            }
            if (character == charEqualToFour) {
                encodedName.append('4');
                continue;
            }
            boolean isCharEqualToFive = Arrays.stream(charsEqualToFive).anyMatch(value -> value == character);
            if (isCharEqualToFive) {
                encodedName.append('5');
                continue;
            }
            if (character == charEqualToSix) {
                encodedName.append('6');
            }
        }

        for (int i = 1; i < encodedName.length() - 1; i++) {
            if (encodedName.charAt(i) == encodedName.charAt(i + 1)) {
                encodedName.deleteCharAt(i + 1);
            }
        }

        if (encodedName.length() > 4) {
            encodedName.setLength(4);
            return encodedName.toString();
        }
        if (encodedName.length() < 4) {
            for (int i = encodedName.length(); i < 4; i++) {
                encodedName.append('0');
            }
            return encodedName.toString();
        }
        return encodedName.toString();
    }
}
