/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.harlie.radiotheater.radiomysterytheater.data;

import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;
import android.util.Log;

import com.harlie.radiotheater.radiomysterytheater.RadioTheaterApplication;
import com.harlie.radiotheater.radiomysterytheater.data.actors.ActorsColumns;
import com.harlie.radiotheater.radiomysterytheater.data.actorsepisodes.ActorsEpisodesColumns;
import com.harlie.radiotheater.radiomysterytheater.data.configepisodes.ConfigEpisodesColumns;
import com.harlie.radiotheater.radiomysterytheater.data.configuration.ConfigurationColumns;
import com.harlie.radiotheater.radiomysterytheater.data.episodes.EpisodesColumns;
import com.harlie.radiotheater.radiomysterytheater.data.episodesactors.EpisodesActorsColumns;
import com.harlie.radiotheater.radiomysterytheater.data.episodeswriters.EpisodesWritersColumns;
import com.harlie.radiotheater.radiomysterytheater.data.writers.WritersColumns;
import com.harlie.radiotheater.radiomysterytheater.data.writersepisodes.WritersEpisodesColumns;
import com.harlie.radiotheater.radiomysterytheater.data_helper.RadioTheaterContract;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.util.HashSet;

import static android.support.test.InstrumentationRegistry.getContext;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@Suite.SuiteClasses({ TestRadioTheaterDb.class })
public class TestRadioTheaterDb {
    private final static String TAG = "LEE: <" + TestRadioTheaterDb.class.getSimpleName() + ">";

    private RadioTheaterHelper helper;
    private RenamingDelegatingContext context;
    private static int testNumber = 0;

    private String TEST_CONFIGURATION_DIR;
    private String TEST_CONFIGEPISODES_DIR;
    private String TEST_EPISODES_DIR;
    private String TEST_EPISODESACTORS_DIR;
    private String TEST_EPISODESWRITERS_DIR;
    private String TEST_ACTORS_DIR;
    private String TEST_ACTORSEPISODES_DIR;
    private String TEST_WRITERS_DIR;
    private String TEST_WRITERSEPISODES_DIR;

    // note: these must match the generated data/RadioTheaterProvider.java
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

    public TestRadioTheaterDb() {
        // content://com.harlie.radiotheater.radiomysterytheater.data.radiotheaterprovider/episodes

        // for some reason I can't get DEX to keep these symbols in my App!
        //java.lang.NoClassDefFoundError: Failed resolution of: Lcom/harlie/radiotheater/radiomysterytheater/data/configuration/ConfigurationColumns;
        //at com.harlie.radiotheater.radiomysterytheater.data.TestRadioTheaterDb.<init>(TestRadioTheaterDb.java:103)

        TEST_CONFIGURATION_DIR = new String(ConfigurationColumns.CONTENT_URI.toString());
        TEST_CONFIGEPISODES_DIR = new String(ConfigEpisodesColumns.CONTENT_URI.toString());
        TEST_EPISODES_DIR = new String(EpisodesColumns.CONTENT_URI.toString());
        TEST_EPISODESACTORS_DIR = new String(EpisodesActorsColumns.CONTENT_URI.toString());
        TEST_EPISODESWRITERS_DIR = new String(EpisodesWritersColumns.CONTENT_URI.toString());
        TEST_ACTORS_DIR = new String(ActorsColumns.CONTENT_URI.toString());
        TEST_ACTORSEPISODES_DIR = new String(ActorsEpisodesColumns.CONTENT_URI.toString());
        TEST_WRITERS_DIR = new String(WritersColumns.CONTENT_URI.toString());
        TEST_WRITERSEPISODES_DIR = new String(WritersEpisodesColumns.CONTENT_URI.toString());
    }

    @Before
    public void setUp() throws Exception {
        Log.v(TAG, "setUp");
        context = new RenamingDelegatingContext(getContext(), "test_");
        helper = RadioTheaterHelper.getInstance(context);
    }

    @After
    public void tearDown() throws Exception {
        Log.v(TAG, "tearDown");
        helper.close();
        getContext().deleteDatabase(RadioTheaterHelper.DATABASE_FILE_NAME);
    }

    // Test that the UriMatcher returns the correct integer value
    // for each of the Uri types the ContentProvider can handle.
    @Test
    public void testUriMatcher() {
        Log.v(TAG, "testUriMatcher");
        UriMatcher testMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        testMatcher.addURI(RadioTheaterProvider.AUTHORITY, ConfigurationColumns.TABLE_NAME, URI_TYPE_CONFIGURATION);
        testMatcher.addURI(RadioTheaterProvider.AUTHORITY, ConfigurationColumns.TABLE_NAME + "/#", URI_TYPE_CONFIGURATION_ID);
        testMatcher.addURI(RadioTheaterProvider.AUTHORITY, ConfigEpisodesColumns.TABLE_NAME, URI_TYPE_CONFIG_EPISODES);
        testMatcher.addURI(RadioTheaterProvider.AUTHORITY, ConfigEpisodesColumns.TABLE_NAME + "/#", URI_TYPE_CONFIG_EPISODES_ID);
        testMatcher.addURI(RadioTheaterProvider.AUTHORITY, EpisodesColumns.TABLE_NAME, URI_TYPE_EPISODES);
        testMatcher.addURI(RadioTheaterProvider.AUTHORITY, EpisodesColumns.TABLE_NAME + "/#", URI_TYPE_EPISODES_ID);
        testMatcher.addURI(RadioTheaterProvider.AUTHORITY, EpisodesActorsColumns.TABLE_NAME, URI_TYPE_EPISODES_ACTORS);
        testMatcher.addURI(RadioTheaterProvider.AUTHORITY, EpisodesActorsColumns.TABLE_NAME + "/#", URI_TYPE_EPISODES_ACTORS_ID);
        testMatcher.addURI(RadioTheaterProvider.AUTHORITY, EpisodesWritersColumns.TABLE_NAME, URI_TYPE_EPISODES_WRITERS);
        testMatcher.addURI(RadioTheaterProvider.AUTHORITY, EpisodesWritersColumns.TABLE_NAME + "/#", URI_TYPE_EPISODES_WRITERS_ID);
        testMatcher.addURI(RadioTheaterProvider.AUTHORITY, ActorsColumns.TABLE_NAME, URI_TYPE_ACTORS);
        testMatcher.addURI(RadioTheaterProvider.AUTHORITY, ActorsColumns.TABLE_NAME + "/#", URI_TYPE_ACTORS_ID);
        testMatcher.addURI(RadioTheaterProvider.AUTHORITY, ActorsEpisodesColumns.TABLE_NAME, URI_TYPE_ACTORS_EPISODES);
        testMatcher.addURI(RadioTheaterProvider.AUTHORITY, ActorsEpisodesColumns.TABLE_NAME + "/#", URI_TYPE_ACTORS_EPISODES_ID);
        testMatcher.addURI(RadioTheaterProvider.AUTHORITY, WritersColumns.TABLE_NAME, URI_TYPE_WRITERS);
        testMatcher.addURI(RadioTheaterProvider.AUTHORITY, WritersColumns.TABLE_NAME + "/#", URI_TYPE_WRITERS_ID);
        testMatcher.addURI(RadioTheaterProvider.AUTHORITY, WritersEpisodesColumns.TABLE_NAME, URI_TYPE_WRITERS_EPISODES);
        testMatcher.addURI(RadioTheaterProvider.AUTHORITY, WritersEpisodesColumns.TABLE_NAME + "/#", URI_TYPE_WRITERS_EPISODES_ID);

        assertEquals("Error: The CONFIGURATION URI was matched incorrectly.",
                testMatcher.match(Uri.parse(TEST_CONFIGURATION_DIR)), testMatcher.match(RadioTheaterContract.ConfigurationEntry.buildConfigurationUri()));
        assertEquals("Error: The CONFIGEPISODES URI was matched incorrectly.",
                testMatcher.match(Uri.parse(TEST_CONFIGEPISODES_DIR)), testMatcher.match(RadioTheaterContract.ConfigEpisodesEntry.buildConfigEpisodesUri()));
        assertEquals("Error: The EPISODES URI was matched incorrectly.",
                testMatcher.match(Uri.parse(TEST_EPISODES_DIR)), testMatcher.match(RadioTheaterContract.EpisodesEntry.buildEpisodesUri()));
        assertEquals("Error: The EPISODESACTORS URI was matched incorrectly.",
                testMatcher.match(Uri.parse(TEST_EPISODESACTORS_DIR)), testMatcher.match(RadioTheaterContract.EpisodesActorsEntry.buildEpisodesActorsUri()));
        assertEquals("Error: The EPISODESWRITERS URI was matched incorrectly.",
                testMatcher.match(Uri.parse(TEST_EPISODESWRITERS_DIR)), testMatcher.match(RadioTheaterContract.EpisodesWritersEntry.buildEpisodesWritersUri()));
        assertEquals("Error: The ACTORS URI was matched incorrectly.",
                testMatcher.match(Uri.parse(TEST_ACTORS_DIR)), testMatcher.match(RadioTheaterContract.ActorsEntry.buildActorsUri()));
        assertEquals("Error: The ACTORSEPISODES URI was matched incorrectly.",
                testMatcher.match(Uri.parse(TEST_ACTORSEPISODES_DIR)), testMatcher.match(RadioTheaterContract.ActorsEpisodesEntry.buildActorsEpisodesUri()));
        assertEquals("Error: The WRITERS URI was matched incorrectly.",
                testMatcher.match(Uri.parse(TEST_WRITERS_DIR)), testMatcher.match(RadioTheaterContract.WritersEntry.buildWritersUri()));
        assertEquals("Error: The WRITERSEPISODES URI was matched incorrectly.",
                testMatcher.match(Uri.parse(TEST_WRITERSEPISODES_DIR)), testMatcher.match(RadioTheaterContract.WritersEpisodesEntry.buildWritersEpisodesUri()));
    }

    @Test
    public void testCreateDb() throws Throwable {
        Log.v(TAG, "testCreateDb");
        // build a HashSet of all of the table names we wish to look for
        // Note that there will be another table in the DB that stores the
        // Android metadata (db version information)
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(RadioTheaterContract.ConfigurationEntry.TABLE_NAME);
        tableNameHashSet.add(RadioTheaterContract.ConfigEpisodesEntry.TABLE_NAME);
        tableNameHashSet.add(RadioTheaterContract.EpisodesEntry.TABLE_NAME);
        tableNameHashSet.add(RadioTheaterContract.EpisodesActorsEntry.TABLE_NAME);
        tableNameHashSet.add(RadioTheaterContract.EpisodesWritersEntry.TABLE_NAME);
        tableNameHashSet.add(RadioTheaterContract.ActorsEntry.TABLE_NAME);
        tableNameHashSet.add(RadioTheaterContract.ActorsEpisodesEntry.TABLE_NAME);
        tableNameHashSet.add(RadioTheaterContract.WritersEntry.TABLE_NAME);
        tableNameHashSet.add(RadioTheaterContract.WritersEpisodesEntry.TABLE_NAME);

        getContext().deleteDatabase(RadioTheaterHelper.DATABASE_FILE_NAME);
        SQLiteDatabase db = helper.getWritableDatabase();
        assertEquals(true, db.isOpen());

        // have we created the tables we want?
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        assertTrue("Error: This means that the database has not been created correctly", cursor.moveToFirst());

        // verify that the tables have been created
        do {
            tableNameHashSet.remove(cursor.getString(0));
        } while (cursor.moveToNext());
        cursor.close();
        cursor = null;

        // if this fails, it means that the database doesn't contain all tables
        assertTrue("Error: Your database is missing tables!", tableNameHashSet.isEmpty());

        Log.v(TAG, "begin testing table columns..");
        // Build a HashSet of all of the column names we want to look for
        HashSet<String> allColumnsHashSet;

        //------------------------------
        // columns for the episodes table
        allColumnsHashSet = new HashSet<String>();
        allColumnsHashSet.add(RadioTheaterContract.EpisodesEntry._ID);
        allColumnsHashSet.add(RadioTheaterContract.EpisodesEntry.FIELD_EPISODE_NUMBER);
        allColumnsHashSet.add(RadioTheaterContract.EpisodesEntry.FIELD_AIRDATE);
        allColumnsHashSet.add(RadioTheaterContract.EpisodesEntry.FIELD_EPISODE_TITLE);
        allColumnsHashSet.add(RadioTheaterContract.EpisodesEntry.FIELD_EPISODE_DESCRIPTION);
        allColumnsHashSet.add(RadioTheaterContract.EpisodesEntry.FIELD_DOWNLOAD_URL);
        allColumnsHashSet.add(RadioTheaterContract.EpisodesEntry.FIELD_WEBLINK_URL);
        allColumnsHashSet.add(RadioTheaterContract.EpisodesEntry.FIELD_RATING);

        // now, do our tables contain the correct columns?
        Cursor episodesCursor = db.rawQuery("PRAGMA table_info(" + RadioTheaterContract.EpisodesEntry.TABLE_NAME + ")", null);
        assertTrue("Error: This means that we were unable to query the database for 'episodes' table information.", episodesCursor.moveToFirst());
        checkAllColumns(episodesCursor, allColumnsHashSet);
        episodesCursor.close();

        //------------------------------
        // columns for the actors table
        allColumnsHashSet = new HashSet<String>();
        allColumnsHashSet.add(RadioTheaterContract.ActorsEntry._ID);
        allColumnsHashSet.add(RadioTheaterContract.ActorsEntry.FIELD_ACTOR_NAME);
        allColumnsHashSet.add(RadioTheaterContract.ActorsEntry.FIELD_ACTOR_URL);
        allColumnsHashSet.add(RadioTheaterContract.ActorsEntry.FIELD_ACTOR_BIO);

        // now, do our tables contain the correct columns?
        Cursor actorsCursor = db.rawQuery("PRAGMA table_info(" + RadioTheaterContract.ActorsEntry.TABLE_NAME + ")", null);
        assertTrue("Error: This means that we were unable to query the database for 'actors' table information.", actorsCursor.moveToFirst());
        checkAllColumns(actorsCursor, allColumnsHashSet);
        actorsCursor.close();

        //------------------------------
        // columns for the writers table
        allColumnsHashSet = new HashSet<String>();
        allColumnsHashSet.add(RadioTheaterContract.WritersEntry._ID);
        allColumnsHashSet.add(RadioTheaterContract.WritersEntry.FIELD_WRITER_NAME);
        allColumnsHashSet.add(RadioTheaterContract.WritersEntry.FIELD_WRITER_URL);
        allColumnsHashSet.add(RadioTheaterContract.WritersEntry.FIELD_WRITER_BIO);

        // now, do our tables contain the correct columns?
        Cursor writersCursor = db.rawQuery("PRAGMA table_info(" + RadioTheaterContract.WritersEntry.TABLE_NAME + ")", null);
        assertTrue("Error: This means that we were unable to query the database for 'writers' table information.", writersCursor.moveToFirst());
        checkAllColumns(writersCursor, allColumnsHashSet);
        writersCursor.close();

        db.close();
    }

    private void checkAllColumns(Cursor cursor, HashSet<String> columnsHashSet) {
        Log.v(TAG, "checkAllColumns");
        Log.v(TAG, "begin with columnsHashSet=" + columnsHashSet);
        int columnNameIndex = cursor.getColumnIndex("name");
        do {
            String columnName = cursor.getString(columnNameIndex);
            Log.v(TAG, "found column " + columnName);
            columnsHashSet.remove(columnName);
        } while (cursor.moveToNext());

        // if this fails, it means that the database doesn't contain all of the required columns for table being tested
        Log.v(TAG, "(should be empty) columnsHashSet=" + columnsHashSet);
        assertTrue("Error: The database doesn't contain all of the required columns!", columnsHashSet.isEmpty());
    }

    private void validateDatabase(SQLiteDatabase db, String tableName, ContentValues someValues) {
        Log.v(TAG, "validateDatabase");

        // Query the database and receive a Cursor back
        Cursor someCursor = db.query(
                tableName, // Table to Query
                null,      // leaving "columns" null just returns all the columns.
                null,      // cols for "where" clause
                null,      // values for "where" clause
                null,      // columns to group by
                null,      // columns to filter by row groups
                null       // sort order
        );

        // Move the cursor to the first valid database row and check to see if we have any rows
        assertTrue("Error: No Records returned from query for table " + tableName, someCursor.moveToFirst());

        // Validate the Query
        TestRadioTheaterUtilities.validateCurrentRecord("testInsertReadDb row failed to validate for " + tableName, someCursor, someValues);

        // Move the cursor to demonstrate that there is only one record in the database
        assertFalse("Error: More than one record returned from query for table " + tableName, someCursor.moveToNext());

        someCursor.close();
    }

    // test that we can insert and query the database tables
    @Test
    public void testEpisodeTables() {
        Log.v(TAG, "testEpisodeTables");
        // First insert the episode, and then use the episodeRowId to insert a actor.
        // Then use the episodeRowId to insert a episode writer.

        ++testNumber;

        ContentValues episodeValues = TestRadioTheaterUtilities.createEpisodeValues(testNumber);
        assertTrue(episodeValues != null);

        // Create actor values for the episode
        long actor_id = 1;
        ContentValues actorValues = TestRadioTheaterUtilities.createActorValues(actor_id, testNumber);
        assertTrue(actorValues != null);

        // Create writer values for the episode
        long writer_id = 1;
        ContentValues writerValues = TestRadioTheaterUtilities.createWritersValues(writer_id, testNumber);
        assertTrue(writerValues != null);

        long episodeRowId = insertAndVerifyEpisode(episodeValues);
        Log.v(TAG, "testEpisodeTables: episodeRowId=" + episodeRowId);

        // Make sure we have a valid row ID.  should be the 2nd row
        assertFalse("Error: Episode Not Inserted Correctly", episodeRowId == -1L);

        // If there's an error in the SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        SQLiteDatabase db = helper.getWritableDatabase();
        assertFalse("getWriteableDatabase must be WRITABLE!", db.isReadOnly());

        /*
        // Insert actor ContentValues into database and get a actor row ID back
        long actorRowId = db.insert(RadioTheaterContract.ActorsEntry.TABLE_NAME, null, actorValues);
        Log.v(TAG, "testEpisodeTables: actorRowId=" + actorRowId);
        assertTrue(actorRowId != -1);
        */

        Uri result;
        result = insertActorValues(actorValues);
        long actorRowId = Long.valueOf(result.toString().lastIndexOf('/')+1);
        Log.v(TAG, "Actor insert actorRowId="+actorRowId+", result="+result);

        /*
        // Insert writer ContentValues into database and get a writer row ID back
        long writerRowId = db.insert(RadioTheaterContract.WritersEntry.TABLE_NAME, null, writerValues);
        Log.v(TAG, "testEpisodeTables: writerRowId=" + writerRowId);
        assertTrue(writerRowId != -1);
        */

        result = insertWriterValues(writerValues);
        long writerRowId = Long.valueOf(result.toString().lastIndexOf('/')+1);
        Log.v(TAG, "Writer insert writerRowId="+writerRowId+", result="+result);

        Log.i(TAG, "NOTE: If the above writes for Episode, Actor and Writer worked, then we have a working Content Provider.");

        validateDatabase(db, RadioTheaterContract.EpisodesEntry.TABLE_NAME, episodeValues);
        validateDatabase(db, RadioTheaterContract.ActorsEntry.TABLE_NAME, actorValues);
        validateDatabase(db, RadioTheaterContract.WritersEntry.TABLE_NAME, writerValues);
    }

    private long insertAndVerifyEpisode(ContentValues episodeValues) {
        Log.v(TAG, "insertAndVerifyEpisode");
        SQLiteDatabase db = helper.getWritableDatabase();
        assertFalse("getWriteableDatabase must be WRITABLE!", db.isReadOnly());

        // Insert ContentValues into database and get a episode row ID back
        Uri result;
        result = insertEpisodeValues(episodeValues);
        Log.v(TAG, "Episode insert result="+result);

        long episodeRowId = Long.valueOf(result.toString().lastIndexOf('/')+1);
        //episodeRowId = db.insert(RadioTheaterContract.EpisodesEntry.TABLE_NAME, null, episodeValues);
        Log.v(TAG, "insertAndVerifyEpisode: episodeRowId=" + episodeRowId);

        // Verify we got a row back.
        assertTrue(episodeRowId != -1);

        // verify the data inserted properly..
        Cursor cursor = db.query(
                RadioTheaterContract.EpisodesEntry.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        // Move the cursor to a valid database row and check to see if we got any records back from the query
        assertTrue("Error: No Records returned from episode query", cursor.moveToFirst());

        // Validate data in resulting Cursor with the original ContentValues
        TestRadioTheaterUtilities.validateCurrentRecord("Error: Episode Query Validation Failed", cursor, episodeValues);

        // Move the cursor to demonstrate that there is only one record in the database
        assertFalse("Error: More than one record returned from episode query", cursor.moveToNext());

        // Close Cursor and Database
        cursor.close();
        return episodeRowId;
    }

    private Uri insertEpisodeValues(ContentValues episodeValues) {
        Log.v(TAG, "insertEpisodeValues=");
        Uri episode = RadioTheaterContract.EpisodesEntry.buildEpisodesUri();
        return context.getContentResolver().insert(episode, episodeValues);
    }

    private Uri insertActorValues(ContentValues actorValues) {
        Log.v(TAG, "insertActorValues");
        Uri actor = RadioTheaterContract.ActorsEntry.buildActorsUri();
        return context.getContentResolver().insert(actor, actorValues);
    }

    private Uri insertWriterValues(ContentValues writerValues) {
        Log.v(TAG, "insertWriterValues");
        Uri writer = RadioTheaterContract.WritersEntry.buildWritersUri();
        return context.getContentResolver().insert(writer, writerValues);
    }

}
