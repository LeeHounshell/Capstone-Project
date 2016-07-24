package com.harlie.radiotheater.radiomysterytheater;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.widget.EditText;
import android.widget.Toast;

import com.harlie.radiotheater.radiomysterytheater.data_helper.DataHelper;
import com.harlie.radiotheater.radiomysterytheater.utils.LogHelper;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A login screen that offers Firebase login via email/password or Google or Facebook or Twitter
 */
public class AuthenticationActivity extends BaseActivity
{
    private final static String TAG = "LEE: <" + AuthenticationActivity.class.getSimpleName() + ">";

    @BindView(R.id.user) EditText username;
    @BindView(R.id.pass) EditText password;

    @BindString(R.string.login_error) String loginErrorMessage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setWindowAnimations(0);

        // first see if Authentication is even needed..
        if (DataHelper.getFirebaseAuth() != null
                && DataHelper.getFirebaseAuth().getCurrentUser() != null
                && DataHelper.getFirebaseAuth().getCurrentUser().getEmail() != null
                && ! doINeedToCreateADatabase())
        {
            DataHelper.setEmail(DataHelper.getFirebaseAuth().getCurrentUser().getEmail());
            DataHelper.setUID(DataHelper.getFirebaseAuth().getCurrentUser().getUid());
            LogHelper.v(TAG, "--> Firebase: user=" + DataHelper.getFirebaseAuth().getCurrentUser().getDisplayName() + " already signed in with email="+ DataHelper.getEmail());
            startAutoplayActivity(false);
            return;
        }
        LogHelper.v(TAG, "--> Firebase: user not signed in");

        // ok, we need to authenticate
        setContentView(R.layout.activity_authentication);
        configureToolbarTitleBehavior();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayShowTitleEnabled(false);
            }
        }

        ButterKnife.bind(this);
        username.setText("");
        password.setText("");
    }

    @OnClick(R.id.submit)
    void submit() {
        LogHelper.v(TAG, "submit - Firebase Login using Email and Password");
        String email = username.getText().toString();
        String pass = password.getText().toString();
        LogHelper.v(TAG, "email="+email);
        if (DataHelper.getFirebaseAuth() != null && isValid(email, pass)) {
            LogHelper.v(TAG, "authenticateEmail - Firebase Auth using Email");
            Intent authEmailIntent = new Intent(this, AuthEmailActivity.class);
            authEmailIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            authEmailIntent.putExtra("email", email);
            authEmailIntent.putExtra("pass", pass);
            authEmailIntent.putExtra("DO_AUTH", true);
            Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(this, android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
            DataHelper.trackSignupAttemptWithFirebaseAnalytics("email");
            authEmailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            LogHelper.v(TAG, "STARTACTIVITY: AuthEmailActivity.class");
            startActivity(authEmailIntent, bundle);
            overridePendingTransition(0,0);
            finish();
        }
        else {
            String message = getResources().getString(R.string.invalid_email);
            Toast.makeText(this, message + " Password must use " + MIN_PASSWORD_LENGTH + "+ characters.", Toast.LENGTH_LONG).show();
        }
    }

    @OnClick(R.id.google_auth)
    void authenticateGoogle() {
        LogHelper.v(TAG, "authenticateGoogle - Firebase Auth using Google");
        Intent authGoogleIntent = new Intent(this, AuthGoogleActivity.class);
        authGoogleIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        authGoogleIntent.putExtra("DO_AUTH", true);
        Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(this, android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
        DataHelper.trackSignupAttemptWithFirebaseAnalytics("google");
        authGoogleIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        LogHelper.v(TAG, "STARTACTIVITY: AuthGoogleActivity.class");
        startActivity(authGoogleIntent, bundle);
        overridePendingTransition(0,0);
        finish();
    }

    @OnClick(R.id.twitter_auth)
    void authenticateTwitter() {
        LogHelper.v(TAG, "authenticateTwitter - Firebase Auth using Twitter");
        Intent authTwitterIntent = new Intent(this, AuthTwitterActivity.class);
        authTwitterIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        authTwitterIntent.putExtra("DO_AUTH", true);
        Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(this, android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
        DataHelper.trackSignupAttemptWithFirebaseAnalytics("twitter");
        authTwitterIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        LogHelper.v(TAG, "STARTACTIVITY: AuthTwitterActivity.class");
        startActivity(authTwitterIntent, bundle);
        overridePendingTransition(0,0);
        finish();
    }

    @OnClick(R.id.facebook_auth)
    void authenticateFacebook() {
        LogHelper.v(TAG, "authenticateFacebook - Firebase Auth using Facebook");
        Intent authFacebookIntent = new Intent(this, AuthFacebookActivity.class);
        authFacebookIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        authFacebookIntent.putExtra("DO_AUTH", true);
        Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(this, android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
        DataHelper.trackSignupAttemptWithFirebaseAnalytics("facebook");
        authFacebookIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        LogHelper.v(TAG, "STARTACTIVITY: AuthFacebookActivity.class");
        startActivity(authFacebookIntent, bundle);
        overridePendingTransition(0,0);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

}

