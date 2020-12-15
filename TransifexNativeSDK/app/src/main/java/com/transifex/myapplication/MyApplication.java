package com.transifex.myapplication;

import android.app.Application;
import android.content.Intent;

import com.transifex.txnative.LocaleState;
import com.transifex.txnative.TxNative;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Uncomment to test our VectorEnabled wrapper
        //AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        // Initialize TxNative
        String token = null;

        LocaleState localeState = new LocaleState(getApplicationContext(),
                "en",
                new String[]{"en", "el", "de", "fr", "ar", "sl"},
                null);
        TxNative.init(getApplicationContext(), localeState, token, null);

        // Fetch all translations from CDS
        TxNative.fetchTranslations(null);

        // Start a service just for testing purposes
        Intent serviceIntent = new Intent(this, SimpleIntentService.class);
        SimpleIntentService.enqueueWork(this, serviceIntent);
    }

}
