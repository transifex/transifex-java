package com.transifex.txnative;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Build;
import android.text.Html;
import android.text.Spanned;
import android.util.AttributeSet;
import android.util.Log;

import java.util.Locale;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

public class Utils {

    private static final String TAG = Utils.class.getSimpleName();

    private static final boolean sIsAppcompatPresent;

    static {
        sIsAppcompatPresent = isClassPresent("androidx.appcompat.widget.Toolbar");
    }

    /**
     * Returns the string resource id that this attributeId is resolving to under the provided
     * attribute set and the current theme.
     * <p>
     *     If the attribute does not point to a resource id, the method returns "0". This can happen
     *     if the attribute is set to a hardcoded string or to "@null" or the attribute has not
     *     beet set.
     * </p>
     * @param context The context.
     * @param attributeSet The view's attribute set.
     * @param attributeId The desired attribute to be retrieved.
     * @return The attribute's resource identifier or "0" if not applicable.
     */
    public static @StringRes
    int getStringResourceId(Context context, @NonNull AttributeSet attributeSet, @AttrRes int attributeId) {
        TypedArray typedArray = context.obtainStyledAttributes(attributeSet, new int[]{attributeId});
        int resourceId = typedArray.getResourceId(0, 0);

        typedArray.recycle();

        return resourceId;
    }

    public static void printAttributeSet(Context context, @NonNull AttributeSet attrs) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < attrs.getAttributeCount(); i++) {
            sb.append(attrs.getAttributeName(i)).append(" = ");

            String attributeValue = attrs.getAttributeValue(i);

            // If the attribute points to a resource, its id will be populated
            int resourceId = attrs.getAttributeResourceValue(i, 0);
            // If not, it may point to an attribute
            if (resourceId == 0) {
                try {
                    // Attributes are in the form "?00000"
                    int lastIndex = attributeValue.lastIndexOf("?");
                    if (lastIndex != -1) {
                        resourceId = Integer.parseInt(attributeValue.substring(lastIndex + 1));
                    }
                }
                catch (Exception ignored){}
            }

            // Get the name of the resource id or attribute id
            String resourceName = null;
            if (resourceId != 0) {
                resourceName = context.getResources().getResourceName(resourceId);
            }

            // Use the resource name if possible. Otherwise, just use the value.
            if (resourceName != null) {
                sb.append(resourceName);
            }
            else {
                sb.append(attributeValue);
            }

            sb.append("\n");
        }

        Log.d(TAG, sb.toString());
    }

    /**
     * Utility method to get the current locale as set in Android.
     */
    public static Locale getCurrentLocale(Context context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            return context.getResources().getConfiguration().getLocales().get(0);
        } else{
            //noinspection deprecation
            return context.getResources().getConfiguration().locale;
        }
    }

    /**
     * Returns a Resources object, derived from the one in the provided context, which uses the
     * desired locale.
     */
    @NonNull
    public static Resources getLocalizedResources(@NonNull Context context, @NonNull Locale desiredLocale) {
        Configuration conf = context.getResources().getConfiguration();
        conf = new Configuration(conf);
        conf.setLocale(desiredLocale);
        Context localizedContext = context.createConfigurationContext(conf);
        return localizedContext.getResources();
    }

    /**
     * Returns a {@link Resources} object configured for the default (non localized) resources.
     * <p>
     * Getting a string from this object, will return the string found in the default
     * <code>`strings.xml`</code> file. Note though that quantity strings will not follow any
     * locale's plural rules.
     */
    @NonNull
    public static Resources getDefaultLocaleResources(@NonNull Context context) {
        return getLocalizedResources(context, new Locale(""));
    }

    public static boolean equals(Object a, Object b) {
        return (a == b) || (a != null && a.equals(b));
    }

    /**
     * Invokes {@link Html#fromHtml(String, int)} on API 24 and newer, otherwise {@code flags} are
     * ignored and {@link Html#fromHtml(String)} is used.
     */
    @SuppressWarnings("deprecation")
    @NonNull
    public static Spanned fromHtml(@NonNull String source, int flags) {
        // taken from androidx.core.text
        if (Build.VERSION.SDK_INT >= 24) {
            return Html.fromHtml(source, flags);
        }
        return Html.fromHtml(source);
    }

    /**
     * Checks, using reflection, if the provided class is available at runtime.
     */
    public static boolean isClassPresent(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (Throwable ex) {
            // Class or one of its dependencies is not present...
            return false;
        }
    }

    /**
     * Checks if <code>Androidx.appcompat</code> classes are available at runtime.
     */
    public static boolean isAppcompatPresent() {
        return sIsAppcompatPresent;
    }
}
