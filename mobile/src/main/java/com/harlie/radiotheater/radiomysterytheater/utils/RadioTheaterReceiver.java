package com.harlie.radiotheater.radiomysterytheater.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

import com.harlie.radiotheater.radiomysterytheater.RadioTheaterService;

public class RadioTheaterReceiver extends BroadcastReceiver {
    private final static String TAG = "LEE: <" + RadioTheaterReceiver.class.getSimpleName() + ">";

    public RadioTheaterReceiver() {
        LogHelper.v(TAG, "RadioTheaterReceiver");
    }

    // method is called when the BroadcastReceiver is receiving an Intent broadcast.
    @Override
    public void onReceive(Context context, Intent intent) {
        LogHelper.v(TAG, "onReceive: intent="+intent.toString());
        if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
            LogHelper.v(TAG, "---> GOT A MEDIA BUTTON EVENT!");
            final KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

            if (event != null && event.getAction() == KeyEvent.ACTION_DOWN) {
                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                        LogHelper.v(TAG, "---> EVENT is KEYCODE_MEDIA_PLAY_PAUSE");
                        context.startService(new Intent(context, RadioTheaterService.class));
                        break;
                    default:
                        LogHelper.v(TAG, "---> EVENT KEYCODE: "+event.getKeyCode());
                }
            }
        }
        else {
            LogHelper.e(TAG, "Intent not implemented: "+intent.toString());
        }
    }

}
