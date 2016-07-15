package com.harlie.radiotheater.radiomysterytheater;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.media.session.PlaybackStateCompat;
import android.widget.RemoteViews;

import com.harlie.radiotheater.radiomysterytheater.data.configepisodes.ConfigEpisodesCursor;
import com.harlie.radiotheater.radiomysterytheater.data_helper.SQLiteHelper;
import com.harlie.radiotheater.radiomysterytheater.playback.LocalPlayback;
import com.harlie.radiotheater.radiomysterytheater.utils.LogHelper;
import com.harlie.radiotheater.radiomysterytheater.utils.RadioTheaterWidgetProvider;

// from: http://www.vogella.com/tutorials/AndroidWidgets/article.html
public class RadioTheaterWidgetService extends Service {
    private final static String TAG = "LEE: <" + RadioTheaterWidgetService.class.getSimpleName() + ">";

    public final static String RADIO_THEATER_WIDGET_CONTROL = "com.harlie.radiotheater.radiomystertheater.WIDGET_BUTTON_PRESS";

    //private MediaPlayer mp;

    private static volatile boolean sPaidVersion;
    private static volatile boolean sGotButtonPress;

    public RadioTheaterWidgetService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleCommand(intent);
        // We want this service to continue running until it is explicitly stopped, so return sticky.
        return START_STICKY;
    }

    private void handleCommand(Intent intent) {
        //mp = MediaPlayer.create(this, R.raw.click);
        //mp.start();

        if (intent.getBooleanExtra("BUTTON_PRESS", false) == false) {
            LogHelper.v(TAG, "handleCommand: IGNORE (visual update only)");
            setGotButtonPress(false);
        }
        else {
            LogHelper.v(TAG, "handleCommand: GOT WIDGET BUTTON PRESS!");
            setGotButtonPress(true);
        }


        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this.getApplicationContext());

        int[] allWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
        if (allWidgetIds == null) {
            LogHelper.w(TAG, "try to fix problem: null EXTRA_APPWIDGET_IDS");
            allWidgetIds = new int[]{R.id.autoplay_widget};
        }

        for (int widgetId : allWidgetIds)
        {
            RemoteViews theWidgetView = new RemoteViews(this.getApplicationContext().getPackageName(), R.layout.radio_theater_widget);

            widgetButtonClick(theWidgetView);

            // Register an onClickListener
            Intent clickIntent = new Intent(this.getApplicationContext(), RadioTheaterWidgetProvider.class);

            clickIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            clickIntent.putExtra(RADIO_THEATER_WIDGET_CONTROL, "true");
            clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    getApplicationContext(), 0, clickIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            theWidgetView.setOnClickPendingIntent(R.id.autoplay_widget, pendingIntent);

            appWidgetManager.updateAppWidget(widgetId, theWidgetView);
        }
        stopSelf();
    }

    private void widgetButtonClick(RemoteViews theWidgetView) {
        int playbackState = LocalPlayback.getCurrentState();
        LogHelper.w(TAG, "playbackState="+playbackState);

        switch (playbackState) {
            case PlaybackStateCompat.STATE_BUFFERING: {
                PleaseWait(theWidgetView);
                break;
            }
            case PlaybackStateCompat.STATE_CONNECTING: {
                PleaseWait(theWidgetView);
                break;
            }
            case PlaybackStateCompat.STATE_ERROR: {
                Autoplay(theWidgetView);
                break;
            }
            case PlaybackStateCompat.STATE_FAST_FORWARDING: {
                PleaseWait(theWidgetView);
                break;
            }
            case PlaybackStateCompat.STATE_NONE: {
                Autoplay(theWidgetView);
                break;
            }
            case PlaybackStateCompat.STATE_PAUSED: {
                Autoplay(theWidgetView);
                break;
            }
            case PlaybackStateCompat.STATE_PLAYING: {
                Pause(theWidgetView);
                break;
            }
            case PlaybackStateCompat.STATE_REWINDING: {
                PleaseWait(theWidgetView);
                break;
            }
            case PlaybackStateCompat.STATE_SKIPPING_TO_NEXT: {
                PleaseWait(theWidgetView);
                break;
            }
            case PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS: {
                PleaseWait(theWidgetView);
                break;
            }
            case PlaybackStateCompat.STATE_SKIPPING_TO_QUEUE_ITEM: {
                PleaseWait(theWidgetView);
                break;
            }
            case PlaybackStateCompat.STATE_STOPPED: {
                Autoplay(theWidgetView);
                break;
            }
        }
    }

    public static void setPaidVersion(Context context, boolean isPaid) {
        LogHelper.v(TAG, "setPaidVersion: isPaid="+isPaid);
        sPaidVersion = isPaid;
        // Build the intent to call the service
        Intent intent = new Intent(context, RadioTheaterWidgetService.class);
        intent.putExtra("REFRESH_WIDGET", true);
        // Update the widgets via the service!
        context.startService(intent);
    }

    public static boolean isWidgetButtonPress() {
        LogHelper.v(TAG, "isWidgetButtonPress: "+sGotButtonPress);
        return sGotButtonPress;
    }

    public static void setGotButtonPress(boolean buttonPress) {
        LogHelper.v(TAG, "setGotButtonPress: buttonPress="+buttonPress);
        sGotButtonPress = buttonPress;
    }

    private void Autoplay(RemoteViews remoteViews) {
        if (sPaidVersion) {
            LogHelper.v(TAG, "PLAY: setting WIDGET to 'Autoplay'");
            remoteViews.setImageViewResource(R.id.autoplay_widget, R.drawable.radio_theater_autoplay_button_selector);
            if (isWidgetButtonPress()) {
                startActionPlay();
            }
        }
        else {
            if (isWidgetButtonPress()) {
                LogHelper.v(TAG, "PLAY: setting WIDGET to Please Wait");
                remoteViews.setImageViewResource(R.id.autoplay_widget, R.drawable.radio_theater_please_wait_button_selector);
                startActionPlay();
            }
            else {
                LogHelper.v(TAG, "PLAY: setting WIDGET to (disabled) 'Autoplay'");
                remoteViews.setImageViewResource(R.id.autoplay_widget, R.drawable.radio_theater_autoplay_disabled_button_selector);
            }
        }
    }

    private void startActionPlay() {
        ConfigEpisodesCursor configCursor = SQLiteHelper.getCursorForNextAvailableEpisode();
        if (AutoplayActivity.getEpisodeData(configCursor)) {
            LogHelper.v(TAG, "PLAY: RadioControlIntentService.startActionPlay");
            RadioControlIntentService.startActionPlay(this.getApplicationContext(), "WIDGET", String.valueOf(BaseActivity.getEpisodeNumber()), BaseActivity.getEpisodeDownloadUrl());
        }
    }

    private void PleaseWait(RemoteViews remoteViews) {
        LogHelper.v(TAG, "WAIT: setting WIDGET to 'Please Wait'");
        remoteViews.setImageViewResource(R.id.autoplay_widget, R.drawable.radio_theater_please_wait_button_selector);
    }

    private void Pause(RemoteViews remoteViews) {
        if (sPaidVersion) {
            LogHelper.v(TAG, "PAUSE: setting WIDGET to 'Please Wait'");
            remoteViews.setImageViewResource(R.id.autoplay_widget, R.drawable.radio_theater_pause_button_selector);
            if (isWidgetButtonPress()) {
                startActionPause();
            }
        }
        else {
            if (isWidgetButtonPress()) {
                LogHelper.v(TAG, "PAUSE: setting WIDGET to Please Wait");
                remoteViews.setImageViewResource(R.id.autoplay_widget, R.drawable.radio_theater_please_wait_button_selector);
                startActionPause();
            }
            else {
                LogHelper.v(TAG, "PAUSE: setting WIDGET to (disabled) 'Autoplay'");
                remoteViews.setImageViewResource(R.id.autoplay_widget, R.drawable.radio_theater_autoplay_disabled_button_selector);
            }
        }
    }

    private void startActionPause() {
        LogHelper.v(TAG, "PAUSE: RadioControlIntentService.startActionPause");
        RadioControlIntentService.startActionPause(this.getApplicationContext(), "WIDGET", String.valueOf(BaseActivity.getEpisodeNumber()), BaseActivity.getEpisodeDownloadUrl());
    }

    @Override
    public IBinder onBind(Intent intent) {
        // return the communication channel to the service. null if none.
        return null;
    }

}
