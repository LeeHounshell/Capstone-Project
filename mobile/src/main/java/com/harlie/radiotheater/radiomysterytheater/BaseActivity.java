package com.harlie.radiotheater.radiomysterytheater;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.harlie.radiotheater.radiomysterytheater.data_helper.LoadRadioTheaterTablesAsyncTask;
import com.harlie.radiotheater.radiomysterytheater.data_helper.RadioTheaterContract;

import at.grabner.circleprogress.CircleProgressView;

@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity {
    private final static String TAG = "LEE: <" + BaseActivity.class.getSimpleName() + ">";

    public FirebaseAuth mAuth;

    protected static final int MIN_EMAIL_LENGTH = 3;
    protected static final int MIN_PASSWORD_LENGTH = 6;
    protected ProgressDialog mProgressDialog;

    private String email;
    private String pass;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.v(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        Firebase.setAndroidContext(this);
    }

    @Override
    public void onDestroy() {
        Log.v(TAG, "onDestroy");
        super.onDestroy();
        hideProgressDialog();
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

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }
        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
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
                                if (task.getException() instanceof com.google.firebase.auth.FirebaseAuthInvalidCredentialsException) {
                                    Log.v(TAG, "*** FAIL - we have a com.google.firebase.auth.FirebaseAuthInvalidCredentialsException ***");
                                    String message = getResources().getString(R.string.invalid_email);
                                    Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
                                    startAuthenticationActivity();
                                }
                                else if (task.getException() instanceof com.google.firebase.auth.FirebaseAuthUserCollisionException) {
                                    Log.v(TAG, "*** OK - we just have a com.google.firebase.auth.FirebaseAuthUserCollisionException ***");
                                    success = true;
                                }
                                else if (task.getException() instanceof com.google.firebase.FirebaseTooManyRequestsException) {
                                    Log.v(TAG, "*** FAIL - we just have a com.google.firebase.FirebaseTooManyRequestsException ***");
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

    protected void loadSqliteDatabase() {
        Log.v(TAG, "*** loadSqliteDatabase ***");
        // Get a reference to our posts
        Firebase ref = new Firebase("https://console.firebase.google.com/project/radio-mystery-theater/database/data/radiomysterytheater");
        final BaseActivity activity = this;
        // Attach an listener to read the data at our posts reference
        Log.v(TAG, "*** FIREBASE REQUEST ***");
        ref.addValueEventListener(new com.firebase.client.ValueEventListener() {

            @Override
            public void onDataChange(com.firebase.client.DataSnapshot dataSnapshot) {
                Log.v(TAG, "*** -------------------------------------------------------------------------------- ***");
                Log.v(TAG, "snapshot="+dataSnapshot.getValue());
                CircleProgressView circleProgressView = (CircleProgressView) findViewById(R.id.circle_view);
                LoadRadioTheaterTablesAsyncTask asyncTask = new LoadRadioTheaterTablesAsyncTask(activity, circleProgressView, dataSnapshot);
                asyncTask.execute();
                Log.v(TAG, "*** -------------------------------------------------------------------------------- ***");
            }

            @Override
            public void onCancelled(FirebaseError databaseError) {
                Log.e(TAG, "The read failed: " + databaseError.getDetails());
                startAuthenticationActivity();
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

}