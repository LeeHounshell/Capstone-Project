package com.harlie.radiotheater.radiomysterytheater;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.harlie.radiotheater.radiomysterytheater.data.RadioTheaterHelper;
import com.harlie.radiotheater.radiomysterytheater.data.configepisodes.ConfigEpisodesColumns;
import com.harlie.radiotheater.radiomysterytheater.data.configepisodes.ConfigEpisodesContentValues;
import com.harlie.radiotheater.radiomysterytheater.data.configepisodes.ConfigEpisodesCursor;
import com.harlie.radiotheater.radiomysterytheater.data.configuration.ConfigurationColumns;
import com.harlie.radiotheater.radiomysterytheater.data.configuration.ConfigurationContentValues;
import com.harlie.radiotheater.radiomysterytheater.data.configuration.ConfigurationCursor;
import com.harlie.radiotheater.radiomysterytheater.data.episodes.EpisodesCursor;
import com.harlie.radiotheater.radiomysterytheater.data_helper.LoadRadioTheaterTablesAsyncTask;
import com.harlie.radiotheater.radiomysterytheater.data_helper.LoadingAsyncTask;
import com.harlie.radiotheater.radiomysterytheater.data_helper.SQLiteHelper;
import com.harlie.radiotheater.radiomysterytheater.firebase.FirebaseConfigEpisode;
import com.harlie.radiotheater.radiomysterytheater.firebase.FirebaseConfiguration;
import com.harlie.radiotheater.radiomysterytheater.playback.LocalPlayback;
import com.harlie.radiotheater.radiomysterytheater.utils.CheckPlayStore;
import com.harlie.radiotheater.radiomysterytheater.utils.CircleViewHelper;
import com.harlie.radiotheater.radiomysterytheater.utils.LogHelper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import at.grabner.circleprogress.AnimationState;
import at.grabner.circleprogress.AnimationStateChangedListener;
import at.grabner.circleprogress.CircleProgressView;
import at.grabner.circleprogress.TextMode;

import static com.harlie.radiotheater.radiomysterytheater.data_helper.RadioTheaterContract.ActorsEntry;
import static com.harlie.radiotheater.radiomysterytheater.data_helper.RadioTheaterContract.ConfigEpisodesEntry;
import static com.harlie.radiotheater.radiomysterytheater.data_helper.RadioTheaterContract.ConfigurationEntry;
import static com.harlie.radiotheater.radiomysterytheater.data_helper.RadioTheaterContract.EpisodesEntry;
import static com.harlie.radiotheater.radiomysterytheater.data_helper.RadioTheaterContract.WritersEntry;

@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity {
    private final static String TAG = "LEE: <" + BaseActivity.class.getSimpleName() + ">";

    protected static final int MAX_TRIAL_EPISODES = 19;
    protected static final int MIN_EMAIL_LENGTH = 5;
    protected static final int MIN_PASSWORD_LENGTH = 6;
    protected static final int TOTAL_SIZE_TO_COPY_IN_BYTES = 1347584;
    protected static final int ANIMATION_DELAY = 100;
    protected static final String FIREBASE_WRITERS_URL = "radiomysterytheater/0/writers";
    protected static final String FIREBASE_ACTORS_URL = "radiomysterytheater/1/actors";
    protected static final String FIREBASE_SHOWS_URL = "radiomysterytheater/2/shows";
    protected static final boolean COPY_PACKAGED_SQLITE_DATABASE = true;

    protected long mDuration;
    protected long mCurrentPosition;

    protected static String sAdvId;
    protected static String sName;
    protected static String sEmail;
    protected static String sUID;
    protected static String sPassword;

    protected static int sAllListenCount;
    protected static long sEpisodeNumber;
    protected static String sAirdate;
    protected static String sEpisodeTitle;
    protected static String sEpisodeDescription;
    protected static String sEpisodeWeblinkUrl;
    protected static String sEpisodeDownloadUrl;

    protected static volatile boolean sOnRestoreInstanceComplete;
    protected static volatile boolean sFoundFirebaseDeviceId;
    protected static volatile boolean sOkLoadFirebaseConfiguration;
    protected static volatile boolean sShowPercentUnit;

    // need to save these across Activities
    protected static volatile boolean sHandleRotationEvent;
    protected static volatile boolean sLoadingScreenEnabled;
    protected static volatile boolean sBeginLoading;
    protected static volatile boolean sAutoplayNextNow;
    protected static volatile boolean sEnableFAB;
    protected static volatile boolean sWaitForMedia;

    // need to save these across Activities
    protected static volatile boolean sPurchased;
    protected static volatile boolean sNoAdsForShow;
    protected static volatile boolean sDownloaded;
    protected static volatile boolean sEpisodeHeard;
    protected static volatile boolean sHaveRealDuration;
    protected static volatile boolean sLoadedOK;

    private static int mCount;

    public static volatile boolean sProgressViewSpinning;

    protected ConfigurationContentValues mConfiguration;
    protected FirebaseAuth mAuth;
    protected Firebase mFirebase;
    protected DatabaseReference mDatabase;
    protected FirebaseAnalytics mFirebaseAnalytics;
    protected Handler mHandler;
    protected View mRootView;
    protected CircleProgressView mCircleView;

    private Interpolator interpolator;
    private RelativeLayout bgViewGroup;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        LogHelper.v(TAG, "onCreate");
        sOnRestoreInstanceComplete = false;

        Transition enterTransition = new android.transition.Fade();
        getWindow().setEnterTransition(enterTransition);
        Transition returnTransition = new android.transition.Slide();
        getWindow().setReturnTransition(returnTransition);

        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            sHandleRotationEvent = true;
            onRestoreInstanceState(savedInstanceState);
        }
        else {
            sHandleRotationEvent = false;
            sLoadingScreenEnabled = false;
            sBeginLoading = false;
            sAutoplayNextNow = false;
            sEnableFAB = false;
            sWaitForMedia = false;

            sPurchased = false;
            sNoAdsForShow = false;
            sDownloaded = false;
            sEpisodeHeard = false;
            sHaveRealDuration = false;
            sLoadedOK = false;

            Bundle playInfo = getIntent().getExtras();
            if (playInfo != null) {
                restorePlayInfoFromBundle(playInfo);
            }
        }

        mRootView = findViewById(android.R.id.content);
        mHandler = new Handler();

        initializeFirebase();

        final BaseActivity baseActivity = this;

        // capture the advertising id and save it
        if (! isLoadedOK()) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    if (getEmail() != null) {
                        try {
                            AdvertisingIdClient.Info adInfo = AdvertisingIdClient.getAdvertisingIdInfo(baseActivity);
                            sAdvId = adInfo != null ? adInfo.getId() : null;
                        }
                        catch (IOException | GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException exception) {
                            LogHelper.e(TAG, "*** UNABLE TO LOAD ADVERT ID ***"); // and it is needed for my Firebase key...
                        }
                        finally {
                            if (sAdvId == null) {
                                sAdvId = getAdvertId(); // from shared pref
                            }
                            if (sAdvId != null) {
                                setAdvertId(sAdvId); // also in shared pref
                                if (sOkLoadFirebaseConfiguration) {
                                    loadAnyExistingFirebaseConfigurationValues(sAdvId);
                                }
                            }
                        }
                    }
                }
            });

            setupWindowAnimations();
        }
    }

    protected void initializeFirebase() {
        Firebase.setAndroidContext(this);
        mAuth = FirebaseAuth.getInstance();
        mFirebase = new Firebase("https://radio-mystery-theater.firebaseio.com");
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    private void setupWindowAnimations() {
        interpolator = AnimationUtils.loadInterpolator(this, android.R.interpolator.linear_out_slow_in);
        setupEnterAnimations();
        setupExitAnimations();
    }

    // transition animation code from: https://github.com/lgvalle/Material-Animations/blob/master/app/src/main/java/com/lgvalle/material_animations/RevealActivity.java
    private void setupEnterAnimations() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        Transition transition = TransitionInflater.from(this).inflateTransition(R.transition.changebounds_with_arcmotion);
        getWindow().setSharedElementEnterTransition(transition);
        transition.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
            }

            @Override
            public void onTransitionEnd(Transition transition) {
                // Removing listener here is very important because shared element transition is executed again backwards on exit. If we don't remove the listener this code will be triggered again.
                transition.removeListener(this);
                hideTarget();
                animateRevealShow(toolbar);
                animateButtonsIn();
            }

            @Override
            public void onTransitionCancel(Transition transition) {
            }

            @Override
            public void onTransitionPause(Transition transition) {
            }

            @Override
            public void onTransitionResume(Transition transition) {
            }
        });
    }

    private void setupExitAnimations() {
        Fade returnTransition = new Fade();
        getWindow().setReturnTransition(returnTransition);
        returnTransition.setDuration(getResources().getInteger(R.integer.anim_duration_medium));
        returnTransition.setStartDelay(getResources().getInteger(R.integer.anim_duration_medium));
        returnTransition.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
                transition.removeListener(this);
                animateButtonsOut();
                animateRevealHide(bgViewGroup);
            }

            @Override
            public void onTransitionEnd(Transition transition) {
            }

            @Override
            public void onTransitionCancel(Transition transition) {
            }

            @Override
            public void onTransitionPause(Transition transition) {
            }

            @Override
            public void onTransitionResume(Transition transition) {
            }
        });
    }

    private void hideTarget() {
        //findViewById(R.id.shared_target).setVisibility(View.GONE);
    }

    private void animateButtonsIn() {
        if (bgViewGroup == null) {
            return;
        }
        for (int i = 0; i < bgViewGroup.getChildCount(); i++) {
            View child = bgViewGroup.getChildAt(i);
            child.animate()
                    .setStartDelay(100 + i * ANIMATION_DELAY)
                    .setInterpolator(interpolator)
                    .alpha(1)
                    .scaleX(1)
                    .scaleY(1);
        }
    }

    private void animateButtonsOut() {
        if (bgViewGroup == null) {
            return;
        }
        for (int i = 0; i < bgViewGroup.getChildCount(); i++) {
            View child = bgViewGroup.getChildAt(i);
            child.animate()
                    .setStartDelay(i)
                    .setInterpolator(interpolator)
                    .alpha(0)
                    .scaleX(0f)
                    .scaleY(0f);
        }
    }

    private void animateRevealShow(View viewRoot) {
        if (viewRoot == null) {
            return;
        }
        int cx = (viewRoot.getLeft() + viewRoot.getRight()) / 2;
        int cy = (viewRoot.getTop() + viewRoot.getBottom()) / 2;
        int finalRadius = Math.max(viewRoot.getWidth(), viewRoot.getHeight());

        Animator anim = ViewAnimationUtils.createCircularReveal(viewRoot, cx, cy, 0, finalRadius);
        viewRoot.setVisibility(View.VISIBLE);
        anim.setDuration(getResources().getInteger(R.integer.anim_duration_long));
        anim.setInterpolator(new AccelerateInterpolator());
        anim.start();
    }

    private void animateRevealHide(final View viewRoot) {
        if (viewRoot == null) {
            return;
        }
        int cx = (viewRoot.getLeft() + viewRoot.getRight()) / 2;
        int cy = (viewRoot.getTop() + viewRoot.getBottom()) / 2;
        int initialRadius = viewRoot.getWidth();

        Animator anim = ViewAnimationUtils.createCircularReveal(viewRoot, cx, cy, initialRadius, 0);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                viewRoot.setVisibility(View.INVISIBLE);
            }
        });
        anim.setDuration(getResources().getInteger(R.integer.anim_duration_medium));
        anim.start();
    }

    @Override
    public void onDestroy() {
        LogHelper.v(TAG, "onDestroy");
        super.onDestroy();

        sAdvId = null;
        sName = null;
        sEmail = null;
        sUID = null;
        sPassword = null;

        sAllListenCount = 0;
        sEpisodeNumber = 0L;
        sAirdate = null;
        sEpisodeTitle = null;
        sEpisodeDescription = null;
        sEpisodeWeblinkUrl = null;
        sEpisodeDownloadUrl = null;

        sOnRestoreInstanceComplete = false;
        sFoundFirebaseDeviceId = false;
        sOkLoadFirebaseConfiguration = false;
        sShowPercentUnit = false;

        sHandleRotationEvent = false;
        sLoadingScreenEnabled = false;
        sBeginLoading = false;
        sAutoplayNextNow = false;
        sEnableFAB = false;
        sWaitForMedia = false;

        sPurchased = false;
        sNoAdsForShow = false;
        sDownloaded = false;
        sEpisodeHeard = false;
        sHaveRealDuration = false;
        sLoadedOK = false;

        mCount = 0;
        sProgressViewSpinning = false;
    }

    @Override
    protected void onResume() {
        LogHelper.v(TAG, "onResume");
        super.onResume();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    public void onBackPressed() {
        LogHelper.v(TAG, "onBackPressed");
        super.onBackPressed();
    }

    //from: http://stackoverflow.com/questions/31662416/show-collapsingtoolbarlayout-title-only-when-collapsed
    protected void configureToolbarTitleBehavior() {
        LogHelper.v(TAG, "configureToolbarTitleBehavior");
        final String title = getResources().getString(R.string.app_name);
        final CollapsingToolbarLayout collapsingToolbarLayout = ((CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar_layout));
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.toolbar_container);
        if (appBarLayout != null) {
            appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
                boolean isShow = false;
                int scrollRange = -1;

                @Override
                public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                    if (appBarLayout != null) {
                        if (scrollRange == -1) {
                            scrollRange = appBarLayout.getTotalScrollRange();
                        }
                    }
                    if (collapsingToolbarLayout != null) {
                        if (scrollRange + verticalOffset == 0) {
                            collapsingToolbarLayout.setTitle(title);
                            isShow = true;
                        } else if (isShow) {
                            collapsingToolbarLayout.setTitle("");
                            isShow = false;
                        }
                    }
                }
            });
        }
    }

    protected boolean isValid(String email, String pass) {
        boolean result = false;
        if (email != null && email.length() > MIN_EMAIL_LENGTH && pass != null && pass.length() >= MIN_PASSWORD_LENGTH) {
            // from: http://stackoverflow.com/questions/624581/what-is-the-best-java-email-address-validation-method
            String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
            java.util.regex.Matcher m = p.matcher(email);
            result = m.matches();
            LogHelper.v(TAG, "check isValid: result="+result+", email="+email);
        }
        return result;
    }

    protected void handleAuthenticationRequestResult(boolean loginSuccess) {
        LogHelper.d(TAG, "handleAuthenticationRequestResult - loginSuccess=" + loginSuccess);
        if (loginSuccess) {
            authenticateRadioMysteryTheaterFirebaseAccount(getEmail(), getPass());
        } else {
            userLoginFailed();
            startAuthenticationActivity();
        }
    }

    //
    // FIREBASE COMMON USER AUTHENTICATION BLOCK
    //
    protected void authenticateRadioMysteryTheaterFirebaseAccount(final String email, final String pass) {
        LogHelper.v(TAG, "authenticateRadioMysteryTheaterFirebaseAccount - Firebase Login using email="+email+", and password="+pass);
        if (mAuth != null && email != null && pass != null && isValid(email, pass)) {
            LogHelper.v(TAG, "authenticateRadioMysteryTheaterFirebaseAccount: GOOD");
            final BaseActivity activity = this;
            mAuth.createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            LogHelper.d(TAG, "createUserWithEmailAndPassword:onComplete:" + task.isSuccessful());

                            // If sign in fails, display a message to the user. If sign in succeeds
                            // the auth state listener will be notified and logic to handle the
                            // signed in user can be handled in the listener.
                            boolean success = task.isSuccessful();
                            if (!success) {
                                success = checkExceptionReason(task, activity);
                            }
                            if (success) {
                                String uid = mAuth.getCurrentUser().getUid();
                                setEmail(email);
                                setPass(pass);
                                setUID(uid);
                                trackSignupWithFirebaseAnalytics();
                                // ok, we're in
                                startAutoplayActivity(true);
                            }
                            else {
                                userLoginFailed();
                                startAuthenticationActivity();
                            }
                        }
                    });
        }
        else {
            LogHelper.w(TAG, "authenticateRadioMysteryTheaterFirebaseAccount: problem authenticating - mAuth="+mAuth+", email="+email+", pass="+pass+", isValid="+isValid(email, pass));
            String message = getResources().getString(R.string.enter_email);
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            startAuthenticationActivity();
        }
    }

    protected boolean checkExceptionReason(@NonNull Task<AuthResult> task, BaseActivity activity) {
        LogHelper.v(TAG, "checkExceptionReason");
        boolean success = false;
        if (task.getException() instanceof com.google.firebase.auth.FirebaseAuthInvalidCredentialsException) {
            LogHelper.v(TAG, "*** FAIL - com.google.firebase.auth.FirebaseAuthInvalidCredentialsException ***");
            String message = getResources().getString(R.string.invalid_email);
            Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
            startAuthenticationActivity();
        }
        else if (task.getException() instanceof com.google.firebase.auth.FirebaseAuthUserCollisionException) {
            LogHelper.v(TAG, "*** OK - com.google.firebase.auth.FirebaseAuthUserCollisionException ***"); // user+pass record already exists, so ignore
            success = true;
        }
        else if(task.getException() instanceof com.google.firebase.auth.FirebaseAuthInvalidUserException) {
            LogHelper.v(TAG, "*** OK - com.google.firebase.auth.FirebaseAuthInvalidUserException"); // found deleted user - so just add them back
            success = true;
        }
        else if (task.getException() instanceof com.google.firebase.FirebaseTooManyRequestsException) {
            LogHelper.v(TAG, "*** FAIL - com.google.firebase.FirebaseTooManyRequestsException ***");
            String message = getResources().getString(R.string.too_many_requests);
            Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
            startAuthenticationActivity();
        }
        else {
            LogHelper.v(TAG, "*** authentication failed *** reason="+task.getException().getLocalizedMessage());
            String message = getResources().getString(R.string.auth_fail);
            Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
            startAuthenticationActivity();
        }
        return success;
    }

    protected void userLoginSuccess() {
        LogHelper.v(TAG, "userLoginSuccess");
        String message = getResources().getString(R.string.successful);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    protected void userLoginFailed() {
        LogHelper.v(TAG, "userLoginFailed");
        String message = getResources().getString(R.string.auth_fail);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void startAuthenticationActivity() {
        LogHelper.v(TAG, "---> startAuthenticationActivity <---");
        Intent authenticationIntent = new Intent(this, AuthenticationActivity.class);
        // close existing activity stack regardless of what's in there and create new root
        authenticationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        authenticationIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        Bundle playInfo = new Bundle();
        savePlayInfoToBundle(playInfo);
        authenticationIntent.putExtras(playInfo);
        authenticationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(this, android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
        startActivity(authenticationIntent, bundle);
        overridePendingTransition(0,0);
        finish();
    }

    public void startAutoplayActivity(boolean animate) {
        LogHelper.v(TAG, "---> startAutoplayActivity: animate="+animate+" <---");
        boolean dbMissing = doINeedToCreateADatabase();
        LogHelper.v(TAG, "---> dbMissing="+dbMissing);
        if (dbMissing) {
            startProgressViewSpinning();
            LogHelper.v(TAG, "*** first need to build the RadioMysteryTheater database (SPINNING) ***");
            String message = getResources().getString(R.string.initializing);
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    Looper.prepare();
                    boolean dbMissing = doINeedToCreateADatabase();
                    if (! dbMissing) {
                        return;
                    }
                    loadSqliteDatabase(true); // copy ok
                    Looper.loop();
                }
            }).start();
        }
        else {
            LogHelper.v(TAG, "*** READY TO START RADIO MYSTERY THEATER ***");
            if (getEmail() != null) {
                userLoginSuccess();
            }
            // save authentication to Shared Prefs
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(RadioTheaterApplication.getRadioTheaterApplicationContext());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("authentication", getEmail());
            editor.putString("userUID", getUID());
            editor.apply();

            Intent autoplayIntent = new Intent(this, AutoplayActivity.class);
            // close existing activity stack regardless of what's in there and create new root
            autoplayIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            // Transition Effects..
            Bundle bundle;
            if (animate) {
                LogHelper.v(TAG, "--> using Simple Transition animation..");
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this);
                bundle = options.toBundle();
            }
            else {
                LogHelper.v(TAG, "--> using Standard Fade Transition animation..");
                bundle = ActivityOptionsCompat.makeCustomAnimation(this, android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
            }
            savePlayInfoToBundle(bundle);
            autoplayIntent.putExtras(bundle);
            autoplayIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(autoplayIntent, bundle);
            finish();
        }
    }

    public void playNow() {
        String episodeNumber = String.valueOf(getEpisodeNumber());
        LogHelper.v(TAG, "playNow: episodeNumber="+episodeNumber);
        if (LocalPlayback.getCurrentState() == PlaybackStateCompat.STATE_PLAYING) {
            LogHelper.v(TAG, "*** STOP PLAYING CURRENT EPISODE BEFORE STARING WITH A FRESH ONE ***");
            RadioControlIntentService.startActionStop(this, "DETAIL", episodeNumber, getEpisodeDownloadUrl());
        }
        Intent autoplayIntent = new Intent(this, AutoplayActivity.class);
        // setup a shared-element transition..
        LogHelper.v(TAG, "onClick - PLAY NOW - using shared element transition");
        AppCompatButton playNow = (AppCompatButton) findViewById(R.id.play_now);
        Bundle playInfo = new Bundle();
        if (playNow != null) {
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this, playNow, "PlayNow");
            playInfo = options.toBundle();
        }

        // NOTE: there is an Android problem setting these intent flags with a shared-element transition:
        //autoplayIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        //
        // to get around this problem, I have over-ridden onBackPressed in AutoplayActivity so that it clears the back stack before exiting.

        savePlayInfoToBundle(playInfo);
        autoplayIntent.putExtras(playInfo);
        autoplayIntent.putExtra("PLAY_NOW", String.valueOf(episodeNumber));
        startActivity(autoplayIntent, playInfo);
    }

    private String copyFileFromAssets(String inFileName, String outFileName) throws Exception {
        LogHelper.v(TAG, "copyFileFromAssets: INPUT="+inFileName+", OUTPUT="+outFileName);
        InputStream input = getApplicationContext().getAssets().open(inFileName);
        if (input == null) {
            //throw new Exception("null input stream for asset="+ inFileName);
            return "null input stream for asset - inFileName="+ inFileName;
        }
        LogHelper.v(TAG, "InputStream is open.");
        OutputStream output = new FileOutputStream(getApplicationContext().getDatabasePath(outFileName));
        if (output == null) {
            //throw new Exception("null output stream for database="+outFileName);
            return "null output stream for database - outFileName="+outFileName;
        }
        LogHelper.v(TAG, "OutputStream is open.");
        byte[] buffer = new byte[1024];
        int len;
        while ((len = input.read(buffer)) > 0) {
            mCount += len;
            try {
                Thread.sleep(3);
            } catch (Exception e) { };
            CircleViewHelper.setCircleViewValue((float) mCount, this);
            output.write(buffer, 0, len);
        }
        output.flush();
        output.close();
        input.close();
        return null;
    }

    //--------------------------------------------------------------------------------
    // SQLite Database Related
    //--------------------------------------------------------------------------------

    protected boolean doINeedToCreateADatabase() {
        LogHelper.v(TAG, "doINeedToCreateADatabase");
        if ((isExistingTable("EPISODES")) && (isExistingTable("ACTORS")) && (isExistingTable("WRITERS")))
        {
            LogHelper.v(TAG, "*** Found SQLITE Tables! ***");
            return false;
        }
        LogHelper.v(TAG, "*** NO SQLITE DATABASE FOUND! ***");
        return true;
    }

    // from: http://stackoverflow.com/questions/3058909/how-does-one-check-if-a-table-exists-in-an-android-sqlite-database
    protected boolean isExistingTable(String tableName) {
        LogHelper.v(TAG, "isExistingTable: "+tableName);
        long rowId = 1;
        Uri CONTENT_URI = null;
        String whereClause = null;
        String whereArgs[] = null;
        if (tableName.toUpperCase(Locale.getDefault()).equals("EPISODES")) {
            CONTENT_URI = EpisodesEntry.buildEpisodeUri(rowId);
            tableName = EpisodesEntry.TABLE_NAME;
            whereClause = EpisodesEntry.FIELD_EPISODE_NUMBER + "=?";
            whereArgs = new String[]{Long.toString(rowId)};
        }
        else if (tableName.toUpperCase(Locale.getDefault()).equals("ACTORS")) {
            CONTENT_URI = ActorsEntry.buildActorUri(rowId);
            tableName = ActorsEntry.TABLE_NAME;
            whereClause = ActorsEntry.FIELD_ACTOR_ID + "=?";
            whereArgs = new String[]{Long.toString(rowId)};
        }
        else if (tableName.toUpperCase(Locale.getDefault()).equals("WRITERS")) {
            CONTENT_URI = WritersEntry.buildWriterUri(rowId);
            tableName = WritersEntry.TABLE_NAME;
            whereClause = WritersEntry.FIELD_WRITER_ID + "=?";
            whereArgs = new String[]{Long.toString(rowId)};
        }
        else if (tableName.toUpperCase(Locale.getDefault()).equals("CONFIGURATION")) {
            if (getEmail() == null || getEmail().length() == 0) {
                CONTENT_URI = null;
                whereClause = null;
            }
            else {
                CONTENT_URI = ConfigurationEntry.buildConfigurationUri();
                tableName = ConfigurationEntry.TABLE_NAME;
                whereClause = ConfigurationEntry.FIELD_USER_EMAIL + "=?";
                whereArgs = new String[]{Long.toString(rowId)};
            }
        }
        if (CONTENT_URI == null) {
            return false;
        }

        Cursor cursor = getContentResolver().query(
                CONTENT_URI, // the 'content://' Uri to query
                null,        // projection String[] - leaving "columns" null just returns all the columns.
                whereClause, // selection - SQL where
                whereArgs,   // selection args String[] - values for the "where" clause
                null         // sort order and limit (String)
        );

        boolean success = false;
        if (cursor == null || cursor.getCount() == 0) {
            LogHelper.v(TAG, "SQL: nothing found for table "+tableName);
        }
        else {
            LogHelper.v(TAG, "SQL: found "+cursor.getCount()+" records in table "+tableName);
            success = true;
            cursor.close();
        }
        return success;
    }

    public Boolean isPaidEpisode(String episode) {
        Boolean isPaidEpi = false;
        //#IFDEF 'PAID'
        //isPaidEpi = true;
        //#ENDIF

        //#IFDEF 'TRIAL'
        if (mConfiguration != null) {
            isPaidEpi = mConfiguration.values().getAsBoolean(ConfigurationColumns.FIELD_PAID_VERSION);
        }
        if (!isPaidEpi) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(RadioTheaterApplication.getRadioTheaterApplicationContext());
            isPaidEpi = sharedPreferences.getBoolean("userPaid", false); // all episodes paid for?
            if (!isPaidEpi) {
                ConfigEpisodesContentValues existing = SQLiteHelper.getConfigForEpisode(episode);
                if (existing != null && existing.values() != null && existing.values().size() != 0) {
                    ContentValues configEpisode = existing.values();
                    isPaidEpi = configEpisode.getAsBoolean(ConfigEpisodesEntry.FIELD_PURCHASED_ACCESS);
                }
            }
        }
        //#ENDIF
        return isPaidEpi;
    }

    public void setPaidEpisode(String episode, Boolean paid) {
        LogHelper.v(TAG, "setPaidEpisode: episode="+episode+", paid="+paid);
        if (episode == null) {
            // NOTE: special case with NULL episode - mark all episodes as paid
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(RadioTheaterApplication.getRadioTheaterApplicationContext());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("userPaid", paid);
            editor.apply();

            // FIXME: add in-app-payments, allow purchase
            // FIXME: update local SQLite Configuration table with user info
            // FIXME: update Firebase record for this user's email to be PAID
        }
        else {
            // see if a record already exists for this episode
            ConfigEpisodesContentValues existing = SQLiteHelper.getConfigForEpisode(episode);
            ContentValues configurationValues;
            try {
                if (existing != null && existing.values() != null && existing.values().size() != 0) {
                    // UPDATE and mark an individual episode as paid
                    configurationValues = existing.values();
                    LogHelper.v(TAG, "FOUND: so update ConfigEntry for episode "+episode+" with paid="+paid);
                    configurationValues.put(ConfigEpisodesEntry.FIELD_EPISODE_NUMBER, episode);
                    configurationValues.put(ConfigEpisodesEntry.FIELD_PURCHASED_ACCESS, paid);
                    SQLiteHelper.updateConfigEntryValues(episode, configurationValues);
                } else {
                    // CREATE and mark an individual episode as paid
                    LogHelper.v(TAG, "NOT FOUND: so create ConfigEntry for episode "+episode+" with paid="+paid);
                    configurationValues = new ContentValues();
                    configurationValues.put(ConfigEpisodesEntry.FIELD_EPISODE_NUMBER, episode);
                    configurationValues.put(ConfigEpisodesEntry.FIELD_PURCHASED_ACCESS, paid);
                    Uri result = SQLiteHelper.insertConfigEntryValues(configurationValues);
                }
            }
            catch (Exception e) {
                LogHelper.e(TAG, "unable to create ConfigEntry for episode "+episode+" e="+e);
            }
        }
    }

    // this kicks off a series of AsyncTasks to load SQL tables from Firebase
    protected void loadSqliteDatabase(boolean okToCopyDatabase) {
        LogHelper.v(TAG, "*** loadSqliteDatabase ***");
        mCircleView = (CircleProgressView) getRootView().findViewById(R.id.circle_view);

        //if (! ((isExistingTable("EPISODES")) && (isExistingTable("ACTORS")) && (isExistingTable("WRITERS")))) {
        //    LoadRadioTheaterTablesAsyncTask.setTesting(true); // load some dummy data instead of JSON
        //}

        if (okToCopyDatabase && COPY_PACKAGED_SQLITE_DATABASE) {
            startProgressViewSpinning();
            String DB_NAME = RadioTheaterHelper.DATABASE_FILE_NAME;
            try {
                // for performance reasons, I have included a prebuilt-sqlite database

                //#IFDEF 'PAID'
                //String error_db_ok = copyFileFromAssets("paid/" + DB_NAME, DB_NAME);
                //String error_jr_ok = copyFileFromAssets("paid/" + DB_NAME + "-journal", DB_NAME + "-journal");
                //#ENDIF

                //#IFDEF 'TRIAL'
                String error_db_ok = copyFileFromAssets("trial/" + DB_NAME, DB_NAME);
                String error_jr_ok = copyFileFromAssets("trial/" + DB_NAME + "-journal", DB_NAME + "-journal");
                //#ENDIF

                if (error_db_ok != null || error_jr_ok != null) {
                    LogHelper.v(TAG, "*** FAILED TO COPY the prebuilt SQLite database *** - error_db_ok="+error_db_ok+", error_jr_ok="+error_jr_ok);
                    String error = (error_db_ok != null ? error_db_ok : "") + " " + (error_jr_ok != null ? error_jr_ok : null);
                    problemLoadingDatabase(error);
                    return;
                }
                LogHelper.v(TAG, "*** successfully copied prebuilt SQLite database ***");
                boolean dbMissing = doINeedToCreateADatabase();
                if (! dbMissing) {
                    LogHelper.v(TAG, "*** successfully accessed prebuilt SQLite database ***");
                    // ok, we're almost in
                    startAutoplayActivity(false);
                    return;
                }
                else {
                    LogHelper.w(TAG, "*** FAILED TO ACCESSS the prebuilt SQLite database ***");
                    // drop through to the 'runLoadState' call below
                }
            }
            catch (Exception any) {
                LogHelper.e(TAG, "problem copying "+DB_NAME+" database! - "+any);
                // this will create a new SQLite database using Firebase source JSON
                // a circle progress view progresses as the database loads - takes a few minutes to run tho
                String error = "problem copying "+DB_NAME+" database! - error="+any.getMessage();
                problemLoadingDatabase(error);
                return;
            }
        }
        // this will create a new SQLite database using Firebase source JSON
        // a circle progress view progresses as the database loads - takes a few minutes to run tho
        runLoadState(LoadRadioTheaterTablesAsyncTask.LoadState.WRITERS); // begin with first load state
    }

    public static boolean getEpisodeData(ConfigEpisodesCursor configCursor) {
        LogHelper.v(TAG, "getEpisodeData");
        boolean foundEpisode = false;
        if (configCursor != null && configCursor.moveToNext()) {
            // found the next episode to listen to
            sEpisodeNumber = configCursor.getFieldEpisodeNumber();
            sPurchased = configCursor.getFieldPurchasedAccess();
            sNoAdsForShow = configCursor.getFieldPurchasedNoads();
            sDownloaded = configCursor.getFieldEpisodeDownloaded();
            sEpisodeHeard = configCursor.getFieldEpisodeHeard();
            configCursor.close();
            foundEpisode = getEpisodeInfoFor(sEpisodeNumber);
        }
        return foundEpisode;
    }

    public static boolean getEpisodeInfoFor(long episodeId) {
        LogHelper.v(TAG, "getEpisodeInfoFor: "+episodeId);
        // get this episode's detail info
        boolean foundEpisode = false;
        EpisodesCursor episodesCursor = SQLiteHelper.getEpisodesCursor(episodeId);
        if (episodesCursor != null && episodesCursor.moveToNext()) {
            sEpisodeNumber = episodesCursor.getFieldEpisodeNumber();
            sAirdate = episodesCursor.getFieldAirdate();
            sEpisodeTitle = episodesCursor.getFieldEpisodeTitle();
            sEpisodeDescription = episodesCursor.getFieldEpisodeDescription();
            sEpisodeWeblinkUrl = Uri.parse(episodesCursor.getFieldWeblinkUrl()).getEncodedPath();
            sEpisodeDownloadUrl = Uri.parse("http://" + episodesCursor.getFieldDownloadUrl()).toString();
            episodesCursor.close();
            foundEpisode = true;
        }
        return foundEpisode;
    }

    protected void startProgressViewSpinning() {
        LogHelper.v(TAG, "startProgressViewSpinning");
        final BaseActivity baseActivity = this;
        if (! sProgressViewSpinning) {
            sProgressViewSpinning = true;
            getHandler().post(new Runnable() {
                @Override
                public void run() {
                    mCount = 0;
                    CircleViewHelper.showCircleView(baseActivity, getCircleView(), CircleViewHelper.CircleViewType.CREATE_DATABASE);
                    CircleViewHelper.setCircleViewMaximum((float) TOTAL_SIZE_TO_COPY_IN_BYTES, baseActivity);
                    CircleViewHelper.setCircleViewValue((float) mCount, baseActivity);
                }
            });
        }
    }

    private void problemExistingDatabase(String fileName) {
        LogHelper.w(TAG, "problemExistingDatabase, fileName="+fileName);
        mCurrentPosition = 0;
        AlertDialog alertDialog = new AlertDialog.Builder(BaseActivity.this).create();
        alertDialog.setTitle(getResources().getString(R.string.existing_database));
        alertDialog.setMessage(getResources().getString(R.string.database_existing_problem) + "\n\nfile="+fileName);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        trackWithFirebaseAnalytics(String.valueOf(sEpisodeNumber), mCurrentPosition, "load sqlite failed");
                        dialog.dismiss();
                        runLoadState(LoadRadioTheaterTablesAsyncTask.LoadState.WRITERS); // begin with first load state - slow via Internet, but it gets there.
                    }
                });
        alertDialog.show();
    }

    private void problemMissingDatabase(String fileName) {
        LogHelper.w(TAG, "problemMissingDatabase: fileName="+fileName);
        mCurrentPosition = 0;
        AlertDialog alertDialog = new AlertDialog.Builder(BaseActivity.this).create();
        alertDialog.setTitle(getResources().getString(R.string.missing_database));
        alertDialog.setMessage(getResources().getString(R.string.database_missing_problem) + "\n\nfile="+fileName);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        trackWithFirebaseAnalytics(String.valueOf(sEpisodeNumber), mCurrentPosition, "load sqlite failed");
                        dialog.dismiss();
                        runLoadState(LoadRadioTheaterTablesAsyncTask.LoadState.WRITERS); // begin with first load state - slow via Internet, but it gets there.
                    }
                });
        alertDialog.show();
    }

    private void problemLoadingDatabase(String error) {
        LogHelper.w(TAG, "problemLoadingDatabase: error="+error);
        mCurrentPosition = 0;
        AlertDialog alertDialog = new AlertDialog.Builder(BaseActivity.this).create();
        alertDialog.setTitle(getResources().getString(R.string.missing_database));
        alertDialog.setMessage(getResources().getString(R.string.database_loading_problem) + "\n\nerror=" + error);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        trackWithFirebaseAnalytics(String.valueOf(sEpisodeNumber), mCurrentPosition, "load sqlite failed");
                        dialog.dismiss();
                        runLoadState(LoadRadioTheaterTablesAsyncTask.LoadState.WRITERS); // begin with first load state - slow via Internet, but it gets there.
                    }
                });
        alertDialog.show();
    }

    protected void maxTrialEpisodesAreReached() {
        final BaseActivity baseActivity = this;
        if (sPurchased != true) {
            LogHelper.v(TAG, "maxTrialEpisodesAreReached");
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle(getResources().getString(R.string.trial_complete));
            alertDialog.setMessage(getResources().getString(R.string.all_trial_heard));
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getResources().getString(R.string.ok),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            CheckPlayStore.upgradeToPaid(baseActivity);
                        }
                    });
            alertDialog.show();
        }
    }

    public void runLoadState(LoadRadioTheaterTablesAsyncTask.LoadState state) {
        LogHelper.v(TAG, "runLoadState: for state="+state);
        if (state == LoadRadioTheaterTablesAsyncTask.LoadState.WRITERS) {
            loadWritersFromFirebase();
        }
        else
        if (state == LoadRadioTheaterTablesAsyncTask.LoadState.ACTORS) {
            loadActorsFromFirebase();
        }
        else
        if (state == LoadRadioTheaterTablesAsyncTask.LoadState.EPISODES) {
            loadEpisodesFromFirebase();
        }
    }

    public void runLoadStateCallback(LoadRadioTheaterTablesAsyncTask.LoadState state) {
        LogHelper.v(TAG, "runLoadStateCallback: for state="+state);
        if (state == LoadRadioTheaterTablesAsyncTask.LoadState.WRITERS) {
            runLoadState(LoadRadioTheaterTablesAsyncTask.LoadState.ACTORS); // next load the ACTORS
        }
        else
        if (state == LoadRadioTheaterTablesAsyncTask.LoadState.ACTORS) {
            runLoadState(LoadRadioTheaterTablesAsyncTask.LoadState.EPISODES); // finally load the EPISODES
        }
        else
        if (state == LoadRadioTheaterTablesAsyncTask.LoadState.EPISODES) {
            // ok, we're almost in
            startAutoplayActivity(false);
        }
    }

    // first load the WRITERS tables
    public void loadWritersFromFirebase() {
        LogHelper.v(TAG, "loadWritersFromFirebase");
        final BaseActivity activity = this;
        // Attach a listener to read the data initially
        LogHelper.v(TAG, "*** FIREBASE REQUEST *** "+FIREBASE_WRITERS_URL);
        getDatabase().child(FIREBASE_WRITERS_URL).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                LogHelper.v(TAG, "*** -------------------------------------------------------------------------------- ***");
                LoadRadioTheaterTablesAsyncTask asyncTask = new LoadRadioTheaterTablesAsyncTask(activity, getCircleView(), dataSnapshot, LoadRadioTheaterTablesAsyncTask.LoadState.WRITERS);
                asyncTask.execute();
                LogHelper.v(TAG, "*** -------------------------------------------------------------------------------- ***");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogHelper.v(TAG, "onCancelled");
                activity.startAuthenticationActivity();
            }
        });
    }

    // next load the ACTORS tables
    public void loadActorsFromFirebase() {
        LogHelper.v(TAG, "loadActorsFromFirebase");
        final BaseActivity activity = this;
        // Attach a listener to read the data initially
        LogHelper.v(TAG, "*** FIREBASE REQUEST *** "+FIREBASE_ACTORS_URL);
        activity.getDatabase().child(FIREBASE_ACTORS_URL).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                LogHelper.v(TAG, "*** -------------------------------------------------------------------------------- ***");
                LoadRadioTheaterTablesAsyncTask asyncTask = new LoadRadioTheaterTablesAsyncTask(activity, getCircleView(), dataSnapshot, LoadRadioTheaterTablesAsyncTask.LoadState.ACTORS);
                asyncTask.execute();
                LogHelper.v(TAG, "*** -------------------------------------------------------------------------------- ***");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogHelper.v(TAG, "onCancelled");
                activity.startAuthenticationActivity();
            }
        });
    }

    // finally load the EPISODES tables
    public void loadEpisodesFromFirebase() {
        LogHelper.v(TAG, "loadEpisodesFromFirebase");
        final BaseActivity activity = this;
        // Attach a listener to read the data initially
        LogHelper.v(TAG, "*** FIREBASE REQUEST *** "+FIREBASE_SHOWS_URL);
        activity.getDatabase().child(FIREBASE_SHOWS_URL).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                LogHelper.v(TAG, "*** -------------------------------------------------------------------------------- ***");
                LoadRadioTheaterTablesAsyncTask asyncTask = new LoadRadioTheaterTablesAsyncTask(activity, getCircleView(), dataSnapshot, LoadRadioTheaterTablesAsyncTask.LoadState.EPISODES);
                asyncTask.execute();
                LogHelper.v(TAG, "*** -------------------------------------------------------------------------------- ***");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogHelper.v(TAG, "onCancelled");
                activity.startAuthenticationActivity();
            }
        });
    }

    public void loadAnyExistingFirebaseConfigurationValues(String deviceId) {
        LogHelper.v(TAG, "*** FIREBASE REQUEST *** - loadAnyExistingFirebaseConfigurationValues: deviceId="+deviceId);
        if (deviceId == null) {
            LogHelper.e(TAG, "ERROR: loadAnyExistingFirebaseConfigurationValues deviceId is null");
            return;
        }
        if (getEmail() == null) {
            LogHelper.e(TAG, "ERROR: loadAnyExistingFirebaseConfigurationValues email is null");
            return;
        }
        final BaseActivity activity = this;

        // Use a timer to determine if this deviceId is tracked inside Firebase
        sFoundFirebaseDeviceId = false;
        getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                LogHelper.v(TAG, "*** CHECK THE USER CONFIGURATION ***");
                updateTheUserConfiguration();
            }
        }, 90000); // allow a minute and a half

        // Attach a listener to read the data initially
        getDatabase().child("configuration").child("device").child(deviceId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                LogHelper.v(TAG, "*** -------------------------------------------------------------------------------- ***");
                // load the dataSnapshot info
                Object configurationObject = dataSnapshot.getValue();
                if (configurationObject == null) {
                    LogHelper.e(TAG, "the Firebase User Configuration (DataSnapshot) is null! deviceId="+getAdvertId()+", email="+getEmail());
                    //updateTheUserConfiguration();
                    return;
                }
                String configurationJSON = configurationObject.toString();
                sFoundFirebaseDeviceId = true;
                LogHelper.v(TAG, "===> Firebase configurationJSON="+configurationJSON);

                //#IFDEF 'PAID'
                //boolean paidVersion = true;
                //boolean purchaseAccess = true;
                //boolean purchaseNoads = true;
                //#ENDIF

                //#IFDEF 'TRIAL'
                boolean paidVersion = false;
                boolean purchaseAccess = false;
                boolean purchaseNoads = false;
                //#ENDIF

                mConfiguration = new ConfigurationContentValues();
                mConfiguration.putFieldUserEmail(getEmail());
                mConfiguration.putFieldUserName(getName() != null ? getName() : "unknown");
                mConfiguration.putFieldDeviceId(getAdvertId());

                mConfiguration.putFieldPaidVersion(paidVersion);
                mConfiguration.putFieldPurchaseAccess(purchaseAccess);
                mConfiguration.putFieldPurchaseNoads(purchaseNoads);

                int decodedListenCount = 0;
                //
                // since we only need a single value from the JSON use a regex pattern
                // EXAMPLE JSON: {firebase_user_name=colefklbBSTHPrRWGPt9BWYcCYS2, firebase_device_id=bf5874b5-0cd5-4457-9401-6fd384edb579, firebase_email=lee@harlie.com, firebase_authenticated=true, firebase_total_listen_count=19}
                //
                Pattern pattern = Pattern.compile(".*firebase_total_listen_count=([0-9]+).*");
                Matcher matcher = pattern.matcher((configurationJSON));
                if (matcher.find()) {
                    try {
                        decodedListenCount = Integer.parseInt(matcher.group(1));
                        LogHelper.v(TAG, "---> DECODED LISTEN_COUNT="+decodedListenCount);
                        checkUpdateWidget(activity, decodedListenCount);
                        getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                enableButtons();
                            }
                        });
                    }
                    catch (NumberFormatException e) {
                        LogHelper.e(TAG, "*** UNABLE TO DECODE LISTEN_COUNT FROM FIREBASE *** - NumberFormatException: configuration="+configurationJSON);
                    }
                }
                mConfiguration.putFieldTotalListenCount(decodedListenCount);
                LogHelper.v(TAG, "*** -------------------------------------------------------------------------------- ***");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogHelper.v(TAG, "loadAnyExistingFirebaseConfigurationValues - onCancelled");
                activity.startAuthenticationActivity();
            }
        });

    }

    protected void enableButtons() {
        LogHelper.v(TAG, "enableButtons");
    }

    public void checkUpdateWidget(Context context, int listenCount) {
        boolean updateWidget = true;

        //#IFDEF 'TRIAL'
        updateWidget = (listenCount < AutoplayActivity.MAX_TRIAL_EPISODES);
        //#ENDIF

        LogHelper.v(TAG, "--> checkUpdateWidget: listenCount="+listenCount+", updateWidget="+updateWidget);
        if (updateWidget && isLoadedOK()) {
            LogHelper.v(TAG, "*** TURN ON WIDGET FUNCTIONALITY ***");
            RadioTheaterWidgetService.setPaidVersion(context, updateWidget);
        }
    }

    // the Configuration exists initially in SQLite, then in Firebase also.
    // but if a new install happens for the account, we need to remember LISTEN_COUNT
    // and pull it from Firebase instead of using local data.
    private void updateTheUserConfiguration() {
        LogHelper.v(TAG, "*** updateTheUserConfiguration ***");
        ConfigurationContentValues configurationContent = null;
        ContentValues sqliteConfiguration = null;
        // 1) load local SQLite entry for deviceId
        ConfigurationCursor configurationCursor = SQLiteHelper.getCursorForConfigurationDevice(getAdvertId());
        if (configurationCursor != null) {
            LogHelper.v(TAG, "found existing SQLite user Configuration");
            configurationContent = SQLiteHelper.getConfigurationContentValues(configurationCursor);
            sqliteConfiguration = configurationContent.values();
            configurationCursor.close();
        }

        if (! sFoundFirebaseDeviceId) { // deviceId not in Firebase
            LogHelper.v(TAG, "device entry for Configuration doesn't exist in Firebase yet..");
            // 2) if local SQLite entry doesn't exist, create it
            if (sqliteConfiguration == null) { // no SQLite Configuration either
                LogHelper.v(TAG, "*** INITIALIZING USER *** - create local SQLite Configuration");
                mConfiguration = new ConfigurationContentValues();
                mConfiguration.putFieldUserEmail(getEmail());
                mConfiguration.putFieldUserName(getName());
                mConfiguration.putFieldDeviceId(getAdvertId());

                //#IFDEF 'PAID'
                //mConfiguration.putFieldAuthenticated(true);
                //mConfiguration.putFieldPurchaseAccess(true);
                //mConfiguration.putFieldPurchaseNoads(true);
                //RadioTheaterWidgetService.setPaidVersion(this, true);
                //#ENDIF

                //#IFDEF 'TRIAL'
                mConfiguration.putFieldAuthenticated(getEmail() != null);
                mConfiguration.putFieldPurchaseAccess(false);
                mConfiguration.putFieldPurchaseNoads(false);
                RadioTheaterWidgetService.setPaidVersion(this, false);
                //#ENDIF

                mConfiguration.putFieldTotalListenCount(0);
                SQLiteHelper.insertConfiguration(mConfiguration.values());
                // 3) update Firebase with the new deviceId entry
                updateFirebaseConfigurationValues(getAdvertId(), mConfiguration.values());
                return;
            }
        }

        ContentValues firebaseConfiguration = null;
        boolean updateFirebaseWithLocal = false;
        if (mConfiguration != null) {
            firebaseConfiguration = mConfiguration.values();
        }
        else if (sqliteConfiguration != null && configurationContent != null) {
            LogHelper.v(TAG, "have SQLite configuration, but not Firebase - so update Firebase with local");
            updateFirebaseWithLocal = true;
        }

        if (mConfiguration != null || updateFirebaseWithLocal) { // found Firebase Configuration
            LogHelper.v(TAG, "updating Firebase entry for Configuration.");
            // 4) merge and update local SQLite and Firebase
            boolean dirty = mergeConfiguratons(sqliteConfiguration, firebaseConfiguration);
            if (dirty) {
                sqliteConfiguration = mConfiguration.values();
                LogHelper.v(TAG, "DIRTY: sqliteConfiguration="+sqliteConfiguration.toString());
                SQLiteHelper.updateConfigurationValues(getAdvertId(), sqliteConfiguration);
                updateFirebaseConfigurationValues(getAdvertId(), mConfiguration.values());
            }
        }
        else {
            LogHelper.v(TAG, "unable to update Firebase Configuration!");
        }
    }

    private boolean mergeConfiguratons(ContentValues sqliteConfiguration, ContentValues firebaseConfiguration) {
        LogHelper.v(TAG, "mergeConfiguratons");
        boolean dirty = false;
        Boolean paidVersion = false;
        Boolean purchaseAccess = false;
        Boolean purchaseNoads = false;
        Boolean firebasePaidVersion = false;
        Boolean firebasePurchaseAccess = false;
        Boolean firebasePurchaseNoads = false;
        Long sqlite_listen_count = Long.valueOf(0);
        Long firebase_listen_count = Long.valueOf(0);

        //#IFDEF 'PAID'
        //paidVersion = true;
        //purchaseAccess = true;
        //purchaseNoads = true;
        //firebasePaidVersion = true;
        //firebasePurchaseAccess = true;
        //firebasePurchaseNoads = true;
        //RadioTheaterWidgetService.setPaidVersion(this, true);
        //#ENDIF

        try {
            if (sqliteConfiguration == null) {
                if (firebaseConfiguration == null) {
                    LogHelper.w(TAG, "both SQLite and Firebase have NO CONFIGURATION! - can't update");
                    return false;
                }
                LogHelper.v(TAG, "no local SQLite configuration exists on this device! - copy from Firebase");
                sqliteConfiguration = firebaseConfiguration;
                dirty = true;
            }
            mConfiguration = new ConfigurationContentValues();
        }
        catch (Exception e) {
            LogHelper.w(TAG, "SQLite: unable to get ConfigurationColumns.FIELD_TOTAL_LISTEN_COUNT, e="+e.getMessage());
        }

        //--------------------------------------------------------------------------------
        //#IFDEF 'TRIAL'
        try {
            paidVersion = sqliteConfiguration.getAsBoolean(ConfigurationColumns.FIELD_PAID_VERSION);
            if (!paidVersion && firebaseConfiguration != null) {
                firebasePaidVersion = firebaseConfiguration.getAsBoolean(ConfigurationColumns.FIELD_PAID_VERSION);
                if (firebasePaidVersion != null) {
                    paidVersion = firebasePaidVersion;
                }
            }
        }
        catch (Exception e) {
            LogHelper.w(TAG, "SQLite: unable to get ConfigurationColumns.FIELD_PAID_VERSION, e="+e.getMessage());
        }
        try {
            purchaseAccess = sqliteConfiguration.getAsBoolean(ConfigurationColumns.FIELD_PURCHASE_ACCESS);
            if (!purchaseAccess && firebaseConfiguration != null) {
                firebasePurchaseAccess = firebaseConfiguration.getAsBoolean(ConfigurationColumns.FIELD_PURCHASE_ACCESS);
                if (firebasePurchaseAccess != null) {
                    purchaseAccess = firebasePurchaseAccess;
                }
            }
        }
        catch (Exception e) {
            LogHelper.w(TAG, "SQLite: unable to get ConfigurationColumns.FIELD_PURCHASE_ACCESS, e="+e.getMessage());
        }
        try {
            purchaseNoads = sqliteConfiguration.getAsBoolean(ConfigurationColumns.FIELD_PURCHASE_NOADS);
            if (!purchaseNoads && firebaseConfiguration != null) {
                firebasePurchaseNoads = firebaseConfiguration.getAsBoolean(ConfigurationColumns.FIELD_PURCHASE_NOADS);
                if (firebasePurchaseNoads != null) {
                    purchaseNoads = firebasePurchaseNoads;
                }
            }
        }
        catch (Exception e) {
            LogHelper.w(TAG, "SQLite: unable to get ConfigurationColumns.FIELD_PURCHASE_NOADS, e="+e.getMessage());
        }
        //#ENDIF
        //--------------------------------------------------------------------------------

        try {
            sqlite_listen_count = sqliteConfiguration.getAsLong(ConfigurationColumns.FIELD_TOTAL_LISTEN_COUNT);
            if (sqlite_listen_count == null) {
                sqlite_listen_count = Long.valueOf(0);
            }
        }
        catch (Exception e) {
            LogHelper.w(TAG, "SQLite: unable to get SQLite ConfigurationColumns.FIELD_TOTAL_LISTEN_COUNT, e="+e.getMessage());
            sqlite_listen_count = Long.valueOf(0);
        }

        try {
            if (firebaseConfiguration != null) {
                firebase_listen_count = firebaseConfiguration.getAsLong(ConfigurationColumns.FIELD_TOTAL_LISTEN_COUNT);
                if (firebase_listen_count == null) {
                    firebase_listen_count = Long.valueOf(0);
                }
            }
        }
        catch (Exception e) {
            LogHelper.w(TAG, "SQLite: unable to get Firebase ConfigurationColumns.FIELD_TOTAL_LISTEN_COUNT, e="+e.getMessage());
            firebase_listen_count = Long.valueOf(0);
        }

        if (sqlite_listen_count > firebase_listen_count) {
            sAllListenCount = (int) sqlite_listen_count.longValue();
            LogHelper.v(TAG, "local listen count GREATER THAN firebase listen count: sAllListenCount="+ sAllListenCount);
            mConfiguration.putFieldTotalListenCount(sAllListenCount);
            dirty = true;
        }
        else {
            sAllListenCount = (int) firebase_listen_count.longValue();
            LogHelper.v(TAG, "firebase listen count GREATER THAN local listen count: sAllListenCount="+ sAllListenCount);
            mConfiguration.putFieldTotalListenCount(sAllListenCount);
            dirty = true;
        }

        if ((paidVersion != null && paidVersion)
                || (firebasePaidVersion != null && firebasePaidVersion))
        {
            mConfiguration.putFieldPaidVersion(true);
            dirty = true;
        }
        if ((purchaseAccess != null && purchaseAccess)
                || (firebasePurchaseAccess != null && firebasePurchaseAccess)
                || (paidVersion != null && paidVersion)
                || (firebasePaidVersion != null && firebasePaidVersion))
        {
            mConfiguration.putFieldPurchaseAccess(true);
            dirty = true;
        }
        if ((purchaseNoads != null && purchaseNoads)
                || (firebasePurchaseNoads != null && firebasePurchaseNoads)
                || (paidVersion != null && paidVersion)
                || (firebasePaidVersion != null && firebasePaidVersion))
        {
            mConfiguration.putFieldPurchaseNoads(true);
            dirty = true;
        }

        //#IFDEF 'TRIAL'
        boolean trialMode = (paidVersion != null && paidVersion) || (purchaseAccess != null && purchaseAccess) || isTrial();
        RadioTheaterWidgetService.setPaidVersion(this, trialMode);
        //#ENDIF

        return dirty;
    }

    // update Firebase User Account info
    public void updateFirebaseConfigurationValues(String deviceId, ContentValues configurationValues) {
        LogHelper.v(TAG, "updateFirebaseConfigurationValues");
        if (getDatabase() == null) {
            initializeFirebase();
        }
        String  email = getEmail();
        Boolean authenticated = email != null;
        Long total_listen_count = configurationValues.getAsLong(ConfigurationColumns.FIELD_TOTAL_LISTEN_COUNT);

        if (total_listen_count < sAllListenCount) {
            total_listen_count = Long.valueOf(sAllListenCount);
        }
        else {
            sAllListenCount = total_listen_count.intValue();
        }

        //#IFDEF 'PAID'
        //Boolean paid_version = true;
        //Boolean purchase_access = true;
        //Boolean purchase_noads = true;
        //#ENDIF

        //#IFDEF 'TRIAL'
        Boolean paid_version = configurationValues.getAsBoolean(ConfigurationColumns.FIELD_PAID_VERSION);
        Boolean purchase_access = configurationValues.getAsBoolean(ConfigurationColumns.FIELD_PURCHASE_ACCESS);
        Boolean purchase_noads = configurationValues.getAsBoolean(ConfigurationColumns.FIELD_PURCHASE_NOADS);
        //#ENDIF

        FirebaseConfiguration firebaseConfiguration = new FirebaseConfiguration(
                email,
                getUID(),
                deviceId,
                authenticated,
                paid_version,
                purchase_access,
                purchase_noads,
                total_listen_count
        );
        firebaseConfiguration.commit(getDatabase(), deviceId);
    }

    // update Firebase User Episode History and Auth
    public void updateFirebaseConfigEntryValues(String episode_number, ContentValues configEntryValues, long duration) {
        LogHelper.v(TAG, "updateFirebaseConfigEntryValues");
        if (getDatabase() == null) {
            initializeFirebase();
        }
        String  email = getEmail();
        Boolean episode_downloaded = configEntryValues.getAsBoolean(ConfigEpisodesColumns.FIELD_EPISODE_DOWNLOADED);
        Boolean episode_heard = configEntryValues.getAsBoolean(ConfigEpisodesColumns.FIELD_EPISODE_HEARD);
        Long    episode_count = configEntryValues.getAsLong(ConfigEpisodesColumns.FIELD_LISTEN_COUNT);

        //#IFDEF 'PAID'
        //Boolean purchased_access = true;
        //Boolean purchased_noads = true;
        //#ENDIF

        //#IFDEF 'TRIAL'
        Boolean purchased_access = configEntryValues.getAsBoolean(ConfigEpisodesColumns.FIELD_PURCHASED_ACCESS);
        Boolean purchased_noads = configEntryValues.getAsBoolean(ConfigEpisodesColumns.FIELD_PURCHASED_NOADS);
        //#ENDIF

        FirebaseConfigEpisode firebaseConfigEpisode = new FirebaseConfigEpisode(
                email,
                episode_number,
                purchased_access,
                purchased_noads,
                episode_downloaded,
                episode_heard,
                episode_count,
                duration
        );
        firebaseConfigEpisode.commit(getDatabase(), getUID());
    }

    /*
    private void deleteFirebaseDatabase() {
        LogHelper.v(TAG, "*** deleteFirebaseDatabase ***");
        mFirebase.child("radiomysterytheater/0").removeValue();
        mFirebase.child("radiomysterytheater/1").removeValue();
        mFirebase.child("radiomysterytheater/2").removeValue();
    }
    */

    //--------------------------------------------------------------------------------
    // CircleView related
    //--------------------------------------------------------------------------------

    public void initCircleView(CircleProgressView circleView, CircleViewHelper.CircleViewType what) {
        LogHelper.v(TAG, "initCircleView (CircleProgressView)");
        if (circleView == null) {
            LogHelper.w(TAG, "initCircleView: null circleView! - what="+what);
            return;
        }
        mCircleView = circleView; // needed to get around a problem with Fragments and findViewById
        if (what == CircleViewHelper.CircleViewType.CREATE_DATABASE) {
            sShowPercentUnit = true;
            LogHelper.v(TAG, "initCircleView: CREATE_DATABASE");
            getCircleView().setUnit("%");
            getCircleView().setUnitVisible(true);
            getCircleView().setTextMode(TextMode.PERCENT); // Shows current percent of the current value from the max value
        }
        else if (what == CircleViewHelper.CircleViewType.PLAY_EPISODE) {
            sShowPercentUnit = false;
            LogHelper.v(TAG, "initCircleView: PLAY_EPISODE");
            getCircleView().setUnit("");
            getCircleView().setUnitVisible(false);
            getCircleView().setTextMode(TextMode.TEXT);
        }
    }

    public void showCircleView() {
        LogHelper.v(TAG, "showCircleView");
        if (getCircleView() == null) {
            LogHelper.w(TAG, "showCircleView: CircleProgressView issue");
            return;
        }
        getCircleView().post(new Runnable() {
            @Override
            public void run() {

                getCircleView().setVisibility(View.VISIBLE);
                LogHelper.w(TAG, "showCircleView: CircleView VISIBLE");
                getCircleView().setAutoTextSize(true); // enable auto text size, previous values are overwritten
                getCircleView().setUnitScale(0.9f); // if you want the calculated text sizes to be bigger/smaller
                getCircleView().setTextScale(0.9f); // if you want the calculated text sizes to be bigger/smaller
                getCircleView().setTextColor(Color.RED);
                getCircleView().setText("Loading.."); //shows the given text in the circle view
                getCircleView().setTextMode(TextMode.TEXT); // Set text mode to text to show text
                getCircleView().spin(); // start spinning
                getCircleView().setShowTextWhileSpinning(true); // Show/hide text in spinning mode

                getCircleView().setOnAnimationStateChangedListener(

                        new AnimationStateChangedListener() {
                            @Override
                            public void onAnimationStateChanged(AnimationState _animationState) {
                                if (getCircleView() != null) {
                                    switch (_animationState) {
                                        case IDLE:
                                        case ANIMATING:
                                        case START_ANIMATING_AFTER_SPINNING:
                                            getCircleView().setTextMode(TextMode.PERCENT); // show percent if not spinning
                                            getCircleView().setUnitVisible(sShowPercentUnit);
                                            break;
                                        case SPINNING:
                                            getCircleView().setTextMode(TextMode.TEXT); // show text while spinning
                                            getCircleView().setUnitVisible(false);
                                        case END_SPINNING:
                                            break;
                                        case END_SPINNING_START_ANIMATING:
                                            break;

                                    }
                                }
                            }
                        }

                );

            }
        });
    }

    public void setCircleViewMaximum(float value) {
        LogHelper.w(TAG, "setCircleViewMaximum: MAX value=" + value);
        if (getCircleView() != null) {
            getCircleView().setMaxValue(value);
            getCircleView().setValue(0);
        }
    }

    public void setCircleViewValue(float value) {
        if (value != 0) {
            LoadingAsyncTask.sLoadingNow = false;
            if (getCircleView() != null) {
                getCircleView().setValue(value);
                //LogHelper.w(TAG, "setCircleViewValue: value=" + value);
                if (value == getCircleView().getMaxValue()) {
                    hideCircleView();
                }
            }
        }
    }

    public void hideCircleView() {
        if (getCircleView() != null) {
            getCircleView().stopSpinning();
            getCircleView().setVisibility(View.INVISIBLE);
            LogHelper.w(TAG, "hideCircleView: CircleView HIDDEN");
        }
    }

    //--------------------------------------------------------------------------------
    // Getters and Setters
    //--------------------------------------------------------------------------------

    public static String getName() {
        if (sName == null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(RadioTheaterApplication.getRadioTheaterApplicationContext());
            sName = sharedPreferences.getString("userName", "");
            if (sName.length() == 0) {
                sName = null;
            }
        }
        return sName;
    }

    public static void setName(String name) {
        if (name != null && name.length() > 0) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(RadioTheaterApplication.getRadioTheaterApplicationContext());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("userName", name);
            editor.apply();
        }
        sName = name;
    }

    public static String getEmail() {
        if (sEmail == null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(RadioTheaterApplication.getRadioTheaterApplicationContext());
            sEmail = sharedPreferences.getString("userEmail", "");
            if (sEmail.length() == 0) {
                sEmail = null;
            }
        }
        return sEmail;
    }

    public static void setEmail(String email) {
        if (email != null && email.length() > 0) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(RadioTheaterApplication.getRadioTheaterApplicationContext());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("userEmail", email);
            editor.apply();
        }
        sEmail = email;
    }

    public static String getPass() {
        if (sPassword == null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(RadioTheaterApplication.getRadioTheaterApplicationContext());
            sPassword = sharedPreferences.getString("userPass", "");
            if (sPassword.length() == 0) {
                sPassword = null;
            }
        }
        return sPassword;
    }

    public static void setPass(String password) {
        if (password != null && password.length() > 0) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(RadioTheaterApplication.getRadioTheaterApplicationContext());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("userPass", password);
            editor.apply();
        }
        sPassword = password;
    }

    public static String getUID() {
        if (sUID == null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(RadioTheaterApplication.getRadioTheaterApplicationContext());
            sUID = sharedPreferences.getString("userUID", "");
            if (sUID.length() == 0) {
                sUID = null;
            }
        }
        return sUID;
    }

    public static void setUID(String uid) {
        if (uid != null && uid.length() > 0) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(RadioTheaterApplication.getRadioTheaterApplicationContext());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("userUID", uid);
            editor.apply();
        }
        sUID = uid;
    }

    public static String getAdvertId() {
        if (sAdvId == null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(RadioTheaterApplication.getRadioTheaterApplicationContext());
            String advertID = sharedPreferences.getString("advertID", sAdvId); // passing null object for default
            if (advertID != null && advertID.length() != 0) {
                sAdvId = advertID;
            }
        }
        LogHelper.v(TAG, "get sAdvId="+ sAdvId);
        return sAdvId;
    }

    public static void setAdvertId(String advId) {
        if (advId != null && advId.length() > 0) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(RadioTheaterApplication.getRadioTheaterApplicationContext());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("advertID", advId);
            editor.apply();
        }
        LogHelper.v(TAG, "set sAdvId="+advId);
        sAdvId = advId;
    }

    public static boolean isTrial() {
        boolean trial = true;
        //#IFDEF 'TRIAL'
        trial = (sAllListenCount < MAX_TRIAL_EPISODES);
        //#ENDIF
        return trial;
    }

    public static boolean isPurchased() {
        return sPurchased;
    }

    public static String getAirdate() {
        return sAirdate;
    }

    public static String getEpisodeTitle() {

        return sEpisodeTitle;
    }

    public static long getEpisodeNumber() {
        return sEpisodeNumber;
    }

    public static void setEpisodeNumber(long episodeNumber) {
        sEpisodeNumber = episodeNumber;
    }

    public static String getEpisodeDescription() {
        return sEpisodeDescription;
    }

    public static String getEpisodeWeblinkUrl() {
        return sEpisodeWeblinkUrl;
    }

    public static String getEpisodeDownloadUrl() {
        return sEpisodeDownloadUrl;
    }

    public static boolean isLoadedOK() {
        return sLoadedOK;
    }

    public Handler getHandler() {
        return mHandler;
    }

    public FirebaseAuth getAuth() {
        return mAuth;
    }

    public Firebase getFirebase() {
        return mFirebase;
    }

    public DatabaseReference getDatabase() {
        return mDatabase;
    }

    public CircleProgressView getCircleView() {
        return mCircleView;
    }

    public View getRootView() {
        return mRootView;
    }

    //--------------------------------------------------------------------------------
    // Autoplay related
    //--------------------------------------------------------------------------------

    private static final String KEY_HANDLE_ROTATION_EVENT   = "rotationEvent";
    private static final String KEY_LOADING_SCREEN_ENABLED  = "loadingEnabled";
    private static final String KEY_BEGIN_LOADING           = "beginLoading";
    private static final String KEY_AUTOPLAY_NEXT_NOW       = "autoplayNextNow";
    private static final String KEY_ENABLE_FAB              = "enableFAB";
    private static final String KEY_WAIT_FOR_MEDIA          = "waitForMedia";

    private static final String KEY_EPISODE                 = "episodeNumber";
    private static final String KEY_PURCHASED               = "purchased";
    private static final String KEY_NOADS                   = "noads";
    private static final String KEY_DOWNLOADED              = "downloaded";
    private static final String KEY_HEARD                   = "heard";
    private static final String KEY_DURATION                = "duration";
    private static final String KEY_REAL_DURATION           = "realDuration";
    private static final String KEY_CURRENT_POSITION        = "position";
    private static final String KEY_LOADED_OK               = "loaded_ok";
    private static final String KEY_AIRDATE                 = "airdate";
    private static final String KEY_TITLE                   = "title";
    private static final String KEY_DESCRIPTION             = "description";
    private static final String KEY_WEBLINK                 = "weblink";
    private static final String KEY_DOWNLOAD_URL            = "downloadUrl";

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        LogHelper.v(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
        savePlayInfoToBundle(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        LogHelper.v(TAG, "onRestoreInstanceState");
        super.onRestoreInstanceState(savedInstanceState);
        this.restorePlayInfoFromBundle(savedInstanceState);
    }

    public void savePlayInfoToBundle(Bundle playInfoBundle) {
        LogHelper.v(TAG, "savePlayInfoToBundle");

        playInfoBundle.putBoolean(KEY_HANDLE_ROTATION_EVENT   , sHandleRotationEvent);
        playInfoBundle.putBoolean(KEY_LOADING_SCREEN_ENABLED  , sLoadingScreenEnabled);
        playInfoBundle.putBoolean(KEY_BEGIN_LOADING           , sBeginLoading);
        playInfoBundle.putBoolean(KEY_AUTOPLAY_NEXT_NOW       , sAutoplayNextNow);
        playInfoBundle.putBoolean(KEY_ENABLE_FAB              , sEnableFAB);
        playInfoBundle.putBoolean(KEY_WAIT_FOR_MEDIA          , sWaitForMedia);

        LogHelper.v(TAG, "APP-STATE (Autoplay): saveAutoplayInfoToBundle: sHandleRotationEvent="+sHandleRotationEvent
                +", sLoadingScreenEnabled="+sLoadingScreenEnabled+", sBeginLoading="+sBeginLoading
                +", sAutoplayNextNow="+sAutoplayNextNow+", sEnableFAB="+sEnableFAB+", sWaitForMedia="+sWaitForMedia+" <<<=========");

        playInfoBundle.putLong(KEY_EPISODE, getEpisodeNumber());
        playInfoBundle.putBoolean(KEY_PURCHASED, sPurchased);
        playInfoBundle.putBoolean(KEY_NOADS, sNoAdsForShow);
        playInfoBundle.putBoolean(KEY_DOWNLOADED, sDownloaded);
        playInfoBundle.putBoolean(KEY_HEARD, sEpisodeHeard);
        playInfoBundle.putLong(KEY_DURATION, mDuration);
        playInfoBundle.putBoolean(KEY_REAL_DURATION, sHaveRealDuration);
        playInfoBundle.putLong(KEY_CURRENT_POSITION, mCurrentPosition);
        playInfoBundle.putBoolean(KEY_LOADED_OK, sLoadedOK);
        playInfoBundle.putString(KEY_AIRDATE, getAirdate());
        playInfoBundle.putString(KEY_TITLE, getEpisodeTitle());
        playInfoBundle.putString(KEY_DESCRIPTION, getEpisodeDescription());
        playInfoBundle.putString(KEY_WEBLINK, getEpisodeWeblinkUrl());
        playInfoBundle.putString(KEY_DOWNLOAD_URL, getEpisodeDownloadUrl());

        LogHelper.v(TAG, "APP-STATE (Base): savePlayInfoToBundle: sEpisodeNumber="+ sEpisodeNumber
                +", sPurchased="+sPurchased+", sNoAdsForShow="+sNoAdsForShow+", sDownloaded="+sDownloaded
                +", sEpisodeHeard="+sEpisodeHeard+", mDuration="+mDuration+", sHaveRealDuration="+sHaveRealDuration
                +", mCurrentPosition="+mCurrentPosition+", sLoadedOK="+sLoadedOK
                +", sAirdate="+ sAirdate +", sEpisodeTitle="+ sEpisodeTitle +", sEpisodeDescription="+ sEpisodeDescription
                +", sEpisodeWeblinkUrl="+ sEpisodeWeblinkUrl +", sEpisodeDownloadUrl="+ sEpisodeDownloadUrl +" <<<=========");
    }

    protected void restorePlayInfoFromBundle(Bundle playInfoBundle) {
        if (!sOnRestoreInstanceComplete) {
            sOnRestoreInstanceComplete = true;

            sHandleRotationEvent = playInfoBundle.getBoolean(KEY_HANDLE_ROTATION_EVENT);
            sLoadingScreenEnabled = playInfoBundle.getBoolean(KEY_LOADING_SCREEN_ENABLED);
            sBeginLoading = playInfoBundle.getBoolean(KEY_BEGIN_LOADING);
            sAutoplayNextNow = playInfoBundle.getBoolean(KEY_AUTOPLAY_NEXT_NOW);
            sEnableFAB = playInfoBundle.getBoolean(KEY_ENABLE_FAB);
            sWaitForMedia = playInfoBundle.getBoolean(KEY_WAIT_FOR_MEDIA);

            LogHelper.v(TAG, "APP-STATE (Autoplay): restoreAutoplayInfoFromBundle: sHandleRotationEvent="+sHandleRotationEvent
                    +", sLoadingScreenEnabled="+sLoadingScreenEnabled+", sBeginLoading="+sBeginLoading
                    +", sAutoplayNextNow="+sAutoplayNextNow+", sEnableFAB="+sEnableFAB+", sWaitForMedia="+sWaitForMedia+" <<<=========");

            sEpisodeNumber = playInfoBundle.getLong(KEY_EPISODE);
            sPurchased = playInfoBundle.getBoolean(KEY_PURCHASED);
            sNoAdsForShow = playInfoBundle.getBoolean(KEY_NOADS);
            sDownloaded = playInfoBundle.getBoolean(KEY_DOWNLOADED);
            sEpisodeHeard = playInfoBundle.getBoolean(KEY_HEARD);
            mDuration = playInfoBundle.getLong(KEY_DURATION);
            sHaveRealDuration = playInfoBundle.getBoolean(KEY_REAL_DURATION);
            mCurrentPosition = playInfoBundle.getLong(KEY_CURRENT_POSITION);
            sLoadedOK = playInfoBundle.getBoolean(KEY_LOADED_OK);
            sAirdate = playInfoBundle.getString(KEY_AIRDATE);
            sEpisodeTitle = playInfoBundle.getString(KEY_TITLE);
            sEpisodeDescription = playInfoBundle.getString(KEY_DESCRIPTION);
            sEpisodeWeblinkUrl = playInfoBundle.getString(KEY_WEBLINK);
            sEpisodeDownloadUrl = playInfoBundle.getString(KEY_DOWNLOAD_URL);

            LogHelper.v(TAG, "APP-STATE (Base): restorePlayInfoFromBundle: sEpisodeNumber="+ sEpisodeNumber
                    +", sPurchased="+sPurchased+", sNoAdsForShow="+sNoAdsForShow+", sDownloaded="+sDownloaded
                    +", sEpisodeHeard="+sEpisodeHeard+", mDuration="+mDuration+", sHaveRealDuration="+sHaveRealDuration
                    +", mCurrentPosition="+mCurrentPosition+", sLoadedOK="+sLoadedOK
                    +", sAirdate="+ sAirdate +", sEpisodeTitle="+ sEpisodeTitle +", sEpisodeDescription="+ sEpisodeDescription
                    +", sEpisodeWeblinkUrl="+ sEpisodeWeblinkUrl +", sEpisodeDownloadUrl="+ sEpisodeDownloadUrl +" <<<=========");

            showCurrentInfo();
        }
    }

    protected void showCurrentInfo() {
        LogHelper.v(TAG, "===> EPISODE INFO"
                + ": sEpisodeTitle=" + getEpisodeTitle()
                + ": sEpisodeNumber=" + getEpisodeNumber()
                + ": sAirdate=" + getAirdate()
                + ": sEpisodeDescription=" + getEpisodeDescription()
                + ": sEpisodeWeblinkUrl=" + getEpisodeWeblinkUrl()
                + ": sEpisodeDownloadUrl=" + getEpisodeDownloadUrl()
                + ", sPurchased=" + sPurchased
                + ", sNoAdsForShow=" + sNoAdsForShow
                + ", sDownloaded=" + sDownloaded
                + ", sEpisodeHeard=" + sEpisodeHeard);
    }

    public void markEpisodeAsHeardAndIncrementPlayCount(long episodeNumber, String episodeIndex, long duration) {
        LogHelper.v(TAG, "markEpisodeAsHeardAndIncrementPlayCount: episodeNumber="+episodeNumber+", episodeIndex="+episodeIndex+", duration="+duration);
        boolean matchError = false;
        if (! String.valueOf(episodeNumber).equals(episodeIndex)) {
            LogHelper.e(TAG, "markEpisodeAsHeardAndIncrementPlayCount: The episodeNumber="+episodeNumber+" and episodeIndex "+episodeIndex+" DONT MATCH");
            matchError = true;
        }

        // UPDATE SQLITE - mark SQLite config episode as "HEARD" and increment "PLAY COUNT" - Also send record to Firebase
        ConfigEpisodesContentValues existing = SQLiteHelper.getConfigForEpisode(episodeIndex);
        long listenCount = 0;
        if (existing != null && existing.values() != null && existing.values().size() != 0) {
            ContentValues configEpisode = existing.values();
            configEpisode.put(ConfigEpisodesEntry.FIELD_EPISODE_HEARD, true);

            listenCount = configEpisode.getAsLong(ConfigEpisodesColumns.FIELD_LISTEN_COUNT);
            ++listenCount;

            configEpisode.put(ConfigEpisodesEntry.FIELD_LISTEN_COUNT, listenCount);
            SQLiteHelper.updateConfigEntryValues(episodeIndex, configEpisode);
            updateFirebaseConfigEntryValues(episodeIndex, configEpisode, duration);
            LogHelper.d(TAG, "markEpisodeAsHeardAndIncrementPlayCount: new LISTEN-COUNT="+listenCount+" for Episode "+episodeIndex+(matchError ? " *** MATCH ERROR EPISODE="+episodeNumber : ""));
        }
        else {
            LogHelper.e(TAG, "*** SQLITE FAILURE - unable to getConfigForEpisode="+episodeIndex+(matchError ? " *** MATCH ERROR EPISODE="+episodeNumber : ""));
        }

        // UPDATE SQLITE - mark SQLite configuration as "HEARD" and increment "PLAY COUNT"
        ConfigurationCursor configurationCursor = SQLiteHelper.getCursorForConfigurationDevice(getAdvertId());
        ConfigurationContentValues existingConfiguration = null;
        if (configurationCursor != null) {
            existingConfiguration = SQLiteHelper.getConfigurationContentValues(configurationCursor);
            configurationCursor.close();
        }
        long total_listen_count = 0;
        if (existingConfiguration != null && existingConfiguration.values() != null && existingConfiguration.values().size() != 0) {
            ContentValues configurationValues = existingConfiguration.values();

            total_listen_count = configurationValues.getAsLong(ConfigurationColumns.FIELD_TOTAL_LISTEN_COUNT);
            ++total_listen_count;

            if (total_listen_count < sAllListenCount) {
                total_listen_count = sAllListenCount;
            }
            else {
                sAllListenCount = (int) total_listen_count;
            }

            configurationValues.put(ConfigurationEntry.FIELD_TOTAL_LISTEN_COUNT, Long.valueOf(sAllListenCount));
            SQLiteHelper.updateConfigurationValues(getAdvertId(), configurationValues);
            updateFirebaseConfigurationValues(getAdvertId(), configurationValues);
            LogHelper.d(TAG, "---> markEpisodeAsHeardAndIncrementPlayCount: new ALL-LISTEN-COUNT="+ total_listen_count +" <---");
        }
        else {
            LogHelper.e(TAG, "*** SQLITE FAILURE - unable to getConfiguration email="+getEmail()+", deviceId="+getAdvertId());
        }

        // send Analytics record to Firebase for Episode+Heard+Count
        trackWithFirebaseAnalytics(episodeIndex, duration, "mark HEARD + playcount="+listenCount+" + allcount="+ total_listen_count);
    }

    public void markEpisodeAs_NOT_Heard(long episodeNumber, String episodeIndex, long duration) {
        LogHelper.v(TAG, "markEpisodeAs_NOT_Heard: episodeNumber="+episodeNumber+", episodeIndex="+episodeIndex+", duration="+duration);
        boolean matchError = false;
        if (! String.valueOf(episodeNumber).equals(episodeIndex)) {
            LogHelper.e(TAG, "markEpisodeAs_NOT_Heard: The episodeNumber="+episodeNumber+" and episodeIndex "+episodeIndex+" DONT MATCH");
            matchError = true;
        }

        // UPDATE SQLITE - mark SQLite config episode as "NOT HEARD" - Also send record to Firebase
        ConfigEpisodesContentValues existing = SQLiteHelper.getConfigForEpisode(episodeIndex);
        if (existing != null && existing.values() != null && existing.values().size() != 0) {
            ContentValues configEpisode = existing.values();
            configEpisode.put(ConfigEpisodesEntry.FIELD_EPISODE_HEARD, false);
            SQLiteHelper.updateConfigEntryValues(episodeIndex, configEpisode);
            updateFirebaseConfigEntryValues(episodeIndex, configEpisode, duration);
            LogHelper.d(TAG, "markEpisodeAs_NOT_Heard: for Episode "+episodeIndex+(matchError ? " *** MATCH ERROR EPISODE="+episodeNumber : ""));
        }
        else {
            LogHelper.e(TAG, "*** SQLITE FAILURE - unable to getConfigForEpisode="+episodeIndex+(matchError ? " *** MATCH ERROR EPISODE="+episodeNumber : ""));
        }

        // send Analytics record to Firebase for Episode+Heard+Count
        trackWithFirebaseAnalytics(episodeIndex, duration, "mark NOT HEARD");
    }

    protected void trackWithFirebaseAnalytics(String episodeIndex, long duration, String comment) {
        if (mFirebaseAnalytics != null && episodeIndex != null && comment != null) {
            LogHelper.v(TAG, "ANALYTICS: trackWithFirebaseAnalytics: episode="+episodeIndex+", duration="+duration+", comment="+comment);
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, episodeIndex);
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, sEpisodeTitle);
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "audio");
            bundle.putString("episode", episodeIndex);

            //#IFDEF 'PAID'
            //bundle.putString("user_action", "PAID: "+comment);
            //#ENDIF

            //#IFDEF 'TRIAL'
            bundle.putString("user_action", "TRIAL: "+comment);
            //#ENDIF

            bundle.putLong("listen_duration", duration);
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            logToFirebase(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        }
    }

    protected void trackWithFirebaseAnalytics(String event, String email, String comment) {
        if (mFirebaseAnalytics != null && event != null && email != null && comment != null) {
            LogHelper.v(TAG, "ANALYTICS: trackWithFirebaseAnalytics: event="+event+", email="+email);
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "event");
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, event);
            bundle.putString(FirebaseAnalytics.Param.ORIGIN, email);

            //#IFDEF 'PAID'
            //bundle.putString("user_action", "PAID: "+comment);
            //#ENDIF

            //#IFDEF 'TRIAL'
            bundle.putString("user_action", "TRIAL: "+comment);
            //#ENDIF

            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            logToFirebase(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        }
    }

    protected void trackSignupAttemptWithFirebaseAnalytics(String signup_using) {
        if (mFirebaseAnalytics != null) {
            LogHelper.v(TAG, "ANALYTICS: trackSignupAttemptWithFirebaseAnalytics signup_using="+signup_using);
            Bundle bundle = new Bundle();
            bundle.putString("using", signup_using);
            mFirebaseAnalytics.logEvent("signup_method", bundle);
        }
    }

    protected void trackSignupWithFirebaseAnalytics() {
        if (mFirebaseAnalytics != null) {
            LogHelper.v(TAG, "ANALYTICS: trackSignupWithFirebaseAnalytics email="+getEmail());
            Bundle bundle = new Bundle();
            bundle.putString("user", getEmail());
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, bundle);
            logToFirebase(FirebaseAnalytics.Event.SIGN_UP, bundle);
        }
    }

    protected void trackLoginWithFirebaseAnalytics() {
        if (mFirebaseAnalytics != null) {
            LogHelper.v(TAG, "ANALYTICS: trackLoginWithFirebaseAnalytics email="+getEmail());
            Bundle bundle = new Bundle();
            bundle.putString("user", getEmail());
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle);
            logToFirebase(FirebaseAnalytics.Event.LOGIN, bundle);
        }
    }

    protected void trackSearchWithFirebaseAnalytics() {
        if (mFirebaseAnalytics != null) {
            LogHelper.v(TAG, "ANALYTICS: trackSearchWithFirebaseAnalytics email="+getEmail());
            Bundle bundle = new Bundle();
            bundle.putString("user", getEmail());
            mFirebaseAnalytics.logEvent("do_search", bundle);
            logToFirebase("do_search", bundle);
        }
    }

    protected void trackSettingsWithFirebaseAnalytics() {
        if (mFirebaseAnalytics != null) {
            LogHelper.v(TAG, "ANALYTICS: trackSettingsWithFirebaseAnalytics email="+getEmail());
            Bundle bundle = new Bundle();
            bundle.putString("user", getEmail());
            mFirebaseAnalytics.logEvent("do_settings", bundle);
            logToFirebase("do_settings", bundle);
        }
    }

    protected void trackAboutWithFirebaseAnalytics() {
        if (mFirebaseAnalytics != null) {
            LogHelper.v(TAG, "ANALYTICS: trackAboutWithFirebaseAnalytics email="+getEmail());
            Bundle bundle = new Bundle();
            bundle.putString("user", getEmail());
            mFirebaseAnalytics.logEvent("do_about", bundle);
            logToFirebase("do_about", bundle);
        }
    }

    protected void logToFirebase(String action, Bundle bundle) {
        if (getEmail() == null) { // ensure the Firebase user is logged-in
            LogHelper.w(TAG, "unable to logToFirebase - Firebase user is not logged in!");
            return;
        }
        String detail = "";
        if (bundle != null) {
            detail = "{";
            for (String key : bundle.keySet()) {
                detail += " " + key + " => " + bundle.get(key) + ";";
            }
            detail += " }";
        }
        String logValue = action + " " + detail;
        LogHelper.v(TAG, "ANALYTICS: logToFirebase - logValue="+logValue);
        final String key = date_key();
        if (getDatabase() != null && key != null) {
            LogHelper.v(TAG, "commit: key=" + key);
            getDatabase().child("log").child(getUID()).child(key).setValue(logValue, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        LogHelper.v(TAG, "logToFirebase: onComplete - databaseError=" + databaseError.getMessage());
                    }
                    if (databaseReference != null) {
                        LogHelper.v(TAG, "logToFirebase: onComplete - databaseReference key=" + databaseReference.getKey());
                    }
                    if (databaseError == null && databaseReference != null) {
                        LogHelper.v(TAG, "logToFirebase: key="+key+" - SUCCESS!");
                    }
                }
            });
        }
    }

    public String date_key() {
        return new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault()).format(new java.util.Date());
    }

}
