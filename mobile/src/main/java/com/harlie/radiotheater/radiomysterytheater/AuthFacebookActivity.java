package com.harlie.radiotheater.radiomysterytheater;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;

// FIXME
public class AuthFacebookActivity extends BaseActivity
{
    private final static String TAG = "LEE: <" + AuthFacebookActivity.class.getSimpleName() + ">";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        boolean do_auth = false;
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("DO_AUTH")) {
                Log.v(TAG, "found DO_AUTH");
                do_auth = intent.getBooleanExtra("DO_AUTH", false);
            }
        }
        if (! do_auth) {
            Log.v(TAG, "DO_AUTH not present in Intent - go back to AuthenticationActivity");
            startAuthenticationActivity();
            return;
        }

        // see if Authentication is even needed..
        mAuth = FirebaseAuth.getInstance();
        if (mAuth == null) {
            Log.v(TAG, "unable to get FirebaseAuth!");
            startAuthenticationActivity();
            return;
        }
        if (mAuth.getCurrentUser() != null && ! doINeedToCreateADatabase()) {
            Log.v(TAG, "--> Firebase: user=" + mAuth.getCurrentUser().getDisplayName() + " already signed in!");
            startAutoplayActivity();
            return;
        }
        Log.v(TAG, "--> Firebase: user not signed in");

        // ok, we need to authenticate
        setContentView(R.layout.activity_auth_facebook);
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
        // FIXME: if database loading prevent back press until finished ??
        startAuthenticationActivity();
    }

}

