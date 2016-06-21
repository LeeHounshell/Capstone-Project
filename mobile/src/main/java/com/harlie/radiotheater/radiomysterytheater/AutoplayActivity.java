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
import com.harlie.radiotheater.radiomysterytheater.utils.LogHelper;
import com.harlie.radiotheater.radiomysterytheater.utils.MediaIDHelper;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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

    private static final long PROGRESS_UPDATE_INTERNAL = 1000;
    private static final long PROGRESS_UPDATE_INITIAL_INTERVAL = 100;
    private static final long DEFAULT_DURATION = 60 * 60 * 1000;

    public enum AutoplayState {
        PLAY, LOADING, PAUSE
    }
    AutoplayState mAutoplayState = AutoplayState.PLAY;

    private AppCompatButton mAutoPlay;
    private CircularSeekBar mCircularSeekBar;
    private AudioManager mAudioManager;
    private MediaBrowserCompat mMediaBrowser;
    private MediaControllerCompat mMediaController;
    private ScheduledFuture<?> mScheduleFuture;
    private PlaybackStateCompat mLastPlaybackState;
    private String mMediaId;
    private int mAudioFocusRequstResult;
    private long mDuration;
    private long mCurrentPosition;
    private boolean mSeeking;

    private final Handler mHandler = new Handler();
    private BroadcastReceiver mReceiver;

    private final ScheduledExecutorService mExecutorService =
            Executors.newSingleThreadScheduledExecutor();

    private final Runnable mUpdateProgressTask = new Runnable() {
        @Override
        public void run() {
            if (!mCircularSeekBar.isProcessingTouchEvents() && !mSeeking && mLastPlaybackState != null) {
                mCurrentPosition = mLastPlaybackState.getPosition();
                if (mLastPlaybackState != null && mLastPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING) {
                    updateProgress();
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
                }
                else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                    LogHelper.v(TAG, "AudioManager.AUDIOFOCUS_LOSS <<---");
                    mAudioFocusRequstResult = 0;
                    controls.stop();
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
                        hidePlaybackControls();
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
                    mLastPlaybackState = state;
                    if (mSeeking && mLastPlaybackState.getState() != PlaybackStateCompat.STATE_PLAYING) {
                        LogHelper.v(TAG, "ignoring onPlaybackStateChanged until seekTo completes");
                    }
                    else if (!mCircularSeekBar.isProcessingTouchEvents()) {
                        LogHelper.d(TAG, "MediaControllerCompat.Callback onPlaybackStateChanged state=" + mLastPlaybackState + " <<<---------");
                        mSeeking = false;
                        updatePlaybackState();
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
                    mLastPlaybackState = mMediaController.getPlaybackState();
                    updatePlaybackState();
                }
            };

    private void updatePlaybackState() {
        LogHelper.v(TAG, "updatePlaybackState");
        if (mLastPlaybackState == null) {
            return;
        }
        switch (mLastPlaybackState.getState()) {
            case PlaybackStateCompat.STATE_PLAYING:
                updateControls();
                scheduleSeekbarUpdate();
                break;
            case PlaybackStateCompat.STATE_PAUSED:
                hidePlaybackControls();
                stopSeekbarUpdate();
                break;
            case PlaybackStateCompat.STATE_NONE:
            case PlaybackStateCompat.STATE_STOPPED:
                hidePlaybackControls();
                stopSeekbarUpdate();
                break;
            case PlaybackStateCompat.STATE_BUFFERING:
                hidePlaybackControls();
                stopSeekbarUpdate();
                break;
            default:
                LogHelper.d(TAG, "unhandled state ", mLastPlaybackState.getState());
                if (mAutoplayState == AutoplayState.LOADING) {
                    LogHelper.v(TAG, "AutoplayState.LOADING - need to reset");
                    mAutoplayState = AutoplayState.PLAY;
                    hidePlaybackControls();
                    problemWithPlayback();
                }
        }
    }

    private void problemLoadingMetadata() {
        LogHelper.v(TAG, "problemLoadingMetadata");
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
        super.onCreate(savedInstanceState);

        mMediaId = null;
        mAudioFocusRequstResult = 0;
        mDuration = DEFAULT_DURATION; // one-hour in ms
        mCurrentPosition = 0;

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
                if (!mCircularSeekBar.isProcessingTouchEvents() && !mSeeking) {
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
                                + ": episodeNumber=" + episodeNumber
                                + ", purchased=" + purchased
                                + ", noAdsForShow=" + noAdsForShow
                                + ", downloaded=" + downloaded
                                + ", episodeHeard=" + episodeHeard
                                + ", listenCount=" + listenCount);

                        // get this episode's detail info
                        EpisodesCursor episodesCursor = getEpisodesCursor(episodeNumber);
                        if (episodesCursor != null && episodesCursor.moveToNext()) {
                            episodeNumber = episodesCursor.getFieldEpisodeNumber();
                            String airdate = episodesCursor.getFieldAirdate();
                            String episodeTitle = episodesCursor.getFieldEpisodeTitle();
                            String episodeDescription = episodesCursor.getFieldEpisodeDescription();
                            String episodeWeblinkUrl = Uri.parse(episodesCursor.getFieldWeblinkUrl()).getEncodedPath();
                            String episodeDownloadUrl = Uri.parse("http://" + episodesCursor.getFieldDownloadUrl()).toString();
                            Float rating = episodesCursor.getFieldRating();
                            mDuration = DEFAULT_DURATION; // one-hour in ms
                            Integer voteCount = episodesCursor.getFieldVoteCount();
                            episodesCursor.close();
                            foundEpisode = true;

                            LogHelper.v(TAG, "===> EPISODE DETAIL"
                                    + ": episodeNumber=" + episodeNumber
                                    + ": airdate=" + airdate
                                    + ": episodeTitle=" + episodeTitle
                                    + ": episodeDescription=" + episodeDescription
                                    + ": episodeWeblinkUrl=" + episodeWeblinkUrl
                                    + ": episodeDownloadUrl=" + episodeDownloadUrl
                                    + ": rating=" + rating
                                    + ": voteCount=" + voteCount);

                            playPauseEpisode(episodeNumber, episodeTitle, episodeDownloadUrl, purchased, downloaded);
                        }
                    }
                    if (!foundEpisode) {
                        LogHelper.v(TAG, "popup alert - ALL EPISODES ARE HEARD!");
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
                        appBarLayout.setExpanded(true);
                    }
                } else {
                    LogHelper.v(TAG, "isProcessingTouchEvents or seeking - onClick ignored");
                }
            }
        });
        mAutoplayState = AutoplayState.PLAY;

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            final AutoplayActivity activity = this;
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!mCircularSeekBar.isProcessingTouchEvents() && !mSeeking) {
                        LogHelper.v(TAG, "onClick - fab");
                        Intent episodeListIntent = new Intent(activity, EpisodeListActivity.class);
                        startActivity(episodeListIntent);
                        // Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        //         .setAction("Action", null).show();
                    }
                    else {
                        LogHelper.v(TAG, "isProcessingTouchEvents or seeking - onClick ignored");
                    }
                }
            });
        }

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
        mDuration = DEFAULT_DURATION; // one-hour in ms
        mCircularSeekBar.setMaxProgress((int) mDuration);
        mCircularSeekBar.setProgress(0);
        mCircularSeekBar.setVisibility(View.INVISIBLE);
        mCircularSeekBar.setEnabled(true);
        mCircularSeekBar.showSeekBar();
        theFrame.addView(mCircularSeekBar);
        mCircularSeekBar.invalidate();

        mCircularSeekBar.setSeekBarChangeListener(new CircularSeekBar.OnSeekChangeListener() {

            @Override
            public void onProgressChange(CircularSeekBar view, int newProgress) {
                if (!mCircularSeekBar.isProcessingTouchEvents() && !mSeeking) {
                    LogHelper.v(TAG, "onProgressChange: newProgress:" + newProgress);
                    mSeeking = true;
                    mCurrentPosition = newProgress;
                    mMediaController.getTransportControls().seekTo(newProgress);
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
    }

    private void playPauseEpisode(long episodeNumber, String episodeTitle, String episodeDownloadUrl, boolean purchased, boolean downloaded) {
        LogHelper.v(TAG, "playPauseEpisode: episodeNumber="+episodeNumber
                +", episodeTitle="+episodeTitle
                +", episodeDownloadUrl="+episodeDownloadUrl
                +", purchased="+purchased
                +", downloaded="+downloaded);

        mLastPlaybackState = mMediaController.getPlaybackState();
        if (mLastPlaybackState != null) {
            MediaControllerCompat.TransportControls controls = mMediaController.getTransportControls();
            switch (mLastPlaybackState.getState()) {
                case PlaybackStateCompat.STATE_BUFFERING: // fall through
                case PlaybackStateCompat.STATE_PLAYING:
                    LogHelper.v(TAG, "controls.pause();");
                    controls.pause();
                    stopSeekbarUpdate();
                    break;
                case PlaybackStateCompat.STATE_PAUSED:
                case PlaybackStateCompat.STATE_STOPPED:
                    LogHelper.v(TAG, "controls.play();");
                    controls.play();
                    scheduleSeekbarUpdate();
                    break;
                default:
                    if (mAutoplayState == AutoplayState.PLAY) {
                        LogHelper.v(TAG, "start auto-playing..");
                        loadingPlaybackControls();
                        mMediaId = RadioTheaterApplication.getRadioTheaterApplicationContext().getResources().getString(R.string.genre);
                        Uri mediaUri = Uri.parse(episodeDownloadUrl);
                        LogHelper.d(TAG, "*** START PLAYBACK *** state=" + mLastPlaybackState.getState() + ", mMediaId=" + mMediaId + ", mediaUri=" + mediaUri);
                        String id = String.valueOf(episodeDownloadUrl.hashCode());
                        String episodeMediaId = MediaIDHelper.createMediaID(id, MediaIDHelper.MEDIA_ID_MUSICS_BY_GENRE, mMediaId);
                        Bundle bundle = new Bundle();
                        mMediaController.getTransportControls().playFromMediaId(episodeMediaId, bundle);
                    }
                    else {
                        LogHelper.v(TAG, "stop auto-playing..");
                        controls.stop();
                        stopSeekbarUpdate();
                        mAutoplayState = AutoplayState.PLAY;
                        hidePlaybackControls();
                        mAudioFocusRequstResult = 0;
                        mCurrentPosition = 0;
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
    }

    private void stopSeekbarUpdate() {
        LogHelper.v(TAG, "stopSeekbarUpdate");
        if (mScheduleFuture != null) {
            mScheduleFuture.cancel(false);
        }
    }

    private void scheduleSeekbarUpdate() {
        LogHelper.v(TAG, "scheduleSeekbarUpdate");
        stopSeekbarUpdate();
        if (!mExecutorService.isShutdown()) {
            mScheduleFuture = mExecutorService.scheduleAtFixedRate(
                    new Runnable() {
                        @Override
                        public void run() {
                            mHandler.post(mUpdateProgressTask);
                        }
                    }, PROGRESS_UPDATE_INITIAL_INTERVAL,
                    PROGRESS_UPDATE_INTERNAL, TimeUnit.MILLISECONDS);
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
        mDuration = (int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
        LogHelper.v(TAG, "===> EPISODE DURATION="+mDuration/1000);
        mCircularSeekBar.setMaxProgress((int) mDuration);
        showPlaybackControls();
        if (mLastPlaybackState != null) {
            mCurrentPosition = mLastPlaybackState.getPosition();
            updateProgress();
        }
    }

    protected void showPlaybackControls() {
        if (!mCircularSeekBar.isProcessingTouchEvents() && !mSeeking) {
            LogHelper.d(TAG, "showPlaybackControls");
            if (mAutoplayState != AutoplayState.LOADING) {
                Drawable pauseButton = ResourcesCompat.getDrawable(getResources(), R.drawable.radio_theater_pause_button_selector, null);
                mAutoPlay.setBackground(pauseButton);
                mCircularSeekBar.setVisibility(View.VISIBLE);
            }
        }
    }

    protected void hidePlaybackControls() {
        if (!mCircularSeekBar.isProcessingTouchEvents() && !mSeeking) {
            LogHelper.d(TAG, "hidePlaybackControls");
            if (mAutoplayState != AutoplayState.LOADING) {
                if (mCurrentPosition > 0) {
                    Drawable resumeButton = ResourcesCompat.getDrawable(getResources(), R.drawable.radio_theater_resume_button_selector, null);
                    mAutoPlay.setBackground(resumeButton);
                }
                else {
                    Drawable autoplayButton = ResourcesCompat.getDrawable(getResources(), R.drawable.radio_theater_autoplay_button_selector, null);
                    mAutoPlay.setBackground(autoplayButton);
                }
            }
            mCircularSeekBar.setVisibility(View.INVISIBLE);
        }
    }

    protected void loadingPlaybackControls() {
        LogHelper.d(TAG, "loadingPlaybackControls");
        Drawable loadingButton = ResourcesCompat.getDrawable(getResources(), R.drawable.radio_theater_loading_button_selector, null);
        mAutoPlay.setBackground(loadingButton);
        mAutoplayState = AutoplayState.LOADING;
    }

    /**
     * Check if the MediaSession is active and in a "playback-able" state
     * (not NONE and not STOPPED).
     *
     * @return true if the MediaSession's state requires playback controls to be visible.
     */
    protected void updateControls() {
        LogHelper.v(TAG, "updateControls");
        if (mMediaController == null ||
                mMediaController.getMetadata() == null ||
                mMediaController.getPlaybackState() == null) {
            return;
        }
        mLastPlaybackState = mMediaController.getPlaybackState();
        switch (mLastPlaybackState.getState()) {
            case PlaybackStateCompat.STATE_BUFFERING:
                LogHelper.v(TAG, "PlaybackStateCompat.STATE_BUFFERING");
                loadingPlaybackControls();
                return;
            case PlaybackStateCompat.STATE_CONNECTING:
                LogHelper.v(TAG, "PlaybackStateCompat.STATE_CONNECTING");
                loadingPlaybackControls();
                return;
            case PlaybackStateCompat.STATE_ERROR:
                LogHelper.v(TAG, "PlaybackStateCompat.STATE_ERROR");
                hidePlaybackControls();
                return;
            case PlaybackStateCompat.STATE_FAST_FORWARDING:
                LogHelper.v(TAG, "PlaybackStateCompat.STATE_FORWARDING");
                return;
            case PlaybackStateCompat.STATE_NONE:
                LogHelper.v(TAG, "PlaybackStateCompat.STATE_NONE");
                hidePlaybackControls();
                return;
            case PlaybackStateCompat.STATE_PAUSED:
                LogHelper.v(TAG, "PlaybackStateCompat.STATE_PAUSED");
                mAutoplayState = AutoplayState.PLAY;
                hidePlaybackControls();
                return;
            case PlaybackStateCompat.STATE_PLAYING:
                LogHelper.v(TAG, "PlaybackStateCompat.STATE_PLAYING");
                mAutoplayState = AutoplayState.PAUSE;
                showPlaybackControls();
                return;
            case PlaybackStateCompat.STATE_REWINDING:
                LogHelper.v(TAG, "PlaybackStateCompat.STATE_REWINDING");
                return;
            case PlaybackStateCompat.STATE_SKIPPING_TO_NEXT:
                LogHelper.v(TAG, "PlaybackStateCompat.STATE_SKIPPING_TO_NEXT");
                return;
            case PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS:
                LogHelper.v(TAG, "PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS");
                return;
            case PlaybackStateCompat.STATE_SKIPPING_TO_QUEUE_ITEM:
                LogHelper.v(TAG, "PlaybackStateCompat.STATE_SKIPPING_TO_QUEUE_ITEM");
                return;
            case PlaybackStateCompat.STATE_STOPPED:
                LogHelper.v(TAG, "PlaybackStateCompat.STATE_STOPPED");
                hidePlaybackControls();
                return;
            default:
                LogHelper.v(TAG, "PlaybackStateCompat.STATE_DEFAULT");
                hidePlaybackControls();
        }
    }

    private void updateProgress() {
        if (!mCircularSeekBar.isProcessingTouchEvents() && !mSeeking) {
            if (mLastPlaybackState == null) {
                mLastPlaybackState = mMediaController.getPlaybackState();
            }
            if (mLastPlaybackState.getState() != PlaybackStateCompat.STATE_PAUSED) {
                // Calculate the elapsed time between the last position update and now and unless
                // paused, we can assume (delta * speed) + current position is approximately the
                // latest position. This ensure that we do not repeatedly call the getPlaybackState()
                // on MediaControllerCompat.
                long timeDelta = SystemClock.elapsedRealtime() - mLastPlaybackState.getLastPositionUpdateTime();
                mCurrentPosition += (int) timeDelta * mLastPlaybackState.getPlaybackSpeed();
            }
            if (mDuration == 0) { // paranoia
                mDuration = DEFAULT_DURATION; // one-hour in ms
                mCircularSeekBar.setMaxProgress((int) mDuration);
            }
            LogHelper.v(TAG, "updateProgress: mCurrentPosition=" + mCurrentPosition + ", mDuration=" + mDuration);
            mCircularSeekBar.setProgress((int) mCurrentPosition);
            mCircularSeekBar.invalidate();
            mCircularSeekBar.setVisibility(View.VISIBLE);
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
                String message = intent.getStringExtra("initialization"); // did metadata load ok?
                LogHelper.v(TAG, "*** RECEIVED BROADCAST: "+message);
                String load_fail = getResources().getString(R.string.error_no_metadata);
                String load_ok = getResources().getString(R.string.metadata_loaded);
                if (message.equals(load_ok)) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mAutoPlay.setVisibility(View.VISIBLE);
                        }
                    });
                }
                else if (message.equals(load_fail)) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            problemLoadingMetadata();
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
        stopSeekbarUpdate();
        mExecutorService.shutdown();
        mAudioManager = null;
        mAutoPlay = null;
        mCircularSeekBar = null;
        mAudioManager = null;
        mMediaBrowser = null;
        mMediaController = null;
        mScheduleFuture = null;
        mLastPlaybackState = null;
        mMediaId = null;
    }

}
