//
// THIS CODE IS REPURPOSED FROM THE GOOGLE UNIVERSAL-MEDIA-PLAYER SAMPLE
//

package com.harlie.radiotheater.radiomysterytheater;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat.MediaItem;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.media.MediaRouter;

import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;
import com.google.android.libraries.cast.companionlibrary.cast.callbacks.VideoCastConsumerImpl;
import com.harlie.radiotheater.radiomysterytheater.data.episodes.EpisodesCursor;
import com.harlie.radiotheater.radiomysterytheater.data_helper.DataHelper;
import com.harlie.radiotheater.radiomysterytheater.model.MusicProvider;
import com.harlie.radiotheater.radiomysterytheater.playback.CastPlayback;
import com.harlie.radiotheater.radiomysterytheater.playback.LocalPlayback;
import com.harlie.radiotheater.radiomysterytheater.playback.Playback;
import com.harlie.radiotheater.radiomysterytheater.playback.PlaybackManager;
import com.harlie.radiotheater.radiomysterytheater.playback.QueueManager;
import com.harlie.radiotheater.radiomysterytheater.utils.CarHelper;
import com.harlie.radiotheater.radiomysterytheater.utils.LogHelper;
import com.harlie.radiotheater.radiomysterytheater.utils.PackageValidator;
import com.harlie.radiotheater.radiomysterytheater.utils.WearHelper;

import java.util.List;

import static com.harlie.radiotheater.radiomysterytheater.utils.MediaIDHelper.MEDIA_ID_ROOT;

/**
 * This class provides a MediaBrowser through a service. It exposes the media library to a browsing
 * client, through the onGetRoot and onLoadChildren methods. It also creates a MediaSession and
 * exposes it through its MediaSession.Token, which allows the client to create a MediaController
 * that connects to and send control commands to the MediaSession remotely. This is useful for
 * user interfaces that need to interact with your media session, like Android Auto. You can
 * (should) also use the same service from your app's UI, which gives a seamless playback
 * experience to the user.
 *
 * To implement a MediaBrowserService, you need to:
 *
 * <ul>
 *
 * <li> Extend {@link android.service.media.MediaBrowserService}, implementing the media browsing
 *      related methods {@link android.service.media.MediaBrowserService#onGetRoot} and
 *      {@link android.service.media.MediaBrowserService#onLoadChildren};
 * <li> In onCreate, start a new {@link android.media.session.MediaSession} and notify its parent
 *      with the session's token {@link android.service.media.MediaBrowserService#setSessionToken};
 *
 * <li> Set a callback on the
 *      {@link android.media.session.MediaSession#setCallback(android.media.session.MediaSession.Callback)}.
 *      The callback will receive all the user's actions, like play, pause, etc;
 *
 * <li> Handle all the actual music playing using any method your app prefers (for example,
 *      {@link android.media.MediaPlayer})
 *
 * <li> Update playbackState, "now playing" metadata and queue, using MediaSession proper methods
 *      {@link android.media.session.MediaSession#setPlaybackState(android.media.session.PlaybackState)}
 *      {@link android.media.session.MediaSession#setMetadata(android.media.MediaMetadata)} and
 *      {@link android.media.session.MediaSession#setQueue(java.util.List)})
 *
 * <li> Declare and export the service in AndroidManifest with an intent receiver for the action
 *      android.media.browse.MediaBrowserService
 *
 * </ul>
 *
 * To make your app compatible with Android Auto, you also need to:
 *
 * <ul>
 *
 * <li> Declare a meta-data tag in AndroidManifest.xml linking to a xml resource
 *      with a &lt;automotiveApp&gt; root element. For a media app, this must include
 *      an &lt;uses name="media"/&gt; element as a child.
 *      For example, in AndroidManifest.xml:
 *          &lt;meta-data android:name="com.google.android.gms.car.application"
 *              android:resource="@xml/automotive_app_desc"/&gt;
 *      And in res/values/automotive_app_desc.xml:
 *          &lt;automotiveApp&gt;
 *              &lt;uses name="media"/&gt;
 *          &lt;/automotiveApp&gt;
 *
 * </ul>

 * @see <a href="README.md">README.md</a> for more details.
 *
 */
public class RadioTheaterService
        extends MediaBrowserServiceCompat
        implements PlaybackManager.PlaybackServiceCallback
{
    private final static String TAG = "LEE: <" + RadioTheaterService.class.getSimpleName() + ">";

    private DataHelper mDataHelper;

    // Extra on MediaSession that contains the Cast device name currently connected to
    public static final String EXTRA_CONNECTED_CAST = "com.harlie.radiotheater.radiomysterytheater.CAST_NAME";
    // The action of the incoming Intent indicating that it contains a command
    // to be executed (see {@link #onStartCommand})
    public static final String ACTION_CMD = "com.harlie.radiotheater.radiomysterytheater.ACTION_CMD";

    // The key in the extras of the incoming Intent indicating the command that
    // should be executed (see {@link #onStartCommand})
    public static final String CMD_NAME = "CMD_NAME";

    // A value of a CMD_NAME key in the extras of the incoming Intent that
    // indicates that the music playback should be started (see {@link #onStartCommand})
    public static final String CMD_PLAY = "CMD_PLAY";
    public static final String CMD_PARAM_EPISODE = "CMD_PARAM_EPISODE";
    public static final String CMD_PARAM_DOWNLOAD_URL = "CMD_PARAM_DOWNLOAD_URL";
    public static final String CMD_PARAM_TITLE = "CMD_PARAM_TITLE";

    // A value of a CMD_NAME key in the extras of the incoming Intent that
    // indicates that the music playback should be paused (see {@link #onStartCommand})
    public static final String CMD_PAUSE = "CMD_PAUSE";

    // A value of a CMD_NAME key in the extras of the incoming Intent that
    // indicates that the music playback should be paused (see {@link #onStartCommand})
    public static final String CMD_SEEK = "CMD_SEEK";
    public static final String CMD_PARAM_SEEK_POSITION = "CMD_PARAM_SEEK_POSITION";

    // A value of a CMD_NAME key in the extras of the incoming Intent that
    // indicates that the music playback should backup by specified amount (see {@link #onStartCommand})
    public static final String CMD_GOBACK = "CMD_GOBACK";
    public static final String CMD_PARAM_GOBACK_AMOUNT = "CMD_PARAM_GOBACK_AMOUNT";

    // A value of a CMD_NAME key in the extras of the incoming Intent that
    // indicates that the music playback should be stopped (see {@link #onStartCommand})
    public static final String CMD_STOP = "CMD_STOP";

    // A value of a CMD_NAME key that indicates that the music playback should skip
    // to the next item in the playback list (see {@link #onStartCommand})
    public static final String CMD_NEXT = "CMD_NEXT";

    // A value of a CMD_NAME key that indicates that the music playback should skip
    // to the previous item in the playback list (see {@link #onStartCommand})
    public static final String CMD_PREV = "CMD_PREV";

    // A value of a CMD_NAME key that indicates that the music playback has completed
    // and should continue to the next item in the playback list (see {@link #onStartCommand})
    public static final String CMD_COMPLETE = "CMD_COMPLETE";

    // A value of a CMD_NAME key that indicates that the music playback should switch
    // to local playback from cast playback.
    public static final String CMD_STOP_CASTING = "CMD_STOP_CASTING";

    // Delay stopSelf by using a handler.
    private static final int STOP_DELAY = 30000;

    private MusicProvider mMusicProvider;
    private PlaybackManager mPlaybackManager;

    private MediaSessionCompat mSession;
    private MediaNotificationManager mMediaNotificationManager;
    private Bundle mSessionExtras;
    private MediaRouter mMediaRouter;
    private PackageValidator mPackageValidator;
    private QueueManager queueManager;

    private boolean mIsConnectedToCar;
    private BroadcastReceiver mCarConnectionReceiver;


    public RadioTheaterService() {
        super();
        mDataHelper = DataHelper.getInstance();
        mDataHelper.dummyWork();
    }

    /**
     * Consumer responsible for switching the Playback instances depending on whether
     * it is connected to a remote player.
     */
    private final VideoCastConsumerImpl mCastConsumer = new VideoCastConsumerImpl() {

        @Override
        public void onApplicationConnected(ApplicationMetadata appMetadata, String sessionId, boolean wasLaunched) {
            // In case we are casting, send the device name as an extra on MediaSession metadata.
            mSessionExtras.putString(EXTRA_CONNECTED_CAST, VideoCastManager.getInstance().getDeviceName());
            mSession.setExtras(mSessionExtras);
            // Now we can switch to CastPlayback
            Playback playback = new CastPlayback(mMusicProvider);
            mMediaRouter.setMediaSessionCompat(mSession);
            mPlaybackManager.switchToPlayback(playback, true);
        }

        @Override
        public void onDisconnectionReason(int reason) {
            // This is our final chance to update the underlying stream position
            // In onDisconnected(), the underlying CastPlayback#mVideoCastConsumer
            // is disconnected and hence we update our local value of stream position
            // to the latest position.
            mPlaybackManager.getPlayback().updateLastKnownStreamPosition();
        }

        @Override
        public void onDisconnected() {
            LogHelper.v(TAG, "onDisconnected");
            mSessionExtras.remove(EXTRA_CONNECTED_CAST);
            mSession.setExtras(mSessionExtras);
            Playback playback = new LocalPlayback(RadioTheaterService.this, mMusicProvider);
            mMediaRouter.setMediaSessionCompat(null);
            mPlaybackManager.switchToPlayback(playback, false);
        }
    };

    /*
     * FIXME: THIS METHOD IS PERFORMANCE-HEAVY ON THE MAIN THREAD!
     * (non-Javadoc)
     * @see android.app.Service#onCreate()
     */
    @Override
    public void onCreate() {
        super.onCreate();
        LogHelper.v(TAG, "onCreate");
        final RadioTheaterService radioTheaterService = this;

        LogHelper.v(TAG, "*** CREATE THE MUSIC PROVIDER ***");
        mMusicProvider = new MusicProvider();

        LogHelper.v(TAG, "*** CREATE THE PACKAGE VALIDATOR ***");
        mPackageValidator = new PackageValidator(radioTheaterService);

        LogHelper.v(TAG, "*** CREATE THE QUEUE MANAGER ***");
        queueManager = new QueueManager(mMusicProvider, getResources(),
                new QueueManager.MetadataUpdateListener() {
                    @Override
                    public void onMetadataChanged(MediaMetadataCompat metadata) {
                        LogHelper.v(TAG, "===> onMetadataChanged");
                        mSession.setMetadata(metadata);
                    }

                    @Override
                    public void onMetadataRetrieveError() {
                        LogHelper.v(TAG, "===> onMetadataRetrieveError");
                        mPlaybackManager.updatePlaybackState(getString(R.string.error_no_metadata));
                    }

                    @Override
                    public void onCurrentQueueIndexUpdated(int queueIndex) {
                        LogHelper.v(TAG, "===> onCurrentQueueIndexUpdated <===");
                        mPlaybackManager.handlePlayRequest();
                    }

                    @Override
                    public void onQueueUpdated(String title, List<MediaSessionCompat.QueueItem> newQueue) {
                        LogHelper.v(TAG, "===> onQueueUpdated <=== - title='"+title+"'");
                        mSession.setQueue(newQueue);
                        mSession.setQueueTitle(title);
                    }
                });

        LogHelper.v(TAG, "*** CREATE THE LOCAL PLAYBACK ***");
        LocalPlayback playback = new LocalPlayback(radioTheaterService, mMusicProvider);
        mPlaybackManager = new PlaybackManager(radioTheaterService, getResources(), mMusicProvider, queueManager, playback);

        LogHelper.v(TAG, "*** START A MEDIA SESSION ***");
        mSession = new MediaSessionCompat(radioTheaterService, "RadioTheaterService");
        setSessionToken(mSession.getSessionToken());

        mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        // To make the app more responsive, fetch and cache catalog information now.
        // This can help improve the response time in the method
        // {@link #onLoadChildren(String, Result<List<MediaItem>>) onLoadChildren()}.
        LogHelper.v(TAG, "*** RETRIEVE MEDIA ASYNC ***");
        mMusicProvider.retrieveMediaAsync(new MusicProvider.Callback() {
            @Override
            public void onMusicCatalogReady(boolean success) {
                LogHelper.v(TAG, "*** CALLBACK *** - onMusicCatalogReady");
            }
        });

        LogHelper.v(TAG, "*** AUTOPLAY INTENT FLAG_UPDATE_CURRENT ***");
        Context context = getApplicationContext();
        Intent intent = new Intent(context, AutoplayActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, 99 /*request code*/,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mSession.setSessionActivity(pi);

        LogHelper.v(TAG, "*** ANDROID WEAR SETUP ***");
        mSessionExtras = new Bundle();
        CarHelper.setSlotReservationFlags(mSessionExtras, true, true, true);
        WearHelper.setSlotReservationFlags(mSessionExtras, true, true);
        WearHelper.setUseBackgroundFromTheme(mSessionExtras, true);
        mSession.setExtras(mSessionExtras);

        LogHelper.v(TAG, "*** ADD VIDEO CAST CONSUMER ***");
        try {
            mMediaNotificationManager = new MediaNotificationManager(radioTheaterService);
        } catch (RemoteException e) {
            throw new IllegalStateException("Could not create a MediaNotificationManager", e);
        }
        VideoCastManager.getInstance().addVideoCastConsumer(mCastConsumer);

        LogHelper.v(TAG, "*** REGISTER CAR RECEIVER ***");
        registerCarConnectionReceiver();

        LogHelper.v(TAG, "*** set the session callback ***");
        mSession.setCallback(mPlaybackManager.getMediaSessionCallback());

        LogHelper.v(TAG, "*** get the media router ***");
        mMediaRouter = MediaRouter.getInstance(getApplicationContext());

        LogHelper.v(TAG, "*** UPDATE PLAYBACK STATE ***");
        mPlaybackManager.updatePlaybackState(null);

        LogHelper.v(TAG, "*** RadioTheaterService is initialized ***");
    }

    /**
     * (non-Javadoc)
     * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
     */
    @Override
    public int onStartCommand(Intent startIntent, int flags, int startId) {
        if (startIntent == null) {
            LogHelper.e(TAG, "=========>>> onStartCommand: startIntent is null! - flags="+flags+", startId="+startId);
            return START_REDELIVER_INTENT;
        }
        LogHelper.v(TAG, "=========>>> onStartCommand: startIntent="+startIntent.getAction()+", flags="+flags+", startId="+startId);
        String action = startIntent.getAction();
        String command = startIntent.getStringExtra(CMD_NAME);
        String episode = startIntent.getStringExtra(CMD_PARAM_EPISODE);
        long episodeId = (episode != null && !episode.equals("null")) ? Long.valueOf(episode) : 0L;

        if (ACTION_CMD.equals(action)) {
            LogHelper.v(TAG, "=========>>> ACTION: "+action+", COMMAND="+command+", EPISODE="+episode);
            mDataHelper.showCurrentInfo();

            if (CMD_PLAY.equals(command)) {
                String title = startIntent.getStringExtra(CMD_PARAM_TITLE);
                String downloadUrl = startIntent.getStringExtra(CMD_PARAM_DOWNLOAD_URL);
                LogHelper.v(TAG, "---> EXECUTE CMD_PLAY: "+title);
                startPlaying(episodeId, title, downloadUrl);
            }

            else if (CMD_PAUSE.equals(command)) {
                LogHelper.v(TAG, "---> EXECUTE CMD_PAUSE");
                mPlaybackManager.handlePauseRequest();
            }

            else if (CMD_SEEK.equals(command)) {
                Integer position = startIntent.getIntExtra(CMD_PARAM_SEEK_POSITION, 0);
                LogHelper.v(TAG, "---> EXECUTE CMD_SEEK: "+position);
                mPlaybackManager.handleSeekRequest(position);
            }

            else if (CMD_GOBACK.equals(command)) {
                Integer amount = startIntent.getIntExtra(CMD_PARAM_GOBACK_AMOUNT, 0);
                LogHelper.v(TAG, "---> EXECUTE CMD_GOBACK: "+amount);
                int position = LocalPlayback.getCurrentPosition();
                position -= amount;
                if (position < 0) {
                    position = 0;
                }
                mPlaybackManager.handleSeekRequest(position);
            }

            else if (CMD_STOP.equals(command)) {
                LogHelper.v(TAG, "---> EXECUTE CMD_STOP");
                mPlaybackManager.handleStopRequest(null);
            }

            else if (CMD_NEXT.equals(command)) {
                long max_episodes = Long.valueOf(getResources().getString(R.string.episodes_count)); // 1399
                episodeId += 1;
                if (episodeId > max_episodes) {
                    episodeId = max_episodes;
                }
                EpisodesCursor episodesCursor = DataHelper.getEpisodesCursor(episodeId);
                if (episodesCursor != null && episodesCursor.moveToNext()) {
                    String title = episodesCursor.getFieldEpisodeTitle();
                    String downloadUrl = Uri.parse("http://" + episodesCursor.getFieldDownloadUrl()).toString();
                    episodesCursor.close();
                    LogHelper.v(TAG, "---> EXECUTE CMD_NEXT: "+episodeId+" - "+title);
                    startPlaying(episodeId, title, downloadUrl);
                }
            }

            else if (CMD_PREV.equals(command)) {
                episodeId -= 1;
                if (episodeId <= 0) {
                    episodeId = 1;
                }
                EpisodesCursor episodesCursor = DataHelper.getEpisodesCursor(episodeId);
                if (episodesCursor != null && episodesCursor.moveToNext()) {
                    String title = episodesCursor.getFieldEpisodeTitle();
                    String downloadUrl = Uri.parse("http://" + episodesCursor.getFieldDownloadUrl()).toString();
                    episodesCursor.close();
                    LogHelper.v(TAG, "---> EXECUTE CMD_PREV: "+episodeId+" - "+title);
                    startPlaying(episodeId, title, downloadUrl);
                }
            }

            else if (CMD_STOP_CASTING.equals(command)) {
                LogHelper.v(TAG, "---> EXECUTE CMD_STOP_CASTING");
                VideoCastManager.getInstance().disconnect();
            }

            else if (CMD_COMPLETE.equals(command)) {
                LogHelper.v(TAG, "---> EXECUTE CMD_COMPLETE: episode="+episode);
                Context context = RadioTheaterApplication.getRadioTheaterApplicationContext();
                Intent intent = new Intent(context, RadioControlIntentService.class);
                intent.setAction(ACTION_CMD);
                intent.putExtra(RadioTheaterService.CMD_NAME, RadioTheaterService.CMD_COMPLETE);
                intent.putExtra(RadioTheaterService.CMD_PARAM_EPISODE, episode);
                context.startService(intent);
            }

            else {
                LogHelper.w(TAG, "*** UNKNOWN ACTION="+action+" for COMMAND="+command);
            }
        } else {
            // Try to handle the intent as a media button event wrapped by MediaButtonReceiver
            MediaButtonReceiver.handleIntent(mSession, startIntent);
        }

        return START_STICKY;
    }

    protected void startPlaying(long episode, String title, String episodeDownloadUrl) {
        LocalPlayback.setCurrentEpisode((int) episode);
        String mediaId = queueManager.setCurrentIndexFromEpisodeId((int) episode, title, episodeDownloadUrl);
        mPlaybackManager.setCurrentMediaId(mediaId);
        mPlaybackManager.handlePlayRequest();
    }

    /**
     * (non-Javadoc)
     * @see android.app.Service#onDestroy()
     */
    @Override
    public void onDestroy() {
        LogHelper.v(TAG, "onDestroy");
        unregisterCarConnectionReceiver();
        // Service is being killed, so make sure we release our resources
        mPlaybackManager.handleStopRequest(null);
        mMediaNotificationManager.stopNotification();
        VideoCastManager.getInstance().removeVideoCastConsumer(mCastConsumer);
        mSession.release();
    }

    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, Bundle rootHints) {
        LogHelper.v(TAG, "onGetRoot: clientPackageName=" + clientPackageName, "; clientUid=" + clientUid + " ; rootHints=", rootHints);
        // To ensure you are not allowing any arbitrary app to browse your app's contents, you
        // need to check the origin:
        if (!mPackageValidator.isCallerAllowed(this, clientPackageName, clientUid)) {
            // If the request comes from an untrusted package, return null. No further calls will
            // be made to other media browsing methods.
            LogHelper.w(TAG, "OnGetRoot: IGNORING request from untrusted package " + clientPackageName);
            return null;
        }
        //noinspection StatementWithEmptyBody
        if (CarHelper.isValidCarPackage(clientPackageName)) {
            // Optional: if your app needs to adapt the music library to show a different subset
            // when connected to the car, this is where you should handle it.
            // If you want to adapt other runtime behaviors, like tweak ads or change some behavior
            // that should be different on cars, you should instead use the boolean flag
            // set by the BroadcastReceiver mCarConnectionReceiver (mIsConnectedToCar).
        }
        //noinspection StatementWithEmptyBody
        if (WearHelper.isValidWearCompanionPackage(clientPackageName)) {
            // Optional: if your app needs to adapt the music library for when browsing from a
            // Wear device, you should return a different MEDIA ROOT here, and then,
            // on onLoadChildren, handle it accordingly.
        }
        return new BrowserRoot(MEDIA_ID_ROOT, null);
    }

    @Override
    public void onLoadChildren(@NonNull final String parentMediaId,
                               @NonNull final Result<List<MediaItem>> result)
    {
        result.sendResult(mMusicProvider.getChildren(parentMediaId, getResources()));
    }

    /**
     * Callback method called from PlaybackManager whenever the music is about to play.
     */
    @Override
    public void onPlaybackStart() {
        if (!mSession.isActive()) {
            LogHelper.v(TAG, "*** WE ARE THE DEFAULT MEDIA RECEIVER NOW ***");
            mSession.setActive(true);
        }

        // The service needs to continue running even after the bound client (usually a
        // MediaController) disconnects, otherwise the music playback will stop.
        // Calling startService(Intent) will keep the service running until it is explicitly killed.
        startService(new Intent(getApplicationContext(), RadioTheaterService.class));
    }

    /**
     * Callback method called from PlaybackManager whenever the music stops playing.
     */
    @Override
    public void onPlaybackStop() {
        stopForeground(true);
    }

    @Override
    public void onNotificationRequired() {
        mMediaNotificationManager.startNotification();
    }

    @Override
    public void onPlaybackStateUpdated(PlaybackStateCompat newState) {
        if (mSession != null) {
            mSession.setPlaybackState(newState);
        }
        else {
            LogHelper.e(TAG, "*** unable to setPlaybackState("+newState+") ***");
        }
    }

    private void registerCarConnectionReceiver() {
        LogHelper.v(TAG, "registerCarConnectionReceiver");
        IntentFilter filter = new IntentFilter(CarHelper.ACTION_MEDIA_STATUS);
        mCarConnectionReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String connectionEvent = intent.getStringExtra(CarHelper.MEDIA_CONNECTION_STATUS);
                mIsConnectedToCar = CarHelper.MEDIA_CONNECTED.equals(connectionEvent);
                LogHelper.i(TAG, "Connection event to Android Auto: ", connectionEvent,
                        " isConnectedToCar=", mIsConnectedToCar);
            }
        };
        registerReceiver(mCarConnectionReceiver, filter);
    }

    private void unregisterCarConnectionReceiver() {
        LogHelper.v(TAG, "unregisterCarConnectionReceiver");
        unregisterReceiver(mCarConnectionReceiver);
    }

}
