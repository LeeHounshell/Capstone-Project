package com.harlie.radiotheater.radiomysterytheater;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

public class RadioTheaterApplication extends android.support.multidex.MultiDexApplication {
    private final static String TAG = "LEE: <" + RadioTheaterApplication.class.getSimpleName() + ">";

    private static Context applicationContext;
    private static RadioTheaterApplication sInstance;

    @Override
    public void onCreate() {
        Log.v(TAG, "onCreate");
        sInstance = this;
        applicationContext = this.getApplicationContext();
        super.onCreate();
        Fabric.with(this, new Crashlytics());
    }

    public static synchronized RadioTheaterApplication getInstance() {
        Log.v(TAG, "getInstance");
        return RadioTheaterApplication.sInstance;
    }

    public static Context getRadioTheaterApplicationContext() {
        return applicationContext;
    }

    @Override
    public void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

}

