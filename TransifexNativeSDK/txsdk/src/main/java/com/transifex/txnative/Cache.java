package com.transifex.txnative;

import org.json.JSONObject;

import java.util.HashMap;
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
     * {@link #setCurrentLocale(String)} should be called once to set the current locale.
     *
     * @param key The key of the string.
     *
     * @return The string or <code>null</code> if it wasn't found.
     */
    @Nullable String get(@NonNull String key);

    /**
     * Update the cache with a map containing locale codes as keys and a list of JSONObjects as
     * values like this:
     * <p>
     * <pre>
     *    {
     *        'fr' : {
     *             'key1' : { 'string' : '...' },
     *             'key2' : { 'string' : '...' },
     *        },
     *        'de' : {
     *             'key3' : { 'string' : '...' },
     *        },
     *        'gr' : {
     *             'key4' : { 'string' : '...' },
     *        },
     *    }
     * </pre>
     *
     * @param translationMap The translation map to use in the cache.
     */
    void update(@NonNull HashMap<String, JSONObject> translationMap);
}
