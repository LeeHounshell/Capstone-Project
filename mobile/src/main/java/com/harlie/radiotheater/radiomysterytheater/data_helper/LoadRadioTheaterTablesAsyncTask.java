package com.harlie.radiotheater.radiomysterytheater.data_helper;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;

import com.harlie.radiotheater.radiomysterytheater.BaseActivity;

import at.grabner.circleprogress.CircleProgressView;

public class LoadRadioTheaterTablesAsyncTask extends AsyncTask<BaseActivity, Void, Boolean> {
    private final static String TAG = "LEE: <" + LoadRadioTheaterTablesAsyncTask.class.getSimpleName() + ">";

    BaseActivity activity;
    CircleProgressView circleProgressView;

    public LoadRadioTheaterTablesAsyncTask(BaseActivity activity, CircleProgressView circleProgressView) {
        Log.v(TAG, "new LoadRadioTheaterTablesAsyncTask");
        this.activity = activity;
        this.circleProgressView = circleProgressView;
    }

    @Override
    protected void onPreExecute() {
        Log.v(TAG, "onPreExecute");
        super.onPreExecute();
        circleProgressView.setVisibility(View.VISIBLE);
    }

    @Override
    protected Boolean doInBackground(BaseActivity... params) {
        Log.v(TAG, "doInBackground");
        for (int i = 0; i < 10; ++i) {
            SystemClock.sleep(1000); // FIXME
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean successTablesLoaded) {
        Log.v(TAG, "onPostExecute");
        super.onPostExecute(successTablesLoaded);
        if (successTablesLoaded) {
            Log.v(TAG, "---> data loaded ok.");
            activity.startAutoplayActivity();
        }
        else {
            Log.v(TAG, "---> data failed to load.");
            activity.startAuthenticationActivity();
        }
    }

}
