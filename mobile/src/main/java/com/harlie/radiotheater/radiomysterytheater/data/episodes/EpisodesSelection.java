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
package com.harlie.radiotheater.radiomysterytheater.data.episodes;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.harlie.radiotheater.radiomysterytheater.data.base.AbstractSelection;

import java.util.Date;

/**
 * Selection for the {@code episodes} table.
 */
public class EpisodesSelection extends AbstractSelection<EpisodesSelection> {
    @Override
    protected Uri baseUri() {
        return EpisodesColumns.CONTENT_URI;
    }

    /**
     * Query the given content resolver using this selection.
     *
     * @param contentResolver The content resolver to query.
     * @param projection A list of which columns to return. Passing null will return all columns, which is inefficient.
     * @return A {@code EpisodesCursor} object, which is positioned before the first entry, or null.
     */
    public EpisodesCursor query(ContentResolver contentResolver, String[] projection) {
        Cursor cursor = contentResolver.query(uri(), projection, sel(), args(), order());
        if (cursor == null) return null;
        return new EpisodesCursor(cursor);
    }

    /**
     * Equivalent of calling {@code query(contentResolver, null)}.
     */
    public EpisodesCursor query(ContentResolver contentResolver) {
        return query(contentResolver, null);
    }

    /**
     * Query the given content resolver using this selection.
     *
     * @param context The context to use for the query.
     * @param projection A list of which columns to return. Passing null will return all columns, which is inefficient.
     * @return A {@code EpisodesCursor} object, which is positioned before the first entry, or null.
     */
    public EpisodesCursor query(Context context, String[] projection) {
        Cursor cursor = context.getContentResolver().query(uri(), projection, sel(), args(), order());
        if (cursor == null) return null;
        return new EpisodesCursor(cursor);
    }

    /**
     * Equivalent of calling {@code query(context, null)}.
     */
    public EpisodesCursor query(Context context) {
        return query(context, null);
    }


    public EpisodesSelection id(long... value) {
        addEquals("episodes." + EpisodesColumns._ID, toObjectArray(value));
        return this;
    }

    public EpisodesSelection idNot(long... value) {
        addNotEquals("episodes." + EpisodesColumns._ID, toObjectArray(value));
        return this;
    }

    public EpisodesSelection orderById(boolean desc) {
        orderBy("episodes." + EpisodesColumns._ID, desc);
        return this;
    }

    public EpisodesSelection orderById() {
        return orderById(false);
    }

    public EpisodesSelection fieldEpisodeNumber(long... value) {
        addEquals(EpisodesColumns.FIELD_EPISODE_NUMBER, toObjectArray(value));
        return this;
    }

    public EpisodesSelection fieldEpisodeNumberNot(long... value) {
        addNotEquals(EpisodesColumns.FIELD_EPISODE_NUMBER, toObjectArray(value));
        return this;
    }

    public EpisodesSelection fieldEpisodeNumberGt(long value) {
        addGreaterThan(EpisodesColumns.FIELD_EPISODE_NUMBER, value);
        return this;
    }

    public EpisodesSelection fieldEpisodeNumberGtEq(long value) {
        addGreaterThanOrEquals(EpisodesColumns.FIELD_EPISODE_NUMBER, value);
        return this;
    }

    public EpisodesSelection fieldEpisodeNumberLt(long value) {
        addLessThan(EpisodesColumns.FIELD_EPISODE_NUMBER, value);
        return this;
    }

    public EpisodesSelection fieldEpisodeNumberLtEq(long value) {
        addLessThanOrEquals(EpisodesColumns.FIELD_EPISODE_NUMBER, value);
        return this;
    }

    public EpisodesSelection orderByFieldEpisodeNumber(boolean desc) {
        orderBy(EpisodesColumns.FIELD_EPISODE_NUMBER, desc);
        return this;
    }

    public EpisodesSelection orderByFieldEpisodeNumber() {
        orderBy(EpisodesColumns.FIELD_EPISODE_NUMBER, false);
        return this;
    }

    public EpisodesSelection fieldAirdate(Date... value) {
        addEquals(EpisodesColumns.FIELD_AIRDATE, value);
        return this;
    }

    public EpisodesSelection fieldAirdateNot(Date... value) {
        addNotEquals(EpisodesColumns.FIELD_AIRDATE, value);
        return this;
    }

    public EpisodesSelection fieldAirdate(long... value) {
        addEquals(EpisodesColumns.FIELD_AIRDATE, toObjectArray(value));
        return this;
    }

    public EpisodesSelection fieldAirdateAfter(Date value) {
        addGreaterThan(EpisodesColumns.FIELD_AIRDATE, value);
        return this;
    }

    public EpisodesSelection fieldAirdateAfterEq(Date value) {
        addGreaterThanOrEquals(EpisodesColumns.FIELD_AIRDATE, value);
        return this;
    }

    public EpisodesSelection fieldAirdateBefore(Date value) {
        addLessThan(EpisodesColumns.FIELD_AIRDATE, value);
        return this;
    }

    public EpisodesSelection fieldAirdateBeforeEq(Date value) {
        addLessThanOrEquals(EpisodesColumns.FIELD_AIRDATE, value);
        return this;
    }

    public EpisodesSelection orderByFieldAirdate(boolean desc) {
        orderBy(EpisodesColumns.FIELD_AIRDATE, desc);
        return this;
    }

    public EpisodesSelection orderByFieldAirdate() {
        orderBy(EpisodesColumns.FIELD_AIRDATE, false);
        return this;
    }

    public EpisodesSelection fieldEpisodeTitle(String... value) {
        addEquals(EpisodesColumns.FIELD_EPISODE_TITLE, value);
        return this;
    }

    public EpisodesSelection fieldEpisodeTitleNot(String... value) {
        addNotEquals(EpisodesColumns.FIELD_EPISODE_TITLE, value);
        return this;
    }

    public EpisodesSelection fieldEpisodeTitleLike(String... value) {
        addLike(EpisodesColumns.FIELD_EPISODE_TITLE, value);
        return this;
    }

    public EpisodesSelection fieldEpisodeTitleContains(String... value) {
        addContains(EpisodesColumns.FIELD_EPISODE_TITLE, value);
        return this;
    }

    public EpisodesSelection fieldEpisodeTitleStartsWith(String... value) {
        addStartsWith(EpisodesColumns.FIELD_EPISODE_TITLE, value);
        return this;
    }

    public EpisodesSelection fieldEpisodeTitleEndsWith(String... value) {
        addEndsWith(EpisodesColumns.FIELD_EPISODE_TITLE, value);
        return this;
    }

    public EpisodesSelection orderByFieldEpisodeTitle(boolean desc) {
        orderBy(EpisodesColumns.FIELD_EPISODE_TITLE, desc);
        return this;
    }

    public EpisodesSelection orderByFieldEpisodeTitle() {
        orderBy(EpisodesColumns.FIELD_EPISODE_TITLE, false);
        return this;
    }

    public EpisodesSelection fieldEpisodeDescription(String... value) {
        addEquals(EpisodesColumns.FIELD_EPISODE_DESCRIPTION, value);
        return this;
    }

    public EpisodesSelection fieldEpisodeDescriptionNot(String... value) {
        addNotEquals(EpisodesColumns.FIELD_EPISODE_DESCRIPTION, value);
        return this;
    }

    public EpisodesSelection fieldEpisodeDescriptionLike(String... value) {
        addLike(EpisodesColumns.FIELD_EPISODE_DESCRIPTION, value);
        return this;
    }

    public EpisodesSelection fieldEpisodeDescriptionContains(String... value) {
        addContains(EpisodesColumns.FIELD_EPISODE_DESCRIPTION, value);
        return this;
    }

    public EpisodesSelection fieldEpisodeDescriptionStartsWith(String... value) {
        addStartsWith(EpisodesColumns.FIELD_EPISODE_DESCRIPTION, value);
        return this;
    }

    public EpisodesSelection fieldEpisodeDescriptionEndsWith(String... value) {
        addEndsWith(EpisodesColumns.FIELD_EPISODE_DESCRIPTION, value);
        return this;
    }

    public EpisodesSelection orderByFieldEpisodeDescription(boolean desc) {
        orderBy(EpisodesColumns.FIELD_EPISODE_DESCRIPTION, desc);
        return this;
    }

    public EpisodesSelection orderByFieldEpisodeDescription() {
        orderBy(EpisodesColumns.FIELD_EPISODE_DESCRIPTION, false);
        return this;
    }

    public EpisodesSelection fieldWeblinkUrl(String... value) {
        addEquals(EpisodesColumns.FIELD_WEBLINK_URL, value);
        return this;
    }

    public EpisodesSelection fieldWeblinkUrlNot(String... value) {
        addNotEquals(EpisodesColumns.FIELD_WEBLINK_URL, value);
        return this;
    }

    public EpisodesSelection fieldWeblinkUrlLike(String... value) {
        addLike(EpisodesColumns.FIELD_WEBLINK_URL, value);
        return this;
    }

    public EpisodesSelection fieldWeblinkUrlContains(String... value) {
        addContains(EpisodesColumns.FIELD_WEBLINK_URL, value);
        return this;
    }

    public EpisodesSelection fieldWeblinkUrlStartsWith(String... value) {
        addStartsWith(EpisodesColumns.FIELD_WEBLINK_URL, value);
        return this;
    }

    public EpisodesSelection fieldWeblinkUrlEndsWith(String... value) {
        addEndsWith(EpisodesColumns.FIELD_WEBLINK_URL, value);
        return this;
    }

    public EpisodesSelection orderByFieldWeblinkUrl(boolean desc) {
        orderBy(EpisodesColumns.FIELD_WEBLINK_URL, desc);
        return this;
    }

    public EpisodesSelection orderByFieldWeblinkUrl() {
        orderBy(EpisodesColumns.FIELD_WEBLINK_URL, false);
        return this;
    }

    public EpisodesSelection fieldDownloadUrl(String... value) {
        addEquals(EpisodesColumns.FIELD_DOWNLOAD_URL, value);
        return this;
    }

    public EpisodesSelection fieldDownloadUrlNot(String... value) {
        addNotEquals(EpisodesColumns.FIELD_DOWNLOAD_URL, value);
        return this;
    }

    public EpisodesSelection fieldDownloadUrlLike(String... value) {
        addLike(EpisodesColumns.FIELD_DOWNLOAD_URL, value);
        return this;
    }

    public EpisodesSelection fieldDownloadUrlContains(String... value) {
        addContains(EpisodesColumns.FIELD_DOWNLOAD_URL, value);
        return this;
    }

    public EpisodesSelection fieldDownloadUrlStartsWith(String... value) {
        addStartsWith(EpisodesColumns.FIELD_DOWNLOAD_URL, value);
        return this;
    }

    public EpisodesSelection fieldDownloadUrlEndsWith(String... value) {
        addEndsWith(EpisodesColumns.FIELD_DOWNLOAD_URL, value);
        return this;
    }

    public EpisodesSelection orderByFieldDownloadUrl(boolean desc) {
        orderBy(EpisodesColumns.FIELD_DOWNLOAD_URL, desc);
        return this;
    }

    public EpisodesSelection orderByFieldDownloadUrl() {
        orderBy(EpisodesColumns.FIELD_DOWNLOAD_URL, false);
        return this;
    }

    public EpisodesSelection fieldRating(Integer... value) {
        addEquals(EpisodesColumns.FIELD_RATING, value);
        return this;
    }

    public EpisodesSelection fieldRatingNot(Integer... value) {
        addNotEquals(EpisodesColumns.FIELD_RATING, value);
        return this;
    }

    public EpisodesSelection fieldRatingGt(int value) {
        addGreaterThan(EpisodesColumns.FIELD_RATING, value);
        return this;
    }

    public EpisodesSelection fieldRatingGtEq(int value) {
        addGreaterThanOrEquals(EpisodesColumns.FIELD_RATING, value);
        return this;
    }

    public EpisodesSelection fieldRatingLt(int value) {
        addLessThan(EpisodesColumns.FIELD_RATING, value);
        return this;
    }

    public EpisodesSelection fieldRatingLtEq(int value) {
        addLessThanOrEquals(EpisodesColumns.FIELD_RATING, value);
        return this;
    }

    public EpisodesSelection orderByFieldRating(boolean desc) {
        orderBy(EpisodesColumns.FIELD_RATING, desc);
        return this;
    }

    public EpisodesSelection orderByFieldRating() {
        orderBy(EpisodesColumns.FIELD_RATING, false);
        return this;
    }

    public EpisodesSelection fieldVoteCount(Integer... value) {
        addEquals(EpisodesColumns.FIELD_VOTE_COUNT, value);
        return this;
    }

    public EpisodesSelection fieldVoteCountNot(Integer... value) {
        addNotEquals(EpisodesColumns.FIELD_VOTE_COUNT, value);
        return this;
    }

    public EpisodesSelection fieldVoteCountGt(int value) {
        addGreaterThan(EpisodesColumns.FIELD_VOTE_COUNT, value);
        return this;
    }

    public EpisodesSelection fieldVoteCountGtEq(int value) {
        addGreaterThanOrEquals(EpisodesColumns.FIELD_VOTE_COUNT, value);
        return this;
    }

    public EpisodesSelection fieldVoteCountLt(int value) {
        addLessThan(EpisodesColumns.FIELD_VOTE_COUNT, value);
        return this;
    }

    public EpisodesSelection fieldVoteCountLtEq(int value) {
        addLessThanOrEquals(EpisodesColumns.FIELD_VOTE_COUNT, value);
        return this;
    }

    public EpisodesSelection orderByFieldVoteCount(boolean desc) {
        orderBy(EpisodesColumns.FIELD_VOTE_COUNT, desc);
        return this;
    }

    public EpisodesSelection orderByFieldVoteCount() {
        orderBy(EpisodesColumns.FIELD_VOTE_COUNT, false);
        return this;
    }
}
