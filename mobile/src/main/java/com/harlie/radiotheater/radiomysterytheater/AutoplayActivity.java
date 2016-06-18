package com.harlie.radiotheater.radiomysterytheater;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

//#IFDEF 'FREE'
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
//#ENDIF

import com.harlie.radiotheater.radiomysterytheater.data.configepisodes.ConfigEpisodesCursor;
import com.harlie.radiotheater.radiomysterytheater.data.episodes.EpisodesCursor;
import com.harlie.radiotheater.radiomysterytheater.utils.LogHelper;

import me.angrybyte.circularslider.CircularSlider;

public class AutoplayActivity extends BaseActivity
{
    private final static String TAG = "LEE: <" + AutoplayActivity.class.getSimpleName() + ">";

    public static final String EXTRA_START_FULLSCREEN = "com.harlie.radiotheater.radiomysterytheater.EXTRA_START_FULLSCREEN";
    /**
     * Optionally used with {@link #EXTRA_START_FULLSCREEN} to carry a MediaDescription to
     * the player Activity, speeding up the screen rendering
     * while the {@link android.support.v4.media.session.MediaControllerCompat} is connecting.
     */
    public static final String EXTRA_CURRENT_MEDIA_DESCRIPTION = "com.harlie.radiotheater.radiomysterytheater.CURRENT_MEDIA_DESCRIPTION";

    private AppCompatButton mAutoPlay;
    private CircularSlider mCircleSlider;
    private MediaBrowserCompat mMediaBrowser;
    private MediaControllerCompat mMediaController;
    private MediaSessionCompat mMediaSession;
    private ComponentName mMediaButtonReceiver;

    private final MediaBrowserCompat.ConnectionCallback mConnectionCallback =
            new MediaBrowserCompat.ConnectionCallback() {
                @Override
                public void onConnected() {
                    LogHelper.d(TAG, "onConnected");
                    try {
                        connectToSession(mMediaBrowser.getSessionToken());
                    } catch (RemoteException e) {
                        LogHelper.e(TAG, e, "could not connect media controller");
                        hidePlaybackControls();
                    }
                }
            };

    // Callback that ensures that we are showing the controls
    private final MediaControllerCompat.Callback mMediaControllerCallback =
            new MediaControllerCompat.Callback() {
                @Override
                public void onPlaybackStateChanged(@NonNull PlaybackStateCompat state) {
                    LogHelper.d(TAG, "onPlaybackStateChanged");
                    if (shouldShowControls()) {
                        showPlaybackControls();
                    } else {
                        LogHelper.d(TAG, "mediaControllerCallback.onPlaybackStateChanged: " +
                                "hiding controls because state is ", state.getState());
                        hidePlaybackControls();
                    }
                }

                @Override
                public void onMetadataChanged(MediaMetadataCompat metadata) {
                    LogHelper.d(TAG, "onMetadataChanged");
                    if (shouldShowControls()) {
                        showPlaybackControls();
                    } else {
                        LogHelper.d(TAG, "mediaControllerCallback.onMetadataChanged: " +
                                "hiding controls because metadata is null");
                        hidePlaybackControls();
                    }
                }
            };

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

        // Connect a media browser just to get the media session token. There are other ways
        // this can be done, for example by sharing the session token directly.
        mMediaBrowser = new MediaBrowserCompat(this,
                new ComponentName(this, RadioTheaterService.class), mConnectionCallback, null);

        mAutoPlay = (AppCompatButton) findViewById(R.id.autoplay);
        mAutoPlay.setOnClickListener(new View.OnClickListener() {
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
                        String airdate = episodesCursor.getFieldAirdate();
                        String episodeTitle = episodesCursor.getFieldEpisodeTitle();
                        String episodeDescription = episodesCursor.getFieldEpisodeDescription();
                        String episodeWeblinkUrl = episodesCursor.getFieldWeblinkUrl();
                        String episodeDownloadUrl = episodesCursor.getFieldDownloadUrl();
                        Float rating = episodesCursor.getFieldRating();
                        Integer voteCount = episodesCursor.getFieldVoteCount();
                        episodesCursor.close();
                        foundEpisode = true;

                        LogHelper.v(TAG, "===> EPISODE DETAIL"
                                +": episodeNumber="+episodeNumber
                                +": airdate="+airdate
                                +": episodeTitle="+episodeTitle
                                +": episodeDescription="+episodeDescription
                                +": episodeWeblinkUrl="+episodeWeblinkUrl
                                +": episodeDownloadUrl="+episodeDownloadUrl
                                +": rating="+rating
                                +": voteCount="+voteCount);

                        press_PLAY_PAUSE();
                    }
                }
                if (!foundEpisode) {
                    // FIXME: popup alert - ALL EPISODES ARE HEARD!
                    appBarLayout.setExpanded(true);
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

        mCircleSlider = (CircularSlider) findViewById(R.id.circular_seekbar);

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

    // from: http://stackoverflow.com/questions/19890643/android-4-4-play-default-music-player/20439822#20439822
    // from: http://stackoverflow.com/questions/11275400/reliably-pausing-media-playback-system-wide-in-android
    private void press_PLAY_PAUSE() {
        LogHelper.v(TAG, "press_PLAY_PAUSE");
        final Context context = getApplicationContext();
        final AudioManager.OnAudioFocusChangeListener af = new AudioManager.OnAudioFocusChangeListener() {
            // do nothing with this listener, but it's required for the next step.

            public void onAudioFocusChange(int focusChange) {
                LogHelper.v(TAG, "onAudioFocusChange");
            }

        };
        final AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);

        // request audio focus from the system
        int request = am.requestAudioFocus(af, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        long eventtime = SystemClock.uptimeMillis();

        Intent downIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
        KeyEvent downEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, 0);
        downIntent.putExtra(Intent.EXTRA_KEY_EVENT, downEvent);
        sendOrderedBroadcast(downIntent, null);

        Intent upIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
        KeyEvent upEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, 0);
        upIntent.putExtra(Intent.EXTRA_KEY_EVENT, upEvent);
        sendOrderedBroadcast(upIntent, null);

        am.abandonAudioFocus(af);
    }

    private void press_NEXT() {
        LogHelper.v(TAG, "press_NEXT");
        long eventtime = SystemClock.uptimeMillis();
        Intent downIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
        KeyEvent downEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_DOWN,   KeyEvent.KEYCODE_MEDIA_NEXT, 0);
        downIntent.putExtra(Intent.EXTRA_KEY_EVENT, downEvent);
        sendOrderedBroadcast(downIntent, null);
    }

    private void press_PREVIOUS() {
        LogHelper.v(TAG, "press_PREVIOUS");
        long eventtime = SystemClock.uptimeMillis();
        Intent downIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
        KeyEvent downEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PREVIOUS, 0);
        downIntent.putExtra(Intent.EXTRA_KEY_EVENT, downEvent);
        sendOrderedBroadcast(downIntent, null);
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

    private void connectToSession(MediaSessionCompat.Token token)
            throws RemoteException
    {
        LogHelper.v(TAG, "connectToSession");
        mMediaController = new MediaControllerCompat(this, token);
        setSupportMediaController(mMediaController);
        mMediaController.registerCallback(mMediaControllerCallback);

        if (shouldShowControls()) {
            showPlaybackControls();
        } else {
            LogHelper.d(TAG, "connectionCallback.onConnected: " +
                    "hiding controls because metadata is null");
            hidePlaybackControls();
        }
    }

    protected void showPlaybackControls() {
        LogHelper.d(TAG, "showPlaybackControls");
        Drawable pauseButton = getResources().getDrawable(R.drawable.radio_theater_pause_button_selector);
        mAutoPlay.setBackground(pauseButton);
        mCircleSlider.setVisibility(View.VISIBLE);
    }

    protected void hidePlaybackControls() {
        LogHelper.d(TAG, "hidePlaybackControls");
        Drawable autoplayButton = getResources().getDrawable(R.drawable.radio_theater_autoplay_button_selector);
        mAutoPlay.setBackground(autoplayButton);
        mCircleSlider.setVisibility(View.GONE);
    }

    /**
     * Check if the MediaSession is active and in a "playback-able" state
     * (not NONE and not STOPPED).
     *
     * @return true if the MediaSession's state requires playback controls to be visible.
     */
    protected boolean shouldShowControls() {
        LogHelper.v(TAG, "shouldShowControls");
        MediaControllerCompat mediaController = getSupportMediaController();
        if (mediaController == null ||
                mediaController.getMetadata() == null ||
                mediaController.getPlaybackState() == null) {
            return false;
        }
        switch (mediaController.getPlaybackState().getState()) {
            case PlaybackStateCompat.STATE_ERROR:
                LogHelper.v(TAG, "PlaybackStateCompat.STATE_ERROR");
                return false;
            case PlaybackStateCompat.STATE_NONE:
                LogHelper.v(TAG, "PlaybackStateCompat.STATE_NONE");
                return false;
            case PlaybackStateCompat.STATE_STOPPED:
                LogHelper.v(TAG, "PlaybackStateCompat.STATE_STOPPED");
                return false;
            default:
                LogHelper.v(TAG, "PlaybackStateCompat.STATE_DEFAULT");
                return true;
        }
    }

    @Override
    protected void onStart() {
        LogHelper.d(TAG, "onStart");
        super.onStart();
        hidePlaybackControls();
        mMediaBrowser.connect();
    }

    @Override
    protected void onStop() {
        LogHelper.d(TAG, "onStop");
        super.onStop();
        if (getSupportMediaController() != null) {
            getSupportMediaController().unregisterCallback(mMediaControllerCallback);
        }
        mMediaBrowser.disconnect();
    }

    @Override
    public void onDestroy() {
        LogHelper.v(TAG, "onDestroy");
        super.onDestroy();
        mAutoPlay = null;
        mCircleSlider = null;
        mMediaBrowser = null;
        mMediaController = null;
        mMediaButtonReceiver = null;
        mMediaSession = null;
    }

}
