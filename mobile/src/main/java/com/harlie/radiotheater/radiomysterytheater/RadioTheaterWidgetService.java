package com.harlie.radiotheater.radiomysterytheater;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.media.session.PlaybackStateCompat;
import android.widget.RemoteViews;

import com.harlie.radiotheater.radiomysterytheater.playback.LocalPlayback;
import com.harlie.radiotheater.radiomysterytheater.utils.LogHelper;
import com.harlie.radiotheater.radiomysterytheater.utils.RadioTheaterWidgetProvider;

// from: http://www.vogella.com/tutorials/AndroidWidgets/article.html
public class RadioTheaterWidgetService extends Service {
    private final static String TAG = "LEE: <" + RadioTheaterWidgetService.class.getSimpleName() + ">";

    public RadioTheaterWidgetService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleCommand(intent);
        // We want this service to continue running until it is explicitly stopped, so return sticky.
        return START_STICKY;
    }

    private void handleCommand(Intent intent) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this.getApplicationContext());

        int[] allWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);

        for (int widgetId : allWidgetIds)
        {
            // this is a bit of a hack.. but i'm time pressed.
            int playbackState = LocalPlayback.getCurrentState();
            LogHelper.w(TAG, "playbackState="+playbackState);

            RemoteViews remoteViews =
                    new RemoteViews(this.getApplicationContext().getPackageName(), R.layout.radio_theater_widget);

            switch (playbackState) {
                case PlaybackStateCompat.STATE_BUFFERING: {
                    PleaseWait(remoteViews);
                    break;
                }
                case PlaybackStateCompat.STATE_CONNECTING: {
                    PleaseWait(remoteViews);
                    break;
                }
                case PlaybackStateCompat.STATE_ERROR: {
                    Autoplay(remoteViews);
                    break;
                }
                case PlaybackStateCompat.STATE_FAST_FORWARDING: {
                    PleaseWait(remoteViews);
                    break;
                }
                case PlaybackStateCompat.STATE_NONE: {
                    Autoplay(remoteViews);
                    break;
                }
                case PlaybackStateCompat.STATE_PAUSED: {
                    Autoplay(remoteViews);
                    break;
                }
                case PlaybackStateCompat.STATE_PLAYING: {
                    Pause(remoteViews);
                    break;
                }
                case PlaybackStateCompat.STATE_REWINDING: {
                    PleaseWait(remoteViews);
                    break;
                }
                case PlaybackStateCompat.STATE_SKIPPING_TO_NEXT: {
                    PleaseWait(remoteViews);
                    break;
                }
                case PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS: {
                    PleaseWait(remoteViews);
                    break;
                }
                case PlaybackStateCompat.STATE_SKIPPING_TO_QUEUE_ITEM: {
                    PleaseWait(remoteViews);
                    break;
                }
                case PlaybackStateCompat.STATE_STOPPED: {
                    Autoplay(remoteViews);
                    break;
                }
            }

            // Register an onClickListener
            Intent clickIntent = new Intent(this.getApplicationContext(), RadioTheaterWidgetProvider.class);

            clickIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    getApplicationContext(), 0, clickIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            remoteViews.setOnClickPendingIntent(R.id.autoplay_widget, pendingIntent);
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
        stopSelf();
    }

    private void Autoplay(RemoteViews remoteViews) {
        LogHelper.v(TAG, "WIDGET: <set the widget to Autoplay>");
        remoteViews.setImageViewResource(R.id.autoplay_widget, R.drawable.radio_theater_autoplay_button_selector);
        RadioTheaterControllerIntentService.startActionPlay(this.getApplicationContext(), null, null);
    }

    private void PleaseWait(RemoteViews remoteViews) {
        LogHelper.v(TAG, "WIDGET: <set the widget to Please Wait>");
        remoteViews.setImageViewResource(R.id.autoplay_widget, R.drawable.radio_theater_pause_disabled_button_selector);
    }

    private void Pause(RemoteViews remoteViews) {
        LogHelper.v(TAG, "WIDGET: <set the widget to Pause>");
        remoteViews.setImageViewResource(R.id.autoplay_widget, R.drawable.radio_theater_pause_button_selector);
        RadioTheaterControllerIntentService.startActionPause(this.getApplicationContext(), null, null);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // return the communication channel to the service. null if none.
        return null;
    }

}
