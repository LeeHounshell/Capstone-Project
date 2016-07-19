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
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;

import com.harlie.radiotheater.radiomysterytheater.BaseActivity;
import com.harlie.radiotheater.radiomysterytheater.R;
import com.harlie.radiotheater.radiomysterytheater.RadioControlIntentService;
import com.harlie.radiotheater.radiomysterytheater.RadioTheaterApplication;
import com.harlie.radiotheater.radiomysterytheater.data.configepisodes.ConfigEpisodesColumns;
import com.harlie.radiotheater.radiomysterytheater.data.configepisodes.ConfigEpisodesCursor;
import com.harlie.radiotheater.radiomysterytheater.data.configepisodes.ConfigEpisodesSelection;
import com.harlie.radiotheater.radiomysterytheater.data.episodes.EpisodesColumns;
import com.harlie.radiotheater.radiomysterytheater.data.episodes.EpisodesCursor;
import com.harlie.radiotheater.radiomysterytheater.data.episodes.EpisodesSelection;
import com.harlie.radiotheater.radiomysterytheater.data_helper.RadioTheaterContract;
import com.harlie.radiotheater.radiomysterytheater.model.MusicProvider;
import com.harlie.radiotheater.radiomysterytheater.utils.AlbumArtCache;
import com.harlie.radiotheater.radiomysterytheater.utils.LogHelper;
import com.harlie.radiotheater.radiomysterytheater.utils.MediaIDHelper;
import com.harlie.radiotheater.radiomysterytheater.utils.NetworkHelper;
import com.harlie.radiotheater.radiomysterytheater.utils.QueueHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static com.harlie.radiotheater.radiomysterytheater.utils.MediaIDHelper.MEDIA_ID_MUSICS_BY_GENRE;
import static com.harlie.radiotheater.radiomysterytheater.utils.QueueHelper.convertToQueue;
import static com.harlie.radiotheater.radiomysterytheater.utils.QueueHelper.getMusicIndexOnQueue;
import static com.harlie.radiotheater.radiomysterytheater.utils.QueueHelper.getPlayingQueueFromSearch;
import static com.harlie.radiotheater.radiomysterytheater.utils.QueueHelper.getRandomQueue;
import static com.harlie.radiotheater.radiomysterytheater.utils.QueueHelper.isIndexPlayable;

/**
 * Simple data provider for queues. Keeps track of a current queue and a current index in the
 * queue. Also provides methods to set the current queue based on common queries, relying on a
 * given MusicProvider to provide the actual media metadata.
 */
public class QueueManager {
    private final static String TAG = "LEE: <" + QueueManager.class.getSimpleName() + ">";

    private MusicProvider mMusicProvider;
    private MetadataUpdateListener mListener;
    private Resources mResources;

    public static List<MediaSessionCompat.QueueItem> getPlayingQueue() {
        return sPlayingQueue;
    }
    private static List<MediaSessionCompat.QueueItem> sPlayingQueue; // "Now playing" queue

    private static volatile int sCurrentIndex = 0;
    private static volatile int sPreviousIndex = -1;
    private static String sDownloadUrl;

    public QueueManager(@NonNull MusicProvider musicProvider,
                        @NonNull Resources resources,
                        @NonNull MetadataUpdateListener listener)
    {
        LogHelper.v(TAG, "QueueManager");
        this.mMusicProvider = musicProvider;
        this.mListener = listener;
        this.mResources = resources;

        if (sPlayingQueue == null) {
            sPlayingQueue = Collections.synchronizedList(new ArrayList<MediaSessionCompat.QueueItem>());
        }
        sCurrentIndex = 0;
    }

    public boolean isSameBrowsingCategory(@NonNull String mediaId) {
        LogHelper.v(TAG, "isSameBrowsingCategory");
        String[] newBrowseHierarchy = MediaIDHelper.getHierarchy(mediaId);
        if (newBrowseHierarchy == null) {
            return false;
        }
        MediaSessionCompat.QueueItem current = getCurrentMusic();
        if (current == null) {
            return false;
        }
        String[] currentBrowseHierarchy = MediaIDHelper.getHierarchy(current.getDescription().getMediaId());
        if (currentBrowseHierarchy == null) {
            return false;
        }
        return Arrays.equals(newBrowseHierarchy, currentBrowseHierarchy);
    }

    private void setCurrentQueueIndex(int index) {
        LogHelper.v(TAG, "setCurrentQueueIndex: index="+index);
        if (getPlayingQueue() != null) {
            if (index >= 0 && index < getPlayingQueue().size()) {
                sCurrentIndex = index;
                mListener.onCurrentQueueIndexUpdated(getCurrentIndex());
            }
        }
        else {
            LogHelper.w(TAG, "*** THE PLAYING QUEUE IS NULL!");
        }
    }

    public boolean setCurrentQueueItem(long queueId) {
        LogHelper.v(TAG, "setCurrentQueueItem: queueId="+queueId);
        // set the current index on queue from the queue Id:
        if (getPlayingQueue() == null) {
            return false;
        }
        int index = getMusicIndexOnQueue(getPlayingQueue(), queueId);
        if (index == -1) {
            setCurrentQueueIndex((int) queueId);
            return queueId >= 0;
        }
        setCurrentQueueIndex(index);
        return index >= 0;
    }

    public boolean setCurrentQueueItem(String mediaId) {
        LogHelper.v(TAG, "setCurrentQueueItem: mediaId="+mediaId);
        // set the current index on queue from the music Id:
        if (getPlayingQueue() == null) {
            return false;
        }
        int index = getMusicIndexOnQueue(getPlayingQueue(), mediaId);
        setCurrentQueueIndex(index);
        return index >= 0;
    }

    public boolean skipQueuePosition(int amount) {
        LogHelper.v(TAG, "skipQueuePosition: amount="+amount);
        if (getPlayingQueue() == null) {
            LogHelper.v(TAG, "the sPlayingQueue is null - can't backup or advance yet.");
            return false;
        }
        int index = getCurrentIndex() + amount;
        if (index < 0) {
            // skip backwards before the first song will keep you on the first song
            index = 0;
        }
        else if (getPlayingQueue().size() > 0) {
            // skip forwards when in last song will cycle back to start of the queue
            index %= getPlayingQueue().size();
        }
        if (!isIndexPlayable(index, getPlayingQueue())) {
            LogHelper.e(TAG, "Cannot increment queue index by ", amount,
                    ". Current=", getCurrentIndex(), " queue length=", getPlayingQueue().size());
            return false;
        }
        sCurrentIndex = index;
        return true;
    }

    public void setQueueFromSearch(String query, Bundle extras) {
        LogHelper.v(TAG, "setQueueFromSearch: query="+query);
        setCurrentQueue(mResources.getString(R.string.search_queue_title),
                getPlayingQueueFromSearch(query, extras, mMusicProvider));
    }

    public void setRandomQueue() {
        LogHelper.v(TAG, "setRandomQueue");
        setCurrentQueue(mResources.getString(R.string.random_queue_title), getRandomQueue(mMusicProvider));
    }

    //-------- RADIO THEATER --------
    public MediaSessionCompat.QueueItem getCurrentMusic() {
        LogHelper.v(TAG, "*** getCurrentMusic ***");
        if (getPlayingQueue() == null) {
            LogHelper.v(TAG, "getCurrentMusic: *** THE PLAYING QUEUE IS NULL! ***");
            return null;
        }
        if (getPlayingQueue().size() == 1) {
            LogHelper.v(TAG, "getCurrentMusic: *** THE PLAYING QUEUE HAS A SINGLE ITEM *** sCurrentIndex="+getCurrentIndex()+", size="+ getPlayingQueue().size());
            return getPlayingQueue().get(0);
        }
        else if (getPlayingQueue().size() == 0) {
            LogHelper.v(TAG, "getCurrentMusic: *** THE PLAYING QUEUE IS EMPTY ***");
            while (MusicProvider.isMediaLoaded() == false && MusicProvider.isOnMusicCatalogReady() == false) {
                LogHelper.v(TAG, "getCurrentMusic: goto sleep (waiting for MusicProvider Media and catalog..)");
                waitabit();
            }
            return null;
        }
        LogHelper.v(TAG, "getCurrentMusic: *** THE PLAYING QUEUE HAS ITEMS *** sCurrentIndex="+getCurrentIndex()+", size="+ getPlayingQueue().size());
        return getPlayingQueue().get(getCurrentIndex());
    }

    //-------- RADIO THEATER --------
    private void waitabit() {
        try {
            long timeInMills = 1000;
            SystemClock.sleep(timeInMills);
        }catch (Exception e) {
            ;
        }
    }

    //-------- RADIO THEATER --------
    public String setCurrentIndexFromEpisodeId(int episodeId, String title, String downloadUrl) {
        LogHelper.v(TAG, "setCurrentIndexFromEpisodeId: episodeId="+episodeId);
        while (MusicProvider.isMediaLoaded() == false && MusicProvider.isOnMusicCatalogReady() == false) {
            LogHelper.v(TAG, "setCurrentIndexFromEpisodeId: goto sleep (waiting for MusicProvider Media and catalog..)");
            waitabit();
        }
        waitabit();

        LogHelper.v(TAG, "---> wakeup! setCurrentIndexFromEpisodeId: episodeId="+episodeId+", title="+title+", downloadUrl="+downloadUrl);

        sDownloadUrl = downloadUrl;

        Iterable<MediaMetadataCompat> title_list = mMusicProvider.searchMusicBySongTitle(title);
        if (title_list == null) {
            LogHelper.e(TAG, "FAIL: could not locate media for title: ", title);
            waitabit();
            return null;
        }
        Iterator<MediaMetadataCompat> tracks = title_list.iterator();
        if (! tracks.hasNext()) {
            LogHelper.e(TAG, "LOADING MEDIA META-DATA? - found no media for title: ", title);
            return null;
        }
        MediaMetadataCompat theMedia = tracks.next();
        String mediaId = theMedia.getDescription().getMediaId();
        LogHelper.v(TAG, "---> working with theMedia="+theMedia.getDescription()+", mediaId="+mediaId);
        String categoryType = MEDIA_ID_MUSICS_BY_GENRE;
        String categoryValue = RadioTheaterApplication.getRadioTheaterApplicationContext().getString(R.string.genre);
        String episodeMediaId = MediaIDHelper.createMediaID(mediaId, categoryType, categoryValue);
        LogHelper.v(TAG, "---------> getCurrentMusic: mediaId="+mediaId+", episodeMediaId="+episodeMediaId+", title="+title);
        Iterable<MediaMetadataCompat> all_episodes = mMusicProvider.getMusicsByGenre(categoryValue);

        List<MediaSessionCompat.QueueItem> all_queued = convertToQueue(all_episodes, categoryType, categoryValue);

        sCurrentIndex = QueueHelper.getMusicIndexOnQueue(all_queued, episodeMediaId);
        try {
            setCurrentQueue(title, all_queued);
        } catch (Exception e) {
            LogHelper.w(TAG, "setCurrentQueue all_queued request - e="+e.getMessage());
        }
        LogHelper.v(TAG, "getCurrentMusic: sCurrentIndex="+getCurrentIndex());

        // automatically start playback
        //RadioControlIntentService.startActionPlay(RadioTheaterApplication.getRadioTheaterApplicationContext(), "setCurrentIndexFromEpisodeId", String.valueOf(episodeId), sDownloadUrl);

        return episodeMediaId;
    }

    //-------- RADIO THEATER --------
    private String getTitleAndDownloadUrlForEpisode(int episodeID) {
        LogHelper.v(TAG, "getTitleAndDownloadUrlForEpisode "+episodeID);
        String title = "<unknown title>";
        // get this episode's detail info: title and downloadUrl
        EpisodesCursor episodesCursor = getEpisodesCursor(episodeID);
        if (episodesCursor != null && episodesCursor.moveToNext()) {
            title = episodesCursor.getFieldEpisodeTitle();
            sDownloadUrl = episodesCursor.getFieldDownloadUrl();
            episodesCursor.close();
        }
        return title;
    }

    //-------- RADIO THEATER --------
    public EpisodesCursor getEpisodesCursor(long episode) {
        EpisodesSelection where = new EpisodesSelection();
        where.fieldEpisodeNumber(episode);
        String order_limit = RadioTheaterContract.EpisodesEntry.FIELD_EPISODE_NUMBER + " ASC LIMIT 1";

        Cursor cursor = RadioTheaterApplication.getRadioTheaterApplicationContext().getContentResolver().query(
                EpisodesColumns.CONTENT_URI,        // the 'content://' Uri to query
                null,                               // projection String[] - leaving "columns" null just returns all the columns.
                where.sel(),                        // selection - SQL where
                where.args(),                       // selection args String[] - values for the "where" clause
                order_limit);                       // sort order and limit (String)

        return (cursor != null && cursor.getCount() > 0) ? new EpisodesCursor(cursor) : null;
    }

    //-------- RADIO THEATER --------
    private int getNextAvailableEpisode() {
        // find the next index to play from SQLite
        int index = getCurrentIndex();
        ConfigEpisodesCursor configCursor = getCursorForNextAvailableEpisode();
        if (configCursor != null && configCursor.moveToNext()) {
            // found the next episode to listen to
            index = (int) configCursor.getFieldEpisodeNumber();
            LogHelper.v(TAG, "*** RADIO THEATER: NEXT AVAILABLE EPISODE #"+index);
            configCursor.close();
        }
        return index;
    }

    //-------- RADIO THEATER --------
    public ConfigEpisodesCursor getCursorForNextAvailableEpisode() {
        ConfigEpisodesSelection where = new ConfigEpisodesSelection();
        // find the next unwatched episode, in airdate order
        where.fieldEpisodeHeard(false);
        if (! NetworkHelper.isOnline(RadioTheaterApplication.getRadioTheaterApplicationContext())) {
            // find the next DOWNLOADED unwatched episode, in airdate order
            where.fieldEpisodeDownloaded(true);
        }

        String order_limit = RadioTheaterContract.ConfigEpisodesEntry.FIELD_EPISODE_NUMBER + " ASC LIMIT 1";

        Cursor cursor = RadioTheaterApplication.getRadioTheaterApplicationContext().getContentResolver().query(
                ConfigEpisodesColumns.CONTENT_URI,  // the 'content://' Uri to query
                null,                               // projection String[] - leaving "columns" null just returns all the columns.
                where.sel(),                        // selection - SQL where
                where.args(),                       // selection args String[] - values for the "where" clause
                order_limit);                       // sort order and limit (String)

        return (cursor != null) ? new ConfigEpisodesCursor(cursor) : null;
    }

    //-------- RADIO THEATER --------
    public static int getCurrentIndex() {
        return sCurrentIndex;
    }

    //-------- RADIO THEATER --------
    public static String getDownloadUrl() {
        return sDownloadUrl;
    }

    public void setQueueFromMusic(String mediaId) {
        LogHelper.v(TAG, "setQueueFromMusic: mediaId="+mediaId);

        // The mediaId used here is not the unique musicId. This one comes from the
        // MediaBrowser, and is actually a "hierarchy-aware mediaID": a concatenation of
        // the hierarchy in MediaBrowser and the actual unique musicID. This is necessary
        // so we can build the correct playing queue, based on where the track was
        // selected from.
        boolean canReuseQueue = false;
        if (mediaId != null && isSameBrowsingCategory(mediaId)) {
            canReuseQueue = setCurrentQueueItem(mediaId);
        }
        if (!canReuseQueue && getPlayingQueue() != null) {
            String queueTitle = mResources.getString(R.string.browse_musics_by_genre_subtitle,
                    MediaIDHelper.extractBrowseCategoryValueFromMediaID(mediaId));
            setCurrentQueue(queueTitle, getPlayingQueue(), mediaId); // WATCHME
        }
        updateMetadata();
    }

    public int getCurrentQueueSize() {
        LogHelper.v(TAG, "getCurrentQueueSize");
        if (getPlayingQueue() == null) {
            return 0;
        }
        return getPlayingQueue().size();
    }

    protected void setCurrentQueue(String title, List<MediaSessionCompat.QueueItem> newQueue) {
        LogHelper.v(TAG, "setCurrentQueue");
        setCurrentQueue(title, newQueue, null);
    }

    protected void setCurrentQueue(String title, List<MediaSessionCompat.QueueItem> newQueue, String initialMediaId) {
        LogHelper.v(TAG, "setCurrentQueue: title="+title);
        sPlayingQueue = newQueue;
        int index = 0;
        if (initialMediaId != null && getPlayingQueue() != null) {
            index = getMusicIndexOnQueue(getPlayingQueue(), initialMediaId);
        }
        sCurrentIndex = Math.max(index, 0);
        mListener.onQueueUpdated(title, newQueue);
    }

    public void updateMetadata() {
        LogHelper.v(TAG, "updateMetadata");
        MediaSessionCompat.QueueItem currentMusic = getCurrentMusic();
        if (currentMusic == null) {
            mListener.onMetadataRetrieveError();
            return;
        }
        final String musicId = MediaIDHelper.extractMusicIDFromMediaID(currentMusic.getDescription().getMediaId());
        MediaMetadataCompat metadata = mMusicProvider.getMusic(musicId);
        if (metadata == null) {
            throw new IllegalArgumentException("Invalid musicId " + musicId);
        }

        mListener.onMetadataChanged(metadata);

        // Set the proper album artwork on the media session, so it can be shown in the
        // locked screen and in other places.
        if (metadata.getDescription().getIconBitmap() == null &&
                metadata.getDescription().getIconUri() != null) {
            String albumUri = metadata.getDescription().getIconUri().toString();
            AlbumArtCache.getInstance().fetch(albumUri, new AlbumArtCache.FetchListener() {
                @Override
                public void onFetched(String artUrl, Bitmap bitmap, Bitmap icon) {
                    mMusicProvider.updateMusicArt(musicId, bitmap, icon);

                    // If we are still playing the same music, notify the listeners:
                    MediaSessionCompat.QueueItem currentMusic = getCurrentMusic();
                    if (currentMusic == null) {
                        return;
                    }
                    String currentPlayingId = MediaIDHelper.extractMusicIDFromMediaID(currentMusic.getDescription().getMediaId());
                    if (musicId.equals(currentPlayingId)) {
                        mListener.onMetadataChanged(mMusicProvider.getMusic(currentPlayingId));
                    }
                }
            });
        }
    }

    public interface MetadataUpdateListener {
        void onMetadataChanged(MediaMetadataCompat metadata);

        void onMetadataRetrieveError();

        void onCurrentQueueIndexUpdated(int queueIndex);

        void onQueueUpdated(String title, List<MediaSessionCompat.QueueItem> newQueue);
    }
}
