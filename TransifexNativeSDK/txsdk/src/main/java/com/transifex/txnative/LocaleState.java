package com.transifex.txnative;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Keeps track of the locale-related information for the application, such as supported locales,
 * source, current locale and resolved locales.
 */
public class LocaleState {

    private static final String TAG = LocaleState.class.getSimpleName();

    private static final boolean DEBUG = false;

    private final String mSourceLocale;
    private final LinkedHashSet<String> mAppLocales;
    private final String[] mTranslatedLocales;
    private Locale mCurrentLocale;
    private String mResolvedLocale;
    private boolean isSourceLocale;

    private final Context mContext;
    private CurrentLocaleListener mListener = null;
    private boolean mRegisteredSystemLocaleReceiver;

    /**
     * A listener of the current locale.
     */
    interface CurrentLocaleListener {

        /**
         * Called when the current locale changes.
         *
         * @param newLocale The new locale. This can be the system locale or the locale set by the
         *                  user, when {@link #setCurrentLocale(Locale)} is called.
         * @param resolvedLocale The resolved locale according to the new locale and the app locales;
         *                       <code>null</code> if no matching locale was found
         */
        void onLocaleChanged(@NonNull Locale newLocale, @Nullable String resolvedLocale);
    }

    /**
     * Creates a LocaleState instance.
     *
     * @param applicationContext  The application context.
     * @param sourceLocale  The locale of the source language, defaults to "en" if <code>null</code>
     *                      is provided.
     * @param appLocales    A list of all locales supported by the application, including the source
     *                      locale; defaults to [sourceLocale]
     *                      if <code>null</code> is provided.
     * @param currentLocale Set to <code>null</code> to use the system's locale, or set to a specific
     *                      locale if your app uses its own locale.
     *
     * @see #setCurrentLocale(Locale)
     */
    public LocaleState(@NonNull Context applicationContext,
                       @Nullable String sourceLocale, @Nullable String[] appLocales,
                       @Nullable Locale currentLocale) {
        mContext = applicationContext;

        if (sourceLocale == null) {
            mSourceLocale = "en";
        }
        else {
            mSourceLocale = sourceLocale;
        }

        if (appLocales == null) {
            mAppLocales = new LinkedHashSet<>(1);
            mAppLocales.add(sourceLocale);
        }
        else {
            // Make sure that mAppLocales contains mSourceLocale
            ArrayList<String> appLocalesList = new ArrayList<>(Arrays.asList(appLocales));
            if (!appLocalesList.contains(mSourceLocale)) {
                appLocalesList.add(0, mSourceLocale);
            }

            mAppLocales = new LinkedHashSet<>(appLocalesList);
        }

        ArrayList<String> translatedLocales = new ArrayList<>(mAppLocales);
        translatedLocales.remove(mSourceLocale);
        mTranslatedLocales = translatedLocales.toArray(new String[0]);

        setCurrentLocale(currentLocale);
    }

    /**
     * The source locale.
     * <p>
     * This is the locale that is used as the source when uploading the files to Transifex.
     *
     * @see LocaleState
     */
    public @NonNull String getSourceLocale() {
        return mSourceLocale;
    }

    /**
     * The app's supported locales, including the source locale.
     *
     * @see LocaleState
     */
    public @NonNull String[] getAppLocales() {
        return mAppLocales.toArray(new String[0]);
    }

    /**
     * The app's supported locales without the source locale.
     * <p>
     * The array can be empty.
     */
    public @NonNull String[] getTranslatedLocales() {
        return mTranslatedLocales;
    }

    /**
     * The current locale as provided by Android or the the locale set
     * using {@link #setCurrentLocale(Locale)}.
     */
    public @NonNull Locale getCurrentLocale() {
        return mCurrentLocale;
    }

    /**
     * The resolved locale is one of the app locales that best matches the current locale.
     *
     * @return The resolved locale or <code>null</code> if no app locale matches the current locale.
     */
    public @Nullable String getResolvedLocale() {
        return mResolvedLocale;
    }

    /**
     * Set the locale used in the app.
     * <p>
     * If set to <code>null</code>, the SDK will automatically use the one selected at Android's
     * settings when the app starts and will update to another locale if it's changed by the user
     * while the app runs.
     * <p>
     * If  your app has its own implementation for selecting locale, you should call this method
     * when initializing the SDK and when the locale changes.
     * <p>
     * <strong>Warning:</strong> if you set a custom locale, your app should also call the necessary
     * Android methods for updating the locale there as well. If not, the SDK may not work correctly
     * in plurals and some other cases.
     *
     * @param currentLocale Set to <code>null</code> to use the system's locale or to a specific
     *                      locale, if your app uses its own locale.
     */
    public void setCurrentLocale(@Nullable Locale currentLocale) {
        Locale newLocale;

        // We update mCurrentLocale and also register or unregister our broadcast receiver

        if (currentLocale == null) {
            newLocale = Utils.getCurrentLocale(mContext);

            // Register receiver for system locale
            if (!mRegisteredSystemLocaleReceiver) {
                IntentFilter filter = new IntentFilter(Intent.ACTION_LOCALE_CHANGED);
                mContext.registerReceiver(mBroadcastReceiver, filter);
                mRegisteredSystemLocaleReceiver = true;
            }
        }
        else {
            newLocale = currentLocale;

            // Unregister receiver for system locale
            if (mRegisteredSystemLocaleReceiver) {
                mContext.unregisterReceiver(mBroadcastReceiver);
                mRegisteredSystemLocaleReceiver = false;
            }
        }

        setCurrentLocaleInternal(newLocale);
    }

    /**
     * Returns <code>true</code> if the source locale matches {@link #getResolvedLocale()
     * the resolved locale}. If it's different or the resolved locale is <code>null</code>, it
     * returns <code>false</code>.
     */
    public boolean isSourceLocale() {
        return isSourceLocale;
    }

    /**
     * Sets the current locale and calls the listener if the value changed.
     *
     * @param currentLocale The current locale.
     */
    private void setCurrentLocaleInternal(Locale currentLocale) {
        if (!Utils.equals(currentLocale, mCurrentLocale)) {
            mCurrentLocale = currentLocale;

            // Find an app locale that best matches the current locale according to Android's
            // resolution strategy: https://developer.android.com/guide/topics/resources/multilingual-support
            mResolvedLocale = null;

            // Try matching both language and region
            if (mAppLocales.contains(mCurrentLocale.toString())) {
                mResolvedLocale = mCurrentLocale.toString();
            }
            // Try matching the closest parent dialect (a locale without a region)
            else if (mAppLocales.contains(mCurrentLocale.getLanguage())) {
                mResolvedLocale = mCurrentLocale.getLanguage();
            }
            // Try matching children locales (locales with different regions)
            else {
                for (String appLocale : mAppLocales) {
                    if (appLocale.startsWith(currentLocale.getLanguage())) {
                        mResolvedLocale = appLocale;
                        break;
                    }
                }
            }

            isSourceLocale = mSourceLocale.equals(mResolvedLocale);

            if (DEBUG) {
                Log.d(TAG, "Locale: " + currentLocale + " resolved: " + mResolvedLocale);
            }

            if (mListener != null) {
                mListener.onLocaleChanged(mCurrentLocale, mResolvedLocale);
            }
        }
    }

    // A receiver of Android's locale changes
    private final BroadcastReceiver mBroadcastReceiver  = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Locale locale = Utils.getCurrentLocale(context);
            setCurrentLocaleInternal(locale);
        }
    };

    /**
     * Sets a listener to be called when the current locale changes.
     * <p>
     * Listener will be called on main thread.
     */
    void setCurrentLocaleListener(CurrentLocaleListener listener) {
        mListener = listener;
    }
}
