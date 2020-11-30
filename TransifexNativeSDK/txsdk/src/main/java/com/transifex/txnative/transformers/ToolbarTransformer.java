package com.transifex.txnative.transformers;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toolbar;

import com.transifex.txnative.Utils;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class ToolbarTransformer extends ViewTransformer {


    @Override
    public void transform(@NonNull Context context, @NonNull View view, @NonNull AttributeSet attrs) {
        super.transform(context, view, attrs);

        Toolbar toolbar = (Toolbar) view;

        CharSequence xa = toolbar.getTitle();

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
