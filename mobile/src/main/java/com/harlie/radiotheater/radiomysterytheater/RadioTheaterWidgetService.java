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
import com.harlie.radiotheater.radiomysterytheater.data_helper.DataHelper;
import com.harlie.radiotheater.radiomysterytheater.playback.LocalPlayback;
import com.harlie.radiotheater.radiomysterytheater.utils.LogHelper;
import com.harlie.radiotheater.radiomysterytheater.utils.RadioTheaterWidgetProvider;

// from: http://www.vogella.com/tutorials/AndroidWidgets/article.html
public class RadioTheaterWidgetService extends Service {
    private final static String TAG = "LEE: <" + RadioTheaterWidgetService.class.getSimpleName() + ">";

    //private MediaPlayer mp;

    private static volatile boolean sGotWidgetButtonPress;

    public RadioTheaterWidgetService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleCommand(intent);
        // We want this service to continue running until it is explicitly stopped, so return sticky.
        return START_STICKY;
    }

    private void handleCommand(Intent intent) {
        LogHelper.v(TAG, "handleCommand: intent="+intent.getAction());
        //mp = MediaPlayer.create(this, R.raw.click);
        //mp.start();

        setGotWidgetButtonPress(false);
        if (intent.getBooleanExtra("ERROR", false)) {
            LogHelper.v(TAG, "*** PLAYBACK ERROR REPORTED ***");
            String radio_control_command = RadioTheaterApplication.getRadioTheaterApplicationContext().getResources().getString(R.string.radio_control_command);
            String message = RadioTheaterApplication.getRadioTheaterApplicationContext().getResources().getString(R.string.error);
            Intent intentMessage = new Intent("android.intent.action.MAIN").putExtra(radio_control_command, message);
            RadioTheaterApplication.getRadioTheaterApplicationContext().sendBroadcast(intentMessage);
        }
        else {
            if (intent.getBooleanExtra("BUTTON_PRESS", false)) {
                LogHelper.v(TAG, "handleCommand: GOT WIDGET BUTTON PRESS!");
                setGotWidgetButtonPress(true);
            }
            else if (intent.getBooleanExtra("VISUAL_ONLY", false)) {
                LogHelper.v(TAG, "handleCommand: *** VISUAL BUTTON UPDATE ONLY ***");
                String radio_control_command = RadioTheaterApplication.getRadioTheaterApplicationContext().getResources().getString(R.string.radio_control_command);
                String message = RadioTheaterApplication.getRadioTheaterApplicationContext().getResources().getString(R.string.update_buttons);
                Intent intentMessage = new Intent("android.intent.action.MAIN").putExtra(radio_control_command, message);
                RadioTheaterApplication.getRadioTheaterApplicationContext().sendBroadcast(intentMessage);
            }
            else {
                LogHelper.v(TAG, "handleCommand: not BUTTON_PRESS and not VISUAL_ONLY");
            }

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this.getApplicationContext());

            int[] allWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
            if (allWidgetIds == null) {
                allWidgetIds = new int[]{R.id.autoplay_widget};
            }

            for (int widgetId : allWidgetIds) {
                RemoteViews theWidgetView = new RemoteViews(this.getApplicationContext().getPackageName(), R.layout.radio_theater_widget);

                boolean enabled = DataHelper.isPurchased() || DataHelper.isTrial();
                if (enabled) {
                    LogHelper.v(TAG, "handleCommand: enabled widget, click ok");
                    widgetButtonClick(theWidgetView);
                }
                else {
                    LogHelper.v(TAG, "handleCommand: not purchased");
                    disableWidgetButtons(theWidgetView);
                }

                // Register an onClickListener
                Intent clickIntent = new Intent(this.getApplicationContext(), RadioTheaterWidgetProvider.class);

                clickIntent.setAction(RadioTheaterWidgetProvider.ACTION_APPWIDGET_BUTTON_CLICK);
                clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        getApplicationContext(), 0, clickIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

                theWidgetView.setOnClickPendingIntent(R.id.autoplay_widget, pendingIntent);

                appWidgetManager.updateAppWidget(widgetId, theWidgetView);
            }
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
                //Stop(theWidgetView);
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
        // Build the intent to call the service
        Intent intent = new Intent(context, RadioTheaterWidgetService.class);
        intent.putExtra("BUTTON_PRESS", false);
        // Update the widgets via the service!
        context.startService(intent);
    }

    public static boolean isWidgetButtonPress() {
        LogHelper.v(TAG, "isWidgetButtonPress: "+ sGotWidgetButtonPress);
        return sGotWidgetButtonPress;
    }

    public static void setGotWidgetButtonPress(boolean buttonPress) {
        LogHelper.v(TAG, "setGotWidgetButtonPress: buttonPress="+buttonPress);
        sGotWidgetButtonPress = buttonPress;
    }

    private void Autoplay(RemoteViews remoteViews) {
        if (isWidgetButtonPress()) {
            LogHelper.v(TAG, "PLAY: setting WIDGET to Please Wait");
            remoteViews.setImageViewResource(R.id.autoplay_widget, R.drawable.radio_theater_please_wait_button_selector);
            startActionPlay();
        }
        else {
            LogHelper.v(TAG, "PLAY: setting WIDGET to 'Autoplay'");
            remoteViews.setImageViewResource(R.id.autoplay_widget, R.drawable.radio_theater_autoplay_button_selector);
        }
    }

    private void startActionPlay() {
        ConfigEpisodesCursor configCursor = DataHelper.getCursorForNextAvailableEpisode();
        if (DataHelper.getEpisodeDataForCursor(configCursor)) {
            LogHelper.v(TAG, "PLAY: RadioControlIntentService.startActionPlay");
            RadioControlIntentService.startActionPlay(this.getApplicationContext(),
                    "WIDGET",
                    DataHelper.getEpisodeNumberString(),
                    DataHelper.getEpisodeDownloadUrl(),
                    DataHelper.getEpisodeTitle());
        }
    }

    private void PleaseWait(RemoteViews remoteViews) {
        LogHelper.v(TAG, "WAIT: setting WIDGET to 'Please Wait'");
        remoteViews.setImageViewResource(R.id.autoplay_widget, R.drawable.radio_theater_please_wait_button_selector);
    }

    private void Pause(RemoteViews remoteViews) {
        if (isWidgetButtonPress()) {
            LogHelper.v(TAG, "PAUSE: setting WIDGET to Please Wait");
            remoteViews.setImageViewResource(R.id.autoplay_widget, R.drawable.radio_theater_please_wait_button_selector);
            startActionPause();
        }
        else {
            LogHelper.v(TAG, "PAUSE: setting WIDGET to 'Pause'");
            remoteViews.setImageViewResource(R.id.autoplay_widget, R.drawable.radio_theater_pause_button_selector);
        }
    }

    private void startActionPause() {
        LogHelper.v(TAG, "PAUSE: RadioControlIntentService.startActionPause");
        RadioControlIntentService.startActionPause(this.getApplicationContext(),
                "WIDGET",
                DataHelper.getEpisodeNumberString(),
                DataHelper.getEpisodeDownloadUrl());
    }

    private void Stop(RemoteViews remoteViews) {
        if (isWidgetButtonPress()) {
            LogHelper.v(TAG, "STOP: setting WIDGET to Please Wait");
            remoteViews.setImageViewResource(R.id.autoplay_widget, R.drawable.radio_theater_please_wait_button_selector);
            startActionStop();
        }
        else {
            LogHelper.v(TAG, "STOP: setting WIDGET to 'Stop'");
            remoteViews.setImageViewResource(R.id.autoplay_widget, R.drawable.radio_theater_stop_button_selector);
        }
    }

    private void startActionStop() {
        LogHelper.v(TAG, "STOP: RadioControlIntentService.startActionStop");
        RadioControlIntentService.startActionStop(this.getApplicationContext(),
                "WIDGET",
                DataHelper.getEpisodeNumberString(),
                DataHelper.getEpisodeDownloadUrl());
    }

    public void disableWidgetButtons(RemoteViews remoteViews) {
        LogHelper.v(TAG, "DISABLE: setting WIDGET to (disabled) 'Autoplay'");
        remoteViews.setImageViewResource(R.id.autoplay_widget, R.drawable.radio_theater_autoplay_disabled_button_selector);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // return the communication channel to the service. null if none.
        return null;
    }

}
