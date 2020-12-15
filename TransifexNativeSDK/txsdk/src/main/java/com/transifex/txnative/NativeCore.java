package com.transifex.txnative;

import android.content.Context;
import android.util.Log;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.github.inflationx.viewpump.ViewPump;

/**
 * The main class of the framework, responsible for orchestrating all functionality.
 */
public class NativeCore {

    public static final String TAG = NativeCore.class.getSimpleName().toString();

    final Context mContext;
    final LocaleState mLocaleState;

    final CDSHandler mCDSHandler;


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
     */
    public NativeCore(@NonNull Context applicationContext,
                      @NonNull LocaleState localeState,
                      @NonNull String token,
                      @Nullable String cdsHost) {
        mContext = applicationContext;
        mLocaleState = localeState;

        mLocaleState.setCurrentLocaleListener(mCurrentLocaleListener);

        if (cdsHost == null) {
            cdsHost = CDSHandler.CDS_HOST;
        }
        mCDSHandler = new CDSHandler(mLocaleState.getTranslatedLocales(), token, null, cdsHost);

        // Initialize ViewPump with our interceptor
        ViewPump.init(ViewPump.builder()
                .addInterceptor(new TxInterceptor())
                .build());


    }

    private LocaleState.CurrentLocaleListener mCurrentLocaleListener = new LocaleState.CurrentLocaleListener() {
        @Override
        public void onLocaleChanged(Locale newLocale) {
            Log.d(TAG, "local changed to " + newLocale);
            //TODO: update the MemoryCache
        }
    };

    /**
     * Fetches translations from CDS.
     *
     * @param localeCode If set to <code>null</code>, it will fetch translations for all locales
     *                   as defined in the SDK configuration.
     */
    public void fetchTranslations(@Nullable String localeCode) {
        mCDSHandler.fetchTranslations(localeCode, new CDSHandler.FetchTranslationsCallback() {

            @Override
            public void onComplete(@Nullable HashMap<String, JSONObject> translationMap) {
                //TODO: update MemoryCache when implemented
                if (translationMap != null) {
                    Log.d(TAG, translationMap.toString());
                }
            }
        });
    }
}
