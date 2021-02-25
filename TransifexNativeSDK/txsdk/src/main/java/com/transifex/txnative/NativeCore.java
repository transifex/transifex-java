package com.transifex.txnative;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.TextUtils;

import com.transifex.common.LocaleData;
import com.transifex.txnative.missingpolicy.MissingPolicy;
import com.transifex.txnative.missingpolicy.SourceStringPolicy;

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.text.HtmlCompat;
import com.transifex.txnative.cache.MemoryCache;
import com.transifex.txnative.cache.TxCache;

/**
 * The main class of the framework, responsible for orchestrating all functionality.
 */
public class NativeCore {

    public static final String TAG = NativeCore.class.getSimpleName();

    final Context mContext;
    final LocaleState mLocaleState;
    final TxCache mCache;
    final MissingPolicy mMissingPolicy;

    final Handler mMainHandler;
    final CDSHandlerAndroid mCDSHandler;
    final Resources mDefaultResources;      // Non-localized resources

    boolean mTestModeEnabled;
    boolean mSupportSpannableEnabled;

    /**
     * Create an instance of the core SDK class.
     * <p>
     * We initialize and set-up the rest of the SDK classes and enable ViewPump interception.
     *
     * @param applicationContext The application context.
     * @param localeState Keeps track of the available and current locales.
     * @param token The Transifex token that can be used for retrieving translations from CDS.
     * @param cdsHost An optional host for the Content Delivery Service; defaults to the production
     *                host provided by Transifex.
     * @param cache The translation cache that holds the translations from the CDS; MemoryCache is
     *             used if set to <code>null</code>.
     * @param missingPolicy Determines how to handle translations that are not available;
     * {@link com.transifex.txnative.missingpolicy.SourceStringPolicy SourceStringPolicy} is used
     *                     if set to <code>null</code>.
     */
    public NativeCore(@NonNull Context applicationContext,
                      @NonNull LocaleState localeState,
                      @NonNull String token,
                      @Nullable String cdsHost,
                      @Nullable TxCache cache,
                      @Nullable MissingPolicy missingPolicy) {
        mContext = applicationContext.getApplicationContext();
        mMainHandler = new Handler(mContext.getMainLooper());
        mLocaleState = localeState;
        mLocaleState.setCurrentLocaleListener(mCurrentLocaleListener);
        mCache = (cache != null) ? cache : new MemoryCache();
        mMissingPolicy = (missingPolicy != null) ? missingPolicy : new SourceStringPolicy();

        if (cdsHost == null) {
            cdsHost = CDSHandlerAndroid.CDS_HOST;
        }
        mCDSHandler = new CDSHandlerAndroid(mLocaleState.getTranslatedLocales(), token, null, cdsHost);

        mDefaultResources = Utils.getDefaultLanguageResources(mContext);
    }

     private final LocaleState.CurrentLocaleListener mCurrentLocaleListener = new LocaleState.CurrentLocaleListener() {
         @Override
         public void onLocaleChanged(@NonNull Locale newLocale, @Nullable String resolvedLocale) {
             // Do nothing
         }
    };

    /**
     * @see TxNative#setTestMode(boolean)
     */
    void setTestMode(boolean enabled) {
        mTestModeEnabled = enabled;
    }

    /**
     * @see TxNative#setSupportSpannable(boolean)
     */
    void setSupportSpannable(boolean enabled) {
        mSupportSpannableEnabled = enabled;
    }

    /**
     * Fetches translations from CDS.
     *
     * @param localeCode If set to <code>null</code>, it will fetch translations for all locales
     *                   as defined in the SDK configuration.
     */
    void fetchTranslations(@Nullable String localeCode) {
        mCDSHandler.fetchTranslationsAsync(localeCode, new CDSHandlerAndroid.FetchTranslationsCallback() {
            @Override
            public void onComplete(final @Nullable LocaleData.TranslationMap translationMap) {
                if (translationMap != null) {

                    // Update mCache using the fetched translationMap in main thread
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mCache.update(translationMap);
                        }
                    });
                }
            }
        });
    }

    /**
     * Return the value of the provided string resource ID under the current locale.
     * <p>
     * This method has similar semantics to {@link Resources#getText(int)} in terms of handling a
     * non existing ID: throws NotFoundException if the given ID does not exist; the returned
     * string is never <code>null</code>.
     *
     * @return The string data associated with the resource, plus possibly styled text information.
     *
     * @throws Resources.NotFoundException Throws NotFoundException if the given ID does not exist
     */
    @NonNull CharSequence translate(TxResources txResources, @StringRes int id) {
        //noinspection ConstantConditions
        return internalTranslate(txResources, id, null, false);
    }

    /**
     * Return the value of the provided string resource ID under the current locale.
     * <p>
     * This method has similar semantics to {@link Resources#getText(int, CharSequence)} in terms of
     * handling a non existing ID: returns <code>def</code>, which can be <code>null</code>, if the given ID
     * does not exist.
     *
     * @return The string data associated with the resource, plus possibly styled text information,
     * or <code>def</code> if <code>id</code> is 0 or not found.
     */
    @Nullable CharSequence translate(TxResources txResources, @StringRes int id, @Nullable CharSequence def) {
        return internalTranslate(txResources, id, def, true);
    }

    /**
     * Return the value of the provided string resource ID under the current locale.
     * <p>
     * If <coode>shouldUseDef</coode> is <code>false</code>, the semantics are described by
     * {@link #translate(TxResources, int)}. If it's <code>true</code>, they are described by
     * {@link #translate(TxResources, int, CharSequence)}.
     *
     * @param txResources TxResources instance.
     * @param id The resource ID.
     * @param def The default CharSequence to return if ID is not found. It's used if shouldUseDef
     *            is true. Otherwise, it's ignored.
     * @param shouldUseDef Set to false for {@link #translate(TxResources, int)} semantics. Set to
     *                     true for {@link #translate(TxResources, int, CharSequence)} semantics.
     *                     When set to true, <code>def</code> is returned if the <code>id</code> is
     *                     not found.
     *
     * @return The string data associated with the resource, plus possibly styled text information,
     * or <code>def</code> if <code>id</code> is 0 or not found and <code>shouldUseDef</code> is true.
     */
    @Nullable private CharSequence internalTranslate(TxResources txResources, @StringRes int id,
                                                     @Nullable CharSequence def, boolean shouldUseDef) {
        try {
            // We don't want to alter string resources, such as
            // "config_inputEventCompatProcessorOverrideClassName", that belong to the android resource
            // package
            if (txResources.isAndroidStringResource(id)) {
                return txResources.getOriginalText(id);
            }
        }
        catch (Resources.NotFoundException e) {
            // The provided ID does not exist. If "shouldUseDef" is true, we emulate the getText(int)
            // behavior. If it's false, we emulate the getText(int, CharSequence) behavior
            if (shouldUseDef) {
                // Return def if the provided id does not exist.
                return def;
            }
            else {
                // Throw exception if the provided id does not exist.
                throw e;
            }
        }

        if (mTestModeEnabled) {
            return TextUtils.concat("test: ", txResources.getOriginalText(id));
        }

        if (mLocaleState.isSourceLocale()) {
            return txResources.getOriginalText(id);
        }

        String translatedString = null;
        if (mLocaleState.getResolvedLocale() != null) {
            translatedString = mCache.get(txResources.getResourceEntryName(id),
                    mLocaleState.getResolvedLocale());
        }

        // String can be null/empty if:
        // 1. our Cache has not been updated with translations yet
        // 2. the resolved locale is null: there is no app locale that matches the current locale
        // 3. our Cache does not have translations for the resolved locale (this shouldn't happen)
        // 4. the key was not found in the Cache for the resolved locale
        if (TextUtils.isEmpty(translatedString)) {
            CharSequence sourceString = mDefaultResources.getText(id);
            return mMissingPolicy.get(sourceString);
        }

        if (mSupportSpannableEnabled) {
            // If a span was found, return a "Spanned" object. Otherwise, return "String".
            Spanned spanned = HtmlCompat.fromHtml(translatedString, HtmlCompat.FROM_HTML_MODE_LEGACY);
            if (spanned.getSpans(0, spanned.length(), Object.class).length != 0) {
                return new SpannedString(spanned);
            }
            else {
                return spanned.toString();
            }
        }
        else {
            // This is faster than "fromHTML()" and is enough for most cases
            return translatedString.replaceAll("&lt;", "<");
        }
    }
}
