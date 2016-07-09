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
package com.harlie.radiotheater.radiomysterytheater.data.actors;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.harlie.radiotheater.radiomysterytheater.data.base.AbstractCursor;

/**
 * Cursor wrapper for the {@code actors} table.
 */
public class ActorsCursor extends AbstractCursor implements ActorsModel {
    public ActorsCursor(Cursor cursor) {
        super(cursor);
    }

    /**
     * Primary key.
     */
    public long getId() {
        Long res = getLongOrNull(ActorsColumns._ID);
        if (res == null)
            throw new NullPointerException("The value of '_id' in the database was null, which is not allowed according to the model definition");
        return res;
    }

    /**
     * Get the {@code field_actor_id} value.
     */
    public long getFieldActorId() {
        Long res = getLongOrNull(ActorsColumns.FIELD_ACTOR_ID);
        if (res == null)
            throw new NullPointerException("The value of 'field_actor_id' in the database was null, which is not allowed according to the model definition");
        return res;
    }

    /**
     * actor full name
     * Cannot be {@code null}.
     */
    @NonNull
    public String getFieldActorName() {
        String res = getStringOrNull(ActorsColumns.FIELD_ACTOR_NAME);
        if (res == null)
            throw new NullPointerException("The value of 'field_actor_name' in the database was null, which is not allowed according to the model definition");
        return res;
    }

    /**
     * actor photo URL
     * Cannot be {@code null}.
     */
    @NonNull
    public String getFieldActorUrl() {
        String res = getStringOrNull(ActorsColumns.FIELD_ACTOR_URL);
        if (res == null)
            throw new NullPointerException("The value of 'field_actor_url' in the database was null, which is not allowed according to the model definition");
        return res;
    }

    /**
     * actor biography
     * Can be {@code null}.
     */
    @Nullable
    public String getFieldActorBio() {
        String res = getStringOrNull(ActorsColumns.FIELD_ACTOR_BIO);
        return res;
    }
}
