package com.harlie.radiotheater.radiomysterytheater;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Intent;
import android.content.Context;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.harlie.radiotheater.radiomysterytheater.playback.LocalPlayback;
import com.harlie.radiotheater.radiomysterytheater.utils.LogHelper;

import java.util.List;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread. This service controls RadioTheater playback.
 */
public class RadioControlIntentService extends IntentService {
    private final static String TAG = "LEE: <" + RadioControlIntentService.class.getSimpleName() + ">";

    private static volatile boolean sAlreadyStarted = false;

    private final IBinder mBinder = new RadioControlServiceBinder();
    private Messenger outMessenger;

    public IBinder onBind(Intent arg0) {
        Bundle extras = arg0.getExtras();
        LogHelper.d(TAG,"RadioControlServiceBinder: service - onBind");
        // Get messager from the Activity
        if (extras != null) {
            LogHelper.d(TAG,"RadioControlServiceBinder: service - onBind with extra");
            outMessenger = (Messenger) extras.get("MESSENGER");
        }
        return mBinder;
    }

    public class RadioControlServiceBinder extends Binder {
        RadioControlServiceBinder getService() {
            return RadioControlServiceBinder.this;
        }
    }

    protected MediaControllerCompat mRadioMediaController;
    protected AudioManager mRadioAudioManager;
    protected MediaBrowserCompat mRadioMediaBrowser;
    protected int mAudioFocusRequstResult;
    protected String mMediaId;

    protected AudioManager.OnAudioFocusChangeListener mAudioFocusChangeListener;
    protected MediaBrowserCompat.SubscriptionCallback mSubscriptionCallback;
    protected MediaControllerCompat.Callback mMediaControllerCallback;
    protected MediaBrowserCompat.ConnectionCallback mConnectionCallback;

    // the controls
    private static final String ACTION_START = "com.harlie.radiotheater.radiomysterytheater.action.START";
    private static final String ACTION_PLAY = "com.harlie.radiotheater.radiomysterytheater.action.PLAY";
    private static final String ACTION_PAUSE = "com.harlie.radiotheater.radiomysterytheater.action.PAUSE";
    private static final String ACTION_SEEK = "com.harlie.radiotheater.radiomysterytheater.action.SEEK";
    private static final String ACTION_GOBACK = "com.harlie.radiotheater.radiomysterytheater.action.GOBACK";
    private static final String ACTION_STOP = "com.harlie.radiotheater.radiomysterytheater.action.STOP";
    private static final String ACTION_NEXT = "com.harlie.radiotheater.radiomysterytheater.action.NEXT";
    private static final String ACTION_PREV = "com.harlie.radiotheater.radiomysterytheater.action.PREV";

    // control parameters
    private static final String EXTRA_EPISODE = "com.harlie.radiotheater.radiomysterytheater.extra.EPISODE";
    private static final String EXTRA_PARAM2 = "com.harlie.radiotheater.radiomysterytheater.extra.PARAM2";
    private static final String EXTRA_TITLE = "com.harlie.radiotheater.radiomysterytheater.extra.TITLE";

    public RadioControlIntentService() {
        super("RadioControlIntentService");
    }

    @Override
    public void onCreate() {
        LogHelper.v(TAG, "onCreate");
        super.onCreate();

        // Callback for Audio Focus
        mAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
            public void onAudioFocusChange(int focusChange) {
                LogHelper.v(TAG, "===> onAudioFocusChange=" + focusChange + " <===");
                if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                    LogHelper.v(TAG, "AudioManager.AUDIOFOCUS_GAIN <<---");
                    mAudioFocusRequstResult = AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
                } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                    LogHelper.v(TAG, "AudioManager.AUDIOFOCUS_LOSS <<---");
                    mAudioFocusRequstResult = 0;
                    Context context = RadioTheaterApplication.getRadioTheaterApplicationContext();
                    startActionStop(context, "onAudioFocusChange (loss)", null, null);
                } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                    LogHelper.v(TAG, "AudioManager.AUDIOFOCUS_LOSS_TRANSIENT <<---");
                    mAudioFocusRequstResult = 0;
                    Context context = RadioTheaterApplication.getRadioTheaterApplicationContext();
                    startActionPause(context, "onAudioFocusChange (loss transient)", String.valueOf(BaseActivity.getEpisodeNumber()), BaseActivity.getEpisodeDownloadUrl());
                } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                    LogHelper.v(TAG, "AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK <<---");
                    mAudioFocusRequstResult = 0;
                    Context context = RadioTheaterApplication.getRadioTheaterApplicationContext();
                    startActionPause(context, "onAudioFocusChange (loss can duck)", String.valueOf(BaseActivity.getEpisodeNumber()), BaseActivity.getEpisodeDownloadUrl());
                }
            }
        };

        // Callback for media subscription
        mSubscriptionCallback = new MediaBrowserCompat.SubscriptionCallback() {
            @Override
            public void onChildrenLoaded(@NonNull String parentId,
                                         @NonNull List<MediaBrowserCompat.MediaItem> children) {
                LogHelper.d(TAG, "********* onChildrenLoaded, parentId=" + parentId + " child count=" + children.size());
                if (children == null || children.isEmpty()) {
                    LogHelper.w(TAG, "onChildrenLoaded: NO CHILDREN");
                }
            }

            @Override
            public void onError(@NonNull String id) {
                LogHelper.e(TAG, "browse subscription onError, id=" + id);
            }
        };

        // Callback that ensures that we are showing the controls
        mMediaControllerCallback = new MediaControllerCompat.Callback() {
            @Override
            public void onPlaybackStateChanged(@NonNull PlaybackStateCompat state) {
                LogHelper.v(TAG, "onPlaybackStateChanged: state=" + state.getState());
                switch (state.getState()) {
                    case PlaybackStateCompat.STATE_PLAYING: {
                        LogHelper.d(TAG, "MediaControllerCompat.Callback onPlaybackStateChanged STATE_PLAYING <<<---------");
                        break;
                    }
                    case PlaybackStateCompat.STATE_PAUSED: {
                        LogHelper.d(TAG, "MediaControllerCompat.Callback onPlaybackStateChanged STATE_PAUSED <<<---------");
                        break;
                    }
                    case PlaybackStateCompat.STATE_SKIPPING_TO_NEXT:
                    case PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS: {
                        LogHelper.d(TAG, "MediaControllerCompat.Callback onPlaybackStateChanged STATE_SKIPPING <<<---------");
                        break;
                    }
                    case PlaybackStateCompat.STATE_BUFFERING: {
                        LogHelper.d(TAG, "MediaControllerCompat.Callback onPlaybackStateChanged STATE_BUFFERING <<<---------");
                        break;
                    }
                    case PlaybackStateCompat.STATE_ERROR:
                    case PlaybackStateCompat.STATE_NONE:
                    case PlaybackStateCompat.STATE_STOPPED: {
                        LogHelper.d(TAG, "MediaControllerCompat.Callback onPlaybackStateChanged STATE_STOPPED <<<---------");
                        break;
                    }
                }
            }

            @Override
            public void onMetadataChanged(MediaMetadataCompat metadata) {
                LogHelper.d(TAG, "onMetadataChanged");
                LogHelper.d(TAG, "MediaControllerCompat.Callback onMetadataChanged metadata=" + metadata + " <<<---------");
            }
        };

        // Callback for Media Browser Connection
        mConnectionCallback = new MediaBrowserCompat.ConnectionCallback() {
            @Override
            public void onConnected() {
                LogHelper.d(TAG, "MediaBrowserCompat.ConnectionCallback onConnected <<<---------");

                try {
                    connectToSession(getMediaBrowser().getSessionToken());
                } catch (RemoteException e) {
                    LogHelper.e(TAG, e, "could not connect media controller");
                }

                //mMediaId = mRadioMediaBrowser.getRoot();
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
                getMediaBrowser().unsubscribe(mMediaId);

                getMediaBrowser().subscribe(mMediaId, mSubscriptionCallback);

                // Add MediaController callback so we can redraw the list when metadata changes:
                if (getRadioMediaController() != null) {
                    LogHelper.v(TAG, "getRadioMediaController().registerCallback(mMediaControllerCallback);");
                    getRadioMediaController().registerCallback(mMediaControllerCallback);
                } else {
                    LogHelper.w(TAG, "UNABLE TO ISSUE: mRadioMediaController.registerCallback(mMediaControllerCallback);");
                }
            }
        };

        //--------------------------------------------------------------------------------
        // INITIALIZE THE MEDIA INTERFACES
        //--------------------------------------------------------------------------------
        // Connect a media browser just to get the media session token. There are other ways
        // this can be done, for example by sharing the session token directly.
        mRadioMediaBrowser = new MediaBrowserCompat(this, new ComponentName(this, RadioTheaterService.class), mConnectionCallback, null);
        LogHelper.d(TAG, "---> mRadioMediaBrowser.connect(); <---");
        mRadioMediaBrowser.connect();
        mRadioAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        // request audio focus from the system
        mAudioFocusRequstResult = getAudioManager().requestAudioFocus(mAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    }

    public void onHandleIntent(Intent intent) {
        LogHelper.v(TAG, "onHandleIntent");
        if (intent != null) {
            final String action = intent.getAction();
            LogHelper.v(TAG, "onHandleIntent: action="+action);
            if (ACTION_START.equals(action)) {
                final String episode = intent.getStringExtra(EXTRA_EPISODE);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionStart(episode, param2);
            } else if (ACTION_PLAY.equals(action)) {
                final String episode = intent.getStringExtra(EXTRA_EPISODE);
                final String downloadUrl = intent.getStringExtra(EXTRA_PARAM2);
                final String title = intent.getStringExtra(EXTRA_TITLE);
                handleActionPlay(episode, downloadUrl, title);
            } else if (ACTION_PAUSE.equals(action)) {
                final String episode = intent.getStringExtra(EXTRA_EPISODE);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionPause(episode, param2);
            } else if (ACTION_SEEK.equals(action)) {
                final String episode = intent.getStringExtra(EXTRA_EPISODE);
                final String position = intent.getStringExtra(EXTRA_PARAM2);
                handleActionSeek(episode, position);
            } else if (ACTION_GOBACK.equals(action)) {
                final String episode = intent.getStringExtra(EXTRA_EPISODE);
                final String amount = intent.getStringExtra(EXTRA_PARAM2);
                handleActionGoback(episode, amount);
            } else if (ACTION_STOP.equals(action)) {
                final String episode = intent.getStringExtra(EXTRA_EPISODE);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionStop(episode, param2);
            } else if (ACTION_NEXT.equals(action)) {
                final String episode = intent.getStringExtra(EXTRA_EPISODE);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionNext(episode, param2);
            } else if (ACTION_PREV.equals(action)) {
                final String episode = intent.getStringExtra(EXTRA_EPISODE);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionPrev(episode, param2);
            }
        }
    }

    private void connectToSession(MediaSessionCompat.Token token)
            throws RemoteException
    {
        LogHelper.v(TAG, "connectToSession");
        mRadioMediaController = new MediaControllerCompat(this, token);
        if (getRadioMediaController() != null) {
            getRadioMediaController().registerCallback(mMediaControllerCallback);
        }
    }

    public MediaControllerCompat getRadioMediaController() {
        return mRadioMediaController;
    }

    public AudioManager getAudioManager() {
        return mRadioAudioManager;
    }

    public MediaBrowserCompat getMediaBrowser() {
        return mRadioMediaBrowser;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        LogHelper.v(TAG, "onStart: intent="+intent.toString());
        super.onStart(intent, startId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogHelper.v(TAG, "onStartCommand: intent="+intent.toString());
        super.onStartCommand(intent, flags, startId);
        return START_STICKY; // sticky hint for service to run until explicitly stopped
    }

    @Override
    public void onDestroy() {
        LogHelper.v(TAG, "onDestroy");
        super.onDestroy();
        if (getAudioManager() != null && mAudioFocusRequstResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            getAudioManager().abandonAudioFocus(mAudioFocusChangeListener);
            mAudioFocusRequstResult = 0;
        }
        if (getRadioMediaController() != null) {
            getRadioMediaController().unregisterCallback(mMediaControllerCallback);
        }
        LogHelper.d(TAG, "---> mRadioMediaBrowser.disconnect();");
        getMediaBrowser().disconnect();
        mRadioAudioManager = null;
        mRadioMediaBrowser = null;
        mRadioMediaController = null;
    }

    //================================================================================
    // PUBLIC API
    //================================================================================

    /**
     * Starts this service to perform action START with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionStart(Context context, String from, String episode, String param2) {
        if (! sAlreadyStarted) {
            sAlreadyStarted = true;
            LogHelper.v(TAG, "startActionStart: from=" + from + ", episode=" + episode);
            Intent intent = new Intent(context, RadioControlIntentService.class);
            intent.setAction(ACTION_START);
            intent.putExtra(EXTRA_EPISODE, episode);
            intent.putExtra(EXTRA_PARAM2, "hello, world!");
            context.startService(intent);
        }
        else {
            LogHelper.v(TAG, "*** startActionStart *** - ALREADY!");
        }
    }

    /**
     * Starts this service to perform action PLAY with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionPlay(Context context, String from, String episode, String downloadUrl, String title) {
        LogHelper.v(TAG, "startActionPlay: from="+from+", episode="+episode);
        LocalPlayback.setPlaybackEnabled(true);
        Intent intent = new Intent(context, RadioControlIntentService.class);
        intent.setAction(ACTION_PLAY);
        intent.putExtra(EXTRA_EPISODE, episode);
        intent.putExtra(EXTRA_PARAM2, downloadUrl);
        intent.putExtra(EXTRA_TITLE, title);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action PAUSE with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionPause(Context context, String from, String episode, String param2) {
        LocalPlayback.setPlaybackEnabled(false);
        if (LocalPlayback.getCurrentState() == PlaybackStateCompat.STATE_PLAYING) {
            LogHelper.v(TAG, "startActionPause: from=" + from + ", episode=" + episode);
            Intent intent = new Intent(context, RadioControlIntentService.class);
            intent.setAction(ACTION_PAUSE);
            intent.putExtra(EXTRA_EPISODE, episode);
            intent.putExtra(EXTRA_PARAM2, param2);
            context.startService(intent);
        }
        else {
            LogHelper.v(TAG, "*** ignored startActionPause (not playing) ***");
        }
    }

    /**
     * Starts this service to perform action SEEK with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionSeek(Context context, String from, String episode, String position) {
        LogHelper.v(TAG, "startActionSeek: from="+from+", episode="+episode+", position="+position);
        Intent intent = new Intent(context, RadioControlIntentService.class);
        intent.setAction(ACTION_SEEK);
        intent.putExtra(EXTRA_EPISODE, episode);
        intent.putExtra(EXTRA_PARAM2, position);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action GOBACK with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionGoback(Context context, String from, String episode, String amount) {
        LogHelper.v(TAG, "startActionGoback: from="+from+", episode="+episode+", amount="+amount);
        Intent intent = new Intent(context, RadioControlIntentService.class);
        intent.setAction(ACTION_GOBACK);
        intent.putExtra(EXTRA_EPISODE, episode);
        intent.putExtra(EXTRA_PARAM2, amount);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action STOP with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionStop(Context context, String from, String episode, String param2) {
        LocalPlayback.setPlaybackEnabled(false);
        if (LocalPlayback.getCurrentState() == PlaybackStateCompat.STATE_PLAYING) {
            LogHelper.v(TAG, "startActionStop: from=" + from + ", episode=" + episode);
            Intent intent = new Intent(context, RadioControlIntentService.class);
            intent.setAction(ACTION_STOP);
            intent.putExtra(EXTRA_EPISODE, episode);
            intent.putExtra(EXTRA_PARAM2, param2);
            context.startService(intent);
        }
        else {
            LogHelper.v(TAG, "*** ignored startActionStop (not playing) ***");
        }
    }

    /**
     * Starts this service to perform action NEXT with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionNext(Context context, String from, String episode, String param2) {
        LocalPlayback.setPlaybackEnabled(true);
        LogHelper.v(TAG, "startActionNext: from="+from+", episode="+episode);
        Intent intent = new Intent(context, RadioControlIntentService.class);
        intent.setAction(ACTION_NEXT);
        intent.putExtra(EXTRA_EPISODE, episode);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action PREV with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionPrev(Context context, String from, String episode, String param2) {
        LocalPlayback.setPlaybackEnabled(true);
        LogHelper.v(TAG, "startActionPrev: from="+from+", episode="+episode);
        Intent intent = new Intent(context, RadioControlIntentService.class);
        intent.setAction(ACTION_PREV);
        intent.putExtra(EXTRA_EPISODE, episode);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    //================================================================================

    /**
     * Handle action START in the provided background thread with the provided
     * parameters.
     */
    private void handleActionStart(String episode, String param2) {
        LogHelper.v(TAG, "---> handleActionStart: episode="+episode+", param2="+param2);
    }

    /**
     * Handle action PLAY in the provided background thread with the provided
     * parameters.
     */
    private void handleActionPlay(String episode, String episodeDownloadUrl, String title) {
        LogHelper.v(TAG, "---> handleActionPlay: episode="+episode+", episodeDownloadUrl="+episodeDownloadUrl);
        if (episode == null) {
            LogHelper.e(TAG, "handleActionPlay: null episode");
            return;
        }
        if (episodeDownloadUrl == null) {
            LogHelper.e(TAG, "handleActionPlay: null episodeDownloadUrl");
            return;
        }
        if (title == null) {
            LogHelper.e(TAG, "handleActionPlay: null title");
            return;
        }
        mMediaId = RadioTheaterApplication.getRadioTheaterApplicationContext().getResources().getString(R.string.genre);
        LogHelper.d(TAG, "*** INITIALIZE PLAYBACK ***  mMediaId=" + mMediaId + ", episodeDownloadUrl=" + episodeDownloadUrl + ", title=" + title);

        Context context = this.getBaseContext();
        Intent radioServiceCommandIntent = new Intent(context, RadioTheaterService.class);
        radioServiceCommandIntent.setAction(RadioTheaterService.ACTION_CMD);
        radioServiceCommandIntent.putExtra(RadioTheaterService.CMD_NAME, RadioTheaterService.CMD_PLAY);
        radioServiceCommandIntent.putExtra(RadioTheaterService.CMD_PARAM_EPISODE, episode);
        radioServiceCommandIntent.putExtra(RadioTheaterService.CMD_PARAM_DOWNLOAD_URL, episodeDownloadUrl);
        radioServiceCommandIntent.putExtra(RadioTheaterService.CMD_PARAM_TITLE, title);
        context.startService(radioServiceCommandIntent);
    }

    /**
     * Handle action PAUSE in the provided background thread with the provided
     * parameters.
     */
    private void handleActionPause(String episode, String param2) {
        Context context = this.getBaseContext();
        LogHelper.v(TAG, "---> handleActionPause: episode="+episode+", param2="+param2);
        Intent radioServiceCommandIntent = new Intent(context, RadioTheaterService.class);
        radioServiceCommandIntent.setAction(RadioTheaterService.ACTION_CMD);
        radioServiceCommandIntent.putExtra(RadioTheaterService.CMD_NAME, RadioTheaterService.CMD_PAUSE);
        radioServiceCommandIntent.putExtra(RadioTheaterService.CMD_PARAM_EPISODE, episode);
        context.startService(radioServiceCommandIntent);
    }

    /**
     * Handle action SEEK in the provided background thread with the provided
     * parameters. Seek playback to the specified position.
     */
    private void handleActionSeek(String episode, String position) {
        Context context = this.getBaseContext();
        LogHelper.v(TAG, "---> handleActionSeek: episode="+episode+", position="+position);
        Intent radioServiceCommandIntent = new Intent(context, RadioTheaterService.class);
        radioServiceCommandIntent.setAction(RadioTheaterService.ACTION_CMD);
        radioServiceCommandIntent.putExtra(RadioTheaterService.CMD_NAME, RadioTheaterService.CMD_SEEK);
        radioServiceCommandIntent.putExtra(RadioTheaterService.CMD_PARAM_EPISODE, episode);
        radioServiceCommandIntent.putExtra(RadioTheaterService.CMD_PARAM_SEEK_POSITION, Integer.valueOf(position));
        context.startService(radioServiceCommandIntent);
    }

    /**
     * Handle action GOBACK in the provided background thread with the provided
     * parameters. Backup playback by the amount of seconds.
     */
    private void handleActionGoback(String episode, String amount) {
        Context context = this.getBaseContext();
        LogHelper.v(TAG, "---> handleActionGoback: episode="+episode+", amount="+amount);
        Intent radioServiceCommandIntent = new Intent(context, RadioTheaterService.class);
        radioServiceCommandIntent.setAction(RadioTheaterService.ACTION_CMD);
        radioServiceCommandIntent.putExtra(RadioTheaterService.CMD_NAME, RadioTheaterService.CMD_GOBACK);
        radioServiceCommandIntent.putExtra(RadioTheaterService.CMD_PARAM_EPISODE, episode);
        radioServiceCommandIntent.putExtra(RadioTheaterService.CMD_PARAM_GOBACK_AMOUNT, Integer.valueOf(amount));
        context.startService(radioServiceCommandIntent);
    }

    /**
     * Handle action STOP in the provided background thread with the provided
     * parameters.
     */
    private void handleActionStop(String episode, String param2) {
        LogHelper.v(TAG, "---> handleActionStop: episode="+episode+", param2="+param2);
        Context context = this.getBaseContext();
        Intent radioServiceCommandIntent = new Intent(context, RadioTheaterService.class);
        radioServiceCommandIntent.setAction(RadioTheaterService.ACTION_CMD);
        radioServiceCommandIntent.putExtra(RadioTheaterService.CMD_NAME, RadioTheaterService.CMD_STOP);
        radioServiceCommandIntent.putExtra(RadioTheaterService.CMD_PARAM_EPISODE, episode);
        context.startService(radioServiceCommandIntent);
    }

    /**
     * Handle action NEXT in the provided background thread with the provided
     * parameters.
     */
    private void handleActionNext(String episode, String param2) {
        LogHelper.v(TAG, "---> handleActionNext: episode="+episode+", param2="+param2);
        Context context = this.getBaseContext();
        Intent radioServiceCommandIntent = new Intent(context, RadioTheaterService.class);
        radioServiceCommandIntent.setAction(RadioTheaterService.ACTION_CMD);
        radioServiceCommandIntent.putExtra(RadioTheaterService.CMD_NAME, RadioTheaterService.CMD_NEXT);
        radioServiceCommandIntent.putExtra(RadioTheaterService.CMD_PARAM_EPISODE, episode);
        context.startService(radioServiceCommandIntent);
    }

    /**
     * Handle action PREV in the provided background thread with the provided
     * parameters.
     */
    private void handleActionPrev(String episode, String param2) {
        LogHelper.v(TAG, "---> handleActionPrev: episode="+episode+", param2="+param2);
        Context context = this.getBaseContext();
        Intent radioServiceCommandIntent = new Intent(context, RadioTheaterService.class);
        radioServiceCommandIntent.setAction(RadioTheaterService.ACTION_CMD);
        radioServiceCommandIntent.putExtra(RadioTheaterService.CMD_NAME, RadioTheaterService.CMD_PREV);
        radioServiceCommandIntent.putExtra(RadioTheaterService.CMD_PARAM_EPISODE, episode);
        context.startService(radioServiceCommandIntent);
    }

}
