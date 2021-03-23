package com.transifex.txnative.missingpolicy;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.PluralsRes;
import androidx.annotation.StringRes;

/**
 * Returns the string using Android's localization system.
 */
public class AndroidMissingPolicy implements MissingPolicy{

    Context context;

    /**
     * Creates a new instance.
     *
     * @param applicationContext The application context. <b>Do not provide</b> a context wrapped by
     *                           {@link com.transifex.txnative.TxNative#wrap(Context) TxNative#wrap(Context)}
     *                           or {@link com.transifex.txnative.TxNative#generalWrap(Context) TxNative#generalWrap(Context)}.
     */
    public AndroidMissingPolicy(@NonNull Context applicationContext) {
        this.context = applicationContext;
    }

    /**
     * Returns a translated string using Android's localization system.
     * <p>
     * The result is equivalent to calling {@link android.content.res.Resources#getText(int)}
     * without using TxNative functionality.
     */
    @Override
    @NonNull public CharSequence get(@NonNull CharSequence sourceString, @StringRes int id,
                                     @NonNull String resourceName, @NonNull String locale) {
        return context.getResources().getText(id);
    }

    /**
     * Returns a translated quantity string using Android's localization system.
     * <p>
     * The result is equivalent to calling {@link android.content.res.Resources#getQuantityText(int, int)}
     * without using TxNative functionality.
     */
    @Override
    @NonNull public CharSequence getQuantityString(
            @NonNull CharSequence sourceQuantityString, @PluralsRes int id, int quantity,
            @NonNull String resourceName, @NonNull String locale) {
        return context.getResources().getQuantityText(id, quantity);
    }
}
