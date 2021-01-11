package com.transifex.txnative;

import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * An interface for classes that act as cache for translations.
 */
public interface Cache {

    /**
     * Sets the locale to get translation from when {@link #get(String)} is used.
     * <p>
     * If set to <code>null</code>, {@link #get(String)} will always return <code>null</code>.
     *
     * @param currentLocale The locale code.
     */
    void setCurrentLocale(@Nullable String currentLocale);

    /**
     * Returns a set of the locale codes supported by the cache.
     */
    @NonNull Set<String> getSupportedLocales();

    /**
     * Get the translation for a certain key for the current locale.
     * <p>
     * {@link #setCurrentLocale(String)} should be called once before calling this method.
     *
     * @param key The key of the string.
     *
     * @return The string or <code>null</code> if it wasn't found.
     */
    @Nullable String get(@NonNull String key);

    /**
     * Update the cache with the provided
     * {@link com.transifex.txnative.LocaleData.TranslationMap TranslationMap}.
     *
     * @param translationMap The translation map to use in the cache.
     */
    void update(@NonNull LocaleData.TranslationMap translationMap);
}
