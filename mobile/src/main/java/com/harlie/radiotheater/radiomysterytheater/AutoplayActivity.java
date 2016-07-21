package com.harlie.radiotheater.radiomysterytheater;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.preference.PreferenceActivity;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.Toast;

//#IFDEF 'TRIAL'
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
//#ENDIF

import com.harlie.radiotheater.radiomysterytheater.data.configepisodes.ConfigEpisodesCursor;
import com.harlie.radiotheater.radiomysterytheater.data_helper.LoadingAsyncTask;
import com.harlie.radiotheater.radiomysterytheater.data_helper.RadioTheaterContract;
import com.harlie.radiotheater.radiomysterytheater.data_helper.SQLiteHelper;
import com.harlie.radiotheater.radiomysterytheater.playback.LocalPlayback;
import com.harlie.radiotheater.radiomysterytheater.utils.CircularSeekBar;
import com.harlie.radiotheater.radiomysterytheater.utils.LogHelper;
import com.harlie.radiotheater.radiomysterytheater.utils.OnSwipeTouchListener;
import com.harlie.radiotheater.radiomysterytheater.utils.ScrollingTextView;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import at.grabner.circleprogress.CircleProgressView;

import static com.harlie.radiotheater.radiomysterytheater.AutoplayActivity.ButtonState.PAUSE;
import static com.harlie.radiotheater.radiomysterytheater.AutoplayActivity.ButtonState.PLAY;
import static com.harlie.radiotheater.radiomysterytheater.AutoplayActivity.ButtonState.PLEASE_WAIT;


public class AutoplayActivity extends BaseActivity {
    private final static String TAG = "LEE: <" + AutoplayActivity.class.getSimpleName() + ">";

    private static final int DELAY_BEFORE_NEXT_CLICK_ALLOWED = 1000;
    private static final int DELAY_BEFORE_NEXT_VERIFY = (15 * 60 * 60);
    private static final int MIN_VISUAL_DELAY = 2000;
    private static long lastVerifyTime = 0L;
    private static long onCreateTime = 0L;

    public enum ButtonState {
        PLAY, PLEASE_WAIT, PAUSE
    }
    private static volatile ButtonState sShowingButton = PLAY;
    public static int getExpectedPlayState() {
        switch (sShowingButton) {
            case PLAY: {
                return 1;
            }
            case PLEASE_WAIT: {
                return 2;
            }
            case PAUSE: {
                return 3;
            }
            default: {
                return 0;
            }
        }
    }

    public enum ControlsState {
        ENABLED, ENABLED_SHOW_PAUSE, DISABLED, DISABLED_SHOW_PAUSE, SEEKING_POSITION
    }

    public static final String EXTRA_START_FULLSCREEN = "com.harlie.radiotheater.radiomysterytheater.EXTRA_START_FULLSCREEN";
    /**
     * Optionally used with {@link #EXTRA_START_FULLSCREEN} to carry a MediaDescription to
     * the player Activity, speeding up the screen rendering
     */
    public static final String EXTRA_CURRENT_MEDIA_DESCRIPTION = "com.harlie.radiotheater.radiomysterytheater.CURRENT_MEDIA_DESCRIPTION";

    private static final long PROGRESS_UPDATE_INTERNAL = 1000;
    private static final long PROGRESS_UPDATE_INITIAL_INTERVAL = 100;
    private static final long THIRTY_SECONDS = 30 * 1000;
    private static final long DEFAULT_DURATION = 50 * 60 * 1000;

    private RadioControlIntentService.RadioControlServiceBinder mRadioControlIntentServiceBinder;
    private AppCompatButton mAutoPlay;
    private FloatingActionButton mFabActionButton;
    private CircularSeekBar mCircularSeekBar;
    private ScrollingTextView mHorizontalScrollingText;
    private ScheduledFuture<?> mScheduleFuture;
    private BroadcastReceiver mRadioReceiver;
    private AppBarLayout mAppBarLayout;
    private long click_time;
    //private MediaPlayer mp;

    private final Handler mHandler = new Handler();
    private boolean sFirstSeekEvent = true;

    private final ScheduledExecutorService mExecutorService =
            Executors.newSingleThreadScheduledExecutor();

    // from: http://stackoverflow.com/questions/8341667/bind-unbind-service-example-android
    public ServiceConnection radioTheaterControlConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder binder) {
            LogHelper.v(TAG, "onServiceConnected");
            mRadioControlIntentServiceBinder = ((RadioControlIntentService.RadioControlServiceBinder) binder).getService();
            LogHelper.d(TAG,"ServiceConnection connected!");
            showServiceData();
        }

        public void onServiceDisconnected(ComponentName className) {
            LogHelper.v(TAG, "onServiceDisconnected");
            mRadioControlIntentServiceBinder = null;
            LogHelper.d(TAG, "ServiceConnection disconnected");
        }

    };

    private void showServiceData() {
        LogHelper.v(TAG, "showServiceData");
        LogHelper.d(TAG, "isBinderAlive()="+mRadioControlIntentServiceBinder.isBinderAlive());
    }

    public Handler radioTheaterControlHandler = new Handler() {
        public void handleMessage(Message message) {
            Bundle data = message.getData();
        }
    };

    public void doBindService() {
        LogHelper.v(TAG, "doBindService");
        Intent intent = new Intent(this, RadioControlIntentService.class);
        // Create a new Messenger for the communication back
        // From the Service to the Activity
        Messenger messenger = new Messenger(radioTheaterControlHandler);
        intent.putExtra("MESSENGER", messenger);
        bindService(intent, radioTheaterControlConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogHelper.v(TAG, "onCreate");
        final AutoplayActivity autoplayActivity = this;
        onCreateTime = System.currentTimeMillis();
        lastVerifyTime = onCreateTime; // allow 15-minute play-time buffer until automatic verifyPaidVersion check

        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        //mp = MediaPlayer.create(this, R.raw.click);

        sOkLoadFirebaseConfiguration = true;
        initializeForEpisode();

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            LogHelper.v(TAG, "rotation event");
            onRestoreInstanceState(savedInstanceState);
        }

        /*
         * FUTURE: TV support
         *
        UiModeManager uiModeManager = (UiModeManager) getSystemService(UI_MODE_SERVICE);
        if (uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION) {
            LogHelper.d(TAG, "Running on a TV Device");
            Intent tvIntent = new Intent(this, TvPlaybackActivity.class);
            tvIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION");
            tvIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            stopSeekbarUpdate();
            LogHelper.v(TAG, "STARTACTIVITY: TvPlaybackActivity.class");
            startActivity(tvIntent);
            overridePendingTransition((R.anim.abc_fade_in, R.anim.abc_fade_out,R.anim.abc_fade_in, R.anim.abc_fade_out);
            finish();
            return;
        }
        */

        runSeekbarUpdateThread();

        setContentView(R.layout.activity_autoplay);

        configureToolbarTitleBehavior();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayShowTitleEnabled(false);
            }
        }

        OnSwipeTouchListener.reset();

        createCircularSeekBar(autoplayActivity);

        setupAutoplayButton(autoplayActivity);

        initializeFloatingActionButton();

        mCircleView = (CircleProgressView) findViewById(R.id.autoplay_circle_view);

        mHorizontalScrollingText = (ScrollingTextView) findViewById(R.id.horizontal_scrolling_text);
        mHorizontalScrollingText.setVisibility(View.INVISIBLE);

        Intent intent = getIntent();
        if (intent != null) {
            final String episodeId = intent.getStringExtra("PLAY_NOW");
            if (episodeId != null) {
                LogHelper.v(TAG, "---> PLAY_NOW "+episodeId+" <---");
                displayScrollingText();
                mAutoPlay.setVisibility(View.VISIBLE);
                if (sWaitForMedia == false) {
                    sWaitForMedia = true;
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        mAppBarLayout.setExpanded(false);
                    }
                    RadioControlIntentService.startActionPlay(autoplayActivity, "MAIN", episodeId, getEpisodeDownloadUrl(), getEpisodeTitle());
                }
            }
        }

        setupAdMob();

        sLoadingScreenEnabled = true;
        RadioControlIntentService.startActionStart(this, "MAIN", String.valueOf(getEpisodeNumber()), null);
    }

    private void runSeekbarUpdateThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                scheduleSeekbarUpdate(); // THREAD TO UPDATE THE CIRCULAR SEEK BAR
            }
        }).start();
    }

    // create a new View for the seek-bar and add it into the main_frame
    protected void createCircularSeekBar(final AutoplayActivity autoplayActivity) {
        LogHelper.v(TAG, "createCircularSeekBar");
        FrameLayout theFrame = (FrameLayout) findViewById(R.id.main_frame);
        mCircularSeekBar = new CircularSeekBar(this);
        getCircularSeekBar().setEnabled(false);
        getCircularSeekBar().setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        getCircularSeekBar().setBarWidth(5);
        getCircularSeekBar().setMaxProgress((int) mDuration);
        getCircularSeekBar().setProgress(0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getCircularSeekBar().setBackgroundColor(getResources().getColor(R.color.transparent, null));
        }
        else {
            getCircularSeekBar().setBackgroundColor(getResources().getColor(R.color.transparent));
        }
        getCircularSeekBar().setVisibility(View.INVISIBLE);
        getCircularSeekBar().invalidate();
        theFrame.addView(getCircularSeekBar());

        getCircularSeekBar().setSeekBarChangeListener(new CircularSeekBar.OnSeekChangeListener() {

            @Override
            public void onProgressChange(CircularSeekBar view, final int newProgress) {
                LogHelper.v(TAG, "onProgressChange: newProgress:" + newProgress);
                sBeginLoading = true;
                RadioControlIntentService.startActionSeek(autoplayActivity, "MAIN", String.valueOf(getEpisodeNumber()), String.valueOf(newProgress));
            }
        });
    }

    protected void setupAutoplayButton(final AutoplayActivity autoplayActivity) {
        mAutoPlay = (AppCompatButton) findViewById(R.id.autoplay);
        mAutoPlay.setFocusable(true);
        showPleaseWaitButton(0);
        mAutoPlay.setVisibility(View.INVISIBLE);
        mAutoPlay.playSoundEffect(SoundEffectConstants.CLICK);

        final Drawable pressedButton = null;
        //final Drawable pressedButton = ResourcesCompat.getDrawable(getResources(), R.drawable.big_button_pressed, null);
        //final Drawable pressedButton = ResourcesCompat.getDrawable(getResources(), R.drawable.autoplay_disabled, null);

        mAppBarLayout = (AppBarLayout) findViewById(R.id.toolbar_container);
        getAutoPlay().setOnTouchListener(new OnSwipeTouchListener(this, getHandler(), getAutoPlay(), pressedButton) {

            private boolean isTimePassed() {
                if (isClickWaitExpired()) return false;
                LogHelper.v(TAG, "*** -GOOD-CLICK- ***");
                sWaitForMedia = true;
                showExpectedControls("isTimePassed-GOOD");
                getHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sWaitForMedia = false;
                        showExpectedControls("isTimePassed-ALLOW_CLICK");
                    }
                }, DELAY_BEFORE_NEXT_CLICK_ALLOWED);
                return true;
            }

            private boolean isClickWaitExpired() {
                long time_now = System.currentTimeMillis();
                if (sWaitForMedia || (time_now - click_time) < DELAY_BEFORE_NEXT_CLICK_ALLOWED) {
                    LogHelper.v(TAG, "*** -IGNORED-CLICK- ***");
                    showExpectedControls("isTimePassed-IGNORED");
                    return true;
                }
                click_time = time_now;
                return false;
            }

            @Override
            public void onClick() {
                ButtonState oldButtonState = sShowingButton;
                if (!isTimePassed()) {
                    displayScrollingText();
                    return;
                }
                LogHelper.v(TAG, "onClick");
                //mp.start();
                verifyPaidVersion(true);
                if (getEpisodeNumber() != 0) {
                    LogHelper.v(TAG, "onClick: prepare episode=" + getEpisodeNumber());
                }
                else {
                    LogHelper.v(TAG, "onClick: need to getCursorForNextAvailableEpisode");
                    ConfigEpisodesCursor configCursor = SQLiteHelper.getCursorForNextAvailableEpisode();
                    if (!getEpisodeDataForCursor(configCursor)) {
                        LogHelper.v(TAG, "popup alert - ALL EPISODES ARE HEARD!");
                        mAppBarLayout.setExpanded(true);
                        heardAllEpisodes();
                        return;
                    }
                }
                displayScrollingText();
                if (oldButtonState == PLAY) {
                    LogHelper.v(TAG, "START PLAYING..");
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        mAppBarLayout.setExpanded(false);
                    }
                    RadioControlIntentService.startActionPlay(autoplayActivity, "MAIN", String.valueOf(getEpisodeNumber()), getEpisodeDownloadUrl(), getEpisodeTitle());
                }
                else if (oldButtonState == PAUSE) {
                    LogHelper.v(TAG, "PAUSE PLAYBACK..");
                    RadioControlIntentService.startActionPause(autoplayActivity, "MAIN", String.valueOf(getEpisodeNumber()), getEpisodeDownloadUrl());
                }
            }

            @Override
            public void onDoubleClick() { // FIXME: OnSwipeTouchListener issue, low priority
                displayScrollingText();
                if (! isTimePassed()) {
                    return;
                }
                //mp.start();
                LogHelper.v(TAG, "onDoubleClick");
                RadioControlIntentService.startActionGoback(autoplayActivity, "MAIN", String.valueOf(getEpisodeNumber()), String.valueOf((THIRTY_SECONDS * 2)));
            }

            @Override
            public void onLongClick(final Drawable buttonImage) {
                displayScrollingText();
                if (! isTimePassed()) {
                    return;
                }
                //mp.start();
                LogHelper.v(TAG, "onLongClick");
                if (LocalPlayback.getCurrentState() == PlaybackStateCompat.STATE_PLAYING) {
                    LogHelper.v(TAG, "*** LONG-CLICK - STOP PLAYING CURRENT EPISODE ***");
                    RadioControlIntentService.startActionStop(autoplayActivity, "MAIN", String.valueOf(getEpisodeNumber()), getEpisodeDownloadUrl());
                }
                getHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        LogHelper.v(TAG, "*** disable circular scrollbar ***");
                        mCurrentPosition = 0;
                        getCircularSeekBar().setProgress(0);
                        getCircularSeekBar().setVisibility(View.INVISIBLE);
                        showExpectedControls("onLongClick");
                    }
                }, 2000);
            }

            @Override
            public void onSwipeRight() {
                displayScrollingText();
                if (! isTimePassed()) {
                    return;
                }
                //mp.start();
                LogHelper.v(TAG, "onSwipeRight");
                RadioControlIntentService.startActionNext(autoplayActivity, "MAIN", String.valueOf(getEpisodeNumber()), getEpisodeDownloadUrl());
                markEpisodeAsHeardAndIncrementPlayCount(getEpisodeNumber(), String.valueOf(getEpisodeNumber()), mCurrentPosition);
            }

            @Override
            public void onSwipeLeft() {
                displayScrollingText();
                if (! isTimePassed()) {
                    return;
                }
                //mp.start();
                LogHelper.v(TAG, "onSwipeLeft");
                RadioControlIntentService.startActionPrev(autoplayActivity, "MAIN", String.valueOf(getEpisodeNumber()), getEpisodeDownloadUrl());
                markEpisodeAs_NOT_Heard(getEpisodeNumber(), String.valueOf(getEpisodeNumber()), 0);
            }

/*
//            @Override
//            public void onSwipeUp() { // FIXME: OnSwipeTouchListener issue, low priority
//                displayScrollingText();
//                if (! isTimePassed()) {
//                    return;
//                }
//                //mp.start();
//                LogHelper.v(TAG, "onSwipeUp");
//                getHandler().post(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (getRadioMediaController() != null) {
//                            mCurrentPosition += THIRTY_SECONDS;
//                            getRadioMediaController().getTransportControls().seekTo(mCurrentPosition);
//                        }
//                    }
//                });
//            }
//
//            @Override
//            public void onSwipeDown() { // FIXME: OnSwipeTouchListener issue, low priority
//                displayScrollingText();
//                if (! isTimePassed()) {
//                    return;
//                }
//                //mp.start();
//                LogHelper.v(TAG, "onSwipeDown");
//                getHandler().post(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (getRadioMediaController() != null) {
//                            mCurrentPosition -= THIRTY_SECONDS;
//                            getRadioMediaController().getTransportControls().seekTo(mCurrentPosition);
//                        }
//                    }
//                });
//            }
*/

        });
    }

    protected void initializeFloatingActionButton() {
        LogHelper.v(TAG, "initializeFloatingActionButton");
        mFabActionButton = (FloatingActionButton) findViewById(R.id.fab);
        getFabActionButton().setFocusable(true);
        if (isLoadedOK()) {
            sEnableFAB = true;
            getFabActionButton().setEnabled(true);
            getFabActionButton().setVisibility(View.VISIBLE);
        }
        else {
            getFabActionButton().setEnabled(false);
            getFabActionButton().setVisibility(View.INVISIBLE);
        }

        final AutoplayActivity activity = this;
        getFabActionButton().setOnTouchListener(new OnSwipeTouchListener(this, getHandler(), getFabActionButton()) {

            @Override
            public void onClick() {
                int lastPlaybackState = LocalPlayback.getCurrentState();
                if (PlaybackStateCompat.STATE_PLAYING != lastPlaybackState) {
                    ConfigEpisodesCursor configCursor = SQLiteHelper.getCursorForNextAvailableEpisode();
                    if (getEpisodeDataForCursor(configCursor)) {
                        displayScrollingText();
                    }
                }
                LogHelper.v(TAG, "onClick - mFabActionButton");
                Intent episodeListIntent = new Intent(activity, EpisodeListActivity.class);
                Bundle playInfo = new Bundle();
                savePlayInfoToBundle(playInfo);
                episodeListIntent.putExtras(playInfo);
                episodeListIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                stopSeekbarUpdate();
                LogHelper.v(TAG, "STARTACTIVITY: EpisodeListActivity.class");
                startActivity(episodeListIntent);
                overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
                if (!sHandleRotationEvent && EpisodeListActivity.isTwoPane() && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    FragmentManager fragmentManager = activity.getSupportFragmentManager();
                    fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                }
            }
        });
    }

    private void setupAdMob() {
        // initialize AdMob
        //#IFDEF 'TRIAL'
        if (sPurchased != true && sNoAdsForShow != true) {
            LogHelper.v(TAG, "setupAdMob");
            String banner_ad_unit_id = getResources().getString(R.string.banner_ad_unit_id);
            MobileAds.initialize(getApplicationContext(), banner_ad_unit_id);

            final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);

            AdView mAdView = (AdView) findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)        // All emulators
                    .addTestDevice(getResources().getString(R.string.test_device1))
                    .addTestDevice(getResources().getString(R.string.test_device2))
                    .build();
            mAdView.loadAd(adRequest);
        }
        //#ENDIF
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        LogHelper.v(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        LogHelper.v(TAG, "onRestoreInstanceState");
        super.onRestoreInstanceState(savedInstanceState);
    }

    //--------------------------------------------------------------------------------
    // UPDATE THE CIRCULAR SEEK BAR
    private final Runnable mUpdateProgressTask = new Runnable() {
        @Override
        public void run() {
            getHandler().post(new Runnable() {
                @Override
                public void run() {
                    updateCircularSeekbar();
                }
            });
        }
    };
    //--------------------------------------------------------------------------------

    @Override
    protected void enableButtons() {
        LogHelper.v(TAG, "-> enableButtons <-");
        getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                sEnableFAB = true;
                showExpectedControls("enableButtons #1");
            }
        }, 3000);
        if (getAutoPlay() != null) {
            mAutoPlay.setEnabled(true);
            mAutoPlay.setVisibility(View.VISIBLE);
            mHorizontalScrollingText.setVisibility(View.VISIBLE);
            getHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (getAutoPlay() != null) {
                        sEnableFAB = true;
                        showExpectedControls("enableButtons #2");
                    }
                    else {
                        LogHelper.w(TAG, "*** UNABLE TO GET AUTOPLAY BUTTON!? ***");
                    }
                }
            }, 2000);
        }
        else {
            LogHelper.w(TAG, "*** UNABLE TO GET AUTOPLAY BUTTON! ***");
        }
    }

    private void do_UpdateControls() {
        LogHelper.v(TAG, "do_UpdateControls");
        getHandler().post(new Runnable() {
            @Override
            public void run() {
                updateControls();
            }
        });
    }

    protected void updateControls() {
        LogHelper.v(TAG, "updateControls");
        if (sBeginLoading) {
            LogHelper.v(TAG, "sBeginLoading - skipping updateControls");
            return;
        }
        showExpectedControls("updateControls");
    }

    private void heardAllEpisodes() {
        AlertDialog alertDialog = new AlertDialog.Builder(AutoplayActivity.this).create();
        alertDialog.setTitle(getResources().getString(R.string.nothing_unheard));
        alertDialog.setMessage(getResources().getString(R.string.heard_everything));
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        trackWithFirebaseAnalytics("ALL-EPISODES", 0, "EVERYTHING HEARD");
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    private void problemLoadingMetadata() {
        LogHelper.v(TAG, "problemLoadingMetadata");
        mCurrentPosition = 0;
        AlertDialog alertDialog = new AlertDialog.Builder(AutoplayActivity.this).create();
        alertDialog.setTitle(getResources().getString(R.string.no_metadata));
        alertDialog.setMessage(getResources().getString(R.string.metadata_loading_problem));
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        trackWithFirebaseAnalytics(String.valueOf(sEpisodeNumber), mCurrentPosition, "load metadata failed");
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    private void problemWithPlayback() {
        LogHelper.v(TAG, "problemWithPlayback");
        mCurrentPosition = 0;
        AlertDialog alertDialog = new AlertDialog.Builder(AutoplayActivity.this).create();
        alertDialog.setTitle(getResources().getString(R.string.unable_to_load));
        alertDialog.setMessage(getResources().getString(R.string.playback_problem));
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        trackWithFirebaseAnalytics(String.valueOf(sEpisodeNumber), mCurrentPosition, "playback failed");
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    protected void displayScrollingText() {
        LogHelper.v(TAG, "displayScrollingText");
        if (isLoadedOK()) {
            getHandler().post(new Runnable() {
                @Override
                public void run() {
                    ScrollingTextView horizontalScrollingText = getHorizontalScrollingText();
                    if (horizontalScrollingText != null && getAirdate() != null && getEpisodeTitle() != null && getEpisodeDescription() != null) {
                        setHorizontalScrollingText(horizontalScrollingText);
                    }
                    else {
                        // error state recovery
                        getEpisodeInfoFor(getEpisodeNumber());
                        if (horizontalScrollingText != null && getAirdate() != null && getEpisodeTitle() != null && getEpisodeDescription() != null) {
                            setHorizontalScrollingText(horizontalScrollingText);
                        }
                        else {
                            LogHelper.v(TAG, "UNABLE TO DISPLAY SCROLLING TEXT: episode="+getEpisodeNumber()+", airdate="+getAirdate()+", title="+getTitle()+", description="+getEpisodeDescription());
                        }
                    }
                }

                protected void setHorizontalScrollingText(ScrollingTextView horizontalScrollingText) {
                    horizontalScrollingText.setText("         ... Airdate: " + RadioTheaterContract.airDate(getAirdate())
                            + " ... Episode #" + getEpisodeNumber() + " ... " + getEpisodeTitle() + " ... " + getEpisodeDescription());
                    horizontalScrollingText.setEnabled(true);
                    horizontalScrollingText.setSelected(true);
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        LogHelper.v(TAG, "onCreateOptionsMenu");
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        LogHelper.v(TAG, "onOptionsItemSelected");
        switch (item.getItemId()) {
/*
            case R.id.search: {
                LogHelper.v(TAG, "-> SEARCH <-");
                // FIXME: voice search
                // FIXME: this 'search' button needs to build a new playlist and then submit that to be the new active playlist.
                // FIXME: because of time-limitations, this feature will be built last, time permitting.
                trackSearchWithFirebaseAnalytics();
                return true;
            }
*/

            case R.id.share: {
                long episode = getEpisodeNumber();
                LogHelper.v(TAG, "-> SHARE <-");
                if (episode == 0) {
                    ConfigEpisodesCursor configCursor = SQLiteHelper.getCursorForNextAvailableEpisode();
                    getEpisodeDataForCursor(configCursor);
                    episode = getEpisodeNumber();
                    if (episode == 0) {
                        String message = getResources().getString(R.string.unable_to_share);
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                        return true;
                    }
                }
                sShareActionProvider = new ShareActionProvider(this);
                Intent shareIntent = setShareIntentForEpisode(episode);
                sShareActionProvider.setShareIntent(shareIntent);
                MenuItemCompat.setActionProvider(item, sShareActionProvider);
                LogHelper.v(TAG, "onCreateOptionsMenu: sShareActionProvider="+sShareActionProvider);
                if (shareIntent != null) {
                    LogHelper.v(TAG, "STARTACTIVITY: shareEpisode");
                    startActivity(Intent.createChooser(shareIntent, "Share via"));
                }
                else {
                    String message = getResources().getString(R.string.unable_to_share);
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                }
                return true;
            }

            case R.id.settings: {
                LogHelper.v(TAG, "-> SETTINGS <-");
                Intent intent = new Intent(this, SettingsActivity.class);
                Bundle playInfo = new Bundle();
                savePlayInfoToBundle(playInfo);
                intent.putExtras(playInfo);
                intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, SettingsActivity.GeneralPreferenceFragment.class.getName());
                intent.putExtra(PreferenceActivity.EXTRA_NO_HEADERS, true);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                stopSeekbarUpdate();
                LogHelper.v(TAG, "STARTACTIVITY: SettingsActivity.class");
                startActivity(intent);
                overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
                trackSettingsWithFirebaseAnalytics();
                return true;
            }

            case R.id.about: {
                LogHelper.v(TAG, "-> ABOUT <-");
                Intent intent = new Intent(this, AboutActivity.class);
                Bundle playInfo = new Bundle();
                savePlayInfoToBundle(playInfo);
                intent.putExtras(playInfo);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                stopSeekbarUpdate();
                LogHelper.v(TAG, "STARTACTIVITY: AboutActivity.class");
                startActivity(intent);
                overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
                trackAboutWithFirebaseAnalytics();
                return true;
            }

            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    @Override
    protected Intent getShareIntent(String episodeTitle, String episodeDescription, String episodeNumber, String episodeDownloadUrl, String webLinkUrl) {
        LogHelper.v(TAG, "getShareIntent");
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // FIXED: only set this for API 21+
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        } else {
            // from: http://stackoverflow.com/questions/32941254/is-there-anything-similar-to-flag-activity-new-document-for-older-apis
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        shareIntent.setType("text/html");

        // always send a link to the free version
        String packageId = getApplicationContext().getPackageName();
        packageId = packageId.replace(".radiomysterytheater.paid", ".radiomysterytheater");
        String appLink = getResources().getString(R.string.email_share_applink) + packageId;

        String episode = getResources().getString(R.string.email_share_episode);
        String checkitout = getResources().getString(R.string.email_share_checkitout);
        String clicktohear = getResources().getString(R.string.email_share_clicktohear);
        String radiomysterytheater = getResources().getString(R.string.email_share_radio);
        String hereis = getResources().getString(R.string.email_share_hereis);
        String webpage = getResources().getString(R.string.email_share_webpage);
        String mp3 = getResources().getString(R.string.email_share_mp3);
        String mp3_download = getResources().getString(R.string.email_share_mp3_download);

        String share_body = "\n<body>\n<br>"+checkitout+"\n\n"
                + episode + episodeNumber + " - \"" + episodeTitle + "\""
                + "\n<br>\n<br>\n" + episodeDescription
                + "\n<br>\n<br> <a href=\"" + appLink + "\">" + clicktohear + episodeTitle + radiomysterytheater + "</a>"
                + "\n<br>\n<br> " + hereis + " <a href=\"" + webLinkUrl.replace("_name", "") + "\">" + webpage + "</a>"
                + "\n<br>\n<br> " + mp3 + " <a href=\"" + episodeDownloadUrl + "\">" + mp3_download + "</a>"
                + "\n</body>\n";

        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_text));
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            shareIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(share_body, Html.FROM_HTML_MODE_LEGACY));
        }
        else {
            //noinspection deprecation
            shareIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(share_body));
        }
        return shareIntent;
    }

    //--------------------------------------------------------------------------------
    // UPDATE THE CIRCULAR SEEK BAR
    //--------------------------------------------------------------------------------
    //
    private void stopSeekbarUpdate() {
        LogHelper.v(TAG, "stopSeekbarUpdate");
        if (getScheduleFuture() != null) {
            LogHelper.v(TAG, "stopSeekbarUpdate: cancelling..");
            getScheduleFuture().cancel(false);
        }
    }

    private void scheduleSeekbarUpdate() {
        LogHelper.v(TAG, "scheduleSeekbarUpdate");
        if (!mExecutorService.isShutdown()) {
            LogHelper.v(TAG, "scheduleSeekbarUpdate: first shutdown old mExecutorService");
            stopSeekbarUpdate();
        }
        LogHelper.v(TAG, "scheduleSeekbarUpdate: start the mExecutorService");
        mScheduleFuture = mExecutorService.scheduleAtFixedRate(
                new Runnable() {
                    @Override
                    public void run() {
                        //LogHelper.v(TAG, "...invoke updateCircularSeekbar...");
                        getHandler().post(mUpdateProgressTask);
                    }
                },
                    PROGRESS_UPDATE_INITIAL_INTERVAL,
                    PROGRESS_UPDATE_INTERNAL,
                    TimeUnit.MILLISECONDS);
    }

    protected void updateCircularSeekbar() {
        if (sFirstSeekEvent) {
            sFirstSeekEvent = false;
            displayScrollingText();
        }
        int lastPlaybackState = LocalPlayback.getCurrentState();
        if (PlaybackStateCompat.STATE_PLAYING == lastPlaybackState) {
            if (getExpectedPlayState() == 1) {
                sWaitForMedia = false;
            }
            if (getAutoPlay() != null) {
                showExpectedControls("updateCircularSeekbar (playing)");
                if (getCircularSeekBar() != null && !getCircularSeekBar().isProcessingTouchEvents()) {
                    LoadingAsyncTask.sDoneLoading = true;
                    // we need to determine the current bar location and update the display
                    mCurrentPosition = (long) LocalPlayback.getCurrentPosition();
                    getCircularSeekBar().setProgress((int) mCurrentPosition);
                    if (sShowingButton != PLEASE_WAIT) {
                        if (mCurrentPosition > 0) {
                            LogHelper.v(TAG, "...updateCircularSeekbar (visible)... " + mCurrentPosition);
                            getCircularSeekBar().setVisibility(View.VISIBLE);
                        } else {
                            LogHelper.v(TAG, "...updateCircularSeekbar (invisible)... " + mCurrentPosition);
                            getCircularSeekBar().setVisibility(View.INVISIBLE);
                        }
                    }
                    // NOTE: a performance impact exists with the verifyPaidVersion operation, so every seekbar update is too much
                    long now = System.currentTimeMillis();
                    if (isLoadedOK() && (now - lastVerifyTime) > DELAY_BEFORE_NEXT_VERIFY) {
                        lastVerifyTime = now;
                        verifyPaidVersion(false);
                    }
                }
            }
        }
        else if (
                PlaybackStateCompat.STATE_PAUSED == lastPlaybackState ||
                        PlaybackStateCompat.STATE_STOPPED == lastPlaybackState
                )
        {
            if (getExpectedPlayState() == 3) {
                sWaitForMedia = false;
            }
        }
    }

    private void updateDuration(long duration) {
        LogHelper.d(TAG, "updateDuration: duration="+duration);
        if (!sHaveRealDuration) {
            mDuration = (int) duration;
        }
        LogHelper.v(TAG, "===> EPISODE DURATION="+mDuration/1000+", sHaveRealDuration="+ sHaveRealDuration);
        getCircularSeekBar().setMaxProgress((int) mDuration);
        showExpectedControls("updateDuration");
    }
    //--------------------------------------------------------------------------------

    protected void loadingScreen() {
        LogHelper.d(TAG, "loadingScreen: enabled="+ sLoadingScreenEnabled);
        if (sLoadingScreenEnabled && !LoadingAsyncTask.sLoadingNow) {
            sBeginLoading = true;
            showExpectedControls("loadingScreen");
            LogHelper.v(TAG, "*** -------------------------------------------------------------------------------- ***");
            LoadingAsyncTask asyncTask = new LoadingAsyncTask(this, mCircleView, getCircularSeekBar(), getAutoPlay());
            asyncTask.execute();
            sProgressViewSpinning = true;
            LogHelper.v(TAG, "*** -------------------------------------------------------------------------------- ***");
        }
    }

    @Override
    public void setCircleViewValue(float value) {
        LogHelper.v(TAG, "setCircleViewValue: "+value);
        super.setCircleViewValue(value);
        mCurrentPosition = (long) value;
        if (value != 0) {
            sBeginLoading = false;
            LoadingAsyncTask.sDoneLoading = true;
        }
        showExpectedControls("setCircleViewValue value="+value);
    }

    @Override
    protected void onPause() {
        LogHelper.d(TAG, "onPause");
//        if (mRadioControlIntentServiceBinder != null) {
//            unbindService(radioTheaterControlConnection);
//            mRadioControlIntentServiceBinder = null;
//        }
        super.onPause();
        this.unregisterReceiver(getReceiver());
    }

    @Override
    protected void onResume() {
        LogHelper.d(TAG, "onResume");
//        if (mRadioControlIntentServiceBinder == null) {
//            doBindService();
//        }
        super.onResume();
        if (isLoadedOK()) {
            enableButtons();
        }
        IntentFilter intentFilter = new IntentFilter("android.intent.action.MAIN");

        //--------------------------------------------------------------------------------
        // AUTOPLAY MESSAGE RECEIVER AND PROCESSOR
        //--------------------------------------------------------------------------------
        final AutoplayActivity autoplayActivity = this;
        mRadioReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                String initialization = RadioTheaterApplication.getRadioTheaterApplicationContext().getResources().getString(R.string.initialization);
                final String message = intent.getStringExtra(initialization); // get any control messages, for example: did metadata load ok? or play:id etc..
                LogHelper.v(TAG, "*** RECEIVED BROADCAST CONTROL: "+message);

                if (message == null) {
                    LogHelper.e(TAG, "*** *** *** FAILED TO RECEIVE MESSAGE! - CRITICAL ERROR");
                    return;
                }

                final String KEY_LOAD_OK = getResources().getString(R.string.metadata_loaded);
                final String KEY_LOAD_FAIL = getResources().getString(R.string.error_no_metadata);
                final String KEY_UPDATE_BUTTONS = getResources().getString(R.string.update_buttons);
                final String KEY_DURATION = getResources().getString(R.string.duration);
                final String KEY_REQUEST = getResources().getString(R.string.request_play);
                final String KEY_PLAYING = getResources().getString(R.string.playing);
                final String KEY_NOPLAY = getResources().getString(R.string.noplay);
                final String KEY_COMPLETION = getResources().getString(R.string.complete);

                // LOAD_OK
                if (message.equals(KEY_LOAD_OK)) {
                    LogHelper.v(TAG, KEY_LOAD_OK);
                    getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            LogHelper.v(TAG, "*** RECEIVED BROADCAST: LOAD_OK - episode="+getEpisodeNumber());
                            LogHelper.v(TAG, "LOAD_OK: "+getEpisodeNumber()+" "+getEpisodeTitle());
                            enableButtons();
                            if (sHandleRotationEvent) {
                                getEpisodeInfoFor(getEpisodeNumber());
                                sLoadedOK = true; // update scrolling text now
                            }
                            displayScrollingText();
                            sLoadedOK = true; // after 'displayScrollingText' because i don't want to see Episode detail until after the first Autoplay click
                            sWaitForMedia = false;
                            autoplayActivity.checkUpdateWidget(autoplayActivity, sAllListenCount);
                        }
                    });
                }

                // LOAD_FAIL
                else if (message.equals(KEY_LOAD_FAIL)) {
                    LogHelper.v(TAG, KEY_LOAD_FAIL);
                    getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            LogHelper.v(TAG, "*** RECEIVED BROADCAST: LOAD_FAIL");
                            sWaitForMedia = false;
                            problemLoadingMetadata();
                            showExpectedControls("LOAD_FAIL");
                        }
                    });
                }

                // UPDATE_BUTTONS
                else if (message.equals(KEY_UPDATE_BUTTONS)) {
                    LogHelper.v(TAG, KEY_UPDATE_BUTTONS);
                    getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            LogHelper.v(TAG, "*** RECEIVED BROADCAST: UPDATE_BUTTONS");
                            showExpectedControls("UPDATE_BUTTONS");
                        }
                    });
                }

                // DURATION
                else if (message.length() > KEY_DURATION.length() && message.substring(0, KEY_DURATION.length()).equals(KEY_DURATION)) {
                    LogHelper.v(TAG, KEY_DURATION);
                    getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            LogHelper.v(TAG, "*** RECEIVED BROADCAST: EPISODE DURATION");
                            sLoadedOK = true;
                            displayScrollingText();
                            showExpectedControls("DURATION");
                            sWaitForMedia = false;
                            mDuration = Long.valueOf(message.substring(KEY_DURATION.length(), message.length()));
                            sHaveRealDuration = true;
                            getCircularSeekBar().setMaxProgress((int) mDuration);
                            LogHelper.v(TAG, "*** REVISED EPISODE DURATION="+mDuration);
                            updateDuration(mDuration);
                            do_UpdateControls();
                        }
                    });
                }

                // REQUEST
                else if (message.length() > KEY_REQUEST.length() && message.substring(0, KEY_REQUEST.length()).equals(KEY_REQUEST)) {
                    String episodeIndex = message.substring(KEY_REQUEST.length(), message.length());
                    LogHelper.v(TAG,  "*** RECEIVED BROADCAST: REQUEST TO PLAY EPISODE "+episodeIndex);
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        setAppBarExpanded(false, false);
                    }
                    sLoadedOK = true;
                    displayScrollingText();
                    showExpectedControls("REQUEST");
                    RadioControlIntentService.startActionPlay(autoplayActivity, "MAIN", String.valueOf(getEpisodeNumber()), getEpisodeDownloadUrl(), getEpisodeTitle());
                }

                // PLAYING
                else if (message.length() > KEY_PLAYING.length() && message.substring(0, KEY_PLAYING.length()).equals(KEY_PLAYING)) {
                    final String episodeIndex = message.substring(KEY_PLAYING.length(), message.length());
                    LogHelper.v(TAG,  "*** RECEIVED BROADCAST: NOW PLAYING EPISODE "+episodeIndex);
                    getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            // NEXT and PREV need to show the new Episode info
                            sLoadedOK = true;
                            sWaitForMedia = false;
                            long episode = Long.valueOf(episodeIndex);
                            setEpisodeNumber(episode);
                            getEpisodeInfoFor(getEpisodeNumber());
                            LogHelper.v(TAG, "PLAYING: "+getEpisodeNumber()+" "+getEpisodeTitle()+", episode="+episode);
                            showExpectedControls("PLAYING");
                            displayScrollingText();
                            setAppBarExpanded(false, true);
                        }
                    });
                }

                // NOPLAY
                else if (message.length() > KEY_NOPLAY.length() && message.substring(0, KEY_NOPLAY.length()).equals(KEY_NOPLAY)) {
                    String episodeIndex = message.substring(KEY_NOPLAY.length(), message.length());
                    LogHelper.v(TAG,  "*** RECEIVED BROADCAST: UNABLE TO PLAY EPISODE "+episodeIndex);
                    getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            LogHelper.v(TAG, "*** RECEIVED BROADCAST: NOT ABLE TO PLAY");
                            sWaitForMedia = false;
                            setAppBarExpanded(true, false);
                            problemWithPlayback();
                        }
                    });
                }

                // COMPLETION
                else if (message.length() > KEY_COMPLETION.length() && message.substring(0, KEY_COMPLETION.length()).equals(KEY_COMPLETION)) {
                    String episodeIndex = message.substring(KEY_COMPLETION.length(), message.length());
                    markEpisodeAsHeardAndIncrementPlayCount(Long.parseLong(episodeIndex), episodeIndex, mDuration);
                    LogHelper.v(TAG,  "*** RECEIVED BROADCAST: COMPLETED PLAY EPISODE "+episodeIndex);
                    ConfigEpisodesCursor configCursor = SQLiteHelper.getCursorForNextAvailableEpisode();
                    if (!getEpisodeDataForCursor(configCursor)) {
                        getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                LogHelper.v(TAG, "popup alert - ALL EPISODES ARE HEARD!");
                                setAppBarExpanded(true, false);
                                sWaitForMedia = false;
                                heardAllEpisodes();
                            }
                        });
                    }
                    else {
                        LogHelper.v(TAG, "=========> START AUTOPLAY FOR NEXT AVAILABLE EPISODE: "+getEpisodeNumber());
                        setAppBarExpanded(true, false);
                        sWaitForMedia = true;
                        showPleaseWaitButton(0);
                        startPlaybackForEpisode(String.valueOf(getEpisodeNumber()), autoplayActivity);
                        displayScrollingText();
                    }
                }

                // UNKNOWN
                else {
                    LogHelper.v(TAG, "*** UNKNOWN MESSAGE VIA INTENT: "+message);
                }
            }
        };
        this.registerReceiver(getReceiver(), intentFilter);
    }

    private void setAppBarExpanded(boolean expanded, boolean override) {
        if (override || getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mAppBarLayout.setExpanded(expanded);
        }
        else {
            mAppBarLayout.setExpanded(true);
        }
    }

    protected void startPlaybackForEpisode(final String episodeIndex, final AutoplayActivity autoplayActivity) {
        LogHelper.v(TAG, "startPlaybackForEpisode: episodeIndex="+episodeIndex);
        getHandler().post(new Runnable() {
            @Override
            public void run() {
                initializeForEpisode();
                getEpisodeInfoFor(Long.parseLong(episodeIndex));
                mCurrentPosition = 0;
                showPleaseWaitButton(0);
                displayScrollingText();
                verifyPaidVersion(true);
                RadioControlIntentService.startActionPlay(autoplayActivity, "MAIN", episodeIndex, getEpisodeDownloadUrl(), getEpisodeTitle());
            }
        });
    }

    private void initializeForEpisode() {
        mDuration = DEFAULT_DURATION; // fifty-minutes in ms
        sHaveRealDuration = false;
        mCurrentPosition = 0;
        sEpisodeDownloadUrl = null;
        sEpisodeTitle = null;
        sEpisodeDescription = null;
    }

    public boolean verifyPaidVersion(final boolean handleClick) {
        LogHelper.v(TAG, "verifyPaidVersion");
        //#IFDEF 'TRIAL'
        final AutoplayActivity autoplayActivity = this;
        if ((sPurchased != true) && ! isTrial()) {
            RadioControlIntentService.startActionStop(autoplayActivity, "MAIN", String.valueOf(getEpisodeNumber()), getEpisodeDownloadUrl());
            getHandler().post(new Runnable() {
                @Override
                public void run() {
                    LogHelper.v(TAG, "*** verifyPaidVersion - TRIAL EXPIRED ***");
                    maxTrialEpisodesAreReached();
                    RadioTheaterWidgetService.setPaidVersion(autoplayActivity, false);
                    if (handleClick) {
                        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                            mAppBarLayout.setExpanded(false);
                        }
                        displayScrollingText();
                        RadioControlIntentService.startActionPlay(autoplayActivity, "MAIN", String.valueOf(getEpisodeNumber()), getEpisodeDownloadUrl(), getEpisodeTitle());
                    }
                }
            });
            return false;
        }
        //#ENDIF
        RadioTheaterWidgetService.setPaidVersion(this, true);
        return true;
    }

    @Override
    protected void onStart() {
        LogHelper.d(TAG, "onStart");
        super.onStart();
    }

    @Override
    protected void onStop() {
        LogHelper.d(TAG, "onStop");
        super.onStop();
    }

    @Override
    public void onDestroy() {
        LogHelper.v(TAG, "onDestroy");
        stopSeekbarUpdate();
        mExecutorService.shutdown();
        super.onDestroy();
        lastVerifyTime = 0L;
        onCreateTime = 0L;
    }

    public AppCompatButton getAutoPlay() {
        return mAutoPlay;
    }

    public FloatingActionButton getFabActionButton() {
        return mFabActionButton;
    }

    public CircularSeekBar getCircularSeekBar() {
        return mCircularSeekBar;
    }

    public ScrollingTextView getHorizontalScrollingText() {
        return mHorizontalScrollingText;
    }

    public ScheduledFuture<?> getScheduleFuture() {
        return mScheduleFuture;
    }

    public BroadcastReceiver getReceiver() {
        return mRadioReceiver;
    }

    public Handler getHandler() {
        return mHandler;
    }

    public void showExpectedControls(String log) {
        long now = System.currentTimeMillis();
        if (sEnableFAB) {
            getFabActionButton().setEnabled(true);
            getFabActionButton().setVisibility(View.VISIBLE);
        }
        else {
            getFabActionButton().setVisibility(View.INVISIBLE);
        }
        int playbackState = LocalPlayback.getCurrentState();
        if (sWaitForMedia) {
            //LogHelper.v(TAG, "showExpectedControls: sWaitForMedia");
            if (mCurrentPosition > 0) {
                showPleaseWaitButton(0);
            }
            else {
                Drawable autoplayDisabledButton = ResourcesCompat.getDrawable(getResources(), R.drawable.radio_theater_autoplay_disabled_button_selector, null);
                getAutoPlay().setBackground(autoplayDisabledButton);
            }
        } else {
            //LogHelper.w(TAG, "showExpectedControls: log="+log+", playbackState="+playbackState);
            switch (playbackState) {
                case PlaybackStateCompat.STATE_BUFFERING: {
                    //LogHelper.v(TAG, "PlaybackStateCompat.STATE_BUFFERING");
                    showPleaseWaitButton(playbackState);
                    break;
                }
                case PlaybackStateCompat.STATE_CONNECTING: {
                    //LogHelper.v(TAG, "PlaybackStateCompat.STATE_CONNECTING");
                    showPleaseWaitButton(playbackState);
                    break;
                }
                case PlaybackStateCompat.STATE_ERROR: {
                    //LogHelper.v(TAG, "PlaybackStateCompat.STATE_ERROR");
                    showAutoplayButton(playbackState);
                    break;
                }
                case PlaybackStateCompat.STATE_FAST_FORWARDING: {
                    //LogHelper.v(TAG, "PlaybackStateCompat.STATE_FAST_FORWARDING");
                    showPleaseWaitButton(playbackState);
                    break;
                }
                case PlaybackStateCompat.STATE_NONE: {
                    //LogHelper.v(TAG, "PlaybackStateCompat.STATE_NONE");
                    showAutoplayButton(playbackState);
                    break;
                }
                case PlaybackStateCompat.STATE_PAUSED: {
                    //LogHelper.v(TAG, "PlaybackStateCompat.STATE_PAUSED");
                    showAutoplayButton(playbackState);
                    break;
                }
                case PlaybackStateCompat.STATE_PLAYING: {
                    //LogHelper.v(TAG, "PlaybackStateCompat.STATE_PLAYING");
                    showPauseButton(playbackState);
                    break;
                }
                case PlaybackStateCompat.STATE_REWINDING: {
                    //LogHelper.v(TAG, "PlaybackStateCompat.STATE_REWINDING");
                    showPleaseWaitButton(playbackState);
                    break;
                }
                case PlaybackStateCompat.STATE_SKIPPING_TO_NEXT: {
                    //LogHelper.v(TAG, "PlaybackStateCompat.STATE_SKIPPING_TO_NEXT");
                    showPleaseWaitButton(playbackState);
                    break;
                }
                case PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS: {
                    //LogHelper.v(TAG, "PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS");
                    showPleaseWaitButton(playbackState);
                    break;
                }
                case PlaybackStateCompat.STATE_SKIPPING_TO_QUEUE_ITEM: {
                    //LogHelper.v(TAG, "PlaybackStateCompat.STATE_SKIPPING_TO_QUEUE_ITEM");
                    showPleaseWaitButton(playbackState);
                    break;
                }
                case PlaybackStateCompat.STATE_STOPPED: {
                    //LogHelper.v(TAG, "PlaybackStateCompat.STATE_STOPPED");
                    showAutoplayButton(playbackState);
                    break;
                }
            }
        }
        getAutoPlay().invalidate(); // fixes a draw bug in Android
    }

    // the player is currently paused
    private boolean showAutoplayButton(int playbackState) {
        //LogHelper.v(TAG, "showAutoplayButton: playbackState="+playbackState);
        mAutoPlay.setEnabled(true);
        sShowingButton = PLAY;
        if (mCurrentPosition > 0) {
            Drawable resumeButton = ResourcesCompat.getDrawable(getResources(), R.drawable.radio_theater_resume_button_selector, null);
            getAutoPlay().setBackground(resumeButton);
            return true;
        }
        else {
            Drawable autoplayButton = ResourcesCompat.getDrawable(getResources(), R.drawable.radio_theater_autoplay_button_selector, null);
            getAutoPlay().setBackground(autoplayButton);
            return false;
        }
    }

    private boolean showPleaseWaitButton(int playbackState) {
        //LogHelper.v(TAG, "showPleaseWaitButton: playbackState="+playbackState);
        mAutoPlay.setEnabled(false);
        Drawable pleaseWaitButton = ResourcesCompat.getDrawable(getResources(), R.drawable.radio_theater_please_wait_button_selector, null);
        getAutoPlay().setBackground(pleaseWaitButton);
        sShowingButton = PLEASE_WAIT;
        getCircularSeekBar().setVisibility(View.INVISIBLE);
        if (mCurrentPosition > 0) {
            return true;
        }
        else {
            return false;
        }
    }

    // the player is currently playing
    private boolean showPauseButton(int playbackState) {
        //LogHelper.v(TAG, "showPauseButton: playbackState="+playbackState);
        mAutoPlay.setEnabled(true);
        Drawable pauseButton = ResourcesCompat.getDrawable(getResources(), R.drawable.radio_theater_pause_button_selector, null);
        getAutoPlay().setBackground(pauseButton);
        sShowingButton = PAUSE;
        return true;
    }

    //
    // NOTE: there is an Android problem setting these 3 intent flags together with a shared-element transition that destroys the transition effect:
    //autoplayIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    //
    // see: BaseActivity.playNow()
    // to get around this problem, I have over-ridden onBackPressed in AutoplayActivity so that it clears the back stack before exiting.
    //
    @Override
    public void onBackPressed() {
        LogHelper.v(TAG, "onBackPressed - exiting");
        super.onBackPressed();
        exit_now();
    }

    protected void exit_now() {
        Intent intent = new Intent(this, SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("EXIT_NOW", "exit");
        LogHelper.v(TAG, "STARTACTIVITY: SplashActivity.class");
        startActivity(intent);
        finish();
    }

}
