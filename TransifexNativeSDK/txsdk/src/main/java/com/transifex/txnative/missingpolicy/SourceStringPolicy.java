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
    @NonNull
    @Override
    public CharSequence get(@NonNull CharSequence sourceString) {
        return sourceString;
    }
}
