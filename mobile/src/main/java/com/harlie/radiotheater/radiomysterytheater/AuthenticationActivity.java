package com.harlie.radiotheater.radiomysterytheater;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
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

import java.util.ArrayList;
import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.Manifest.permission.READ_CONTACTS;
import static com.firebase.ui.auth.ui.AcquireEmailHelper.RC_SIGN_IN;

/**
 * A login screen that offers Firebase login via email/password or Google or Facebook or Twitter
 */
public class AuthenticationActivity
        extends BaseActivity
        implements GoogleApiClient.OnConnectionFailedListener {
    private final static String TAG = "LEE: <" + AuthenticationActivity.class.getSimpleName() + ">";

    @BindView(R.id.user) EditText username;
    @BindView(R.id.pass) EditText password;

    @BindString(R.string.login_error) String loginErrorMessage;

    private FirebaseAuth mAuth;
    private GoogleApiClient mGoogleApiClient;

    @OnClick(R.id.submit)
    void submit() {
        Log.v(TAG, "submit - Firebase Login using Email and Password");
        String email = username.getText().toString();
        String pass = password.getText().toString();
        Log.v(TAG, "email="+email);
        mAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        boolean success = task.isSuccessful();
                        if (!success) {
                            Log.w(TAG, "signInWithEmail", task.getException());
                        }
                        handleAuthenticationRequestResult(success);
                    }
                });
    }

    private void handleAuthenticationRequestResult(boolean success) {
        Log.d(TAG, "handleAuthenticationRequestResult - success="+success);
        if (!success) {
            Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show();
            String email = username.getText().toString();
            String pass = password.getText().toString();
            if ((email != null && email.length() > 0) && (pass != null && pass.length() > 0)) {
                Log.v(TAG, "FIXME: - confirm new account request, then create new account for email="+email);
            }
        }
        else {
            Toast.makeText(this, "Authenticated", Toast.LENGTH_SHORT).show();
            startAutoplayActivity();
        }
    }

    @OnClick(R.id.google_auth)
    void authenticateGoogle() {
        Log.v(TAG, "authenticateGoogle - Firebase Auth using Google");
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    // (call)back from the above Google Authentication request
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v(TAG, "onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            boolean success = result.isSuccess();
            if (success) {
                GoogleSignInAccount acct = result.getSignInAccount();
                Log.v(TAG, "Google Sign In was successful for "+acct.getDisplayName());
            }
            handleAuthenticationRequestResult(success);
        }
    }

    @OnClick(R.id.twitter_auth)
    void authenticateTwitter() {
        Log.v(TAG, "authenticateTwitter - Firebase Auth using Twitter");
        // TODO - FIXME
    }

    @OnClick(R.id.facebook_auth)
    void authenticateFacebook() {
        Log.v(TAG, "authenticateTwitter - Firebase Auth using Facebook");
        // TODO - FIXME
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setProviders(
                                AuthUI.FACEBOOK_PROVIDER)
                        .build(),
                RC_SIGN_IN);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // first see if Authentication is even needed..
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            Log.v(TAG, "--> Firebase: user="+mAuth.getCurrentUser()+" already signed in!");
            loadRadioTheaterSQLiteTables();
            startAutoplayActivity();
        } else {
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

            // Configure sign-in to request the user's ID, email address, and basic
            // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build();

            // Build a GoogleApiClient with access to the Google Sign-In API and the
            // options specified by gso.
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build();
        }
    }

    private void startAutoplayActivity() {
        Intent autoplayIntent = new Intent(this, AutoplayActivity.class);
        startActivity(autoplayIntent);
        finish();
    }

    private void loadRadioTheaterSQLiteTables() {
        Log.v(TAG, "loadRadioTheaterSQLiteTables - TODO");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.v(TAG, "onConnectionFailed - connectionResult="+connectionResult.isSuccess());
        handleAuthenticationRequestResult(connectionResult.isSuccess());
    }
}

