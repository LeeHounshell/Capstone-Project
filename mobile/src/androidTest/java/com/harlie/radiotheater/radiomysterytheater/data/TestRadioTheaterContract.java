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

import android.net.Uri;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.harlie.radiotheater.radiomysterytheater.data_helper.RadioTheaterContract;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

// Test the RadioTheaterContract
@RunWith(AndroidJUnit4.class)
@Suite.SuiteClasses({ TestRadioTheaterContract.class })
public class TestRadioTheaterContract {
    private final static String TAG = "LEE: <" + TestRadioTheaterContract.class.getSimpleName() + ">";

    private static final long TEST_EPISODE = 1;
    private static final long TEST_ACTOR = 1;
    private static final long TEST_WRITER = 1;

    @Before
    public void setUp() throws Exception {
        Log.v(TAG, "setUp");
    }

    @After
    public void tearDown() throws Exception {
        Log.v(TAG, "tearDown");
    }

    @Test
    public void testBuildEpisodeUri() {
        Log.v(TAG, "testBuildEpisodeUri");
        Uri episodeUri = RadioTheaterContract.EpisodesEntry.buildEpisodeUri(TEST_EPISODE);
        assertNotNull("Error: Null Episode Uri returned.", episodeUri);
        assertEquals("Error: Episode _ID not properly appended to the end of the Uri",
                Long.toString(TEST_EPISODE), episodeUri.getLastPathSegment());
        assertEquals("Error: Episode Uri doesn't match our expected result", episodeUri.toString(),
                "content://" + RadioTheaterProvider.AUTHORITY + "/episodes/" + TEST_EPISODE);
    }

    @Test
    public void testBuildActorUri() {
        Log.v(TAG, "testBuildActorUri");
        Uri actorUri = RadioTheaterContract.ActorsEntry.buildActorUri(TEST_ACTOR);
        assertNotNull("Error: Null Actor Uri returned.", actorUri);
        assertEquals("Error: Actor _ID not properly appended to the end of the Uri",
                Long.toString(TEST_ACTOR), actorUri.getLastPathSegment());
        assertEquals("Error: Actor Uri doesn't match our expected result", actorUri.toString(),
                "content://" + RadioTheaterProvider.AUTHORITY + "/actors/" + TEST_ACTOR);
    }

    @Test
    public void testBuildWriterUri() {
        Log.v(TAG, "testBuildWriterUri");
        Uri writerUri = RadioTheaterContract.WritersEntry.buildWriterUri(TEST_WRITER);
        assertNotNull("Error: Null Writer Uri returned.", writerUri);
        assertEquals("Error: Writer _ID not properly appended to the end of the Uri",
                Long.toString(TEST_WRITER), writerUri.getLastPathSegment());
        assertEquals("Error: Actor Uri doesn't match our expected result", writerUri.toString(),
                "content://" + RadioTheaterProvider.AUTHORITY + "/writers/" + TEST_WRITER);
    }

}
