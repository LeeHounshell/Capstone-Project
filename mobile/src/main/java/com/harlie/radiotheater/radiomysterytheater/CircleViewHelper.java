package com.harlie.radiotheater.radiomysterytheater;

import android.os.Handler;

public class CircleViewHelper {
    private static BaseActivity sBaseActivity = null;

    private static void init(BaseActivity activity) {
        sBaseActivity = activity;
    }

    public static void onDestroy() {
        sBaseActivity = null;
    }

    public static void showCircleView(BaseActivity activity) {
        init(activity);
        if (sBaseActivity != null) {
            Handler handler = sBaseActivity.getHandler();
            if (handler != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        sBaseActivity.showCircleView();
                    }
                });
            }
        }
    }

    public static void initializeCircleViewValue(final float circleMax, BaseActivity activity) {
        init(activity);
        if (sBaseActivity != null) {
            Handler handler = sBaseActivity.getHandler();
            if (handler != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        sBaseActivity.initializeCircleViewValue(circleMax);
                    }
                });
            }
        }
    }

    public static void setCircleViewValue(final float value, BaseActivity activity) {
        init(activity);
        if (sBaseActivity != null) {
            Handler handler = sBaseActivity.getHandler();
            if (handler != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        sBaseActivity.setCircleViewValue(value);
                    }
                });
            }
        }
    }

    public static void hideCircleView(BaseActivity activity) {
        init(activity);
        if (sBaseActivity != null) {
            Handler handler = sBaseActivity.getHandler();
            if (handler != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        sBaseActivity.hideCircleView();
                    }
                });
            }
        }
    }

}
