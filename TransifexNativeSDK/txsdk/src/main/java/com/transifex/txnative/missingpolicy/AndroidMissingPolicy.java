package com.transifex.txnative.missingpolicy;

import android.content.Context;
import android.content.res.Resources;

import androidx.annotation.NonNull;
import androidx.annotation.PluralsRes;
import androidx.annotation.StringRes;

/**
 * Returns a translated string using Android's localization system.
 * <p>
 * You can use this policy to fall back to translations provided via <code>strings.xml</code>
 * when a translation string can't be provided by TxNative's cache.
 */
public class AndroidMissingPolicy implements MissingPolicy{


    public AndroidMissingPolicy(){

    }

    /**
     * Creates a new instance.
     * <p>
     * This constructor has been deprecated. A context is no longer needed.
     *
     * @param applicationContext The application context. <b>Do not provide</b> a context wrapped by
     *                           {@link com.transifex.txnative.TxNative#wrap(Context) TxNative#wrap(Context)}.
     */
    @Deprecated
    public AndroidMissingPolicy(@NonNull Context applicationContext) {
    }

    /**
     * Returns a translated string using Android's localization system.
     * <p>
     * The result is equivalent to calling {@link android.content.res.Resources#getText(int)}
     * without using TxNative functionality.
     */
    @Override
    @NonNull public CharSequence get(@NonNull Resources resources,
                                     @NonNull CharSequence sourceString, @StringRes int id,
                                     @NonNull String resourceName, @NonNull String locale) {
        return resources.getText(id);
    }

    /**
     * Returns a translated quantity string using Android's localization system.
     * <p>
     * The result is equivalent to calling {@link android.content.res.Resources#getQuantityText(int, int)}
     * without using TxNative functionality.
     */
    @Override
    @NonNull public CharSequence getQuantityString(@NonNull Resources resources,
            @NonNull CharSequence sourceQuantityString, @PluralsRes int id, int quantity,
            @NonNull String resourceName, @NonNull String locale) {
        return resources.getQuantityText(id, quantity);
    }
}
