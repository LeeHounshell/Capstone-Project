package com.harlie.radiotheater.radiomysterytheater;

import android.util.Log;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

public class RadioTheaterApplication extends android.support.multidex.MultiDexApplication {
    private final static String TAG = "LEE: <" + RadioTheaterApplication.class.getSimpleName() + ">";

    @Override
    public void onCreate() {
        Log.v(TAG, "onCreate");
        super.onCreate();
        Fabric.with(this, new Crashlytics());
    }
}
