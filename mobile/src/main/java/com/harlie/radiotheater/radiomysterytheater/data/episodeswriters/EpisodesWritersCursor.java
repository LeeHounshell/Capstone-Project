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

import android.database.Cursor;
import android.support.annotation.NonNull;

import com.harlie.radiotheater.radiomysterytheater.data.base.AbstractCursor;

/**
 * Cursor wrapper for the {@code episodes_writers} table.
 */
public class EpisodesWritersCursor extends AbstractCursor implements EpisodesWritersModel {
    public EpisodesWritersCursor(Cursor cursor) {
        super(cursor);
    }

    /**
     * Primary key.
     */
    public long getId() {
        Long res = getLongOrNull(EpisodesWritersColumns._ID);
        if (res == null)
            throw new NullPointerException("The value of '_id' in the database was null, which is not allowed according to the model definition");
        return res;
    }

    /**
     * Get the {@code field_episode_number} value.
     */
    public long getFieldEpisodeNumber() {
        Long res = getLongOrNull(EpisodesWritersColumns.FIELD_EPISODE_NUMBER);
        if (res == null)
            throw new NullPointerException("The value of 'field_episode_number' in the database was null, which is not allowed according to the model definition");
        return res;
    }

    /**
     * Get the {@code field_writer_id} value.
     */
    public long getFieldWriterId() {
        Long res = getLongOrNull(EpisodesWritersColumns.FIELD_WRITER_ID);
        if (res == null)
            throw new NullPointerException("The value of 'field_writer_id' in the database was null, which is not allowed according to the model definition");
        return res;
    }

    /**
     * Get the {@code field_writer_name} value.
     * Cannot be {@code null}.
     */
    @NonNull
    public String getFieldWriterName() {
        String res = getStringOrNull(EpisodesWritersColumns.FIELD_WRITER_NAME);
        if (res == null)
            throw new NullPointerException("The value of 'field_writer_name' in the database was null, which is not allowed according to the model definition");
        return res;
    }
}
