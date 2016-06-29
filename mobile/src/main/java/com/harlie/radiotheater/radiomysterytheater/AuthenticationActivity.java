package com.harlie.radiotheater.radiomysterytheater;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.widget.EditText;
import android.widget.Toast;

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

        // first see if Authentication is even needed..
        if (getAuth() != null && getAuth().getCurrentUser() != null && getAuth().getCurrentUser().getEmail() != null && ! doINeedToCreateADatabase()) {
            setEmail(getAuth().getCurrentUser().getEmail());
            setUID(getAuth().getCurrentUser().getUid());
            LogHelper.v(TAG, "--> Firebase: user=" + getAuth().getCurrentUser().getDisplayName() + " already signed in with email="+getEmail());
            startAutoplayActivity();
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
        if (getAuth() != null && email != null && pass != null && isValid(email, pass)) {
            LogHelper.v(TAG, "authenticateEmail - Firebase Auth using Email");
            Intent authEmailIntent = new Intent(this, AuthEmailActivity.class);
            authEmailIntent.putExtra("email", email);
            authEmailIntent.putExtra("pass", pass);
            authEmailIntent.putExtra("DO_AUTH", true);
            Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(this, android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
            startActivity(authEmailIntent, bundle);
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
        authGoogleIntent.putExtra("DO_AUTH", true);
        Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(this, android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
        startActivity(authGoogleIntent, bundle);
        finish();
    }

    @OnClick(R.id.twitter_auth)
    void authenticateTwitter() {
        LogHelper.v(TAG, "authenticateTwitter - Firebase Auth using Twitter");
        Intent authTwitterIntent = new Intent(this, AuthTwitterActivity.class);
        authTwitterIntent.putExtra("DO_AUTH", true);
        Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(this, android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
        startActivity(authTwitterIntent, bundle);
        finish();
    }

    @OnClick(R.id.facebook_auth)
    void authenticateFacebook() {
        LogHelper.v(TAG, "authenticateFacebook - Firebase Auth using Facebook");
        Intent authFacebookIntent = new Intent(this, AuthFacebookActivity.class);
        authFacebookIntent.putExtra("DO_AUTH", true);
        Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(this, android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
        startActivity(authFacebookIntent, bundle);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

}

