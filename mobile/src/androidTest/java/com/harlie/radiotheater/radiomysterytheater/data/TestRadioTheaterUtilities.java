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
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.test.RenamingDelegatingContext;
import android.util.Log;

import com.harlie.radiotheater.radiomysterytheater.data_helper.RadioTheaterContract;
import com.harlie.radiotheater.radiomysterytheater.util.PollingCheck;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.Set;

import static android.support.test.InstrumentationRegistry.getContext;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

// Functions and test data to make it easier to test the database and Content Provider.
public class TestRadioTheaterUtilities {
    private final static String TAG = "LEE: <" + TestRadioTheaterUtilities.class.getSimpleName() + ">";

    private static RadioTheaterHelper helper;

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

    static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        Log.v(TAG, "validateCursor");
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }

    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Log.v(TAG, "validateCurrentRecord");
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }

    // setup content for a default episode row for database tests.
    static ContentValues createEpisodeValues(int testNumber) {
        Log.v(TAG, "createEpisodeValues");
        ContentValues episodeValues = new ContentValues();
        switch (testNumber) {
            case 1:
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_EPISODE_NUMBER, "0001");
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_AIRDATE, "1974-01-06");
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_EPISODE_TITLE, "The Old Ones Are Hard to Kill");
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_EPISODE_DESCRIPTION, "An old lady rents a room to a sick boarder. She runs into problems with his strange deathbed confession.");
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_DOWNLOAD_URL, "www.cbsrmt.com/mp3/CBS Radio Mystery Theater 74-01-06 e0001 The Old Ones Are Hard to Kill.mp3");
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_WEBLINK_URL, "www.cbsrmt.com/episode_name-1-the-old-ones-are-hard-to-kill.html");
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_RATING, 3.2);
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_VOTE_COUNT, 1);
                break;
            case 2:
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_EPISODE_NUMBER, "0002");
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_AIRDATE, "1974-01-07");
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_EPISODE_TITLE, "The Return of the Moresbys");
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_EPISODE_DESCRIPTION, "A husband kills his wife for donating all their money. Now, he is certain that she has been reincarnated in the form of a cat to wreak revenge on him.");
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_DOWNLOAD_URL, "www.cbsrmt.com/mp3/CBS Radio Mystery Theater 74-01-07 e0002 The Return of the Moresbys.mp3");
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_WEBLINK_URL, "www.cbsrmt.com/episode_name-2-the-return-of-the-moresbys.html");
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_RATING, 2.8);
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_VOTE_COUNT, 1);
                break;
            case 3:
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_EPISODE_NUMBER, "0003");
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_AIRDATE, "1974-01-08");
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_EPISODE_TITLE, "The Bullet");
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_EPISODE_DESCRIPTION, "An accident kills a man but he is made to return to Earth to trade places with the fated victim.");
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_DOWNLOAD_URL, "www.cbsrmt.com/mp3/CBS Radio Mystery Theater 74-01-08 e0003 The Bullet.mp3");
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_WEBLINK_URL, "www.cbsrmt.com/episode_name-3-the-bullet.html");
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_RATING, 3.4);
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_VOTE_COUNT, 1);
                break;
            case 4:
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_EPISODE_NUMBER, "0004");
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_AIRDATE, "1974-01-09");
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_EPISODE_TITLE, "Lost Dog");
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_EPISODE_DESCRIPTION, "A husband cannot appreciate his wife's phobia of dogs. Once he gets one home, his wife sets it against him.");
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_DOWNLOAD_URL, "www.cbsrmt.com/mp3/CBS Radio Mystery Theater 74-01-09 e0004 Lost Dog.mp3");
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_WEBLINK_URL, "www.cbsrmt.com/episode_name-4-lost-dog.html");
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_RATING, 3.2);
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_VOTE_COUNT, 1);
                break;
            case 5:
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_EPISODE_NUMBER, "0005");
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_AIRDATE, "1974-01-10");
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_EPISODE_TITLE, "No Hiding Place");
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_EPISODE_DESCRIPTION, "The would-be CEO of his father-in-law's corporation is threatened with the revelation of a dark deed in his past.");
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_DOWNLOAD_URL, "www.cbsrmt.com/mp3/CBS Radio Mystery Theater 74-01-10 e0005 No Hiding Place.mp3");
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_WEBLINK_URL, "www.cbsrmt.com/episode_name-5-no-hiding-place.html");
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_RATING, 3.3);
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_VOTE_COUNT, 1);
                break;
            case 6:
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_EPISODE_NUMBER, "0006");
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_AIRDATE, "1974-01-11");
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_EPISODE_TITLE, "Honeymoon with Death");
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_EPISODE_DESCRIPTION, "Her husband disappears after their return from the honeymoon and the young wife's sister insists that the nuptial was a mere fantasy. She attempts to persuade a cop that her husband was indeed killed.");
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_DOWNLOAD_URL, "www.cbsrmt.com/mp3/CBS Radio Mystery Theater 74-01-11 e0006 Honeymoon with Death.mp3");
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_WEBLINK_URL, "www.cbsrmt.com/episode_name-6-honeymoon-with-death.html");
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_RATING, 3.5);
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_VOTE_COUNT, 1);
                break;
            case 7:
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_EPISODE_NUMBER, "0007");
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_AIRDATE, "1974-01-12");
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_EPISODE_TITLE, "I Warn You Three Times");
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_EPISODE_DESCRIPTION, "A daring attempt to clear their car windows during a snowstorm leads to the mysterious disappearance of a woman's husband. The reasons turn out to be increasingly intriguing.");
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_DOWNLOAD_URL, "www.cbsrmt.com/mp3/CBS Radio Mystery Theater 74-01-12 e0007 I Warn You Three Times.mp3");
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_WEBLINK_URL, "www.cbsrmt.com/episode_name-7-i-warn-you-three-times.html");
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_RATING, 3.5);
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_VOTE_COUNT, 1);
                break;
            case 8:
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_EPISODE_NUMBER, "0008");
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_AIRDATE, "1974-01-13");
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_EPISODE_DESCRIPTION, "Dr. Ryan Stone (Sandra Bullock), a brilliant medical engineer on her first Shuttle mission, with veteran astronaut Matt Kowalsky (George Clooney) in command of his last flight before retiring.");
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_EPISODE_TITLE, "Cold Storage");
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_DOWNLOAD_URL, "www.cbsrmt.com/mp3/CBS Radio Mystery Theater 74-01-13 e0008 Cold Storage.mp3");
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_WEBLINK_URL, "www.cbsrmt.com/episode_name-8-cold-storage.html");
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_RATING, 3.6);
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_VOTE_COUNT, 1);
                break;
            case 9:
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_EPISODE_NUMBER, "0009");
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_AIRDATE, "1974-01-14");
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_EPISODE_TITLE, "WALL*E");
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_EPISODE_DESCRIPTION, "An old flame torments a playboy after her death in a horse riding accident.");
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_DOWNLOAD_URL, "www.cbsrmt.com/mp3/CBS Radio Mystery Theater 74-01-14 e0009 Death Rides a Stallion.mp3");
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_WEBLINK_URL, "www.cbsrmt.com/episode_name-9-death-rides-a-stallion.html");
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_RATING, 3.3);
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_VOTE_COUNT, 1);
                break;
            case 10:
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_EPISODE_NUMBER, "0010");
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_AIRDATE, "1974-01-15");
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_EPISODE_TITLE, "The Resident");
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_EPISODE_DESCRIPTION, "A move from the countryside to town leaves a retired lady prey to a cat named Evil and a young girl who forcefully moves in with her!");
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_DOWNLOAD_URL, "www.cbsrmt.com/mp3/CBS Radio Mystery Theater 74-01-15 e0010 The Resident.mp3");
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_WEBLINK_URL, "www.cbsrmt.com/episode_name-10-the-resident.html");
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_RATING, 1);
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_VOTE_COUNT, 1);
                break;
            default:
                assertTrue("invalid value for testNumber=" + testNumber, false);
        }
        return episodeValues;
    }

    private static long insertEpisodeValues(Context context, int testNumber) {
        Log.v(TAG, "insertEpisodeValues");
        // insert test record into the database
        SQLiteDatabase db = helper.getWritableDatabase();
        assertFalse("getWriteableDatabase must be WRITABLE!", db.isReadOnly());
        ContentValues testValues = TestRadioTheaterUtilities.createEpisodeValues(testNumber);
        long episodeRowId = db.insert(RadioTheaterContract.EpisodesEntry.TABLE_NAME, null, testValues);
        // Verify we got a row back.
        Log.v(TAG, "insertEpisodeValues: episodeRowId=" + episodeRowId);
        assertTrue("Error: Failure to insert Episode Values", episodeRowId != -1);
        return episodeRowId;
    }

    // setup content for a default actor row for database tests.
    static ContentValues createActorValues(long actor_id, int testNumber) {
        Log.v(TAG, "createActorValues");
        ContentValues actorValues = new ContentValues();
        switch (testNumber) {
            case 1:
                actorValues.put(RadioTheaterContract.ActorsEntry.FIELD_ACTOR_ID, actor_id);
                actorValues.put(RadioTheaterContract.ActorsEntry.FIELD_ACTOR_NAME, "DeKoven, Roger");
                actorValues.put(RadioTheaterContract.ActorsEntry.FIELD_ACTOR_URL, "dekoven_roger.jpg");
                break;
            case 2:
                actorValues.put(RadioTheaterContract.ActorsEntry.FIELD_ACTOR_ID, actor_id);
                actorValues.put(RadioTheaterContract.ActorsEntry.FIELD_ACTOR_NAME, "Janney, Leon");
                actorValues.put(RadioTheaterContract.ActorsEntry.FIELD_ACTOR_URL, "janney_leon.jpg");
                break;
            case 3:
                actorValues.put(RadioTheaterContract.ActorsEntry.FIELD_ACTOR_ID, actor_id);
                actorValues.put(RadioTheaterContract.ActorsEntry.FIELD_ACTOR_NAME, "Moorehead, Agnes");
                actorValues.put(RadioTheaterContract.ActorsEntry.FIELD_ACTOR_URL, "moorehead_agnes.jpg");
                break;
            case 4:
                actorValues.put(RadioTheaterContract.ActorsEntry.FIELD_ACTOR_ID, actor_id);
                actorValues.put(RadioTheaterContract.ActorsEntry.FIELD_ACTOR_NAME, "O'Neal, Patrick");
                actorValues.put(RadioTheaterContract.ActorsEntry.FIELD_ACTOR_URL, "oneal_patrick.jpg");
                break;
            case 5:
                actorValues.put(RadioTheaterContract.ActorsEntry.FIELD_ACTOR_ID, actor_id);
                actorValues.put(RadioTheaterContract.ActorsEntry.FIELD_ACTOR_NAME, "Ocko, Dan");
                actorValues.put(RadioTheaterContract.ActorsEntry.FIELD_ACTOR_URL, "ocko_dan.jpg");
                break;
            case 6:
                actorValues.put(RadioTheaterContract.ActorsEntry.FIELD_ACTOR_ID, actor_id);
                actorValues.put(RadioTheaterContract.ActorsEntry.FIELD_ACTOR_NAME, "Pryor, Nick");
                actorValues.put(RadioTheaterContract.ActorsEntry.FIELD_ACTOR_URL, "pryor_nick.jpg");
                break;
            case 7:
                actorValues.put(RadioTheaterContract.ActorsEntry.FIELD_ACTOR_ID, actor_id);
                actorValues.put(RadioTheaterContract.ActorsEntry.FIELD_ACTOR_NAME, "Seldes, Marian");
                actorValues.put(RadioTheaterContract.ActorsEntry.FIELD_ACTOR_URL, "seldes_marian.jpg");
                break;
            case 8:
                actorValues.put(RadioTheaterContract.ActorsEntry.FIELD_ACTOR_ID, actor_id);
                actorValues.put(RadioTheaterContract.ActorsEntry.FIELD_ACTOR_NAME, "Bell, Ralph");
                actorValues.put(RadioTheaterContract.ActorsEntry.FIELD_ACTOR_URL, "bell_ralph.jpg");
                break;
            case 9:
                actorValues.put(RadioTheaterContract.ActorsEntry.FIELD_ACTOR_ID, actor_id);
                actorValues.put(RadioTheaterContract.ActorsEntry.FIELD_ACTOR_NAME, "Haines, Larry");
                actorValues.put(RadioTheaterContract.ActorsEntry.FIELD_ACTOR_URL, "haines_larry.jpg");
                break;
            case 10:
                actorValues.put(RadioTheaterContract.ActorsEntry.FIELD_ACTOR_ID, actor_id);
                actorValues.put(RadioTheaterContract.ActorsEntry.FIELD_ACTOR_NAME, "Juster, Evie");
                actorValues.put(RadioTheaterContract.ActorsEntry.FIELD_ACTOR_URL, "juster_evie.jpg");
                break;
            case 11:
                actorValues.put(RadioTheaterContract.ActorsEntry.FIELD_ACTOR_ID, actor_id);
                actorValues.put(RadioTheaterContract.ActorsEntry.FIELD_ACTOR_NAME, "Newman, Martin");
                actorValues.put(RadioTheaterContract.ActorsEntry.FIELD_ACTOR_URL, "newman_martin.jpg");
                break;
            case 12:
                actorValues.put(RadioTheaterContract.ActorsEntry.FIELD_ACTOR_ID, actor_id);
                actorValues.put(RadioTheaterContract.ActorsEntry.FIELD_ACTOR_NAME, "Dryden, Robert");
                actorValues.put(RadioTheaterContract.ActorsEntry.FIELD_ACTOR_URL, "dryden_robert.jpg");
                break;
            case 13:
                actorValues.put(RadioTheaterContract.ActorsEntry.FIELD_ACTOR_ID, actor_id);
                actorValues.put(RadioTheaterContract.ActorsEntry.FIELD_ACTOR_NAME, "Hunter, Kim");
                actorValues.put(RadioTheaterContract.ActorsEntry.FIELD_ACTOR_URL, "hunter_kim.jpg");
                break;
            case 14:
                actorValues.put(RadioTheaterContract.ActorsEntry.FIELD_ACTOR_ID, actor_id);
                actorValues.put(RadioTheaterContract.ActorsEntry.FIELD_ACTOR_NAME, "Mack, Gilbert");
                actorValues.put(RadioTheaterContract.ActorsEntry.FIELD_ACTOR_URL, "mack_gilbert.jpg");
                break;
            case 15:
                actorValues.put(RadioTheaterContract.ActorsEntry.FIELD_ACTOR_ID, actor_id);
                actorValues.put(RadioTheaterContract.ActorsEntry.FIELD_ACTOR_NAME, "Matthews, George");
                actorValues.put(RadioTheaterContract.ActorsEntry.FIELD_ACTOR_URL, "matthews_george.jpg");
                break;
            case 16:
                actorValues.put(RadioTheaterContract.ActorsEntry.FIELD_ACTOR_ID, actor_id);
                actorValues.put(RadioTheaterContract.ActorsEntry.FIELD_ACTOR_NAME, "Patinkin, Mandy");
                actorValues.put(RadioTheaterContract.ActorsEntry.FIELD_ACTOR_URL, "patinkin_mandy.jpg");
                break;
            default:
                assertTrue("invalid value for testNumber", false);
        }
        return actorValues;
    }

    private static long insertActorValues(Context context, long episode_id, int testNumber) {
        Log.v(TAG, "insertActorValues");
        // insert test record into the database
        SQLiteDatabase db = helper.getWritableDatabase();
        assertFalse("getWriteableDatabase must be WRITABLE!", db.isReadOnly());
        ContentValues testValues = TestRadioTheaterUtilities.createActorValues(episode_id, testNumber);
        long actorRowId = db.insert(RadioTheaterContract.ActorsEntry.TABLE_NAME, null, testValues);
        Log.v(TAG, "insertActorValues: actorRowId=" + actorRowId);
        // Verify we got a row back.
        assertTrue("Error: Failure to insert Actor Values", actorRowId != -1);
        return actorRowId;
    }

    // setup content for a default writers row for database tests.
    static ContentValues createWritersValues(long writer_id, int testNumber) {
        Log.v(TAG, "createWritersValues");
        ContentValues writerValues = new ContentValues();
        switch (testNumber) {
            case 1:
                writerValues.put(RadioTheaterContract.WritersEntry.FIELD_WRITER_ID, writer_id);
                writerValues.put(RadioTheaterContract.WritersEntry.FIELD_WRITER_NAME, "Slesar, Henry");
                writerValues.put(RadioTheaterContract.WritersEntry.FIELD_WRITER_URL, "slesar_henry.jpg");
                break;
            case 2:
                writerValues.put(RadioTheaterContract.WritersEntry.FIELD_WRITER_ID, writer_id);
                writerValues.put(RadioTheaterContract.WritersEntry.FIELD_WRITER_NAME, "Dann, Sam");
                writerValues.put(RadioTheaterContract.WritersEntry.FIELD_WRITER_URL, "dann_sam.jpg");
                break;
            case 3:
                writerValues.put(RadioTheaterContract.WritersEntry.FIELD_WRITER_ID, writer_id);
                writerValues.put(RadioTheaterContract.WritersEntry.FIELD_WRITER_NAME, "Sloan, Sidney");
                writerValues.put(RadioTheaterContract.WritersEntry.FIELD_WRITER_URL, "sloan_sidney.jpg");
                break;
            case 4:
                writerValues.put(RadioTheaterContract.WritersEntry.FIELD_WRITER_ID, writer_id);
                writerValues.put(RadioTheaterContract.WritersEntry.FIELD_WRITER_NAME, "Lowthar, George");
                writerValues.put(RadioTheaterContract.WritersEntry.FIELD_WRITER_URL, "lowthar_george.jpg");
                break;
            case 5:
                writerValues.put(RadioTheaterContract.WritersEntry.FIELD_WRITER_ID, writer_id);
                writerValues.put(RadioTheaterContract.WritersEntry.FIELD_WRITER_NAME, "Martin, Ian");
                writerValues.put(RadioTheaterContract.WritersEntry.FIELD_WRITER_URL, "martin_ian.jpg");
                break;
            case 6:
                writerValues.put(RadioTheaterContract.WritersEntry.FIELD_WRITER_ID, writer_id);
                writerValues.put(RadioTheaterContract.WritersEntry.FIELD_WRITER_NAME, "Eric, Elspeth");
                writerValues.put(RadioTheaterContract.WritersEntry.FIELD_WRITER_URL, "eric_elspeth.jpg");
                break;
            default:
                assertTrue("invalid value for testNumber", false);
        }
        return writerValues;
    }

    private static long insertWriterValues(Context context, long episode_id, int testNumber) {
        Log.v(TAG, "insertWriterValues");
        // insert test record into the database
        SQLiteDatabase db = helper.getWritableDatabase();
        assertFalse("getWriteableDatabase must be WRITABLE!", db.isReadOnly());
        ContentValues testValues = TestRadioTheaterUtilities.createWritersValues(episode_id, testNumber);
        long writerRowId = db.insert(RadioTheaterContract.WritersEntry.TABLE_NAME, null, testValues);
        // Verify we got a row back.
        Log.v(TAG, "insertWriterValues: writerRowId=" + writerRowId);
        assertTrue("Error: Failure to insert Writers Values", writerRowId != -1);
        return writerRowId;
    }

    @Test
    static TestContentObserver getTestContentObserver() {
        Log.v(TAG, "getTestContentObserver");
        return TestContentObserver.getTestContentObserver();
    }

    @Test
    void testEpisodesDatabase() {
        Log.v(TAG, "testEpisodesDatabase");
        int testNumber = 1;
        long episode_id = insertEpisodeValues(getContext(), testNumber);
        assertTrue("Error: Failure to insert Episode record", episode_id > 0);
        long actor_id = insertActorValues(getContext(), episode_id, testNumber);
        assertTrue("Error: Failure to insert Actor record", actor_id > 0);
        long writer_id = insertWriterValues(getContext(), episode_id, testNumber);
        assertTrue("Error: Failure to insert Writer record", writer_id > 0);
    }

    // Use the TestProvider utility class to test the ContentObserver callbacks using the PollingCheck class.
    // Note that this only tests that the onChange function is called; it does not test that the correct Uri is returned.
    private static class TestContentObserver extends ContentObserver {
        private final static String TAG = "LEE: <" + TestContentObserver.class.getSimpleName() + ">";

        final HandlerThread mHT;
        boolean mContentChanged;

        private TestContentObserver(HandlerThread ht) {
            super(new Handler(ht.getLooper()));
            Log.v(TAG, "TestContentObserver");
            mHT = ht;
        }

        static TestContentObserver getTestContentObserver() {
            Log.v(TAG, "getTestContentObserver");
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        // On earlier versions of Android, this onChange method is called
        @Override
        public void onChange(boolean selfChange) {
            Log.v(TAG, "onChange");
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            Log.v(TAG, "onChange");
            mContentChanged = true;
        }

        public void waitForNotificationOrFail() {
            Log.v(TAG, "waitForNotificationOrFail");
            // Note: The PollingCheck class is taken from the Android CTS (Compatibility Test Suite).
            // It's useful to look at the Android CTS source for ideas on how to test your Android
            // applications.  The reason that PollingCheck works is that, by default, the JUnit
            // testing framework is not running on the main Android application thread.

            new PollingCheck(5000) {
                @Override
                protected boolean check() {
                    return mContentChanged;
                }
            }.run();

            mHT.quit();
        }
    }

}
