package com.transifex.txnative;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import io.github.inflationx.viewpump.ViewPump;

/**
 * The main class of the framework, responsible for orchestrating all functionality.
 */
public class NativeCore {

    public static final String TAG = NativeCore.class.getSimpleName();

    final Context mContext;
    final LocaleState mLocaleState;
    final Cache mCache;

    final Handler mMainHandler;
    final CDSHandler mCDSHandler;

    boolean mTestModeEnabled;

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
     * @param cache: The translation cache that holds the translations from the CDS; MemoryCache is
     *             used if set to <code>null</code>.
     */
    public NativeCore(@NonNull Context applicationContext,
                      @NonNull LocaleState localeState,
                      @NonNull String token,
                      @Nullable String cdsHost,
                      @Nullable Cache cache) {
        mContext = applicationContext.getApplicationContext();
        mMainHandler = new Handler(mContext.getMainLooper());
        mLocaleState = localeState;
        mLocaleState.setCurrentLocaleListener(mCurrentLocaleListener);
        mCache = (cache != null) ? cache : new MemoryCache();
        mCache.setCurrentLocale(mLocaleState.getResolvedLocale());

        if (cdsHost == null) {
            cdsHost = CDSHandler.CDS_HOST;
        }
        mCDSHandler = new CDSHandler(mLocaleState.getTranslatedLocales(), token, null, cdsHost);

        // Initialize ViewPump with our interceptor
        ViewPump.init(ViewPump.builder()
                .addInterceptor(new TxInterceptor())
                .build());

    }

     private final LocaleState.CurrentLocaleListener mCurrentLocaleListener = new LocaleState.CurrentLocaleListener() {
         @Override
         public void onLocaleChanged(@NonNull Locale newLocale, @Nullable String resolvedLocale) {
             mCache.setCurrentLocale(resolvedLocale);
         }
    };

    /**
     * @see TxNative#setTestMode(boolean)
     */
    void setTestMode(boolean enabled) {
        mTestModeEnabled = enabled;
    }

    /**
     * Fetches translations from CDS.
     *
     * @param localeCode If set to <code>null</code>, it will fetch translations for all locales
     *                   as defined in the SDK configuration.
     */
    void fetchTranslations(@Nullable String localeCode) {
        mCDSHandler.fetchTranslations(localeCode, new CDSHandler.FetchTranslationsCallback() {
            @Override
            public void onComplete(final @Nullable HashMap<String, JSONObject> translationMap) {
                if (translationMap != null) {
                    //Log.d(TAG, translationMap.toString());

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

    @Nullable String translate(TxResources txResources, @StringRes int id) {
        // We don't want to alter string resources, such as
        // "config_inputEventCompatProcessorOverrideClassName", that belong to the android resource
        // package
        if (txResources.isAndroidStringResource(id)) {
            return txResources.getOriginalString(id);
        }

        if (mTestModeEnabled) {
            return "test: " + txResources.getOriginalString(id);
        }

        if (mLocaleState.isSourceLocale()) {
            return txResources.getOriginalString(id);
        }

        String translatedString = mCache.get(txResources.getResourceEntryName(id));
        // String can be null if:
        // 1. our Cache has not been updated with translations yet
        // 2. the resolved locale is null: there is no app locale that matches the current locale
        // 3. our Cache does not have translations for the current locale (this shouldn't happen)
        // 4. the key was not found in the Cache for the current locale
        //TODO: implement missing policy

        if (translatedString == null) {
            return "";
        }

        //TODO: I may have to use Html.fromHtml() to strip HTML characters

        return translatedString;
    }
}
