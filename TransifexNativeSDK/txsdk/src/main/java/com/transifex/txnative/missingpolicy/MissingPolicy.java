package com.transifex.txnative.missingpolicy;

import android.content.res.Resources;

import com.transifex.txnative.LocaleState;
import com.transifex.txnative.TxResources;

import androidx.annotation.NonNull;
import androidx.annotation.PluralsRes;
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
     * @param resources A Resources object. This is the wrapped resources object returned by
     * {@link TxResources#getWrappedResources()}.
     * @param sourceString The source string.
     * @param id The string resource identifier as defined by
     * {@link Resources#getIdentifier(String, String, String)}.
     * @param resourceName The entry name of the string resource as defined by
     * {@link Resources#getResourceEntryName(int)}.
     * @param locale The current locale as returned by {@link LocaleState#getResolvedLocale()}.
     *
     * @return The translated string.
     */
    @NonNull CharSequence get(@NonNull Resources resources, @NonNull CharSequence sourceString,
                              @StringRes int id, @NonNull String resourceName, @NonNull String locale);

    /**
     * Return a quantity string as a translation based on the given source quantity string and
     * quantity.
     * <p>
     * Classes that implement this interface may choose to return anything relevant to the given
     * source string or not, based on their custom policy.
     *
     * @param resources A Resources object. This is the wrapped resources object returned by
     * {@link TxResources#getWrappedResources()}.
     * @param sourceQuantityString The source string having grammatically correct pluralization for
     *                             the given quantity.
     * @param id The plurals resource identifier as defined by
     * {@link Resources#getIdentifier(String, String, String)}.
     * @param quantity The number used to get the correct string for the current language's plural
     *                 rules.
     * @param resourceName The entry name of the plurals resource as defined by
     * {@link Resources#getResourceEntryName(int)}.
     * @param locale The current locale as returned by {@link LocaleState#getResolvedLocale()}.
     *
     * @return The translated string.
     */
    @NonNull CharSequence getQuantityString(@NonNull Resources resources,
                                            @NonNull CharSequence sourceQuantityString,
                                            @PluralsRes int id, int quantity,
                                            @NonNull String resourceName, @NonNull String locale);
}
