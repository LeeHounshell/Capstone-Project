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

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.harlie.radiotheater.radiomysterytheater.data.base.AbstractContentValues;

/**
 * Content values wrapper for the {@code episodes_actors} table.
 */
public class EpisodesActorsContentValues extends AbstractContentValues {
    @Override
    public Uri uri() {
        return EpisodesActorsColumns.CONTENT_URI;
    }

    /**
     * Update row(s) using the values stored by this object and the given selection.
     *
     * @param contentResolver The content resolver to use.
     * @param where The selection to use (can be {@code null}).
     */
    public int update(ContentResolver contentResolver, @Nullable EpisodesActorsSelection where) {
        return contentResolver.update(uri(), values(), where == null ? null : where.sel(), where == null ? null : where.args());
    }

    /**
     * Update row(s) using the values stored by this object and the given selection.
     *
     * @param contentResolver The content resolver to use.
     * @param where The selection to use (can be {@code null}).
     */
    public int update(Context context, @Nullable EpisodesActorsSelection where) {
        return context.getContentResolver().update(uri(), values(), where == null ? null : where.sel(), where == null ? null : where.args());
    }

    public EpisodesActorsContentValues putFieldEpisodeNumber(long value) {
        mContentValues.put(EpisodesActorsColumns.FIELD_EPISODE_NUMBER, value);
        return this;
    }


    public EpisodesActorsContentValues putFieldActorId(long value) {
        mContentValues.put(EpisodesActorsColumns.FIELD_ACTOR_ID, value);
        return this;
    }


    public EpisodesActorsContentValues putFieldActorName(@NonNull String value) {
        if (value == null) throw new IllegalArgumentException("fieldActorName must not be null");
        mContentValues.put(EpisodesActorsColumns.FIELD_ACTOR_NAME, value);
        return this;
    }

}
