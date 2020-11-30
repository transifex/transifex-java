package com.transifex.txnative.transformers;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.transifex.txnative.Utils;

import androidx.annotation.NonNull;

public class ViewTransformer {

    public void transform(@NonNull Context context, @NonNull View view, @NonNull AttributeSet attrs) {
        int contentDescriptionId = Utils.getStringResourceId(context, attrs, android.R.attr.contentDescription);
        if (contentDescriptionId != 0) {
            view.setContentDescription(context.getString(contentDescriptionId));
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int tooltipTextId = Utils.getStringResourceId(context, attrs, android.R.attr.tooltipText);
            if (tooltipTextId != 0) {
                view.setTooltipText(context.getString(tooltipTextId));
            }
        }
    }

}
