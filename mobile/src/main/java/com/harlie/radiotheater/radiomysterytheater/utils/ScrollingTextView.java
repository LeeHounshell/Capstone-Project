// from: http://androidsnips.blogspot.com/2011/12/create-auto-scrolling-marquee-textview.html
package com.harlie.radiotheater.radiomysterytheater.utils;

import android.content.Context;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

public class ScrollingTextView extends TextView {
    private final static String TAG = "LEE: <" + ScrollingTextView.class.getSimpleName() + ">";

    public ScrollingTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setVisibility(View.VISIBLE);
        setEllipsize(TextUtils.TruncateAt.MARQUEE);
        setSingleLine();
    }

    public ScrollingTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setVisibility(View.VISIBLE);
        setEllipsize(TextUtils.TruncateAt.MARQUEE);
        setSingleLine();
    }

    public ScrollingTextView(Context context) {
        super(context);
        setVisibility(View.VISIBLE);
        setEllipsize(TextUtils.TruncateAt.MARQUEE);
        setSingleLine();
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        if (focused) {
            super.onFocusChanged(focused, direction, previouslyFocusedRect);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean focused) {
        if (focused) {
            super.onWindowFocusChanged(focused);
        }
    }

    @Override
    public boolean isFocused() {
        return true;
    }

}
