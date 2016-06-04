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

import com.firebase.ui.auth.AuthUI;
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
public class AuthenticationActivity extends BaseActivity {
    private final static String TAG = "LEE: <" + AuthenticationActivity.class.getSimpleName() + ">";

    @BindView(R.id.user) EditText username;
    @BindView(R.id.pass) EditText password;

    @BindString(R.string.login_error) String loginErrorMessage;

    @OnClick(R.id.submit)
    void submit() {
        Log.v(TAG, "submit - Firebase Login using Email and Password");
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setProviders(
                                AuthUI.EMAIL_PROVIDER)
                        .build(),
                RC_SIGN_IN);
    }

    @OnClick(R.id.google_auth)
    void authenticateGoogle() {
        Log.v(TAG, "authenticateGoogle - Firebase Auth using Google");
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setProviders(
                                AuthUI.GOOGLE_PROVIDER)
                        .build(),
                RC_SIGN_IN);
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
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            Log.v(TAG, "Firebase: user already signed in");
            loadRadioTheaterSQLiteTables();
            Intent autoplayIntent = new Intent(this, AutoplayActivity.class);
            startActivity(autoplayIntent);
            finish();
        } else {
            Log.v(TAG, "Firebase: user not signed in");
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
        }
    }

    private void loadRadioTheaterSQLiteTables() {
        Log.v(TAG, "loadRadioTheaterSQLiteTables - TODO");
    }

}

