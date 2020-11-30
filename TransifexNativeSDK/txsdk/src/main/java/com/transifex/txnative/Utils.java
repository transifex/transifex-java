package com.transifex.txnative;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

public class Utils {

    private static final String TAG = Utils.class.getSimpleName();

    /**
     * Returns the string resource id that this attributeId is resolving to under the provided
     * attribute set and the current theme.
     * <p>
     *     If the attribute does not point to a resource id, the method returns "0". This can happen
     *     if the attribute is set to hardcoded string or to "@null" or the attribute has not
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
}
