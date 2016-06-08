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
package com.harlie.radiotheater.radiomysterytheater.data.writersepisodes;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.harlie.radiotheater.radiomysterytheater.data.base.AbstractSelection;

/**
 * Selection for the {@code writers_episodes} table.
 */
public class WritersEpisodesSelection extends AbstractSelection<WritersEpisodesSelection> {
    @Override
    protected Uri baseUri() {
        return WritersEpisodesColumns.CONTENT_URI;
    }

    /**
     * Query the given content resolver using this selection.
     *
     * @param contentResolver The content resolver to query.
     * @param projection A list of which columns to return. Passing null will return all columns, which is inefficient.
     * @return A {@code WritersEpisodesCursor} object, which is positioned before the first entry, or null.
     */
    public WritersEpisodesCursor query(ContentResolver contentResolver, String[] projection) {
        Cursor cursor = contentResolver.query(uri(), projection, sel(), args(), order());
        if (cursor == null) return null;
        return new WritersEpisodesCursor(cursor);
    }

    /**
     * Equivalent of calling {@code query(contentResolver, null)}.
     */
    public WritersEpisodesCursor query(ContentResolver contentResolver) {
        return query(contentResolver, null);
    }

    /**
     * Query the given content resolver using this selection.
     *
     * @param context The context to use for the query.
     * @param projection A list of which columns to return. Passing null will return all columns, which is inefficient.
     * @return A {@code WritersEpisodesCursor} object, which is positioned before the first entry, or null.
     */
    public WritersEpisodesCursor query(Context context, String[] projection) {
        Cursor cursor = context.getContentResolver().query(uri(), projection, sel(), args(), order());
        if (cursor == null) return null;
        return new WritersEpisodesCursor(cursor);
    }

    /**
     * Equivalent of calling {@code query(context, null)}.
     */
    public WritersEpisodesCursor query(Context context) {
        return query(context, null);
    }


    public WritersEpisodesSelection id(long... value) {
        addEquals("writers_episodes." + WritersEpisodesColumns._ID, toObjectArray(value));
        return this;
    }

    public WritersEpisodesSelection idNot(long... value) {
        addNotEquals("writers_episodes." + WritersEpisodesColumns._ID, toObjectArray(value));
        return this;
    }

    public WritersEpisodesSelection orderById(boolean desc) {
        orderBy("writers_episodes." + WritersEpisodesColumns._ID, desc);
        return this;
    }

    public WritersEpisodesSelection orderById() {
        return orderById(false);
    }

    public WritersEpisodesSelection fieldWriterId(long... value) {
        addEquals(WritersEpisodesColumns.FIELD_WRITER_ID, toObjectArray(value));
        return this;
    }

    public WritersEpisodesSelection fieldWriterIdNot(long... value) {
        addNotEquals(WritersEpisodesColumns.FIELD_WRITER_ID, toObjectArray(value));
        return this;
    }

    public WritersEpisodesSelection fieldWriterIdGt(long value) {
        addGreaterThan(WritersEpisodesColumns.FIELD_WRITER_ID, value);
        return this;
    }

    public WritersEpisodesSelection fieldWriterIdGtEq(long value) {
        addGreaterThanOrEquals(WritersEpisodesColumns.FIELD_WRITER_ID, value);
        return this;
    }

    public WritersEpisodesSelection fieldWriterIdLt(long value) {
        addLessThan(WritersEpisodesColumns.FIELD_WRITER_ID, value);
        return this;
    }

    public WritersEpisodesSelection fieldWriterIdLtEq(long value) {
        addLessThanOrEquals(WritersEpisodesColumns.FIELD_WRITER_ID, value);
        return this;
    }

    public WritersEpisodesSelection orderByFieldWriterId(boolean desc) {
        orderBy(WritersEpisodesColumns.FIELD_WRITER_ID, desc);
        return this;
    }

    public WritersEpisodesSelection orderByFieldWriterId() {
        orderBy(WritersEpisodesColumns.FIELD_WRITER_ID, false);
        return this;
    }

    public WritersEpisodesSelection fieldEpisodeNumber(long... value) {
        addEquals(WritersEpisodesColumns.FIELD_EPISODE_NUMBER, toObjectArray(value));
        return this;
    }

    public WritersEpisodesSelection fieldEpisodeNumberNot(long... value) {
        addNotEquals(WritersEpisodesColumns.FIELD_EPISODE_NUMBER, toObjectArray(value));
        return this;
    }

    public WritersEpisodesSelection fieldEpisodeNumberGt(long value) {
        addGreaterThan(WritersEpisodesColumns.FIELD_EPISODE_NUMBER, value);
        return this;
    }

    public WritersEpisodesSelection fieldEpisodeNumberGtEq(long value) {
        addGreaterThanOrEquals(WritersEpisodesColumns.FIELD_EPISODE_NUMBER, value);
        return this;
    }

    public WritersEpisodesSelection fieldEpisodeNumberLt(long value) {
        addLessThan(WritersEpisodesColumns.FIELD_EPISODE_NUMBER, value);
        return this;
    }

    public WritersEpisodesSelection fieldEpisodeNumberLtEq(long value) {
        addLessThanOrEquals(WritersEpisodesColumns.FIELD_EPISODE_NUMBER, value);
        return this;
    }

    public WritersEpisodesSelection orderByFieldEpisodeNumber(boolean desc) {
        orderBy(WritersEpisodesColumns.FIELD_EPISODE_NUMBER, desc);
        return this;
    }

    public WritersEpisodesSelection orderByFieldEpisodeNumber() {
        orderBy(WritersEpisodesColumns.FIELD_EPISODE_NUMBER, false);
        return this;
    }
}
