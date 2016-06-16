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
package com.harlie.radiotheater.radiomysterytheater.data.actorsepisodes;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.harlie.radiotheater.radiomysterytheater.data.base.AbstractContentValues;

/**
 * Content values wrapper for the {@code actors_episodes} table.
 */
public class ActorsEpisodesContentValues extends AbstractContentValues {
    @Override
    public Uri uri() {
        return ActorsEpisodesColumns.CONTENT_URI;
    }

    /**
     * Update row(s) using the values stored by this object and the given selection.
     *
     * @param contentResolver The content resolver to use.
     * @param where The selection to use (can be {@code null}).
     */
    public int update(ContentResolver contentResolver, @Nullable ActorsEpisodesSelection where) {
        return contentResolver.update(uri(), values(), where == null ? null : where.sel(), where == null ? null : where.args());
    }

    /**
     * Update row(s) using the values stored by this object and the given selection.
     *
     * @param contentResolver The content resolver to use.
     * @param where The selection to use (can be {@code null}).
     */
    public int update(Context context, @Nullable ActorsEpisodesSelection where) {
        return context.getContentResolver().update(uri(), values(), where == null ? null : where.sel(), where == null ? null : where.args());
    }

    public ActorsEpisodesContentValues putFieldActorId(long value) {
        mContentValues.put(ActorsEpisodesColumns.FIELD_ACTOR_ID, value);
        return this;
    }


    public ActorsEpisodesContentValues putFieldEpisodeNumber(long value) {
        mContentValues.put(ActorsEpisodesColumns.FIELD_EPISODE_NUMBER, value);
        return this;
    }

}
