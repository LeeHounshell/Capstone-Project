package com.harlie.radiotheater.radiomysterytheater;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.harlie.radiotheater.radiomysterytheater.data_helper.LoadRadioTheaterTablesAsyncTask;
import com.harlie.radiotheater.radiomysterytheater.data_helper.RadioTheaterContract;

import at.grabner.circleprogress.AnimationState;
import at.grabner.circleprogress.AnimationStateChangedListener;
import at.grabner.circleprogress.CircleProgressView;
import at.grabner.circleprogress.TextMode;

@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity {
    private final static String TAG = "LEE: <" + BaseActivity.class.getSimpleName() + ">";

    protected static final String FIREBASE_WRITERS_URL = "radiomysterytheater/0/writers";
    protected static final String FIREBASE_ACTORS_URL = "radiomysterytheater/1/actors";
    protected static final String FIREBASE_SHOWS_URL = "radiomysterytheater/2/shows";

    private FirebaseAuth mAuth;
    private Firebase mFirebase;
    private DatabaseReference mDatabase;
    private Handler mHandler;
    private View mRootView;
    private CircleProgressView mCircleView;
    private boolean mShowUnit;

    protected static final int MIN_EMAIL_LENGTH = 3;
    protected static final int MIN_PASSWORD_LENGTH = 6;

    private String email;
    private String pass;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.v(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        mRootView = findViewById(android.R.id.content);
        mHandler = new Handler();
        Firebase.setAndroidContext(this);
        mAuth = FirebaseAuth.getInstance();
        mFirebase = new Firebase("https://radio-mystery-theater.firebaseio.com");
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public void onDestroy() {
        Log.v(TAG, "onDestroy");
        super.onDestroy();
        mAuth = null;
        mCircleView = null;
        mDatabase = null;
        mHandler = null;
        mRootView = null;
    }

    @Override
    protected void onResume() {
        Log.v(TAG, "onResume");
        super.onResume();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    public void onBackPressed() {
        Log.v(TAG, "onBackPressed");
        super.onBackPressed();
    }

    //from: http://stackoverflow.com/questions/31662416/show-collapsingtoolbarlayout-title-only-when-collapsed
    protected void configureToolbarTitleBehavior() {
        Log.v(TAG, "configureToolbarTitleBehavior");
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

    protected boolean doINeedToCreateADatabase() {
        Log.v(TAG, "doINeedToCreateADatabase");
        if ((isExistingTable("EPISODES")) && (isExistingTable("ACTORS")) && (isExistingTable("WRITERS"))) {
            Log.v(TAG, "*** Found SQLITE Tables! ***");
            return false;
        }
        Log.v(TAG, "*** NO SQLITE DATABASE FOUND! ***");
        return true;
    }

    protected boolean isValid(String email, String pass) {
        boolean result = false;
        if (email != null && email.length() > MIN_EMAIL_LENGTH && pass != null && pass.length() >= MIN_PASSWORD_LENGTH) {
            // from: http://stackoverflow.com/questions/624581/what-is-the-best-java-email-address-validation-method
            String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
            java.util.regex.Matcher m = p.matcher(email);
            result = m.matches();
            Log.v(TAG, "check isValid: result="+result+", email="+email);
        }
        return result;
    }

    protected void handleAuthenticationRequestResult(boolean loginSuccess) {
        Log.d(TAG, "handleAuthenticationRequestResult - loginSuccess=" + loginSuccess);
        if (loginSuccess) {
            changeRadioMysteryTheaterFirebaseAccount(getEmail(), getPass());
        } else {
            userLoginFailed();
            startAuthenticationActivity();
        }
    }

    protected void changeRadioMysteryTheaterFirebaseAccount(String email, String pass) {
        Log.v(TAG, "changeRadioMysteryTheaterFirebaseAccount - Firebase Login using email="+email+", and password");
        if (mAuth != null && email != null && pass != null && isValid(email, pass)) {
            final BaseActivity activity = this;
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
                                success = checkExceptionReason(task, activity);
                            }
                            if (success) {
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
        Log.v(TAG, "checkExceptionReason");
        boolean success = false;
        if (task.getException() instanceof com.google.firebase.auth.FirebaseAuthInvalidCredentialsException) {
            Log.v(TAG, "*** FAIL - com.google.firebase.auth.FirebaseAuthInvalidCredentialsException ***");
            String message = getResources().getString(R.string.invalid_email);
            Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
            startAuthenticationActivity();
        }
        else if (task.getException() instanceof com.google.firebase.auth.FirebaseAuthUserCollisionException) {
            Log.v(TAG, "*** OK - com.google.firebase.auth.FirebaseAuthUserCollisionException ***"); // user+pass record already exists, so ignore
            success = true;
        }
        else if(task.getException() instanceof com.google.firebase.auth.FirebaseAuthInvalidUserException) {
            Log.v(TAG, "*** OK - com.google.firebase.auth.FirebaseAuthInvalidUserException"); // found deleted user - so just add them back
            success = true;
        }
        else if (task.getException() instanceof com.google.firebase.FirebaseTooManyRequestsException) {
            Log.v(TAG, "*** FAIL - com.google.firebase.FirebaseTooManyRequestsException ***");
            String message = getResources().getString(R.string.too_many_requests);
            Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
            startAuthenticationActivity();
        }
        else {
            Log.v(TAG, "*** authentication failed *** reason="+task.getException().getLocalizedMessage());
            String message = getResources().getString(R.string.auth_fail);
            Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
            startAuthenticationActivity();
        }
        return success;
    }

    protected void userLoginSuccess() {
        Log.v(TAG, "userLoginSuccess");
        String message = getResources().getString(R.string.successful);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    protected void userLoginFailed() {
        Log.v(TAG, "userLoginFailed");
        String message = getResources().getString(R.string.auth_fail);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // from: http://stackoverflow.com/questions/3058909/how-does-one-check-if-a-table-exists-in-an-android-sqlite-database
    protected boolean isExistingTable(String tableName) {
        Log.v(TAG, "isExistingTable: "+tableName);
        long rowId = 1;
        Uri CONTENT_URI = null;
        String whereClause = null;
        if (tableName.toUpperCase().equals("EPISODES")) {
            CONTENT_URI = RadioTheaterContract.EpisodesEntry.buildEpisodeUri(rowId);
            tableName = RadioTheaterContract.EpisodesEntry.TABLE_NAME;
            whereClause = RadioTheaterContract.EpisodesEntry.FIELD_EPISODE_NUMBER + "=?";
        }
        else if (tableName.toUpperCase().equals("ACTORS")) {
            CONTENT_URI = RadioTheaterContract.ActorsEntry.buildActorUri(rowId);
            tableName = RadioTheaterContract.ActorsEntry.TABLE_NAME;
            whereClause = RadioTheaterContract.ActorsEntry.FIELD_ACTOR_ID + "=?";
        }
        else if (tableName.toUpperCase().equals("WRITERS")) {
            CONTENT_URI = RadioTheaterContract.WritersEntry.buildWriterUri(rowId);
            tableName = RadioTheaterContract.WritersEntry.TABLE_NAME;
            whereClause = RadioTheaterContract.WritersEntry.FIELD_WRITER_ID + "=?";
        }
        if ( CONTENT_URI == null || whereClause == null) {
            return false;
        }

        Cursor cursor = getContentResolver().query(
                CONTENT_URI, // the 'content://' Uri to query
                null,        // projection String[] - leaving "columns" null just returns all the columns.
                whereClause, // selection - SQL where
                new String[]{Long.toString(rowId)}, // selection args String[] - values for the "where" clause
                null         // sort order (String)
        );

        boolean success = false;
        if (cursor.getCount() == 0) {
            Log.v(TAG, "SQL: nothing found for table "+tableName);
        }
        else {
            Log.v(TAG, "SQL: found data in table "+tableName);
            success = true;
        }
        cursor.close();
        return success;
    }

    /*
    private void deleteFirebaseDatabase() {
        Log.v(TAG, "*** deleteFirebaseDatabase ***");
        mFirebase.child("radiomysterytheater/0").removeValue();
        mFirebase.child("radiomysterytheater/1").removeValue();
        mFirebase.child("radiomysterytheater/2").removeValue();
    }
    */

    // this kicks off a series of AsyncTasks to load SQL tables from Firebase
    protected void loadSqliteDatabase() {
        Log.v(TAG, "*** loadSqliteDatabase ***");
        initCircleView(mRootView);

        //if (! ((isExistingTable("EPISODES")) && (isExistingTable("ACTORS")) && (isExistingTable("WRITERS")))) {
        //    LoadRadioTheaterTablesAsyncTask.setTesting(true); // load some dummy data instead of JSON
        //}

        runLoadState(LoadRadioTheaterTablesAsyncTask.LoadState.WRITERS); // begin with first load state
    }

    public void runLoadState(LoadRadioTheaterTablesAsyncTask.LoadState state) {
        Log.v(TAG, "runLoadState: for state="+state);
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
        Log.v(TAG, "runLoadStateCallback: for state="+state);
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
        Log.v(TAG, "loadWritersFromFirebase");
        final BaseActivity activity = this;
        // Attach a listener to read the data initially
        Log.v(TAG, "*** FIREBASE REQUEST *** "+FIREBASE_WRITERS_URL);
        mDatabase.child(FIREBASE_WRITERS_URL).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.v(TAG, "*** -------------------------------------------------------------------------------- ***");
                LoadRadioTheaterTablesAsyncTask asyncTask = new LoadRadioTheaterTablesAsyncTask(activity, mCircleView, dataSnapshot, LoadRadioTheaterTablesAsyncTask.LoadState.WRITERS);
                asyncTask.execute();
                Log.v(TAG, "*** -------------------------------------------------------------------------------- ***");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.v(TAG, "onCancelled");
                activity.startAuthenticationActivity();
            }
        });
    }

    // next load the ACTORS tables
    public void loadActorsFromFirebase() {
        Log.v(TAG, "loadActorsFromFirebase");
        final BaseActivity activity = this;
        // Attach a listener to read the data initially
        Log.v(TAG, "*** FIREBASE REQUEST *** "+FIREBASE_ACTORS_URL);
        activity.mDatabase.child(FIREBASE_ACTORS_URL).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.v(TAG, "*** -------------------------------------------------------------------------------- ***");
                LoadRadioTheaterTablesAsyncTask asyncTask = new LoadRadioTheaterTablesAsyncTask(activity, mCircleView, dataSnapshot, LoadRadioTheaterTablesAsyncTask.LoadState.ACTORS);
                asyncTask.execute();
                Log.v(TAG, "*** -------------------------------------------------------------------------------- ***");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.v(TAG, "onCancelled");
                activity.startAuthenticationActivity();
            }
        });
    }

    // finally load the EPISODES tables
    public void loadEpisodesFromFirebase() {
        Log.v(TAG, "loadEpisodesFromFirebase");
        final BaseActivity activity = this;
        // Attach a listener to read the data initially
        Log.v(TAG, "*** FIREBASE REQUEST *** "+FIREBASE_SHOWS_URL);
        activity.mDatabase.child(FIREBASE_SHOWS_URL).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.v(TAG, "*** -------------------------------------------------------------------------------- ***");
                LoadRadioTheaterTablesAsyncTask asyncTask = new LoadRadioTheaterTablesAsyncTask(activity, mCircleView, dataSnapshot, LoadRadioTheaterTablesAsyncTask.LoadState.EPISODES);
                asyncTask.execute();
                Log.v(TAG, "*** -------------------------------------------------------------------------------- ***");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.v(TAG, "onCancelled");
                activity.startAuthenticationActivity();
            }
        });
    }

    public void startAutoplayActivity() {
        Log.v(TAG, "---> startAutoplayActivity <---");
        boolean dbMissing = doINeedToCreateADatabase();
        Log.v(TAG, "---> dbMissing="+dbMissing);
        if (dbMissing) {
            Log.v(TAG, "*** first need to build the RadioMysteryTheater database ***");
            String message = getResources().getString(R.string.initializing);
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            loadSqliteDatabase();
        }
        else {
            Log.v(TAG, "*** READY TO START RADIO MYSTERY THEATER ***");
            userLoginSuccess();
            Intent autoplayIntent = new Intent(this, AutoplayActivity.class);
            // close existing activity stack regardless of what's in there and create new root
            autoplayIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(autoplayIntent);
            finish();
        }
    }

    public void startAuthenticationActivity() {
        Log.v(TAG, "---> startAuthenticationActivity <---");
        Intent authenticationIntent = new Intent(this, AuthenticationActivity.class);
        // close existing activity stack regardless of what's in there and create new root
        authenticationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(authenticationIntent);
        finish();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
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

    //--------------------------------------------------------------------------------
    // CircleView related
    //--------------------------------------------------------------------------------
    public void initCircleView(View view) {
        Log.v(TAG, "initCircleView (View)");
        mCircleView = (CircleProgressView) view.findViewById(R.id.circle_view);
        mShowUnit = false;
    }

    public void initCircleView(CircleProgressView circleView) {
        Log.v(TAG, "initCircleView (CircleProgressView)");
        mCircleView = circleView; // needed to get around a problem with Fragments and findViewById
        mShowUnit = false;
    }

    public void showCircleView() {
        Log.v(TAG, "showCircleView");
        if (mCircleView == null) {
            Log.w(TAG, "showCircleView: CircleProgressView issue");
            return;
        }
        mCircleView.post(new Runnable() {
            @Override
            public void run() {

                mShowUnit = false;
                mCircleView.setVisibility(View.VISIBLE);
                Log.w(TAG, "showCircleView: CircleView VISIBLE");
                mCircleView.setAutoTextSize(true); // enable auto text size, previous values are overwritten
                mCircleView.setUnitScale(0.9f); // if you want the calculated text sizes to be bigger/smaller
                mCircleView.setTextScale(0.9f); // if you want the calculated text sizes to be bigger/smaller
                mCircleView.setTextColor(Color.RED);
                mCircleView.setText("Loading.."); //shows the given text in the circle view
                mCircleView.setTextMode(TextMode.TEXT); // Set text mode to text to show text
                mCircleView.spin(); // start spinning
                mCircleView.setShowTextWhileSpinning(true); // Show/hide text in spinning mode

                mCircleView.setOnAnimationStateChangedListener(

                        new AnimationStateChangedListener() {
                            @Override
                            public void onAnimationStateChanged(AnimationState _animationState) {
                                if (mCircleView != null) {
                                    switch (_animationState) {
                                        case IDLE:
                                        case ANIMATING:
                                        case START_ANIMATING_AFTER_SPINNING:
                                            mCircleView.setTextMode(TextMode.PERCENT); // show percent if not spinning
                                            mCircleView.setUnitVisible(mShowUnit);
                                            break;
                                        case SPINNING:
                                            mCircleView.setTextMode(TextMode.TEXT); // show text while spinning
                                            mCircleView.setUnitVisible(false);
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

    public void initializeCircleViewValue(float value) {
        if (mCircleView != null) {
            mShowUnit = true;
            mCircleView.setUnit("%");
            mCircleView.setUnitVisible(mShowUnit);
            mCircleView.setTextMode(TextMode.PERCENT); // Shows current percent of the current value from the max value
            mCircleView.setMaxValue(value);
            mCircleView.setValue(0);
            Log.w(TAG, "initializeCircleViewValue: CircleView MAX=" + value);
        }
    }

    public void setCircleViewValue(float value) {
        if (mCircleView != null) {
            mCircleView.setValue(value);
            Log.w(TAG, "setCircleViewValue: CircleView value=" + value);
            if (value == mCircleView.getMaxValue()) {
                hideCircleView();
            }
        }
    }

    public void hideCircleView() {
        if (mCircleView != null) {
            mCircleView.stopSpinning();
            mCircleView.setVisibility(View.GONE);
            Log.w(TAG, "hideCircleView: CircleView HIDDEN");
        }
    }

}