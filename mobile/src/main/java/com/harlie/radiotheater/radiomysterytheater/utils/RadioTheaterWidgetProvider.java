package com.harlie.radiotheater.radiomysterytheater.utils;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.harlie.radiotheater.radiomysterytheater.R;
import com.harlie.radiotheater.radiomysterytheater.RadioTheaterApplication;
import com.harlie.radiotheater.radiomysterytheater.RadioTheaterWidgetService;

// from: http://www.vogella.com/tutorials/AndroidWidgets/article.html
public class RadioTheaterWidgetProvider extends AppWidgetProvider {
    private final static String TAG = "LEE: <" + RadioTheaterWidgetProvider.class.getSimpleName() + ">";

    private static final String ACTION_CLICK = "ACTION_CLICK";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        LogHelper.v(TAG, "onUpdate");
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        notifyWidget(context, appWidgetManager);

// old
//        for (int widgetId : allWidgetIds)
//        {
//            // create some random data
//            int number = (new Random().nextInt(100));
//
//            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.radio_theater_widget);
//            LogHelper.w(TAG, "--------------------------------------> Widget Example number="+String.valueOf(number));
//            // Set the text
//            remoteViews.setTextViewText(R.id.autoplay_widget, String.valueOf(number));
//
//            // Register an onClickListener
//            Intent intent = new Intent(context, RadioTheaterWidgetProvider.class);
//
//            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
//            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
//
//            PendingIntent pendingIntent =
//                    PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//            remoteViews.setOnClickPendingIntent(R.id.autoplay_widget, pendingIntent);
//            appWidgetManager.updateAppWidget(widgetId, remoteViews);
//        }
    }

    public static void notifyWidget(Context context, AppWidgetManager appWidgetManager) {
        LogHelper.v(TAG, "notifyWidget");
        // Get all ids
        ComponentName thisWidget = new ComponentName(context, RadioTheaterWidgetProvider.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

        // Build the intent to call the service
        Intent intent = new Intent(context.getApplicationContext(), RadioTheaterWidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);

        // Update the widgets via the service!
        context.startService(intent);
    }

}
