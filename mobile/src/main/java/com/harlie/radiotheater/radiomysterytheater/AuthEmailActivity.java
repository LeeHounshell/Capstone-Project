package com.harlie.radiotheater.radiomysterytheater;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.harlie.radiotheater.radiomysterytheater.data_helper.DataHelper;
import com.harlie.radiotheater.radiomysterytheater.utils.LogHelper;

public class AuthEmailActivity extends BaseActivity
{
    private final static String TAG = "LEE: <" + AuthEmailActivity.class.getSimpleName() + ">";

    private boolean cancelTimer = false;

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
            return;
        }

        // see if Authentication is even needed..
        if (DataHelper.getFirebaseAuth() == null) {
            LogHelper.v(TAG, "unable to get FirebaseAuth!");
            startAuthenticationActivity();
            return;
        }
        if (DataHelper.getFirebaseAuth().getCurrentUser() != null && ! doINeedToCreateADatabase()) {
            LogHelper.v(TAG, "--> Firebase: user=" + DataHelper.getFirebaseAuth().getCurrentUser().getDisplayName() + " already signed in!");
            startAutoplayActivity(false);
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

        DataHelper.setName(null);
        DataHelper.setEmail(null);
        DataHelper.setPass(null);
        DataHelper.setUID(null);

        boolean found_login_info = true;
        if (intent.hasExtra("email")) {
            DataHelper.setEmail(intent.getStringExtra("email"));
        }
        else {
            found_login_info = false;
        }
        if (intent.hasExtra("pass")) {
            DataHelper.setPass(intent.getStringExtra("pass"));
        }
        else {
            found_login_info = false;
        }
        if (intent.hasExtra("name")) {
            DataHelper.setName(intent.getStringExtra("name"));
        }
        if (! found_login_info || DataHelper.getEmail() == null || DataHelper.getEmail().length() == 0 || DataHelper.getPass() == null || DataHelper.getPass().length() == 0) {
            LogHelper.e(TAG, "Failure to extract email and pass from Intent bundle!");
            String message = getResources().getString(R.string.enter_email);
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            startAuthenticationActivity();
            return;
        }
        LogHelper.v(TAG, "===> attempting signin for email="+ DataHelper.getEmail());

        final AuthEmailActivity activity = this;
        DataHelper.getFirebaseAuth().signInWithEmailAndPassword(DataHelper.getEmail(), DataHelper.getPass())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        cancelTimer = true;
                        boolean success = false;
                        if (DataHelper.getEmail() != null && DataHelper.getPass() != null) {
                            success = task.isSuccessful();
                            LogHelper.d(TAG, "signInWithEmailAndPassword:onComplete: success=" + success);

                            // If sign in fails, display a message to the user. If sign in succeeds
                            // the auth state listener will be notified and logic to handle the
                            // signed in user can be handled in the listener.
                            if (!success) {
                                success = checkExceptionReason(task, activity);
                                if (!success) {
                                    LogHelper.v(TAG, "no success. signin failed.");
                                    startAuthenticationActivity();
                                    return;
                                }
                            }
                            if (DataHelper.getUID() == null) {
                                @SuppressWarnings("ConstantConditions") String uid = DataHelper.getFirebaseAuth().getCurrentUser().getUid();
                                DataHelper.setUID(uid);
                            }
                            DataHelper.trackLoginWithFirebaseAnalytics();
                        }
                        else {
                            LogHelper.e(TAG, "email or password is null!");
                        }
                        handleAuthenticationRequestResult(success);
                    }
                });

        // just in case of auth problems
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (! cancelTimer) {
                    LogHelper.v(TAG, "********* TIMER TRIPPED *********");
                    problemAuthenticating("auth did not complete");
                }
                else {
                    LogHelper.v(TAG, "********* TIMER CANCELLED *********");
                }
            }
        }, 19000); // nineteen seconds
    }

    private void problemAuthenticating(@SuppressWarnings("SameParameterValue") String error) {
        LogHelper.w(TAG, "problemAuthenticating: error="+error);
        DataHelper.setCurrentPosition(0);
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(getResources().getString(R.string.problem_authenticating));
        alertDialog.setMessage(getResources().getString(R.string.firebase_authentication_error) + "\n\nerror=" + error);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        startAuthenticationActivity();
                    }
                });
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        LogHelper.v(TAG, "onBackPressed");
        startAuthenticationActivity();
    }

}

