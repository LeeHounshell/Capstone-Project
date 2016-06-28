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

    private String mMediaId;

    private static volatile boolean sLoadedMediaMetaData = false;

    @Override
    public Iterator<MediaMetadataCompat> iterator() {
        mMediaId = RadioTheaterApplication.getRadioTheaterApplicationContext().getResources().getString(R.string.genre);
        ArrayList<MediaMetadataCompat> tracks = new ArrayList<>();
        if (!sLoadedMediaMetaData) {
            sLoadedMediaMetaData = true;
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
        float ratingPercent = (float) ((rating * 100.0) / 5.0);
        RatingCompat ratingCompat = RatingCompat.newPercentageRating(ratingPercent);
        String episodeWriter = getWriterForEpisodeId(episodeNumber);

        String artist = RadioTheaterApplication.getRadioTheaterApplicationContext().getResources().getString(R.string.e_g_marshall);
        String genre = RadioTheaterApplication.getRadioTheaterApplicationContext().getResources().getString(R.string.genre);
        String iconUrl = RadioTheaterApplication.getRadioTheaterApplicationContext().getResources().getString(R.string.icon_url);
        String keyArtUrl =  RadioTheaterApplication.getRadioTheaterApplicationContext().getResources().getString(R.string.key_art_url);
        String hauntedUrl =  RadioTheaterApplication.getRadioTheaterApplicationContext().getResources().getString(R.string.haunted_url);
        Drawable iconDrawable = ResourcesCompat.getDrawable(RadioTheaterApplication.getRadioTheaterApplicationContext().getResources(), R.drawable.logo_icon, null);
        Bitmap iconBitmap = BitmapHelper.drawableToBitmap(iconDrawable);
        Drawable keyArtDrawable = ResourcesCompat.getDrawable(RadioTheaterApplication.getRadioTheaterApplicationContext().getResources(), R.drawable.e_g_marshall_1970, null);
        Bitmap keyArtBitmap = BitmapHelper.drawableToBitmap(keyArtDrawable);
        Drawable hauntedDrawable = ResourcesCompat.getDrawable(RadioTheaterApplication.getRadioTheaterApplicationContext().getResources(), R.drawable.haunted, null);
        Bitmap hauntedBitmap = BitmapHelper.drawableToBitmap(hauntedDrawable);
        int totalTrackCount = Integer.valueOf(RadioTheaterApplication.getRadioTheaterApplicationContext().getResources().getString(R.string.episodes_count));
        int duration = 60 * 60 * 1000; // one-hour in ms
        String id = String.valueOf(episodeDownloadUrl.hashCode());
        //String episodeMediaId = MediaIDHelper.createMediaID(id, MediaIDHelper.MEDIA_ID_ROOT, mMediaId);

        LogHelper.d(TAG, "found episode: #"+episodeNumber+" '"+episodeTitle+"' by "+episodeWriter+" with id="+id);

        // Adding the episode source to the MediaMetadata (and consequently using it in the
        // mediaSession.setMetadata) is not a good idea for a real world player app, because
        // the session metadata can be accessed by notification listeners.

        //noinspection ResourceType
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
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ART, keyArtBitmap)
//                .putString(MediaMetadataCompat.METADATA_KEY_ART_URI, keyArtUrl)
                .putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, iconBitmap)
//                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, iconUrl)
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, hauntedBitmap)
//                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, hauntedUrl)
                .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, episodeNumber)
                .putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, totalTrackCount)
                .putRating(MediaMetadataCompat.METADATA_KEY_RATING, ratingCompat)
                .build();

        return theMetadata;
    }

}
