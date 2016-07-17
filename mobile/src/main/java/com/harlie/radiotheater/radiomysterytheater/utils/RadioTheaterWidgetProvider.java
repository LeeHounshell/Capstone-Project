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
    private static volatile int lastPlaybackState = -1;

    @Override
    public void onReceive(Context context, Intent intent) {
        LogHelper.v(TAG, "onReceive: action="+intent.getAction());
        super.onReceive(context, intent);
        if (intent.getStringExtra(RadioTheaterWidgetService.RADIO_THEATER_WIDGET_CONTROL) != null) {
            LogHelper.v(TAG, "*** WIDGET CONTROL ***");
        }
        if (intent.getBooleanExtra("BUTTON_PRESS", false) == true) {
            LogHelper.v(TAG, "*** WIDGET BUTTON PRESS ***");
        }
        if (intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)) {
            isInitialized = false; // just update the display, don't press anything
        }
        if (intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_ENABLED)) {
            isInitialized = false; // just update the display, don't press anything
        }
//        if (intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_DELETED)) {
//            isInitialized = false;
//        }
//        else if (intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_DISABLED)) {
//            isInitialized = false;
//        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        LogHelper.v(TAG, "onUpdate");
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        notifyWidget(context, appWidgetManager, true);
    }

    public static void notifyWidget(Context context, AppWidgetManager instance, boolean isWidgetButtonPress) {
        Intent intent = new Intent(context.getApplicationContext(), RadioTheaterWidgetService.class);
        if (!isInitialized) {
            LogHelper.v(TAG, "notifyWidget: (visual update only) - playback state="+LocalPlayback.getCurrentState()+", prior state="+lastPlaybackState);
            lastPlaybackState = LocalPlayback.getCurrentState();
            intent.putExtra("BUTTON_PRESS", false);
        }
        else if (isWidgetButtonPress) {
            LogHelper.v(TAG, "notifyWidget: (BUTTON_PRESS) - playback state="+LocalPlayback.getCurrentState()+", prior state="+lastPlaybackState);
            intent.putExtra("BUTTON_PRESS", true);
        }
        else {
            LogHelper.v(TAG, "notifyWidget - PLAYBACK STATE="+LocalPlayback.getCurrentState()+", prior state="+lastPlaybackState);
            intent.putExtra("BUTTON_PRESS", false);
        }
        isInitialized = true;
        // Build the intent to call the service
        ComponentName thisWidget = new ComponentName(context, RadioTheaterWidgetProvider.class);
        int[] allWidgetIds = instance.getAppWidgetIds(thisWidget);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);
        context.startService(intent); // Update widget via the service!
    }

}
