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

import android.content.UriMatcher;
import android.net.Uri;
import android.util.Log;

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

import static junit.framework.Assert.assertEquals;

/*
    this class utilizes constants declared with package protection inside of the UriMatcher.
    This is why the test must be in the same data package as the Android app code.
    It is a compromise between data hiding and testability.
 */
public class TestRadioTheaterUriMatcher {
    private final static String TAG = "LEE: <" + TestRadioTheaterDb.class.getSimpleName() + ">";

    // content://com.harlie.radiotheater.radiomysterytheater.data.radiotheaterprovider/episodes
    private final Uri TEST_CONFIGURATION_DIR = ConfigurationColumns.CONTENT_URI;
    private final Uri TEST_CONFIGEPISODES_DIR = ConfigEpisodesColumns.CONTENT_URI;
    private final Uri TEST_EPISODES_DIR = EpisodesColumns.CONTENT_URI;
    private final Uri TEST_EPISODESACTORS_DIR = EpisodesActorsColumns.CONTENT_URI;
    private final Uri TEST_EPISODESWRITERS_DIR = EpisodesWritersColumns.CONTENT_URI;
    private final Uri TEST_ACTORS_DIR = ActorsColumns.CONTENT_URI;
    private final Uri TEST_ACTORSEPISODES_DIR = ActorsEpisodesColumns.CONTENT_URI;
    private final Uri TEST_WRITERS_DIR = WritersColumns.CONTENT_URI;
    private final Uri TEST_WRITERSEPISODES_DIR = WritersEpisodesColumns.CONTENT_URI;

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

    // dummy object to fool dex
    ActorsColumns dummyActorsColumns;
    ActorsEpisodesColumns dummyActorsEpisodesColumns;
    ConfigurationColumns dummyConfigurationColumns;
    ConfigEpisodesColumns dummyConfigEpisodesColumns;
    EpisodesColumns dummyEpisodesColumns;
    EpisodesActorsColumns dummyEpisodesActorsColumns;
    EpisodesWritersColumns dummyEpisodesWritersColumns;
    WritersColumns dummyWritersColumns;
    WritersEpisodesColumns dummyWritersEpisodesColumns;

    @Before
    public void setUp() throws Exception {
        Log.v(TAG, "setUp");
        dummyActorsColumns = new ActorsColumns();
        dummyActorsEpisodesColumns = new ActorsEpisodesColumns();
        dummyConfigEpisodesColumns = new ConfigEpisodesColumns();
        dummyConfigurationColumns = new ConfigurationColumns();
        dummyEpisodesColumns = new EpisodesColumns();
        dummyEpisodesActorsColumns = new EpisodesActorsColumns();
        dummyEpisodesWritersColumns = new EpisodesWritersColumns();
        dummyWritersColumns = new WritersColumns();
        dummyWritersEpisodesColumns = new WritersEpisodesColumns();
    }

    @After
    public void tearDown() throws Exception {
        Log.v(TAG, "tearDown");
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
                testMatcher.match(TEST_CONFIGURATION_DIR), testMatcher.match(RadioTheaterContract.ConfigurationEntry.buildConfigurationUri()));
        assertEquals("Error: The CONFIGEPISODES URI was matched incorrectly.",
                testMatcher.match(TEST_CONFIGEPISODES_DIR), testMatcher.match(RadioTheaterContract.ConfigEpisodesEntry.buildConfigEpisodesUri()));
        assertEquals("Error: The EPISODES URI was matched incorrectly.",
                testMatcher.match(TEST_EPISODES_DIR), testMatcher.match(RadioTheaterContract.EpisodesEntry.buildEpisodesUri()));
        assertEquals("Error: The EPISODESACTORS URI was matched incorrectly.",
                testMatcher.match(TEST_EPISODESACTORS_DIR), testMatcher.match(RadioTheaterContract.EpisodesActorsEntry.buildEpisodesActorsUri()));
        assertEquals("Error: The EPISODESWRITERS URI was matched incorrectly.",
                testMatcher.match(TEST_EPISODESWRITERS_DIR), testMatcher.match(RadioTheaterContract.EpisodesWritersEntry.buildEpisodesWritersUri()));
        assertEquals("Error: The ACTORS URI was matched incorrectly.",
                testMatcher.match(TEST_ACTORS_DIR), testMatcher.match(RadioTheaterContract.ActorsEntry.buildActorsUri()));
        assertEquals("Error: The ACTORSEPISODES URI was matched incorrectly.",
                testMatcher.match(TEST_ACTORSEPISODES_DIR), testMatcher.match(RadioTheaterContract.ActorsEpisodesEntry.buildActorsEpisodesUri()));
        assertEquals("Error: The WRITERS URI was matched incorrectly.",
                testMatcher.match(TEST_WRITERS_DIR), testMatcher.match(RadioTheaterContract.WritersEntry.buildWritersUri()));
        assertEquals("Error: The WRITERSEPISODES URI was matched incorrectly.",
                testMatcher.match(TEST_WRITERSEPISODES_DIR), testMatcher.match(RadioTheaterContract.WritersEpisodesEntry.buildWritersEpisodesUri()));
    }

}
