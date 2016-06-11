package com.harlie.radiotheater.radiomysterytheater;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.widget.ImageView;

// NOTE: build uses 'preprocessor.gradle' here
//#IFDEF 'debug'
import com.harlie.radiotheater.radiomysterytheater.RadioTheaterApplication;
//#ENDIF

public class AboutActivity extends BaseActivity {
    private final static String TAG = "LEE: <" + AboutActivity.class.getSimpleName() + ">";

    private int mTopInset;
    private View mUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            // restore our old session..
            onRestoreInstanceState(savedInstanceState);
        }

        setContentView(R.layout.activity_about);

        getTheme().applyStyle(new FontPreferences(this).getFontStyle().getResId(), true);

        configureToolbarTitleBehavior();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayShowTitleEnabled(false);
            }
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
                showMyLinkedInProfile();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // get the image view
        ImageView imageView = (ImageView) findViewById(R.id.LeeHounshellImage);

        // set the ontouch listener
        if (imageView != null) {
            imageView.setOnTouchListener(
                    new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(
                                View v,
                                MotionEvent event
                        ) {
                            Log.v(TAG, "onTouch()");
                            switch (event.getAction()) {
                                case MotionEvent.ACTION_DOWN: {
                                    ImageView view = (ImageView) v;
                                    // overlay is black with transparency of 0x77 (119)
                                    view.getDrawable().setColorFilter(0x77000000, PorterDuff.Mode.SRC_ATOP);
                                    view.invalidate();
                                    break;
                                }
                                case MotionEvent.ACTION_UP:
                                case MotionEvent.ACTION_CANCEL: {
                                    Log.v(TAG, "view Linked-In");
                                    ImageView view = (ImageView) v;
                                    // clear the overlay
                                    view.getDrawable().clearColorFilter();
                                    view.invalidate();
                                    showMyLinkedInProfile();
                                    break;
                                }
                            }
                            return true;
                        }
                    }
            );
        }

        final View upButtonContainer = findViewById(R.id.up_container);

        mUpButton = findViewById(R.id.action_up);
        if (mUpButton != null) {
            mUpButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.v(TAG, "onClick");
                    onBackPressed();
                }
            });
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (upButtonContainer != null) {
                upButtonContainer.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                    @Override
                    public WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                            view.onApplyWindowInsets(windowInsets);
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                            mTopInset = windowInsets.getSystemWindowInsetTop();
                        }
                        upButtonContainer.setTranslationY(mTopInset);
                        return windowInsets;
                    }
                });
            }
        }
    }

    private void showMyLinkedInProfile() {
        // show my Linked-In profile
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setData(Uri.parse("http://www.linkedin.com/pub/lee-hounshell/2/674/852"));
        startActivity(intent);
    }

}

