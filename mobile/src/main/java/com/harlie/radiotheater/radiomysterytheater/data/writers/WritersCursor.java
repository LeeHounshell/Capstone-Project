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
package com.harlie.radiotheater.radiomysterytheater.data.writers;

import java.util.Date;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.harlie.radiotheater.radiomysterytheater.data.base.AbstractCursor;
import com.harlie.radiotheater.radiomysterytheater.data.writersepisodes.*;

/**
 * Cursor wrapper for the {@code writers} table.
 */
public class WritersCursor extends AbstractCursor implements WritersModel {
    public WritersCursor(Cursor cursor) {
        super(cursor);
    }

    /**
     * Primary key.
     */
    public long getId() {
        Long res = getLongOrNull(WritersColumns._ID);
        if (res == null)
            throw new NullPointerException("The value of '_id' in the database was null, which is not allowed according to the model definition");
        return res;
    }

    /**
     * Get the {@code field_writer_id} value.
     */
    public long getFieldWriterId() {
        Long res = getLongOrNull(WritersColumns.FIELD_WRITER_ID);
        if (res == null)
            throw new NullPointerException("The value of 'field_writer_id' in the database was null, which is not allowed according to the model definition");
        return res;
    }

    /**
     * Get the {@code field_writer_id} value.
     */
    public long getWritersEpisodesFieldWriterId() {
        Long res = getLongOrNull(WritersEpisodesColumns.FIELD_WRITER_ID);
        if (res == null)
            throw new NullPointerException("The value of 'field_writer_id' in the database was null, which is not allowed according to the model definition");
        return res;
    }

    /**
     * Get the {@code field_episode_number} value.
     */
    public long getWritersEpisodesFieldEpisodeNumber() {
        Long res = getLongOrNull(WritersEpisodesColumns.FIELD_EPISODE_NUMBER);
        if (res == null)
            throw new NullPointerException("The value of 'field_episode_number' in the database was null, which is not allowed according to the model definition");
        return res;
    }

    /**
     * writer full name
     * Cannot be {@code null}.
     */
    @NonNull
    public String getFieldWriterName() {
        String res = getStringOrNull(WritersColumns.FIELD_WRITER_NAME);
        if (res == null)
            throw new NullPointerException("The value of 'field_writer_name' in the database was null, which is not allowed according to the model definition");
        return res;
    }

    /**
     * writer photo URL
     * Cannot be {@code null}.
     */
    @NonNull
    public String getFieldWriterUrl() {
        String res = getStringOrNull(WritersColumns.FIELD_WRITER_URL);
        if (res == null)
            throw new NullPointerException("The value of 'field_writer_url' in the database was null, which is not allowed according to the model definition");
        return res;
    }

    /**
     * writer biography
     * Can be {@code null}.
     */
    @Nullable
    public String getFieldWriterBio() {
        String res = getStringOrNull(WritersColumns.FIELD_WRITER_BIO);
        return res;
    }
}
