package com.transifex.txnative;

import android.app.Application;
import android.content.Context;

import com.transifex.txnative.wrappers.TxContextWrapper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;


/**
 * The entry point of TransifexNative SDK.
 */
public class TxNative {

    public static final String TAG = TxNative.class.getSimpleName();

    private static NativeCore sNativeCore = null;

    /**
     *
     */
    /**
     * Initialize the SDK.
     * <p>
     *     Should be called in {@link Application#onCreate()}.
     * </p>
     *
     * @param applicationContext The application context.
     * @param locales Configures the locales supported by the SDK.
     * @param token The Transifex token that can be used for retrieving translations from CDS.
     * @param cdsHost An optional host for the Content Delivery Service; defaults to the production
     *                host provided by Transifex.
     */
    public static void init(@NonNull Context applicationContext,
                            @NonNull LocaleState locales,
                            @NonNull String token,
                            @Nullable String cdsHost) {

        if (sNativeCore != null) {
            throw new RuntimeException("TxNative is already initialized");
        }

        sNativeCore = new NativeCore(applicationContext, locales, token, cdsHost);
    }

    //TODO: mention that it works asynchronously and how it affects the cache when we have local
    // cache implemented

    /**
     * Fetches the translations from CDS.
     *
     * @param localeCode An optional locale to fetch translations from; if  set to <code>null</code>,
     *                   it will fetch translations for all locales as defined in the SDK
     *                   configuration.
     */
    public static void fetchTranslations(@Nullable String localeCode) {
        if (sNativeCore == null) {
            throw new RuntimeException("TxNative has not been initialized");
        }

        sNativeCore.fetchTranslations(localeCode);
    }

    /**
     * Wraps the activity's base context to enable TransifexNative functionality inside activities.
     *
     * <p>
     *     Check out the installation guide regarding the usage of this method.
     * </p>
     *
     * @param context The activity context to wrap.
     * @return The wrapped context.
     */
    public static Context wrap(Context context) {
        return ViewPumpContextWrapper.wrap(new TxContextWrapper(context));
    }

    /**
     * Wraps a context to enable TransifexNative functionality in services and other scopes besides
     * activities.
     * <p>
     * <b>Warning: </b>You should use <code>getBaseContext()</code>, instead of
     * <code>getApplicationContext()</code> when using string methods in services.
     * <p>
     * Check out the installation guide regarding the usage of this method.
     *
     * @param context The service context to wrap.
     * @return teh wrapped context.
     */
    public static Context generalWrap(Context context) {
        return new TxContextWrapper(context);
    }
}
