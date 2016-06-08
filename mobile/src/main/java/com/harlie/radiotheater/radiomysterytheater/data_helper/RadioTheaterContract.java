/*
 * This is a helper class for the generated Sqlite 'data' package.
 * This class is not generated, and must be manually updated when
 * database tables are added or removed.  See 'generate_data_contentprovider'
 */
package com.harlie.radiotheater.radiomysterytheater.data_helper;

import android.content.ContentUris;
import android.net.Uri;
import android.text.format.Time;
import android.util.Log;

import com.harlie.radiotheater.radiomysterytheater.data.RadioTheaterProvider;
import com.harlie.radiotheater.radiomysterytheater.data.actors.ActorsColumns;
import com.harlie.radiotheater.radiomysterytheater.data.actorsepisodes.ActorsEpisodesColumns;
import com.harlie.radiotheater.radiomysterytheater.data.configepisodes.ConfigEpisodesColumns;
import com.harlie.radiotheater.radiomysterytheater.data.configuration.ConfigurationColumns;
import com.harlie.radiotheater.radiomysterytheater.data.episodes.EpisodesColumns;
import com.harlie.radiotheater.radiomysterytheater.data.episodesactors.EpisodesActorsColumns;
import com.harlie.radiotheater.radiomysterytheater.data.episodeswriters.EpisodesWritersColumns;
import com.harlie.radiotheater.radiomysterytheater.data.writers.WritersColumns;
import com.harlie.radiotheater.radiomysterytheater.data.writersepisodes.WritersEpisodesColumns;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class RadioTheaterContract {
    private final static String TAG = "LEE: <" + RadioTheaterContract.class.getSimpleName() + ">";

    // Reference the AUTHORITY generated from 'generate-data-provider.sh'
    public static final String CONTENT_AUTHORITY = RadioTheaterProvider.AUTHORITY;

    // To make it easy to query for the exact date, we normalize all dates that go into
    // the database to the start of the the Julian day at UTC.
    public static long normalizeDate(long episodeAirDate) {
        // normalize the start date to the beginning of the (UTC) day
        Time time = new Time();
        time.set(episodeAirDate);
        int julianDay = Time.getJulianDay(episodeAirDate, time.gmtoff);
        return time.setJulianDay(julianDay);
    }

    public static String airDate(long episodeAirDate) {
        if (episodeAirDate != 0) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(episodeAirDate);
            if (calendar.get(Calendar.YEAR) > 1900 && calendar.get(Calendar.YEAR) < 2100) {
                SimpleDateFormat dt1 = new SimpleDateFormat("yyyy-MM-dd");
                String airDate = dt1.format(calendar.getTime());
                return airDate;
            } else {
                Log.w(TAG, "invalid air date! - calendar=" + calendar);
            }
        }
        return "";
    }

    /* Inner class that defines the table contents of the configuration table */
    public static final class ConfigurationEntry extends ConfigurationColumns {

        public static Uri buildConfigurationUri(long id) {
            Log.v(TAG, "buildConfigurationUri");
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildConfigurationUri() {
            Log.v(TAG, "buildConfigurationUri");
            return CONTENT_URI;
        }

    }

    /* Inner class that defines the table contents of the configuration table */
    public static final class ConfigEpisodesEntry extends ConfigEpisodesColumns {

        public static Uri buildConfigEpisodeUri(long id) {
            Log.v(TAG, "buildConfigEpisodeUri");
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildConfigEpisodesUri() {
            Log.v(TAG, "buildConfigEpisodesUri");
            return CONTENT_URI;
        }

    }

    /* Inner class that defines the table contents of the episodes table */
    public static final class EpisodesEntry extends EpisodesColumns {

        public static Uri buildEpisodeUri(long id) {
            Log.v(TAG, "buildEpisodeUri");
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildEpisodesUri() {
            Log.v(TAG, "buildEpisodesUri");
            return CONTENT_URI;
        }

    }

    /* Inner class that defines the table contents of the episodesactors table */
    public static final class EpisodesActorsEntry extends EpisodesActorsColumns {

        public static Uri buildEpisodeActorUri(long id) {
            Log.v(TAG, "buildEpisodeActorUri");
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildEpisodesActorsUri() {
            Log.v(TAG, "buildEpisodesActorsUri");
            return CONTENT_URI;
        }

    }

    /* Inner class that defines the table contents of the episodeswriters table */
    public static final class EpisodesWritersEntry extends EpisodesWritersColumns {

        public static Uri buildEpisodeWriterUri(long id) {
            Log.v(TAG, "buildEpisodeWriterUri");
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildEpisodesWritersUri() {
            Log.v(TAG, "buildEpisodesWritersUri");
            return CONTENT_URI;
        }

    }

    /* Inner class that defines the table contents of the actors table */
    public static final class ActorsEntry extends ActorsColumns {

        public static Uri buildActorUri(long id) {
            Log.v(TAG, "buildActorUri");
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildActorsUri() {
            Log.v(TAG, "buildActorsUri");
            return CONTENT_URI;
        }

    }

    /* Inner class that defines the table contents of the actorsepisodes table */
    public static final class ActorsEpisodesEntry extends ActorsEpisodesColumns {

        public static Uri buildActorEpisodesUri(long id) {
            Log.v(TAG, "buildActorEpisodesUri");
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildActorsEpisodesUri() {
            Log.v(TAG, "buildActorsEpisodesUri");
            return CONTENT_URI;
        }

    }

    /* Inner class that defines the table contents of the writers table */
    public static final class WritersEntry extends WritersColumns {

        public static Uri buildWriterUri(long id) {
            Log.v(TAG, "buildWriterUri");
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildWritersUri() {
            Log.v(TAG, "buildWritersUri");
            return CONTENT_URI;
        }

    }

    /* Inner class that defines the table contents of the writersepisodes table */
    public static final class WritersEpisodesEntry extends WritersEpisodesColumns {

        public static Uri buildWriterEpisodesUri(long id) {
            Log.v(TAG, "buildWriterEpisodesUri");
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildWritersEpisodesUri() {
            Log.v(TAG, "buildWritersEpisodesUri");
            return CONTENT_URI;
        }

    }

}
