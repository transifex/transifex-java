package com.transifex.txnative.missingpolicy;


import android.content.res.Resources;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.PluralsRes;
import androidx.annotation.StringRes;

/**
 * Wraps the source string with a custom format.
 * <p>
 * Example:
 * <p>
 * {@code new WrappedStringPolicy(">>", "<<").get("Click here");}
 * <p>
 * Returns:
 * <p>
 * {@code ">>Click here<<"}
 */
public class WrappedStringPolicy implements MissingPolicy{

    private final String start;
    private final String end;
    private final int length;

    /**
     * Creates a new instance with the provided strings.
     *
     * @param start The string to prepend the source String. Can be <code>null</code>.
     * @param end The string to append to the source string. Can be <code>null</code>.
     */
    public WrappedStringPolicy(@Nullable String start, @Nullable String end) {
        this.start = start;
        this.end = end;

        int length = 0;
        if (!TextUtils.isEmpty(start)) {
            length += start.length();
        }
        if (!TextUtils.isEmpty(end)) {
            length += end.length();
        }
        this.length = length;
    }

    /**
     * Wraps the provided sourceString with the <code>start</code> and <code>end</code> strings.
     * <p>
     * If sourceString is {@link Spanned}, a {@link SpannedString} containing the same spans is
     * returned.
     */
    @NonNull CharSequence wrapString(@NonNull CharSequence sourceString) {
        if (TextUtils.isEmpty(start) && TextUtils.isEmpty(end)) {
            return sourceString;
        }

        boolean isSpanned = sourceString instanceof Spanned;
        if (isSpanned) {
            SpannableStringBuilder sb = new SpannableStringBuilder();
            if (!TextUtils.isEmpty(start)) {
                sb.append(start);
            }
            sb.append(sourceString);
            if (!TextUtils.isEmpty(end)) {
                sb.append(end);
            }
            return new SpannedString(sb);
        }
        else {
            StringBuilder sb = new StringBuilder(sourceString.length() + length);
            if (!TextUtils.isEmpty(start)) {
                sb.append(start);
            }
            sb.append(sourceString);
            if (!TextUtils.isEmpty(end)) {
                sb.append(end);
            }
            return sb.toString();
        }
    }

    /**
     * Returns a wrapped string.
     */
    @Override
    @NonNull public CharSequence get(@NonNull Resources resources,
                                     @NonNull CharSequence sourceString, @StringRes int id,
                                     @NonNull String resourceName, @NonNull String locale) {
        return wrapString(sourceString);
    }

    /**
     * Returns a wrapped quantity string.
     */
    @Override
    @NonNull public CharSequence getQuantityString(@NonNull Resources resources,
            @NonNull CharSequence sourceQuantityString, @PluralsRes int id, int quantity,
            @NonNull String resourceName, @NonNull String locale) {
        return wrapString(sourceQuantityString);
    }
}
