package com.harlie.radiotheater.radiomysterytheater.data_helper;

import android.os.AsyncTask;
import android.support.v7.widget.AppCompatButton;
import android.view.View;

import com.harlie.radiotheater.radiomysterytheater.AutoplayActivity;
import com.harlie.radiotheater.radiomysterytheater.utils.CircleViewHelper;
import com.harlie.radiotheater.radiomysterytheater.utils.CircularSeekBar;
import com.harlie.radiotheater.radiomysterytheater.utils.LogHelper;

import at.grabner.circleprogress.CircleProgressView;

public class LoadingAsyncTask extends AsyncTask<AutoplayActivity, Void, Boolean> {
    private final static String TAG = "LEE: <" + LoadingAsyncTask.class.getSimpleName() + ">";

    public static volatile boolean sLoadingNow;
    public static volatile boolean sDoneLoading;

    private static int mCount;
    private final static int sMaxCount = 100;

    private AutoplayActivity mActivity;
    private CircleProgressView mCircleProgressView;
    private CircularSeekBar mCircularSeekBar;

    public LoadingAsyncTask(AutoplayActivity activity, CircleProgressView circleProgressView, CircularSeekBar circularSeekBar) {
        LogHelper.v(TAG, "new LoadingAsyncTask");
        this.mActivity = activity;
        this.mCircleProgressView = circleProgressView;
        this.mCircularSeekBar = circularSeekBar;
        mCount = 0;
        sDoneLoading = false;
    }

    @Override
    protected void onPreExecute() {
        LogHelper.v(TAG, "onPreExecute");
        CircleViewHelper.showCircleView(mActivity, mCircleProgressView, CircleViewHelper.CircleViewType.PLAY_EPISODE);
        CircleViewHelper.setCircleViewMaximum((float) sMaxCount, mActivity);
        CircleViewHelper.setCircleViewValue((float) mCount, mActivity);
        mCircleProgressView.setVisibility(View.VISIBLE);
    }

    @Override
    protected Boolean doInBackground(AutoplayActivity... params) {
        LogHelper.v(TAG, "doInBackground");
        Boolean rc = false;
        while (rc == false) {
            ++mCount;
            CircleViewHelper.setCircleViewValue((float) mCount, mActivity);
            if (mCount == 1) {
                mActivity.initiateLoadingTask(mActivity);
            }
            try {
                Thread.sleep(1000);
            }
            catch (InterruptedException e) { }
            if (mCount == sMaxCount || sDoneLoading) {
                rc = true;
            }
        }
        return rc;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        LogHelper.v(TAG, "onPostExecute: success="+success);
        super.onPostExecute(success);
        mCircleProgressView.stopSpinning();
        mCircleProgressView.setVisibility(View.INVISIBLE);
    }

}
