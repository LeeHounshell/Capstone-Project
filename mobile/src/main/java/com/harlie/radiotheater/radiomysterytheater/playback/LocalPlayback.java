/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//
// THIS CODE IS REPURPOSED FROM THE GOOGLE UNIVERSAL-MEDIA-PLAYER SAMPLE
//

package com.harlie.radiotheater.radiomysterytheater.playback;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;

import com.harlie.radiotheater.radiomysterytheater.R;
import com.harlie.radiotheater.radiomysterytheater.RadioControlIntentService;
import com.harlie.radiotheater.radiomysterytheater.RadioTheaterApplication;
import com.harlie.radiotheater.radiomysterytheater.RadioTheaterService;
import com.harlie.radiotheater.radiomysterytheater.model.MusicProvider;
import com.harlie.radiotheater.radiomysterytheater.model.MusicProviderSource;
import com.harlie.radiotheater.radiomysterytheater.utils.LogHelper;
import com.harlie.radiotheater.radiomysterytheater.utils.MediaIDHelper;
import com.harlie.radiotheater.radiomysterytheater.utils.RadioTheaterWidgetProvider;

import java.io.IOException;

import static android.media.MediaPlayer.OnCompletionListener;
import static android.media.MediaPlayer.OnErrorListener;
import static android.media.MediaPlayer.OnPreparedListener;
import static android.media.MediaPlayer.OnSeekCompleteListener;
import static android.support.v4.media.session.MediaSessionCompat.QueueItem;

/**
 * A class that implements local media playback using {@link android.media.MediaPlayer}
 */
public class LocalPlayback
        implements
            Playback,
            AudioManager.OnAudioFocusChangeListener,
            OnCompletionListener,
            OnErrorListener,
            OnPreparedListener,
            OnSeekCompleteListener
{
    private final static String TAG = "LEE: <" + LocalPlayback.class.getSimpleName() + ">";

    // The volume we set the media player to when we lose audio focus, but are
    // allowed to reduce the volume instead of stopping playback.
    public static final float VOLUME_DUCK = 0.2f;
    // The volume we set the media player when we have audio focus.
    public static final float VOLUME_NORMAL = 1.0f;

    // we don't have audio focus, and can't duck (play at a low volume)
    private static final int AUDIO_NO_FOCUS_NO_DUCK = 0;
    // we don't have focus, but can duck (play at a low volume)
    private static final int AUDIO_NO_FOCUS_CAN_DUCK = 1;
    // we have full audio focus
    private static final int AUDIO_FOCUSED  = 2;

    // NOTE: made this static to allow direct communications getting the seek-bar value
    private static LocalPlayback theLocalPlayback;

    private final Context mContext;
    private final WifiManager.WifiLock mWifiLock;
    private boolean mPlayOnFocusGain;
    private Callback mCallback;
    private final MusicProvider mMusicProvider;

    private volatile static boolean sPlaybackEnabled;
    private volatile static boolean sAudioNoisyReceiverRegistered;
    private volatile static boolean sPlaybackRequested;
    private volatile static int sCurrentPosition;
    private volatile static int sCurrentEpisode;
    private volatile static int sCurrentState;
    private volatile static int sCurrentNotifyState;
    private volatile static String sCurrentMediaId;

    // Type of audio focus we have:
    private int mAudioFocus = AUDIO_NO_FOCUS_NO_DUCK;
    private final AudioManager mAudioManager;
    private MediaPlayer mMediaPlayer;

    private final IntentFilter mAudioNoisyIntentFilter =
            new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);

    private final BroadcastReceiver mAudioNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LogHelper.v(TAG, "onReceive");
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                LogHelper.d(TAG, "Headphones disconnected.");
                if (isPlaying()) {
                    Intent i = new Intent(context, RadioTheaterService.class);
                    i.setAction(RadioTheaterService.ACTION_CMD);
                    i.putExtra(RadioTheaterService.CMD_NAME, RadioTheaterService.CMD_PAUSE);
                    mContext.startService(i);
                }
            }
        }
    };

    public LocalPlayback(Context context, MusicProvider musicProvider) {
        LogHelper.v(TAG, "LocalPlayback");
        theLocalPlayback = this;
        this.mContext = context;
        this.mMusicProvider = musicProvider;
        this.mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        // Create the Wifi lock (this does not acquire the lock, this just creates it)
        this.mWifiLock = ((WifiManager) context.getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "uAmp_lock");
        setCurrentState(PlaybackStateCompat.STATE_NONE);
    }

    @Override
    protected void finalize() throws Throwable {
        LogHelper.v(TAG, "finalize");
        //theLocalPlayback = null;
        super.finalize();
    }

    @Override
    public void start() {
        LogHelper.v(TAG, "start");
    }

    @Override
    public void stop(boolean notifyListeners) {
        sPlaybackRequested = false;
        if (getState() != PlaybackStateCompat.STATE_STOPPED) {
            LogHelper.v(TAG, "stop: notifyListeners=" + notifyListeners);
            setCurrentState(PlaybackStateCompat.STATE_STOPPED);
            if (notifyListeners && mCallback != null) {
                mCallback.onPlaybackStatusChanged(getCurrentState());
            }
            setCurrentStreamPosition(getCurrentStreamPosition());
            // Give up Audio focus
            giveUpAudioFocus();
            unregisterAudioNoisyReceiver();
            // Relax all resources
            relaxResources(true);
        }
        else {
            LogHelper.v(TAG, "stop (ignored - already stopped): notifyListeners=" + notifyListeners);
        }
        Context context = RadioTheaterApplication.getRadioTheaterApplicationContext();
        RadioTheaterWidgetProvider.notifyWidget(context, AppWidgetManager.getInstance(context), false);
    }

    @Override
    public void setState(int state) {
        LogHelper.v(TAG, "setState: state="+state);
        setCurrentState(state);
    }

    @Override
    public int getState() {
        LogHelper.v(TAG, "getState");
        return sCurrentState;
    }

    @Override
    public boolean isConnected() {
        LogHelper.v(TAG, "isConnected");
        return true;
    }

    @Override
    public boolean isPlaying() {
        LogHelper.v(TAG, "isPlaying");
        return mPlayOnFocusGain || (mMediaPlayer != null && mMediaPlayer.isPlaying());
    }

    @Override
    public int getCurrentStreamPosition() {
        if (mMediaPlayer != null) {
            sCurrentPosition = mMediaPlayer.getCurrentPosition();
        }
        //LogHelper.v(TAG, "getCurrentStreamPosition: "+sCurrentPosition);
        return sCurrentPosition;
    }

    @Override
    public void updateLastKnownStreamPosition() {
        LogHelper.v(TAG, "updateLastKnownStreamPosition");
        if (mMediaPlayer != null) {
            setCurrentStreamPosition(mMediaPlayer.getCurrentPosition());
        }
    }

    //-------- RADIO THEATER --------
    public static int getCurrentPosition() {
        if (theLocalPlayback != null) {
            sCurrentPosition = theLocalPlayback.getCurrentStreamPosition();
        }
        LogHelper.v(TAG, "getCurrentPosition: "+sCurrentPosition);
        return sCurrentPosition;
    }

    //-------- RADIO THEATER --------
    private void notifyEpisodePlaying() {
        String initialization = RadioTheaterApplication.getRadioTheaterApplicationContext().getResources().getString(R.string.initialization);
        String playing = RadioTheaterApplication.getRadioTheaterApplicationContext().getResources().getString(R.string.playing);
        String message = playing + getCurrentEpisode();
        LogHelper.v(TAG, "notifyEpisodePlaying: message="+message);
        Intent intentMessage = new Intent("android.intent.action.MAIN").putExtra(initialization, message);
        RadioTheaterApplication.getRadioTheaterApplicationContext().sendBroadcast(intentMessage);
    }

    //-------- RADIO THEATER --------
    private void notifyEpisodeDuration() {
        String initialization = RadioTheaterApplication.getRadioTheaterApplicationContext().getResources().getString(R.string.initialization);
        String duration = RadioTheaterApplication.getRadioTheaterApplicationContext().getResources().getString(R.string.duration);
        String message = duration + String.valueOf(mMediaPlayer.getDuration());
        LogHelper.v(TAG, "notifyEpisodeDuration: message="+message);
        Intent intentMessage = new Intent("android.intent.action.MAIN").putExtra(initialization, message);
        RadioTheaterApplication.getRadioTheaterApplicationContext().sendBroadcast(intentMessage);
    }

    //-------- RADIO THEATER --------
    private void notifyEpisodeComplete() {
        String initialization = RadioTheaterApplication.getRadioTheaterApplicationContext().getResources().getString(R.string.initialization);
        String complete = RadioTheaterApplication.getRadioTheaterApplicationContext().getResources().getString(R.string.complete);
        String message = complete + getCurrentEpisode();
        LogHelper.v(TAG, "notifyEpisodeComplete: message="+message);
        Intent intentMessage = new Intent("android.intent.action.MAIN").putExtra(initialization, message);
        RadioTheaterApplication.getRadioTheaterApplicationContext().sendBroadcast(intentMessage);
    }

    //-------- RADIO THEATER --------
    private void notifyIfUnableToPlay() {
        String initialization = RadioTheaterApplication.getRadioTheaterApplicationContext().getResources().getString(R.string.initialization);
        String noplay = RadioTheaterApplication.getRadioTheaterApplicationContext().getResources().getString(R.string.noplay);
        String message = noplay + getCurrentEpisode();
        LogHelper.v(TAG, "notifyIfUnableToPlay: message="+message);
        Intent intentMessage = new Intent("android.intent.action.MAIN").putExtra(initialization, message);
        RadioTheaterApplication.getRadioTheaterApplicationContext().sendBroadcast(intentMessage);
    }

    static public void setCurrentEpisode(int currentEpisode) {
        sCurrentEpisode = currentEpisode;
    }
    static public int getCurrentEpisode() {
        return sCurrentEpisode;
    }

    static void setCurrentState(int currentState) {
        LogHelper.v(TAG, "WIDGET: setCurrentState="+currentState);
        sCurrentState = currentState;
        if (sCurrentNotifyState != sCurrentState) {
            sCurrentNotifyState = sCurrentState;
        }
    }
    static public int getCurrentState() {
        return sCurrentState;
    }

    //-------- RADIO THEATER --------
    @Override
    public void play(QueueItem item) {
        LogHelper.v(TAG, "PLAY: item="+item.getDescription().getMediaId()+", title="+item.getDescription().getTitle());
        sPlaybackRequested = true;
        String mediaId = item.getDescription().getMediaId();
        boolean mediaHasChanged = !TextUtils.equals(mediaId, sCurrentMediaId);
        if (mediaHasChanged) {
            setCurrentStreamPosition(0);
            sCurrentMediaId = mediaId;
            notifyEpisodePlaying();
        }
        if (! sPlaybackEnabled
                || (getCurrentState() == PlaybackStateCompat.STATE_PLAYING && ! mediaHasChanged))
        {
            LogHelper.v(TAG, "PLAY: *** IGNORED 'play' REQUEST *** - mediaId="+mediaId+", enabled="+sPlaybackEnabled+", current state="+getCurrentState());
            Context context = RadioTheaterApplication.getRadioTheaterApplicationContext();
            RadioTheaterWidgetProvider.notifyWidget(context, AppWidgetManager.getInstance(context), false);
            return;
        }

        LogHelper.v(TAG, "PLAY: ---> *** RADIO MYSTERY THEATER: PLAY EPISODE - " + item.getDescription().getTitle() + ", mediaId=" + item.getDescription().getMediaId());
        mPlayOnFocusGain = true;
        tryToGetAudioFocus();
        registerAudioNoisyReceiver();

        if (getCurrentState() == PlaybackStateCompat.STATE_PAUSED && !mediaHasChanged && mMediaPlayer != null) {
            configMediaPlayerState();
        }
        else {
            setCurrentState(PlaybackStateCompat.STATE_STOPPED);
            Context context = RadioTheaterApplication.getRadioTheaterApplicationContext();
            RadioTheaterWidgetProvider.notifyWidget(context, AppWidgetManager.getInstance(context), false);
            relaxResources(false); // release everything except MediaPlayer

            String source = null;
            assert mediaId != null;
            String musicId = MediaIDHelper.extractMusicIDFromMediaID(mediaId);
            boolean error = false;
            if (musicId == null) {
                LogHelper.e(TAG, "*** UNABLE TO GET MUSIC ID *** - mediaId=" + mediaId);
                error = true;
            }
            else {
                MediaMetadataCompat mediaMetadataCompat = mMusicProvider.getMusic(musicId);
                if (mediaMetadataCompat == null) {
                    LogHelper.e(TAG, "*** UNABLE TO GET MEDIA METADATA COMPAT *** - musicId=" + musicId + ", mediaId=" + mediaId);
                    error = true;
                }
                else {
                    MediaMetadataCompat track = MediaMetadataCompat.fromMediaMetadata(mediaMetadataCompat.getMediaMetadata());
                    if (track == null) {
                        LogHelper.e(TAG, "*** UNABLE TO LOAD TRACK *** - musicId=" + musicId + ", mediaId=" + mediaId);
                        error = true;
                    }
                    else {
                        //noinspection ResourceType
                        source = track.getString(MusicProviderSource.CUSTOM_METADATA_TRACK_SOURCE);
                        LogHelper.v(TAG, "===> source=" + source);
                    }
                }
            }
            if (error) {
                LogHelper.v(TAG, "*** MEDIA METADATA NOT FOUND ***");
            }

            if (source != null) {
                LogHelper.v(TAG, "PLAY: source="+source);
                try {
                    createMediaPlayerIfNeeded();

                    setCurrentState(PlaybackStateCompat.STATE_BUFFERING);

                    mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    //Context context = RadioTheaterApplication.getRadioTheaterApplicationContext();
                    //mMediaPlayer.setDataSource(context, Uri.parse(source));
                    mMediaPlayer.setDataSource(source.replaceAll(" ", "%20"));

                    // Starts preparing the media player in the background. When
                    // it's done, it will call our OnPreparedListener (that is,
                    // the onPrepared() method on this class, since we set the
                    // listener to 'this'). Until the media player is prepared,
                    // we *cannot* call start() on it!
                    mMediaPlayer.prepareAsync();

                    // If we are streaming from the internet, we want to hold a
                    // Wifi lock, which prevents the Wifi radio from going to
                    // sleep while the song is playing.
                    mWifiLock.acquire();

                    if (mCallback != null) {
                        mCallback.onPlaybackStatusChanged(getCurrentState());
                    }

                }
                catch (IOException ex) {
                    LogHelper.e(TAG, ex, "Exception playing song");
                    if (mCallback != null) {
                        mCallback.onError(ex.getMessage());
                        notifyIfUnableToPlay();
                    }
                }
            }
            else {
                LogHelper.w(TAG, "*** SOURCE is null ***");
            }
        }
        Context context = RadioTheaterApplication.getRadioTheaterApplicationContext();
        RadioTheaterWidgetProvider.notifyWidget(context, AppWidgetManager.getInstance(context), false);
    }

    @Override
    public void pause() {
        sPlaybackRequested = false;
        if (getState() == PlaybackStateCompat.STATE_PLAYING) {
            LogHelper.v(TAG, "pause - ok, stop playing");
            // Pause media player and cancel the 'foreground service' state.
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                setCurrentStreamPosition(mMediaPlayer.getCurrentPosition());
            }
            // while paused, retain the MediaPlayer but give up audio focus
            relaxResources(false);
            giveUpAudioFocus();
            setCurrentState(PlaybackStateCompat.STATE_PAUSED);
            if (mCallback != null) {
                mCallback.onPlaybackStatusChanged(getCurrentState());
            }
        }
        else {
            LogHelper.v(TAG, "pause - not playing anything");
        }
        unregisterAudioNoisyReceiver();
        Context context = RadioTheaterApplication.getRadioTheaterApplicationContext();
        RadioTheaterWidgetProvider.notifyWidget(context, AppWidgetManager.getInstance(context), false);
    }

    @Override
    public void seekTo(int position) {
        if (getState() != PlaybackStateCompat.ACTION_SEEK_TO && position != sCurrentPosition) {
            LogHelper.v(TAG, "seekTo: position=" + position);

            if (mMediaPlayer == null) {
                // If we do not have a current media player, simply update the current position
                setCurrentStreamPosition(position);
            } else {
                if (mMediaPlayer.isPlaying()) {
                    setCurrentState(PlaybackStateCompat.STATE_BUFFERING);
                }
                mMediaPlayer.seekTo(position);
                if (mCallback != null) {
                    mCallback.onPlaybackStatusChanged(getCurrentState());
                }
            }
        }
        else {
            LogHelper.v(TAG, "seekTo (ignored - already there): position=" + position);
        }
        Context context = RadioTheaterApplication.getRadioTheaterApplicationContext();
        RadioTheaterWidgetProvider.notifyWidget(context, AppWidgetManager.getInstance(context), false);
    }

    @Override
    public void setCallback(Callback callback) {
        LogHelper.v(TAG, "setCallback");
        this.mCallback = callback;
    }

    @Override
    public void setCurrentStreamPosition(int pos) {
        LogHelper.v(TAG, "---------> setCurrentStreamPosition: pos="+pos);
        sCurrentPosition = pos;
    }

    @Override
    public void setCurrentMediaId(String mediaId) {
        LogHelper.v(TAG, "setCurrentMediaId: mediaId="+mediaId);
        sCurrentMediaId = mediaId;
    }

    @Override
    public String getCurrentMediaId() {
        LogHelper.v(TAG, "getCurrentMediaId");
        return sCurrentMediaId;
    }

    public static boolean isPlaybackEnabled() {
        LogHelper.v(TAG, "isPlaybackEnabled: "+sPlaybackEnabled);
        return sPlaybackEnabled;
    }

    public static void setPlaybackEnabled(boolean sPlaybackEnabled) {
        LogHelper.v(TAG, "setPlaybackEnabled: "+sPlaybackEnabled);
        LocalPlayback.sPlaybackEnabled = sPlaybackEnabled;
    }

    /**
     * Try to get the system audio focus.
     */
    private void tryToGetAudioFocus() {
        if (mAudioFocus != AUDIO_FOCUSED) {
            LogHelper.v(TAG, "tryToGetAudioFocus");
            int result = mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN);
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                mAudioFocus = AUDIO_FOCUSED;
            }
        }
    }

    /**
     * Give up the audio focus.
     */
    private void giveUpAudioFocus() {
        if (mAudioFocus == AUDIO_FOCUSED) {
            LogHelper.v(TAG, "giveUpAudioFocus");
            if (mAudioManager.abandonAudioFocus(this) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                mAudioFocus = AUDIO_NO_FOCUS_NO_DUCK;
            }
        }
    }

    /**
     * Reconfigures MediaPlayer according to audio focus settings and
     * starts/restarts it. This method starts/restarts the MediaPlayer
     * respecting the current audio focus state. So if we have focus, it will
     * play normally; if we don't have focus, it will either leave the
     * MediaPlayer paused or set it to a low volume, depending on what is
     * allowed by the current focus settings. This method assumes mPlayer !=
     * null, so if you are calling it, you have to do so from a context where
     * you are sure this is the case.
     */
    private void configMediaPlayerState() {
        LogHelper.v(TAG, "configMediaPlayerState. mAudioFocus=", mAudioFocus);
        if (mAudioFocus == AUDIO_NO_FOCUS_NO_DUCK) {
            // If we don't have audio focus and can't duck, we have to pause,
            if (getCurrentState() == PlaybackStateCompat.STATE_PLAYING) {
                pause();
            }
        } else {  // we have audio focus:
            if (mAudioFocus == AUDIO_NO_FOCUS_CAN_DUCK) {
                mMediaPlayer.setVolume(VOLUME_DUCK, VOLUME_DUCK); // we'll be relatively quiet
            } else {
                if (mMediaPlayer != null) {
                    mMediaPlayer.setVolume(VOLUME_NORMAL, VOLUME_NORMAL); // we can be loud again
                } // else do something for remote client.
            }
            // If we were playing when we lost focus, we need to resume playing.
            if (mPlayOnFocusGain) {
                LogHelper.v(TAG, "*** mPlayOnFocusGain ***");
                startPlaying();
                mPlayOnFocusGain = false;
            }
        }
        if (mCallback != null) {
            mCallback.onPlaybackStatusChanged(getCurrentState());
        }
    }

    private void startPlaying() {
        if (sPlaybackRequested) {
            LogHelper.v(TAG, "startPlaying");
            sPlaybackRequested = false;
            if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
                LogHelper.d(TAG, "startPlaying: configMediaPlayerState startMediaPlayer. seeking to " + sCurrentPosition);
                if (sCurrentPosition == mMediaPlayer.getCurrentPosition()) {
                    LogHelper.v(TAG, "startPlaying: ********* START PLAYING *********");
                    mMediaPlayer.start();
                    setCurrentState(PlaybackStateCompat.STATE_PLAYING);
                } else {
                    LogHelper.v(TAG, "startPlaying: ********* BUFFERING *********");
                    sCurrentPosition = mMediaPlayer.getCurrentPosition();
                    mMediaPlayer.seekTo(sCurrentPosition);
                    setCurrentState(PlaybackStateCompat.STATE_BUFFERING);
                }
            }
        }
        else {
            LogHelper.v(TAG, "*** IGNORED 'startPlaying' REQUEST *** - enabled="+sPlaybackEnabled+", current state="+getCurrentState());
        }
        Context context = RadioTheaterApplication.getRadioTheaterApplicationContext();
        RadioTheaterWidgetProvider.notifyWidget(context, AppWidgetManager.getInstance(context), false);
    }

    /**
     * Called by AudioManager on audio focus changes.
     * Implementation of {@link android.media.AudioManager.OnAudioFocusChangeListener}
     */
    @Override
    public void onAudioFocusChange(int focusChange) {
        LogHelper.v(TAG, "onAudioFocusChange: focusChange=", focusChange);
        if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
            // We have gained focus:
            mAudioFocus = AUDIO_FOCUSED;

        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS ||
                focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ||
                focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
            // We have lost focus. If we can duck (low playback volume), we can keep playing.
            // Otherwise, we need to pause the playback.
            boolean canDuck = focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK;
            mAudioFocus = canDuck ? AUDIO_NO_FOCUS_CAN_DUCK : AUDIO_NO_FOCUS_NO_DUCK;

            // If we are playing, we need to reset media player by calling configMediaPlayerState
            // with mAudioFocus properly set.
            if (getCurrentState() == PlaybackStateCompat.STATE_PLAYING && !canDuck) {
                // If we don't have audio focus and can't duck, we save the information that
                // we were playing, so that we can resume playback once we get the focus back.
                mPlayOnFocusGain = true;
            }
        } else {
            LogHelper.e(TAG, "onAudioFocusChange: Ignoring unsupported focusChange: ", focusChange);
        }
        configMediaPlayerState();
        Context context = RadioTheaterApplication.getRadioTheaterApplicationContext();
        RadioTheaterWidgetProvider.notifyWidget(context, AppWidgetManager.getInstance(context), false);
    }

    /**
     * Called when MediaPlayer has completed a seek
     *
     * @see OnSeekCompleteListener
     */
    @Override
    public void onSeekComplete(MediaPlayer mp) {
        LogHelper.v(TAG, "onSeekComplete: position=", mp.getCurrentPosition());
        setCurrentStreamPosition(mp.getCurrentPosition());
        if (getCurrentState() == PlaybackStateCompat.STATE_BUFFERING) {
            mMediaPlayer.start();
            setCurrentState(PlaybackStateCompat.STATE_PLAYING);
        }
        if (mCallback != null) {
            mCallback.onPlaybackStatusChanged(getCurrentState());
        }
        Context context = RadioTheaterApplication.getRadioTheaterApplicationContext();
        RadioTheaterWidgetProvider.notifyWidget(context, AppWidgetManager.getInstance(context), false);
    }

    /**
     * Called when media player is done playing current episode.
     *
     * @see OnCompletionListener
     */
    @Override
    public void onCompletion(MediaPlayer player) {
        LogHelper.v(TAG, "onCompletion");
        // The media player finished playing the current episode, so we go ahead
        // and start the next.
        if (mCallback != null) {
            mCallback.onCompletion();
        }
        setCurrentStreamPosition(0);
        stop(false); // the next PLAY REQUEST will automatically come from Autoplay
        notifyEpisodeComplete(); // RADIO THEATER Notification
    }

    /**
     * Called when media player is done preparing.
     *
     * @see OnPreparedListener
     */
    @Override
    public void onPrepared(MediaPlayer player) {
        LogHelper.v(TAG, "onPrepared");
        // The media player is done preparing. That means we can start playing if we
        // have audio focus.
        configMediaPlayerState();
        notifyEpisodeDuration(); // RADIO THEATER Notification
    }

    /**
     * Called when there's an error playing media. When this happens, the media
     * player goes to the Error state. We warn the user about the error and
     * reset the media player.
     *
     * @see OnErrorListener
     */
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        LogHelper.e(TAG, "onError: what=" + what + ", extra=" + extra);
        if (what == 1 && extra == -2147483648) {
            // The '1' value corresponds to the constant in MediaPlayer.MEDIA_ERROR_UNKNOWN
            // -2147483648 corresponds to hexadecimal 0x80000000 which is defined as UNKNOWN_ERROR in frameworks/native/include/utils/Errors.h
            LogHelper.e(TAG, "onError: MediaPlayer.MEDIA_ERROR_UNKNOWN - UNKNOWN_ERROR - *** IGNORED ERROR - NO ACTION TAKEN ***");
            return true;
        }
        if (mCallback != null) {
            mCallback.onError("MediaPlayer error " + what + " (" + extra + ")");
        }
        if (what == -38) {
            LogHelper.v(TAG, "*** MEDIA PLAYER NEEDS RESET - ERROR=-38 ***");
        }
        stop(true);
        relaxResources(true);
        // LocalPlayback is stopped now, but still need to notify the app that display buttons should update their state..
        Context context = RadioTheaterApplication.getRadioTheaterApplicationContext();
        RadioControlIntentService.startActionStop(context, "ERROR", String.valueOf(LocalPlayback.getCurrentEpisode()), "error="+String.valueOf(what));
        return true; // true indicates we handled the error
    }

    /**
     * Makes sure the media player exists and has been reset. This will create
     * the media player if needed, or reset the existing media player if one
     * already exists.
     */
    private void createMediaPlayerIfNeeded() {
        LogHelper.v(TAG, "createMediaPlayerIfNeeded. needed?=", (mMediaPlayer==null));
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();

            // Make sure the media player will acquire a wake-lock while
            // playing. If we don't do that, the CPU might go to sleep while the
            // song is playing, causing playback to stop.
            mMediaPlayer.setWakeMode(mContext.getApplicationContext(),
                    PowerManager.PARTIAL_WAKE_LOCK);

            // we want the media player to notify us when it's ready preparing,
            // and when it's done playing:
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.setOnErrorListener(this);
            mMediaPlayer.setOnSeekCompleteListener(this);
        } else {
            mMediaPlayer.reset();
        }
    }

    /**
     * Releases resources used by the service for playback. This includes the
     * "foreground service" status, the wake locks and possibly the MediaPlayer.
     *
     * @param releaseMediaPlayer Indicates whether the Media Player should also
     *            be released or not
     */
    private void relaxResources(boolean releaseMediaPlayer) {
        LogHelper.v(TAG, "relaxResources: releaseMediaPlayer=", releaseMediaPlayer);

        // stop and release the Media Player, if it's available
        if (releaseMediaPlayer && mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

        // we can also release the Wifi lock, if we're holding it
        if (mWifiLock.isHeld()) {
            mWifiLock.release();
        }
    }

    private void registerAudioNoisyReceiver() {
        LogHelper.v(TAG, "registerAudioNoisyReceiver");
        if (!sAudioNoisyReceiverRegistered) {
            mContext.registerReceiver(mAudioNoisyReceiver, mAudioNoisyIntentFilter);
            sAudioNoisyReceiverRegistered = true;
        }
    }

    private void unregisterAudioNoisyReceiver() {
        LogHelper.v(TAG, "unregisterAudioNoisyReceiver");
        if (sAudioNoisyReceiverRegistered) {
            mContext.unregisterReceiver(mAudioNoisyReceiver);
            sAudioNoisyReceiverRegistered = false;
        }
    }
}
