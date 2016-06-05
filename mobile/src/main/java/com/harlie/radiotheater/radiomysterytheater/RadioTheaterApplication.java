package com.harlie.radiotheater.radiomysterytheater;

import android.content.Context;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

public class RadioTheaterApplication extends android.support.multidex.MultiDexApplication {
    private final static String TAG = "LEE: <" + RadioTheaterApplication.class.getSimpleName() + ">";

    private static Context applicationContext;

    @Override
    public void onCreate() {
        Log.v(TAG, "onCreate");
        applicationContext = this;
        super.onCreate();
        Fabric.with(this, new Crashlytics());
    }

    public static Context getRadioTheaterApplicationContext() {
        return applicationContext;
    }

}
