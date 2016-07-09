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
package com.harlie.radiotheater.radiomysterytheater.data;

import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import com.harlie.radiotheater.radiomysterytheater.data.actors.ActorsColumns;
import com.harlie.radiotheater.radiomysterytheater.data.actorsepisodes.ActorsEpisodesColumns;
import com.harlie.radiotheater.radiomysterytheater.data.base.BaseContentProvider;
import com.harlie.radiotheater.radiomysterytheater.data.configepisodes.ConfigEpisodesColumns;
import com.harlie.radiotheater.radiomysterytheater.data.configuration.ConfigurationColumns;
import com.harlie.radiotheater.radiomysterytheater.data.episodes.EpisodesColumns;
import com.harlie.radiotheater.radiomysterytheater.data.episodesactors.EpisodesActorsColumns;
import com.harlie.radiotheater.radiomysterytheater.data.episodeswriters.EpisodesWritersColumns;
import com.harlie.radiotheater.radiomysterytheater.data.writers.WritersColumns;
import com.harlie.radiotheater.radiomysterytheater.data.writersepisodes.WritersEpisodesColumns;

import java.util.Arrays;

public class RadioTheaterProvider extends BaseContentProvider {
    private static final String TAG = RadioTheaterProvider.class.getSimpleName();

    //private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final boolean DEBUG = false;

    private static final String TYPE_CURSOR_ITEM = "vnd.android.cursor.item/";
    private static final String TYPE_CURSOR_DIR = "vnd.android.cursor.dir/";

//#IFDEF 'PAID'
    //public static final String AUTHORITY = "com.harlie.radiotheater.radiomysterytheater.paid.data.radiotheaterprovider";
//#ENDIF

//#IFDEF 'TRIAL'
    public static final String AUTHORITY = "com.harlie.radiotheater.radiomysterytheater.trial.data.radiotheaterprovider";
//#ENDIF

    public static final String CONTENT_URI_BASE = "content://" + AUTHORITY;

    private static final int URI_TYPE_ACTORS = 0;
    private static final int URI_TYPE_ACTORS_ID = 1;

    private static final int URI_TYPE_ACTORS_EPISODES = 2;
    private static final int URI_TYPE_ACTORS_EPISODES_ID = 3;

    private static final int URI_TYPE_CONFIG_EPISODES = 4;
    private static final int URI_TYPE_CONFIG_EPISODES_ID = 5;

    private static final int URI_TYPE_CONFIGURATION = 6;
    private static final int URI_TYPE_CONFIGURATION_ID = 7;

    private static final int URI_TYPE_EPISODES = 8;
    private static final int URI_TYPE_EPISODES_ID = 9;

    private static final int URI_TYPE_EPISODES_ACTORS = 10;
    private static final int URI_TYPE_EPISODES_ACTORS_ID = 11;

    private static final int URI_TYPE_EPISODES_WRITERS = 12;
    private static final int URI_TYPE_EPISODES_WRITERS_ID = 13;

    private static final int URI_TYPE_WRITERS = 14;
    private static final int URI_TYPE_WRITERS_ID = 15;

    private static final int URI_TYPE_WRITERS_EPISODES = 16;
    private static final int URI_TYPE_WRITERS_EPISODES_ID = 17;



    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        URI_MATCHER.addURI(AUTHORITY, ActorsColumns.TABLE_NAME, URI_TYPE_ACTORS);
        URI_MATCHER.addURI(AUTHORITY, ActorsColumns.TABLE_NAME + "/#", URI_TYPE_ACTORS_ID);
        URI_MATCHER.addURI(AUTHORITY, ActorsEpisodesColumns.TABLE_NAME, URI_TYPE_ACTORS_EPISODES);
        URI_MATCHER.addURI(AUTHORITY, ActorsEpisodesColumns.TABLE_NAME + "/#", URI_TYPE_ACTORS_EPISODES_ID);
        URI_MATCHER.addURI(AUTHORITY, ConfigEpisodesColumns.TABLE_NAME, URI_TYPE_CONFIG_EPISODES);
        URI_MATCHER.addURI(AUTHORITY, ConfigEpisodesColumns.TABLE_NAME + "/#", URI_TYPE_CONFIG_EPISODES_ID);
        URI_MATCHER.addURI(AUTHORITY, ConfigurationColumns.TABLE_NAME, URI_TYPE_CONFIGURATION);
        URI_MATCHER.addURI(AUTHORITY, ConfigurationColumns.TABLE_NAME + "/#", URI_TYPE_CONFIGURATION_ID);
        URI_MATCHER.addURI(AUTHORITY, EpisodesColumns.TABLE_NAME, URI_TYPE_EPISODES);
        URI_MATCHER.addURI(AUTHORITY, EpisodesColumns.TABLE_NAME + "/#", URI_TYPE_EPISODES_ID);
        URI_MATCHER.addURI(AUTHORITY, EpisodesActorsColumns.TABLE_NAME, URI_TYPE_EPISODES_ACTORS);
        URI_MATCHER.addURI(AUTHORITY, EpisodesActorsColumns.TABLE_NAME + "/#", URI_TYPE_EPISODES_ACTORS_ID);
        URI_MATCHER.addURI(AUTHORITY, EpisodesWritersColumns.TABLE_NAME, URI_TYPE_EPISODES_WRITERS);
        URI_MATCHER.addURI(AUTHORITY, EpisodesWritersColumns.TABLE_NAME + "/#", URI_TYPE_EPISODES_WRITERS_ID);
        URI_MATCHER.addURI(AUTHORITY, WritersColumns.TABLE_NAME, URI_TYPE_WRITERS);
        URI_MATCHER.addURI(AUTHORITY, WritersColumns.TABLE_NAME + "/#", URI_TYPE_WRITERS_ID);
        URI_MATCHER.addURI(AUTHORITY, WritersEpisodesColumns.TABLE_NAME, URI_TYPE_WRITERS_EPISODES);
        URI_MATCHER.addURI(AUTHORITY, WritersEpisodesColumns.TABLE_NAME + "/#", URI_TYPE_WRITERS_EPISODES_ID);
    }

    @Override
    protected SQLiteOpenHelper createSqLiteOpenHelper() {
        return RadioTheaterHelper.getInstance(getContext());
    }

    @Override
    protected boolean hasDebug() {
        return DEBUG;
    }

    @Override
    public String getType(Uri uri) {
        int match = URI_MATCHER.match(uri);
        switch (match) {
            case URI_TYPE_ACTORS:
                return TYPE_CURSOR_DIR + ActorsColumns.TABLE_NAME;
            case URI_TYPE_ACTORS_ID:
                return TYPE_CURSOR_ITEM + ActorsColumns.TABLE_NAME;

            case URI_TYPE_ACTORS_EPISODES:
                return TYPE_CURSOR_DIR + ActorsEpisodesColumns.TABLE_NAME;
            case URI_TYPE_ACTORS_EPISODES_ID:
                return TYPE_CURSOR_ITEM + ActorsEpisodesColumns.TABLE_NAME;

            case URI_TYPE_CONFIG_EPISODES:
                return TYPE_CURSOR_DIR + ConfigEpisodesColumns.TABLE_NAME;
            case URI_TYPE_CONFIG_EPISODES_ID:
                return TYPE_CURSOR_ITEM + ConfigEpisodesColumns.TABLE_NAME;

            case URI_TYPE_CONFIGURATION:
                return TYPE_CURSOR_DIR + ConfigurationColumns.TABLE_NAME;
            case URI_TYPE_CONFIGURATION_ID:
                return TYPE_CURSOR_ITEM + ConfigurationColumns.TABLE_NAME;

            case URI_TYPE_EPISODES:
                return TYPE_CURSOR_DIR + EpisodesColumns.TABLE_NAME;
            case URI_TYPE_EPISODES_ID:
                return TYPE_CURSOR_ITEM + EpisodesColumns.TABLE_NAME;

            case URI_TYPE_EPISODES_ACTORS:
                return TYPE_CURSOR_DIR + EpisodesActorsColumns.TABLE_NAME;
            case URI_TYPE_EPISODES_ACTORS_ID:
                return TYPE_CURSOR_ITEM + EpisodesActorsColumns.TABLE_NAME;

            case URI_TYPE_EPISODES_WRITERS:
                return TYPE_CURSOR_DIR + EpisodesWritersColumns.TABLE_NAME;
            case URI_TYPE_EPISODES_WRITERS_ID:
                return TYPE_CURSOR_ITEM + EpisodesWritersColumns.TABLE_NAME;

            case URI_TYPE_WRITERS:
                return TYPE_CURSOR_DIR + WritersColumns.TABLE_NAME;
            case URI_TYPE_WRITERS_ID:
                return TYPE_CURSOR_ITEM + WritersColumns.TABLE_NAME;

            case URI_TYPE_WRITERS_EPISODES:
                return TYPE_CURSOR_DIR + WritersEpisodesColumns.TABLE_NAME;
            case URI_TYPE_WRITERS_EPISODES_ID:
                return TYPE_CURSOR_ITEM + WritersEpisodesColumns.TABLE_NAME;

        }
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if (DEBUG) Log.d(TAG, "insert uri=" + uri + " values=" + values);
        return super.insert(uri, values);
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        if (DEBUG) Log.d(TAG, "bulkInsert uri=" + uri + " values.length=" + values.length);
        return super.bulkInsert(uri, values);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (DEBUG) Log.d(TAG, "update uri=" + uri + " values=" + values + " selection=" + selection + " selectionArgs=" + Arrays.toString(selectionArgs));
        return super.update(uri, values, selection, selectionArgs);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if (DEBUG) Log.d(TAG, "delete uri=" + uri + " selection=" + selection + " selectionArgs=" + Arrays.toString(selectionArgs));
        return super.delete(uri, selection, selectionArgs);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (DEBUG)
            Log.d(TAG, "query uri=" + uri + " selection=" + selection + " selectionArgs=" + Arrays.toString(selectionArgs) + " sortOrder=" + sortOrder
                    + " groupBy=" + uri.getQueryParameter(QUERY_GROUP_BY) + " having=" + uri.getQueryParameter(QUERY_HAVING) + " limit=" + uri.getQueryParameter(QUERY_LIMIT));
        return super.query(uri, projection, selection, selectionArgs, sortOrder);
    }

    @Override
    protected QueryParams getQueryParams(Uri uri, String selection, String[] projection) {
        QueryParams res = new QueryParams();
        String id = null;
        int matchedId = URI_MATCHER.match(uri);
        switch (matchedId) {
            case URI_TYPE_ACTORS:
            case URI_TYPE_ACTORS_ID:
                res.table = ActorsColumns.TABLE_NAME;
                res.idColumn = ActorsColumns._ID;
                res.tablesWithJoins = ActorsColumns.TABLE_NAME;
                res.orderBy = ActorsColumns.DEFAULT_ORDER;
                break;

            case URI_TYPE_ACTORS_EPISODES:
            case URI_TYPE_ACTORS_EPISODES_ID:
                res.table = ActorsEpisodesColumns.TABLE_NAME;
                res.idColumn = ActorsEpisodesColumns._ID;
                res.tablesWithJoins = ActorsEpisodesColumns.TABLE_NAME;
                res.orderBy = ActorsEpisodesColumns.DEFAULT_ORDER;
                break;

            case URI_TYPE_CONFIG_EPISODES:
            case URI_TYPE_CONFIG_EPISODES_ID:
                res.table = ConfigEpisodesColumns.TABLE_NAME;
                res.idColumn = ConfigEpisodesColumns._ID;
                res.tablesWithJoins = ConfigEpisodesColumns.TABLE_NAME;
                res.orderBy = ConfigEpisodesColumns.DEFAULT_ORDER;
                break;

            case URI_TYPE_CONFIGURATION:
            case URI_TYPE_CONFIGURATION_ID:
                res.table = ConfigurationColumns.TABLE_NAME;
                res.idColumn = ConfigurationColumns._ID;
                res.tablesWithJoins = ConfigurationColumns.TABLE_NAME;
                res.orderBy = ConfigurationColumns.DEFAULT_ORDER;
                break;

            case URI_TYPE_EPISODES:
            case URI_TYPE_EPISODES_ID:
                res.table = EpisodesColumns.TABLE_NAME;
                res.idColumn = EpisodesColumns._ID;
                res.tablesWithJoins = EpisodesColumns.TABLE_NAME;
                res.orderBy = EpisodesColumns.DEFAULT_ORDER;
                break;

            case URI_TYPE_EPISODES_ACTORS:
            case URI_TYPE_EPISODES_ACTORS_ID:
                res.table = EpisodesActorsColumns.TABLE_NAME;
                res.idColumn = EpisodesActorsColumns._ID;
                res.tablesWithJoins = EpisodesActorsColumns.TABLE_NAME;
                res.orderBy = EpisodesActorsColumns.DEFAULT_ORDER;
                break;

            case URI_TYPE_EPISODES_WRITERS:
            case URI_TYPE_EPISODES_WRITERS_ID:
                res.table = EpisodesWritersColumns.TABLE_NAME;
                res.idColumn = EpisodesWritersColumns._ID;
                res.tablesWithJoins = EpisodesWritersColumns.TABLE_NAME;
                res.orderBy = EpisodesWritersColumns.DEFAULT_ORDER;
                break;

            case URI_TYPE_WRITERS:
            case URI_TYPE_WRITERS_ID:
                res.table = WritersColumns.TABLE_NAME;
                res.idColumn = WritersColumns._ID;
                res.tablesWithJoins = WritersColumns.TABLE_NAME;
                res.orderBy = WritersColumns.DEFAULT_ORDER;
                break;

            case URI_TYPE_WRITERS_EPISODES:
            case URI_TYPE_WRITERS_EPISODES_ID:
                res.table = WritersEpisodesColumns.TABLE_NAME;
                res.idColumn = WritersEpisodesColumns._ID;
                res.tablesWithJoins = WritersEpisodesColumns.TABLE_NAME;
                res.orderBy = WritersEpisodesColumns.DEFAULT_ORDER;
                break;

            default:
                throw new IllegalArgumentException("The uri '" + uri + "' is not supported by this ContentProvider");
        }

        switch (matchedId) {
            case URI_TYPE_ACTORS_ID:
            case URI_TYPE_ACTORS_EPISODES_ID:
            case URI_TYPE_CONFIG_EPISODES_ID:
            case URI_TYPE_CONFIGURATION_ID:
            case URI_TYPE_EPISODES_ID:
            case URI_TYPE_EPISODES_ACTORS_ID:
            case URI_TYPE_EPISODES_WRITERS_ID:
            case URI_TYPE_WRITERS_ID:
            case URI_TYPE_WRITERS_EPISODES_ID:
                id = uri.getLastPathSegment();
        }
        if (id != null) {
            if (selection != null) {
                res.selection = res.table + "." + res.idColumn + "=" + id + " and (" + selection + ")";
            } else {
                res.selection = res.table + "." + res.idColumn + "=" + id;
            }
        } else {
            res.selection = selection;
        }
        return res;
    }
}
