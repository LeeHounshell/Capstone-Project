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

import android.content.Context;
import android.content.ContentResolver;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.harlie.radiotheater.radiomysterytheater.data.base.AbstractContentValues;

/**
 * Content values wrapper for the {@code episodes} table.
 */
public class EpisodesContentValues extends AbstractContentValues {
    @Override
    public Uri uri() {
        return EpisodesColumns.CONTENT_URI;
    }

    /**
     * Update row(s) using the values stored by this object and the given selection.
     *
     * @param contentResolver The content resolver to use.
     * @param where The selection to use (can be {@code null}).
     */
    public int update(ContentResolver contentResolver, @Nullable EpisodesSelection where) {
        return contentResolver.update(uri(), values(), where == null ? null : where.sel(), where == null ? null : where.args());
    }

    /**
     * Update row(s) using the values stored by this object and the given selection.
     *
     * @param contentResolver The content resolver to use.
     * @param where The selection to use (can be {@code null}).
     */
    public int update(Context context, @Nullable EpisodesSelection where) {
        return context.getContentResolver().update(uri(), values(), where == null ? null : where.sel(), where == null ? null : where.args());
    }

    public EpisodesContentValues putFieldEpisodeNumber(long value) {
        mContentValues.put(EpisodesColumns.FIELD_EPISODE_NUMBER, value);
        return this;
    }


    /**
     * Air Date
     */
    public EpisodesContentValues putFieldAirdate(@NonNull Date value) {
        if (value == null) throw new IllegalArgumentException("fieldAirdate must not be null");
        mContentValues.put(EpisodesColumns.FIELD_AIRDATE, value.getTime());
        return this;
    }


    public EpisodesContentValues putFieldAirdate(long value) {
        mContentValues.put(EpisodesColumns.FIELD_AIRDATE, value);
        return this;
    }

    /**
     * the episode title
     */
    public EpisodesContentValues putFieldEpisodeTitle(@NonNull String value) {
        if (value == null) throw new IllegalArgumentException("fieldEpisodeTitle must not be null");
        mContentValues.put(EpisodesColumns.FIELD_EPISODE_TITLE, value);
        return this;
    }


    /**
     * episode description
     */
    public EpisodesContentValues putFieldEpisodeDescription(@NonNull String value) {
        if (value == null) throw new IllegalArgumentException("fieldEpisodeDescription must not be null");
        mContentValues.put(EpisodesColumns.FIELD_EPISODE_DESCRIPTION, value);
        return this;
    }


    /**
     * url download path
     */
    public EpisodesContentValues putFieldDownloadUrl(@NonNull String value) {
        if (value == null) throw new IllegalArgumentException("fieldDownloadUrl must not be null");
        mContentValues.put(EpisodesColumns.FIELD_DOWNLOAD_URL, value);
        return this;
    }


    /**
     * episode rating
     */
    public EpisodesContentValues putFieldRating(@Nullable Integer value) {
        mContentValues.put(EpisodesColumns.FIELD_RATING, value);
        return this;
    }

    public EpisodesContentValues putFieldRatingNull() {
        mContentValues.putNull(EpisodesColumns.FIELD_RATING);
        return this;
    }
}
