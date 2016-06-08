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
package com.harlie.radiotheater.radiomysterytheater.data.configepisodes;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.harlie.radiotheater.radiomysterytheater.data.base.AbstractContentValues;

/**
 * Content values wrapper for the {@code config_episodes} table.
 */
public class ConfigEpisodesContentValues extends AbstractContentValues {
    @Override
    public Uri uri() {
        return ConfigEpisodesColumns.CONTENT_URI;
    }

    /**
     * Update row(s) using the values stored by this object and the given selection.
     *
     * @param contentResolver The content resolver to use.
     * @param where The selection to use (can be {@code null}).
     */
    public int update(ContentResolver contentResolver, @Nullable ConfigEpisodesSelection where) {
        return contentResolver.update(uri(), values(), where == null ? null : where.sel(), where == null ? null : where.args());
    }

    /**
     * Update row(s) using the values stored by this object and the given selection.
     *
     * @param contentResolver The content resolver to use.
     * @param where The selection to use (can be {@code null}).
     */
    public int update(Context context, @Nullable ConfigEpisodesSelection where) {
        return context.getContentResolver().update(uri(), values(), where == null ? null : where.sel(), where == null ? null : where.args());
    }

    public ConfigEpisodesContentValues putFieldEpisodeNumber(long value) {
        mContentValues.put(ConfigEpisodesColumns.FIELD_EPISODE_NUMBER, value);
        return this;
    }


    public ConfigEpisodesContentValues putFieldPurchaseAccess(boolean value) {
        mContentValues.put(ConfigEpisodesColumns.FIELD_PURCHASE_ACCESS, value);
        return this;
    }


    public ConfigEpisodesContentValues putFieldPurchaseNoads(boolean value) {
        mContentValues.put(ConfigEpisodesColumns.FIELD_PURCHASE_NOADS, value);
        return this;
    }


    public ConfigEpisodesContentValues putFieldEpisodePermision(boolean value) {
        mContentValues.put(ConfigEpisodesColumns.FIELD_EPISODE_PERMISION, value);
        return this;
    }


    public ConfigEpisodesContentValues putFieldEpisodeHeard(boolean value) {
        mContentValues.put(ConfigEpisodesColumns.FIELD_EPISODE_HEARD, value);
        return this;
    }


    public ConfigEpisodesContentValues putFieldListenCount(int value) {
        mContentValues.put(ConfigEpisodesColumns.FIELD_LISTEN_COUNT, value);
        return this;
    }

}
