package com.harlie.radiotheater.radiomysterytheater;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

//#IFDEF 'FREE'
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
//#ENDIF

import com.harlie.radiotheater.radiomysterytheater.data.configepisodes.ConfigEpisodesCursor;
import com.harlie.radiotheater.radiomysterytheater.data.episodes.EpisodesCursor;
import com.harlie.radiotheater.radiomysterytheater.data_helper.LoadingAsyncTask;
import com.harlie.radiotheater.radiomysterytheater.utils.LogHelper;
import com.harlie.radiotheater.radiomysterytheater.utils.MediaIDHelper;
import com.harlie.radiotheater.radiomysterytheater.utils.OnSwipeTouchListener;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import at.grabner.circleprogress.CircleProgressView;

public class AutoplayActivity extends BaseActivity
{
    private final static String TAG = "LEE: <" + AutoplayActivity.class.getSimpleName() + ">";

    public enum ControlsState {
        ENABLED, ENABLED_SHOW_PAUSE, DISABLED, DISABLED_SHOW_PAUSE, SEEKING_POSITION
    }

    public static final String EXTRA_START_FULLSCREEN = "com.harlie.radiotheater.radiomysterytheater.EXTRA_START_FULLSCREEN";
    /**
     * Optionally used with {@link #EXTRA_START_FULLSCREEN} to carry a MediaDescription to
     * the player Activity, speeding up the screen rendering
     * while the {@link android.support.v4.media.session.MediaControllerCompat} is connecting.
     */
    public static final String EXTRA_CURRENT_MEDIA_DESCRIPTION = "com.harlie.radiotheater.radiomysterytheater.CURRENT_MEDIA_DESCRIPTION";

    private static final long PROGRESS_UPDATE_INTERNAL = 1000;
    private static final long PROGRESS_UPDATE_INITIAL_INTERVAL = 100;
    private static final long THIRTY_SECONDS = 30 * 1000;
    private static final long DEFAULT_DURATION = 60 * 60 * 1000;

    private AppCompatButton mAutoPlay;
    private FloatingActionButton mFabActionButton;
    private CircularSeekBar mCircularSeekBar;
    private AudioManager mAudioManager;
    private MediaBrowserCompat mMediaBrowser;
    private MediaControllerCompat mMediaController;
    private ScheduledFuture<?> mScheduleFuture;
    private final Handler mHandler = new Handler();
    private BroadcastReceiver mReceiver;

    private volatile boolean mHandleRotationEvent;
    private volatile boolean mNeed2RestartPlayback;
    private volatile boolean mLoadingScreenEnabled;
    private volatile boolean mBeginLoading;
    private volatile boolean mSeekUpdateRunning;

    private final ScheduledExecutorService mExecutorService =
            Executors.newSingleThreadScheduledExecutor();

    private final Runnable mUpdateProgressTask = new Runnable() {
        @Override
        public void run() {
            if (mCircularSeekBar != null && !mCircularSeekBar.isProcessingTouchEvents() && !mSeeking) {
                PlaybackStateCompat lastPlaybackState = mMediaController.getPlaybackState();
                mCurrentPosition = lastPlaybackState.getPosition();
                if (lastPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING) {
                    if (!mCircularSeekBar.isProcessingTouchEvents() && !mSeeking) {
                        LoadingAsyncTask.mDoneLoading = true;
                        // Calculate the elapsed time between the last position update and now and unless
                        // paused, we can assume (delta * speed) + current position is approximately the
                        // latest position. This ensure that we do not repeatedly call the getPlaybackState()
                        // on MediaControllerCompat.
                        long timeDelta = SystemClock.elapsedRealtime() - lastPlaybackState.getLastPositionUpdateTime();
                        mCurrentPosition += (int) timeDelta * lastPlaybackState.getPlaybackSpeed();
                        getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                LogHelper.v(TAG, "SEEKBAR setProgress mCurrentPosition="+mCurrentPosition);
                                mCircularSeekBar.setProgress((int) mCurrentPosition);
                            }
                        });
                    }
                }
            }
        }
    };

    // Callback for Audio Focus
    private final AudioManager.OnAudioFocusChangeListener mAudioFocusChangeListener =
        new AudioManager.OnAudioFocusChangeListener() {
            public void onAudioFocusChange(int focusChange) {
                LogHelper.v(TAG, "===> onAudioFocusChange="+focusChange+" <===");
                MediaControllerCompat.TransportControls controls = mMediaController.getTransportControls();
                if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                    LogHelper.v(TAG, "AudioManager.AUDIOFOCUS_GAIN <<---");
                    mAudioFocusRequstResult = AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
                }
                else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                    LogHelper.v(TAG, "AudioManager.AUDIOFOCUS_LOSS <<---");
                    mAudioFocusRequstResult = 0;
                    controls.stop();
                    setAutoplayState(AutoplayState.READY2PLAY, "onAudioFocusChange - READY2PLAY");
                    mCurrentPosition = 0;
                }
                else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                    LogHelper.v(TAG, "AudioManager.AUDIOFOCUS_LOSS_TRANSIENT <<---");
                    mAudioFocusRequstResult = 0;
                    controls.pause();
                }
                else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                    LogHelper.v(TAG, "AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK <<---");
                    mAudioFocusRequstResult = 0;
                    controls.pause();
                }
            }
        };

    // Callback for media subscription
    private final MediaBrowserCompat.SubscriptionCallback mSubscriptionCallback =
            new MediaBrowserCompat.SubscriptionCallback() {
                @Override
                public void onChildrenLoaded(@NonNull String parentId,
                                             @NonNull List<MediaBrowserCompat.MediaItem> children) {
                    if (children == null || children.isEmpty()) {
                        LogHelper.w(TAG, "onChildrenLoaded: NO CHILDREN!");
                        return;
                    }
                    LogHelper.d(TAG, "********* onChildrenLoaded, parentId=" + parentId + "  count=" + children.size());
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
                    }
                    catch (RemoteException e) {
                        LogHelper.e(TAG, e, "could not connect media controller");
                    }

                    //mMediaId = mMediaBrowser.getRoot();
                    mMediaId = getResources().getString(R.string.genre);

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
                        if (mNeed2RestartPlayback) {
                            LogHelper.v(TAG, "*** NEED TO RESTART PLAYBACK! - mNeed2RestartPlayback = true;");
                            mNeed2RestartPlayback = false;
                            playPauseEpisode();
                        }
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
                    LogHelper.v(TAG, "onPlaybackStateChanged: state="+state.getState());
                    switch (state.getState()) {
                        case PlaybackStateCompat.STATE_PLAYING: {
                            LogHelper.d(TAG, "MediaControllerCompat.Callback onPlaybackStateChanged STATE_PLAYING <<<---------");
                            setAutoplayState(AutoplayState.PLAYING, "onPlaybackChanged - PLAYING");
                            mSeeking = false;
                            mBeginLoading = false;
                            break;
                        }
                        case PlaybackStateCompat.STATE_BUFFERING: {
                            LogHelper.d(TAG, "MediaControllerCompat.Callback onPlaybackStateChanged STATE_BUFFERING <<<---------");
                            setAutoplayState(AutoplayState.LOADING, "onPlaybackChanged - LOADING");
                            break;
                        }
                        case PlaybackStateCompat.STATE_PAUSED: {
                            LogHelper.d(TAG, "MediaControllerCompat.Callback onPlaybackStateChanged STATE_PAUSED <<<---------");
                            setAutoplayState(AutoplayState.PAUSED, "onPlaybackChanged - PAUSED");
                            mSeeking = false;
                            mBeginLoading = false;
                            break;
                        }
                        case PlaybackStateCompat.STATE_ERROR:
                        case PlaybackStateCompat.STATE_NONE:
                        case PlaybackStateCompat.STATE_STOPPED: {
                            LogHelper.d(TAG, "MediaControllerCompat.Callback onPlaybackStateChanged STATE_STOPPED <<<---------");
                            setAutoplayState(AutoplayState.READY2PLAY, "onPlaybackChanged - READY2PLAY");
                            mSeeking = false;
                            break;
                        }
                    }
                    if (mSeeking) {
                        LogHelper.v(TAG, "*** ignoring onPlaybackStateChanged until seekTo completes");
                        getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                loadingScreen();
                            }
                        });
                    }
                    else if (state.getState() == PlaybackStateCompat.STATE_PLAYING) {
                        getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                enableButtons();
                                managePlaybackControls(ControlsState.ENABLED_SHOW_PAUSE, "onPlaybackChanged - manage PLAYING");
                                scheduleSeekbarUpdate();
                            }
                        });
                    }
                    else if (state.getState() == PlaybackStateCompat.STATE_PAUSED) {
                        getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                enableButtons();
                                managePlaybackControls(ControlsState.ENABLED_SHOW_PAUSE, "onPlaybackChanged - manage PAUSED");
                                stopSeekbarUpdate();
                            }
                        });
                    }
                    else if (state.getState() == PlaybackStateCompat.STATE_STOPPED) {
                        getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                enableButtons();
                                managePlaybackControls(ControlsState.ENABLED, "onPlaybackChanged - manage STOPPED");
                                stopSeekbarUpdate();
                            }
                        });
                    }
                }

                @Override
                public void onMetadataChanged(MediaMetadataCompat metadata) {
                    LogHelper.d(TAG, "onMetadataChanged");
                    LogHelper.d(TAG, "MediaControllerCompat.Callback onMetadataChanged metadata="+metadata+" <<<---------");
                    if (metadata != null) {
                        updateMediaDescription(metadata.getDescription());
                        updateDuration(metadata);
                    }
                    do_UpdateControls();
                }
            };

    private void enableButtons() {
        mAutoPlay.setVisibility(View.VISIBLE);
        mAutoPlay.setEnabled(true);
        mFabActionButton.setVisibility(View.VISIBLE);
        mFabActionButton.setEnabled(true);
    }

    private void do_UpdateControls() {
        LogHelper.v(TAG, "do_UpdateControls");
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                updateControls();
            }
        });
    }

    /**
     * Check if the MediaSession is active and in a "playback-able" state
     * (not NONE and not STOPPED).
     *
     * @return true if the MediaSession's state requires playback controls to be visible.
     */
    protected void updateControls() {
        LogHelper.v(TAG, "updateControls");
        if (mBeginLoading) {
            LogHelper.v(TAG, "mBeginLoading - skipping updateControls");
            return;
        }
        switch (mAutoplayState) {
            case READY2PLAY: {
                LogHelper.v(TAG, "updateControls - READY2PLAY");
                managePlaybackControls(ControlsState.ENABLED, "updateControls");
                break;
            }
            case LOADING: {
                LogHelper.v(TAG, "updateControls - LOADING");
                loadingScreen();
                managePlaybackControls(ControlsState.ENABLED, "updateControls");
                break;
            }
            case PLAYING: {
                LogHelper.v(TAG, "updateControls - PLAYING");
                managePlaybackControls(ControlsState.ENABLED_SHOW_PAUSE, "updateControls");
                break;
            }
            case PAUSED: {
                LogHelper.v(TAG, "updateControls - PAUSED");
                managePlaybackControls(ControlsState.ENABLED_SHOW_PAUSE, "updateControls");
                break;
            }
        }
    }

    private void heardAllEpisodes() {
        AlertDialog alertDialog = new AlertDialog.Builder(AutoplayActivity.this).create();
        alertDialog.setTitle(getResources().getString(R.string.nothing_unheard));
        alertDialog.setMessage(getResources().getString(R.string.heard_everything));
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    private void problemLoadingMetadata() {
        LogHelper.v(TAG, "problemLoadingMetadata");
        mCurrentPosition = 0;
        AlertDialog alertDialog = new AlertDialog.Builder(AutoplayActivity.this).create();
        alertDialog.setTitle(getResources().getString(R.string.no_metadata));
        alertDialog.setMessage(getResources().getString(R.string.metadata_loading_problem));
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    private void problemWithPlayback() {
        LogHelper.v(TAG, "problemWithPlayback");
        mCurrentPosition = 0;
        AlertDialog alertDialog = new AlertDialog.Builder(AutoplayActivity.this).create();
        alertDialog.setTitle(getResources().getString(R.string.unable_to_load));
        alertDialog.setMessage(getResources().getString(R.string.playback_problem));
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogHelper.v(TAG, "onCreate");

        mMediaId = null;
        mAudioFocusRequstResult = 0;
        mDuration = DEFAULT_DURATION; // one-hour in ms
        mHaveRealDuration = false;
        mCurrentPosition = 0;
        setAutoplayState(AutoplayState.READY2PLAY, "onCreate");

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            LogHelper.v(TAG, "rotation event");
            mHandleRotationEvent = true;
        }

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
        mAutoPlay.setVisibility(View.INVISIBLE);
        mAutoPlay.setEnabled(false);
        //final Drawable pressedButton = ResourcesCompat.getDrawable(getResources(), R.drawable.big_button_pressed, null);
        final Drawable pressedButton = (mAutoplayState == AutoplayState.PLAYING)
                ? ResourcesCompat.getDrawable(getResources(), R.drawable.pause_disabled, null)
                : ResourcesCompat.getDrawable(getResources(), R.drawable.autoplay_disabled, null);
        mAutoPlay.setOnTouchListener(new OnSwipeTouchListener(this, mHandler, mAutoPlay, pressedButton) {

            @Override
            public void onClick() {
                LogHelper.v(TAG, "onClick");
                handleAutoplayClick();
            }

            @Override
            public void onDoubleClick() {
                LogHelper.v(TAG, "onDoubleClick");
                if (mCurrentPosition > THIRTY_SECONDS) {
                    getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            mCurrentPosition -= THIRTY_SECONDS;
                            mMediaController.getTransportControls().seekTo(mCurrentPosition);
                        }
                    });
                }
            }

            @Override
            public void onLongClick(final Drawable buttonImage) {
                LogHelper.v(TAG, "onLongClick");
                if (mMediaController != null) {
                    MediaControllerCompat.TransportControls controls = mMediaController.getTransportControls();
                    controls.stop();
                }
                getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        LogHelper.v(TAG, "put back the Autoplay button.");
                        mCurrentPosition = 0;
                        setAutoplayState(AutoplayState.READY2PLAY, "onLongClick");
                        mAutoPlay.setBackgroundDrawable(buttonImage);
                        mAutoPlay.setVisibility(View.VISIBLE);
                        mAutoPlay.invalidate();
                        managePlaybackControls(ControlsState.ENABLED_SHOW_PAUSE, "onLongClick");
                    }
                });
            }
        });

        mFabActionButton = (FloatingActionButton) findViewById(R.id.fab);
        if (mFabActionButton != null) {
            final AutoplayActivity activity = this;
            mFabActionButton.setOnTouchListener(new OnSwipeTouchListener(this) {

                @Override
                public void onClick() {
                    if (!mCircularSeekBar.isProcessingTouchEvents() && !mSeeking) {
                        LogHelper.v(TAG, "onClick - mFabActionButton");
                        Intent episodeListIntent = new Intent(activity, EpisodeListActivity.class);
                        Bundle playInfo = new Bundle();
                        savePlayInfoToBundle(playInfo);
                        episodeListIntent.putExtras(playInfo);
                        startActivity(episodeListIntent);
                    }
                    else {
                        LogHelper.v(TAG, "isProcessingTouchEvents or seeking - onClick ignored");
                    }
                }
            });
        }

        mCircleView = (CircleProgressView) findViewById(R.id.autoplay_circle_view);

        // create a new View for the seek-bar and add it into the main_frame
        FrameLayout theFrame = (FrameLayout) findViewById(R.id.main_frame);
        mCircularSeekBar = new CircularSeekBar(this);
        mCircularSeekBar.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        mCircularSeekBar.setBarWidth(5);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mCircularSeekBar.setBackgroundColor(getResources().getColor(R.color.transparent, null));
        }
        else {
            mCircularSeekBar.setBackgroundColor(getResources().getColor(R.color.transparent));
        }
        mCircularSeekBar.setMaxProgress((int) mDuration);
        mCircularSeekBar.setProgress(0);
        mCircularSeekBar.setVisibility(View.INVISIBLE);
        mCircularSeekBar.setEnabled(true);
        mCircularSeekBar.showSeekBar();
        theFrame.addView(mCircularSeekBar);
        mCircularSeekBar.invalidate();

        mCircularSeekBar.setSeekBarChangeListener(new CircularSeekBar.OnSeekChangeListener() {

            @Override
            public void onProgressChange(CircularSeekBar view, final int newProgress) {
                LogHelper.v(TAG, "onProgressChange: newProgress:" + newProgress);
                mBeginLoading = true;
                if (!mCircularSeekBar.isProcessingTouchEvents() && !mSeeking) {
                    scheduleSeekbarUpdate();
                    getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            mSeeking = true;
                            mCurrentPosition = newProgress;
                            managePlaybackControls(ControlsState.SEEKING_POSITION, "setSeekBarChangeListener");
                            mMediaController.getTransportControls().seekTo(newProgress);
                        }
                    });
                }
            }
        });

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

        mLoadingScreenEnabled = true;
    }

    private void handleAutoplayClick() {
        LogHelper.v(TAG, "handleAutoplayClick");
        mAutoPlay.setEnabled(false);
        if (!mCircularSeekBar.isProcessingTouchEvents() && !mSeeking) {
            LogHelper.v(TAG, "do autoPlay");
            boolean foundEpisode = false;
            AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.toolbar_container);

            ConfigEpisodesCursor configCursor = getCursorForNextAvailableEpisode();
            if (configCursor != null && configCursor.moveToNext()) {
                // found the next episode to listen to
                appBarLayout.setExpanded(false);
                mEpisodeNumber = configCursor.getFieldEpisodeNumber();
                mPurchased = configCursor.getFieldPurchasedAccess();
                mNoAdsForShow = configCursor.getFieldPurchasedNoads();
                mDownloaded = configCursor.getFieldEpisodeDownloaded();
                mEpisodeHeard = configCursor.getFieldEpisodeHeard();
                configCursor.close();

                // get this episode's detail info
                EpisodesCursor episodesCursor = getEpisodesCursor(mEpisodeNumber);
                if (episodesCursor != null && episodesCursor.moveToNext()) {
                    mEpisodeNumber = episodesCursor.getFieldEpisodeNumber();
                    mAirdate = episodesCursor.getFieldAirdate();
                    mEpisodeTitle = episodesCursor.getFieldEpisodeTitle();
                    mEpisodeDescription = episodesCursor.getFieldEpisodeDescription();
                    mEpisodeWeblinkUrl = Uri.parse(episodesCursor.getFieldWeblinkUrl()).getEncodedPath();
                    mEpisodeDownloadUrl = Uri.parse("http://" + episodesCursor.getFieldDownloadUrl()).toString();
                    episodesCursor.close();
                    foundEpisode = true;
                    playPauseEpisode();
                }
            }
            if (!foundEpisode) {
                LogHelper.v(TAG, "popup alert - ALL EPISODES ARE HEARD!");
                heardAllEpisodes();
                appBarLayout.setExpanded(true);
            }
        }
        else {
            LogHelper.v(TAG, "isProcessingTouchEvents or seeking - onClick ignored");
        }
    }

    private void playPauseEpisode() {
        LogHelper.v(TAG, "playPauseEpisode");
        managePlaybackControls(ControlsState.DISABLED_SHOW_PAUSE, "playPauseEpisode");
        showCurrentInfo();
        MediaControllerCompat.TransportControls controls = mMediaController.getTransportControls();
        PlaybackStateCompat lastPlaybackState = mMediaController.getPlaybackState();
        LogHelper.v(TAG, "playPauseEpisode: lastPlaybackState="+lastPlaybackState.getState());
        switch (lastPlaybackState.getState()) {
            case PlaybackStateCompat.STATE_BUFFERING:
            case PlaybackStateCompat.STATE_PLAYING: {
                LogHelper.v(TAG, "controls.pause();");
                controls.pause();
                setAutoplayState(AutoplayState.PAUSED, "playPauseEpisode - PAUSED");
                break;
            }
            case PlaybackStateCompat.STATE_PAUSED:
            case PlaybackStateCompat.STATE_STOPPED: {
                LogHelper.v(TAG, "controls.play();");
                controls.play();
                setAutoplayState(AutoplayState.PLAYING, "playPauseEpisode - PLAYING");
                break;
            }
            default: {
                if (mAutoplayState == AutoplayState.LOADING) {
                    LogHelper.v(TAG, "setup for auto-playing..");
                    loadingScreen();
                    mMediaId = RadioTheaterApplication.getRadioTheaterApplicationContext().getResources().getString(R.string.genre);
                    Uri mediaUri = Uri.parse(mEpisodeDownloadUrl);
                    LogHelper.d(TAG, "*** INITIALIZE PLAYBACK *** state=" + lastPlaybackState.getState() + ", mMediaId=" + mMediaId + ", mediaUri=" + mediaUri);
                    String id = String.valueOf(mEpisodeDownloadUrl.hashCode());
                    String episodeMediaId = MediaIDHelper.createMediaID(id, MediaIDHelper.MEDIA_ID_MUSICS_BY_GENRE, mMediaId);
                    Bundle bundle = new Bundle();
                    mMediaController.getTransportControls().playFromMediaId(episodeMediaId, bundle);
                }
                else {
                    LogHelper.v(TAG, "*** START PLAYBACK *** state: " + lastPlaybackState.getState());
                    LogHelper.v(TAG, "controls.play();");
                    controls.play();
                    setAutoplayState(AutoplayState.PLAYING, "playPauseEpisode - PLAYING");
                }
            }
        }
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
                Bundle playInfo = new Bundle();
                savePlayInfoToBundle(playInfo);
                intent.putExtras(playInfo);
                startActivity(intent);
                return true;
            }
            case R.id.about: {
                Intent intent = new Intent(this, AboutActivity.class);
                Bundle playInfo = new Bundle();
                savePlayInfoToBundle(playInfo);
                intent.putExtras(playInfo);
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
        if (mHandleRotationEvent) {
            LogHelper.v(TAG, "*** RESTORE PLAYBACK STATE AFTER ROTATION ***");
            getHandler().post(new Runnable() {
                @Override
                public void run() {
                    if (mAutoplayState != AutoplayState.READY2PLAY || mSeeking || mPlaying) {
                        LogHelper.v(TAG, "RESTORE PLAYBACK - EPISODE: "+mEpisodeTitle);
                        MediaControllerCompat.TransportControls controls = mMediaController.getTransportControls();
                        controls.play();
                        scheduleSeekbarUpdate();
                        setAutoplayState(AutoplayState.PLAYING, "playPauseEpisode - PLAYING");
                    }
                    else {
                        showCurrentInfo();
                    }
                    if (mSeeking) {
                        LogHelper.v(TAG, "RESTORE PLAYBACK - SEEKING TO: "+mCurrentPosition);
                        mMediaController.getTransportControls().seekTo(mCurrentPosition);
                    }
                }
            });
        }
    }

    private void stopSeekbarUpdate() {
        LogHelper.v(TAG, "stopSeekbarUpdate");
        if (mScheduleFuture != null) {
            mSeekUpdateRunning = false;
            mScheduleFuture.cancel(false);
        }
    }

    private void scheduleSeekbarUpdate() {
        if (! mSeekUpdateRunning) {
            LogHelper.v(TAG, "scheduleSeekbarUpdate");
            if (!mExecutorService.isShutdown()) {
                mScheduleFuture = mExecutorService.scheduleAtFixedRate(
                        new Runnable() {
                            @Override
                            public void run() {
                                LogHelper.v(TAG, "post mUpdateProgressTask");
                                mSeekUpdateRunning = true;
                                mHandler.post(mUpdateProgressTask);
                            }
                        }, PROGRESS_UPDATE_INITIAL_INTERVAL,
                        PROGRESS_UPDATE_INTERNAL, TimeUnit.MILLISECONDS);
            }
        }
    }

    private void updateMediaDescription(MediaDescriptionCompat description) {
        LogHelper.d(TAG, "updateMediaDescription: description="+description);
        // FIXME: update description
    }

    private void updateDuration(MediaMetadataCompat metadata) {
        LogHelper.d(TAG, "updateDuration");
        if (metadata == null) {
            return;
        }
        if (! mHaveRealDuration) {
            mDuration = (int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
        }
        LogHelper.v(TAG, "===> EPISODE DURATION="+mDuration/1000+", mHaveRealDuration="+mHaveRealDuration);
        mCircularSeekBar.setMaxProgress((int) mDuration);
        managePlaybackControls(ControlsState.ENABLED_SHOW_PAUSE, "updateDuration");
    }

    protected void loadingScreen() {
        LogHelper.d(TAG, "loadingScreen: enabled="+mLoadingScreenEnabled);
        if (mLoadingScreenEnabled && !LoadingAsyncTask.mLoadingNow && mAutoplayState == AutoplayState.READY2PLAY) {
            mBeginLoading = true;
            managePlaybackControls(ControlsState.DISABLED_SHOW_PAUSE, "loadingScreen");
            LogHelper.v(TAG, "*** -------------------------------------------------------------------------------- ***");
            setAutoplayState(AutoplayState.LOADING, "loadingScreen");
            LoadingAsyncTask asyncTask = new LoadingAsyncTask(this, mCircleView, mCircularSeekBar, mAutoPlay);
            asyncTask.execute();
            LogHelper.v(TAG, "*** -------------------------------------------------------------------------------- ***");
        }
    }

    @Override
    public void setCircleViewValue(float value) {
        LogHelper.v(TAG, "setCircleViewValue: "+value);
        super.setCircleViewValue(value);
        mCurrentPosition = (long) value;
        if (value != 0) {
            mBeginLoading = false;
            LoadingAsyncTask.mDoneLoading = true;
            managePlaybackControls(value == mCircleView.getMaxValue() ? ControlsState.DISABLED_SHOW_PAUSE : ControlsState.ENABLED_SHOW_PAUSE, "setCircleViewValue - PLAYING");
        }
        else {
            managePlaybackControls(ControlsState.DISABLED_SHOW_PAUSE, "setCircleViewValue - STILL LOADING");
        }
    }

    @Override
    protected void onResume() {
        LogHelper.d(TAG, "onResume");
        super.onResume();
        IntentFilter intentFilter = new IntentFilter("android.intent.action.MAIN");

        mReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                final String message = intent.getStringExtra("initialization"); // did metadata load ok?
                LogHelper.v(TAG, "*** RECEIVED BROADCAST: "+message);
                String load_fail = getResources().getString(R.string.error_no_metadata);
                String load_ok = getResources().getString(R.string.metadata_loaded);
                final String duration = getResources().getString(R.string.duration);
                if (message.equals(load_ok)) {
                    LogHelper.v(TAG, load_ok);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            LogHelper.v(TAG, "*** RECEIVED BROADCAST: LOAD_OK");
                            mLoadedOK = true;
                            enableButtons();
                            managePlaybackControls(ControlsState.ENABLED, "onReceive");
                        }
                    });
                }
                else if (message.equals(load_fail)) {
                    LogHelper.v(TAG, load_fail);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            LogHelper.v(TAG, "*** RECEIVED BROADCAST: LOAD_FAIL");
                            problemLoadingMetadata();
                        }
                    });
                }
                else if (message.substring(0, duration.length()).equals(duration)) {
                    LogHelper.v(TAG, duration);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            LogHelper.v(TAG, "*** RECEIVED BROADCAST: DURATION");
                            mDuration = Long.valueOf(message.substring(duration.length(), message.length()));
                            mHaveRealDuration = true;
                            mCircularSeekBar.setMaxProgress((int) mDuration);
                            LogHelper.v(TAG, "*** REVISED EPISODE DURATION="+mDuration);
                            do_UpdateControls();
                        }
                    });
                }
                else {
                    LogHelper.v(TAG, "*** UNKNOWN MESSAGE VIA INTENT: "+message);
                }
            }
        };
        this.registerReceiver(mReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        LogHelper.d(TAG, "onPause");
        super.onPause();
        this.unregisterReceiver(mReceiver);
    }

    @Override
    protected void onStart() {
        LogHelper.d(TAG, "onStart");
        super.onStart();
        if (mAutoplayState == AutoplayState.PLAYING) {
            LogHelper.v(TAG, "*** we need to keep playing from where we left off.. mNeed2RestartPlayback = true;");
            mNeed2RestartPlayback = true;
        }
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
        stopSeekbarUpdate();
        mExecutorService.shutdown();
        mAudioManager = null;
        mAutoPlay = null;
        mCircularSeekBar = null;
        mAudioManager = null;
        mMediaBrowser = null;
        mMediaController = null;
        mScheduleFuture = null;
    }

    // it seems the easier one wants to make a program to use, the more complex it becomes internally.
    // the purpose of the next two blocks of code is to intelligently present controls (or not) depending on state.
    public void managePlaybackControls(final ControlsState controlState, String log) {
        LogHelper.d(TAG, "managePlaybackControls: controlState="+ controlState +" - "+log);
        if (controlState == ControlsState.SEEKING_POSITION) {
            LogHelper.v(TAG, "manage - SEEKING_POSITION");
            Drawable pauseButton = ResourcesCompat.getDrawable(getResources(), R.drawable.radio_theater_pause_disabled_button_selector, null);
            mAutoPlay.setBackground(pauseButton);
            mAutoPlay.setEnabled(false);
            mFabActionButton.setEnabled(false);
        }
        else if (mBeginLoading || LoadingAsyncTask.mLoadingNow || mAudioFocusRequstResult != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            LogHelper.v(TAG, "manage - WAIT UNTIL FINISHED");
            mAutoPlay.setEnabled(false);
            mFabActionButton.setEnabled(false);
        }
        else {
            switch (mAutoplayState) {
                case READY2PLAY: {
                    LogHelper.v(TAG, "manage - READY2PLAY");
                    showExpectedControls(controlState);
                    break;
                }
                case LOADING: {
                    LogHelper.v(TAG, "manage - LOADING");
                    mCircularSeekBar.setVisibility(View.INVISIBLE);
                    mAutoPlay.setEnabled(false);
                    mFabActionButton.setEnabled(false);
                    break;
                }
                case PLAYING: {
                    LogHelper.v(TAG, "manage - PLAYING");
                    showExpectedControls(controlState);
                    break;
                }
                case PAUSED: {
                    LogHelper.v(TAG, "manage - PAUSED");
                    showExpectedControls(controlState);
                    break;
                }
            }
        }
        mAutoPlay.invalidate(); // fixes a draw bug in Android
    }

    private void showExpectedControls(ControlsState controlState) {
        if (LoadingAsyncTask.mLoadingNow) {
            if (controlState == ControlsState.ENABLED) {
                controlState = ControlsState.DISABLED;
            }
            else if (controlState == ControlsState.ENABLED_SHOW_PAUSE) {
                controlState = ControlsState.DISABLED_SHOW_PAUSE;
            }
        }
        LogHelper.d(TAG, "showExpectedControls: controlState="+ controlState);
        switch (controlState) {
            case ENABLED: {
                if (mAutoplayState == AutoplayState.PLAYING && mCurrentPosition > 0) {
                    Drawable pauseButton = ResourcesCompat.getDrawable(getResources(), R.drawable.radio_theater_pause_button_selector, null);
                    mAutoPlay.setBackground(pauseButton);
                    mCircularSeekBar.setVisibility(View.VISIBLE);
                }
                else if (mCurrentPosition > 0){
                    Drawable resumeButton = ResourcesCompat.getDrawable(getResources(), R.drawable.radio_theater_resume_button_selector, null);
                    mAutoPlay.setBackground(resumeButton);
                    mCircularSeekBar.setVisibility(View.INVISIBLE);
                }
                else {
                    Drawable autoplayButton = ResourcesCompat.getDrawable(getResources(), R.drawable.radio_theater_autoplay_button_selector, null);
                    mAutoPlay.setBackground(autoplayButton);
                    mCircularSeekBar.setVisibility(View.INVISIBLE);
                }
                mAutoPlay.setEnabled(true);
                mFabActionButton.setEnabled(true);
                break;
            }
            case ENABLED_SHOW_PAUSE: {
                if (mAutoplayState == AutoplayState.PLAYING) {
                    Drawable pauseButton = ResourcesCompat.getDrawable(getResources(), R.drawable.radio_theater_pause_button_selector, null);
                    mAutoPlay.setBackground(pauseButton);
                    mCircularSeekBar.setVisibility(View.VISIBLE);
                }
                else {
                    Drawable resumeButton = ResourcesCompat.getDrawable(getResources(), R.drawable.radio_theater_resume_button_selector, null);
                    mAutoPlay.setBackground(resumeButton);
                    mCircularSeekBar.setVisibility(View.INVISIBLE);
                }
                mAutoPlay.setEnabled(true);
                mFabActionButton.setEnabled(true);
                break;
            }
            case DISABLED: {
                if (mAutoplayState == AutoplayState.PLAYING && mCurrentPosition > 0) {
                    Drawable pauseButton = ResourcesCompat.getDrawable(getResources(), R.drawable.radio_theater_pause_disabled_button_selector, null);
                    mAutoPlay.setBackground(pauseButton);
                    mCircularSeekBar.setVisibility(View.VISIBLE);
                }
                else if (mCurrentPosition > 0){
                    Drawable resumeButton = ResourcesCompat.getDrawable(getResources(), R.drawable.radio_theater_resume_button_selector, null);
                    mAutoPlay.setBackground(resumeButton);
                    mCircularSeekBar.setVisibility(View.INVISIBLE);
                }
                else {
                    Drawable autoplayButton = ResourcesCompat.getDrawable(getResources(), R.drawable.radio_theater_autoplay_disabled_button_selector, null);
                    mAutoPlay.setBackground(autoplayButton);
                    mCircularSeekBar.setVisibility(View.INVISIBLE);
                }
                mAutoPlay.setEnabled(false);
                mFabActionButton.setEnabled(false);
                break;
            }
            case DISABLED_SHOW_PAUSE: {
                Drawable pauseButton = ResourcesCompat.getDrawable(getResources(), R.drawable.radio_theater_pause_disabled_button_selector, null);
                mAutoPlay.setBackground(pauseButton);
                if (mAutoplayState == AutoplayState.PLAYING) {
                    mCircularSeekBar.setVisibility(View.VISIBLE);
                }
                else {
                    mCircularSeekBar.setVisibility(View.INVISIBLE);
                }
                mAutoPlay.setEnabled(false);
                mFabActionButton.setEnabled(false);
                break;
            }
        }
    }

}
