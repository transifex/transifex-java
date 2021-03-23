package com.transifex.txnative.missingpolicy;

import java.util.HashMap;
import java.util.HashSet;

import androidx.annotation.NonNull;
import androidx.annotation.PluralsRes;
import androidx.annotation.StringRes;

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
    private static final HashSet<Character> sFormatSpecifierChars = new HashSet<>();

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

        sFormatSpecifierChars.add('b');
        sFormatSpecifierChars.add('h');
        sFormatSpecifierChars.add('s');
        sFormatSpecifierChars.add('c');
        sFormatSpecifierChars.add('d');
        sFormatSpecifierChars.add('o');
        sFormatSpecifierChars.add('x');
        sFormatSpecifierChars.add('e');
        sFormatSpecifierChars.add('f');
        sFormatSpecifierChars.add('g');
        sFormatSpecifierChars.add('a');
        sFormatSpecifierChars.add('t');
        sFormatSpecifierChars.add('n');
    }

    /**
     * Replaces each character of the source string with the mapped one found in {@link #sTable}..
     * <p>
     * Format specifiers such as <code>%D, %32.12f, %tM</code> pass-through without being altered.
     * <p>
     * If sourceString is {@link android.text.Spanned}, it is converted to {@link String} and thus
     * any spans are lost.
     */
    @NonNull String processString(@NonNull CharSequence sourceString) {
        char[] charArray = sourceString.toString().toCharArray();
        boolean expectingFormatSpecifierChar = false;
        boolean expectingDateFlag = false;
        for (int i = 0; i < charArray.length; i++) {
            char character = charArray[i];
            char lowerCaseCharacter = Character.toLowerCase(character);

            // Skip changing format specifiers such as %D, %32.12f, %tM.
            // When reaching a '%', we skip changing characters until we find one of the
            // expected specifier characters. If that character is 't', we expect one more
            // character, which is the date flag.
            if (expectingFormatSpecifierChar) {
                if (sFormatSpecifierChars.contains(lowerCaseCharacter)) {
                    if (lowerCaseCharacter == 't') {
                        expectingDateFlag = true;
                    }
                    expectingFormatSpecifierChar = false;
                }
                continue;
            }
            else {
                if (character == '%') {
                    expectingFormatSpecifierChar = true;
                    continue;
                }
            }
            if (expectingDateFlag) {
                expectingDateFlag = false;
                continue;
            }

            Character replacementCharacter = sTable.get(character);
            if (replacementCharacter != null) {
                charArray[i] = replacementCharacter;
            }
        }

        return new String(charArray);
    }

    /**
     * Returns a string that looks somewhat like the source string.
     * <p>
     * Note that {@link android.text.Spanned} objects are converted to simple Strings when
     * processed and will lose their markup.
     */
    @Override
    @NonNull public CharSequence get(@NonNull CharSequence sourceString, @StringRes int id,
                                      @NonNull String resourceName, @NonNull String locale) {
        return processString(sourceString);
    }

    /**
     * Returns a quantity string that looks somewhat like the source string.
     * <p>
     * Note that {@link android.text.Spanned} objects are converted to simple Strings when
     * processed and will lose their markup.
     */
    @Override
    @NonNull public CharSequence getQuantityString(
            @NonNull CharSequence sourceQuantityString, @PluralsRes int id, int quantity,
            @NonNull String resourceName, @NonNull String locale) {
        return processString(sourceQuantityString);
    }
}
