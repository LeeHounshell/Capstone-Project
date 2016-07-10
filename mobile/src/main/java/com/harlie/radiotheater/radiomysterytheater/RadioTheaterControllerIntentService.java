package com.harlie.radiotheater.radiomysterytheater;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import com.harlie.radiotheater.radiomysterytheater.utils.LogHelper;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread. This service controls RadioTheater playback.
 */
public class RadioTheaterControllerIntentService extends IntentService {
    private final static String TAG = "LEE: <" + RadioTheaterControllerIntentService.class.getSimpleName() + ">";

    // the controls
    private static final String ACTION_PLAY = "com.harlie.radiotheater.radiomysterytheater.action.PLAY";
    private static final String ACTION_STOP = "com.harlie.radiotheater.radiomysterytheater.action.STOP";
    private static final String ACTION_PAUSE = "com.harlie.radiotheater.radiomysterytheater.action.PAUSE";
    private static final String ACTION_NEXT = "com.harlie.radiotheater.radiomysterytheater.action.NEXT";
    private static final String ACTION_PREV = "com.harlie.radiotheater.radiomysterytheater.action.PREV";

    // control parameters
    private static final String EXTRA_EPISODE = "com.harlie.radiotheater.radiomysterytheater.extra.EPISODE";
    private static final String EXTRA_PARAM2 = "com.harlie.radiotheater.radiomysterytheater.extra.PARAM2";

    public RadioTheaterControllerIntentService() {
        super("RadioTheaterControllerIntentService");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        onHandleIntent(intent);
        // We want this service to continue running until it is explicitly stopped, so return sticky.
        return START_STICKY;
    }

    /**
     * Starts this service to perform action Play with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionPlay(Context context, String episode, String param2) {
        LogHelper.v(TAG, "startActionPlay: episode="+episode);
        Intent intent = new Intent(context, RadioTheaterControllerIntentService.class);
        intent.setAction(ACTION_PLAY);
        intent.putExtra(EXTRA_EPISODE, episode);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Stop with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionStop(Context context, String episode, String param2) {
        LogHelper.v(TAG, "startActionStop: episode="+episode);
        Intent intent = new Intent(context, RadioTheaterControllerIntentService.class);
        intent.setAction(ACTION_STOP);
        intent.putExtra(EXTRA_EPISODE, episode);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Pause with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionPause(Context context, String episode, String param2) {
        LogHelper.v(TAG, "startActionPause: episode="+episode);
        Intent intent = new Intent(context, RadioTheaterControllerIntentService.class);
        intent.setAction(ACTION_PAUSE);
        intent.putExtra(EXTRA_EPISODE, episode);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Next with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionNext(Context context, String episode, String param2) {
        LogHelper.v(TAG, "startActionNext: episode="+episode);
        Intent intent = new Intent(context, RadioTheaterControllerIntentService.class);
        intent.setAction(ACTION_NEXT);
        intent.putExtra(EXTRA_EPISODE, episode);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Prev with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionPrev(Context context, String episode, String param2) {
        LogHelper.v(TAG, "startActionPrev: episode="+episode);
        Intent intent = new Intent(context, RadioTheaterControllerIntentService.class);
        intent.setAction(ACTION_PREV);
        intent.putExtra(EXTRA_EPISODE, episode);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_PLAY.equals(action)) {
                final String episode = intent.getStringExtra(EXTRA_EPISODE);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionPlay(episode, param2);
            } else if (ACTION_STOP.equals(action)) {
                final String episode = intent.getStringExtra(EXTRA_EPISODE);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionStop(episode, param2);
            } else if (ACTION_PAUSE.equals(action)) {
                final String episode = intent.getStringExtra(EXTRA_EPISODE);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionPause(episode, param2);
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

    /**
     * Handle action Play in the provided background thread with the provided
     * parameters.
     */
    private void handleActionPlay(String episode, String param2) {
        LogHelper.v(TAG, "handleActionPlay: episode="+episode+", param2="+param2);
    }

    /**
     * Handle action Stop in the provided background thread with the provided
     * parameters.
     */
    private void handleActionStop(String episode, String param2) {
        LogHelper.v(TAG, "handleActionStop: episode="+episode+", param2="+param2);
    }

    /**
     * Handle action Pause in the provided background thread with the provided
     * parameters.
     */
    private void handleActionPause(String episode, String param2) {
        LogHelper.v(TAG, "handleActionPause: episode="+episode+", param2="+param2);
    }

    /**
     * Handle action Next in the provided background thread with the provided
     * parameters.
     */
    private void handleActionNext(String episode, String param2) {
        LogHelper.v(TAG, "handleActionNext: episode="+episode+", param2="+param2);
    }

    /**
     * Handle action Next in the provided background thread with the provided
     * parameters.
     */
    private void handleActionPrev(String episode, String param2) {
        LogHelper.v(TAG, "handleActionPrev: episode="+episode+", param2="+param2);
    }

}
