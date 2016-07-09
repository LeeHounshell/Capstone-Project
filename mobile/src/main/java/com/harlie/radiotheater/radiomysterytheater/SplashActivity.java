package com.harlie.radiotheater.radiomysterytheater;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import com.harlie.radiotheater.radiomysterytheater.utils.LogHelper;

public class SplashActivity extends AppCompatActivity
{
    private final static String TAG = "LEE: <" + SplashActivity.class.getSimpleName() + ">";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(RadioTheaterApplication.getRadioTheaterApplicationContext());
        String email = sharedPreferences.getString("userEmail", "");
        String authenticated = sharedPreferences.getString("authentication", "");
        if (authenticated.length() > 0 && authenticated.equals(email)) {
            LogHelper.v(TAG, "Authenticated.");
            Intent intent = new Intent(this, AutoplayActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        else {
            LogHelper.v(TAG, "Need to Authenticate.");
            Intent intent = new Intent(this, AuthenticationActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        finish();
    }

}
