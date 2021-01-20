package com.transifex.txnative.missingpolicy;

import androidx.annotation.NonNull;

/**
 * An interface for classes that determine what translation is returned when the requested
 * translation is not available.
 */
public interface MissingPolicy {

    /**
     * Return a string as a translation based on the given source string.
     * <p>
     * Classes that implement this interface may choose to return anything relevant to the given
     * source string or not, based on their custom policy.
     *
     * @param sourceString The source string.
     */
    @NonNull CharSequence get(@NonNull CharSequence sourceString);
}
