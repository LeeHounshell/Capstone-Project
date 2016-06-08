package com.harlie.radiotheater.radiomysterytheater;

import android.os.Bundle;
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

        // first see if Authentication is even needed..
        mAuth = FirebaseAuth.getInstance();
        if (mAuth != null && mAuth.getCurrentUser() != null) {
            Log.v(TAG, "--> Firebase: user=" + mAuth.getCurrentUser().getDisplayName() + " already signed in!");
            if (doINeedToCreateADatabase()) {
                Log.v(TAG, "TODO - FIXME - CREATE-DATABASE");
            }
            startAutoplayActivity();
            return;
        }
        Log.v(TAG, "--> Firebase: user not signed in");

        // ok, we need to authenticate
        /*
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
        */
    }

    @Override
    public void onBackPressed() {
        // FIXME: if database loading prevent back press until finished ??
        startAuthenticationActivity();
        finish();
    }

}

