package com.transifex.txnative.transformers;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.google.android.material.textfield.TextInputLayout;
import com.transifex.txnative.Utils;

import androidx.annotation.NonNull;

public class TextInputLayoutTransformer extends ViewTransformer{

    @Override
    public void transform(@NonNull Context context, @NonNull View view, @NonNull AttributeSet attrs) {
        super.transform(context, view, attrs);

        TextInputLayout textInputLayout = (TextInputLayout) view;

        int hintResourceId = Utils.getStringResourceId(context, attrs, android.R.attr.hint);
        if (hintResourceId != 0) {
            textInputLayout.setHint(hintResourceId);
        }
    }
}
