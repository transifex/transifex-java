package com.transifex.txnative;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.transifex.txnative.cache.TxCache;
import com.transifex.txnative.missingpolicy.MissingPolicy;
import com.transifex.txnative.wrappers.TxContextWrapper;

import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.app.TxContextWrappingDelegateJava2;
import androidx.appcompat.app.ViewPumpAppCompatDelegate;
import dev.b3nedikt.viewpump.ViewPump;


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
        ViewPump.init(new TxInterceptor());
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
     * where all tags are parsed into spans. If disabled, a  {@link String} is returned at all times
     * and all tags are kept as plain text. It's enabled by default.
     * <p>
     * Leave it enabled, if you have strings that contain HTML tags using {@code "<"} and  {@code ">"}
     * characters and you want HTML styling to be applied when the strings are referenced in your
     * layout.
     * <p>
     * Disable it if your strings are HTML-escaped such as the following:
     * <pre>{@code
     * <resources>
     *   <string name="welcome_messages">Hello, %1$s! You have &lt;b>%2$d new messages&lt;/b>.</string>
     * </resources>
     * }
     * </pre>
     * In this case, you can use {@link androidx.core.text.HtmlCompat#fromHtml(String, int)}
     * on the result of <code>getText()</code> or <code>getString()</code> to get a SpannedString,
     * which you can set to a view programmatically.
     *
     * @see <a href="https://developer.android.com/guide/topics/resources/string-resource#StylingWithHTML">
     *     https://developer.android.com/guide/topics/resources/string-resource#StylingWithHTML</a>
     * @see NativeCore#getSpannedString(String)
     */
    public static void setSupportSpannable(boolean enabled ){
        if (sNativeCore == null) {
            throw new RuntimeException("TxNative has not been initialized");
        }

        sNativeCore.setSupportSpannable(enabled);
    }

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
     * @param tags An optional set of tags. If defined, only strings that have all of the given tags
     *             will be fetched.
     */
    public static void fetchTranslations(@Nullable String localeCode, @Nullable Set<String> tags) {
        if (sNativeCore == null) {
            throw new RuntimeException("TxNative has not been initialized");
        }

        sNativeCore.fetchTranslations(localeCode, tags);
    }

    /**
     * Fetches the translations from CDS and updates the cache.
     * 
     * @see #fetchTranslations(String, Set) 
     */
    public static void fetchTranslations(@Nullable String localeCode) {
        if (sNativeCore == null) {
            throw new RuntimeException("TxNative has not been initialized");
        }

        sNativeCore.fetchTranslations(localeCode, null);
    }

    /**
     * Wraps a context to enable TransifexNative functionality inside activities, services or
     * other scopes.
     *
     * <p>
     *     Check out the installation guide regarding the usage of this method.
     * <p>
     *   <b>Warning: </b>You should use <code>getBaseContext()</code>, instead of
     *      <code>getApplicationContext()</code> when using string methods in services.

     *
     * @param context The activity context to wrap.
     *
     * @return The wrapped context.
     */
    public static Context wrap(@NonNull Context context) {
        if (sNativeCore == null) {
            Log.e(TAG, "Wrapping failed because TxNative has not been initialized yet");
            return context;
        }

        if (context.getResources() instanceof TxResources) {
            Log.w(TAG, "Provided context is already wrapped.");
            return context;
        }

        return new TxContextWrapper(context, sNativeCore);
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
    @Deprecated
    public static Context generalWrap(@NonNull Context context) {
        return wrap(context);
    }

    /**
     * Wraps the {@link AppCompatDelegate} to enable TransifexNative functionality in an activity
     * that extends {@link androidx.appcompat.app.AppCompatActivity}.
     * <p>
     * This method should be called in {@link AppCompatActivity#getDelegate()}.
     *
     * @param delegate The activity's AppCompatDelegate.
     * @param baseContext The activity's base context.
     *
     * @return The wrapped AppCompatDelegate.
     */
    public static @NonNull AppCompatDelegate wrapAppCompatDelegate(@NonNull AppCompatDelegate delegate, @NonNull Context baseContext) {
        return new ViewPumpAppCompatDelegate(delegate, baseContext, TxNative::wrap);
    }
}
