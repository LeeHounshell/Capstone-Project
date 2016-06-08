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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.RenamingDelegatingContext;
import android.util.Log;

import com.harlie.radiotheater.radiomysterytheater.data_helper.RadioTheaterContract;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;

import static android.support.test.InstrumentationRegistry.getContext;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class TestRadioTheaterDb {
    private final static String TAG = "LEE: <" + TestRadioTheaterDb.class.getSimpleName() + ">";

    private RadioTheaterHelper helper;

    @Before
    public void setUp() throws Exception {
        Log.v(TAG, "setUp");
        RenamingDelegatingContext context = new RenamingDelegatingContext(getContext(), "test_");
        helper = RadioTheaterHelper.getInstance(context);
    }

    @After
    public void tearDown() throws Exception {
        Log.v(TAG, "tearDown");
        helper.close();
        getContext().deleteDatabase(RadioTheaterHelper.DATABASE_FILE_NAME);
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

        int testNumber = 1;
        ContentValues episodeValues = TestRadioTheaterUtilities.createEpisodeValues(testNumber);
        long episodeRowId = insertAndVerifyEpisode(episodeValues);
        Log.v(TAG, "testEpisodeTables: episodeRowId=" + episodeRowId);

        // Make sure we have a valid row ID.
        assertFalse("Error: Episode Not Inserted Correctly", episodeRowId == -1L);

        // If there's an error in the SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        SQLiteDatabase db = helper.getWritableDatabase();
        assertFalse("getWriteableDatabase must be WRITABLE!", db.isReadOnly());

        // Create actor values for the episode
        ContentValues actorValues = TestRadioTheaterUtilities.createActorValues(episodeRowId, testNumber);
        assertTrue(actorValues != null);

        // Insert actor ContentValues into database and get a actor row ID back
        long actorRowId = db.insert(RadioTheaterContract.ActorsEntry.TABLE_NAME, null, actorValues);
        Log.v(TAG, "testEpisodeTables: actorRowId=" + actorRowId);
        assertTrue(actorRowId != -1);

        // Create writer values for the episode
        ContentValues writerValues = TestRadioTheaterUtilities.createWritersValues(episodeRowId, testNumber);
        assertTrue(writerValues != null);

        // Insert writer ContentValues into database and get a writer row ID back
        long writerRowId = db.insert(RadioTheaterContract.WritersEntry.TABLE_NAME, null, writerValues);
        Log.v(TAG, "testEpisodeTables: writerRowId=" + writerRowId);
        assertTrue(writerRowId != -1);

        validateDatabase(db, RadioTheaterContract.EpisodesEntry.TABLE_NAME, episodeValues);
        validateDatabase(db, RadioTheaterContract.ActorsEntry.TABLE_NAME, actorValues);
        validateDatabase(db, RadioTheaterContract.WritersEntry.TABLE_NAME, writerValues);
    }


    private long insertAndVerifyEpisode(ContentValues episodeValues) {
        Log.v(TAG, "insertAndVerifyEpisode");
        SQLiteDatabase db = helper.getWritableDatabase();
        assertFalse("getWriteableDatabase must be WRITABLE!", db.isReadOnly());

        // Insert ContentValues into database and get a episode row ID back
        long episodeRowId;
        episodeRowId = db.insert(RadioTheaterContract.EpisodesEntry.TABLE_NAME, null, episodeValues);
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

}
