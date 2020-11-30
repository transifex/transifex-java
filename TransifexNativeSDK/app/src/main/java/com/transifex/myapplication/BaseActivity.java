package com.transifex.myapplication;

import android.app.Activity;
import android.content.Context;

import com.transifex.txnative.TxNative;

public class BaseActivity extends Activity {


    @Override
    protected void attachBaseContext(Context base) {
        // Wrap the Activity context
        super.attachBaseContext(TxNative.wrap(base));
    }

}
