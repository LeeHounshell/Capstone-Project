package com.harlie.radiotheater.radiomysterytheater;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.Button;

import com.harlie.radiotheater.radiomysterytheater.utils.LogHelper;

import java.text.SimpleDateFormat;
import java.util.Locale;

// from: https://github.com/obaro/SimpleWearApp
public class RadioWearActivity extends WearableActivity {
    private final static String TAG = "LEE: <" + RadioWearActivity.class.getSimpleName() + ">";

    private static final SimpleDateFormat AMBIENT_DATE_FORMAT =
            new SimpleDateFormat("HH:mm", Locale.US);

    private Button mAutoplayButton;
    private String mState;
    private BroadcastReceiver mWearReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogHelper.v(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radio);
        setAmbientEnabled();

        LogHelper.v(TAG, "connect WearTalkService..");
        WearTalkService.connect(this);

        mState = "toggle";
        mAutoplayButton = (Button) findViewById(R.id.autoplayButton);
        mAutoplayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                autoplay(view);
            }
        });
    }

    public void autoplay(View view) {
        LogHelper.v(TAG, "autoplay: CLICK! - currently showing radioState="+WearTalkService.getRadioState());
        // sync with phone now
        WearTalkService.createSyncMessage(this, mState);
    }

    @Override
    protected void onResume() {
        LogHelper.v(TAG, "onResume");
        super.onResume();
        IntentFilter intentFilter = new IntentFilter("com.harlie.radiotheater.radiomysterytheater.WEAR");
        mWearReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, final Intent intent) {
                try {
                    String wear_control_command = getResources().getString(R.string.wear_control_command);
                    final String message = intent.getStringExtra(wear_control_command); // new buttonState
                    int radioState = Integer.parseInt(message);
                    // TODO: create a CardView that shows detail about the currently playing episode
                    LogHelper.v(TAG, "*** RECEIVED WEAR CONTROL MESSAGE: radioState=" + radioState);
                    if (radioState == PlaybackStateCompat.STATE_PLAYING) {
                        mAutoplayButton.setBackground(getResources().getDrawable(R.drawable.radio_theater_pause_button_selector, getTheme()));
                    }
                    else {
                        mAutoplayButton.setBackground(getResources().getDrawable(R.drawable.radio_theater_autoplay_button_selector, getTheme()));
                    }
                }
                catch (Exception e) {
                    LogHelper.e(TAG, "*** ERROR DURING PROCESSING OF WEAR DATA CHANGE NOTIFICATION *** - e="+e);
                }
            }
        };
        this.registerReceiver(mWearReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        LogHelper.v(TAG, "onPause");
        super.onPause();
        this.unregisterReceiver(mWearReceiver);
    }

}
