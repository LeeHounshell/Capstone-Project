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

import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.harlie.radiotheater.radiomysterytheater.R;
import com.harlie.radiotheater.radiomysterytheater.model.MusicProvider;
import com.harlie.radiotheater.radiomysterytheater.utils.LogHelper;
import com.harlie.radiotheater.radiomysterytheater.utils.MediaIDHelper;
import com.harlie.radiotheater.radiomysterytheater.utils.WearHelper;

/**
 * Manage the interactions among the container service, the queue manager and the actual playback.
 */
public class PlaybackManager implements Playback.Callback {
    private final static String TAG = "LEE: <" + PlaybackManager.class.getSimpleName() + ">";

    // Action to thumbs up a media item
    private static final String CUSTOM_ACTION_THUMBS_UP = "com.harlie.radiotheater.radiomysterytheater.THUMBS_UP";

    public static QueueManager getQueueManager() {
        return sQueueManager;
    }
    private static QueueManager sQueueManager;

    private MusicProvider mMusicProvider;
    private Resources mResources;
    private Playback mPlayback;
    private PlaybackServiceCallback mServiceCallback;
    private MediaSessionCallback mMediaSessionCallback;

    public PlaybackManager(PlaybackServiceCallback serviceCallback, Resources resources,
                           MusicProvider musicProvider, QueueManager queueManager,
                           Playback playback)
    {
        LogHelper.v(TAG, "PlaybackManager");
        sQueueManager = queueManager;
        mMusicProvider = musicProvider;
        mServiceCallback = serviceCallback;
        mResources = resources;
        mMediaSessionCallback = new MediaSessionCallback();
        mPlayback = playback;
        mPlayback.setCallback(this);
    }

    public Playback getPlayback() {
        LogHelper.v(TAG, "getPlayback");
        return mPlayback;
    }

    public MediaSessionCompat.Callback getMediaSessionCallback() {
        LogHelper.v(TAG, "getMediaSessionCallback");
        return mMediaSessionCallback;
    }

    //-------- RADIO THEATER --------
    /**
     * Handle a request to play music
     */
    public void handlePlayRequest() {
        LogHelper.v(TAG, "handlePlayRequest: mState=" + mPlayback.getState());
        MediaSessionCompat.QueueItem currentMusic = getQueueManager().getCurrentMusic();
        if (currentMusic != null) {
            mServiceCallback.onPlaybackStart();
            mPlayback.play(currentMusic);
        }
    }

    //-------- RADIO THEATER --------
    /**
     * Handle a request to pause music
     */
    public void handlePauseRequest() {
        LogHelper.v(TAG, "handlePauseRequest: mState=" + mPlayback.getState());
        if (mPlayback.isPlaying()) {
            mPlayback.pause();
            mServiceCallback.onPlaybackStop();
        }
    }

    //-------- RADIO THEATER --------
    /**
     * Handle a request to seek to a specified position and continue playback
     */
    public void handleSeekRequest(int position) {
        LogHelper.v(TAG, "handleSeekRequest: position=" + position);
        mPlayback.seekTo(position);
        LogHelper.v(TAG, "handleSeekRequest: need to resume playback");
        MediaSessionCompat.QueueItem currentMusic = getQueueManager().getCurrentMusic();
        if (currentMusic != null) {
            mPlayback.play(currentMusic);
        }
    }

    //-------- RADIO THEATER --------
    /**
     * Handle a request to stop music
     *
     * @param withError Error message in case the stop has an unexpected cause. The error
     *                  message will be set in the PlaybackState and will be visible to
     *                  MediaController clients.
     */
    public void handleStopRequest(String withError) {
        LogHelper.v(TAG, "handleStopRequest: mState=" + mPlayback.getState() + ((withError != null) ? (" error="+withError) : ""));
        mPlayback.stop(true);
        mServiceCallback.onPlaybackStop();
        updatePlaybackState(withError);
    }

    /**
     * Update the current media player state, optionally showing an error message.
     *
     * @param withError if not null, error message to present to the user.
     */
    public void updatePlaybackState(String withError) {
        LogHelper.v(TAG, "updatePlaybackState: playback state=" + mPlayback.getState() + ((withError != null) ? (" error="+withError) : ""));
        long position = PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN;
        if (mPlayback != null && mPlayback.isConnected()) {
            position = mPlayback.getCurrentStreamPosition();
        }

        //noinspection ResourceType
        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(getAvailableActions());

        setCustomAction(stateBuilder);
        int state = mPlayback.getState();

        // If there is an error message, send it to the playback state:
        if (withError != null) {
            // Error states are really only supposed to be used for errors that cause playback to
            // stop unexpectedly and persist until the user takes action to fix it.
            stateBuilder.setErrorMessage(withError);
            state = PlaybackStateCompat.STATE_ERROR;
        }
        //noinspection ResourceType
        stateBuilder.setState(state, position, 1.0f, SystemClock.elapsedRealtime());

        // Set the activeQueueItemId if the current index is valid.
        MediaSessionCompat.QueueItem currentMusic = getQueueManager().getCurrentMusic();
        if (currentMusic != null) {
            stateBuilder.setActiveQueueItemId(currentMusic.getQueueId());
        }

        mServiceCallback.onPlaybackStateUpdated(stateBuilder.build());

        if (state == PlaybackStateCompat.STATE_PLAYING ||
                state == PlaybackStateCompat.STATE_PAUSED) {
            mServiceCallback.onNotificationRequired();
        }
    }

    private void setCustomAction(PlaybackStateCompat.Builder stateBuilder) {
        LogHelper.v(TAG, "setCustomAction");
        MediaSessionCompat.QueueItem currentMusic = getQueueManager().getCurrentMusic();
        if (currentMusic == null) {
            return;
        }
        // Set appropriate "Favorite" icon on Custom action:
        String mediaId = currentMusic.getDescription().getMediaId();
        if (mediaId == null) {
            return;
        }
        String musicId = MediaIDHelper.extractMusicIDFromMediaID(mediaId);
        int favoriteIcon = mMusicProvider.isFavorite(musicId) ?
                R.drawable.ic_star_on : R.drawable.ic_star_off;
        LogHelper.d(TAG, "updatePlaybackState, setting Favorite custom action of music ",
                musicId, " current favorite=", mMusicProvider.isFavorite(musicId));
        Bundle customActionExtras = new Bundle();
        WearHelper.setShowCustomActionOnWear(customActionExtras, true);
        stateBuilder.addCustomAction(new PlaybackStateCompat.CustomAction.Builder(
                CUSTOM_ACTION_THUMBS_UP, mResources.getString(R.string.favorite), favoriteIcon)
                .setExtras(customActionExtras)
                .build());
    }

    private long getAvailableActions() {
        LogHelper.v(TAG, "getAvailableActions");
        long actions =
                PlaybackStateCompat.ACTION_PLAY |
                PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID |
                PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH |
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT;
        if (mPlayback.isPlaying()) {
            actions |= PlaybackStateCompat.ACTION_PAUSE;
        }
        return actions;
    }

    /**
     * Implementation of the Playback.Callback interface
     */
    @Override
    public void onCompletion() {
        LogHelper.v(TAG, "onCompletion");
        // The media player finished playing the current song, so we go ahead
        // and start the next.
        if (getQueueManager().skipQueuePosition(1)) {
            handlePlayRequest();
            getQueueManager().updateMetadata();
        } else {
            // If skipping was not possible, we stop and release the resources:
            handleStopRequest(null);
        }
    }

    @Override
    public void onPlaybackStatusChanged(int state) {
        LogHelper.v(TAG, "onPlaybackStatusChanged");
        updatePlaybackState(null);
    }

    @Override
    public void onError(String error) {
        LogHelper.v(TAG, "onError");
        updatePlaybackState(error);
    }

    @Override
    public void setCurrentMediaId(String mediaId) {
        LogHelper.v(TAG, "setCurrentMediaId", mediaId);
        getQueueManager().setQueueFromMusic(mediaId);
    }

    /**
     * Switch to a different Playback instance, maintaining all playback state, if possible.
     *
     * @param playback switch to this playback
     */
    public void switchToPlayback(Playback playback, boolean resumePlaying) {
        LogHelper.v(TAG, "switchToPlayback");
        if (playback == null) {
            throw new IllegalArgumentException("Playback cannot be null");
        }
        // suspend the current one.
        int oldState = mPlayback.getState();
        int pos = mPlayback.getCurrentStreamPosition();
        String currentMediaId = mPlayback.getCurrentMediaId();
        mPlayback.stop(false);
        playback.setCallback(this);
        playback.setCurrentStreamPosition(pos < 0 ? 0 : pos);
        playback.setCurrentMediaId(currentMediaId);
        playback.start();
        // finally swap the instance
        mPlayback = playback;
        switch (oldState) {
            case PlaybackStateCompat.STATE_BUFFERING:
            case PlaybackStateCompat.STATE_CONNECTING:
            case PlaybackStateCompat.STATE_PAUSED:
                mPlayback.pause();
                break;
            case PlaybackStateCompat.STATE_PLAYING:
                MediaSessionCompat.QueueItem currentMusic = getQueueManager().getCurrentMusic();
                if (resumePlaying && currentMusic != null) {
                    mPlayback.play(currentMusic);
                } else if (!resumePlaying) {
                    mPlayback.pause();
                } else {
                    mPlayback.stop(true);
                }
                break;
            case PlaybackStateCompat.STATE_NONE:
                break;
            default:
                LogHelper.d(TAG, "Default called. Old state is ", oldState);
        }
    }

    private class MediaSessionCallback extends MediaSessionCompat.Callback {
        private final String TAG = "LEE: <" + MediaSessionCallback.class.getSimpleName() + ">";

        @Override
        public void onPlay() {
            LogHelper.v(TAG, "play");
            if (getQueueManager().getCurrentMusic() == null) {
                getQueueManager().setOrderedQueue();
            }
            handlePlayRequest();
        }

        @Override
        public void onSkipToQueueItem(long queueId) {
            LogHelper.v(TAG, "onSkipToQueueItem:" + queueId);
            getQueueManager().setCurrentQueueItem(queueId);
            handlePlayRequest();
            getQueueManager().updateMetadata();
        }

        @Override
        public void onSeekTo(long position) {
            LogHelper.v(TAG, "onSeekTo:", position);
            mPlayback.seekTo((int) position);
        }

        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            LogHelper.v(TAG, "=========> onPlayFromMediaId mediaId="+mediaId+", extras="+extras);
            getQueueManager().setQueueFromMusic(mediaId);
            handlePlayRequest();
        }

        @Override
        public void onPause() {
            LogHelper.v(TAG, "onPause - current state=" + mPlayback.getState());
            handlePauseRequest();
        }

        @Override
        public void onStop() {
            LogHelper.v(TAG, "onStop - current state=" + mPlayback.getState());
            handleStopRequest(null);
        }

        @Override
        public void onSkipToNext() {
            LogHelper.v(TAG, "onSkipToNext");
            if (getQueueManager().skipQueuePosition(1)) {
                handlePlayRequest();
            } else {
                handleStopRequest("Cannot skip");
            }
            getQueueManager().updateMetadata();
        }

        @Override
        public void onSkipToPrevious() {
            LogHelper.v(TAG, "onSkipToPrevious");
            if (getQueueManager().skipQueuePosition(-1)) {
                handlePlayRequest();
            } else {
                handleStopRequest("Cannot skip");
            }
            getQueueManager().updateMetadata();
        }

        @Override
        public void onCustomAction(@NonNull String action, Bundle extras) {
            LogHelper.v(TAG, "onCustomAction");
            if (CUSTOM_ACTION_THUMBS_UP.equals(action)) {
                LogHelper.i(TAG, "onCustomAction: favorite for current track");
                MediaSessionCompat.QueueItem currentMusic = getQueueManager().getCurrentMusic();
                if (currentMusic != null) {
                    String mediaId = currentMusic.getDescription().getMediaId();
                    if (mediaId != null) {
                        String musicId = MediaIDHelper.extractMusicIDFromMediaID(mediaId);
                        mMusicProvider.setFavorite(musicId, !mMusicProvider.isFavorite(musicId));
                    }
                }
                // playback state needs to be updated because the "Favorite" icon on the
                // custom action will change to reflect the new favorite state.
                updatePlaybackState(null);
            } else {
                LogHelper.e(TAG, "Unsupported action: ", action);
            }
        }

        /**
         * Handle free and contextual searches.
         * <p/>
         * All voice searches on Android Auto are sent to this method through a connected
         * {@link android.support.v4.media.session.MediaControllerCompat}.
         * <p/>
         * Threads and async handling:
         * Search, as a potentially slow operation, should run in another thread.
         * <p/>
         * Since this method runs on the main thread, most apps with non-trivial metadata
         * should defer the actual search to another thread (for example, by using
         * an {@link AsyncTask} as we do here).
         **/
        @Override
        public void onPlayFromSearch(final String query, final Bundle extras) {
            LogHelper.d(TAG, "onPlayFromSearch - query=", query, " extras=", extras);
            mPlayback.setState(PlaybackStateCompat.STATE_CONNECTING);
            getQueueManager().setQueueFromSearch(query, extras);
            handlePlayRequest();
            getQueueManager().updateMetadata();
        }
    }

    public interface PlaybackServiceCallback {
        void onPlaybackStart();

        void onNotificationRequired();

        void onPlaybackStop();

        void onPlaybackStateUpdated(PlaybackStateCompat newState);
    }
}
