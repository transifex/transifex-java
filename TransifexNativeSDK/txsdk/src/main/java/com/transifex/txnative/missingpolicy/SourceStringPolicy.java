package com.transifex.txnative.missingpolicy;

import android.content.res.Resources;

import androidx.annotation.NonNull;
import androidx.annotation.PluralsRes;
import androidx.annotation.StringRes;

/**
 * Returns the source string when the translation string is missing.
 */
public class SourceStringPolicy implements MissingPolicy {

    /**
     * Return the source string as the translation string.
     */
    @Override
    @NonNull public CharSequence get(@NonNull Resources resources,
                                     @NonNull CharSequence sourceString, @StringRes int id,
                                     @NonNull String resourceName, @NonNull String locale) {
        return sourceString;
    }

    /**
     * Returns the source quantity string as the translation quantity string.
     */
    @Override
    @NonNull public CharSequence getQuantityString(@NonNull Resources resources,
            @NonNull CharSequence sourceQuantityString, @PluralsRes int id, int quantity,
            @NonNull String resourceName, @NonNull String locale) {
        return sourceQuantityString;
    }
}
