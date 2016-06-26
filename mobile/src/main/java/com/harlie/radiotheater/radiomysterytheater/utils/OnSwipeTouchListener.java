// from: http://stackoverflow.com/questions/19538747/how-to-use-both-ontouch-and-onclick-for-an-imagebutton
// modified by Lee Hounshell

package com.harlie.radiotheater.radiomysterytheater.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.AppCompatButton;
import android.view.GestureDetector;
import android.support.v4.view.GestureDetectorCompat ;
import android.view.MotionEvent;
import android.view.View;

public class OnSwipeTouchListener implements View.OnTouchListener {
    private final static String TAG = "LEE: <" + OnSwipeTouchListener.class.getSimpleName() + ">";

    private GestureDetectorCompat mGestureDetectorCompat;
    private AppCompatButton mCompatButton;
    private FloatingActionButton mFloatingButton;
    private Handler mHandler;
    private Drawable mNotPressedDrawable;
    private Drawable mPressedDrawable;
    private boolean onLongPress;

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
        LogHelper.v(TAG, "onTouch");
        boolean rc = mGestureDetectorCompat.onTouchEvent(motionEvent);
        if (mHandler != null) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    onLongPress = false;
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
                    break;
                }
                case MotionEvent.ACTION_UP: {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            LogHelper.v(TAG, "onTouch - ACTION_UP");
                            if (mCompatButton != null) {
                                // put back the original button image
                                mCompatButton.setBackgroundDrawable(mNotPressedDrawable);
                            }
                            if (onLongPress) {
                                LogHelper.v(TAG, "onTouch - onLongClick");
                                onLongClick(mNotPressedDrawable);
                            }
                            else {
                                LogHelper.v(TAG, "onTouch - onClick");
                                onClick();
                            }
                        }
                    });
                    break;
                }
                case MotionEvent.ACTION_CANCEL: {
                    onLongPress = false;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            LogHelper.v(TAG, "onTouch - ACTION_CANCEL");
                            if (mCompatButton != null) {
                                // put back the original button image
                                mCompatButton.setBackgroundDrawable(mNotPressedDrawable);
                            }
                        }
                    });
                    break;
                }
            }
        }
        return rc;
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 200;
        private static final int SWIPE_VELOCITY_THRESHOLD = 200;

        @Override
        public boolean onDown(MotionEvent e) {
            LogHelper.v(TAG, "onDown");
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            LogHelper.v(TAG, "onDoubleTap");
            onDoubleClick();
            return super.onDoubleTap(e);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            LogHelper.v(TAG, "onLongPress");
            onLongPress = true;
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
    }

    public void onSwipeLeft() {
    }

    public void onSwipeUp() {
    }

    public void onSwipeDown() {
    }

    public void onClick() {
    }

    public void onDoubleClick() {
    }

    public void onLongClick(Drawable image) {
    }

}

