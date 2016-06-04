package com.harlie.radiotheater.radiomysterytheater;

import android.util.Log;

public class RadioTheaterApplication extends android.support.multidex.MultiDexApplication {
    private final static String TAG = "LEE: <" + RadioTheaterApplication.class.getSimpleName() + ">";

    @Override
    public void onCreate() {
        Log.v(TAG, "onCreate");
        super.onCreate();
    }
}
