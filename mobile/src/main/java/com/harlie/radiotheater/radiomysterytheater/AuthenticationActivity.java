package com.harlie.radiotheater.radiomysterytheater;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

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
        mAuth = FirebaseAuth.getInstance();
        if (mAuth != null && mAuth.getCurrentUser() != null && mAuth.getCurrentUser().getEmail() != null) {
            setEmail(mAuth.getCurrentUser().getEmail());
            Log.v(TAG, "--> Firebase: user=" + mAuth.getCurrentUser().getDisplayName() + " already signed in with email="+getEmail());
            if (doINeedToCreateADatabase()) {
                Log.v(TAG, "TODO - FIXME - CREATE-DATABASE");
            }
            startAutoplayActivity();
            return;
        }
        Log.v(TAG, "--> Firebase: user not signed in");

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
        Log.v(TAG, "submit - Firebase Login using Email and Password");
        String email = username.getText().toString();
        String pass = password.getText().toString();
        Log.v(TAG, "email="+email);
        if (mAuth != null && email != null && pass != null && isValid(email, pass)) {
            Log.v(TAG, "authenticateEmail - Firebase Auth using Email");
            Intent authEmailIntent = new Intent(this, AuthEmailActivity.class);
            authEmailIntent.putExtra("email", email);
            authEmailIntent.putExtra("pass", pass);
            startActivity(authEmailIntent);
            finish();
        }
        else {
            Toast.makeText(this, "Inavlid Email. Password must use " + MIN_PASSWORD_LENGTH + "+ characters.", Toast.LENGTH_LONG).show();
        }
    }

    @OnClick(R.id.google_auth)
    void authenticateGoogle() {
        Log.v(TAG, "authenticateGoogle - Firebase Auth using Google");
        Intent authGoogleIntent = new Intent(this, AuthGoogleActivity.class);
        startActivity(authGoogleIntent);
        finish();
    }

    @OnClick(R.id.twitter_auth)
    void authenticateTwitter() {
        Log.v(TAG, "authenticateTwitter - Firebase Auth using Twitter");
        Intent authTwitterIntent = new Intent(this, AuthTwitterActivity.class);
        startActivity(authTwitterIntent);
        finish();
    }

    @OnClick(R.id.facebook_auth)
    void authenticateFacebook() {
        Log.v(TAG, "authenticateFacebook - Firebase Auth using Facebook");
        Intent authFacebookIntent = new Intent(this, AuthFacebookActivity.class);
        startActivity(authFacebookIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

}

