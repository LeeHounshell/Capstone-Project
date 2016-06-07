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
package com.harlie.radiotheater.radiomysterytheater.data.writers;

import java.util.Date;

import android.content.Context;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import com.harlie.radiotheater.radiomysterytheater.data.base.AbstractSelection;
import com.harlie.radiotheater.radiomysterytheater.data.writersepisodes.*;

/**
 * Selection for the {@code writers} table.
 */
public class WritersSelection extends AbstractSelection<WritersSelection> {
    @Override
    protected Uri baseUri() {
        return WritersColumns.CONTENT_URI;
    }

    /**
     * Query the given content resolver using this selection.
     *
     * @param contentResolver The content resolver to query.
     * @param projection A list of which columns to return. Passing null will return all columns, which is inefficient.
     * @return A {@code WritersCursor} object, which is positioned before the first entry, or null.
     */
    public WritersCursor query(ContentResolver contentResolver, String[] projection) {
        Cursor cursor = contentResolver.query(uri(), projection, sel(), args(), order());
        if (cursor == null) return null;
        return new WritersCursor(cursor);
    }

    /**
     * Equivalent of calling {@code query(contentResolver, null)}.
     */
    public WritersCursor query(ContentResolver contentResolver) {
        return query(contentResolver, null);
    }

    /**
     * Query the given content resolver using this selection.
     *
     * @param context The context to use for the query.
     * @param projection A list of which columns to return. Passing null will return all columns, which is inefficient.
     * @return A {@code WritersCursor} object, which is positioned before the first entry, or null.
     */
    public WritersCursor query(Context context, String[] projection) {
        Cursor cursor = context.getContentResolver().query(uri(), projection, sel(), args(), order());
        if (cursor == null) return null;
        return new WritersCursor(cursor);
    }

    /**
     * Equivalent of calling {@code query(context, null)}.
     */
    public WritersCursor query(Context context) {
        return query(context, null);
    }


    public WritersSelection id(long... value) {
        addEquals("writers." + WritersColumns._ID, toObjectArray(value));
        return this;
    }

    public WritersSelection idNot(long... value) {
        addNotEquals("writers." + WritersColumns._ID, toObjectArray(value));
        return this;
    }

    public WritersSelection orderById(boolean desc) {
        orderBy("writers." + WritersColumns._ID, desc);
        return this;
    }

    public WritersSelection orderById() {
        return orderById(false);
    }

    public WritersSelection fieldWriterId(long... value) {
        addEquals(WritersColumns.FIELD_WRITER_ID, toObjectArray(value));
        return this;
    }

    public WritersSelection fieldWriterIdNot(long... value) {
        addNotEquals(WritersColumns.FIELD_WRITER_ID, toObjectArray(value));
        return this;
    }

    public WritersSelection fieldWriterIdGt(long value) {
        addGreaterThan(WritersColumns.FIELD_WRITER_ID, value);
        return this;
    }

    public WritersSelection fieldWriterIdGtEq(long value) {
        addGreaterThanOrEquals(WritersColumns.FIELD_WRITER_ID, value);
        return this;
    }

    public WritersSelection fieldWriterIdLt(long value) {
        addLessThan(WritersColumns.FIELD_WRITER_ID, value);
        return this;
    }

    public WritersSelection fieldWriterIdLtEq(long value) {
        addLessThanOrEquals(WritersColumns.FIELD_WRITER_ID, value);
        return this;
    }

    public WritersSelection orderByFieldWriterId(boolean desc) {
        orderBy(WritersColumns.FIELD_WRITER_ID, desc);
        return this;
    }

    public WritersSelection orderByFieldWriterId() {
        orderBy(WritersColumns.FIELD_WRITER_ID, false);
        return this;
    }

    public WritersSelection writersEpisodesFieldWriterId(long... value) {
        addEquals(WritersEpisodesColumns.FIELD_WRITER_ID, toObjectArray(value));
        return this;
    }

    public WritersSelection writersEpisodesFieldWriterIdNot(long... value) {
        addNotEquals(WritersEpisodesColumns.FIELD_WRITER_ID, toObjectArray(value));
        return this;
    }

    public WritersSelection writersEpisodesFieldWriterIdGt(long value) {
        addGreaterThan(WritersEpisodesColumns.FIELD_WRITER_ID, value);
        return this;
    }

    public WritersSelection writersEpisodesFieldWriterIdGtEq(long value) {
        addGreaterThanOrEquals(WritersEpisodesColumns.FIELD_WRITER_ID, value);
        return this;
    }

    public WritersSelection writersEpisodesFieldWriterIdLt(long value) {
        addLessThan(WritersEpisodesColumns.FIELD_WRITER_ID, value);
        return this;
    }

    public WritersSelection writersEpisodesFieldWriterIdLtEq(long value) {
        addLessThanOrEquals(WritersEpisodesColumns.FIELD_WRITER_ID, value);
        return this;
    }

    public WritersSelection orderByWritersEpisodesFieldWriterId(boolean desc) {
        orderBy(WritersEpisodesColumns.FIELD_WRITER_ID, desc);
        return this;
    }

    public WritersSelection orderByWritersEpisodesFieldWriterId() {
        orderBy(WritersEpisodesColumns.FIELD_WRITER_ID, false);
        return this;
    }

    public WritersSelection writersEpisodesFieldEpisodeNumber(long... value) {
        addEquals(WritersEpisodesColumns.FIELD_EPISODE_NUMBER, toObjectArray(value));
        return this;
    }

    public WritersSelection writersEpisodesFieldEpisodeNumberNot(long... value) {
        addNotEquals(WritersEpisodesColumns.FIELD_EPISODE_NUMBER, toObjectArray(value));
        return this;
    }

    public WritersSelection writersEpisodesFieldEpisodeNumberGt(long value) {
        addGreaterThan(WritersEpisodesColumns.FIELD_EPISODE_NUMBER, value);
        return this;
    }

    public WritersSelection writersEpisodesFieldEpisodeNumberGtEq(long value) {
        addGreaterThanOrEquals(WritersEpisodesColumns.FIELD_EPISODE_NUMBER, value);
        return this;
    }

    public WritersSelection writersEpisodesFieldEpisodeNumberLt(long value) {
        addLessThan(WritersEpisodesColumns.FIELD_EPISODE_NUMBER, value);
        return this;
    }

    public WritersSelection writersEpisodesFieldEpisodeNumberLtEq(long value) {
        addLessThanOrEquals(WritersEpisodesColumns.FIELD_EPISODE_NUMBER, value);
        return this;
    }

    public WritersSelection orderByWritersEpisodesFieldEpisodeNumber(boolean desc) {
        orderBy(WritersEpisodesColumns.FIELD_EPISODE_NUMBER, desc);
        return this;
    }

    public WritersSelection orderByWritersEpisodesFieldEpisodeNumber() {
        orderBy(WritersEpisodesColumns.FIELD_EPISODE_NUMBER, false);
        return this;
    }

    public WritersSelection fieldWriterName(String... value) {
        addEquals(WritersColumns.FIELD_WRITER_NAME, value);
        return this;
    }

    public WritersSelection fieldWriterNameNot(String... value) {
        addNotEquals(WritersColumns.FIELD_WRITER_NAME, value);
        return this;
    }

    public WritersSelection fieldWriterNameLike(String... value) {
        addLike(WritersColumns.FIELD_WRITER_NAME, value);
        return this;
    }

    public WritersSelection fieldWriterNameContains(String... value) {
        addContains(WritersColumns.FIELD_WRITER_NAME, value);
        return this;
    }

    public WritersSelection fieldWriterNameStartsWith(String... value) {
        addStartsWith(WritersColumns.FIELD_WRITER_NAME, value);
        return this;
    }

    public WritersSelection fieldWriterNameEndsWith(String... value) {
        addEndsWith(WritersColumns.FIELD_WRITER_NAME, value);
        return this;
    }

    public WritersSelection orderByFieldWriterName(boolean desc) {
        orderBy(WritersColumns.FIELD_WRITER_NAME, desc);
        return this;
    }

    public WritersSelection orderByFieldWriterName() {
        orderBy(WritersColumns.FIELD_WRITER_NAME, false);
        return this;
    }

    public WritersSelection fieldWriterUrl(String... value) {
        addEquals(WritersColumns.FIELD_WRITER_URL, value);
        return this;
    }

    public WritersSelection fieldWriterUrlNot(String... value) {
        addNotEquals(WritersColumns.FIELD_WRITER_URL, value);
        return this;
    }

    public WritersSelection fieldWriterUrlLike(String... value) {
        addLike(WritersColumns.FIELD_WRITER_URL, value);
        return this;
    }

    public WritersSelection fieldWriterUrlContains(String... value) {
        addContains(WritersColumns.FIELD_WRITER_URL, value);
        return this;
    }

    public WritersSelection fieldWriterUrlStartsWith(String... value) {
        addStartsWith(WritersColumns.FIELD_WRITER_URL, value);
        return this;
    }

    public WritersSelection fieldWriterUrlEndsWith(String... value) {
        addEndsWith(WritersColumns.FIELD_WRITER_URL, value);
        return this;
    }

    public WritersSelection orderByFieldWriterUrl(boolean desc) {
        orderBy(WritersColumns.FIELD_WRITER_URL, desc);
        return this;
    }

    public WritersSelection orderByFieldWriterUrl() {
        orderBy(WritersColumns.FIELD_WRITER_URL, false);
        return this;
    }

    public WritersSelection fieldWriterBio(String... value) {
        addEquals(WritersColumns.FIELD_WRITER_BIO, value);
        return this;
    }

    public WritersSelection fieldWriterBioNot(String... value) {
        addNotEquals(WritersColumns.FIELD_WRITER_BIO, value);
        return this;
    }

    public WritersSelection fieldWriterBioLike(String... value) {
        addLike(WritersColumns.FIELD_WRITER_BIO, value);
        return this;
    }

    public WritersSelection fieldWriterBioContains(String... value) {
        addContains(WritersColumns.FIELD_WRITER_BIO, value);
        return this;
    }

    public WritersSelection fieldWriterBioStartsWith(String... value) {
        addStartsWith(WritersColumns.FIELD_WRITER_BIO, value);
        return this;
    }

    public WritersSelection fieldWriterBioEndsWith(String... value) {
        addEndsWith(WritersColumns.FIELD_WRITER_BIO, value);
        return this;
    }

    public WritersSelection orderByFieldWriterBio(boolean desc) {
        orderBy(WritersColumns.FIELD_WRITER_BIO, desc);
        return this;
    }

    public WritersSelection orderByFieldWriterBio() {
        orderBy(WritersColumns.FIELD_WRITER_BIO, false);
        return this;
    }
}
