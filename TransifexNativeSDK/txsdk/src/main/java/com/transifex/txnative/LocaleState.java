package com.transifex.txnative;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Keeps track of the locale-related information for the application, such as supported locales,
 * source and current locale.
 * <p>
 * Source locale is the locale used as the source when uploading the files to Transifex.
 * <p>
 * AppLocales is the list of all locales, including the source locale, that this application
 * supports.
 */
public class LocaleState {

    private final String mSourceLocale;
    private final String[] mAppLocales;
    private final String[] mTranslatedLocales;
    private Locale mCurrentLocale;

    private final Context mContext;
    private CurrentLocaleListener mListener = null;
    private boolean isRegistered;

    interface CurrentLocaleListener {

        void onLocaleChanged(Locale newLocale);
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
     * @param currentLocale Set to <code>null</code> to use the system's locale or to a specific
     *                      locale, if your app uses its own locale.
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
            mAppLocales = new String[]{mSourceLocale};
        }
        else {
            // Make sure that mAppLocales contains mSourceLocale
            ArrayList<String> appLocalesList = new ArrayList<>(Arrays.asList(appLocales));
            if (!appLocalesList.contains(mSourceLocale)) {
                appLocalesList.add(0, mSourceLocale);
                mAppLocales = appLocalesList.toArray(new String[0]);
            }
            else {
                mAppLocales = appLocales;
            }
        }

        ArrayList<String> translatedLocales = new ArrayList<>(Arrays.asList(mAppLocales));
        translatedLocales.remove(mSourceLocale);
        mTranslatedLocales = translatedLocales.toArray(new String[0]);

        setCurrentLocale(currentLocale);
    }

    /**
     * The source locale.
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
        return mAppLocales;
    }

    /**
     * An array containing the app's locales without the source locale.
     * <p>
     * The array can be empty.
     */
    public @NonNull String[] getTranslatedLocales() {
        return mTranslatedLocales;
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
            IntentFilter filter = new IntentFilter(Intent.ACTION_LOCALE_CHANGED);
            if (!isRegistered) {
                mContext.registerReceiver(mBroadcastReceiver, filter);
                isRegistered = true;
            }
        }
        else {
            newLocale = currentLocale;

            // Unregister receiver for system locale
            if (isRegistered) {
                mContext.unregisterReceiver(mBroadcastReceiver);
                isRegistered = false;
            }
        }

        setCurrentLocaleInternal(newLocale);
    }

    /**
     * Sets the current locale and calls the listener if the value changed.
     *
     * @param currentLocale The current locale.
     */
    private void setCurrentLocaleInternal(Locale currentLocale) {
        if (!Utils.equals(currentLocale, mCurrentLocale)) {
            mCurrentLocale = currentLocale;

            if (mListener != null) {
                mListener.onLocaleChanged(mCurrentLocale);
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
