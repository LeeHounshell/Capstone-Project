package com.harlie.radiotheater.radiomysterytheater;

import android.annotation.SuppressLint;
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
import com.harlie.radiotheater.radiomysterytheater.data_helper.DataHelper;
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

    private static boolean isAutoplayActive;

    private static final int DEFAULT_MEDIA_LOADING_DELAY = 13000;
    private static final int DELAY_BEFORE_NEXT_CLICK_ALLOWED = 1000;
    private static final int DELAY_BEFORE_NEXT_VERIFY = (15 * 60 * 60);
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

    public static Handler sRadioTheaterControlHandler = new Handler() {
        public void handleMessage(Message message) {
            Bundle data = message.getData();
        }
    };

    public void doBindService() {
        LogHelper.v(TAG, "doBindService");
        Intent intent = new Intent(this, RadioControlIntentService.class);
        // Create a new Messenger for the communication back
        // From the Service to the Activity
        Messenger messenger = new Messenger(sRadioTheaterControlHandler);
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
                if (!sWaitForMedia) {
                    sWaitForMedia = true;
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        mAppBarLayout.setExpanded(false);
                    }
                    RadioControlIntentService.startActionPlay(autoplayActivity,
                            "MAIN",
                            episodeId,
                            DataHelper.getEpisodeDownloadUrl(),
                            DataHelper.getEpisodeTitle());

                    getHandler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (sWaitForMedia) {
                                sWaitForMedia = false;
                                enableButtons();
                            }
                        }
                    }, DEFAULT_MEDIA_LOADING_DELAY);
                }
            }
        }

        setupAdMob();

        RadioControlIntentService.startActionStart(this,
                "MAIN",
                DataHelper.getEpisodeNumberString(),
                null);

        if (sHandleRotationEvent && DataHelper.isLoadedOK() && !sWaitForMedia) {
            LogHelper.v(TAG, "*** ROTATION EVENT! ***");
            enableButtons();
            displayScrollingText();
        }

        LogHelper.v(TAG, "connect WearTalkService..");
        WearTalkService.connect(RadioTheaterApplication.getInstance().getApplicationContext());
        LogHelper.v(TAG, "notify WearTalkService..");
        WearTalkService.sendRadioDataToWear();

        // ensure the buttons are always available
        getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                enableButtons();
                displayScrollingText();
                showExpectedControls("postDelayed");
            }
        }, 6000);
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
        getCircularSeekBar().setMaxProgress((int) DataHelper.getDuration());
        getCircularSeekBar().setProgress(0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getCircularSeekBar().setBackgroundColor(getResources().getColor(R.color.transparent, null));
        }
        else {
            //noinspection deprecation
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
                RadioControlIntentService.startActionSeek(autoplayActivity,
                        "MAIN",
                        DataHelper.getEpisodeNumberString(),
                        String.valueOf(newProgress));
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
        //noinspection BooleanMethodIsAlwaysInverted
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
            public void onClick() { // Autoplay Button
                ButtonState oldButtonState = sShowingButton;
                if (!isTimePassed()) {
                    displayScrollingText();
                    return;
                }
                LogHelper.v(TAG, "onClick");
                //mp.start();
                verifyPaidVersion(true);
                if (DataHelper.getEpisodeNumber() != 0) {
                    LogHelper.v(TAG, "onClick: prepare episode=" + DataHelper.getEpisodeNumber());
                }
                else {
                    LogHelper.v(TAG, "onClick: need to getCursorForNextAvailableEpisode");
                    ConfigEpisodesCursor configCursor = DataHelper.getCursorForNextAvailableEpisode();
                    if (!DataHelper.getEpisodeDataForCursor(configCursor)) {
                        LogHelper.v(TAG, "popup alert - ALL EPISODES ARE HEARD!");
                        mAppBarLayout.setExpanded(true);
                        heardAllEpisodes();
                        return;
                    }
                }
                displayScrollingText();
                DataHelper.getEpisodeInfoFor(DataHelper.getEpisodeNumber());
                if (oldButtonState == PLAY) {
                    LogHelper.v(TAG, "START PLAYING..");
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        mAppBarLayout.setExpanded(false);
                    }
//                    if (mCurrentPosition == 0) {
//                        getHandler().post(new Runnable() {
//                            @Override
//                            public void run() {
//                                LogHelper.v(TAG, "*** SHOW LOADING SCREEN ***");
//                                sLoadingScreenEnabled = true;
//                                loadingScreen();
//                            }
//                        });
//                        return;
//                    }
                    LogHelper.v(TAG, "*** REQUEST PLAYBACK FOR EPISODE " + DataHelper.getEpisodeNumber() + " ***");
                    RadioControlIntentService.startActionPlay(autoplayActivity,
                            "MAIN",
                            DataHelper.getEpisodeNumberString(),
                            DataHelper.getEpisodeDownloadUrl(),
                            DataHelper.getEpisodeTitle());
                }
                else if (oldButtonState == PAUSE) {
                    LogHelper.v(TAG, "PAUSE PLAYBACK..");
                    RadioControlIntentService.startActionPause(autoplayActivity,
                            "MAIN",
                            DataHelper.getEpisodeNumberString(),
                            DataHelper.getEpisodeDownloadUrl());
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
                RadioControlIntentService.startActionGoback(autoplayActivity,
                        "MAIN",
                        DataHelper.getEpisodeNumberString(),
                        String.valueOf((THIRTY_SECONDS * 2)));
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
                    RadioControlIntentService.startActionStop(autoplayActivity,
                            "MAIN",
                            DataHelper.getEpisodeNumberString(),
                            DataHelper.getEpisodeDownloadUrl());
                }
                getHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        LogHelper.v(TAG, "*** disable circular scrollbar ***");
                        DataHelper.setCurrentPosition(0);
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
                RadioControlIntentService.startActionNext(autoplayActivity,
                        "MAIN",
                        DataHelper.getEpisodeNumberString(),
                        DataHelper.getEpisodeDownloadUrl());

                DataHelper.markEpisodeAsHeardAndIncrementPlayCount(DataHelper.getEpisodeNumber(), DataHelper.getEpisodeNumberString(), DataHelper.getCurrentPosition());
            }

            @Override
            public void onSwipeLeft() {
                displayScrollingText();
                if (! isTimePassed()) {
                    return;
                }
                //mp.start();
                LogHelper.v(TAG, "onSwipeLeft");
                RadioControlIntentService.startActionPrev(autoplayActivity,
                        "MAIN",
                        DataHelper.getEpisodeNumberString(),
                        DataHelper.getEpisodeDownloadUrl());

                DataHelper.markEpisodeAs_NOT_Heard(DataHelper.getEpisodeNumber(), DataHelper.getEpisodeNumberString(), 0);
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
        getFabActionButton().setEnabled(false);
        getFabActionButton().setVisibility(View.INVISIBLE);

        final AutoplayActivity activity = this;
        getFabActionButton().setOnTouchListener(new OnSwipeTouchListener(this, getHandler(), getFabActionButton()) {

            @SuppressLint("PrivateResource")
            @Override
            public void onClick() { // FloatingActiionButton
                int lastPlaybackState = LocalPlayback.getCurrentState();
                DataHelper.getEpisodeInfoFor(DataHelper.getEpisodeNumber());
                if (PlaybackStateCompat.STATE_PLAYING != lastPlaybackState) {
                    ConfigEpisodesCursor configCursor = DataHelper.getCursorForNextAvailableEpisode();
                    if (DataHelper.getEpisodeDataForCursor(configCursor)) {
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
        if (! DataHelper.isPurchased() && ! DataHelper.isNoAdsForShow()) {
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

    @Override
    public void enableButtons() {
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
                        DataHelper.trackWithFirebaseAnalytics("ALL-EPISODES", 0, "EVERYTHING HEARD");
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    private void problemLoadingMetadata() {
        LogHelper.v(TAG, "problemLoadingMetadata");
        DataHelper.setCurrentPosition(0);
        AlertDialog alertDialog = new AlertDialog.Builder(AutoplayActivity.this).create();
        alertDialog.setTitle(getResources().getString(R.string.no_metadata));
        alertDialog.setMessage(getResources().getString(R.string.metadata_loading_problem));
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        DataHelper.trackWithFirebaseAnalytics(DataHelper.getEpisodeNumberString(), DataHelper.getCurrentPosition(), "load metadata failed");
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    private void problemWithPlayback() {
        LogHelper.v(TAG, "problemWithPlayback");
        DataHelper.setCurrentPosition(0);
        AlertDialog alertDialog = new AlertDialog.Builder(AutoplayActivity.this).create();
        alertDialog.setTitle(getResources().getString(R.string.unable_to_load));
        alertDialog.setMessage(getResources().getString(R.string.playback_problem));
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        DataHelper.trackWithFirebaseAnalytics(DataHelper.getEpisodeNumberString(), DataHelper.getCurrentPosition(), "playback failed");
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    protected void displayScrollingText() {
        LogHelper.v(TAG, "displayScrollingText");
        if (DataHelper.isLoadedOK()) {
            getHandler().post(new Runnable() {
                @Override
                public void run() {
                    ScrollingTextView horizontalScrollingText = getHorizontalScrollingText();
                    if (horizontalScrollingText != null
                            && DataHelper.getAirdate() != null
                            && DataHelper.getEpisodeTitle() != null
                            && DataHelper.getEpisodeDescription() != null)
                    {
                        setHorizontalScrollingText(horizontalScrollingText);
                        horizontalScrollingText.setVisibility(View.VISIBLE);
                    }
                    else {
                        // error state recovery
                        DataHelper.getEpisodeInfoFor(DataHelper.getEpisodeNumber());
                        if (horizontalScrollingText != null
                                && DataHelper.getAirdate() != null
                                && DataHelper.getEpisodeTitle() != null
                                && DataHelper.getEpisodeDescription() != null)
                        {
                            setHorizontalScrollingText(horizontalScrollingText);
                            horizontalScrollingText.setVisibility(View.VISIBLE);
                        }
                        else {
                            LogHelper.v(TAG, "UNABLE TO DISPLAY SCROLLING TEXT: episode="+ DataHelper.getEpisodeNumber()
                                    +", airdate="+ DataHelper.getAirdate()
                                    +", title="+ DataHelper.getEpisodeTitle()
                                    +", description="+ DataHelper.getEpisodeDescription());
                        }
                    }
                }

                protected void setHorizontalScrollingText(ScrollingTextView horizontalScrollingText) {
                    String remain = "";

                    //#IFDEF 'TRIAL'
                    if (DataHelper.isConfigurationLoaded()) {
                        int count = DataHelper.MAX_TRIAL_EPISODES - DataHelper.getAllListenCount();
                        if (count > 0) {
                            remain = String.valueOf(count) + " " + getResources().getString(R.string.trial_remain);
                        }
                        else {
                            remain = getResources().getString(R.string.trial_complete);
                        }
                    }
                    //#ENDIF

                    horizontalScrollingText.setText("         "+remain+" ... Airdate: " + RadioTheaterContract.airDate(DataHelper.getAirdate())
                            + " ... Episode #" + DataHelper.getEpisodeNumber() + " ... " + DataHelper.getEpisodeTitle() + " ... " + DataHelper.getEpisodeDescription());
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

    @SuppressLint("PrivateResource")
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
                long episode = DataHelper.getEpisodeNumber();
                LogHelper.v(TAG, "-> SHARE <-");
                if (episode == 0) {
                    ConfigEpisodesCursor configCursor = DataHelper.getCursorForNextAvailableEpisode();
                    DataHelper.getEpisodeDataForCursor(configCursor);
                    episode = DataHelper.getEpisodeNumber();
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
                DataHelper.trackSettingsWithFirebaseAnalytics();
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
                DataHelper.trackAboutWithFirebaseAnalytics();
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
            // only set this for API 21+
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
                    DataHelper.setCurrentPosition((long) LocalPlayback.getCurrentPosition());
                    getCircularSeekBar().setProgress((int) DataHelper.getCurrentPosition());
                    if (sShowingButton != PLEASE_WAIT) {
                        if (DataHelper.getCurrentPosition() > 0) {
                            LogHelper.v(TAG, "...updateCircularSeekbar (visible)... " + DataHelper.getCurrentPosition());
                            getCircularSeekBar().setVisibility(View.VISIBLE);
                        } else {
                            LogHelper.v(TAG, "...updateCircularSeekbar (invisible)... " + DataHelper.getCurrentPosition());
                            getCircularSeekBar().setVisibility(View.INVISIBLE);
                        }
                    }
                    // NOTE: a performance impact exists with the verifyPaidVersion operation, so every seekbar update is too much
                    long now = System.currentTimeMillis();
                    if (DataHelper.isLoadedOK() && (now - lastVerifyTime) > DELAY_BEFORE_NEXT_VERIFY) {
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
            DataHelper.setDuration((int) duration);
        }
        LogHelper.v(TAG, "===> EPISODE DURATION="+ DataHelper.getDuration()/1000+", sHaveRealDuration="+ sHaveRealDuration);
        getCircularSeekBar().setMaxProgress((int) DataHelper.getDuration());
        showExpectedControls("updateDuration");
    }

    //--------------------------------------------------------------------------------
    // LOADING SCREEN
    //--------------------------------------------------------------------------------

//    protected void loadingScreen() {
//        LogHelper.d(TAG, "loadingScreen: enabled="+ sLoadingScreenEnabled);
//        if (sLoadingScreenEnabled && !LoadingAsyncTask.sLoadingNow) {
//            sBeginLoading = true;
//            LogHelper.v(TAG, "*** -------------------------------------------------------------------------------- ***");
//            sProgressViewSpinning = true;
//            LoadingAsyncTask asyncTask = new LoadingAsyncTask(this, mCircleView, getCircularSeekBar());
//            asyncTask.execute();
//            LogHelper.v(TAG, "*** -------------------------------------------------------------------------------- ***");
//        }
//    }

//    @Override
//    public void setCircleViewValue(float value) {
//        LogHelper.v(TAG, "setCircleViewValue: "+value);
//        super.setCircleViewValue(value);
//        if (value != 0) {
//            sBeginLoading = false;
//        }
//    }

//    protected void stopLoadingScreen() {
//        LogHelper.v(TAG, "*** STOP LOADING SCREEN ***");
//        sLoadingScreenEnabled = false;
//        sProgressViewSpinning = false;
//        LoadingAsyncTask.sDoneLoading = true;
//    }

    public void initiateLoadingTask(AutoplayActivity autoplayActivity) {
        LogHelper.v(TAG, "initiateLoadingTask: *** REQUEST PLAYBACK FOR EPISODE " + DataHelper.getEpisodeNumber() + " ***");
        RadioControlIntentService.startActionPlay(autoplayActivity,
                "MAIN",
                DataHelper.getEpisodeNumberString(),
                DataHelper.getEpisodeDownloadUrl(),
                DataHelper.getEpisodeTitle());
    }

    //--------------------------------------------------------------------------------

    @Override
    protected void onPause() {
        LogHelper.d(TAG, "onPause");
//        if (mRadioControlIntentServiceBinder != null) {
//            unbindService(radioTheaterControlConnection);
//            mRadioControlIntentServiceBinder = null;
//        }
        super.onPause();
        isAutoplayActive = false;
        this.unregisterReceiver(getReceiver());
    }

    @Override
    protected void onResume() {
        LogHelper.d(TAG, "onResume");
//        if (mRadioControlIntentServiceBinder == null) {
//            doBindService();
//        }
        super.onResume();
        if (DataHelper.isLoadedOK()) {
            enableButtons();
        }
        IntentFilter intentFilter = new IntentFilter("android.intent.action.MAIN");
        handleNotifyNewPlaybackState();
        this.registerReceiver(getReceiver(), intentFilter);
        isAutoplayActive = true;
    }

    private void setAppBarExpanded(boolean expanded, boolean override) {
        if (override || getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mAppBarLayout.setExpanded(expanded);
        }
        else {
            mAppBarLayout.setExpanded(true);
        }
    }

    protected void startPlaybackForEpisode(final long nextEpisode, final AutoplayActivity autoplayActivity) {
        LogHelper.v(TAG, "startPlaybackForEpisode: episode="+ DataHelper.getEpisodeNumber());
        getHandler().post(new Runnable() {
            @Override
            public void run() {
                initializeForEpisode();
                DataHelper.setEpisodeNumber(nextEpisode);
                DataHelper.getEpisodeInfoFor(DataHelper.getEpisodeNumber());
                showPleaseWaitButton(0);
                displayScrollingText();
                verifyPaidVersion(true);
                RadioControlIntentService.startActionPlay(autoplayActivity,
                        "MAIN",
                        DataHelper.getEpisodeNumberString(),
                        DataHelper.getEpisodeDownloadUrl(),
                        DataHelper.getEpisodeTitle());
            }
        });
    }

    private void initializeForEpisode() {
        sHaveRealDuration = false;
        DataHelper.setDuration(DEFAULT_DURATION); // fifty-minutes in ms
        DataHelper.setCurrentPosition(0);
        DataHelper.setEpisodeDownloadUrl(null);
        DataHelper.setEpisodeTitle(null);
        DataHelper.setEpisodeDescription(null);
    }

    public boolean verifyPaidVersion(final boolean handleClick) {
        LogHelper.v(TAG, "verifyPaidVersion");
        //#IFDEF 'TRIAL'
        final AutoplayActivity autoplayActivity = this;
        if (! DataHelper.isPurchased() && ! DataHelper.isTrial()) {
            RadioControlIntentService.startActionStop(autoplayActivity,
                    "MAIN",
                    DataHelper.getEpisodeNumberString(),
                    DataHelper.getEpisodeDownloadUrl());
            getHandler().post(new Runnable() {
                @Override
                public void run() {
                    LogHelper.v(TAG, "*** verifyPaidVersion - TRIAL EXPIRED ***");
                    maxTrialEpisodesAreReached();
                    RadioTheaterWidgetService.setPaidVersion(autoplayActivity, false);
                    if (handleClick) {
                        displayScrollingText();
                        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                            mAppBarLayout.setExpanded(false);
                        }
                        RadioControlIntentService.startActionPlay(autoplayActivity,
                                "MAIN",
                                DataHelper.getEpisodeNumberString(),
                                DataHelper.getEpisodeDownloadUrl(),
                                DataHelper.getEpisodeTitle());
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
        if (DataHelper.isLoadedOK() && !sWaitForMedia) {
            displayScrollingText();
            enableButtons();
            showExpectedControls("onStart");
        }
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

    public static boolean isAutoplayActive() {
        return isAutoplayActive;
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
            if (DataHelper.getCurrentPosition() > 0) {
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
                    RadioControlIntentService.startActionStop(this,
                            "RECOVER",
                            DataHelper.getEpisodeNumberString(),
                            DataHelper.getEpisodeDownloadUrl());
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
        if (DataHelper.getCurrentPosition() > 0) {
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
        return DataHelper.getCurrentPosition() > 0;
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

    //================================================================================
    // THE PRIMARY MESSAGE RECEIVER AND HANDLING PROCESSOR
    //================================================================================

    protected void handleNotifyNewPlaybackState() {
        LogHelper.v(TAG, "handleNotifyNewPlaybackState");
        final AutoplayActivity autoplayActivity = this;
        mRadioReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, final Intent intent) {
                String radio_control_command = RadioTheaterApplication.getRadioTheaterApplicationContext().getResources().getString(R.string.radio_control_command);
                final String message = intent.getStringExtra(radio_control_command); // get any control messages, for example: did metadata load ok? or play:id etc..
                LogHelper.v(TAG, "*** RECEIVED BROADCAST CONTROL: "+message);

                if (message == null) {
                    LogHelper.e(TAG, "*** *** *** FAILED TO RECEIVE MESSAGE! - CRITICAL ERROR");
                    return;
                }

                final String KEY_LOAD_OK = getResources().getString(R.string.metadata_loaded);
                final String KEY_LOAD_FAIL = getResources().getString(R.string.error_no_metadata);
                final String KEY_UPDATE_SCROLLTEXT = getResources().getString(R.string.update_scroll_text);
                final String KEY_UPDATE_BUTTONS = getResources().getString(R.string.update_buttons);
                final String KEY_DURATION = getResources().getString(R.string.duration);
                final String KEY_ERROR = getResources().getString(R.string.error);
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
                            LogHelper.v(TAG, "*** RECEIVED BROADCAST: LOAD_OK - episode="+ DataHelper.getEpisodeNumber());
                            LogHelper.v(TAG, "LOAD_OK: "+ DataHelper.getEpisodeNumber()+" "+ DataHelper.getEpisodeTitle());
                            //stopLoadingScreen();
                            sWaitForMedia = false;
                            displayScrollingText();
                            enableButtons();
                            if (sHandleRotationEvent) {
                                LogHelper.v(TAG, "*** ROTATION EVENT - NEED TO RELOAD EPISODE INFO FOR "+ DataHelper.getEpisodeNumber());
                                DataHelper.getEpisodeInfoFor(DataHelper.getEpisodeNumber());
                            }
                            DataHelper.setLoadedOK(true); // after 'displayScrollingText' because i don't want to see Episode detail until after the first Autoplay click
                            sWaitForMedia = false;
                            DataHelper.checkUpdateWidget(autoplayActivity, DataHelper.getAllListenCount());
                        }
                    });
                    WearTalkService.sendRadioDataToWear();
                }

                // LOAD_FAIL
                else if (message.equals(KEY_LOAD_FAIL)) {
                    LogHelper.v(TAG, KEY_LOAD_FAIL);
                    getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            LogHelper.v(TAG, "*** RECEIVED BROADCAST: LOAD_FAIL");
                            //stopLoadingScreen();
                            sWaitForMedia = false;
                            problemLoadingMetadata();
                            showExpectedControls("LOAD_FAIL");
                        }
                    });
                    WearTalkService.sendRadioDataToWear();
                }

                // UPDATE_SCROLLTEXT
                else if (message.equals(KEY_UPDATE_SCROLLTEXT)) {
                    LogHelper.v(TAG, KEY_UPDATE_SCROLLTEXT);
                    getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            LogHelper.v(TAG, "*** RECEIVED BROADCAST: UPDATE_SCROLLTEXT: "+intent.toString());
                            displayScrollingText();
                        }
                    });
                    WearTalkService.sendRadioDataToWear();
                }

                // UPDATE_BUTTONS
                else if (message.equals(KEY_UPDATE_BUTTONS)) {
                    LogHelper.v(TAG, KEY_UPDATE_BUTTONS);
                    getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            LogHelper.v(TAG, "*** RECEIVED BROADCAST: UPDATE_BUTTONS: "+intent.toString());
                            showExpectedControls("UPDATE_BUTTONS");
                        }
                    });
                    WearTalkService.sendRadioDataToWear();
                }

                // DURATION
                else if (message.length() > KEY_DURATION.length() && message.substring(0, KEY_DURATION.length()).equals(KEY_DURATION)) {
                    LogHelper.v(TAG, KEY_DURATION);
                    getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            LogHelper.v(TAG, "*** RECEIVED BROADCAST: EPISODE DURATION");
                            //stopLoadingScreen();
                            DataHelper.setLoadedOK(true);
                            showExpectedControls("DURATION");
                            sWaitForMedia = false;
                            displayScrollingText();
                            enableButtons();
                            DataHelper.setDuration(Long.valueOf(message.substring(KEY_DURATION.length(), message.length())));
                            sHaveRealDuration = true;
                            getCircularSeekBar().setMaxProgress((int) DataHelper.getDuration());
                            LogHelper.v(TAG, "*** REVISED EPISODE DURATION="+ DataHelper.getDuration());
                            updateDuration(DataHelper.getDuration());
                            do_UpdateControls();
                        }
                    });
                    WearTalkService.sendRadioDataToWear();
                }

                // ERROR
                else if (message.equals(KEY_ERROR)) {
                    LogHelper.v(TAG, KEY_ERROR);
                    getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            LogHelper.v(TAG, "*** RECEIVED BROADCAST: PLAYBACK ERROR!");
                            //stopLoadingScreen();
                            sWaitForMedia = false;
                            setAppBarExpanded(true, false);
                            showExpectedControls("PLAYBACK ERROR");
                            problemWithPlayback();
                        }
                    });
                    WearTalkService.sendRadioDataToWear();
                }

                // REQUEST
                else if (message.length() > KEY_REQUEST.length() && message.substring(0, KEY_REQUEST.length()).equals(KEY_REQUEST)) {
                    final String episodeIndex = message.substring(KEY_REQUEST.length(), message.length());
                    LogHelper.v(TAG,  "*** RECEIVED BROADCAST: REQUEST TO PLAY EPISODE "+episodeIndex);
                    getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                                setAppBarExpanded(false, false);
                            }
                            DataHelper.setLoadedOK(true);
                            long episode = Long.valueOf(episodeIndex);
                            DataHelper.setEpisodeNumber(episode);
                            DataHelper.getEpisodeInfoFor(episode);
                            displayScrollingText();
                            showExpectedControls("REQUEST");
                            RadioControlIntentService.startActionPlay(autoplayActivity,
                                    "MAIN",
                                    DataHelper.getEpisodeNumberString(),
                                    DataHelper.getEpisodeDownloadUrl(),
                                    DataHelper.getEpisodeTitle());
                        }
                    });
                    WearTalkService.sendRadioDataToWear();
                }

                // PLAYING
                else if (message.length() > KEY_PLAYING.length() && message.substring(0, KEY_PLAYING.length()).equals(KEY_PLAYING)) {
                    final String episodeIndex = message.substring(KEY_PLAYING.length(), message.length());
                    LogHelper.v(TAG,  "*** RECEIVED BROADCAST: NOW PLAYING EPISODE "+episodeIndex);
                    getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            // NEXT and PREV need to show the new Episode info
                            //stopLoadingScreen();
                            long episode = Long.valueOf(episodeIndex);
                            DataHelper.setEpisodeNumber(episode);
                            DataHelper.getEpisodeInfoFor(episode);
                            LogHelper.v(TAG, "PLAYING: title="+ DataHelper.getEpisodeTitle()+", episode="+episode);
                            DataHelper.setLoadedOK(true);
                            sWaitForMedia = false;
                            displayScrollingText();
                            enableButtons();
                            showPauseButton(0);
                            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                                setAppBarExpanded(true, true);
                            }
                            else {
                                setAppBarExpanded(false, true);
                            }
                        }
                    });
                    WearTalkService.sendRadioDataToWear();
                }

                // NOPLAY
                else if (message.length() > KEY_NOPLAY.length() && message.substring(0, KEY_NOPLAY.length()).equals(KEY_NOPLAY)) {
                    String episodeIndex = message.substring(KEY_NOPLAY.length(), message.length());
                    LogHelper.v(TAG,  "*** RECEIVED BROADCAST: UNABLE TO PLAY EPISODE "+episodeIndex);
                    getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            LogHelper.v(TAG, "*** RECEIVED BROADCAST: NOT ABLE TO PLAY");
                            //stopLoadingScreen();
                            sWaitForMedia = false;
                            setAppBarExpanded(true, false);
                            problemWithPlayback();
                        }
                    });
                    WearTalkService.sendRadioDataToWear();
                }

                // COMPLETION
                else if (message.length() > KEY_COMPLETION.length() && message.substring(0, KEY_COMPLETION.length()).equals(KEY_COMPLETION)) {
                    String episodeIndex = message.substring(KEY_COMPLETION.length(), message.length());
                    LogHelper.v(TAG,  "*** RECEIVED BROADCAST: COMPLETED PLAY EPISODE "+episodeIndex);
                    try {
                        long episode = Long.parseLong(episodeIndex);
                        DataHelper.markEpisodeAsHeardAndIncrementPlayCount(episode, episodeIndex, DataHelper.getDuration());
                    }
                    catch (NumberFormatException e) {
                        LogHelper.e(TAG, "unable to markEpisodeAsHeardAndIncrementPlayCount: decode episodeIndex="+episodeIndex);
                    }
                    initializeForEpisode();
                    ConfigEpisodesCursor configCursor = DataHelper.getCursorForNextAvailableEpisode();
                    if (! DataHelper.getEpisodeDataForCursor(configCursor)) {
                        getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                LogHelper.v(TAG, "popup alert - ALL EPISODES ARE HEARD!");
                                if (! EpisodeListActivity.isTwoPane() || getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                                    setAppBarExpanded(true, false);
                                }
                                sWaitForMedia = false;
                                heardAllEpisodes();
                            }
                        });
                    }
                    else {
                        final long nextEpisode = DataHelper.getEpisodeNumber();
                        LogHelper.v(TAG, "=========> START AUTOPLAY FOR NEXT AVAILABLE EPISODE: "+nextEpisode);
                        sWaitForMedia = true;
                        showPleaseWaitButton(0);
                        startPlaybackForEpisode(nextEpisode, autoplayActivity);

                        getHandler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (sWaitForMedia) {
                                    sWaitForMedia = false;
                                    enableButtons();
                                }
                            }
                        }, DEFAULT_MEDIA_LOADING_DELAY);
                    }
                    WearTalkService.sendRadioDataToWear();
                }

                // UNKNOWN
                else {
                    LogHelper.v(TAG, "*** UNKNOWN MESSAGE VIA INTENT: "+message);
                    sWaitForMedia = false;
                    getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            //stopLoadingScreen();
                            sWaitForMedia = false;
                            enableButtons();
                            showExpectedControls("UNKNOWN");
                        }
                    });
                }
            }
        };
    }

}
