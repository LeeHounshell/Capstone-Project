package com.harlie.radiotheater.radiomysterytheater;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.ContentValues;
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
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.harlie.radiotheater.radiomysterytheater.data.RadioTheaterHelper;
import com.harlie.radiotheater.radiomysterytheater.data.configepisodes.ConfigEpisodesContentValues;
import com.harlie.radiotheater.radiomysterytheater.data.configuration.ConfigurationColumns;
import com.harlie.radiotheater.radiomysterytheater.data_helper.LoadRadioTheaterTablesAsyncTask;
import com.harlie.radiotheater.radiomysterytheater.data_helper.LoadingAsyncTask;
import com.harlie.radiotheater.radiomysterytheater.data_helper.DataHelper;
import com.harlie.radiotheater.radiomysterytheater.playback.LocalPlayback;
import com.harlie.radiotheater.radiomysterytheater.utils.CheckPlayStore;
import com.harlie.radiotheater.radiomysterytheater.utils.CircleViewHelper;
import com.harlie.radiotheater.radiomysterytheater.utils.LogHelper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

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

    protected static final boolean COPY_PREBUILT_SQLITE_DATABASE = true;

    protected static final int MIN_EMAIL_LENGTH = 5;
    protected static final int MIN_PASSWORD_LENGTH = 6;
    protected static final int TOTAL_SIZE_TO_COPY_IN_BYTES = 1347584;
    protected static final int ANIMATION_DELAY = 100;
    protected static final String FIREBASE_WRITERS_URL = "radiomysterytheater/0/writers";
    protected static final String FIREBASE_ACTORS_URL = "radiomysterytheater/1/actors";
    protected static final String FIREBASE_SHOWS_URL = "radiomysterytheater/2/shows";
    protected static final boolean COPY_PACKAGED_SQLITE_DATABASE = true;

    protected static ShareActionProvider sShareActionProvider;
    protected static Intent sShareIntent;

    protected static volatile boolean sOnRestoreInstanceComplete;
    protected static volatile boolean sOkLoadFirebaseConfiguration;
    protected static volatile boolean sShowPercentUnit;

    // need to save these across Activities
    protected static volatile boolean sHandleRotationEvent;
    protected static volatile boolean sLoadingScreenEnabled;
    protected static volatile boolean sBeginLoading;
    protected static volatile boolean sEnableFAB;
    protected static volatile boolean sWaitForMedia;

    // need to save these across Activities
    protected static volatile boolean sHaveRealDuration;

    private static int sCount;

    public static volatile boolean sProgressViewSpinning;

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
            sEnableFAB = false;
            sWaitForMedia = false;

            DataHelper.setPurchased(false);
            DataHelper.setNoAdsForShow(false);
            DataHelper.setDownloaded(false);
            DataHelper.setEpisodeHeard(false);
            DataHelper.setLoadedOK(false);
            sHaveRealDuration = false;

            Bundle playInfo = getIntent().getExtras();
            if (playInfo != null) {
                restorePlayInfoFromBundle(playInfo);
            }
        }

        mRootView = findViewById(android.R.id.content);
        mHandler = new Handler();

        DataHelper.initializeFirebase();

        final BaseActivity baseActivity = this;

        // capture the advertising id and save it
        if (! DataHelper.isLoadedOK()) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    if (DataHelper.getEmail() != null) {
                        try {
                            AdvertisingIdClient.Info adInfo = AdvertisingIdClient.getAdvertisingIdInfo(baseActivity);
                            DataHelper.setAdvertId((adInfo != null) ? adInfo.getId() : null);
                        }
                        catch (IOException | GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException exception) {
                            LogHelper.e(TAG, "*** UNABLE TO LOAD ADVERT ID ***"); // and it is needed for my Firebase key...
                        }
                        finally {
                            if (DataHelper.getAdvId() == null) {
                                DataHelper.setAdvId(DataHelper.getAdvertId()); // from shared pref
                            }
                            if (DataHelper.getAdvId() != null) {
                                DataHelper.setAdvertId(DataHelper.getAdvId()); // now also in shared pref
                                if (sOkLoadFirebaseConfiguration) {
                                    DataHelper.loadAnyExistingFirebaseConfigurationValues(baseActivity);
                                }
                            }
                        }
                    }
                }
            });

            setupWindowAnimations();
        }
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
    public boolean onCreateOptionsMenu(Menu menu) {
        LogHelper.v(TAG, "onCreateOptionsMenu");
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public void onDestroy() {
        LogHelper.v(TAG, "onDestroy");
        super.onDestroy();
        sShareActionProvider = null;
        sShareIntent = null;
        sOnRestoreInstanceComplete = false;
        sOkLoadFirebaseConfiguration = false;
        sShowPercentUnit = false;
        sHandleRotationEvent = false;
        sLoadingScreenEnabled = false;
        sBeginLoading = false;
        sEnableFAB = false;
        sWaitForMedia = false;
        sHaveRealDuration = false;
        sProgressViewSpinning = false;
        sCount = 0;
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
            authenticateRadioMysteryTheaterFirebaseAccount(DataHelper.getEmail(), DataHelper.getPass());
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
        if (DataHelper.getFirebaseAuth() != null && DataHelper.getEmail() != null && DataHelper.getPass() != null && isValid(email, pass)) {
            LogHelper.v(TAG, "authenticateRadioMysteryTheaterFirebaseAccount: GOOD");
            final BaseActivity activity = this;
            DataHelper.getFirebaseAuth().createUserWithEmailAndPassword(email, pass)
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
                                String uid;
                                try {
                                    uid = DataHelper.getFirebaseAuth().getCurrentUser().getUid();
                                }
                                catch (NullPointerException e) {
                                    LogHelper.e(TAG, "UNABLE TO GET FIREBASE USER ID!");
                                    userLoginFailed();
                                    startAuthenticationActivity();
                                    return;
                                }
                                DataHelper.setEmail(email);
                                DataHelper.setPass(pass);
                                DataHelper.setUID(uid);
                                DataHelper.trackSignupWithFirebaseAnalytics();
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
            LogHelper.w(TAG, "authenticateRadioMysteryTheaterFirebaseAccount: problem authenticating - FirebaseAuth="+ DataHelper.getFirebaseAuth()
                    +", email="+ DataHelper.getEmail()
                    +", pass="+ DataHelper.getPass()
                    +", isValid="+isValid(email, pass));
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
            try {
                //noinspection ConstantConditions
                LogHelper.v(TAG, "*** authentication failed *** reason=" + task.getException().getLocalizedMessage());
            }
            catch (NullPointerException e) { }
            String message = getResources().getString(R.string.auth_fail);
            Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
            startAuthenticationActivity();
        }
        return success;
    }

    protected void userLoginSuccess() {
        LogHelper.v(TAG, "userLoginSuccess");
        if (! DataHelper.isLoadedOK()) {
            String message = getResources().getString(R.string.successful);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
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
        authenticationIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        authenticationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        Bundle playInfo = new Bundle();
        savePlayInfoToBundle(playInfo);
        authenticationIntent.putExtras(playInfo);
        Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(this, android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
        LogHelper.v(TAG, "STARTACTIVITY: AuthenticationActivity.class");
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
                    loadSqliteDatabase(COPY_PREBUILT_SQLITE_DATABASE); // copy ok? (or if not ok then load via Firebase)
                    Looper.loop();
                }
            }).start();
        }
        else {
            LogHelper.v(TAG, "*** READY TO START RADIO MYSTERY THEATER ***");
            if (DataHelper.getEmail() != null) {
                userLoginSuccess();
            }
            // save authentication to Shared Prefs
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(RadioTheaterApplication.getRadioTheaterApplicationContext());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("authentication", DataHelper.getEmail());
            editor.putString("userUID", DataHelper.getUID());
            editor.apply();

            Intent autoplayIntent = new Intent(this, AutoplayActivity.class);
            // close existing activity stack regardless of what's in there and create new root
            autoplayIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
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
            LogHelper.v(TAG, "STARTACTIVITY: AutoplayActivity.class");
            startActivity(autoplayIntent, bundle);
            finish();
        }
    }

    public void playNow() {
        String episodeNumber = DataHelper.getEpisodeNumberString();
        LogHelper.v(TAG, "playNow: episodeNumber="+episodeNumber);
        if (LocalPlayback.getCurrentState() == PlaybackStateCompat.STATE_PLAYING) {
            LogHelper.v(TAG, "*** STOP PLAYING CURRENT EPISODE BEFORE STARING WITH A FRESH ONE ***");
            RadioControlIntentService.startActionStop(this, "DETAIL", episodeNumber, DataHelper.getEpisodeDownloadUrl());
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
        //autoplayIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        //
        // to get around this problem, I have over-ridden onBackPressed in AutoplayActivity so that it clears the back stack before exiting.

        savePlayInfoToBundle(playInfo);
        autoplayIntent.putExtras(playInfo);
        autoplayIntent.putExtra("PLAY_NOW", String.valueOf(episodeNumber));
        LogHelper.v(TAG, "STARTACTIVITY: AutoplayActivity.class");
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
        //noinspection ConstantConditions
        if (output == null) {
            //throw new Exception("null output stream for database="+outFileName);
            return "null output stream for database - outFileName="+outFileName;
        }
        LogHelper.v(TAG, "OutputStream is open.");
        byte[] buffer = new byte[1024];
        int len;
        while ((len = input.read(buffer)) > 0) {
            sCount += len;
            //noinspection EmptyCatchBlock
            try {
                Thread.sleep(3);
            } catch (Exception e) { }
            CircleViewHelper.setCircleViewValue((float) sCount, this);
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
            if (DataHelper.getEmail() == null || DataHelper.getEmail().length() == 0) {
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
        if (DataHelper.getConfiguration() != null) {
            isPaidEpi = DataHelper.getConfiguration().values().getAsBoolean(ConfigurationColumns.FIELD_PAID_VERSION);
        }
        if (!isPaidEpi) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(RadioTheaterApplication.getRadioTheaterApplicationContext());
            isPaidEpi = sharedPreferences.getBoolean("userPaid", false); // all episodes paid for?
            if (!isPaidEpi) {
                ConfigEpisodesContentValues existing = DataHelper.getConfigEpisodeForEpisode(episode);
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
            ConfigEpisodesContentValues existing = DataHelper.getConfigEpisodeForEpisode(episode);
            ContentValues configurationValues;
            try {
                if (existing != null && existing.values() != null && existing.values().size() != 0) {
                    // UPDATE and mark an individual episode as paid
                    configurationValues = existing.values();
                    LogHelper.v(TAG, "FOUND: so update ConfigEntry for episode "+episode+" with paid="+paid);
                    configurationValues.put(ConfigEpisodesEntry.FIELD_EPISODE_NUMBER, episode);
                    configurationValues.put(ConfigEpisodesEntry.FIELD_PURCHASED_ACCESS, paid);
                    DataHelper.updateConfigEpisodesEntry(episode, configurationValues);
                } else {
                    // CREATE and mark an individual episode as paid
                    LogHelper.v(TAG, "NOT FOUND: so create ConfigEntry for episode "+episode+" with paid="+paid);
                    configurationValues = new ContentValues();
                    configurationValues.put(ConfigEpisodesEntry.FIELD_EPISODE_NUMBER, episode);
                    configurationValues.put(ConfigEpisodesEntry.FIELD_PURCHASED_ACCESS, paid);
                    Uri result = DataHelper.insertConfigEntry(configurationValues);
                }
            }
            catch (Exception e) {
                LogHelper.e(TAG, "unable to create ConfigEntry for episode "+episode+" e="+e);
            }
        }
    }

    // this kicks off a series of AsyncTasks to load SQL tables from Firebase
    protected void loadSqliteDatabase(@SuppressWarnings("SameParameterValue") boolean okToCopyDatabase) {
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

    public Intent setShareIntentForEpisode(long episodeId) {
        LogHelper.v(TAG, "setShareIntentForEpisode");
        if (DataHelper.getEpisodeNumber() != episodeId) {
            DataHelper.getEpisodeInfoFor(episodeId);
        }
        // share button setup
        if (sShareActionProvider != null) {
            sShareIntent = getShareIntent(DataHelper.getEpisodeTitle(),
                    DataHelper.getEpisodeDescription(),
                    DataHelper.getEpisodeNumberString(),
                    DataHelper.getEpisodeDownloadUrl(),
                    DataHelper.getEpisodeWeblinkUrl());
            if (getShareIntent() != null) {
                sShareActionProvider.setShareIntent(sShareIntent);
            }
            return getShareIntent();
        }
        LogHelper.w(TAG, "*** sShareActionProvider is null! ***");
        return null;
    }

    // allow each activity to define a custom share message that is only used when sShareActionProvider is set
    // method would be labeled 'abstract' except I don't want implementation to be required
    protected Intent getShareIntent(String episodeTitle, String episodeDescription, String episodeNumber, String episodeDownloadUrl, String webLinkUrl) {return null;}

    protected void startProgressViewSpinning() {
        LogHelper.v(TAG, "startProgressViewSpinning");
        final BaseActivity baseActivity = this;
        if (! sProgressViewSpinning) {
            getHandler().post(new Runnable() {
                @Override
                public void run() {
                    sCount = 0;
                    CircleViewHelper.showCircleView(baseActivity, getCircleView(), CircleViewHelper.CircleViewType.CREATE_DATABASE);
                    CircleViewHelper.setCircleViewMaximum((float) TOTAL_SIZE_TO_COPY_IN_BYTES, baseActivity);
                    CircleViewHelper.setCircleViewValue((float) sCount, baseActivity);
                }
            });
        }
    }

    protected void problemWithSQLiteDatabase() {
        LogHelper.v(TAG, "problemWithSQLiteDatabase");
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(getResources().getString(R.string.sqlite_failed));
        alertDialog.setMessage(getResources().getString(R.string.database_loading_problem));
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        DataHelper.trackWithFirebaseAnalytics(DataHelper.getEpisodeNumberString(), DataHelper.getCurrentPosition(), "sqlite database failed");
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    protected void problemExistingDatabase(String fileName) {
        LogHelper.w(TAG, "problemExistingDatabase, fileName="+fileName);
        AlertDialog alertDialog = new AlertDialog.Builder(BaseActivity.this).create();
        alertDialog.setTitle(getResources().getString(R.string.existing_database));
        alertDialog.setMessage(getResources().getString(R.string.database_existing_problem) + "\n\nfile="+fileName);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String episode = DataHelper.getEpisodeNumberString();
                        DataHelper.trackWithFirebaseAnalytics(episode, DataHelper.getCurrentPosition(), "load sqlite failed");
                        dialog.dismiss();
                        runLoadState(LoadRadioTheaterTablesAsyncTask.LoadState.WRITERS); // begin with first load state - slow via Internet, but it gets there.
                    }
                });
        alertDialog.show();
    }

    protected void problemMissingDatabase(String fileName) {
        LogHelper.w(TAG, "problemMissingDatabase: fileName="+fileName);
        AlertDialog alertDialog = new AlertDialog.Builder(BaseActivity.this).create();
        alertDialog.setTitle(getResources().getString(R.string.missing_database));
        alertDialog.setMessage(getResources().getString(R.string.database_missing_problem) + "\n\nfile="+fileName);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String episode = DataHelper.getEpisodeNumberString();
                        DataHelper.trackWithFirebaseAnalytics(episode, DataHelper.getCurrentPosition(), "load sqlite failed");
                        dialog.dismiss();
                        runLoadState(LoadRadioTheaterTablesAsyncTask.LoadState.WRITERS); // begin with first load state - slow via Internet, but it gets there.
                    }
                });
        alertDialog.show();
    }

    protected void problemLoadingDatabase(String error) {
        LogHelper.w(TAG, "problemLoadingDatabase: error="+error);
        AlertDialog alertDialog = new AlertDialog.Builder(BaseActivity.this).create();
        alertDialog.setTitle(getResources().getString(R.string.missing_database));
        alertDialog.setMessage(getResources().getString(R.string.database_loading_problem) + "\n\nerror=" + error);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        DataHelper.trackWithFirebaseAnalytics(DataHelper.getEpisodeNumberString(), DataHelper.getCurrentPosition(), "load sqlite failed");
                        dialog.dismiss();
                        runLoadState(LoadRadioTheaterTablesAsyncTask.LoadState.WRITERS); // begin with first load state - slow via Internet, but it gets there.
                    }
                });
        alertDialog.show();
    }

    protected void maxTrialEpisodesAreReached() {
        final BaseActivity baseActivity = this;
        if (! DataHelper.isPurchased()) {
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
        DataHelper.getDatabase().child(FIREBASE_WRITERS_URL).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                LogHelper.v(TAG, "*** -------------------------------------------------------------------------------- ***");
                LoadRadioTheaterTablesAsyncTask asyncTask =
                        new LoadRadioTheaterTablesAsyncTask(activity,
                                getCircleView(),
                                dataSnapshot,
                                LoadRadioTheaterTablesAsyncTask.LoadState.WRITERS);
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
        DataHelper.getDatabase().child(FIREBASE_ACTORS_URL).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                LogHelper.v(TAG, "*** -------------------------------------------------------------------------------- ***");
                LoadRadioTheaterTablesAsyncTask asyncTask =
                        new LoadRadioTheaterTablesAsyncTask(activity,
                                getCircleView(),
                                dataSnapshot,
                                LoadRadioTheaterTablesAsyncTask.LoadState.ACTORS);
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
        DataHelper.getDatabase().child(FIREBASE_SHOWS_URL).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                LogHelper.v(TAG, "*** -------------------------------------------------------------------------------- ***");
                LoadRadioTheaterTablesAsyncTask asyncTask =
                        new LoadRadioTheaterTablesAsyncTask(activity,
                                getCircleView(),
                                dataSnapshot,
                                LoadRadioTheaterTablesAsyncTask.LoadState.EPISODES);
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

    public void enableButtons() {
        LogHelper.v(TAG, "enableButtons");
    }

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
            getCircleView().setTextColor(Color.RED);
            getCircleView().setUnitScale(0.9f); // if you want the calculated text sizes to be bigger/smaller
            getCircleView().setTextScale(0.9f); // if you want the calculated text sizes to be bigger/smaller
        }
        else if (what == CircleViewHelper.CircleViewType.PLAY_EPISODE) {
            sShowPercentUnit = false;
            LogHelper.v(TAG, "initCircleView: PLAY_EPISODE");
            getCircleView().setUnit("");
            getCircleView().setUnitVisible(false);
            getCircleView().setTextMode(TextMode.TEXT);
            getCircleView().setText("");
            getCircleView().setTextColor(Color.YELLOW);
            getCircleView().setUnitScale(0.9f); // if you want the calculated text sizes to be bigger/smaller
            getCircleView().setTextScale(0.5f); // if you want the calculated text sizes to be bigger/smaller
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

    public Handler getHandler() {
        return mHandler;
    }

    public CircleProgressView getCircleView() {
        return mCircleView;
    }

    public View getRootView() {
        return mRootView;
    }

    public static Intent getShareIntent() {
        return sShareIntent;
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
        playInfoBundle.putBoolean(KEY_ENABLE_FAB              , sEnableFAB);
        playInfoBundle.putBoolean(KEY_WAIT_FOR_MEDIA          , sWaitForMedia);

        LogHelper.v(TAG, "APP-STATE (Autoplay): saveAutoplayInfoToBundle: sHandleRotationEvent="+sHandleRotationEvent
                +", sLoadingScreenEnabled="+sLoadingScreenEnabled+", sBeginLoading="+sBeginLoading
                +", sEnableFAB="+sEnableFAB+", sWaitForMedia="+sWaitForMedia+" <<<=========");

        playInfoBundle.putBoolean(KEY_REAL_DURATION, sHaveRealDuration);

        playInfoBundle.putLong(KEY_EPISODE, DataHelper.getEpisodeNumber());
        playInfoBundle.putBoolean(KEY_PURCHASED, DataHelper.isPurchased());
        playInfoBundle.putBoolean(KEY_NOADS, DataHelper.isNoAdsForShow());
        playInfoBundle.putBoolean(KEY_DOWNLOADED, DataHelper.isDownloaded());
        playInfoBundle.putBoolean(KEY_HEARD, DataHelper.isEpisodeHeard());
        playInfoBundle.putLong(KEY_DURATION, DataHelper.getDuration());
        playInfoBundle.putLong(KEY_CURRENT_POSITION, DataHelper.getCurrentPosition());
        playInfoBundle.putBoolean(KEY_LOADED_OK, DataHelper.isLoadedOK());
        playInfoBundle.putString(KEY_AIRDATE, DataHelper.getAirdate());
        playInfoBundle.putString(KEY_TITLE, DataHelper.getEpisodeTitle());
        playInfoBundle.putString(KEY_DESCRIPTION, DataHelper.getEpisodeDescription());
        playInfoBundle.putString(KEY_WEBLINK, DataHelper.getEpisodeWeblinkUrl());
        playInfoBundle.putString(KEY_DOWNLOAD_URL, DataHelper.getEpisodeDownloadUrl());

        LogHelper.v(TAG, "APP-STATE (Base): savePlayInfoToBundle: EpisodeNumber="+ DataHelper.getEpisodeNumber()
                +", Purchased="+ DataHelper.isPurchased()+", NoAdsForShow="+ DataHelper.isNoAdsForShow()+", Downloaded="+ DataHelper.isDownloaded()
                +", EpisodeHeard="+ DataHelper.isEpisodeHeard()+", Duration="+ DataHelper.getDuration()+", sHaveRealDuration="+sHaveRealDuration
                +", mCurrentPosition="+ DataHelper.getCurrentPosition()+", LoadedOK="+ DataHelper.isLoadedOK()+", Airdate="+ DataHelper.getAirdate()
                +", EpisodeTitle="+ DataHelper.getEpisodeTitle()+", EpisodeDescription="+ DataHelper.getEpisodeDescription()
                +", EpisodeWeblinkUrl="+ DataHelper.getEpisodeWeblinkUrl()+", EpisodeDownloadUrl="+ DataHelper.getEpisodeDownloadUrl() +" <<<=========");
    }

    protected void restorePlayInfoFromBundle(Bundle playInfoBundle) {
        if (!sOnRestoreInstanceComplete) {
            sOnRestoreInstanceComplete = true;

            sHandleRotationEvent = playInfoBundle.getBoolean(KEY_HANDLE_ROTATION_EVENT);
            sLoadingScreenEnabled = playInfoBundle.getBoolean(KEY_LOADING_SCREEN_ENABLED);
            sBeginLoading = playInfoBundle.getBoolean(KEY_BEGIN_LOADING);
            sEnableFAB = playInfoBundle.getBoolean(KEY_ENABLE_FAB);
            sWaitForMedia = playInfoBundle.getBoolean(KEY_WAIT_FOR_MEDIA);

            LogHelper.v(TAG, "APP-STATE (Autoplay): restoreAutoplayInfoFromBundle: sHandleRotationEvent="+sHandleRotationEvent
                    +", sLoadingScreenEnabled="+sLoadingScreenEnabled+", sBeginLoading="+sBeginLoading
                    +", sEnableFAB="+sEnableFAB+", sWaitForMedia="+sWaitForMedia+" <<<=========");

            sHaveRealDuration = playInfoBundle.getBoolean(KEY_REAL_DURATION);

            DataHelper.setEpisodeNumber(playInfoBundle.getLong(KEY_EPISODE));
            DataHelper.setPurchased(playInfoBundle.getBoolean(KEY_PURCHASED));
            DataHelper.setNoAdsForShow(playInfoBundle.getBoolean(KEY_NOADS));
            DataHelper.setDownloaded(playInfoBundle.getBoolean(KEY_DOWNLOADED));
            DataHelper.setEpisodeHeard(playInfoBundle.getBoolean(KEY_HEARD));
            DataHelper.setDuration(playInfoBundle.getLong(KEY_DURATION));
            DataHelper.setCurrentPosition(playInfoBundle.getLong(KEY_CURRENT_POSITION));
            DataHelper.setLoadedOK(playInfoBundle.getBoolean(KEY_LOADED_OK));
            DataHelper.setAirdate(playInfoBundle.getString(KEY_AIRDATE));
            DataHelper.setEpisodeTitle(playInfoBundle.getString(KEY_TITLE));
            DataHelper.setEpisodeDescription(playInfoBundle.getString(KEY_DESCRIPTION));
            DataHelper.setEpisodeWeblinkUrl(playInfoBundle.getString(KEY_WEBLINK));
            DataHelper.setEpisodeDownloadUrl(playInfoBundle.getString(KEY_DOWNLOAD_URL));

            LogHelper.v(TAG, "APP-STATE (Base): restorePlayInfoFromBundle: sEpisodeNumber="+ DataHelper.getEpisodeNumber()
                    +", Purchased="+ DataHelper.isPurchased()+", NoAdsForShow="+ DataHelper.isNoAdsForShow()+", Downloaded="+ DataHelper.isDownloaded()
                    +", EpisodeHeard="+ DataHelper.isEpisodeHeard()+", Duration="+ DataHelper.getDuration()+", sHaveRealDuration="+sHaveRealDuration
                    +", mCurrentPosition="+ DataHelper.getCurrentPosition()+", LoadedOK="+ DataHelper.isLoadedOK()+", Airdate="+ DataHelper.getAirdate()
                    +", EpisodeTitle="+ DataHelper.getEpisodeTitle() +", EpisodeDescription="+ DataHelper.getEpisodeDescription()
                    +", EpisodeWeblinkUrl="+ DataHelper.getEpisodeWeblinkUrl() +", EpisodeDownloadUrl="+ DataHelper.getEpisodeDownloadUrl()
                    +" <<<=========");

            DataHelper.showCurrentInfo();
        }
    }

}
