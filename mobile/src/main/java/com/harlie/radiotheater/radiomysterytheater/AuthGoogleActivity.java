package com.harlie.radiotheater.radiomysterytheater;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.harlie.radiotheater.radiomysterytheater.utils.LogHelper;

import static com.firebase.ui.auth.ui.AcquireEmailHelper.RC_SIGN_IN;

public class AuthGoogleActivity
        extends BaseActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
{
    private final static String TAG = "LEE: <" + AuthGoogleActivity.class.getSimpleName() + ">";

    private static final String STATE_RESOLVING_ERROR = "resolving_error";

    private GoogleApiClient mGoogleApiClient;
    private Handler handler;

    // Request code to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    // Unique tag for the error dialog fragment
    private static final String DIALOG_ERROR = "dialog_error";
    // Bool to track whether the app is already resolving an error
    private boolean mResolvingError = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        LogHelper.v(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        mResolvingError = savedInstanceState != null
                && savedInstanceState.getBoolean(STATE_RESOLVING_ERROR, false);
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
        if (getAuth() == null) {
            LogHelper.v(TAG, "unable to get FirebaseAuth!");
            startAuthenticationActivity();
            return;
        }
        if (getAuth().getCurrentUser() != null && ! doINeedToCreateADatabase()) {
            LogHelper.v(TAG, "--> Firebase: user=" + getAuth().getCurrentUser().getDisplayName() + " already signed in!");
            startAutoplayActivity(false);
            overridePendingTransition(0,0);
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
        signInIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivityForResult(signInIntent, RC_SIGN_IN);
        overridePendingTransition(0,0);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_RESOLVING_ERROR, mResolvingError);
    }

    @Override
    public void onBackPressed() {
        LogHelper.v(TAG, "onBackPressed");
        startAuthenticationActivity();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        LogHelper.v(TAG, "onConnectionFailed - connectionResult=" + connectionResult.isSuccess());
        if (mResolvingError) {
            // Already attempting to resolve an error.
            LogHelper.v(TAG, "Already attempting to resolve an error.");
            startAuthenticationActivity();
        } else if (connectionResult.hasResolution()) {
            try {
                mResolvingError = true;
                connectionResult.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                LogHelper.v(TAG, "There was an error with the resolution intent. Try again.");
                mGoogleApiClient.connect();
            }
        } else {
            // Show dialog using GoogleApiAvailability.getErrorDialog()
            LogHelper.v(TAG, "Show dialog using GoogleApiAvailability.getErrorDialog()");
            showErrorDialog(connectionResult.getErrorCode());
            mResolvingError = true;
        }
    }

    // from: https://developers.google.com/android/guides/api-client#handle_connection_failures
    // Creates a dialog for an error message
    private void showErrorDialog(final int errorCode) {
        // Create a fragment for the error dialog
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getSupportFragmentManager(), "errordialog");
    }

    // Called from ErrorDialogFragment when the dialog is dismissed.
    public void onDialogDismissed() {
        mResolvingError = false;
        startAuthenticationActivity();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LogHelper.v(TAG, "---> onConnected: bundle="+bundle.toString());
    }

    @Override
    public void onConnectionSuspended(int i) {
        LogHelper.v(TAG, "---> onConnectionSuspended: i="+i);
    }

    // A fragment to display an error dialog
    public static class ErrorDialogFragment extends DialogFragment {
        public ErrorDialogFragment() { }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = this.getArguments().getInt(DIALOG_ERROR);
            return GoogleApiAvailability.getInstance().getErrorDialog(
                    this.getActivity(), errorCode, REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            ((AuthGoogleActivity) getActivity()).onDialogDismissed();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogHelper.v(TAG, "---> onActivityResult: requestCode="+requestCode+", resultCode="+resultCode);
        super.onActivityResult(requestCode, resultCode, data);
        //
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        //
        if (requestCode == REQUEST_RESOLVE_ERROR) {
            LogHelper.v(TAG, "REQUEST_RESOLVE_ERROR");
            mResolvingError = false;
            if (resultCode == RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                LogHelper.v(TAG, "Make sure the app is not already connected or attempting to connect");
                if (!mGoogleApiClient.isConnecting() && !mGoogleApiClient.isConnected()) {
                    LogHelper.v(TAG, "...looks ok to connect");
                    mGoogleApiClient.connect();
                    return;
                }
            }
        }
        else if (requestCode == RC_SIGN_IN) {
            LogHelper.v(TAG, "RC_SIGN_IN");
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result == null) {
                LogHelper.v(TAG, "GoogleSignInResult is null!");
                handleAuthenticationRequestResult(false);
                return;
            }
            if (result.isSuccess()) {
                LogHelper.v(TAG, "result.isSuccess() == true");
                GoogleSignInAccount acct = result.getSignInAccount();
                if (acct != null) {
                    final String personName = acct.getDisplayName();
                    final String personEmail = BuildConfig.SECRET_AUTH_PREFIX + acct.getEmail(); // does not collide with real email
                    final String personId = acct.getId();
                    final String personPass = acct.getId() + BuildConfig.SECRET_PASS_SUFFIX; // unique, repeatable and private
                    final Uri personPhoto = acct.getPhotoUrl();
                    setEmail(personEmail);
                    setPass(personPass);
                    // we're in.  now here come's the trick:
                    // ok, now do the login invisibly with email to avoid extra app manifest permissions
                    LogHelper.v(TAG, "GOOGLE: name=" + personName + ", id=" + personId + ", email=" + getEmail() + ", url=" + personPhoto);
                    Intent authEmailIntent = new Intent(this, AuthEmailActivity.class);
                    authEmailIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    authEmailIntent.putExtra("name", personName);
                    authEmailIntent.putExtra("email", personEmail);
                    authEmailIntent.putExtra("pass", personPass);
                    authEmailIntent.putExtra("photo", personPhoto);
                    authEmailIntent.putExtra("DO_AUTH", true);
                    LogHelper.v(TAG, "---> DO_AUTH");
                    authEmailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(authEmailIntent);
                    overridePendingTransition(0, 0);
                    finish();
                    return;
                }
            }
        }
        LogHelper.v(TAG, "GOOGLE: unable to retrieve: name, email, id, photo");
        startAuthenticationActivity();
    }

}

