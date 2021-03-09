package com.transifex.txnative.missingpolicy;


import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Wraps the source string with a custom format.
 * <p>
 * Example:
 * <p>
 * <code>new WrappedStringPolicy(">>", "<<").get("Click here");</code>
 * <p>
 * Returns:
 * <p>
 * ">>Click here<<"
 * </pre>
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
     * Return a string that wraps the source string.
     */
    @Override
    @NonNull public CharSequence get(@NonNull CharSequence sourceString) {
        if (TextUtils.isEmpty(start) && TextUtils.isEmpty(end)) {
            return sourceString;
        }

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
