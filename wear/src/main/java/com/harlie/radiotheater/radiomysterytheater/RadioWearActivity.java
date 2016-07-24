package com.harlie.radiotheater.radiomysterytheater;

import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.DelayedConfirmationView;
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

    private Button autoplayButton;
    private DelayedConfirmationView delayedView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogHelper.v(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radio);
        setAmbientEnabled();
    }

    public void autoplay(View view) {
        LogHelper.v(TAG, "autoplay: CLICK!");
    }

}
