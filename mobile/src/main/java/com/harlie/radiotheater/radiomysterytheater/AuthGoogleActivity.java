package com.harlie.radiotheater.radiomysterytheater;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;

import static com.firebase.ui.auth.ui.AcquireEmailHelper.RC_SIGN_IN;

public class AuthGoogleActivity
        extends BaseActivity
        implements GoogleApiClient.OnConnectionFailedListener
{
    private final static String TAG = "LEE: <" + AuthGoogleActivity.class.getSimpleName() + ">";

    private GoogleApiClient mGoogleApiClient;

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
                .build();

        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onBackPressed() {
        // FIXME: if database loading prevent back press until finished ??
        startAuthenticationActivity();
        finish();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.v(TAG, "onConnectionFailed - connectionResult=" + connectionResult.isSuccess());
        handleAuthenticationRequestResult(connectionResult.isSuccess());
    }

    protected void handleAuthenticationRequestResult(boolean success) {
        Log.d(TAG, "handleAuthenticationRequestResult - success=" + success);
        if (!success) {
            userLoginFailed();
            startAuthenticationActivity();
        } else {
            userLoginSuccess();
            // FIXME: verify Firebase read/write access before authorizing here
            startAutoplayActivity();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v(TAG, "onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount acct = result.getSignInAccount();
                String personName = acct.getDisplayName();
                String personEmail = acct.getEmail();
                String personId = acct.getId();
                Uri personPhoto = acct.getPhotoUrl();
                setEmail(personEmail);
                Log.v(TAG, "GOOGLE: name=" + personName + ", id=" + personId + ", email=" + getEmail() + ", url=" + personPhoto);
            }
            handleAuthenticationRequestResult(result.isSuccess());
        }
    }

}

