package com.transifex.myapplication;

import android.os.Bundle;
import android.webkit.WebView;

import com.transifex.txnative.activity.TxBaseAppCompatActivity;

public class WebViewActivity extends TxBaseAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        ((WebView)findViewById(R.id.webview)).loadUrl("file:///android_asset/example.html");
    }
}