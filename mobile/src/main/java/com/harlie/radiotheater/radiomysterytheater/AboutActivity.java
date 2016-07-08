package com.harlie.radiotheater.radiomysterytheater;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.widget.ImageView;
import android.widget.TextView;

import com.harlie.radiotheater.radiomysterytheater.utils.FontPreferences;
import com.harlie.radiotheater.radiomysterytheater.utils.LogHelper;

// NOTE: build uses 'preprocessor.gradle' here
//#IFDEF 'debug'

//#ENDIF

public class AboutActivity extends BaseActivity {
    private final static String TAG = "LEE: <" + AboutActivity.class.getSimpleName() + ">";

    private int mTopInset;
    private View mUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogHelper.v(TAG, "onCreate");
        super.onCreate(savedInstanceState);

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

        AppCompatImageButton fab = (android.support.v7.widget.AppCompatImageButton) findViewById(R.id.linkedin);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
                showMyLinkedInProfile();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final TextView textView = (TextView) findViewById(R.id.trial_or_paid);
        PackageInfo pInfo = null;
        String version = "";
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        //#IFDEF 'PAID'
        //textView.setText(getResources().getString(R.string.paid_version)+" "+version);
        //#ENDIF
        //#IFDEF 'TRIAL'
        textView.setText(getResources().getString(R.string.trial_version)+" "+version);
        //#ENDIF

        // get the image view
        final ImageView imageView = (ImageView) findViewById(R.id.LeeHounshellImage);

        // set the ontouch listener
        if (imageView != null) {
            imageView.setOnTouchListener(
                    new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(
                                View v,
                                MotionEvent event
                        ) {
                            LogHelper.v(TAG, "onTouch()");
                            switch (event.getAction()) {
                                case MotionEvent.ACTION_DOWN: {
                                    ImageView view = (ImageView) v;
                                    // overlay is black with transparency of 0x77 (119)
                                    view.getDrawable().setColorFilter(0x77000000, PorterDuff.Mode.SRC_ATOP);
                                    view.invalidate();
                                    break;
                                }
                                case MotionEvent.ACTION_UP:
                                    imageView.performClick();
                                case MotionEvent.ACTION_CANCEL: {
                                    LogHelper.v(TAG, "view Linked-In");
                                    ImageView view = (ImageView) v;
                                    // clear the overlay
                                    view.getDrawable().clearColorFilter();
                                    view.invalidate();
                                    sendEmail_to_LeeHounshell();
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
                    LogHelper.v(TAG, "onClick");
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

    private void sendEmail_to_LeeHounshell() {
        LogHelper.v(TAG, "sendEmail_to_LeeHounshell");
        String contact = getResources().getString(R.string.app_contact);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("plain/text");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[] {contact});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Re: the 'Radio Mystery Theater' Android app");
        intent.putExtra(Intent.EXTRA_TEXT, "");
        startActivity(Intent.createChooser(intent, ""));
    }

    private void showMyLinkedInProfile() {
        LogHelper.v(TAG, "showMyLinkedInProfile");
        // show my Linked-In profile
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setData(Uri.parse("http://www.linkedin.com/pub/lee-hounshell/2/674/852"));
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        LogHelper.v(TAG, "onBackPressed");
        super.onBackPressed();
        Intent autoplayIntent = new Intent(this, AutoplayActivity.class);
        // close existing activity stack regardless of what's in there and create new root
        //autoplayIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        Bundle playInfo = new Bundle();
        savePlayInfoToBundle(playInfo);
        autoplayIntent.putExtras(playInfo);
        startActivity(autoplayIntent);
    }

}

