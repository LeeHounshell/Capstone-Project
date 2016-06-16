package com.harlie.radiotheater.radiomysterytheater;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.appcompat.*;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.firebase.auth.FirebaseAuth;

import com.harlie.radiotheater.radiomysterytheater.BuildConfig;
import com.harlie.radiotheater.radiomysterytheater.utils.LogHelper;

import static com.firebase.ui.auth.ui.AcquireEmailHelper.RC_SIGN_IN;

public class AuthGoogleActivity
        extends BaseActivity
        implements GoogleApiClient.OnConnectionFailedListener
{
    private final static String TAG = "LEE: <" + AuthGoogleActivity.class.getSimpleName() + ">";

    private GoogleApiClient mGoogleApiClient;
    private Handler handler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        LogHelper.v(TAG, "onCreate");
        super.onCreate(savedInstanceState);

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
        if (getAuth() == null) {
            LogHelper.v(TAG, "unable to get FirebaseAuth!");
            startAuthenticationActivity();
            return;
        }
        if (getAuth().getCurrentUser() != null && ! doINeedToCreateADatabase()) {
            LogHelper.v(TAG, "--> Firebase: user=" + getAuth().getCurrentUser().getDisplayName() + " already signed in!");
            startAutoplayActivity();
            return;
        }
        LogHelper.v(TAG, "--> Firebase: user not signed in");

        // ok, we need to authenticate
        setContentView(R.layout.activity_auth_google);
        configureToolbarTitleBehavior();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayShowTitleEnabled(false);
            }
        }

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addApi(Plus.API)
                .build();

        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onBackPressed() {
        LogHelper.v(TAG, "onBackPressed");
        // FIXME: if database loading prevent back press until finished ??
        startAuthenticationActivity();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        LogHelper.v(TAG, "onConnectionFailed - connectionResult=" + connectionResult.isSuccess());
        handleAuthenticationRequestResult(connectionResult.isSuccess());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogHelper.v(TAG, "onActivityResult: requestCode="+requestCode+", resultCode="+resultCode);
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result == null) {
                LogHelper.v(TAG, "GoogleSignInResult is null!");
                handleAuthenticationRequestResult(false);
                return;
            }
            if (result.isSuccess()) {
                GoogleSignInAccount acct = result.getSignInAccount();
                if (acct != null) {
                    final String personName = acct.getDisplayName();
                    final String personEmail = BuildConfig.SECRET_AUTH_PREFIX + acct.getEmail(); // does not collide with real email
                    final String personId = acct.getId();
                    final String personPass = acct.getId() + BuildConfig.SECRET_PASS_SUFFIX; // unique, repeatable and private
                    final Uri personPhoto = acct.getPhotoUrl();
                    setEmail(personEmail);
                    setPass(personPass);
                    // ok, now do the login invisibly with email to avoid extra app manifest permissions
                    LogHelper.v(TAG, "GOOGLE: name=" + personName + ", id=" + personId + ", email=" + getEmail() + ", url=" + personPhoto);
                    Intent authEmailIntent = new Intent(this, AuthEmailActivity.class);
                    authEmailIntent.putExtra("name", personName);
                    authEmailIntent.putExtra("email", personEmail);
                    authEmailIntent.putExtra("pass", personPass);
                    authEmailIntent.putExtra("photo", personPhoto);
                    authEmailIntent.putExtra("DO_AUTH", true);
                    startActivity(authEmailIntent);
                    finish();
                    return;
                }
            }
            LogHelper.v(TAG, "GOOGLE: unable to retrieve: name, email, id, photo");
            handleAuthenticationRequestResult(false);
        }
    }

}

