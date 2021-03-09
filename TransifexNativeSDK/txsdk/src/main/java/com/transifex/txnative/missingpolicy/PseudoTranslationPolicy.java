package com.transifex.txnative.missingpolicy;

import java.util.HashMap;

import androidx.annotation.NonNull;

/**
 * Returns a string that looks like the source string but contains accented characters.
 * <p>
 * Example:
 * <p>
 * <code>get("The quick brown fox");</code>
 * <p>
 * Returns:
 * <p>
 * "Ťȟê ʠüıċǩ ƀȓøẁñ ƒøẋ"
 * </pre>
 */
public class PseudoTranslationPolicy implements MissingPolicy {

    private static final HashMap<Character, Character> sTable = new HashMap<Character, Character>();

    static {
        sTable.put('A', 'Å');
        sTable.put('B', 'Ɓ');
        sTable.put('C', 'Ċ');
        sTable.put('D', 'Đ');
        sTable.put('E', 'Ȅ');
        sTable.put('F', 'Ḟ');
        sTable.put('G', 'Ġ');
        sTable.put('H', 'Ȟ');
        sTable.put('I', 'İ');
        sTable.put('J', 'Ĵ');
        sTable.put('K', 'Ǩ');
        sTable.put('L', 'Ĺ');
        sTable.put('M', 'Ṁ');
        sTable.put('N', 'Ñ');
        sTable.put('O', 'Ò');
        sTable.put('P', 'Ƥ');
        sTable.put('Q', 'Ꝗ');
        sTable.put('R', 'Ȓ');
        sTable.put('S', 'Ș');
        sTable.put('T', 'Ť');
        sTable.put('U', 'Ü');
        sTable.put('V', 'Ṽ');
        sTable.put('W', 'Ẃ');
        sTable.put('X', 'Ẍ');
        sTable.put('Y', 'Ẏ');
        sTable.put('Z', 'Ž');

        sTable.put('a', 'à');
        sTable.put('b', 'ƀ');
        sTable.put('c', 'ċ');
        sTable.put('d', 'đ');
        sTable.put('e', 'ê');
        sTable.put('f', 'ƒ');
        sTable.put('g', 'ğ');
        sTable.put('h', 'ȟ');
        sTable.put('i', 'ı');
        sTable.put('j', 'ǰ');
        sTable.put('k', 'ǩ');
        sTable.put('l', 'ĺ');
        sTable.put('m', 'ɱ');
        sTable.put('n', 'ñ');
        sTable.put('o', 'ø');
        sTable.put('p', 'ƥ');
        sTable.put('q', 'ʠ');
        sTable.put('r', 'ȓ');
        sTable.put('s', 'š');
        sTable.put('t', 'ť');
        sTable.put('u', 'ü');
        sTable.put('v', 'ṽ');
        sTable.put('w', 'ẁ');
        sTable.put('x', 'ẋ');
        sTable.put('y', 'ÿ');
        sTable.put('z', 'ź');
    }

    /**
     * Return a string that looks somewhat like the source string.
     *
     * @param sourceString The source string.
     * @return A string that looks like the source string.
     */

    @Override
    @NonNull public CharSequence get(@NonNull CharSequence sourceString) {
        char[] charArray = sourceString.toString().toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            char character = charArray[i];
            Character replacementCharacter = sTable.get(character);
            if (replacementCharacter != null) {
                charArray[i] = replacementCharacter;
            }
        }

        return new String(charArray);
    }
}
