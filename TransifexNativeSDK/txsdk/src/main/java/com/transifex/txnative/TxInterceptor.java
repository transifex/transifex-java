package com.transifex.txnative;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import android.widget.Toolbar;

import com.google.android.material.textfield.TextInputLayout;
import com.transifex.txnative.transformers.SupportToolbarTransformer;
import com.transifex.txnative.transformers.TextInputLayoutTransformer;
import com.transifex.txnative.transformers.TextViewTransformer;
import com.transifex.txnative.transformers.ToolbarTransformer;
import com.transifex.txnative.transformers.ViewTransformer;

import androidx.annotation.NonNull;
import io.github.inflationx.viewpump.InflateResult;
import io.github.inflationx.viewpump.Interceptor;

/**
 * ViewPump interceptor that transforms inflated views using the appropriate
 * {@link ViewTransformer}.
 * <p>
 *     ViewTransforms should change the text elements of views by calling their respective methods.
 *     Since we have wrapped the context and thus the resources, all string related calls will be
 *     handled by {@link TxResources}.
 * </p>
 */
class TxInterceptor implements Interceptor {

    public static final String TAG = TxInterceptor.class.getSimpleName();

    private final ViewTransformer mViewTransformer;
    private final TextViewTransformer mTextViewTransformer;
    private ToolbarTransformer mToolbarTransformer;
    private final SupportToolbarTransformer mSupportToolbarTransformer;
    private final TextInputLayoutTransformer mTextInputLayoutTransformer;

    public TxInterceptor() {
        mViewTransformer = new ViewTransformer();
        mTextViewTransformer = new TextViewTransformer();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mToolbarTransformer = new ToolbarTransformer();
        }
        mSupportToolbarTransformer = new SupportToolbarTransformer();
        mTextInputLayoutTransformer = new TextInputLayoutTransformer();
    }

    @NonNull
    @Override
    public InflateResult intercept(@NonNull Chain chain) {
        InflateResult result = chain.proceed(chain.request());

        View view = result.view();
        if (view == null) {
            return result;
        }

        AttributeSet attrs = result.attrs();
        Context context = result.context();
        if (attrs != null) {
            if (view instanceof TextView) {
                mTextViewTransformer.transform(context, view, attrs);
            }
            else if (Utils.isAppcompatPresent() && view instanceof androidx.appcompat.widget.Toolbar) {
                mSupportToolbarTransformer.transform(context, view, attrs);
            }
            else if (Utils.isMaterialComponentsPresent() && view instanceof TextInputLayout) {
                mTextInputLayoutTransformer.transform(context, view, attrs);
            }
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && view instanceof Toolbar) {
                mToolbarTransformer.transform(context, view, attrs);
            }
            else {
                mViewTransformer.transform(context, view, attrs);
            }
        }

        return result;
    }

}
