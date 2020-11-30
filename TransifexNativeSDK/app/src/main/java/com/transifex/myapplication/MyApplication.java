package com.transifex.myapplication;

import android.app.Application;

import com.transifex.txnative.TxNative;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        //AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        // Initialize TxNative
        TxNative.init();
    }

}
