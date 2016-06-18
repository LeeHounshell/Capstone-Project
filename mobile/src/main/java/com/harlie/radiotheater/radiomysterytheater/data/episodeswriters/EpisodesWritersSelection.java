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
package com.harlie.radiotheater.radiomysterytheater.data.episodeswriters;

import java.util.Date;

import android.content.Context;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import com.harlie.radiotheater.radiomysterytheater.data.base.AbstractSelection;

/**
 * Selection for the {@code episodes_writers} table.
 */
public class EpisodesWritersSelection extends AbstractSelection<EpisodesWritersSelection> {
    @Override
    protected Uri baseUri() {
        return EpisodesWritersColumns.CONTENT_URI;
    }

    /**
     * Query the given content resolver using this selection.
     *
     * @param contentResolver The content resolver to query.
     * @param projection A list of which columns to return. Passing null will return all columns, which is inefficient.
     * @return A {@code EpisodesWritersCursor} object, which is positioned before the first entry, or null.
     */
    public EpisodesWritersCursor query(ContentResolver contentResolver, String[] projection) {
        Cursor cursor = contentResolver.query(uri(), projection, sel(), args(), order());
        if (cursor == null) return null;
        return new EpisodesWritersCursor(cursor);
    }

    /**
     * Equivalent of calling {@code query(contentResolver, null)}.
     */
    public EpisodesWritersCursor query(ContentResolver contentResolver) {
        return query(contentResolver, null);
    }

    /**
     * Query the given content resolver using this selection.
     *
     * @param context The context to use for the query.
     * @param projection A list of which columns to return. Passing null will return all columns, which is inefficient.
     * @return A {@code EpisodesWritersCursor} object, which is positioned before the first entry, or null.
     */
    public EpisodesWritersCursor query(Context context, String[] projection) {
        Cursor cursor = context.getContentResolver().query(uri(), projection, sel(), args(), order());
        if (cursor == null) return null;
        return new EpisodesWritersCursor(cursor);
    }

    /**
     * Equivalent of calling {@code query(context, null)}.
     */
    public EpisodesWritersCursor query(Context context) {
        return query(context, null);
    }


    public EpisodesWritersSelection id(long... value) {
        addEquals("episodes_writers." + EpisodesWritersColumns._ID, toObjectArray(value));
        return this;
    }

    public EpisodesWritersSelection idNot(long... value) {
        addNotEquals("episodes_writers." + EpisodesWritersColumns._ID, toObjectArray(value));
        return this;
    }

    public EpisodesWritersSelection orderById(boolean desc) {
        orderBy("episodes_writers." + EpisodesWritersColumns._ID, desc);
        return this;
    }

    public EpisodesWritersSelection orderById() {
        return orderById(false);
    }

    public EpisodesWritersSelection fieldEpisodeNumber(long... value) {
        addEquals(EpisodesWritersColumns.FIELD_EPISODE_NUMBER, toObjectArray(value));
        return this;
    }

    public EpisodesWritersSelection fieldEpisodeNumberNot(long... value) {
        addNotEquals(EpisodesWritersColumns.FIELD_EPISODE_NUMBER, toObjectArray(value));
        return this;
    }

    public EpisodesWritersSelection fieldEpisodeNumberGt(long value) {
        addGreaterThan(EpisodesWritersColumns.FIELD_EPISODE_NUMBER, value);
        return this;
    }

    public EpisodesWritersSelection fieldEpisodeNumberGtEq(long value) {
        addGreaterThanOrEquals(EpisodesWritersColumns.FIELD_EPISODE_NUMBER, value);
        return this;
    }

    public EpisodesWritersSelection fieldEpisodeNumberLt(long value) {
        addLessThan(EpisodesWritersColumns.FIELD_EPISODE_NUMBER, value);
        return this;
    }

    public EpisodesWritersSelection fieldEpisodeNumberLtEq(long value) {
        addLessThanOrEquals(EpisodesWritersColumns.FIELD_EPISODE_NUMBER, value);
        return this;
    }

    public EpisodesWritersSelection orderByFieldEpisodeNumber(boolean desc) {
        orderBy(EpisodesWritersColumns.FIELD_EPISODE_NUMBER, desc);
        return this;
    }

    public EpisodesWritersSelection orderByFieldEpisodeNumber() {
        orderBy(EpisodesWritersColumns.FIELD_EPISODE_NUMBER, false);
        return this;
    }

    public EpisodesWritersSelection fieldWriterId(long... value) {
        addEquals(EpisodesWritersColumns.FIELD_WRITER_ID, toObjectArray(value));
        return this;
    }

    public EpisodesWritersSelection fieldWriterIdNot(long... value) {
        addNotEquals(EpisodesWritersColumns.FIELD_WRITER_ID, toObjectArray(value));
        return this;
    }

    public EpisodesWritersSelection fieldWriterIdGt(long value) {
        addGreaterThan(EpisodesWritersColumns.FIELD_WRITER_ID, value);
        return this;
    }

    public EpisodesWritersSelection fieldWriterIdGtEq(long value) {
        addGreaterThanOrEquals(EpisodesWritersColumns.FIELD_WRITER_ID, value);
        return this;
    }

    public EpisodesWritersSelection fieldWriterIdLt(long value) {
        addLessThan(EpisodesWritersColumns.FIELD_WRITER_ID, value);
        return this;
    }

    public EpisodesWritersSelection fieldWriterIdLtEq(long value) {
        addLessThanOrEquals(EpisodesWritersColumns.FIELD_WRITER_ID, value);
        return this;
    }

    public EpisodesWritersSelection orderByFieldWriterId(boolean desc) {
        orderBy(EpisodesWritersColumns.FIELD_WRITER_ID, desc);
        return this;
    }

    public EpisodesWritersSelection orderByFieldWriterId() {
        orderBy(EpisodesWritersColumns.FIELD_WRITER_ID, false);
        return this;
    }

    public EpisodesWritersSelection fieldWriterName(String... value) {
        addEquals(EpisodesWritersColumns.FIELD_WRITER_NAME, value);
        return this;
    }

    public EpisodesWritersSelection fieldWriterNameNot(String... value) {
        addNotEquals(EpisodesWritersColumns.FIELD_WRITER_NAME, value);
        return this;
    }

    public EpisodesWritersSelection fieldWriterNameLike(String... value) {
        addLike(EpisodesWritersColumns.FIELD_WRITER_NAME, value);
        return this;
    }

    public EpisodesWritersSelection fieldWriterNameContains(String... value) {
        addContains(EpisodesWritersColumns.FIELD_WRITER_NAME, value);
        return this;
    }

    public EpisodesWritersSelection fieldWriterNameStartsWith(String... value) {
        addStartsWith(EpisodesWritersColumns.FIELD_WRITER_NAME, value);
        return this;
    }

    public EpisodesWritersSelection fieldWriterNameEndsWith(String... value) {
        addEndsWith(EpisodesWritersColumns.FIELD_WRITER_NAME, value);
        return this;
    }

    public EpisodesWritersSelection orderByFieldWriterName(boolean desc) {
        orderBy(EpisodesWritersColumns.FIELD_WRITER_NAME, desc);
        return this;
    }

    public EpisodesWritersSelection orderByFieldWriterName() {
        orderBy(EpisodesWritersColumns.FIELD_WRITER_NAME, false);
        return this;
    }
}
