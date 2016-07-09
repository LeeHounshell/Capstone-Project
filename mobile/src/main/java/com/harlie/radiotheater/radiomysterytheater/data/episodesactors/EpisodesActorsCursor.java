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
package com.harlie.radiotheater.radiomysterytheater.data.episodesactors;

import android.database.Cursor;
import android.support.annotation.NonNull;

import com.harlie.radiotheater.radiomysterytheater.data.base.AbstractCursor;

/**
 * Cursor wrapper for the {@code episodes_actors} table.
 */
public class EpisodesActorsCursor extends AbstractCursor implements EpisodesActorsModel {
    public EpisodesActorsCursor(Cursor cursor) {
        super(cursor);
    }

    /**
     * Primary key.
     */
    public long getId() {
        Long res = getLongOrNull(EpisodesActorsColumns._ID);
        if (res == null)
            throw new NullPointerException("The value of '_id' in the database was null, which is not allowed according to the model definition");
        return res;
    }

    /**
     * Get the {@code field_episode_number} value.
     */
    public long getFieldEpisodeNumber() {
        Long res = getLongOrNull(EpisodesActorsColumns.FIELD_EPISODE_NUMBER);
        if (res == null)
            throw new NullPointerException("The value of 'field_episode_number' in the database was null, which is not allowed according to the model definition");
        return res;
    }

    /**
     * Get the {@code field_actor_id} value.
     */
    public long getFieldActorId() {
        Long res = getLongOrNull(EpisodesActorsColumns.FIELD_ACTOR_ID);
        if (res == null)
            throw new NullPointerException("The value of 'field_actor_id' in the database was null, which is not allowed according to the model definition");
        return res;
    }

    /**
     * Get the {@code field_actor_name} value.
     * Cannot be {@code null}.
     */
    @NonNull
    public String getFieldActorName() {
        String res = getStringOrNull(EpisodesActorsColumns.FIELD_ACTOR_NAME);
        if (res == null)
            throw new NullPointerException("The value of 'field_actor_name' in the database was null, which is not allowed according to the model definition");
        return res;
    }
}
