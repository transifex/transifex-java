package com.transifex.common;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Classes that hold the localization data when received by CDS or stored locally.
 */
public class LocaleData {

    /**
     * A class that holds a String.
     */
    public static class StringInfo {

        public StringInfo(String string) {
            this.string = string;
        }

        public String string;

        @NonNull
        @Override
        public String toString() {
            return "{" + "string='" + string + '\'' + '}';
        }
    }

    /**
     * The data structure the CDS responds with for a locale request:
     * <p>
     * <pre>
     * {@code
     *   {
     *     data: {
     *       <key>: {
     *         'string': <string>
     *       }
     *     },
     *     meta: {
     *       ...
     *     }
     *   }
     * }
     * </pre>
     *
     * @see <a href="https://github.com/transifex/transifex-delivery/#pull-content">
     *     https://github.com/transifex/transifex-delivery/#pull-content</a>
     */
    public static class TxPullResponseData {

        public HashMap<String, StringInfo> data;

        @NonNull
        @Override
        public String toString() {
            return "{" + "data=" + data + '}';
        }
    }

    /**
     * The data structure the CDS accepts when pushing the source strings.
     *
     * @see TxPullResponseData
     * @see <a href="https://github.com/transifex/transifex-delivery/#push-content">
     *    https://github.com/transifex/transifex-delivery/#push-content</a>
     */
    public static class TxPostData extends TxPullResponseData {

        public static class Meta {
            public Boolean purge;

            @NonNull
            @Override
            public String toString() {
                return "{purge=" + purge + "}";
            }
        }

        public Meta meta;

        public TxPostData(@NonNull LinkedHashMap<String, StringInfo> data, @Nullable Meta meta) {
            this.data = data;
            this.meta = meta;
        }

        @NonNull
        @Override
        public String toString() {
            return "{" + "data=" + data + ", meta=" + meta + "}";
        }
    }

    /**
     * The data structure that CDS responds with when pushing the source strings.
     *
     * @see <a href="https://github.com/transifex/transifex-delivery/#push-content">
     *     https://github.com/transifex/transifex-delivery/#push-content</a>
     */
    public static class TxPostResponseData {
        public int created;
        public int updated;
        public int skipped;
        public int deleted;
        public int failed;
        public String[] errors;
    }

    /**
     * A class holding key-value pairs for some locale's strings.
     */
    public static class LocaleStrings {

        @SerializedName("map")
        private final HashMap<String, StringInfo> mHashMap;

        /**
         * Creates a LocaleStrings object which uses the provided {@link HashMap}.
         * <p>
         * The HashMap should contain resource entry names as keys and {@link StringInfo} as values
         * like this:
         * <p>
         * <pre>
         *    {
         *        'key1' : { 'string' : '...' },
         *        'key2' : { 'string' : '...' },
         *    }
         * </pre>
         */
        public LocaleStrings(@NonNull HashMap<String, StringInfo> map) {
            mHashMap = map;
        }

        /**
         * Return the string value associated with the provided key, or <code>null</code>
         * if it isn't found.
         */
        @Nullable
        public String get(String key) {
            StringInfo stringInfo = mHashMap.get(key);
            if (stringInfo != null) {
                return stringInfo.string;
            }
            return null;
        }

        @NonNull
        @Override
        public String toString() {
            return mHashMap.toString();
        }
    }

    /**
     * A class that maps {@link LocaleStrings} to locales.
     */
    public static class TranslationMap {

        @SerializedName("map")
        private final HashMap<String, LocaleStrings> mHashMap;

        /**
         * Constructs an empty <tt>TranslationMap</tt> with the specified initial capacity.
         *
         * @param initialCapacity The initial capacity. Set to the number of expected locales.
         */
        public TranslationMap(int initialCapacity) {
            mHashMap = new HashMap<>(initialCapacity);
        }

        /**
         *  Creates a TranslationMap object which uses the provided {@link HashMap}.
         * <p>
         *  The HashMap should contain locale codes as keys and {@link LocaleStrings} as values like
         *  this:
         * <pre>
         *    {
         *        'fr' : {
         *             'key1' : { 'string' : '...' },
         *             'key2' : { 'string' : '...' },
         *        },
         *        'de' : {
         *             'key1' : { 'string' : '...' },
         *        },
         *        'el' : {
         *             'key1' : { 'string' : '...' },
         *        },
         *    }
         * </pre>
         */
        public TranslationMap(@NonNull HashMap<String, LocaleStrings> map) {
            this.mHashMap = map;
        }

        /**
         *  Associates the specified locale with the specified {@link LocaleStrings} object in this
         *  map.
         */
        public void put(@NonNull String locale, @NonNull LocaleStrings localeStrings) {
            mHashMap.put(locale, localeStrings);
        }

        /**
         * Return the {@code LocaleStrings} object associated with the provided locale, or
         * <code>null</code> if it isn't found.
         */
        @Nullable public LocaleStrings get(String locale) {
            return mHashMap.get(locale);
        }

        /**
         * Returns a <code>Set</code> with the locales supported by the <code>TranslationMap</code>.
         */
        @NonNull public Set<String> getLocales() {
            return mHashMap.keySet();
        }

        @NonNull
        @Override
        public String toString() {
            return mHashMap.toString();
        }
    }

}