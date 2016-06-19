package com.harlie.radiotheater.radiomysterytheater;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
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
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

//#IFDEF 'FREE'
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
//#ENDIF

import com.harlie.radiotheater.radiomysterytheater.data.configepisodes.ConfigEpisodesCursor;
import com.harlie.radiotheater.radiomysterytheater.data.episodes.EpisodesCursor;
import com.harlie.radiotheater.radiomysterytheater.utils.LogHelper;
import com.harlie.radiotheater.radiomysterytheater.utils.MediaIDHelper;

import java.util.ArrayList;
import java.util.List;

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
    private AudioManager mAudioManager;
    private int mAudioFocusRequstResult;
    private MediaBrowserCompat mMediaBrowser;
    private MediaControllerCompat mMediaController;
    private String mMediaId;

    static final int STATE_INVALID = -1;
    static final int STATE_NONE = 0;
    static final int STATE_PLAYABLE = 1;
    static final int STATE_PAUSED = 2;
    static final int STATE_PLAYING = 3;

    // Callback for Audio Focus
    private final AudioManager.OnAudioFocusChangeListener mAudioFocusChangeListener =
        new AudioManager.OnAudioFocusChangeListener() {
            public void onAudioFocusChange(int focusChange) {
                LogHelper.v(TAG, "===> onAudioFocusChange="+focusChange+" <===");
                if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                    LogHelper.v(TAG, "AudioManager.AUDIOFOCUS_GAIN <<---");
                }
                else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                    LogHelper.v(TAG, "AudioManager.AUDIOFOCUS_LOSS <<---");
                    mAudioFocusRequstResult = 0;
                    // FIXME: stop playback
                }
                else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                    LogHelper.v(TAG, "AudioManager.AUDIOFOCUS_LOSS_TRANSIENT <<---");
                    mAudioFocusRequstResult = 0;
                    // FIXME: pause playback
                }
                else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                    LogHelper.v(TAG, "AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK <<---");
                    mAudioFocusRequstResult = 0;
                    // FIXME: lower volume
                }
            }
        };

    // Callback for media subscription
    private final MediaBrowserCompat.SubscriptionCallback mSubscriptionCallback =
            new MediaBrowserCompat.SubscriptionCallback() {
                @Override
                public void onChildrenLoaded(@NonNull String parentId,
                                             @NonNull List<MediaBrowserCompat.MediaItem> children) {
                    try {
                        LogHelper.d(TAG, "********* onChildrenLoaded, parentId=" + parentId + "  count=" + children.size());
                        for (MediaBrowserCompat.MediaItem item : children) {
                            LogHelper.v(TAG, "item="+item.getDescription()+", MediaId="+item.getMediaId());
                            mMediaId = item.getMediaId();
                        }
                    } catch (Throwable t) {
                        LogHelper.e(TAG, "Error on childrenloaded", t);
                    }
                }

                @Override
                public void onError(@NonNull String id) {
                    LogHelper.e(TAG, "browse subscription onError, id=" + id);
                }
            };

    // Callback for Media Browser Connection
    private final MediaBrowserCompat.ConnectionCallback mConnectionCallback =
            new MediaBrowserCompat.ConnectionCallback() {
                @Override
                public void onConnected() {
                    LogHelper.d(TAG, "MediaBrowserCompat.ConnectionCallback onConnected <<<---------");

                    try {
                        connectToSession(mMediaBrowser.getSessionToken());
                    } catch (RemoteException e) {
                        LogHelper.e(TAG, e, "could not connect media controller");
                        hidePlaybackControls();
                    }

                    mMediaId = mMediaBrowser.getRoot();

                    // Unsubscribing before subscribing is required if this mediaId already has a subscriber
                    // on this MediaBrowser instance. Subscribing to an already subscribed mediaId will replace
                    // the callback, but won't trigger the initial callback.onChildrenLoaded.
                    //
                    // This is temporary: A bug is being fixed that will make subscribe
                    // consistently call onChildrenLoaded initially, no matter if it is replacing an existing
                    // subscriber or not. Currently this only happens if the mediaID has no previous
                    // subscriber or if the media content changes on the service side, so we need to
                    // unsubscribe first.
                    mMediaBrowser.unsubscribe(mMediaId);

                    mMediaBrowser.subscribe(mMediaId, mSubscriptionCallback);

                    // Add MediaController callback so we can redraw the list when metadata changes:
                    if (mMediaController != null) {
                        LogHelper.v(TAG, "mMediaController.registerCallback(mMediaControllerCallback);");
                        mMediaController.registerCallback(mMediaControllerCallback);
                    }
                    else {
                        LogHelper.w(TAG, "UNABLE: mMediaController.registerCallback(mMediaControllerCallback);");
                    }
                }
            };

    // Callback that ensures that we are showing the controls
    private final MediaControllerCompat.Callback mMediaControllerCallback =
            new MediaControllerCompat.Callback() {
                @Override
                public void onPlaybackStateChanged(@NonNull PlaybackStateCompat state) {
                    LogHelper.d(TAG, "MediaControllerCompat.Callback onPlaybackStateChanged state="+state+" <<<---------");
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
                    LogHelper.d(TAG, "MediaControllerCompat.Callback onMetadataChanged metadata="+metadata+" <<<---------");
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

                        playPauseEpisode(episodeNumber, episodeTitle, episodeDownloadUrl, purchased, downloaded);
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

        // manually (debug) start the RadioTheaterService
        //Intent it = new Intent(this, RadioTheaterService.class);
        //startService(it); // Start the Radio Theater service.
    }

    private void playPauseEpisode(long episodeNumber, String episodeTitle, String episodeDownloadUrl, boolean purchased, boolean downloaded) {
        LogHelper.v(TAG, "playPauseEpisode: episodeNumber="+episodeNumber
                +", episodeTitle="+episodeTitle
                +", episodeDownloadUrl="+episodeDownloadUrl
                +", purchased="+purchased
                +", downloaded="+downloaded);

        PlaybackStateCompat state = getSupportMediaController().getPlaybackState();
        if (state != null) {
            MediaControllerCompat.TransportControls controls = getSupportMediaController().getTransportControls();
            switch (state.getState()) {
                case PlaybackStateCompat.STATE_PLAYING: // fall through
                case PlaybackStateCompat.STATE_BUFFERING:
                    LogHelper.v(TAG, "controls.pause();");
                    controls.pause();
                    // FIXME: stopSeekbarUpdate();
                    break;
                case PlaybackStateCompat.STATE_PAUSED:
                case PlaybackStateCompat.STATE_STOPPED:
                    LogHelper.v(TAG, "controls.play();");
                    controls.play();
                    // FIXME: scheduleSeekbarUpdate();
                    break;
                default:
                    Uri mediaUri = Uri.parse(episodeDownloadUrl);
                    LogHelper.d(TAG, "*** START PLAYBACK *** state="+state.getState()+", mMediaId="+mMediaId+", mediaUri="+mediaUri);
                    mMediaController.getTransportControls().playFromUri(mediaUri, null);
                    press_PLAY_PAUSE();
            }
        }
    }

    // from: http://stackoverflow.com/questions/19890643/android-4-4-play-default-music-player/20439822#20439822
    // from: http://stackoverflow.com/questions/11275400/reliably-pausing-media-playback-system-wide-in-android
    private void press_PLAY_PAUSE() {
        LogHelper.v(TAG, "press_PLAY_PAUSE");
        final Context context = getApplicationContext();
        if (mAudioFocusRequstResult != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mAudioFocusRequstResult = mAudioManager.requestAudioFocus(mAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }
        if (mAudioFocusRequstResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            LogHelper.v(TAG, "---> AudioFocus GRANTED");
            long eventtime = SystemClock.uptimeMillis();

            Intent downIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
            KeyEvent downEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, 0);
            downIntent.putExtra(Intent.EXTRA_KEY_EVENT, downEvent);
            sendOrderedBroadcast(downIntent, null);

            Intent upIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
            KeyEvent upEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, 0);
            upIntent.putExtra(Intent.EXTRA_KEY_EVENT, upEvent);
            sendOrderedBroadcast(upIntent, null);

            // hold audio focus until playback stopped
        }
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
            LogHelper.d(TAG, "connectToSession: hiding controls");
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
    protected void onResume() {
        LogHelper.d(TAG, "onResume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        LogHelper.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onStart() {
        LogHelper.d(TAG, "onStart");
        super.onStart();
        hidePlaybackControls();
        LogHelper.d(TAG, "---> mMediaBrowser.connect();");
        mMediaBrowser.connect();
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        // request audio focus from the system
        mAudioFocusRequstResult = mAudioManager.requestAudioFocus(mAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    }

    @Override
    protected void onStop() {
        LogHelper.d(TAG, "onStop");
        super.onStop();
        if (mAudioManager != null && mAudioFocusRequstResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mAudioManager.abandonAudioFocus(mAudioFocusChangeListener);
            mAudioFocusRequstResult = 0;
        }
        if (mMediaController != null) {
            mMediaController.unregisterCallback(mMediaControllerCallback);
        }
        LogHelper.d(TAG, "---> mMediaBrowser.disconnect();");
        mMediaBrowser.disconnect();
    }

    @Override
    public void onDestroy() {
        LogHelper.v(TAG, "onDestroy");
        super.onDestroy();
        mAudioManager = null;
        mAutoPlay = null;
        mCircleSlider = null;
        mAudioManager = null;
        mMediaBrowser = null;
        mMediaController = null;
        mMediaId = null;
    }

}
