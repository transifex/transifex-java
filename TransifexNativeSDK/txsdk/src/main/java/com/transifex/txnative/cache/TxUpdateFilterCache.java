package com.transifex.txnative.cache;

import android.text.TextUtils;

import com.transifex.common.LocaleData;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Map;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;


/**
 * Decorator class that updates the internal cache using a certain update policy when the
 * {@link #update(LocaleData.TranslationMap)} method is called.
 *
 * @see TxCacheUpdatePolicy
 */
public class TxUpdateFilterCache extends TxDecoratorCache {

    /**
     * Update policy that specifies the way that the internal cache is updated with new
     * translations.
     * <p>
     * You can find an easy to understand table containing a number of cases and how each policy
     * updates the cache below:
     *
     * <pre>
     *     {@code
     * | Key || Cache | New  || Replace All   | Update using Translated        |
     * |-----||-------|------||---------------|--------------------------------|
     * | a   || "a"   | -    || -             | "a"                            |
     * | b   || "b"   | "B"  || "B"           | "B"                            |
     * | c   || "c"   | ""   || ""            | "c"                            |
     * | d   || ""    | -    || -             | ""                             |
     * | e   || ""    | "E"  || "E"           | "E"                            |
     * | f   || -     | "F"  || "F"           | "F"                            |
     * | g   || -     | ""   || ""            | -                              |
     * }
     * </pre>
     * Here's an example on how to read the table above:
     * <ul>
     *     <li>given a string with <code>key="c"</code></li>
     *     <li>and a cache that has <code>"c"</code> as the stored value for this key (<code>"c" -> "c"</code>)</li>
     *     <li>if an empty translation arrives for this string (<code>""</code>)
     *     <ul>
     *         <li>if policy is <code>REPLACE_ALL</code>, then the cache will be updated so that
     *         (<code>"c" -> ""</code>)</li>
     *         <li>in contrast to that, if policy is <code>UPDATE_USING_TRANSLATED</code>, then the
     *         cache will stay as is (<code>"c" -> "c"</code>), because the new translation is
     *         empty</li>
     *     </ul>
     *     </li>
     * </ul>
     * A <code>"-"</code> value means that the respective key does not exist. For example:
     * <ul>
     *     <li>given a string with <code>key="f"</code></li>
     *     <li>and a cache that has no entry with <code>"f"</code> as a key</li>
     *     <li>if a translation arrives for this string (<code>"f" -> "F"</code>)
     *     <ul>
     *         <li>if policy is <code>REPLACE_ALL</code>, then the cache will be updated by adding a
     *         new entry so that (<code>"f" -> "F"</code>)</li>
     *         <li>if policy is <code>UPDATE_USING_TRANSLATED</code>, then the same will happen,
     *         since the new translation is not empty</li>
     *     </ul>
     *     </li>
     * </ul>
     *
     */
    @IntDef({TxCacheUpdatePolicy.REPLACE_ALL, TxCacheUpdatePolicy.UPDATE_USING_TRANSLATED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TxCacheUpdatePolicy {
        /**
         * Discards the existing cache entries completely and populates the cache with the new entries,
         * even if they contain empty translations.
         */
        int REPLACE_ALL = 0;

        /**
         * Updates the existing cache with the new entries that have a non-empty translation.
         */
        int UPDATE_USING_TRANSLATED = 1;
    }

    private final @TxCacheUpdatePolicy int mPolicy;

    /**
     * Creates a new instance having the provided {@link TxCacheUpdatePolicy TxCacheUpdatePolicy}
     * and internal cache.
     *
     * @param policy One of the available {@link TxCacheUpdatePolicy update policies}.
     * @param internalCache The internal cache to be used.
     */
    public TxUpdateFilterCache(@TxCacheUpdatePolicy int policy, @NonNull TxCache internalCache) {
        super(internalCache);
        mPolicy = policy;
    }

    /**
     * Updates the internal cache with the provided translations using the update policy specified
     * in the constructor.
     * <p>
     * Note that after calculating the new translations with the current update policy, the internal
     * cache's {@link TxCache#update(LocaleData.TranslationMap)} method is called to set them.
     * Depending on the cache's implementation, the actual result may be different to the calculated
     * one.
     */
    @Override
    public void update(@NonNull LocaleData.TranslationMap translationMap) {
        if (mPolicy == TxCacheUpdatePolicy.REPLACE_ALL) {
            super.update(translationMap);
        }
        else if (mPolicy == TxCacheUpdatePolicy.UPDATE_USING_TRANSLATED) {
            // Make a copy of the internal cache's TranslationMap, in order to apply the updates there
            LocaleData.TranslationMap updatedTranslations = new LocaleData.TranslationMap(get());

            // For each locale
            for (String locale : translationMap.getLocales()) {
                LocaleData.LocaleStrings localeStrings = translationMap.get(locale);
                if (localeStrings == null) {
                    continue; // Can't happen. Just to suppress lint
                }
                LocaleData.LocaleStrings updatedLocaleStrings = updatedTranslations.get(locale);

                // For each key-value entry of localeStrings
                for (Map.Entry<String, LocaleData.StringInfo> entry : localeStrings.getMap().entrySet()) {
                    // Make sure that the new entry contains a translation, otherwise don't process it.
                    if (entry.getValue() == null || TextUtils.isEmpty(entry.getValue().string)) {
                        continue;
                    }

                    // If needed, create new LocaleStrings object and add it to the updated
                    // translation map
                    if (updatedLocaleStrings == null) {
                        updatedLocaleStrings = new LocaleData.LocaleStrings(localeStrings.getMap().size());
                        updatedTranslations.put(locale, updatedLocaleStrings);
                    }

                    updatedLocaleStrings.put(entry.getKey(), entry.getValue());
                }

                // Update the internal cache with the updated translations
                super.update(updatedTranslations);
            }
        }
    }
}
