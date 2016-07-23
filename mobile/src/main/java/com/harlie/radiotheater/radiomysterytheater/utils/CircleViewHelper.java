package com.harlie.radiotheater.radiomysterytheater.utils;

import android.os.Handler;

import com.harlie.radiotheater.radiomysterytheater.BaseActivity;

import at.grabner.circleprogress.CircleProgressView;

public class CircleViewHelper {

    public enum CircleViewType {
        CREATE_DATABASE, PLAY_EPISODE, DOWNLOAD_EPISODE
    }

    public static void showCircleView(final BaseActivity activity, final CircleProgressView circleProgressView, final CircleViewHelper.CircleViewType whatToDo) {
        if (activity != null) {
            Handler handler = activity.getHandler();
            if (handler != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        activity.initCircleView(circleProgressView, whatToDo);
                        activity.showCircleView();
                        BaseActivity.sProgressViewSpinning = true;
                    }
                });
            }
        }
    }

    public static void setCircleViewMaximum(final float circleMax, final BaseActivity activity) {
        if (activity != null) {
            Handler handler = activity.getHandler();
            if (handler != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        activity.setCircleViewMaximum(circleMax);
                    }
                });
            }
        }
    }

    public static void setCircleViewValue(final float value, final BaseActivity activity) {
        if (activity != null) {
            Handler handler = activity.getHandler();
            if (handler != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        activity.setCircleViewValue(value);
                        BaseActivity.sProgressViewSpinning = true;
                    }
                });
            }
        }
    }

    public static void hideCircleView(final BaseActivity activity) {
        if (activity != null) {
            Handler handler = activity.getHandler();
            if (handler != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        activity.hideCircleView();
                        BaseActivity.sProgressViewSpinning = false;
                    }
                });
            }
        }
    }

}
