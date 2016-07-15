package com.harlie.radiotheater.radiomysterytheater.utils;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.harlie.radiotheater.radiomysterytheater.RadioTheaterWidgetService;
import com.harlie.radiotheater.radiomysterytheater.playback.LocalPlayback;

// from: http://www.vogella.com/tutorials/AndroidWidgets/article.html
public class RadioTheaterWidgetProvider extends AppWidgetProvider {
    private final static String TAG = "LEE: <" + RadioTheaterWidgetProvider.class.getSimpleName() + ">";

    private static final String ACTION_CLICK = "ACTION_CLICK";

    private static volatile boolean isInitialized = false;
    private static volatile int lastPlaybackState = 0;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        LogHelper.v(TAG, "onUpdate");
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        notifyWidget(context, appWidgetManager);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        LogHelper.v(TAG, "onReceive: action="+intent.getAction());
        super.onReceive(context, intent);
        if (intent.getStringExtra(RadioTheaterWidgetService.RADIO_THEATER_WIDGET_CONTROL) != null) {
            LogHelper.v(TAG, "*** WIDGET PRESS ***");
        }
    }

    public static void notifyWidget(Context context, AppWidgetManager instance) {
        if (LocalPlayback.getCurrentState() != lastPlaybackState) {
            lastPlaybackState = LocalPlayback.getCurrentState();
            LogHelper.v(TAG, "notifyWidget - NEW PLAYBACK STATE="+lastPlaybackState);
            // Build the intent to call the service
            ComponentName thisWidget = new ComponentName(context, RadioTheaterWidgetProvider.class);
            int[] allWidgetIds = instance.getAppWidgetIds(thisWidget);
            Intent intent = new Intent(context.getApplicationContext(), RadioTheaterWidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);
            context.startService(intent); // Update widget via the service!
        }
        else {
            LogHelper.v(TAG, "notifyWidget: ignored - unchanged playback State="+lastPlaybackState);
        }
    }

}
