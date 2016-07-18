package com.harlie.radiotheater.radiomysterytheater.utils;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.harlie.radiotheater.radiomysterytheater.RadioTheaterApplication;
import com.harlie.radiotheater.radiomysterytheater.RadioTheaterWidgetService;
import com.harlie.radiotheater.radiomysterytheater.playback.LocalPlayback;

// from: http://www.vogella.com/tutorials/AndroidWidgets/article.html
public class RadioTheaterWidgetProvider extends AppWidgetProvider {
    private final static String TAG = "LEE: <" + RadioTheaterWidgetProvider.class.getSimpleName() + ">";

    public static final String ACTION_APPWIDGET_BUTTON_CLICK = "BUTTON_CLICK";

    private static volatile boolean isFirstTime = true;
    private static volatile boolean isReset = false;
    private static volatile int lastPlaybackState = -1;
    private static BroadcastReceiver buttonClickReceiver = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        LogHelper.v(TAG, "onReceive: action="+intent.getAction());
        super.onReceive(context, intent);
        String action = intent.getAction();

        if (intent.getBooleanExtra("BUTTON_PRESS", false) == true) {
            LogHelper.v(TAG, "*** WIDGET BUTTON PRESS ***");
            isReset = true;
        }

        if (action.equals(RadioTheaterWidgetProvider.ACTION_APPWIDGET_BUTTON_CLICK)) {
            LogHelper.v(TAG, "*** RadioTheaterWidgetProvider.ACTION_APPWIDGET_BUTTON_CLICK ***");
            isReset = true;
            notifyWidget(context, AppWidgetManager.getInstance(context), isReset); // <<--- DO BUTTON CLICK!
        }

        else if (action.equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)) {
            LogHelper.v(TAG, "*** WIDGET CONTROL VISUAL UPDATE ***");
            isReset = false;
        }

        else if (action.equals(AppWidgetManager.ACTION_APPWIDGET_ENABLED)) {
            isReset = false;
        }

//        if (action.equals(AppWidgetManager.ACTION_APPWIDGET_DELETED)) {
//            isReset = false;
//        }
//        else if (action.equals(AppWidgetManager.ACTION_APPWIDGET_DISABLED)) {
//            isReset = false;
//        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        LogHelper.v(TAG, "onUpdate");
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        notifyWidget(context, appWidgetManager, isReset);
    }

    public static void notifyWidget(Context context, AppWidgetManager instance, boolean isWidgetButtonPress) {
        if (isFirstTime) {
            isFirstTime = false;
            LogHelper.v(TAG, "*** CREATE A RECEIVER TO COLLECT ANY WIDGET BUTTON PRESS ***");
            IntentFilter buttonClickFilter = new IntentFilter();
            buttonClickFilter.addAction(RadioTheaterWidgetProvider.ACTION_APPWIDGET_BUTTON_CLICK);
            buttonClickReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    LogHelper.v(TAG, "BUTTON_CLICK_RECEIVER: *** onReceive *** - intent="+intent.getAction());
                }
            };
            RadioTheaterApplication.getRadioTheaterApplicationContext().registerReceiver(buttonClickReceiver, buttonClickFilter);
        }

        Intent intent = new Intent(context.getApplicationContext(), RadioTheaterWidgetService.class);
        if (!isReset) {
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
        isReset = true;
        // Build the intent to call the service
        ComponentName thisWidget = new ComponentName(context, RadioTheaterWidgetProvider.class);
        int[] allWidgetIds = instance.getAppWidgetIds(thisWidget);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);
        context.startService(intent); // Update widget via the service!
    }

    @Override
    protected void finalize() throws Throwable {
        LogHelper.v(TAG, "finalize");
        if (buttonClickReceiver != null) {
            LogHelper.v(TAG, "*** unregister receiver ***");
            RadioTheaterApplication.getRadioTheaterApplicationContext().unregisterReceiver(buttonClickReceiver);
            buttonClickReceiver = null;
        }
        super.finalize();
    }

}
