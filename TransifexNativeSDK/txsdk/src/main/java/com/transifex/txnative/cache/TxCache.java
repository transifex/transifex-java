package com.transifex.txnative.cache;

import com.transifex.common.LocaleData;

import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * An interface for classes that act as cache for translations.
 */
public interface TxCache {

    /**
     * Gets all translations from the cache in the form of a
     * {@link LocaleData.TranslationMap TranslationMap} object.
     * <p>
     * The returned object should not be altered as the cache may use it internally.
     */
    @NonNull LocaleData.TranslationMap get();

    /**
     * Get the translation for a certain key and locale pair.
     *
     * @param key The key of the string.
     * @param locale The locale code.
     *
     * @return The string if the key was found in the cache; <code>null</code> if the provided locale
     * does not exist in the cache or the key does not exist for this locale; empty string if the
     * string has not yet been translated for this locale
     */
    @Nullable String get(@NonNull String key, @NonNull String locale);

    /**
     * Update the cache with the provided
     * {@link LocaleData.TranslationMap TranslationMap}.
     * <p>
     * The translation map should not be changed after providing it to the cache, because the cache
     * implementation may use it without making a copy.
     */
    void update(@NonNull LocaleData.TranslationMap translationMap);
}
