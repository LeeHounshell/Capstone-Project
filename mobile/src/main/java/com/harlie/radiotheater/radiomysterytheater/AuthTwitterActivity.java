package com.harlie.radiotheater.radiomysterytheater;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

import com.harlie.radiotheater.radiomysterytheater.utils.LogHelper;

// FIXME
public class AuthTwitterActivity extends BaseActivity
{
    private final static String TAG = "LEE: <" + AuthTwitterActivity.class.getSimpleName() + ">";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        LogHelper.v(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        getWindow().setWindowAnimations(0);

        boolean do_auth = false;
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("DO_AUTH")) {
                LogHelper.v(TAG, "found DO_AUTH");
                do_auth = intent.getBooleanExtra("DO_AUTH", false);
            }
        }
        if (! do_auth) {
            LogHelper.v(TAG, "DO_AUTH not present in Intent - go back to AuthenticationActivity");
            startAuthenticationActivity();
            overridePendingTransition(0,0);
            return;
        }

        // see if Authentication is even needed..
        if (getAuth() == null) {
            LogHelper.v(TAG, "unable to get FirebaseAuth!");
            startAuthenticationActivity();
            overridePendingTransition(0,0);
            return;
        }
        if (getAuth().getCurrentUser() != null && ! doINeedToCreateADatabase()) {
            LogHelper.v(TAG, "--> Firebase: user=" + getAuth().getCurrentUser().getDisplayName() + " already signed in!");
            startAutoplayActivity(false);
            overridePendingTransition(0,0);
            return;
        }
        LogHelper.v(TAG, "--> Firebase: user not signed in");

        // ok, we need to authenticate
        setContentView(R.layout.activity_auth_twitter);
        configureToolbarTitleBehavior();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayShowTitleEnabled(false);
            }
        }
    }

    @Override
    public void onBackPressed() {
        startAuthenticationActivity();
        overridePendingTransition(0,0);
    }

}

