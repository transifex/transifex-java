package com.transifex.txnative.missingpolicy;

import org.jetbrains.annotations.NotNull;

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
    @NotNull
    @Override
    public String get(@NonNull String sourceString) {
        return sourceString;
    }
}
