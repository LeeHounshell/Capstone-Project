package com.harlie.radiotheater.radiomysterytheater;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.firebase.client.Firebase;
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
import com.harlie.radiotheater.radiomysterytheater.data.actors.ActorsColumns;
import com.harlie.radiotheater.radiomysterytheater.data.actors.ActorsCursor;
import com.harlie.radiotheater.radiomysterytheater.data.actors.ActorsSelection;
import com.harlie.radiotheater.radiomysterytheater.data.actorsepisodes.ActorsEpisodesColumns;
import com.harlie.radiotheater.radiomysterytheater.data.actorsepisodes.ActorsEpisodesCursor;
import com.harlie.radiotheater.radiomysterytheater.data.actorsepisodes.ActorsEpisodesSelection;
import com.harlie.radiotheater.radiomysterytheater.data.configepisodes.ConfigEpisodesColumns;
import com.harlie.radiotheater.radiomysterytheater.data.configepisodes.ConfigEpisodesContentValues;
import com.harlie.radiotheater.radiomysterytheater.data.configepisodes.ConfigEpisodesCursor;
import com.harlie.radiotheater.radiomysterytheater.data.configepisodes.ConfigEpisodesSelection;
import com.harlie.radiotheater.radiomysterytheater.data.configuration.ConfigurationColumns;
import com.harlie.radiotheater.radiomysterytheater.data.episodes.EpisodesColumns;
import com.harlie.radiotheater.radiomysterytheater.data.episodes.EpisodesCursor;
import com.harlie.radiotheater.radiomysterytheater.data.episodes.EpisodesSelection;
import com.harlie.radiotheater.radiomysterytheater.data.writers.WritersColumns;
import com.harlie.radiotheater.radiomysterytheater.data.writers.WritersCursor;
import com.harlie.radiotheater.radiomysterytheater.data.writers.WritersSelection;
import com.harlie.radiotheater.radiomysterytheater.data.writersepisodes.WritersEpisodesColumns;
import com.harlie.radiotheater.radiomysterytheater.data.writersepisodes.WritersEpisodesCursor;
import com.harlie.radiotheater.radiomysterytheater.data.writersepisodes.WritersEpisodesSelection;

import com.harlie.radiotheater.radiomysterytheater.data_helper.LoadRadioTheaterTablesAsyncTask;
import com.harlie.radiotheater.radiomysterytheater.data_helper.LoadingAsyncTask;
import com.harlie.radiotheater.radiomysterytheater.firebase.FirebaseConfigEpisode;
import com.harlie.radiotheater.radiomysterytheater.firebase.FirebaseConfiguration;
import com.harlie.radiotheater.radiomysterytheater.utils.CircleViewHelper;
import com.harlie.radiotheater.radiomysterytheater.utils.LogHelper;
import com.harlie.radiotheater.radiomysterytheater.utils.NetworkHelper;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;

import at.grabner.circleprogress.AnimationState;
import at.grabner.circleprogress.AnimationStateChangedListener;
import at.grabner.circleprogress.CircleProgressView;
import at.grabner.circleprogress.TextMode;

import static com.harlie.radiotheater.radiomysterytheater.data_helper.RadioTheaterContract.*;

@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity {
    private final static String TAG = "LEE: <" + BaseActivity.class.getSimpleName() + ">";

    protected static final boolean COPY_PACKAGED_SQLITE_DATABASE = true;
    protected static final int TOTAL_SIZE_TO_COPY_IN_BYTES = 1347584;
    protected static final String FIREBASE_WRITERS_URL = "radiomysterytheater/0/writers";
    protected static final String FIREBASE_ACTORS_URL = "radiomysterytheater/1/actors";
    protected static final String FIREBASE_SHOWS_URL = "radiomysterytheater/2/shows";

    public enum AutoplayState {
        READY2PLAY, LOADING, PLAYING, PAUSED
    }
    protected AutoplayState mAutoplayState = AutoplayState.READY2PLAY;

    protected int mAudioFocusRequstResult;
    protected long mDuration;
    protected long mCurrentPosition;
    protected String mAirdate;
    protected String mEpisodeTitle;
    protected String mEpisodeDescription;
    protected String mEpisodeWeblinkUrl;
    protected String mEpisodeDownloadUrl;

    protected String mMediaId;
    protected long mEpisodeNumber;

    public static volatile long sKickstartTime;

    private static volatile boolean sOnRestoreInstanceComplete;

    protected static volatile boolean sHandleRotationEvent;
    protected static volatile boolean sNeed2RestartPlayback;
    protected static volatile boolean sLoadingScreenEnabled;
    protected static volatile boolean sBeginLoading;
    protected static volatile boolean sSeekUpdateRunning;
    protected static volatile boolean sAutoplayNextNow;
    protected static volatile boolean sOkKickstart;

    protected static volatile boolean sPurchased;
    protected static volatile boolean sNoAdsForShow;
    protected static volatile boolean sDownloaded;
    protected static volatile boolean sEpisodeHeard;
    protected static volatile boolean sHaveRealDuration;
    protected static volatile boolean sSeeking;
    protected static volatile boolean sPlaying;
    protected static volatile boolean sLoadedOK;
    protected static volatile boolean sShowUnit;
    protected static volatile boolean sCopiedDatabaseSuccess;
    public static volatile boolean sProgressViewSpinning;

    protected static final int MIN_EMAIL_LENGTH = 3;
    protected static final int MIN_PASSWORD_LENGTH = 6;

    protected FirebaseAuth mAuth;
    protected Firebase mFirebase;
    protected DatabaseReference mDatabase;
    protected FirebaseAnalytics mFirebaseAnalytics;
    protected Handler mHandler;
    protected View mRootView;
    protected CircleProgressView mCircleView;

    private static int mCount;

    private String email;
    private String uid;
    private String pass;
    private Boolean isPaid;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        LogHelper.v(TAG, "onCreate");
        sOnRestoreInstanceComplete = false;

        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            sHandleRotationEvent = true;
            onRestoreInstanceState(savedInstanceState);
        }
        else {
            sHandleRotationEvent = false;
            sNeed2RestartPlayback = false;
            sLoadingScreenEnabled = false;
            sBeginLoading = false;
            sSeekUpdateRunning = false;
            sAutoplayNextNow = false;
            sOkKickstart = false;

            sPurchased = false;
            sNoAdsForShow = false;
            sDownloaded = false;
            sEpisodeHeard = false;
            sHaveRealDuration = false;
            sSeeking = false;
            sPlaying = false;
            sLoadedOK = false;
            sShowUnit = false;
            sCopiedDatabaseSuccess = false;

            Bundle playInfo = getIntent().getExtras();
            if (playInfo != null) {
                restorePlayInfoFromBundle(playInfo);
            }
        }

        mRootView = findViewById(android.R.id.content);
        mHandler = new Handler();
        Firebase.setAndroidContext(this);
        mAuth = FirebaseAuth.getInstance();
        mFirebase = new Firebase("https://radio-mystery-theater.firebaseio.com");
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    @Override
    public void onDestroy() {
        LogHelper.v(TAG, "onDestroy");
        super.onDestroy();
        mAuth = null;
        mCircleView = null;
        mDatabase = null;
        mHandler = null;
        mRootView = null;
        mMediaId = null;
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
    protected void authenticateRadioMysteryTheaterFirebaseAccount(final String email, String pass) {
        LogHelper.v(TAG, "authenticateRadioMysteryTheaterFirebaseAccount - Firebase Login using email="+email+", and password");
        if (mAuth != null && email != null && pass != null && isValid(email, pass)) {
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
                                setUID(uid);
                                trackSignupWithFirebaseAnalytics();
                                startAutoplayActivity();
                            }
                            else {
                                userLoginFailed();
                                startAuthenticationActivity();
                            }
                        }
                    });
        }
        else {
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
        Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(this, android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
        startActivity(authenticationIntent, bundle);
        overridePendingTransition(0,0);
        finish();
    }

    public String getEmail() {
        if (email == null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(RadioTheaterApplication.getRadioTheaterApplicationContext());
            email = sharedPreferences.getString("userEmail", "");
            if (email.length() == 0) {
                email = null;
            }
        }
        return email;
    }

    public void setEmail(String email) {
        if (email != null && email.length() > 0) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(RadioTheaterApplication.getRadioTheaterApplicationContext());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("userEmail", email);
            editor.apply();
        }
        this.email = email;
    }

    public String getUID() {
        if (uid == null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(RadioTheaterApplication.getRadioTheaterApplicationContext());
            uid = sharedPreferences.getString("userUID", "");
            if (uid.length() == 0) {
                uid = null;
            }
        }
        return uid;
    }

    public void setUID(String uid) {
        if (uid != null && uid.length() > 0) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(RadioTheaterApplication.getRadioTheaterApplicationContext());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("userUID", uid);
            editor.apply();
        }
        this.uid = uid;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public void startAutoplayActivity() {
        LogHelper.v(TAG, "---> startAutoplayActivity <---");
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
            editor.putString("uid", getUID());
            editor.apply();

            Intent autoplayIntent = new Intent(this, AutoplayActivity.class);
            // close existing activity stack regardless of what's in there and create new root
            //autoplayIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            Bundle playInfo = new Bundle();
            savePlayInfoToBundle(playInfo);
            autoplayIntent.putExtras(playInfo);
            Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(this, android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
            startActivity(autoplayIntent, bundle);
            finish();
        }
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
        Boolean isPaid = true;
        //
        // FIXME: if (existing == null)
        //            need to query Firebase record for this user to see if they paid already..
        //            and need to see if this episode is on the user's list of 10 free episodes.
        //
        // NOTE: the code below uses the #IFDEF gradle preprocessor
        //#IFDEF 'TRIAL'
        isPaid = new Boolean(false);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(RadioTheaterApplication.getRadioTheaterApplicationContext());
        isPaid = sharedPreferences.getBoolean("userPaid", false); // all episodes paid for?
        if (!isPaid) {
            ConfigEpisodesContentValues existing = getConfigForEpisode(episode);
            if (existing != null) {
                ContentValues configEpisode = existing.values();
                isPaid = configEpisode.getAsBoolean(ConfigEpisodesEntry.FIELD_PURCHASED_ACCESS);
            }
        }
        //#ENDIF
        return isPaid;
    }

    public void setPaidEpisode(String episode, Boolean paid) {
        LogHelper.v(TAG, "setPaidEpisode: episode="+episode+", paid="+paid);
        if (episode == null) {
            // NOTE: special case with NULL episode - mark all episodes as paid
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(RadioTheaterApplication.getRadioTheaterApplicationContext());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("userPaid", paid);
            editor.apply();

            // FIXME: update local SQLite Configuration table with user info
            // FIXME: update Firebase record for this user's email to be PAID
        }
        else {
            // see if a record already exists for this episode
            ConfigEpisodesContentValues existing = getConfigForEpisode(episode);
            ContentValues configurationValues;
            try {
                if (existing != null) {
                    // UPDATE and mark an individual episode as paid
                    configurationValues = existing.values();
                    LogHelper.v(TAG, "FOUND: so update ConfigEntry for episode "+episode+" with paid="+paid);
                    configurationValues.put(ConfigEpisodesEntry.FIELD_EPISODE_NUMBER, episode);
                    configurationValues.put(ConfigEpisodesEntry.FIELD_PURCHASED_ACCESS, paid);
                    updateConfigEntryValues(episode, configurationValues);
                } else {
                    // CREATE and mark an individual episode as paid
                    LogHelper.v(TAG, "NOT FOUND: so create ConfigEntry for episode "+episode+" with paid="+paid);
                    configurationValues = new ContentValues();
                    configurationValues.put(ConfigEpisodesEntry.FIELD_EPISODE_NUMBER, episode);
                    configurationValues.put(ConfigEpisodesEntry.FIELD_PURCHASED_ACCESS, paid);
                    Uri result = insertConfigEntryValues(configurationValues);
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
                    sCopiedDatabaseSuccess = true;
                    startAutoplayActivity();
                    overridePendingTransition(0,0);
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
                        trackWithFirebaseAnalytics(String.valueOf(mEpisodeNumber), mCurrentPosition, "load sqlite failed");
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
                        trackWithFirebaseAnalytics(String.valueOf(mEpisodeNumber), mCurrentPosition, "load sqlite failed");
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
                        trackWithFirebaseAnalytics(String.valueOf(mEpisodeNumber), mCurrentPosition, "load sqlite failed");
                        dialog.dismiss();
                        runLoadState(LoadRadioTheaterTablesAsyncTask.LoadState.WRITERS); // begin with first load state - slow via Internet, but it gets there.
                    }
                });
        alertDialog.show();
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
            // ok, we're in
            startAutoplayActivity();
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

    public ConfigEpisodesContentValues getConfigForEpisode(String episode) {
        ConfigEpisodesContentValues record = null;
        ConfigEpisodesSelection where = new ConfigEpisodesSelection();
        where.fieldEpisodeNumber(Long.parseLong(episode));
        String order_limit = ConfigEpisodesEntry.FIELD_EPISODE_NUMBER + " ASC LIMIT 1";

        Cursor cursor = getContentResolver().query(
                ConfigEpisodesColumns.CONTENT_URI,  // the 'content://' Uri to query
                null,                               // projection String[] - leaving "columns" null just returns all the columns.
                where.sel(),                        // selection - SQL where
                where.args(),                       // selection args String[] - values for the "where" clause
                order_limit);                       // sort order and limit (String)

        if (cursor != null && cursor.getCount() > 0) {
            ConfigEpisodesCursor configEpisodesCursor = new ConfigEpisodesCursor(cursor);
            record = getConfigEpisodesContentValues(configEpisodesCursor);
        }
        else {
            LogHelper.v(TAG, "SQL: episode "+episode+" not found");
            // FIXME: need to query Firebase record for this user (maybe - not if used by EpisodeRecyclerViewAdapter)
        }
        return record;
    }

    public EpisodesCursor getEpisodesCursor(long episode) {
        EpisodesSelection where = new EpisodesSelection();
        where.fieldEpisodeNumber(episode);
        String order_limit = EpisodesEntry.FIELD_EPISODE_NUMBER + " ASC LIMIT 1";

        Cursor cursor = getContentResolver().query(
                EpisodesColumns.CONTENT_URI,        // the 'content://' Uri to query
                null,                               // projection String[] - leaving "columns" null just returns all the columns.
                where.sel(),                        // selection - SQL where
                where.args(),                       // selection args String[] - values for the "where" clause
                order_limit);                       // sort order and limit (String)

        return (cursor != null && cursor.getCount() > 0) ? new EpisodesCursor(cursor) : null;
    }

    public ActorsCursor getActorsCursor(long actor) {
        ActorsSelection where = new ActorsSelection();
        where.fieldActorId(actor);
        String order_limit = ActorsEntry.FIELD_ACTOR_ID + " ASC LIMIT 1";

        Cursor cursor = getContentResolver().query(
                ActorsColumns.CONTENT_URI,          // the 'content://' Uri to query
                null,                               // projection String[] - leaving "columns" null just returns all the columns.
                where.sel(),                        // selection - SQL where
                where.args(),                       // selection args String[] - values for the "where" clause
                order_limit);                       // sort order and limit (String)

        return (cursor != null && cursor.getCount() > 0) ? new ActorsCursor(cursor) : null;
    }

    public WritersCursor getWritersCursor(long writer) {
        WritersSelection where = new WritersSelection();
        where.fieldWriterId(writer);
        String order_limit = WritersEntry.FIELD_WRITER_ID + " ASC LIMIT 1";

        Cursor cursor = getContentResolver().query(
                WritersColumns.CONTENT_URI,         // the 'content://' Uri to query
                null,                               // projection String[] - leaving "columns" null just returns all the columns.
                where.sel(),                        // selection - SQL where
                where.args(),                       // selection args String[] - values for the "where" clause
                order_limit);                       // sort order and limit (String)

        return (cursor != null && cursor.getCount() > 0) ? new WritersCursor(cursor) : null;
    }

    public ActorsEpisodesCursor getActorsEpisodesCursor(long episode) {
        ActorsEpisodesSelection where = new ActorsEpisodesSelection();
        where.fieldEpisodeNumber(episode);
        String order_limit = ActorsEpisodesEntry.FIELD_EPISODE_NUMBER + " ASC";

        Cursor cursor = getContentResolver().query(
                ActorsEpisodesColumns.CONTENT_URI,  // the 'content://' Uri to query
                null,                               // projection String[] - leaving "columns" null just returns all the columns.
                where.sel(),                        // selection - SQL where
                where.args(),                       // selection args String[] - values for the "where" clause
                order_limit);                       // sort order and limit (String)

        return (cursor != null && cursor.getCount() > 0) ? new ActorsEpisodesCursor(cursor) : null;
    }

    public WritersEpisodesCursor getWritersEpisodesCursor(long episode) {
        WritersEpisodesSelection where = new WritersEpisodesSelection();
        where.fieldEpisodeNumber(episode);
        String order_limit = WritersEpisodesEntry.FIELD_EPISODE_NUMBER + " ASC";

        Cursor cursor = getContentResolver().query(
                WritersEpisodesColumns.CONTENT_URI, // the 'content://' Uri to query
                null,                               // projection String[] - leaving "columns" null just returns all the columns.
                where.sel(),                        // selection - SQL where
                where.args(),                       // selection args String[] - values for the "where" clause
                order_limit);                       // sort order and limit (String)

        return (cursor != null && cursor.getCount() > 0) ? new WritersEpisodesCursor(cursor) : null;
    }

    public ConfigEpisodesCursor getCursorForNextAvailableEpisode() {
        ConfigEpisodesSelection where = new ConfigEpisodesSelection();
        // find the next unwatched episode, in airdate order
        where.fieldEpisodeHeard(false);
        if (! NetworkHelper.isOnline(this)) {
            // find the next DOWNLOADED unwatched episode, in airdate order
            where.fieldEpisodeDownloaded(true);
        }

        String order_limit = ConfigEpisodesEntry.FIELD_EPISODE_NUMBER + " ASC LIMIT 1";

        Cursor cursor = getContentResolver().query(
                ConfigEpisodesColumns.CONTENT_URI,  // the 'content://' Uri to query
                null,                               // projection String[] - leaving "columns" null just returns all the columns.
                where.sel(),                        // selection - SQL where
                where.args(),                       // selection args String[] - values for the "where" clause
                order_limit);                       // sort order and limit (String)

        return (cursor != null) ? new ConfigEpisodesCursor(cursor) : null;
    }

    @NonNull
    private ConfigEpisodesContentValues getConfigEpisodesContentValues(ConfigEpisodesCursor cursor) {
        LogHelper.v(TAG, "getConfigEpisodesContentValues: SQL found "+cursor.getCount()+" records");
        ConfigEpisodesContentValues record = new ConfigEpisodesContentValues();

        if (cursor.moveToNext()) {
            try {
                long episodeNumber = cursor.getFieldEpisodeNumber();
                record.putFieldEpisodeNumber(episodeNumber);

                //#IFDEF 'PAID'
                //boolean purchased = true;
                //boolean noAdsForShow = true;
                //#ENDIF

                //#IFDEF 'TRIAL'
                boolean purchased = cursor.getFieldPurchasedAccess();
                boolean noAdsForShow = cursor.getFieldPurchasedNoads();
                //#ENDIF

                record.putFieldPurchasedAccess(purchased);
                record.putFieldPurchasedNoads(noAdsForShow);

                boolean downloaded = cursor.getFieldEpisodeDownloaded();
                record.putFieldEpisodeDownloaded(downloaded);

                boolean episodeHeard = cursor.getFieldEpisodeHeard();
                record.putFieldEpisodeHeard(episodeHeard);

                int listenCount = cursor.getFieldListenCount();
                record.putFieldListenCount(listenCount);
            } catch (Exception e) {
                LogHelper.e(TAG, "RECORD NOT FOUND: Exception=" + e);
                record = null;
            }
        }
        cursor.close();
        return record;
    }

    public Uri insertConfigEntryValues(ContentValues configEntryValues) {
        LogHelper.v(TAG, "insertConfigEntryValues");
        Uri configEntry = ConfigEpisodesEntry.buildConfigEpisodesUri();
        return this.getContentResolver().insert(configEntry, configEntryValues);
    }

    // update local SQLite
    public int updateConfigEntryValues(String episode, ContentValues configEntryValues) {
        LogHelper.v(TAG, "updateConfigEntryValues");
        Uri configEntry = ConfigEpisodesEntry.buildConfigEpisodesUri();
        String whereClause = ConfigEpisodesEntry.FIELD_EPISODE_NUMBER + "=?";
        String whereCondition[] = new String[]{episode};
        int rc = this.getContentResolver().update(configEntry, configEntryValues, whereClause, whereCondition);
        return rc;
    }

    public void loadAnyExistingFirebaseConfigurationValues(String deviceId) {
        LogHelper.v(TAG, "*** FIREBASE REQUEST *** - loadAnyExistingFirebaseConfigurationValues: deviceId="+deviceId);
        if (deviceId == null) {
            LogHelper.e(TAG, "ERROR: loadAnyExistingFirebaseConfigurationValues deviceId is null");
            return;
        }
        final BaseActivity activity = this;
        // Attach a listener to read the data initially
        getDatabase().child("configuration").child(deviceId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                LogHelper.v(TAG, "*** -------------------------------------------------------------------------------- ***");
                // load the dataSnapshot info
                Object configurationObject = dataSnapshot.getValue();
                if (configurationObject == null) {
                    LogHelper.e(TAG, "the Firebase User Configuration (DataSnapshot) is null! email="+email);
                    return;
                }
                String configurationJSON = configurationObject.toString();
                LogHelper.v(TAG, "configurationJSON="+configurationJSON);
                // FIXME: lee
                LogHelper.v(TAG, "*** -------------------------------------------------------------------------------- ***");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogHelper.v(TAG, "loadAnyExistingFirebaseConfigurationValues - onCancelled");
                activity.startAuthenticationActivity();
            }
        });

    }

    // update Firebase User Account info
    public void updateFirebaseConfigurationValues(ContentValues configurationValues, String deviceId) {
        String  email = getEmail();
        Boolean authenticated = configurationValues.getAsBoolean(ConfigurationColumns.FIELD_AUTHENTICATED);
        Boolean paid_version = configurationValues.getAsBoolean(ConfigurationColumns.FIELD_PAID_VERSION);
        Boolean purchase_access = configurationValues.getAsBoolean(ConfigurationColumns.FIELD_PURCHASE_ACCESS);
        Boolean purchase_noads = configurationValues.getAsBoolean(ConfigurationColumns.FIELD_PURCHASE_NOADS);
        Long    total_listen_count = configurationValues.getAsLong(ConfigurationColumns.FIELD_TOTAL_LISTEN_COUNT);

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
        String  email = getEmail();
        Boolean purchased_access = configEntryValues.getAsBoolean(ConfigEpisodesColumns.FIELD_PURCHASED_ACCESS);
        Boolean purchased_noads = configEntryValues.getAsBoolean(ConfigEpisodesColumns.FIELD_PURCHASED_NOADS);
        Boolean episode_downloaded = configEntryValues.getAsBoolean(ConfigEpisodesColumns.FIELD_EPISODE_DOWNLOADED);
        Boolean episode_heard = configEntryValues.getAsBoolean(ConfigEpisodesColumns.FIELD_EPISODE_HEARD);
        Long    episode_count = configEntryValues.getAsLong(ConfigEpisodesColumns.FIELD_LISTEN_COUNT);

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
        sShowUnit = false;
        if (what == CircleViewHelper.CircleViewType.CREATE_DATABASE) {
            LogHelper.v(TAG, "initCircleView: CREATE_DATABASE");
            getCircleView().setUnit("%");
            getCircleView().setUnitVisible(true);
            getCircleView().setTextMode(TextMode.PERCENT); // Shows current percent of the current value from the max value
        }
        else if (what == CircleViewHelper.CircleViewType.PLAY_EPISODE) {
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

                sShowUnit = false;
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
                                            getCircleView().setUnitVisible(sShowUnit);
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
            mAutoplayState = AutoplayState.PLAYING;
            LoadingAsyncTask.mLoadingNow = false;
            if (getCircleView() != null) {
                getCircleView().setValue(value);
                LogHelper.w(TAG, "setCircleViewValue: value=" + value);
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
    // Getters
    //--------------------------------------------------------------------------------

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

    public String getAirdate() {
        return mAirdate;
    }

    public String getEpisodeTitle() {

        return mEpisodeTitle;
    }

    public long getEpisodeNumber() {
        return mEpisodeNumber;
    }

    public String getEpisodeDescription() {
        return mEpisodeDescription;
    }

    public String getEpisodeWeblinkUrl() {
        return mEpisodeWeblinkUrl;
    }

    public String getEpisodeDownloadUrl() {
        return mEpisodeDownloadUrl;
    }

    public String getMediaId() {
        return mMediaId;
    }

    public CircleProgressView getCircleView() {
        return mCircleView;
    }

    public View getRootView() {
        return mRootView;
    }

    public AutoplayState getAutoplayState() {
        return mAutoplayState;
    }

    public static boolean isPlaying() {
        return sPlaying;
    }

    public static boolean isLoadedOK() {
        return sLoadedOK;
    }

    //--------------------------------------------------------------------------------
    // Autoplay related
    //--------------------------------------------------------------------------------

    private static final String KEY_HANDLE_ROTATION_EVENT   = "rotationEvent";
    private static final String KEY_NEED_RESTART_PLAYBACK   = "restartPlayback";
    private static final String KEY_LOADING_SCREEN_ENABLED  = "loadingEnabled";
    private static final String KEY_BEGIN_LOADING           = "beginLoading";
    private static final String KEY_SEEK_UPDATE_RUNNING     = "seekUpdateRunning";
    private static final String KEY_AUTOPLAY_NEXT_NOW       = "autoplayNextNow";
    private static final String KEY_KICKSTART_OK            = "kickstartOk";
    private static final String KEY_KICKSTART_TIME          = "kickstartTime";

    private static final String KEY_MEDIA_ID                = "mediaId";
    private static final String KEY_EPISODE                 = "episodeNumber";
    private static final String KEY_PURCHASED               = "purchased";
    private static final String KEY_NOADS                   = "noads";
    private static final String KEY_DOWNLOADED              = "downloaded";
    private static final String KEY_HEARD                   = "heard";
    private static final String KEY_AUDIO_FOCUS             = "audioFocus";
    private static final String KEY_DURATION                = "duration";
    private static final String KEY_REAL_DURATION           = "realDuration";
    private static final String KEY_CURRENT_POSITION        = "position";
    private static final String KEY_SEEKING                 = "seeking";
    private static final String KEY_PLAYING                 = "playing";
    private static final String KEY_LOADED_OK               = "loaded_ok";
    private static final String KEY_AIRDATE                 = "airdate";
    private static final String KEY_TITLE                   = "title";
    private static final String KEY_DESCRIPTION             = "description";
    private static final String KEY_WEBLINK                 = "weblink";
    private static final String KEY_DOWNLOAD_URL            = "downloadUrl";
    private static final String KEY_AUTOPLAY_STATE          = "autoplayState";

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
        playInfoBundle.putBoolean(KEY_NEED_RESTART_PLAYBACK   , sNeed2RestartPlayback);
        playInfoBundle.putBoolean(KEY_LOADING_SCREEN_ENABLED  , sLoadingScreenEnabled);
        playInfoBundle.putBoolean(KEY_BEGIN_LOADING           , sBeginLoading);
        playInfoBundle.putBoolean(KEY_SEEK_UPDATE_RUNNING     , sSeekUpdateRunning);
        playInfoBundle.putBoolean(KEY_AUTOPLAY_NEXT_NOW       , sAutoplayNextNow);
        playInfoBundle.putBoolean(KEY_KICKSTART_OK            , sOkKickstart);
        playInfoBundle.putLong   (KEY_KICKSTART_TIME          , sKickstartTime);

        LogHelper.v(TAG, "APP-STATE (Autoplay): saveAutoplayInfoToBundle: sHandleRotationEvent="+sHandleRotationEvent+", sNeed2RestartPlayback="+sNeed2RestartPlayback
                +", sLoadingScreenEnabled="+sLoadingScreenEnabled+", sBeginLoading="+sBeginLoading+", sSeekUpdateRunning="+sSeekUpdateRunning
                +", sAutoplayNextNow="+sAutoplayNextNow+", sOkKickstart="+sOkKickstart+", sKickstartTime="+sKickstartTime+" <<<=========");

        playInfoBundle.putString(KEY_MEDIA_ID, getMediaId());
        playInfoBundle.putLong(KEY_EPISODE, getEpisodeNumber());
        playInfoBundle.putBoolean(KEY_PURCHASED, sPurchased);
        playInfoBundle.putBoolean(KEY_NOADS, sNoAdsForShow);
        playInfoBundle.putBoolean(KEY_DOWNLOADED, sDownloaded);
        playInfoBundle.putBoolean(KEY_HEARD, sEpisodeHeard);
        playInfoBundle.putInt(KEY_AUDIO_FOCUS, mAudioFocusRequstResult);
        playInfoBundle.putLong(KEY_DURATION, mDuration);
        playInfoBundle.putBoolean(KEY_REAL_DURATION, sHaveRealDuration);
        playInfoBundle.putLong(KEY_CURRENT_POSITION, mCurrentPosition);
        playInfoBundle.putBoolean(KEY_SEEKING, sSeeking);
        playInfoBundle.putBoolean(KEY_PLAYING, sPlaying);
        playInfoBundle.putBoolean(KEY_LOADED_OK, sLoadedOK);
        playInfoBundle.putString(KEY_AIRDATE, getAirdate());
        playInfoBundle.putString(KEY_TITLE, getEpisodeTitle());
        playInfoBundle.putString(KEY_DESCRIPTION, getEpisodeDescription());
        playInfoBundle.putString(KEY_WEBLINK, getEpisodeWeblinkUrl());
        playInfoBundle.putString(KEY_DOWNLOAD_URL, getEpisodeDownloadUrl());

        switch (getAutoplayState()) {
            case READY2PLAY: {
                playInfoBundle.putInt(KEY_AUTOPLAY_STATE, 0);
                break;
            }
            case LOADING: {
                playInfoBundle.putInt(KEY_AUTOPLAY_STATE, 1);
                break;
            }
            case PLAYING: {
                playInfoBundle.putInt(KEY_AUTOPLAY_STATE, 2);
                break;
            }
            case PAUSED: {
                playInfoBundle.putInt(KEY_AUTOPLAY_STATE, 3);
                break;
            }
        }

        LogHelper.v(TAG, "APP-STATE (Base): savePlayInfoToBundle: mMediaId="+mMediaId+", mEpisodeNumber="+mEpisodeNumber
                +", sPurchased="+sPurchased+", sNoAdsForShow="+sNoAdsForShow+", sDownloaded="+sDownloaded+", sEpisodeHeard="+sEpisodeHeard
                +", mAudioFocusRequestResult="+mAudioFocusRequstResult+", mDuration="+mDuration+", sHaveRealDuration="+sHaveRealDuration
                +", mCurrentPosition="+mCurrentPosition+", sSeeking="+sSeeking+", sPlaying="+sPlaying+", sLoadedOK="+sLoadedOK
                +", mAirdate="+mAirdate+", mEpisodeTitle="+mEpisodeTitle+", mEpisodeDescription="+mEpisodeDescription
                +", mEpisodeWeblinkUrl="+mEpisodeWeblinkUrl+", mEpisodeDownloadUrl="+mEpisodeDownloadUrl+", mAutoplayState="+mAutoplayState+" <<<=========");
    }

    protected void restorePlayInfoFromBundle(Bundle playInfoBundle) {
        if (!sOnRestoreInstanceComplete) {
            sOnRestoreInstanceComplete = true;

            sHandleRotationEvent = playInfoBundle.getBoolean(KEY_HANDLE_ROTATION_EVENT);
            sNeed2RestartPlayback = playInfoBundle.getBoolean(KEY_NEED_RESTART_PLAYBACK);
            sLoadingScreenEnabled = playInfoBundle.getBoolean(KEY_LOADING_SCREEN_ENABLED);
            sBeginLoading = playInfoBundle.getBoolean(KEY_BEGIN_LOADING);
            sSeekUpdateRunning = playInfoBundle.getBoolean(KEY_SEEK_UPDATE_RUNNING);
            sAutoplayNextNow = playInfoBundle.getBoolean(KEY_AUTOPLAY_NEXT_NOW);
            sOkKickstart = playInfoBundle.getBoolean(KEY_KICKSTART_OK);
            sKickstartTime = playInfoBundle.getLong(KEY_KICKSTART_TIME);

            LogHelper.v(TAG, "APP-STATE (Autoplay): restoreAutoplayInfoFromBundle: sHandleRotationEvent="+sHandleRotationEvent+", sNeed2RestartPlayback="+sNeed2RestartPlayback
                    +", sLoadingScreenEnabled="+sLoadingScreenEnabled+", sBeginLoading="+sBeginLoading+", sSeekUpdateRunning="+sSeekUpdateRunning
                    +", sAutoplayNextNow="+sAutoplayNextNow+", sOkKickstart="+sOkKickstart+", sKickstartTime="+sKickstartTime+" <<<=========");

            mMediaId = playInfoBundle.getString(KEY_MEDIA_ID);
            mEpisodeNumber = playInfoBundle.getLong(KEY_EPISODE);
            sPurchased = playInfoBundle.getBoolean(KEY_PURCHASED);
            sNoAdsForShow = playInfoBundle.getBoolean(KEY_NOADS);
            sDownloaded = playInfoBundle.getBoolean(KEY_DOWNLOADED);
            sEpisodeHeard = playInfoBundle.getBoolean(KEY_HEARD);
            mAudioFocusRequstResult = playInfoBundle.getInt(KEY_AUDIO_FOCUS);
            mDuration = playInfoBundle.getLong(KEY_DURATION);
            sHaveRealDuration = playInfoBundle.getBoolean(KEY_REAL_DURATION);
            mCurrentPosition = playInfoBundle.getLong(KEY_CURRENT_POSITION);
            sSeeking = playInfoBundle.getBoolean(KEY_SEEKING);
            sPlaying = playInfoBundle.getBoolean(KEY_PLAYING);
            sLoadedOK = playInfoBundle.getBoolean(KEY_LOADED_OK);
            mAirdate = playInfoBundle.getString(KEY_AIRDATE);
            mEpisodeTitle = playInfoBundle.getString(KEY_TITLE);
            mEpisodeDescription = playInfoBundle.getString(KEY_DESCRIPTION);
            mEpisodeWeblinkUrl = playInfoBundle.getString(KEY_WEBLINK);
            mEpisodeDownloadUrl = playInfoBundle.getString(KEY_DOWNLOAD_URL);

            int state = playInfoBundle.getInt(KEY_AUTOPLAY_STATE);
            if (state == 1) {
                setAutoplayState(AutoplayState.LOADING, "restorePlayInfoFromBundle - LOADING");
            } else if (state == 2) {
                setAutoplayState(AutoplayState.PLAYING, "restorePlayInfoFromBundle - PLAYING");
            } else if (state == 3) {
                setAutoplayState(AutoplayState.PAUSED, "restorePlayInfoFromBundle - PAUSED");
            } else {
                setAutoplayState(AutoplayState.READY2PLAY, "restorePlayInfoFromBundle - READY2PLAY");
            }

            LogHelper.v(TAG, "APP-STATE (Base): restorePlayInfoFromBundle: mMediaId="+mMediaId+", mEpisodeNumber="+mEpisodeNumber
                    +", sPurchased="+sPurchased+", sNoAdsForShow="+sNoAdsForShow+", sDownloaded="+sDownloaded+", sEpisodeHeard="+sEpisodeHeard
                    +", mAudioFocusRequestResult="+mAudioFocusRequstResult+", mDuration="+mDuration+", sHaveRealDuration="+sHaveRealDuration
                    +", mCurrentPosition="+mCurrentPosition+", sSeeking="+sSeeking+", sPlaying="+sPlaying+", sLoadedOK="+sLoadedOK
                    +", mAirdate="+mAirdate+", mEpisodeTitle="+mEpisodeTitle+", mEpisodeDescription="+mEpisodeDescription
                    +", mEpisodeWeblinkUrl="+mEpisodeWeblinkUrl+", mEpisodeDownloadUrl="+mEpisodeDownloadUrl+", mAutoplayState="+mAutoplayState+" <<<=========");

            showCurrentInfo();
        }
    }

    protected void showCurrentInfo() {
        String state = "unknown";
        switch (getAutoplayState()) {
            case READY2PLAY: {
                state = "READY to PLAY";
                break;
            }
            case LOADING: {
                state = "LOADING Episode";
                break;
            }
            case PLAYING: {
                state = "PLAYING Episode";
                break;
            }
            case PAUSED: {
                state = "Episode PAUSED";
                break;
            }
        }
        LogHelper.v(TAG, "===> EPISODE INFO"
                + ": mEpisodeTitle=" + getEpisodeTitle()
                + ": mEpisodeNumber=" + getEpisodeNumber()
                + ": mAirdate=" + getAirdate()
                + ": mEpisodeDescription=" + getEpisodeDescription()
                + ": mEpisodeWeblinkUrl=" + getEpisodeWeblinkUrl()
                + ": mEpisodeDownloadUrl=" + getEpisodeDownloadUrl()
                + ", sPurchased=" + sPurchased
                + ", sNoAdsForShow=" + sNoAdsForShow
                + ", sDownloaded=" + sDownloaded
                + ", sEpisodeHeard=" + sEpisodeHeard
                + ", mAutoplayState=" + state);
    }

    protected void setAutoplayState(AutoplayState autoplayState, String log) {
        LogHelper.v(TAG, "setAutoplayState: "+autoplayState+", "+log);
        this.mAutoplayState = autoplayState;
    }

    public void markEpisodeAsHeardAndIncrementPlayCount(long episodeNumber, String episodeIndex, long duration) {
        LogHelper.v(TAG, "markEpisodeAsHeardAndIncrementPlayCount: episodeNumber="+episodeNumber+", episodeIndex="+episodeIndex+", duration="+duration);
        boolean matchError = false;
        if (! String.valueOf(episodeNumber).equals(episodeIndex)) {
            LogHelper.e(TAG, "markEpisodeAsHeardAndIncrementPlayCount: The episodeNumber="+episodeNumber+" and episodeIndex "+episodeIndex+" DONT MATCH");
            matchError = true;
        }

        // UPDATE SQLITE - mark SQLite config episode as "HEARD" and increment "PLAY COUNT" - Also send record to Firebase
        ConfigEpisodesContentValues existing = getConfigForEpisode(episodeIndex);
        long listenCount = 0;
        if (existing != null) {
            ContentValues configEpisode = existing.values();
            configEpisode.put(ConfigEpisodesEntry.FIELD_EPISODE_HEARD, true);
            listenCount = configEpisode.getAsLong(ConfigEpisodesColumns.FIELD_LISTEN_COUNT);
            ++listenCount;
            configEpisode.put(ConfigEpisodesEntry.FIELD_LISTEN_COUNT, listenCount);
            updateConfigEntryValues(episodeIndex, configEpisode);
            updateFirebaseConfigEntryValues(episodeIndex, configEpisode, duration);
            LogHelper.d(TAG, "markEpisodeAsHeardAndIncrementPlayCount: new LISTEN-COUNT="+listenCount+" for Episode "+episodeIndex+(matchError ? " *** MATCH ERROR EPISODE="+episodeNumber : ""));
        }
        else {
            LogHelper.e(TAG, "*** SQLITE FAILURE - unable to getConfigForEpisode="+episodeIndex+(matchError ? " *** MATCH ERROR EPISODE="+episodeNumber : ""));
        }

        // send Analytics record to Firebase for Episode+Heard+Count
        trackWithFirebaseAnalytics(episodeIndex, duration, "mark HEARD + playcount="+listenCount);
    }

    public void markEpisodeAs_NOT_Heard(long episodeNumber, String episodeIndex, long duration) {
        LogHelper.v(TAG, "markEpisodeAs_NOT_Heard: episodeNumber="+episodeNumber+", episodeIndex="+episodeIndex+", duration="+duration);
        boolean matchError = false;
        if (! String.valueOf(episodeNumber).equals(episodeIndex)) {
            LogHelper.e(TAG, "markEpisodeAs_NOT_Heard: The episodeNumber="+episodeNumber+" and episodeIndex "+episodeIndex+" DONT MATCH");
            matchError = true;
        }

        // UPDATE SQLITE - mark SQLite config episode as "NOT HEARD" - Also send record to Firebase
        ConfigEpisodesContentValues existing = getConfigForEpisode(episodeIndex);
        if (existing != null) {
            ContentValues configEpisode = existing.values();
            configEpisode.put(ConfigEpisodesEntry.FIELD_EPISODE_HEARD, false);
            updateConfigEntryValues(episodeIndex, configEpisode);
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
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, mEpisodeTitle);
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
        // FIXME: ensure the Firebase user is logged-in
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
