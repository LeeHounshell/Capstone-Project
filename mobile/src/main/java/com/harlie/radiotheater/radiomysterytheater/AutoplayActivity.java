package com.harlie.radiotheater.radiomysterytheater;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;

//#IFDEF 'FREE'
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import me.angrybyte.circularslider.CircularSlider;
//#ENDIF

public class AutoplayActivity extends BaseActivity {
    private final static String TAG = "LEE: <" + AutoplayActivity.class.getSimpleName() + ">";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_autoplay);
        configureToolbarTitleBehavior();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayShowTitleEnabled(false);
            }
        }

        AppCompatButton autoPlay = (AppCompatButton) findViewById(R.id.autoplay);
        autoPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "CLICK - autoPlay");
                AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.toolbar_container);
                appBarLayout.setExpanded(false);
                CircularSlider circleSlider = (CircularSlider) findViewById(R.id.circular_seekbar);
                circleSlider.setVisibility(View.VISIBLE);
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            final AutoplayActivity activity = this;
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    //         .setAction("Action", null).show();
                    Log.v(TAG, "CLICK - fab");
                    Intent episodeListIntent = new Intent(activity, EpisodeListActivity.class);
                    startActivity(episodeListIntent);
                }
            });
        }

        // initialize AdMob - note this code uses the Gradle #IFDEF / #ENDIF gradle preprocessor
        //#IFDEF 'FREE'
        String banner_ad_unit_id = getResources().getString(R.string.banner_ad_unit_id);
        MobileAds.initialize(getApplicationContext(), banner_ad_unit_id);

        final TelephonyManager tm =(TelephonyManager)getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)        // All emulators
                .addTestDevice(getResources().getString(R.string.test_device1))
                .addTestDevice(getResources().getString(R.string.test_device2))
                .build();
        mAdView.loadAd(adRequest);
        //#ENDIF
    }

}
