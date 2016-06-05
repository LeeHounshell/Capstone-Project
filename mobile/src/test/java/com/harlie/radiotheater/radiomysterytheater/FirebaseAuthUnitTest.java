package com.harlie.radiotheater.radiomysterytheater;

import android.content.Context;
import android.support.annotation.NonNull;
import android.test.AndroidTestCase;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

// from: http://www.it1me.com/it-answers?id=36809852&ttl=Unit+Testing+with+Firebase
public class FirebaseAuthUnitTest extends AndroidTestCase {
    private final static String TAG = "LEE: <" + FirebaseAuthUnitTest.class.getSimpleName() + ">";

    private CountDownLatch authSignal = null;
    private FirebaseAuth mAuth;
    private GoogleApiClient mGoogleApiClient;

    @Override
    public void setUp() throws InterruptedException {
        authSignal = new CountDownLatch(1);

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        Context context = RadioTheaterApplication.getRadioTheaterApplicationContext();
        if (gso != null && context != null) {
            // Build a GoogleApiClient with access to the Google Sign-In API and the
            // options specified by gso.
            mGoogleApiClient = new GoogleApiClient.Builder(RadioTheaterApplication.getRadioTheaterApplicationContext())
//                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build();
        }

        try {
            mAuth = FirebaseAuth.getInstance();
        }
        catch (IllegalStateException e) {
            Log.w(TAG, "known bug with FirebaseAuth - IllegalStateException found.");
            mAuth = null;
            return;
        }

        if (mAuth.getCurrentUser() == null) {
            mAuth.signInWithEmailAndPassword("lee.hounshell@gmail.com", "123456789").addOnCompleteListener(
                    new OnCompleteListener<AuthResult>() {

                        @Override
                        public void onComplete(@NonNull final Task<AuthResult> task) {

                            final AuthResult result = task.getResult();
                            final FirebaseUser user = result.getUser();
                            authSignal.countDown();
                        }
                    });
        } else {
            authSignal.countDown();
        }
        authSignal.await(10, TimeUnit.SECONDS);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        if (mAuth != null) {
            mAuth.signOut();
            mAuth = null;
        }
    }

    @Test
    public void testWrite() throws InterruptedException {
        final CountDownLatch writeSignal = new CountDownLatch(1);

        FirebaseDatabase database;
        try {
            database = FirebaseDatabase.getInstance();
        }
        catch (IllegalStateException e) {
            Log.w(TAG, "known bug with FirebaseDatabase - IllegalStateException found.");
            mAuth = null;
            return;
        }

        DatabaseReference myRef = database.getReference("message");

        myRef.setValue("Do you have data? You'll love Firebase. - 3")
                .addOnCompleteListener(new OnCompleteListener<Void>() {

                    @Override
                    public void onComplete(@NonNull final Task<Void> task) {
                        writeSignal.countDown();
                    }
                });

        writeSignal.await(10, TimeUnit.SECONDS);
    }

}
