package com.transifex.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Spanned;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.transifex.txnative.activity.TxBaseAppCompatActivity;

import androidx.appcompat.widget.Toolbar;
import androidx.core.text.HtmlCompat;


// Extend your activity from BaseAppCompatActivity or BaseActivity
public class MainActivity extends TxBaseAppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    Toolbar mToolbar;
    TextView mHelloLabel;
    TextView mWelcomeLabel;
    TextView mArrayLabel;
    TextView mPluralLabel;
    TextView mFormatLabel;
    TextView mStyledLabel;
    TextView mReferenceUseLabel;
    Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mHelloLabel = findViewById(R.id.hello_label);

        // In this activity, we make use of all the different type of string methods supported by
        // Android: https://developer.android.com/guide/topics/resources/string-resource

        // Simple text
        mWelcomeLabel = findViewById(R.id.welcome_label);
        mWelcomeLabel.setText(getString(R.string.welcome_text));
        //mWelcomeLabel.setText(R.string.welcome_text); // this also works

        // String array [not supported at the moment]
        mArrayLabel = findViewById(R.id.array_label);
        {
            String[] strings = getResources().getStringArray(R.array.elements_array);
            StringBuilder sb = new StringBuilder();
            for (String segment : strings) {
                sb.append(segment);
                sb.append(" ");
            }
            mArrayLabel.setText(sb.toString());
        }

        // Quantity strings
        mPluralLabel = findViewById(R.id.plural_label);
        {
            String pluralString = getResources().getQuantityString(R.plurals.duration_seconds, 2, 2);
            mPluralLabel.setText(pluralString);
        }

        // Formatted string
        mFormatLabel = findViewById(R.id.formatted_label);
        {
            String formattedString = getResources().getString(R.string.hello_user, "John", 2);
            mFormatLabel.setText(formattedString);
        }

        // Styled text
        mStyledLabel = findViewById(R.id.styled_label);
        {
            String string = getResources().getString(R.string.styled_text);
            Spanned styledText = HtmlCompat.fromHtml(string, HtmlCompat.FROM_HTML_MODE_COMPACT);
            mStyledLabel.setText(styledText);
        }

        // Themed attribute referencing string resource
        mReferenceUseLabel = findViewById(R.id.reference_use);
        {
            TypedValue typedValue = new TypedValue();
            getTheme().resolveAttribute(R.attr.theme_string, typedValue, true);
            mReferenceUseLabel.setText(typedValue.resourceId);
        }

        mButton = findViewById(R.id.button);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, WebViewActivity.class));
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return true;
    }

}