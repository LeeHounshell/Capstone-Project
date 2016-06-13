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

import android.annotation.TargetApi;
import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.DefaultDatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;

import com.harlie.radiotheater.radiomysterytheater.BuildConfig;
import com.harlie.radiotheater.radiomysterytheater.data.actors.ActorsColumns;
import com.harlie.radiotheater.radiomysterytheater.data.actorsepisodes.ActorsEpisodesColumns;
import com.harlie.radiotheater.radiomysterytheater.data.configepisodes.ConfigEpisodesColumns;
import com.harlie.radiotheater.radiomysterytheater.data.configuration.ConfigurationColumns;
import com.harlie.radiotheater.radiomysterytheater.data.episodes.EpisodesColumns;
import com.harlie.radiotheater.radiomysterytheater.data.episodesactors.EpisodesActorsColumns;
import com.harlie.radiotheater.radiomysterytheater.data.episodeswriters.EpisodesWritersColumns;
import com.harlie.radiotheater.radiomysterytheater.data.writers.WritersColumns;
import com.harlie.radiotheater.radiomysterytheater.data.writersepisodes.WritersEpisodesColumns;

public class RadioTheaterHelper extends SQLiteOpenHelper {
    private static final String TAG = RadioTheaterHelper.class.getSimpleName();

    public static final String DATABASE_FILE_NAME = "radiomysterytheater.db";
    private static final int DATABASE_VERSION = 1;
    private static RadioTheaterHelper sInstance;
    private final Context mContext;
    private final RadioTheaterHelperCallbacks mOpenHelperCallbacks;

    // @formatter:off
    public static final String SQL_CREATE_TABLE_ACTORS = "CREATE TABLE IF NOT EXISTS "
            + ActorsColumns.TABLE_NAME + " ( "
            + ActorsColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + ActorsColumns.FIELD_ACTOR_ID + " INTEGER NOT NULL, "
            + ActorsColumns.FIELD_ACTOR_NAME + " TEXT NOT NULL, "
            + ActorsColumns.FIELD_ACTOR_URL + " TEXT NOT NULL, "
            + ActorsColumns.FIELD_ACTOR_BIO + " TEXT "
            + ", CONSTRAINT field_unique UNIQUE (field_actor_name) ON CONFLICT ABORT"
            + " );";

    public static final String SQL_CREATE_TABLE_ACTORS_EPISODES = "CREATE TABLE IF NOT EXISTS "
            + ActorsEpisodesColumns.TABLE_NAME + " ( "
            + ActorsEpisodesColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + ActorsEpisodesColumns.FIELD_ACTOR_ID + " INTEGER NOT NULL, "
            + ActorsEpisodesColumns.FIELD_EPISODE_NUMBER + " INTEGER NOT NULL "
            + ", CONSTRAINT field_unique UNIQUE (field_actor_id, field_episode_number) ON CONFLICT ABORT"
            + " );";

    public static final String SQL_CREATE_TABLE_CONFIG_EPISODES = "CREATE TABLE IF NOT EXISTS "
            + ConfigEpisodesColumns.TABLE_NAME + " ( "
            + ConfigEpisodesColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + ConfigEpisodesColumns.FIELD_EPISODE_NUMBER + " INTEGER NOT NULL, "
            + ConfigEpisodesColumns.FIELD_PURCHASE_ACCESS + " INTEGER NOT NULL DEFAULT 0, "
            + ConfigEpisodesColumns.FIELD_PURCHASE_NOADS + " INTEGER NOT NULL DEFAULT 0, "
            + ConfigEpisodesColumns.FIELD_EPISODE_PERMISION + " INTEGER NOT NULL DEFAULT 0, "
            + ConfigEpisodesColumns.FIELD_EPISODE_HEARD + " INTEGER NOT NULL DEFAULT 0, "
            + ConfigEpisodesColumns.FIELD_LISTEN_COUNT + " INTEGER NOT NULL DEFAULT 0 "
            + ", CONSTRAINT field_unique UNIQUE (field_episode_number) ON CONFLICT ABORT"
            + " );";

    public static final String SQL_CREATE_TABLE_CONFIGURATION = "CREATE TABLE IF NOT EXISTS "
            + ConfigurationColumns.TABLE_NAME + " ( "
            + ConfigurationColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + ConfigurationColumns.FIELD_USER_EMAIL + " TEXT NOT NULL, "
            + ConfigurationColumns.FIELD_USER_NAME + " TEXT DEFAULT 'unknown', "
            + ConfigurationColumns.FIELD_AUTHENTICATED + " INTEGER DEFAULT 0, "
            + ConfigurationColumns.FIELD_DEVICE_ID + " TEXT, "
            + ConfigurationColumns.FIELD_PAID_VERSION + " INTEGER NOT NULL DEFAULT 0, "
            + ConfigurationColumns.FIELD_PURCHASE_ACCESS + " INTEGER NOT NULL DEFAULT 0, "
            + ConfigurationColumns.FIELD_PURCHASE_NOADS + " INTEGER NOT NULL DEFAULT 0, "
            + ConfigurationColumns.FIELD_TOTAL_LISTEN_COUNT + " INTEGER NOT NULL DEFAULT 0 "
            + ", CONSTRAINT fk_field_user_email FOREIGN KEY (" + ConfigurationColumns.FIELD_USER_EMAIL + ") REFERENCES config_episodes (_id) ON DELETE CASCADE"
            + ", CONSTRAINT field_unique UNIQUE (field_user_email) ON CONFLICT ABORT"
            + " );";

    public static final String SQL_CREATE_TABLE_EPISODES = "CREATE TABLE IF NOT EXISTS "
            + EpisodesColumns.TABLE_NAME + " ( "
            + EpisodesColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + EpisodesColumns.FIELD_EPISODE_NUMBER + " INTEGER NOT NULL, "
            + EpisodesColumns.FIELD_AIRDATE + " INTEGER NOT NULL, "
            + EpisodesColumns.FIELD_EPISODE_TITLE + " TEXT NOT NULL, "
            + EpisodesColumns.FIELD_EPISODE_DESCRIPTION + " TEXT NOT NULL, "
            + EpisodesColumns.FIELD_WEBLINK_URL + " TEXT, "
            + EpisodesColumns.FIELD_DOWNLOAD_URL + " TEXT NOT NULL, "
            + EpisodesColumns.FIELD_RATING + " INTEGER, "
            + EpisodesColumns.FIELD_VOTE_COUNT + " INTEGER "
            + ", CONSTRAINT field_unique UNIQUE (field_episode_number) ON CONFLICT ABORT"
            + " );";

    public static final String SQL_CREATE_TABLE_EPISODES_ACTORS = "CREATE TABLE IF NOT EXISTS "
            + EpisodesActorsColumns.TABLE_NAME + " ( "
            + EpisodesActorsColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + EpisodesActorsColumns.FIELD_EPISODE_NUMBER + " INTEGER NOT NULL, "
            + EpisodesActorsColumns.FIELD_ACTOR_ID + " INTEGER NOT NULL "
            + ", CONSTRAINT field_unique UNIQUE (field_episode_number, field_actor_id) ON CONFLICT ABORT"
            + " );";

    public static final String SQL_CREATE_TABLE_EPISODES_WRITERS = "CREATE TABLE IF NOT EXISTS "
            + EpisodesWritersColumns.TABLE_NAME + " ( "
            + EpisodesWritersColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + EpisodesWritersColumns.FIELD_EPISODE_NUMBER + " INTEGER NOT NULL, "
            + EpisodesWritersColumns.FIELD_WRITER_ID + " INTEGER NOT NULL "
            + ", CONSTRAINT field_unique UNIQUE (field_episode_number, field_writer_id) ON CONFLICT ABORT"
            + " );";

    public static final String SQL_CREATE_TABLE_WRITERS = "CREATE TABLE IF NOT EXISTS "
            + WritersColumns.TABLE_NAME + " ( "
            + WritersColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + WritersColumns.FIELD_WRITER_ID + " INTEGER NOT NULL, "
            + WritersColumns.FIELD_WRITER_NAME + " TEXT NOT NULL, "
            + WritersColumns.FIELD_WRITER_URL + " TEXT NOT NULL, "
            + WritersColumns.FIELD_WRITER_BIO + " TEXT "
            + ", CONSTRAINT field_unique UNIQUE (field_writer_name) ON CONFLICT ABORT"
            + " );";

    public static final String SQL_CREATE_TABLE_WRITERS_EPISODES = "CREATE TABLE IF NOT EXISTS "
            + WritersEpisodesColumns.TABLE_NAME + " ( "
            + WritersEpisodesColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + WritersEpisodesColumns.FIELD_WRITER_ID + " INTEGER NOT NULL, "
            + WritersEpisodesColumns.FIELD_EPISODE_NUMBER + " INTEGER NOT NULL "
            + ", CONSTRAINT field_unique UNIQUE (field_writer_id, field_episode_number) ON CONFLICT ABORT"
            + " );";

    // @formatter:on

    public static RadioTheaterHelper getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = newInstance(context.getApplicationContext());
        }
        return sInstance;
    }

    private static RadioTheaterHelper newInstance(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            return newInstancePreHoneycomb(context);
        }
        return newInstancePostHoneycomb(context);
    }


    /*
     * Pre Honeycomb.
     */
    private static RadioTheaterHelper newInstancePreHoneycomb(Context context) {
        return new RadioTheaterHelper(context);
    }

    private RadioTheaterHelper(Context context) {
        super(context, DATABASE_FILE_NAME, null, DATABASE_VERSION);
        mContext = context;
        mOpenHelperCallbacks = new RadioTheaterHelperCallbacks();
    }


    /*
     * Post Honeycomb.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static RadioTheaterHelper newInstancePostHoneycomb(Context context) {
        return new RadioTheaterHelper(context, new DefaultDatabaseErrorHandler());
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private RadioTheaterHelper(Context context, DatabaseErrorHandler errorHandler) {
        super(context, DATABASE_FILE_NAME, null, DATABASE_VERSION, errorHandler);
        mContext = context;
        mOpenHelperCallbacks = new RadioTheaterHelperCallbacks();
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");
        mOpenHelperCallbacks.onPreCreate(mContext, db);
        db.execSQL(SQL_CREATE_TABLE_ACTORS);
        db.execSQL(SQL_CREATE_TABLE_ACTORS_EPISODES);
        db.execSQL(SQL_CREATE_TABLE_CONFIG_EPISODES);
        db.execSQL(SQL_CREATE_TABLE_CONFIGURATION);
        db.execSQL(SQL_CREATE_TABLE_EPISODES);
        db.execSQL(SQL_CREATE_TABLE_EPISODES_ACTORS);
        db.execSQL(SQL_CREATE_TABLE_EPISODES_WRITERS);
        db.execSQL(SQL_CREATE_TABLE_WRITERS);
        db.execSQL(SQL_CREATE_TABLE_WRITERS_EPISODES);
        mOpenHelperCallbacks.onPostCreate(mContext, db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            setForeignKeyConstraintsEnabled(db);
        }
        mOpenHelperCallbacks.onOpen(mContext, db);
    }

    private void setForeignKeyConstraintsEnabled(SQLiteDatabase db) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            setForeignKeyConstraintsEnabledPreJellyBean(db);
        } else {
            setForeignKeyConstraintsEnabledPostJellyBean(db);
        }
    }

    private void setForeignKeyConstraintsEnabledPreJellyBean(SQLiteDatabase db) {
        db.execSQL("PRAGMA foreign_keys=ON;");
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setForeignKeyConstraintsEnabledPostJellyBean(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        mOpenHelperCallbacks.onUpgrade(mContext, db, oldVersion, newVersion);
    }
}
