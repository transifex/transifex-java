package com.transifex.txnative.transformers;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.transifex.txnative.Utils;

import androidx.annotation.NonNull;

public class TextViewTransformer extends ViewTransformer {

    @Override
    public void transform(@NonNull Context context, @NonNull View view, @NonNull AttributeSet attrs) {
        super.transform(context, view, attrs);

        TextView textView = (TextView) view;

        int textResourceId = Utils.getStringResourceId(context, attrs, android.R.attr.text);
        if (textResourceId != 0) {
            //String textResourceName = context.getResources().getResourceEntryName(textResourceId);

            // This will be handled by our overridden resources
            textView.setText(textResourceId);
        }

        int hintResourceId = Utils.getStringResourceId(context, attrs, android.R.attr.hint);
        if (hintResourceId != 0) {
            textView.setHint(hintResourceId);
        }
    }

}

