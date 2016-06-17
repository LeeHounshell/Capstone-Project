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

import android.content.Context;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import com.harlie.radiotheater.radiomysterytheater.data.base.AbstractSelection;

/**
 * Selection for the {@code config_episodes} table.
 */
public class ConfigEpisodesSelection extends AbstractSelection<ConfigEpisodesSelection> {
    @Override
    protected Uri baseUri() {
        return ConfigEpisodesColumns.CONTENT_URI;
    }

    /**
     * Query the given content resolver using this selection.
     *
     * @param contentResolver The content resolver to query.
     * @param projection A list of which columns to return. Passing null will return all columns, which is inefficient.
     * @return A {@code ConfigEpisodesCursor} object, which is positioned before the first entry, or null.
     */
    public ConfigEpisodesCursor query(ContentResolver contentResolver, String[] projection) {
        Cursor cursor = contentResolver.query(uri(), projection, sel(), args(), order());
        if (cursor == null) return null;
        return new ConfigEpisodesCursor(cursor);
    }

    /**
     * Equivalent of calling {@code query(contentResolver, null)}.
     */
    public ConfigEpisodesCursor query(ContentResolver contentResolver) {
        return query(contentResolver, null);
    }

    /**
     * Query the given content resolver using this selection.
     *
     * @param context The context to use for the query.
     * @param projection A list of which columns to return. Passing null will return all columns, which is inefficient.
     * @return A {@code ConfigEpisodesCursor} object, which is positioned before the first entry, or null.
     */
    public ConfigEpisodesCursor query(Context context, String[] projection) {
        Cursor cursor = context.getContentResolver().query(uri(), projection, sel(), args(), order());
        if (cursor == null) return null;
        return new ConfigEpisodesCursor(cursor);
    }

    /**
     * Equivalent of calling {@code query(context, null)}.
     */
    public ConfigEpisodesCursor query(Context context) {
        return query(context, null);
    }


    public ConfigEpisodesSelection id(long... value) {
        addEquals("config_episodes." + ConfigEpisodesColumns._ID, toObjectArray(value));
        return this;
    }

    public ConfigEpisodesSelection idNot(long... value) {
        addNotEquals("config_episodes." + ConfigEpisodesColumns._ID, toObjectArray(value));
        return this;
    }

    public ConfigEpisodesSelection orderById(boolean desc) {
        orderBy("config_episodes." + ConfigEpisodesColumns._ID, desc);
        return this;
    }

    public ConfigEpisodesSelection orderById() {
        return orderById(false);
    }

    public ConfigEpisodesSelection fieldEpisodeNumber(long... value) {
        addEquals(ConfigEpisodesColumns.FIELD_EPISODE_NUMBER, toObjectArray(value));
        return this;
    }

    public ConfigEpisodesSelection fieldEpisodeNumberNot(long... value) {
        addNotEquals(ConfigEpisodesColumns.FIELD_EPISODE_NUMBER, toObjectArray(value));
        return this;
    }

    public ConfigEpisodesSelection fieldEpisodeNumberGt(long value) {
        addGreaterThan(ConfigEpisodesColumns.FIELD_EPISODE_NUMBER, value);
        return this;
    }

    public ConfigEpisodesSelection fieldEpisodeNumberGtEq(long value) {
        addGreaterThanOrEquals(ConfigEpisodesColumns.FIELD_EPISODE_NUMBER, value);
        return this;
    }

    public ConfigEpisodesSelection fieldEpisodeNumberLt(long value) {
        addLessThan(ConfigEpisodesColumns.FIELD_EPISODE_NUMBER, value);
        return this;
    }

    public ConfigEpisodesSelection fieldEpisodeNumberLtEq(long value) {
        addLessThanOrEquals(ConfigEpisodesColumns.FIELD_EPISODE_NUMBER, value);
        return this;
    }

    public ConfigEpisodesSelection orderByFieldEpisodeNumber(boolean desc) {
        orderBy(ConfigEpisodesColumns.FIELD_EPISODE_NUMBER, desc);
        return this;
    }

    public ConfigEpisodesSelection orderByFieldEpisodeNumber() {
        orderBy(ConfigEpisodesColumns.FIELD_EPISODE_NUMBER, false);
        return this;
    }

    public ConfigEpisodesSelection fieldPurchasedAccess(boolean value) {
        addEquals(ConfigEpisodesColumns.FIELD_PURCHASED_ACCESS, toObjectArray(value));
        return this;
    }

    public ConfigEpisodesSelection orderByFieldPurchasedAccess(boolean desc) {
        orderBy(ConfigEpisodesColumns.FIELD_PURCHASED_ACCESS, desc);
        return this;
    }

    public ConfigEpisodesSelection orderByFieldPurchasedAccess() {
        orderBy(ConfigEpisodesColumns.FIELD_PURCHASED_ACCESS, false);
        return this;
    }

    public ConfigEpisodesSelection fieldPurchasedNoads(boolean value) {
        addEquals(ConfigEpisodesColumns.FIELD_PURCHASED_NOADS, toObjectArray(value));
        return this;
    }

    public ConfigEpisodesSelection orderByFieldPurchasedNoads(boolean desc) {
        orderBy(ConfigEpisodesColumns.FIELD_PURCHASED_NOADS, desc);
        return this;
    }

    public ConfigEpisodesSelection orderByFieldPurchasedNoads() {
        orderBy(ConfigEpisodesColumns.FIELD_PURCHASED_NOADS, false);
        return this;
    }

    public ConfigEpisodesSelection fieldEpisodeDownloaded(boolean value) {
        addEquals(ConfigEpisodesColumns.FIELD_EPISODE_DOWNLOADED, toObjectArray(value));
        return this;
    }

    public ConfigEpisodesSelection orderByFieldEpisodeDownloaded(boolean desc) {
        orderBy(ConfigEpisodesColumns.FIELD_EPISODE_DOWNLOADED, desc);
        return this;
    }

    public ConfigEpisodesSelection orderByFieldEpisodeDownloaded() {
        orderBy(ConfigEpisodesColumns.FIELD_EPISODE_DOWNLOADED, false);
        return this;
    }

    public ConfigEpisodesSelection fieldEpisodeHeard(boolean value) {
        addEquals(ConfigEpisodesColumns.FIELD_EPISODE_HEARD, toObjectArray(value));
        return this;
    }

    public ConfigEpisodesSelection orderByFieldEpisodeHeard(boolean desc) {
        orderBy(ConfigEpisodesColumns.FIELD_EPISODE_HEARD, desc);
        return this;
    }

    public ConfigEpisodesSelection orderByFieldEpisodeHeard() {
        orderBy(ConfigEpisodesColumns.FIELD_EPISODE_HEARD, false);
        return this;
    }

    public ConfigEpisodesSelection fieldListenCount(int... value) {
        addEquals(ConfigEpisodesColumns.FIELD_LISTEN_COUNT, toObjectArray(value));
        return this;
    }

    public ConfigEpisodesSelection fieldListenCountNot(int... value) {
        addNotEquals(ConfigEpisodesColumns.FIELD_LISTEN_COUNT, toObjectArray(value));
        return this;
    }

    public ConfigEpisodesSelection fieldListenCountGt(int value) {
        addGreaterThan(ConfigEpisodesColumns.FIELD_LISTEN_COUNT, value);
        return this;
    }

    public ConfigEpisodesSelection fieldListenCountGtEq(int value) {
        addGreaterThanOrEquals(ConfigEpisodesColumns.FIELD_LISTEN_COUNT, value);
        return this;
    }

    public ConfigEpisodesSelection fieldListenCountLt(int value) {
        addLessThan(ConfigEpisodesColumns.FIELD_LISTEN_COUNT, value);
        return this;
    }

    public ConfigEpisodesSelection fieldListenCountLtEq(int value) {
        addLessThanOrEquals(ConfigEpisodesColumns.FIELD_LISTEN_COUNT, value);
        return this;
    }

    public ConfigEpisodesSelection orderByFieldListenCount(boolean desc) {
        orderBy(ConfigEpisodesColumns.FIELD_LISTEN_COUNT, desc);
        return this;
    }

    public ConfigEpisodesSelection orderByFieldListenCount() {
        orderBy(ConfigEpisodesColumns.FIELD_LISTEN_COUNT, false);
        return this;
    }
}
