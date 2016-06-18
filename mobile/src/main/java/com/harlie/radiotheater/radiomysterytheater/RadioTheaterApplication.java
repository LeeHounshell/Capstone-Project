package com.harlie.radiotheater.radiomysterytheater;

import android.content.Context;
import android.support.multidex.MultiDex;

import com.crashlytics.android.Crashlytics;
import com.google.android.libraries.cast.companionlibrary.cast.CastConfiguration;
import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;
import com.harlie.radiotheater.radiomysterytheater.utils.LogHelper;

import java.util.Locale;

import io.fabric.sdk.android.Fabric;

public class RadioTheaterApplication extends android.support.multidex.MultiDexApplication {
    private final static String TAG = "LEE: <" + RadioTheaterApplication.class.getSimpleName() + ">";

    private static Context applicationContext;
    private static RadioTheaterApplication sInstance;

    @Override
    public void onCreate() {
        LogHelper.v(TAG, "onCreate");
        sInstance = this;
        applicationContext = this.getApplicationContext();
        super.onCreate();

        String applicationId = getString(R.string.app_id);

        // Build a CastConfiguration object and initialize VideoCastManager
        CastConfiguration options = new CastConfiguration.Builder(applicationId)
                .enableAutoReconnect()
                .enableCaptionManagement()
                .enableDebug()
                .enableLockScreen()
                .enableNotification()
                .enableWifiReconnection()
                .setCastControllerImmersive(true)
                .setLaunchOptions(false, Locale.getDefault())
                .setNextPrevVisibilityPolicy(CastConfiguration.NEXT_PREV_VISIBILITY_POLICY_DISABLED)
                .addNotificationAction(CastConfiguration.NOTIFICATION_ACTION_REWIND, false)
                .addNotificationAction(CastConfiguration.NOTIFICATION_ACTION_PLAY_PAUSE, true)
                .addNotificationAction(CastConfiguration.NOTIFICATION_ACTION_DISCONNECT, true)
                .setForwardStep(10)
                .build();
        VideoCastManager.initialize(this, options);

        Fabric.with(this, new Crashlytics());
    }

    public static synchronized RadioTheaterApplication getInstance() {
        LogHelper.v(TAG, "getInstance");
        return RadioTheaterApplication.sInstance;
    }

    public static Context getRadioTheaterApplicationContext() {
        return applicationContext;
    }

    @Override
    public void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    /**
     * Loading queue items. The only reason we are using this instead of using the VideoCastManager
     * directly is to get around an issue on receiver side for HLS + VTT for a queue; this will be
     * addressed soon and the following workaround will be removed.
     * from: https://github.com/googlecast/CastVideos-android/blob/master/src/com/google/sample/cast/refplayer/CastApplication.java#L42
     */
//    public void loadQueue(MediaQueueItem[] items, int startIndex)
//            throws TransientNetworkDisconnectionException, NoConnectionException {
//        final VideoCastManager castManager = VideoCastManager.getInstance();
//        castManager.addVideoCastConsumer(new VideoCastConsumerImpl() {
//            @Override
//            public void onMediaQueueOperationResult(int operationId, int statusCode) {
//                if (operationId == VideoCastManager.QUEUE_OPERATION_LOAD) {
//                    if (statusCode == CastStatusCodes.SUCCESS) {
//                        castManager.setActiveTrackIds(new long[]{});
//                    }
//                    castManager.removeVideoCastConsumer(this);
//                }
//            }
//        });
//        castManager.queueLoad(items, startIndex, MediaStatus.REPEAT_MODE_REPEAT_OFF, null);
//    }

}

