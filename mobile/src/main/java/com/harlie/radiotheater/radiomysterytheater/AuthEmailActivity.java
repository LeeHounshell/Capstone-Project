package com.harlie.radiotheater.radiomysterytheater;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.harlie.radiotheater.radiomysterytheater.utils.LogHelper;

public class AuthEmailActivity extends BaseActivity
{
    private final static String TAG = "LEE: <" + AuthEmailActivity.class.getSimpleName() + ">";

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
            startAutoplayActivity();
            overridePendingTransition(0,0);
            return;
        }
        LogHelper.v(TAG, "--> Firebase: user not signed in");

        // ok, we need to authenticate
        setContentView(R.layout.activity_auth_email);
        configureToolbarTitleBehavior();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayShowTitleEnabled(false);
            }
        }

        setEmail(null);
        setUID(null);
        setPass(null);
        boolean found_login_info = true;
        if (intent.hasExtra("email")) {
            setEmail(intent.getStringExtra("email"));
        }
        else {
            found_login_info = false;
        }
        if (intent.hasExtra("pass")) {
            setPass(intent.getStringExtra("pass"));
        }
        else {
            found_login_info = false;
        }
        if (! found_login_info || getEmail() == null || getEmail().length() == 0 || getPass() == null || getPass().length() == 0) {
            LogHelper.e(TAG, "Failure to extract email and pass from Intent bundle!");
            String message = getResources().getString(R.string.enter_email);
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            startAuthenticationActivity();
            overridePendingTransition(0,0);
            return;
        }
        LogHelper.v(TAG, "attempting signin for email="+getEmail());

        final AuthEmailActivity activity = this;
        getAuth().signInWithEmailAndPassword(getEmail(), getPass())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        LogHelper.d(TAG, "signInWithEmailAndPassword:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        boolean success = task.isSuccessful();
                        if (!success) {
                            success = checkExceptionReason(task, activity);
                            if (! success) {
                                LogHelper.v(TAG, "no success. signin failed.");
                                startAuthenticationActivity();
                                overridePendingTransition(0,0);
                                return;
                            }
                        }
                        if (success) {
                            if (getUID() == null) {
                                String uid = mAuth.getCurrentUser().getUid();
                                setUID(uid);
                            }
                            trackLoginWithFirebaseAnalytics();
                        }
                        handleAuthenticationRequestResult(success);
                    }
                });
    }

    @Override
    public void onBackPressed() {
        LogHelper.v(TAG, "onBackPressed");
        startAuthenticationActivity();
        overridePendingTransition(0,0);
    }

}

