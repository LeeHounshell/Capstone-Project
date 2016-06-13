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

import java.util.Date;

import android.content.Context;
import android.content.ContentResolver;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.harlie.radiotheater.radiomysterytheater.data.base.AbstractContentValues;

/**
 * Content values wrapper for the {@code actors} table.
 */
public class ActorsContentValues extends AbstractContentValues {
    @Override
    public Uri uri() {
        return ActorsColumns.CONTENT_URI;
    }

    /**
     * Update row(s) using the values stored by this object and the given selection.
     *
     * @param contentResolver The content resolver to use.
     * @param where The selection to use (can be {@code null}).
     */
    public int update(ContentResolver contentResolver, @Nullable ActorsSelection where) {
        return contentResolver.update(uri(), values(), where == null ? null : where.sel(), where == null ? null : where.args());
    }

    /**
     * Update row(s) using the values stored by this object and the given selection.
     *
     * @param contentResolver The content resolver to use.
     * @param where The selection to use (can be {@code null}).
     */
    public int update(Context context, @Nullable ActorsSelection where) {
        return context.getContentResolver().update(uri(), values(), where == null ? null : where.sel(), where == null ? null : where.args());
    }

    public ActorsContentValues putFieldActorId(long value) {
        mContentValues.put(ActorsColumns.FIELD_ACTOR_ID, value);
        return this;
    }


    /**
     * actor full name
     */
    public ActorsContentValues putFieldActorName(@NonNull String value) {
        if (value == null) throw new IllegalArgumentException("fieldActorName must not be null");
        mContentValues.put(ActorsColumns.FIELD_ACTOR_NAME, value);
        return this;
    }


    /**
     * actor photo URL
     */
    public ActorsContentValues putFieldActorUrl(@NonNull String value) {
        if (value == null) throw new IllegalArgumentException("fieldActorUrl must not be null");
        mContentValues.put(ActorsColumns.FIELD_ACTOR_URL, value);
        return this;
    }


    /**
     * actor biography
     */
    public ActorsContentValues putFieldActorBio(@Nullable String value) {
        mContentValues.put(ActorsColumns.FIELD_ACTOR_BIO, value);
        return this;
    }

    public ActorsContentValues putFieldActorBioNull() {
        mContentValues.putNull(ActorsColumns.FIELD_ACTOR_BIO);
        return this;
    }
}
