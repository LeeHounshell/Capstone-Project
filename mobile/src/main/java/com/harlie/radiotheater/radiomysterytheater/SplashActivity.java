package com.harlie.radiotheater.radiomysterytheater;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.transition.Transition;

import com.harlie.radiotheater.radiomysterytheater.utils.LogHelper;

public class SplashActivity extends AppCompatActivity
{
    private final static String TAG = "LEE: <" + SplashActivity.class.getSimpleName() + ">";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (intent != null && getIntent().getStringExtra("EXIT_NOW") != null) {
            LogHelper.v(TAG, "exiting.");
            finish();
            return;
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(RadioTheaterApplication.getRadioTheaterApplicationContext());
        String email = sharedPreferences.getString("userEmail", "");
        String authenticated = sharedPreferences.getString("authentication", "");
        if (authenticated.length() > 0 && authenticated.equals(email)) {
            LogHelper.v(TAG, "Authenticated.");
            Intent autoplayIntent = new Intent(this, AutoplayActivity.class);
            autoplayIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            // setup the primary app-startup Transition effect
            LogHelper.v(TAG, "Transitioning using animation..");
            Transition exitTransition = new android.transition.Fade();
            getWindow().setExitTransition(exitTransition);
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this);
            Bundle bundle = options.toBundle();
            LogHelper.v(TAG, "STARTACTIVITY: AutoplayActivity.class");
            startActivity(autoplayIntent, bundle);
        }
        else {
            LogHelper.v(TAG, "Need to Authenticate.");
            Intent authenticationIntent = new Intent(this, AuthenticationActivity.class);
            authenticationIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            authenticationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            LogHelper.v(TAG, "STARTACTIVITY: AuthenticationActivity.class");
            startActivity(authenticationIntent);
        }
        finish();
    }

}
