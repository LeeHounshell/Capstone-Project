// from: http://stackoverflow.com/questions/19538747/how-to-use-both-ontouch-and-onclick-for-an-imagebutton
// modified by Lee Hounshell

package com.harlie.radiotheater.radiomysterytheater.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.AppCompatButton;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;

public class OnSwipeTouchListener implements View.OnTouchListener {
    private final static String TAG = "LEE: <" + OnSwipeTouchListener.class.getSimpleName() + ">";

    private final static long THREE_SECONDS = (3 * 1000);

    private GestureDetectorCompat mGestureDetectorCompat;
    private AppCompatButton mCompatButton;
    private FloatingActionButton mFloatingButton;
    private Handler mHandler;
    private Drawable mNotPressedDrawable;
    private Drawable mPressedDrawable;
    private Timer mDownTimer = new Timer();

    private static volatile boolean onDownClick;
    private static volatile boolean onLongClick;
    private static volatile boolean onDoubleClick;
    private static volatile boolean onSwipeLeft;
    private static volatile boolean onSwipeRight;
    private static volatile boolean onSwipeUp;
    private static volatile boolean onSwipeDown;

    private static volatile long sDownPressTime;
    private static volatile boolean sIgnoreUpPressUntilDown;

    public static void reset() {
        LogHelper.v(TAG, "reset");
        sDownPressTime = 0;
        sIgnoreUpPressUntilDown = false;
        clearInput();
    }

    public OnSwipeTouchListener(Context context, Handler handler, AppCompatButton button, Drawable pressed) {
        LogHelper.v(TAG, "OnSwipeTouchListener - BUTTON");
        mHandler = handler;
        mCompatButton = button;
        mFloatingButton = null;
        mPressedDrawable = pressed;
        mNotPressedDrawable = null;
        mGestureDetectorCompat = new GestureDetectorCompat(context, new GestureListener(), mHandler);
    }

    public OnSwipeTouchListener(Context context, Handler handler, FloatingActionButton button) {
        LogHelper.v(TAG, "OnSwipeTouchListener - BUTTON");
        mHandler = handler;
        mCompatButton = null;
        mFloatingButton = button;
        mPressedDrawable = null;
        mNotPressedDrawable = null;
        mGestureDetectorCompat = new GestureDetectorCompat(context, new GestureListener(), mHandler);
    }

    public boolean onTouch(final View view, final MotionEvent motionEvent) {
        boolean rc = mGestureDetectorCompat.onTouchEvent(motionEvent);
        if (mHandler != null) {
            switch (motionEvent.getAction()) {

                case MotionEvent.ACTION_DOWN: {
                    sDownPressTime = System.currentTimeMillis();
                    clearInput();
                    onDownClick = true;
                    if (mPressedDrawable != null) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                LogHelper.v(TAG, "onTouch - ACTION_DOWN");
                                if (mCompatButton != null) {
                                    mNotPressedDrawable = mCompatButton.getCompoundDrawables()[0]; // get original button image, as of right now.
                                    mCompatButton.setBackgroundDrawable(mPressedDrawable);
                                }
                            }
                        });
                    }

                    mDownTimer = new Timer();
                    mDownTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            long time = System.currentTimeMillis();
                            if (sDownPressTime < (time - THREE_SECONDS)) {
                                sIgnoreUpPressUntilDown = true;
                                clearInput();
                                if (mHandler != null) {
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            onActionUp(); // automatic ACTION_UP after 3 seconds and onClick()
                                        }
                                    });
                                }
                            }
                        }
                    }, THREE_SECONDS);
                    break;
                }

                case MotionEvent.ACTION_UP: {
                    mDownTimer.cancel();
                    if (!onDownClick) {
                        break;
                    }
                    onDownClick = false;
                    if (sIgnoreUpPressUntilDown) {
                        sIgnoreUpPressUntilDown = false;
                    }
                    else {
                        if (mHandler != null){
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    onActionUp();
                                }
                            });
                        }
                    }
                    break;
                }

                case MotionEvent.ACTION_CANCEL: {
//                    mDownTimer.cancel();
//                    onLongClick = false;
//                    if (mHandler != null) {
//                        mHandler.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                LogHelper.v(TAG, "onTouch - ACTION_CANCEL");
//                                if (mCompatButton != null) {
//                                    // put back the original button image
//                                    mCompatButton.setBackgroundDrawable(mNotPressedDrawable);
//                                }
//                            }
//                        });
//                    }
                    break;
                }
            }
        }
        return rc;
    }

    private static void clearInput() {
        onDownClick = false;
        onLongClick = false;
        onDoubleClick = false;
        onSwipeLeft = false;
        onSwipeRight = false;
        onSwipeUp = false;
        onSwipeDown = false;
    }

    private void onActionUp() {
        LogHelper.v(TAG, "onTouch - ACTION_UP");
        if (mCompatButton != null) {
            // put back the original button image
            mCompatButton.setBackgroundDrawable(mNotPressedDrawable);
        }
        if (onLongClick) {
            LogHelper.v(TAG, "onTouch - onLongClick");
            onLongClick(mNotPressedDrawable);
        } else if (onDoubleClick) {
            LogHelper.v(TAG, "onTouch - onDoubleClick");
            onDoubleClick();
        } else if (onSwipeRight) {
            LogHelper.v(TAG, "onTouch - SWIPE - onSwipeRight");
            onSwipeRight();
        } else if (onSwipeLeft) {
            LogHelper.v(TAG, "onTouch - SWIPE - onSwipeLeft");
            onSwipeLeft();
        } else if (onSwipeDown) {
            LogHelper.v(TAG, "onTouch - SWIPE - onSwipeDown");
            onSwipeDown();
        } else if (onSwipeUp) {
            LogHelper.v(TAG, "onTouch - SWIPE - onSwipeUp");
            onSwipeUp();
        } else {
            LogHelper.v(TAG, "onTouch - onClick");
            onClick();
        }
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private final String TAG = "LEE: <" + GestureListener.class.getSimpleName() + ">";

        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent e) {
            LogHelper.v(TAG, "onDown");
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) { // FIXME
            LogHelper.v(TAG, "onDoubleTap");
            onDoubleClick();
            return super.onDoubleTap(e);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            LogHelper.v(TAG, "onLongPress");
            onLongClick = true;
            super.onLongPress(e);
        }

        // Determines the fling velocity and then fires the appropriate swipe event accordingly
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight();
                        } else {
                            onSwipeLeft();
                        }
                    }
                } else {
                    if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            onSwipeDown();
                        } else {
                            onSwipeUp();
                        }
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return result;
        }
    }

    public void onSwipeRight() {
        LogHelper.v(TAG, "onSwipeRight");
        onSwipeRight = true;
    }

    public void onSwipeLeft() {
        LogHelper.v(TAG, "onSwipeLeft");
        onSwipeLeft = true;
    }

    public void onSwipeUp() {
        LogHelper.v(TAG, "onSwipeUp");
        onSwipeUp = true;
    }

    public void onSwipeDown() {
        LogHelper.v(TAG, "onSwipeDown");
        onSwipeDown = true;
    }

    public void onClick() {
        LogHelper.v(TAG, "onClick");
    }

    public void onDoubleClick() {
        LogHelper.v(TAG, "onDoubleClick");
        onDoubleClick = true;
    }

    public void onLongClick(Drawable image) {
        LogHelper.v(TAG, "onLongClick");
        onLongClick = true;
    }

}

