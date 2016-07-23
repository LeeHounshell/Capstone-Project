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

package com.harlie.radiotheater.radiomysterytheater.model;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.RatingCompat;

import com.harlie.radiotheater.radiomysterytheater.R;
import com.harlie.radiotheater.radiomysterytheater.RadioTheaterApplication;
import com.harlie.radiotheater.radiomysterytheater.data.episodes.EpisodesColumns;
import com.harlie.radiotheater.radiomysterytheater.data.episodes.EpisodesCursor;
import com.harlie.radiotheater.radiomysterytheater.data.episodeswriters.EpisodesWritersColumns;
import com.harlie.radiotheater.radiomysterytheater.data.episodeswriters.EpisodesWritersCursor;
import com.harlie.radiotheater.radiomysterytheater.data.episodeswriters.EpisodesWritersSelection;
import com.harlie.radiotheater.radiomysterytheater.data_helper.RadioTheaterContract;
import com.harlie.radiotheater.radiomysterytheater.utils.BitmapHelper;
import com.harlie.radiotheater.radiomysterytheater.utils.LogHelper;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Utility class to get a list of Episodes's based on a server-side JSON configuration.
 * Note the JSON configuration is already loaded into the local SQLite database.
 * Also note the local SQLite database gets Firebase updates for any data changes,
 * so the media's ultimate "source" can still be considered as sesrver-side JSON.
 */
public class RemoteJSONSource implements MusicProviderSource {
    private final static String TAG = "LEE: <" + RemoteJSONSource.class.getSimpleName() + ">";

    private static volatile boolean sLoadedMediaMetaData = false;

    @Override
    public Iterator<MediaMetadataCompat> iterator() {
        ArrayList<MediaMetadataCompat> tracks = new ArrayList<>();
        if (!sLoadedMediaMetaData) {
            sLoadedMediaMetaData = true; // WATCHME: determine properly if Meta-Data needs to be (re)loaded.
            EpisodesCursor episodesCursor = getEpisodes();
            if (episodesCursor != null) {
                while (episodesCursor.moveToNext()) {
                    tracks.add(buildFromEpisodesCursor(episodesCursor));
                }
                episodesCursor.close();
            }
        }
        else {
            LogHelper.v(TAG, "*** ALREADY LOADED THE MEDIA-META-DATA!!! ***");
        }
        return tracks.iterator();
    }

    public EpisodesCursor getEpisodes() {
        String order_limit = RadioTheaterContract.EpisodesEntry.FIELD_EPISODE_NUMBER + " ASC";

        Cursor cursor = RadioTheaterApplication.getRadioTheaterApplicationContext().getContentResolver().query(
                EpisodesColumns.CONTENT_URI,        // the 'content://' Uri to query
                null,                               // projection String[] - leaving "columns" null just returns all the columns.
                null,                               // selection - SQL where
                null,                               // selection args String[] - values for the "where" clause
                order_limit);                       // sort order and limit (String)

        return (cursor != null && cursor.getCount() > 0) ? new EpisodesCursor(cursor) : null;
    }

    private String getWriterForEpisodeId(Long episodeNumber) {
        //LogHelper.v(TAG, "getWriterForEpisodeId: "+episodeNumber);
        EpisodesWritersSelection where = new EpisodesWritersSelection();
        where.fieldEpisodeNumber(episodeNumber);
        String order_limit = RadioTheaterContract.EpisodesWritersEntry.FIELD_WRITER_ID + " ASC LIMIT 1";

        Cursor cursor = RadioTheaterApplication.getRadioTheaterApplicationContext().getContentResolver().query(
                EpisodesWritersColumns.CONTENT_URI, // the 'content://' Uri to query
                null,                               // projection String[] - leaving "columns" null just returns all the columns.
                where.sel(),                        // selection - SQL where
                where.args(),                       // selection args String[] - values for the "where" clause
                order_limit);                       // sort order and limit (String)

        String writer = null;
        if (cursor != null && cursor.getCount() > 0) {
            EpisodesWritersCursor episodesWritersCursor = new EpisodesWritersCursor(cursor);
            if (episodesWritersCursor.moveToNext()) {
                writer = episodesWritersCursor.getFieldWriterName();
            }
            cursor.close();
        }
        if (writer == null) {
            writer = "Unknown";
        }
        return writer;
    }

    private MediaMetadataCompat buildFromEpisodesCursor(EpisodesCursor episodesCursor) {

        Long episodeNumber = episodesCursor.getFieldEpisodeNumber();
        String airdate = episodesCursor.getFieldAirdate(); // yyyy-MM-dd
        Long airdate_year = Long.valueOf(airdate.substring(0, 4));
        String episodeTitle = episodesCursor.getFieldEpisodeTitle();
        String episodeDescription = episodesCursor.getFieldEpisodeDescription();
        String episodeDownloadUrl = Uri.parse("http://"+episodesCursor.getFieldDownloadUrl()).toString();
        Float rating = episodesCursor.getFieldRating();
        float f_rating = (rating == null) ? (float) 0.0 : rating.floatValue();
        float ratingPercent = (float) ((f_rating * 100.0) / 5.0);
        RatingCompat ratingCompat = RatingCompat.newPercentageRating(ratingPercent);
        String episodeWriter = getWriterForEpisodeId(episodeNumber);

        String artist = RadioTheaterApplication.getRadioTheaterApplicationContext().getResources().getString(R.string.e_g_marshall);
        String genre = RadioTheaterApplication.getRadioTheaterApplicationContext().getResources().getString(R.string.genre);
        @SuppressWarnings("UnusedAssignment") String iconUrl = RadioTheaterApplication.getRadioTheaterApplicationContext().getResources().getString(R.string.icon_url);
        @SuppressWarnings("UnusedAssignment") String keyArtUrl =  RadioTheaterApplication.getRadioTheaterApplicationContext().getResources().getString(R.string.key_art_url);
        @SuppressWarnings("UnusedAssignment") String hauntedUrl =  RadioTheaterApplication.getRadioTheaterApplicationContext().getResources().getString(R.string.haunted_url);
        Drawable iconDrawable = ResourcesCompat.getDrawable(RadioTheaterApplication.getRadioTheaterApplicationContext().getResources(), R.drawable.logo_icon, null);
        @SuppressWarnings("UnusedAssignment") Bitmap iconBitmap = BitmapHelper.drawableToBitmap(iconDrawable);
        Drawable keyArtDrawable = ResourcesCompat.getDrawable(RadioTheaterApplication.getRadioTheaterApplicationContext().getResources(), R.drawable.e_g_marshall_1970, null);
        @SuppressWarnings("UnusedAssignment") Bitmap keyArtBitmap = BitmapHelper.drawableToBitmap(keyArtDrawable);
        Drawable hauntedDrawable = ResourcesCompat.getDrawable(RadioTheaterApplication.getRadioTheaterApplicationContext().getResources(), R.drawable.haunted, null);
        @SuppressWarnings("UnusedAssignment") Bitmap hauntedBitmap = BitmapHelper.drawableToBitmap(hauntedDrawable);
        int totalTrackCount = Integer.valueOf(RadioTheaterApplication.getRadioTheaterApplicationContext().getResources().getString(R.string.episodes_count));
        int duration = 50 * 60 * 1000; // fifty-minutes in ms
        String id = String.valueOf(episodeDownloadUrl.hashCode());

        //LogHelper.d(TAG, "found episode: #"+episodeNumber+" '"+episodeTitle+"' by "+episodeWriter+" with id="+id);

        // Adding the episode source to the MediaMetadata (and consequently using it in the
        // mediaSession.setMetadata) is not a good idea for a real world player app, because
        // the session metadata can be accessed by notification listeners.

        //--------------------------------------------------------------------------------
        // FIXME - FAILED BINDER TRANSACTION error with images.  (removed the images from MetaData for temp fix)
        /*
        07-11 00:50:33.274 8581-8581/com.harlie.radiotheater.radiomysterytheater.paid V/LEE: <QueueManager>: ---------> getCurrentMusic: mediaId=881854013, episodeMediaId=__BY_GENRE__/__RADIO_DRAMA__|881854013, title=The Old Ones Are Hard to Kill
        07-11 00:50:33.274 8581-8581/com.harlie.radiotheater.radiomysterytheater.paid V/LEE: <MusicProvider>: getMusicsByGenre: genre=__RADIO_DRAMA__
        07-11 00:50:33.274 8581-8581/com.harlie.radiotheater.radiomysterytheater.paid V/LEE: <MusicProvider>: getMusicsByGenre(__RADIO_DRAMA__) found 1
        07-11 00:50:33.274 8581-8581/com.harlie.radiotheater.radiomysterytheater.paid V/LEE: <QueueHelper>: convertToQueue
        07-11 00:50:33.297 8581-8581/com.harlie.radiotheater.radiomysterytheater.paid V/LEE: <QueueHelper>: getMusicIndexOnQueue: mediaId=__BY_GENRE__/__RADIO_DRAMA__|881854013
        07-11 00:50:33.298 8581-8581/com.harlie.radiotheater.radiomysterytheater.paid V/LEE: <QueueManager>: setCurrentQueue
        07-11 00:50:33.298 8581-8581/com.harlie.radiotheater.radiomysterytheater.paid V/LEE: <QueueManager>: setCurrentQueue: title=The Old Ones Are Hard to Kill
        07-11 00:50:33.321 8581-8581/com.harlie.radiotheater.radiomysterytheater.paid E/JavaBinder: !!! FAILED BINDER TRANSACTION !!!  (parcel size = 65936)
        07-11 00:50:33.324 8581-8581/com.harlie.radiotheater.radiomysterytheater.paid E/Dead object in setQueue.: Transaction failed on small parcel; remote process probably died
        android.os.DeadObjectException: Transaction failed on small parcel; remote process probably died
        at android.os.BinderProxy.transactNative(Native Method)
        at android.os.BinderProxy.transact(Binder.java:503)
        at android.media.session.ISession$Stub$Proxy.setQueue(ISession.java:439)
        at android.media.session.MediaSession.setQueue(MediaSession.java:438)
        at android.support.v4.media.session.MediaSessionCompatApi21.setQueue(MediaSessionCompatApi21.java:124)
        at android.support.v4.media.session.MediaSessionCompat$MediaSessionImplApi21.setQueue(MediaSessionCompat.java:2264)
        at android.support.v4.media.session.MediaSessionCompat.setQueue(MediaSessionCompat.java:434)
        at com.harlie.radiotheater.radiomysterytheater.RadioTheaterService$2.onQueueUpdated(RadioTheaterService.java:236)
        at com.harlie.radiotheater.radiomysterytheater.playback.QueueManager.setCurrentQueue(QueueManager.java:385)
        at com.harlie.radiotheater.radiomysterytheater.playback.QueueManager.setCurrentQueue(QueueManager.java:374)
        at com.harlie.radiotheater.radiomysterytheater.playback.QueueManager.setCurrentIndexFromEpisodeId(QueueManager.java:260)
        at com.harlie.radiotheater.radiomysterytheater.playback.QueueManager.getCurrentMusic(QueueManager.java:189)
        at com.harlie.radiotheater.radiomysterytheater.playback.PlaybackManager.setCustomAction(PlaybackManager.java:159)
        at com.harlie.radiotheater.radiomysterytheater.playback.PlaybackManager.updatePlaybackState(PlaybackManager.java:130)
        at com.harlie.radiotheater.radiomysterytheater.playback.PlaybackManager.onPlaybackStatusChanged(PlaybackManager.java:215)
        at com.harlie.radiotheater.radiomysterytheater.playback.LocalPlayback.stop(LocalPlayback.java:137)
        at com.harlie.radiotheater.radiomysterytheater.playback.PlaybackManager.handleStopRequest(PlaybackManager.java:109)
        at com.harlie.radiotheater.radiomysterytheater.RadioTheaterService.onDestroy(RadioTheaterService.java:334)
        at android.app.ActivityThread.handleStopService(ActivityThread.java:3082)
        at android.app.ActivityThread.-wrap21(ActivityThread.java)
        at android.app.ActivityThread$H.handleMessage(ActivityThread.java:1448)
        at android.os.Handler.dispatchMessage(Handler.java:104)
        at android.os.Looper.loop(Looper.java:148)
        at android.app.ActivityThread.main(ActivityThread.java:5460)
        at java.lang.reflect.Method.invoke(Native Method)
        at com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:726)
        at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:616)
        07-11 00:50:33.326 8581-8581/com.harlie.radiotheater.radiomysterytheater.paid V/LEE: <QueueManager>: getCurrentMusic: sCurrentIndex=0
        */
        //--------------------------------------------------------------------------------

        //noinspection ResourceType,UnnecessaryLocalVariable
        MediaMetadataCompat theMetadata = new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, id)
                .putString(MusicProviderSource.CUSTOM_METADATA_TRACK_SOURCE, episodeDownloadUrl)
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, episodeTitle)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, episodeDescription)
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, episodeDescription)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, episodeTitle)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                .putString(MediaMetadataCompat.METADATA_KEY_WRITER, episodeWriter)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
                .putString(MediaMetadataCompat.METADATA_KEY_DATE, airdate)
                .putLong(MediaMetadataCompat.METADATA_KEY_YEAR, airdate_year)
                .putString(MediaMetadataCompat.METADATA_KEY_GENRE, genre)
//                .putBitmap(MediaMetadataCompat.METADATA_KEY_ART, keyArtBitmap)
//                .putString(MediaMetadataCompat.METADATA_KEY_ART_URI, keyArtUrl)
//                .putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, iconBitmap)
//                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, iconUrl)
//                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, hauntedBitmap)
//                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, hauntedUrl)
                .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, episodeNumber)
                .putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, totalTrackCount)
                .putRating(MediaMetadataCompat.METADATA_KEY_RATING, ratingCompat)
                .build();

        return theMetadata;
    }

}
