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

import static com.firebase.ui.auth.ui.AcquireEmailHelper.RC_SIGN_IN;

//import com.google.firebase.auth.AuthCredential;
//import com.google.firebase.auth.TwitterAuthProvider;
//import com.twitter.sdk.android.Twitter;
//import com.twitter.sdk.android.core.Callback;
//import com.twitter.sdk.android.core.Result;
//import com.twitter.sdk.android.core.TwitterAuthConfig;
//import com.twitter.sdk.android.core.TwitterException;
//import com.twitter.sdk.android.core.TwitterSession;
//import com.twitter.sdk.android.core.identity.TwitterLoginButton;
//import io.fabric.sdk.android.Fabric;

/**
 * A login screen that offers Firebase login via email/password or Google or Facebook or Twitter
 */
public class AuthenticationActivity
        extends BaseActivity
        implements GoogleApiClient.OnConnectionFailedListener {
    private final static String TAG = "LEE: <" + AuthenticationActivity.class.getSimpleName() + ">";
    private static final int MIN_PASSWORD_LENGTH = 6;

    @BindView(R.id.user) EditText username;
    @BindView(R.id.pass) EditText password;

    @BindString(R.string.login_error) String loginErrorMessage;

    private FirebaseAuth mAuth;
    private GoogleApiClient mGoogleApiClient;
    //private TwitterLoginButton mTwitterLoginButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // first see if Authentication is even needed..
        mAuth = FirebaseAuth.getInstance();
        if (mAuth != null && mAuth.getCurrentUser() != null) {
            Log.v(TAG, "--> Firebase: user=" + mAuth.getCurrentUser().getDisplayName() + " already signed in!");
            checkIfNeedToCreateDatabase();
            startAutoplayActivity();
            return;
        }
        Log.v(TAG, "--> Firebase: user not signed in");

        //TwitterAuthConfig authConfig = new TwitterAuthConfig(BuildConfig.TWITTER_API_KEY, BuildConfig.TWITTER_API_SECRET);
        //Fabric.with(this, new Twitter(authConfig));

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

        /*
        mTwitterLoginButton = (TwitterLoginButton) findViewById(R.id.twitter_login_button);
        mTwitterLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                // The TwitterSession is also available through:
                // Twitter.getInstance().core.getSessionManager().getActiveSession()
                TwitterSession session = result.data;
                // TODO: Remove toast and use the TwitterSession's userID
                // with your app's user model
                String msg = "@" + session.getUserName() + " logged in! (#" + session.getUserId() + ")";
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
            @Override
            public void failure(TwitterException exception) {
                Log.d("TwitterKit", "Login with Twitter failure", exception);
            }
        });
        */
    }

    @OnClick(R.id.submit)
    void submit() {
        Log.v(TAG, "submit - Firebase Login using Email and Password");
        String email = username.getText().toString();
        String pass = password.getText().toString();
        Log.v(TAG, "email="+email);
        if (mAuth != null && email != null && pass != null && isValid(email, pass)) {
            mAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithEmailAndPassword:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        boolean success = task.isSuccessful();
                        if (!success) {
                            Log.w(TAG, "signInWithEmailAndPassword: exception=", task.getException());
                        }
                        handleAuthenticationRequestResult(success);
                    }
                });
        }
        else {
            Toast.makeText(this, "Enter your Email and Password", Toast.LENGTH_LONG).show();
        }
    }

    private void createRadioMysteryTheaterFirebaseAccount() {
        Log.v(TAG, "createRadioMysteryTheaterFirebaseAccount - Firebase Login using Email and Password");
        String email = username.getText().toString();
        String pass = password.getText().toString();
        if (mAuth != null && email != null && pass != null && isValid(email, pass)) {
            mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmailAndPassword:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        boolean success = task.isSuccessful();
                        if (!success) {
                            Log.w(TAG, "createUserWithEmailAndPassword: exception=", task.getException());
                            newUserCreationFailed();
                        }
                        else {
                            handleAuthenticationRequestResult(success);
                        }
                    }
                });
        }
        else {
            Toast.makeText(this, "Enter your Email and Password", Toast.LENGTH_LONG).show();
        }
    }

    private boolean isValid(String email, String pass) {
        boolean result = true;
        if (pass != null && pass.length() >= MIN_PASSWORD_LENGTH) {
            // from: http://stackoverflow.com/questions/624581/what-is-the-best-java-email-address-validation-method
            String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
            java.util.regex.Matcher m = p.matcher(email);
            result = m.matches();
        }
        else {
            Toast.makeText(this, "Password too short. Use "+MIN_PASSWORD_LENGTH+"+ characters.", Toast.LENGTH_LONG).show();
            result = false;
        }
        return result;
    }

    private void newUserCreationFailed() {
        Log.v(TAG, "newUserCreationFailed");
        Toast.makeText(this, "Invalid Email or Password", Toast.LENGTH_SHORT).show();
    }

    private void userLoginSuccess() {
        Log.v(TAG, "userLoginFailed");
        Toast.makeText(this, "Successful", Toast.LENGTH_SHORT).show();
    }

    private void userLoginFailed() {
        Log.v(TAG, "userLoginFailed");
        Toast.makeText(this, "Authorization Failed", Toast.LENGTH_SHORT).show();
    }

    private void handleAuthenticationRequestResult(boolean success) {
        Log.d(TAG, "handleAuthenticationRequestResult - success="+success);
        if (!success) {
            userLoginFailed();
            String email = username.getText().toString();
            String pass = password.getText().toString();
            if ((email != null && email.length() > 0) && (pass != null && pass.length() > 0)) {
                Log.v(TAG, "confirm new account creation for email="+email);
                createRadioMysteryTheaterFirebaseAccount();
            }
        }
        else {
            userLoginSuccess();
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
        Log.v(TAG, "onActivityResult - requestCode="+requestCode+", resultCode="+resultCode);
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Log.v(TAG, "Google result!");
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            boolean success = false;
            if (result != null) {
                success = result.isSuccess();
            }
            if (success) {
                GoogleSignInAccount acct = result.getSignInAccount();
                Log.v(TAG, "Google Sign In was successful for "+acct.getDisplayName());
            }
            handleAuthenticationRequestResult(success);
        }
        /*
        else {
            Log.v(TAG, "Twitter result!");
            // Make sure that the mTwitterLoginButton hears the result from any
            // Activity that it triggered.
            mTwitterLoginButton.onActivityResult(requestCode, resultCode, data);
        }
        */

    }

    /*
    private void handleTwitterSession(TwitterSession session) {
        Log.d(TAG, "handleTwitterSession:" + session);

        AuthCredential credential = TwitterAuthProvider.getCredential(
                session.getAuthToken().token,
                session.getAuthToken().secret);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        boolean success = task.isSuccessful();
                        if (!success) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            userLoginFailed();
                        }
                        else {
                            userLoginSuccess();
                            handleAuthenticationRequestResult(success);
                        }
                    }
                });
    }
    */

    @OnClick(R.id.facebook_auth)
    void authenticateFacebook() {
        Log.v(TAG, "authenticateTwitter - Firebase Auth using Facebook");
        // TODO - FIXME
    }

    private void startAutoplayActivity() {
        Intent autoplayIntent = new Intent(this, AutoplayActivity.class);
        startActivity(autoplayIntent);
        finish();
    }

    private void checkIfNeedToCreateDatabase() {
        Log.v(TAG, "checkIfNeedToCreateDatabase - TODO");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.v(TAG, "onConnectionFailed - connectionResult="+connectionResult.isSuccess());
        handleAuthenticationRequestResult(connectionResult.isSuccess());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

}

