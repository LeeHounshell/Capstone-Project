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
package com.harlie.radiotheater.radiomysterytheater.data.actors;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.harlie.radiotheater.radiomysterytheater.data.base.AbstractSelection;

/**
 * Selection for the {@code actors} table.
 */
public class ActorsSelection extends AbstractSelection<ActorsSelection> {
    @Override
    protected Uri baseUri() {
        return ActorsColumns.CONTENT_URI;
    }

    /**
     * Query the given content resolver using this selection.
     *
     * @param contentResolver The content resolver to query.
     * @param projection A list of which columns to return. Passing null will return all columns, which is inefficient.
     * @return A {@code ActorsCursor} object, which is positioned before the first entry, or null.
     */
    public ActorsCursor query(ContentResolver contentResolver, String[] projection) {
        Cursor cursor = contentResolver.query(uri(), projection, sel(), args(), order());
        if (cursor == null) return null;
        return new ActorsCursor(cursor);
    }

    /**
     * Equivalent of calling {@code query(contentResolver, null)}.
     */
    public ActorsCursor query(ContentResolver contentResolver) {
        return query(contentResolver, null);
    }

    /**
     * Query the given content resolver using this selection.
     *
     * @param context The context to use for the query.
     * @param projection A list of which columns to return. Passing null will return all columns, which is inefficient.
     * @return A {@code ActorsCursor} object, which is positioned before the first entry, or null.
     */
    public ActorsCursor query(Context context, String[] projection) {
        Cursor cursor = context.getContentResolver().query(uri(), projection, sel(), args(), order());
        if (cursor == null) return null;
        return new ActorsCursor(cursor);
    }

    /**
     * Equivalent of calling {@code query(context, null)}.
     */
    public ActorsCursor query(Context context) {
        return query(context, null);
    }


    public ActorsSelection id(long... value) {
        addEquals("actors." + ActorsColumns._ID, toObjectArray(value));
        return this;
    }

    public ActorsSelection idNot(long... value) {
        addNotEquals("actors." + ActorsColumns._ID, toObjectArray(value));
        return this;
    }

    public ActorsSelection orderById(boolean desc) {
        orderBy("actors." + ActorsColumns._ID, desc);
        return this;
    }

    public ActorsSelection orderById() {
        return orderById(false);
    }

    public ActorsSelection fieldActorId(long... value) {
        addEquals(ActorsColumns.FIELD_ACTOR_ID, toObjectArray(value));
        return this;
    }

    public ActorsSelection fieldActorIdNot(long... value) {
        addNotEquals(ActorsColumns.FIELD_ACTOR_ID, toObjectArray(value));
        return this;
    }

    public ActorsSelection fieldActorIdGt(long value) {
        addGreaterThan(ActorsColumns.FIELD_ACTOR_ID, value);
        return this;
    }

    public ActorsSelection fieldActorIdGtEq(long value) {
        addGreaterThanOrEquals(ActorsColumns.FIELD_ACTOR_ID, value);
        return this;
    }

    public ActorsSelection fieldActorIdLt(long value) {
        addLessThan(ActorsColumns.FIELD_ACTOR_ID, value);
        return this;
    }

    public ActorsSelection fieldActorIdLtEq(long value) {
        addLessThanOrEquals(ActorsColumns.FIELD_ACTOR_ID, value);
        return this;
    }

    public ActorsSelection orderByFieldActorId(boolean desc) {
        orderBy(ActorsColumns.FIELD_ACTOR_ID, desc);
        return this;
    }

    public ActorsSelection orderByFieldActorId() {
        orderBy(ActorsColumns.FIELD_ACTOR_ID, false);
        return this;
    }

    public ActorsSelection fieldActorName(String... value) {
        addEquals(ActorsColumns.FIELD_ACTOR_NAME, value);
        return this;
    }

    public ActorsSelection fieldActorNameNot(String... value) {
        addNotEquals(ActorsColumns.FIELD_ACTOR_NAME, value);
        return this;
    }

    public ActorsSelection fieldActorNameLike(String... value) {
        addLike(ActorsColumns.FIELD_ACTOR_NAME, value);
        return this;
    }

    public ActorsSelection fieldActorNameContains(String... value) {
        addContains(ActorsColumns.FIELD_ACTOR_NAME, value);
        return this;
    }

    public ActorsSelection fieldActorNameStartsWith(String... value) {
        addStartsWith(ActorsColumns.FIELD_ACTOR_NAME, value);
        return this;
    }

    public ActorsSelection fieldActorNameEndsWith(String... value) {
        addEndsWith(ActorsColumns.FIELD_ACTOR_NAME, value);
        return this;
    }

    public ActorsSelection orderByFieldActorName(boolean desc) {
        orderBy(ActorsColumns.FIELD_ACTOR_NAME, desc);
        return this;
    }

    public ActorsSelection orderByFieldActorName() {
        orderBy(ActorsColumns.FIELD_ACTOR_NAME, false);
        return this;
    }

    public ActorsSelection fieldActorUrl(String... value) {
        addEquals(ActorsColumns.FIELD_ACTOR_URL, value);
        return this;
    }

    public ActorsSelection fieldActorUrlNot(String... value) {
        addNotEquals(ActorsColumns.FIELD_ACTOR_URL, value);
        return this;
    }

    public ActorsSelection fieldActorUrlLike(String... value) {
        addLike(ActorsColumns.FIELD_ACTOR_URL, value);
        return this;
    }

    public ActorsSelection fieldActorUrlContains(String... value) {
        addContains(ActorsColumns.FIELD_ACTOR_URL, value);
        return this;
    }

    public ActorsSelection fieldActorUrlStartsWith(String... value) {
        addStartsWith(ActorsColumns.FIELD_ACTOR_URL, value);
        return this;
    }

    public ActorsSelection fieldActorUrlEndsWith(String... value) {
        addEndsWith(ActorsColumns.FIELD_ACTOR_URL, value);
        return this;
    }

    public ActorsSelection orderByFieldActorUrl(boolean desc) {
        orderBy(ActorsColumns.FIELD_ACTOR_URL, desc);
        return this;
    }

    public ActorsSelection orderByFieldActorUrl() {
        orderBy(ActorsColumns.FIELD_ACTOR_URL, false);
        return this;
    }

    public ActorsSelection fieldActorBio(String... value) {
        addEquals(ActorsColumns.FIELD_ACTOR_BIO, value);
        return this;
    }

    public ActorsSelection fieldActorBioNot(String... value) {
        addNotEquals(ActorsColumns.FIELD_ACTOR_BIO, value);
        return this;
    }

    public ActorsSelection fieldActorBioLike(String... value) {
        addLike(ActorsColumns.FIELD_ACTOR_BIO, value);
        return this;
    }

    public ActorsSelection fieldActorBioContains(String... value) {
        addContains(ActorsColumns.FIELD_ACTOR_BIO, value);
        return this;
    }

    public ActorsSelection fieldActorBioStartsWith(String... value) {
        addStartsWith(ActorsColumns.FIELD_ACTOR_BIO, value);
        return this;
    }

    public ActorsSelection fieldActorBioEndsWith(String... value) {
        addEndsWith(ActorsColumns.FIELD_ACTOR_BIO, value);
        return this;
    }

    public ActorsSelection orderByFieldActorBio(boolean desc) {
        orderBy(ActorsColumns.FIELD_ACTOR_BIO, desc);
        return this;
    }

    public ActorsSelection orderByFieldActorBio() {
        orderBy(ActorsColumns.FIELD_ACTOR_BIO, false);
        return this;
    }
}
