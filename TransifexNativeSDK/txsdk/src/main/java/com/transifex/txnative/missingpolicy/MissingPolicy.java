package com.transifex.txnative.missingpolicy;

import android.content.res.Resources;

import com.transifex.txnative.LocaleState;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

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
     * @param id The string resource identifier as defined by
     * {@link Resources#getIdentifier(String, String, String)}.
     * @param resourceName The entry name of the string resource as defined by
     * {@link Resources#getResourceEntryName(int)}.
     * @param locale The current locale as returned by {@link LocaleState#getResolvedLocale()}.
     *
     * @return The translated string.
     */
    @NonNull CharSequence get(@NonNull CharSequence sourceString, @StringRes int id,
                              @NonNull String resourceName, @NonNull String locale);
}
