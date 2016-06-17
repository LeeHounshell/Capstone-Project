package com.harlie.radiotheater.radiomysterytheater;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

//#IFDEF 'FREE'
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
//#ENDIF

import com.harlie.radiotheater.radiomysterytheater.data.configepisodes.ConfigEpisodesContentValues;
import com.harlie.radiotheater.radiomysterytheater.data.configepisodes.ConfigEpisodesCursor;
import com.harlie.radiotheater.radiomysterytheater.data.episodes.EpisodesColumns;
import com.harlie.radiotheater.radiomysterytheater.data.episodes.EpisodesContentValues;
import com.harlie.radiotheater.radiomysterytheater.data.episodes.EpisodesCursor;
import com.harlie.radiotheater.radiomysterytheater.utils.LogHelper;

import java.util.Date;

import me.angrybyte.circularslider.CircularSlider;

public class AutoplayActivity extends BaseActivity {
    private final static String TAG = "LEE: <" + AutoplayActivity.class.getSimpleName() + ">";

    public static final String EXTRA_START_FULLSCREEN = "com.harlie.radiotheater.radiomysterytheater.EXTRA_START_FULLSCREEN";
    /**
     * Optionally used with {@link #EXTRA_START_FULLSCREEN} to carry a MediaDescription to
     * the {@link FullScreenPlayerActivity}, speeding up the screen rendering
     * while the {@link android.support.v4.media.session.MediaControllerCompat} is connecting.
     */
    public static final String EXTRA_CURRENT_MEDIA_DESCRIPTION = "com.harlie.radiotheater.radiomysterytheater.CURRENT_MEDIA_DESCRIPTION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogHelper.v(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        /*
         * FUTURE: TV support
         *
        UiModeManager uiModeManager = (UiModeManager) getSystemService(UI_MODE_SERVICE);
        if (uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION) {
            LogHelper.d(TAG, "Running on a TV Device");
            Intent tvIntent = new Intent(this, TvPlaybackActivity.class);
            startActivity(tvIntent);
            finish();
            return;
        }
        */
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
                LogHelper.v(TAG, "CLICK - autoPlay");
                boolean foundEpisode = false;
                AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.toolbar_container);

                ConfigEpisodesCursor configCursor = getCursorForNextAvailableEpisode();
                if (configCursor != null && configCursor.moveToNext()) {
                    // found the next episode to listen to
                    appBarLayout.setExpanded(false);
                    long episodeNumber = configCursor.getFieldEpisodeNumber();
                    boolean purchased = configCursor.getFieldPurchasedAccess();
                    boolean noAdsForShow = configCursor.getFieldPurchasedNoads();
                    boolean downloaded = configCursor.getFieldEpisodeDownloaded();
                    boolean episodeHeard = configCursor.getFieldEpisodeHeard();
                    int listenCount = configCursor.getFieldListenCount();
                    configCursor.close();

                    LogHelper.v(TAG, "===> NEXT EPISODE TO PLAY"
                            +": episodeNumber="+episodeNumber
                            +", purchased="+purchased
                            +", noAdsForShow="+noAdsForShow
                            +", downloaded="+downloaded
                            +", episodeHeard="+episodeHeard
                            +", listenCount="+listenCount);

                    // get this episode's info
                    EpisodesCursor episodesCursor = getEpisodesCursor(episodeNumber);
                    if (episodesCursor != null && episodesCursor.moveToNext()) {
                        episodeNumber = episodesCursor.getFieldEpisodeNumber();
                        Date airdate = episodesCursor.getFieldAirdate();
                        String episodeTitle = episodesCursor.getFieldEpisodeTitle();
                        String episodeDescription = episodesCursor.getFieldEpisodeDescription();
                        String episodeWeblinkUrl = episodesCursor.getFieldWeblinkUrl();
                        String episodeDownloadUrl = episodesCursor.getFieldDownloadUrl();
                        Integer rating = episodesCursor.getFieldRating();
                        Integer voteCount = episodesCursor.getFieldVoteCount();
                        episodesCursor.close();
                        foundEpisode = true;

                        LogHelper.v(TAG, "===> EPISODE DETAIL"
                                +": episodeNumber="+episodeNumber
                                +": airdate="+airdate.toString()
                                +": episodeTitle="+episodeTitle
                                +": episodeDescription="+episodeDescription
                                +": episodeWeblinkUrl="+episodeWeblinkUrl
                                +": episodeDownloadUrl="+episodeDownloadUrl
                                +": rating="+rating
                                +": voteCount="+voteCount);

                        CircularSlider circleSlider = (CircularSlider) findViewById(R.id.circular_seekbar);
                        circleSlider.setVisibility(View.VISIBLE);
                    }
                }
                if (!foundEpisode) {
                    // FIXME: popup alert - ALL EPISODES ARE HEARD!
                    appBarLayout.setExpanded(true);
                    CircularSlider circleSlider = (CircularSlider) findViewById(R.id.circular_seekbar);
                    circleSlider.setVisibility(View.GONE);
                }
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
                    LogHelper.v(TAG, "CLICK - fab");
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        LogHelper.v(TAG, "onCreateOptionsMenu");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        LogHelper.v(TAG, "onOptionsItemSelected");
        switch (item.getItemId()) {
            case R.id.search: {
                // FIXME:
                return true;
            }
            case R.id.settings: {
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            }
            case R.id.about: {
                Intent intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

}
