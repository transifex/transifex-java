package com.transifex.txnative;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.transifex.txnative.missingpolicy.MissingPolicy;
import com.transifex.txnative.wrappers.TxContextWrapper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.transifex.txnative.cache.TxCache;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;


/**
 * The entry point of TransifexNative SDK.
 */
public class TxNative {

    public static final String TAG = TxNative.class.getSimpleName();

    private static NativeCore sNativeCore = null;

    /**
     * Initialize the SDK.
     * <p>
     *     Should be called in {@link Application#onCreate()}.
     * </p>
     *
     * @param applicationContext The application context.
     * @param locales Configures the locales supported by the SDK.
     * @param token The Transifex token that can be used for retrieving translations from CDS.
     * @param cdsHost An optional host for the Content Delivery Service; if set to <code>null</code>,
     *               the production host provided by Transifex is used.
     * @param cache The translation cache that holds the translations from the CDS;
     * {@link com.transifex.txnative.cache.TxStandardCache TxStandardCache} is used if set to
     *              <code>null</code>.
     * @param missingPolicy Determines how to handle translations that are not available;
     * {@link com.transifex.txnative.missingpolicy.SourceStringPolicy SourceStringPolicy} is used
     *                     if set to <code>null</code>.
     */
    public static void init(@NonNull Context applicationContext,
                            @NonNull LocaleState locales,
                            @NonNull String token,
                            @Nullable String cdsHost,
                            @Nullable TxCache cache,
                            @Nullable MissingPolicy missingPolicy) {

        if (sNativeCore != null) {
            throw new RuntimeException("TxNative is already initialized");
        }

        sNativeCore = new NativeCore(applicationContext, locales, token, cdsHost, cache, missingPolicy);

        // Initialize ViewPump with our interceptor
        ViewPump.Builder viewPumpBuilder = ViewPump.builder();
        viewPumpBuilder.addInterceptor(new TxInterceptor());
        ViewPump viewPump = viewPumpBuilder.build();
        ViewPump.init(viewPump);
    }

    /**
     * When test mode is enabled, TransifexNative functionality is disabled: the translations provided
     * by the SDK are not used. The original strings, as provided by Android's localization system,
     * are returned after being prefixed with "test:".
     * <p>
     * Test mode can be toggled multiple times while the app is running. The activity has to be
     * recreated so that the strings are reloaded.
     */
    public static void setTestMode(boolean enabled) {
        if (sNativeCore == null) {
            throw new RuntimeException("TxNative has not been initialized");
        }

        sNativeCore.setTestMode(enabled);
    }

    /**
     * If enabled, the <code>getText()</code> method can return a {@link android.text.SpannedString SpannedString}
     * if tags exist in the source string. If disabled, a plain String is always returned. It's
     * disabled by default.
     * <p>
     * Enable it, if your strings contain HTML tags using "<" and ">" characters. Leave it disabled
     * if your strings are HTML-escaped such as the following:
     * <pre>{@code
     * <resources>
     *   <string name="welcome_messages">Hello, %1$s! You have &lt;b>%2$d new messages&lt;/b>.</string>
     * </resources>
     * }
     * </pre>
     *
     * <p>
     * If enabled, {@link android.text.Html#fromHtml(String, int) Html#fromHtml(String, int)} is
     * used internally which is more CPU demanding than if left disabled.
     *
     * @see <a href="https://developer.android.com/guide/topics/resources/string-resource#StylingWithHTML">
     *     https://developer.android.com/guide/topics/resources/string-resource#StylingWithHTML</a>
     */
    public static void setSupportSpannable(boolean enabled ){
        if (sNativeCore == null) {
            throw new RuntimeException("TxNative has not been initialized");
        }

        sNativeCore.setSupportSpannable(enabled);
    }

    //TODO: update the documentation, when local cache is implemented, to explain when these
    // translations affect the app. Currently, they affect it instantly but the activity has
    // to be reloaded after they have been fetched.

    /**
     * Fetches the translations from CDS and updates the cache.
     * <p>
     * The call returns instantly and fetches the translations asynchronously. If the translations
     * are fetched successfully, the cache is updated.
     * <p>
     * Note that updating the cache may or may not affect the translations shown in the app's UI.
     * This depends on the cache's implementation. Read
     * {@link com.transifex.txnative.cache.TxStandardCache here} for the default cache
     * implementation used by the TxNative SDK.
     *
     * @param localeCode An optional locale to fetch translations for; if  set to <code>null</code>,
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
     *
     * @return The wrapped context.
     */
    public static Context wrap(Context context) {
        if (sNativeCore == null) {
            Log.e(TAG, "Wrapping failed because TxNative has not been initialized yet");
            return context;
        }

        return ViewPumpContextWrapper.wrap(new TxContextWrapper(context, sNativeCore));
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
     *
     * @return The wrapped context.
     */
    public static Context generalWrap(Context context) {
        if (sNativeCore == null) {
            Log.e(TAG, "Wrapping failed because TxNative has not been initialized yet");
            return context;
        }

        return new TxContextWrapper(context, sNativeCore);
    }
}
