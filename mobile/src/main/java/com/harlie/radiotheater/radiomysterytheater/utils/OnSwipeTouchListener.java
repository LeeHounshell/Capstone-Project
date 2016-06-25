// from: http://stackoverflow.com/questions/19538747/how-to-use-both-ontouch-and-onclick-for-an-imagebutton

package com.harlie.radiotheater.radiomysterytheater.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatButton;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class OnSwipeTouchListener implements View.OnTouchListener {
    private final static String TAG = "LEE: <" + OnSwipeTouchListener.class.getSimpleName() + ">";

    private GestureDetector mGestureDetector;
    private AppCompatButton mAutoPlay;
    private Drawable mPressedButton;
    private boolean mDoLongPress;

    public OnSwipeTouchListener(Context c) {
        LogHelper.v(TAG, "OnSwipeTouchListener");
        mPressedButton = null;
        mGestureDetector = new GestureDetector(c, new GestureListener());
    }

    public OnSwipeTouchListener(Context c, AppCompatButton autoPlay, Drawable pressed) {
        LogHelper.v(TAG, "OnSwipeTouchListener - AUTOPLAY");
        mAutoPlay = autoPlay;
        mPressedButton = pressed;
        mGestureDetector = new GestureDetector(c, new GestureListener());
    }

    public boolean onTouch(final View view, final MotionEvent motionEvent) {
        LogHelper.v(TAG, "onTouch");
        if (mPressedButton != null) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    LogHelper.v(TAG, "onTouch - ACTION_DOWN");
                    mAutoPlay.setBackgroundDrawable(mPressedButton);
                    break;
                }
                case MotionEvent.ACTION_UP: {
                    LogHelper.v(TAG, "onTouch - ACTION_UP");
                    if (!mDoLongPress) {
                        mAutoPlay.setVisibility(View.INVISIBLE);
                    }
                    mDoLongPress = false;
                    break;
                }
            }
        }
        return mGestureDetector.onTouchEvent(motionEvent);
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent e) {
            LogHelper.v(TAG, "onDown");
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            LogHelper.v(TAG, "onSingleTapUp");
            onClick();
            return super.onSingleTapUp(e);
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
            mDoLongPress = true;
            onLongClick();
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

    public void onLongClick() {
    }

}

