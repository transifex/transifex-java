package com.transifex.txnative;

import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * An interface for classes that act as cache for translations.
 */
public interface Cache {

    /**
     * Returns a set of the locale codes supported by the cache.
     */
    @NonNull Set<String> getSupportedLocales();

    /**
     * Get the translation for a certain key and locale pair.
     *
     * @param key The key of the string.
     * @param locale The locale code.
     *
     * @return The string or <code>null</code> if it wasn't found or if the provided locale does not
     * exist in the cache.
     */
    @Nullable String get(@NonNull String key, @Nullable String locale);

    /**
     * Update the cache with the provided
     * {@link com.transifex.txnative.LocaleData.TranslationMap TranslationMap}.
     *
     * @param translationMap The translation map to use in the cache.
     */
    void update(@NonNull LocaleData.TranslationMap translationMap);
}
