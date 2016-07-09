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
import android.database.Cursor;
import android.net.Uri;

import com.harlie.radiotheater.radiomysterytheater.data.base.AbstractSelection;

/**
 * Selection for the {@code actors_episodes} table.
 */
public class ActorsEpisodesSelection extends AbstractSelection<ActorsEpisodesSelection> {
    @Override
    protected Uri baseUri() {
        return ActorsEpisodesColumns.CONTENT_URI;
    }

    /**
     * Query the given content resolver using this selection.
     *
     * @param contentResolver The content resolver to query.
     * @param projection A list of which columns to return. Passing null will return all columns, which is inefficient.
     * @return A {@code ActorsEpisodesCursor} object, which is positioned before the first entry, or null.
     */
    public ActorsEpisodesCursor query(ContentResolver contentResolver, String[] projection) {
        Cursor cursor = contentResolver.query(uri(), projection, sel(), args(), order());
        if (cursor == null) return null;
        return new ActorsEpisodesCursor(cursor);
    }

    /**
     * Equivalent of calling {@code query(contentResolver, null)}.
     */
    public ActorsEpisodesCursor query(ContentResolver contentResolver) {
        return query(contentResolver, null);
    }

    /**
     * Query the given content resolver using this selection.
     *
     * @param context The context to use for the query.
     * @param projection A list of which columns to return. Passing null will return all columns, which is inefficient.
     * @return A {@code ActorsEpisodesCursor} object, which is positioned before the first entry, or null.
     */
    public ActorsEpisodesCursor query(Context context, String[] projection) {
        Cursor cursor = context.getContentResolver().query(uri(), projection, sel(), args(), order());
        if (cursor == null) return null;
        return new ActorsEpisodesCursor(cursor);
    }

    /**
     * Equivalent of calling {@code query(context, null)}.
     */
    public ActorsEpisodesCursor query(Context context) {
        return query(context, null);
    }


    public ActorsEpisodesSelection id(long... value) {
        addEquals("actors_episodes." + ActorsEpisodesColumns._ID, toObjectArray(value));
        return this;
    }

    public ActorsEpisodesSelection idNot(long... value) {
        addNotEquals("actors_episodes." + ActorsEpisodesColumns._ID, toObjectArray(value));
        return this;
    }

    public ActorsEpisodesSelection orderById(boolean desc) {
        orderBy("actors_episodes." + ActorsEpisodesColumns._ID, desc);
        return this;
    }

    public ActorsEpisodesSelection orderById() {
        return orderById(false);
    }

    public ActorsEpisodesSelection fieldActorId(long... value) {
        addEquals(ActorsEpisodesColumns.FIELD_ACTOR_ID, toObjectArray(value));
        return this;
    }

    public ActorsEpisodesSelection fieldActorIdNot(long... value) {
        addNotEquals(ActorsEpisodesColumns.FIELD_ACTOR_ID, toObjectArray(value));
        return this;
    }

    public ActorsEpisodesSelection fieldActorIdGt(long value) {
        addGreaterThan(ActorsEpisodesColumns.FIELD_ACTOR_ID, value);
        return this;
    }

    public ActorsEpisodesSelection fieldActorIdGtEq(long value) {
        addGreaterThanOrEquals(ActorsEpisodesColumns.FIELD_ACTOR_ID, value);
        return this;
    }

    public ActorsEpisodesSelection fieldActorIdLt(long value) {
        addLessThan(ActorsEpisodesColumns.FIELD_ACTOR_ID, value);
        return this;
    }

    public ActorsEpisodesSelection fieldActorIdLtEq(long value) {
        addLessThanOrEquals(ActorsEpisodesColumns.FIELD_ACTOR_ID, value);
        return this;
    }

    public ActorsEpisodesSelection orderByFieldActorId(boolean desc) {
        orderBy(ActorsEpisodesColumns.FIELD_ACTOR_ID, desc);
        return this;
    }

    public ActorsEpisodesSelection orderByFieldActorId() {
        orderBy(ActorsEpisodesColumns.FIELD_ACTOR_ID, false);
        return this;
    }

    public ActorsEpisodesSelection fieldActorName(String... value) {
        addEquals(ActorsEpisodesColumns.FIELD_ACTOR_NAME, value);
        return this;
    }

    public ActorsEpisodesSelection fieldActorNameNot(String... value) {
        addNotEquals(ActorsEpisodesColumns.FIELD_ACTOR_NAME, value);
        return this;
    }

    public ActorsEpisodesSelection fieldActorNameLike(String... value) {
        addLike(ActorsEpisodesColumns.FIELD_ACTOR_NAME, value);
        return this;
    }

    public ActorsEpisodesSelection fieldActorNameContains(String... value) {
        addContains(ActorsEpisodesColumns.FIELD_ACTOR_NAME, value);
        return this;
    }

    public ActorsEpisodesSelection fieldActorNameStartsWith(String... value) {
        addStartsWith(ActorsEpisodesColumns.FIELD_ACTOR_NAME, value);
        return this;
    }

    public ActorsEpisodesSelection fieldActorNameEndsWith(String... value) {
        addEndsWith(ActorsEpisodesColumns.FIELD_ACTOR_NAME, value);
        return this;
    }

    public ActorsEpisodesSelection orderByFieldActorName(boolean desc) {
        orderBy(ActorsEpisodesColumns.FIELD_ACTOR_NAME, desc);
        return this;
    }

    public ActorsEpisodesSelection orderByFieldActorName() {
        orderBy(ActorsEpisodesColumns.FIELD_ACTOR_NAME, false);
        return this;
    }

    public ActorsEpisodesSelection fieldEpisodeNumber(long... value) {
        addEquals(ActorsEpisodesColumns.FIELD_EPISODE_NUMBER, toObjectArray(value));
        return this;
    }

    public ActorsEpisodesSelection fieldEpisodeNumberNot(long... value) {
        addNotEquals(ActorsEpisodesColumns.FIELD_EPISODE_NUMBER, toObjectArray(value));
        return this;
    }

    public ActorsEpisodesSelection fieldEpisodeNumberGt(long value) {
        addGreaterThan(ActorsEpisodesColumns.FIELD_EPISODE_NUMBER, value);
        return this;
    }

    public ActorsEpisodesSelection fieldEpisodeNumberGtEq(long value) {
        addGreaterThanOrEquals(ActorsEpisodesColumns.FIELD_EPISODE_NUMBER, value);
        return this;
    }

    public ActorsEpisodesSelection fieldEpisodeNumberLt(long value) {
        addLessThan(ActorsEpisodesColumns.FIELD_EPISODE_NUMBER, value);
        return this;
    }

    public ActorsEpisodesSelection fieldEpisodeNumberLtEq(long value) {
        addLessThanOrEquals(ActorsEpisodesColumns.FIELD_EPISODE_NUMBER, value);
        return this;
    }

    public ActorsEpisodesSelection orderByFieldEpisodeNumber(boolean desc) {
        orderBy(ActorsEpisodesColumns.FIELD_EPISODE_NUMBER, desc);
        return this;
    }

    public ActorsEpisodesSelection orderByFieldEpisodeNumber() {
        orderBy(ActorsEpisodesColumns.FIELD_EPISODE_NUMBER, false);
        return this;
    }
}
