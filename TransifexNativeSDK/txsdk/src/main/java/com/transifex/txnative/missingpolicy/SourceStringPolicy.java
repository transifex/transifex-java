package com.transifex.txnative.missingpolicy;

import androidx.annotation.NonNull;

/**
 * Returns the source string when the translation string is missing.
 */
public class SourceStringPolicy implements MissingPolicy {

    /**
     * Return the source string as the translation string.
     *
     * @param sourceString The source string.
     *
     * @return The source string.
     */
    @Override
    @NonNull public CharSequence get(@NonNull CharSequence sourceString, int id,
                                     @NonNull String resourceName, @NonNull String locale) {
        return sourceString;
    }
}
