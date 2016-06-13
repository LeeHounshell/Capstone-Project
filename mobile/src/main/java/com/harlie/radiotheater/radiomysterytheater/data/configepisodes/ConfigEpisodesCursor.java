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

import java.util.Date;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.harlie.radiotheater.radiomysterytheater.data.base.AbstractCursor;

/**
 * Cursor wrapper for the {@code config_episodes} table.
 */
public class ConfigEpisodesCursor extends AbstractCursor implements ConfigEpisodesModel {
    public ConfigEpisodesCursor(Cursor cursor) {
        super(cursor);
    }

    /**
     * Primary key.
     */
    public long getId() {
        Long res = getLongOrNull(ConfigEpisodesColumns._ID);
        if (res == null)
            throw new NullPointerException("The value of '_id' in the database was null, which is not allowed according to the model definition");
        return res;
    }

    /**
     * Get the {@code field_episode_number} value.
     */
    public long getFieldEpisodeNumber() {
        Long res = getLongOrNull(ConfigEpisodesColumns.FIELD_EPISODE_NUMBER);
        if (res == null)
            throw new NullPointerException("The value of 'field_episode_number' in the database was null, which is not allowed according to the model definition");
        return res;
    }

    /**
     * Get the {@code field_purchase_access} value.
     */
    public boolean getFieldPurchaseAccess() {
        Boolean res = getBooleanOrNull(ConfigEpisodesColumns.FIELD_PURCHASE_ACCESS);
        if (res == null)
            throw new NullPointerException("The value of 'field_purchase_access' in the database was null, which is not allowed according to the model definition");
        return res;
    }

    /**
     * Get the {@code field_purchase_noads} value.
     */
    public boolean getFieldPurchaseNoads() {
        Boolean res = getBooleanOrNull(ConfigEpisodesColumns.FIELD_PURCHASE_NOADS);
        if (res == null)
            throw new NullPointerException("The value of 'field_purchase_noads' in the database was null, which is not allowed according to the model definition");
        return res;
    }

    /**
     * Get the {@code field_episode_permision} value.
     */
    public boolean getFieldEpisodePermision() {
        Boolean res = getBooleanOrNull(ConfigEpisodesColumns.FIELD_EPISODE_PERMISION);
        if (res == null)
            throw new NullPointerException("The value of 'field_episode_permision' in the database was null, which is not allowed according to the model definition");
        return res;
    }

    /**
     * Get the {@code field_episode_heard} value.
     */
    public boolean getFieldEpisodeHeard() {
        Boolean res = getBooleanOrNull(ConfigEpisodesColumns.FIELD_EPISODE_HEARD);
        if (res == null)
            throw new NullPointerException("The value of 'field_episode_heard' in the database was null, which is not allowed according to the model definition");
        return res;
    }

    /**
     * Get the {@code field_listen_count} value.
     */
    public int getFieldListenCount() {
        Integer res = getIntegerOrNull(ConfigEpisodesColumns.FIELD_LISTEN_COUNT);
        if (res == null)
            throw new NullPointerException("The value of 'field_listen_count' in the database was null, which is not allowed according to the model definition");
        return res;
    }
}
