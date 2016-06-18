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
package com.harlie.radiotheater.radiomysterytheater.data.episodeswriters;

import java.util.Date;

import android.content.Context;
import android.content.ContentResolver;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.harlie.radiotheater.radiomysterytheater.data.base.AbstractContentValues;

/**
 * Content values wrapper for the {@code episodes_writers} table.
 */
public class EpisodesWritersContentValues extends AbstractContentValues {
    @Override
    public Uri uri() {
        return EpisodesWritersColumns.CONTENT_URI;
    }

    /**
     * Update row(s) using the values stored by this object and the given selection.
     *
     * @param contentResolver The content resolver to use.
     * @param where The selection to use (can be {@code null}).
     */
    public int update(ContentResolver contentResolver, @Nullable EpisodesWritersSelection where) {
        return contentResolver.update(uri(), values(), where == null ? null : where.sel(), where == null ? null : where.args());
    }

    /**
     * Update row(s) using the values stored by this object and the given selection.
     *
     * @param contentResolver The content resolver to use.
     * @param where The selection to use (can be {@code null}).
     */
    public int update(Context context, @Nullable EpisodesWritersSelection where) {
        return context.getContentResolver().update(uri(), values(), where == null ? null : where.sel(), where == null ? null : where.args());
    }

    public EpisodesWritersContentValues putFieldEpisodeNumber(long value) {
        mContentValues.put(EpisodesWritersColumns.FIELD_EPISODE_NUMBER, value);
        return this;
    }


    public EpisodesWritersContentValues putFieldWriterId(long value) {
        mContentValues.put(EpisodesWritersColumns.FIELD_WRITER_ID, value);
        return this;
    }


    public EpisodesWritersContentValues putFieldWriterName(@NonNull String value) {
        if (value == null) throw new IllegalArgumentException("fieldWriterName must not be null");
        mContentValues.put(EpisodesWritersColumns.FIELD_WRITER_NAME, value);
        return this;
    }

}
