package com.harlie.radiotheater.radiomysterytheater.data_helper;

import android.os.AsyncTask;
import android.support.v7.widget.AppCompatButton;
import android.view.View;

import com.harlie.radiotheater.radiomysterytheater.AutoplayActivity;
import com.harlie.radiotheater.radiomysterytheater.CircleViewHelper;
import com.harlie.radiotheater.radiomysterytheater.CircularSeekBar;
import com.harlie.radiotheater.radiomysterytheater.utils.LogHelper;

import at.grabner.circleprogress.CircleProgressView;

public class LoadingAsyncTask extends AsyncTask<AutoplayActivity, Void, Boolean> {
    private final static String TAG = "LEE: <" + LoadingAsyncTask.class.getSimpleName() + ">";

    public static volatile boolean mLoadingNow;
    public static volatile boolean mDoneLoading;

    private static int mCount;
    private final static int sMaxCount = 100;

    private AutoplayActivity mActivity;
    private AppCompatButton mAutoPlay;
    private CircleProgressView mCircleProgressView;
    private CircularSeekBar mCircularSeekBar;

    public LoadingAsyncTask(AutoplayActivity activity, CircleProgressView circleProgressView, CircularSeekBar circularSeekBar, AppCompatButton autoPlay) {
        LogHelper.v(TAG, "new LoadingAsyncTask");
        mLoadingNow = true;
        this.mActivity = activity;
        this.mAutoPlay = autoPlay;
        this.mCircleProgressView = circleProgressView;
        this.mCircularSeekBar = circularSeekBar;
        mCount = 0;
        mDoneLoading = false;
        mActivity.setAutoPlayVisibility(View.INVISIBLE, "LoadingAsyncTask");
        mCircularSeekBar.setVisibility(View.INVISIBLE);
        mCircleProgressView.setVisibility(View.INVISIBLE);
        // get the buttons to disappear before the progress view appears.
        mAutoPlay.getHandler().post(new Runnable() {
            @Override
            public void run() {
                mAutoPlay.setVisibility(View.INVISIBLE);
                mCircularSeekBar.setVisibility(View.INVISIBLE);
            }
        });
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
            try {
                Thread.sleep(1000);
            }
            catch (InterruptedException e) { }
            if (mCount == sMaxCount || mDoneLoading) {
                rc = true;
            }
        }
        return rc;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        LogHelper.v(TAG, "onPostExecute: success="+success);
        super.onPostExecute(success);
        mLoadingNow = false;
        mCircleProgressView.stopSpinning();
        mCircleProgressView.setVisibility(View.INVISIBLE);
        mActivity.getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mActivity.setAutoPlayVisibility(View.VISIBLE, "onPostExecute");
            }
        }, 1000);
    }

}
