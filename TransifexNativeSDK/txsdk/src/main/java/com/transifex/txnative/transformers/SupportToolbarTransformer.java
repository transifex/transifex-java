package com.transifex.txnative.transformers;


import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.transifex.txnative.Utils;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

public class SupportToolbarTransformer extends ViewTransformer {

    @Override
    public void transform(@NonNull Context context, @NonNull View view, @NonNull AttributeSet attrs) {
        super.transform(context, view, attrs);

        Toolbar toolbar = (Toolbar) view;

        int titleResourceId = Utils.getStringResourceId(context, attrs, android.R.attr.title);
        int titleCompatResourceId = Utils.getStringResourceId(context, attrs, androidx.appcompat.R.attr.title);
        if (titleResourceId != 0) {
            toolbar.setTitle(titleResourceId);
        }
        else if (titleCompatResourceId != 0) {
            toolbar.setTitle(titleCompatResourceId);
        }

        int subtitleResourceId = Utils.getStringResourceId(context, attrs, android.R.attr.subtitle);
        int subtitleCompatResourceId = Utils.getStringResourceId(context, attrs, androidx.appcompat.R.attr.subtitle);
        if (subtitleResourceId != 0) {
            toolbar.setSubtitle(subtitleResourceId);
        }
        else if (subtitleCompatResourceId != 0) {
            toolbar.setSubtitle(subtitleCompatResourceId);
        }
    }
}
