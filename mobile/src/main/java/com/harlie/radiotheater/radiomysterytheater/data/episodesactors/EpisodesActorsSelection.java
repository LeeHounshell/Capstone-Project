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
import android.database.Cursor;
import android.net.Uri;

import com.harlie.radiotheater.radiomysterytheater.data.base.AbstractSelection;

/**
 * Selection for the {@code episodes_actors} table.
 */
public class EpisodesActorsSelection extends AbstractSelection<EpisodesActorsSelection> {
    @Override
    protected Uri baseUri() {
        return EpisodesActorsColumns.CONTENT_URI;
    }

    /**
     * Query the given content resolver using this selection.
     *
     * @param contentResolver The content resolver to query.
     * @param projection A list of which columns to return. Passing null will return all columns, which is inefficient.
     * @return A {@code EpisodesActorsCursor} object, which is positioned before the first entry, or null.
     */
    public EpisodesActorsCursor query(ContentResolver contentResolver, String[] projection) {
        Cursor cursor = contentResolver.query(uri(), projection, sel(), args(), order());
        if (cursor == null) return null;
        return new EpisodesActorsCursor(cursor);
    }

    /**
     * Equivalent of calling {@code query(contentResolver, null)}.
     */
    public EpisodesActorsCursor query(ContentResolver contentResolver) {
        return query(contentResolver, null);
    }

    /**
     * Query the given content resolver using this selection.
     *
     * @param context The context to use for the query.
     * @param projection A list of which columns to return. Passing null will return all columns, which is inefficient.
     * @return A {@code EpisodesActorsCursor} object, which is positioned before the first entry, or null.
     */
    public EpisodesActorsCursor query(Context context, String[] projection) {
        Cursor cursor = context.getContentResolver().query(uri(), projection, sel(), args(), order());
        if (cursor == null) return null;
        return new EpisodesActorsCursor(cursor);
    }

    /**
     * Equivalent of calling {@code query(context, null)}.
     */
    public EpisodesActorsCursor query(Context context) {
        return query(context, null);
    }


    public EpisodesActorsSelection id(long... value) {
        addEquals("episodes_actors." + EpisodesActorsColumns._ID, toObjectArray(value));
        return this;
    }

    public EpisodesActorsSelection idNot(long... value) {
        addNotEquals("episodes_actors." + EpisodesActorsColumns._ID, toObjectArray(value));
        return this;
    }

    public EpisodesActorsSelection orderById(boolean desc) {
        orderBy("episodes_actors." + EpisodesActorsColumns._ID, desc);
        return this;
    }

    public EpisodesActorsSelection orderById() {
        return orderById(false);
    }

    public EpisodesActorsSelection fieldEpisodeNumber(long... value) {
        addEquals(EpisodesActorsColumns.FIELD_EPISODE_NUMBER, toObjectArray(value));
        return this;
    }

    public EpisodesActorsSelection fieldEpisodeNumberNot(long... value) {
        addNotEquals(EpisodesActorsColumns.FIELD_EPISODE_NUMBER, toObjectArray(value));
        return this;
    }

    public EpisodesActorsSelection fieldEpisodeNumberGt(long value) {
        addGreaterThan(EpisodesActorsColumns.FIELD_EPISODE_NUMBER, value);
        return this;
    }

    public EpisodesActorsSelection fieldEpisodeNumberGtEq(long value) {
        addGreaterThanOrEquals(EpisodesActorsColumns.FIELD_EPISODE_NUMBER, value);
        return this;
    }

    public EpisodesActorsSelection fieldEpisodeNumberLt(long value) {
        addLessThan(EpisodesActorsColumns.FIELD_EPISODE_NUMBER, value);
        return this;
    }

    public EpisodesActorsSelection fieldEpisodeNumberLtEq(long value) {
        addLessThanOrEquals(EpisodesActorsColumns.FIELD_EPISODE_NUMBER, value);
        return this;
    }

    public EpisodesActorsSelection orderByFieldEpisodeNumber(boolean desc) {
        orderBy(EpisodesActorsColumns.FIELD_EPISODE_NUMBER, desc);
        return this;
    }

    public EpisodesActorsSelection orderByFieldEpisodeNumber() {
        orderBy(EpisodesActorsColumns.FIELD_EPISODE_NUMBER, false);
        return this;
    }

    public EpisodesActorsSelection fieldWriterId(long... value) {
        addEquals(EpisodesActorsColumns.FIELD_WRITER_ID, toObjectArray(value));
        return this;
    }

    public EpisodesActorsSelection fieldWriterIdNot(long... value) {
        addNotEquals(EpisodesActorsColumns.FIELD_WRITER_ID, toObjectArray(value));
        return this;
    }

    public EpisodesActorsSelection fieldWriterIdGt(long value) {
        addGreaterThan(EpisodesActorsColumns.FIELD_WRITER_ID, value);
        return this;
    }

    public EpisodesActorsSelection fieldWriterIdGtEq(long value) {
        addGreaterThanOrEquals(EpisodesActorsColumns.FIELD_WRITER_ID, value);
        return this;
    }

    public EpisodesActorsSelection fieldWriterIdLt(long value) {
        addLessThan(EpisodesActorsColumns.FIELD_WRITER_ID, value);
        return this;
    }

    public EpisodesActorsSelection fieldWriterIdLtEq(long value) {
        addLessThanOrEquals(EpisodesActorsColumns.FIELD_WRITER_ID, value);
        return this;
    }

    public EpisodesActorsSelection orderByFieldWriterId(boolean desc) {
        orderBy(EpisodesActorsColumns.FIELD_WRITER_ID, desc);
        return this;
    }

    public EpisodesActorsSelection orderByFieldWriterId() {
        orderBy(EpisodesActorsColumns.FIELD_WRITER_ID, false);
        return this;
    }
}
