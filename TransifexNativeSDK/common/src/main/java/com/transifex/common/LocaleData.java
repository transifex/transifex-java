package com.transifex.common;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Classes that hold the localization data.
 */
public class LocaleData {

    //region CDS API

    /**
     * A class that holds a <code>String</code> value.
     */
    public static class StringInfo {

        public StringInfo(@NonNull String string) {
            this.string = string;
        }

        public StringInfo(@NonNull String string, @Nullable Meta meta) {
            this.string = string;
            this.meta = meta;
        }

        public final String string;

        public static class Meta {
            public Set<String> tags;

            @Override
            @NonNull
            public String toString() {
                return "{tags=" + tags + "}";
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                Meta meta = (Meta) o;

                return tags != null ? tags.equals(meta.tags) : meta.tags == null;
            }

            @Override
            public int hashCode() {
                return tags != null ? tags.hashCode() : 0;
            }
        }

        public Meta meta;

        public void appendTags(@NonNull Set<String> tags) {
            if (tags == null || tags.isEmpty()) {
                return;
            }
            if (meta == null) {
                meta = new Meta();
            }
            if (meta.tags == null) {
                meta.tags = new LinkedHashSet<>(tags);
            }
            else {
                meta.tags.addAll(tags);
            }
        }

        @Override
        @NonNull
        public String toString() {
            if (meta == null) {
                return "{" + "string='" + string + '\'' + '}';
            }
            else {
                return "{" + "string='" + string + '\'' + ", meta=" + meta + '}';
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            StringInfo that = (StringInfo) o;

            if (string != null ? !string.equals(that.string) : that.string != null) return false;
            return meta != null ? meta.equals(that.meta) : that.meta == null;
        }

        @Override
        public int hashCode() {
            if (string == null)
                return 0;
            // We don't care about meta
            return string.hashCode();
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

        public TxPullResponseData() {}

        public TxPullResponseData(@NonNull HashMap<String, StringInfo> data) {
            this.data = data;
        }

        @Override
        @NonNull
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

            @Override
            @NonNull
            public String toString() {
                return "{purge=" + purge + "}";
            }
        }

        public Meta meta;

        public TxPostData(@NonNull LinkedHashMap<String, StringInfo> data, @Nullable Meta meta) {
            this.data = data;
            this.meta = meta;
        }

        @Override
        @NonNull
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
        public Error[] errors;

        public boolean isSuccessful() {
            return errors == null || errors.length == 0;
        }

        public static class Error {
            public int status;
            public String code;
            public String title;
            public String detail;

            @Override
            public String toString() {
                return "Error{" +
                        "status=" + status +
                        ", code='" + code + '\'' +
                        ", title='" + title + '\'' +
                        ", detail='" + detail + '\'' +
                        '}';
            }
        }
    }

    //endregion

    //region txNative

    /**
     * A class holding some locale's strings. It maps <code>String</code> keys
     * to {@link StringInfo} objects.
     */
    public static class LocaleStrings {

        @SerializedName("map")
        private final HashMap<String, StringInfo> mHashMap;

        /**
         * Creates a LocaleStrings object which uses the provided {@link HashMap}.
         * <p>
         * The HashMap should map Android resource entry names to {@link StringInfo} objects like
         * this:
         * <p>
         * <pre>
         *    {
         *        'key1' : LocaleString,
         *        'key2' : LocaleString,
         *    }
         * </pre>
         */
        public LocaleStrings(@NonNull HashMap<String, StringInfo> map) {
            mHashMap = map;
        }

        /**
         * Creates a new LocaleStrings object by making a copy of the provided one.
         */
        public LocaleStrings(@NonNull LocaleStrings localeStrings) {
            // Just copy the mapping. No need to copy StringInfo objects since they are read-only.
            mHashMap = new HashMap<>(localeStrings.mHashMap);
        }

        /**
         * Creates an empty LocaleStrings object with the specified initial capacity.
         *
         * @param initialCapacity The initial capacity. Set to the number of expected strings.
         */
        public LocaleStrings(int initialCapacity) {
            mHashMap = new HashMap<>(initialCapacity);
        }

        /**
         * Associates the specified key with the specified {@link StringInfo} object.
         */
        public void put(@NonNull String key, @NonNull StringInfo stringInfo) {
            mHashMap.put(key, stringInfo);
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

        /**
         * Returns the underlying data structure.
         * <p>
         * Changes to the returned map, will affect the object.
         */
        public @NonNull HashMap<String, StringInfo> getMap() {
            return mHashMap;
        }

        @Override
        @NonNull
        public String toString() {
            if (mHashMap == null) {
                return "";
            }
            return mHashMap.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LocaleStrings that = (LocaleStrings) o;
            return (mHashMap == that.mHashMap) || (mHashMap != null && mHashMap.equals(that.mHashMap));
        }

        @Override
        public int hashCode() {
            if (mHashMap == null)
                return 0;
            return mHashMap.hashCode();
        }
    }

    /**
     * A class that holds translations for multiple locales. It maps locale codes to
     * {@link LocaleStrings} objects.
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
         *  The HashMap should map locale codes to {@link LocaleStrings} like this:
         * <pre>
         * {
         *    'fr' : LocaleStrings,
         *    'de' : LocaleStrings,
         *    'el' : LocaleStrings,
         * }
         * </pre>
         */
        public TranslationMap(@NonNull HashMap<String, LocaleStrings> map) {
            mHashMap = map;
        }

        /**
         * Creates a new TranslationMap object by making a copy of the provided one.
         */
        public TranslationMap(@NonNull TranslationMap translationMap) {
            mHashMap = new HashMap<>(translationMap.mHashMap.size());
            // Copy mapping and LocaleStrings objects
            for (Map.Entry<String, LocaleStrings> entry : translationMap.mHashMap.entrySet()) {
                LocaleStrings localeStrings = entry.getValue();
                LocaleStrings copiedLocaleStrings =
                        (localeStrings != null) ? new LocaleStrings(localeStrings) : null;
                mHashMap.put(entry.getKey(), copiedLocaleStrings);
            }
        }

        /**
         *  Associates the specified locale with the specified {@link LocaleStrings} object.
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

        /**
         * Returns <code>true</code> if the object contains no locale to LocaleStrings mappings.
         *
         * @return <code>true</code> if it's empty, <code>false</code> otherwise.
         */
        public boolean isEmpty() {
            return mHashMap.isEmpty();
        }

        @Override
        @NonNull
        public String toString() {
            if (mHashMap == null) {
                return "";
            }
            return mHashMap.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TranslationMap that = (TranslationMap) o;
            return (mHashMap == that.mHashMap) || (mHashMap != null && mHashMap.equals(that.mHashMap));
        }

        @Override
        public int hashCode() {
            if (mHashMap == null)
                return 0;
            return mHashMap.hashCode();
        }
    }

    //endregion

}