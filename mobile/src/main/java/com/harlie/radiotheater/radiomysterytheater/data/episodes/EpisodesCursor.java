//
//===========================================================================================================
// THIS IS GENERATED CODE! - do not edit
//
// To edit, change JSON configs under 'com/harlie/radiotheater/radiomysterytheater/generate_data_contentprovider/'
// Then run the bash script './generate-data-contentprovider.sh' in that same directory
// to create new Java code under 'com/harlie/radiotheater/radiomysterytheater/data/' + note old 'data' is deleted!
//
// THIS IS GENERATED CODE! - do not edit
//===========================================================================================================
//
package com.harlie.radiotheater.radiomysterytheater.data.episodes;

import java.util.Date;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.harlie.radiotheater.radiomysterytheater.data.base.AbstractCursor;

/**
 * Cursor wrapper for the {@code episodes} table.
 */
public class EpisodesCursor extends AbstractCursor implements EpisodesModel {
    public EpisodesCursor(Cursor cursor) {
        super(cursor);
    }

    /**
     * Primary key.
     */
    public long getId() {
        Long res = getLongOrNull(EpisodesColumns._ID);
        if (res == null)
            throw new NullPointerException("The value of '_id' in the database was null, which is not allowed according to the model definition");
        return res;
    }

    /**
     * Get the {@code field_episode_number} value.
     */
    public long getFieldEpisodeNumber() {
        Long res = getLongOrNull(EpisodesColumns.FIELD_EPISODE_NUMBER);
        if (res == null)
            throw new NullPointerException("The value of 'field_episode_number' in the database was null, which is not allowed according to the model definition");
        return res;
    }

    /**
     * Air Date
     * Cannot be {@code null}.
     */
    @NonNull
    public String getFieldAirdate() {
        String res = getStringOrNull(EpisodesColumns.FIELD_AIRDATE);
        if (res == null)
            throw new NullPointerException("The value of 'field_airdate' in the database was null, which is not allowed according to the model definition");
        return res;
    }

    /**
     * the episode title
     * Cannot be {@code null}.
     */
    @NonNull
    public String getFieldEpisodeTitle() {
        String res = getStringOrNull(EpisodesColumns.FIELD_EPISODE_TITLE);
        if (res == null)
            throw new NullPointerException("The value of 'field_episode_title' in the database was null, which is not allowed according to the model definition");
        return res;
    }

    /**
     * episode description
     * Cannot be {@code null}.
     */
    @NonNull
    public String getFieldEpisodeDescription() {
        String res = getStringOrNull(EpisodesColumns.FIELD_EPISODE_DESCRIPTION);
        if (res == null)
            throw new NullPointerException("The value of 'field_episode_description' in the database was null, which is not allowed according to the model definition");
        return res;
    }

    /**
     * url weblink path
     * Can be {@code null}.
     */
    @Nullable
    public String getFieldWeblinkUrl() {
        String res = getStringOrNull(EpisodesColumns.FIELD_WEBLINK_URL);
        return res;
    }

    /**
     * url download path
     * Cannot be {@code null}.
     */
    @NonNull
    public String getFieldDownloadUrl() {
        String res = getStringOrNull(EpisodesColumns.FIELD_DOWNLOAD_URL);
        if (res == null)
            throw new NullPointerException("The value of 'field_download_url' in the database was null, which is not allowed according to the model definition");
        return res;
    }

    /**
     * episode rating
     * Can be {@code null}.
     */
    @Nullable
    public Float getFieldRating() {
        Float res = getFloatOrNull(EpisodesColumns.FIELD_RATING);
        return res;
    }

    /**
     * episode vote count
     * Can be {@code null}.
     */
    @Nullable
    public Integer getFieldVoteCount() {
        Integer res = getIntegerOrNull(EpisodesColumns.FIELD_VOTE_COUNT);
        return res;
    }
}
