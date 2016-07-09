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
package com.harlie.radiotheater.radiomysterytheater.data.writersepisodes;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.harlie.radiotheater.radiomysterytheater.data.base.AbstractContentValues;

/**
 * Content values wrapper for the {@code writers_episodes} table.
 */
public class WritersEpisodesContentValues extends AbstractContentValues {
    @Override
    public Uri uri() {
        return WritersEpisodesColumns.CONTENT_URI;
    }

    /**
     * Update row(s) using the values stored by this object and the given selection.
     *
     * @param contentResolver The content resolver to use.
     * @param where The selection to use (can be {@code null}).
     */
    public int update(ContentResolver contentResolver, @Nullable WritersEpisodesSelection where) {
        return contentResolver.update(uri(), values(), where == null ? null : where.sel(), where == null ? null : where.args());
    }

    /**
     * Update row(s) using the values stored by this object and the given selection.
     *
     * @param contentResolver The content resolver to use.
     * @param where The selection to use (can be {@code null}).
     */
    public int update(Context context, @Nullable WritersEpisodesSelection where) {
        return context.getContentResolver().update(uri(), values(), where == null ? null : where.sel(), where == null ? null : where.args());
    }

    public WritersEpisodesContentValues putFieldWriterId(long value) {
        mContentValues.put(WritersEpisodesColumns.FIELD_WRITER_ID, value);
        return this;
    }


    public WritersEpisodesContentValues putFieldWriterName(@NonNull String value) {
        if (value == null) throw new IllegalArgumentException("fieldWriterName must not be null");
        mContentValues.put(WritersEpisodesColumns.FIELD_WRITER_NAME, value);
        return this;
    }


    public WritersEpisodesContentValues putFieldEpisodeNumber(long value) {
        mContentValues.put(WritersEpisodesColumns.FIELD_EPISODE_NUMBER, value);
        return this;
    }

}
