package com.harlie.radiotheater.radiomysterytheater;

import android.os.Handler;

public class CircleViewHelper {

    public static void showCircleView(final BaseActivity activity) {
        if (activity != null) {
            Handler handler = activity.getHandler();
            if (handler != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        activity.showCircleView();
                    }
                });
            }
        }
    }

    public static void initializeCircleViewValue(final float circleMax, final BaseActivity activity) {
        if (activity != null) {
            Handler handler = activity.getHandler();
            if (handler != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        activity.initializeCircleViewValue(circleMax);
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
                    }
                });
            }
        }
    }

}
