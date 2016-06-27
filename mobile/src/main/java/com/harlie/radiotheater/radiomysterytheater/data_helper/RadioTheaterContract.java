/*
 * This is a helper class for the generated Sqlite 'data' package.
 * This class is not generated, and must be manually updated when
 * database tables are added or removed.  See 'generate_data_contentprovider'
 */
package com.harlie.radiotheater.radiomysterytheater.data_helper;

import android.content.ContentUris;
import android.net.Uri;
import android.text.format.Time;

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
import com.harlie.radiotheater.radiomysterytheater.utils.LogHelper;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class RadioTheaterContract {
    private final static String TAG = "LEE: <" + RadioTheaterContract.class.getSimpleName() + ">";

    // Reference the AUTHORITY generated from 'generate-data-provider.sh'
    public static final String CONTENT_AUTHORITY = RadioTheaterProvider.AUTHORITY;

    public static String airDate(String episodeAirDate) {
        DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        DateFormat outputFormat = new SimpleDateFormat("EEEE', 'MMMM d', ' yyyy", Locale.getDefault());
        Date date = null;
        try {
            date = inputFormat.parse(episodeAirDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String outputDateStr = outputFormat.format(date);
        LogHelper.v(TAG, "airdate="+episodeAirDate+", formatted="+outputDateStr);
        return outputDateStr;
    }

    /* Inner class that defines the table contents of the configuration table */
    public static final class ConfigurationEntry extends ConfigurationColumns {

        public static Uri buildConfigurationUri(long id) {
            LogHelper.v(TAG, "buildConfigurationUri");
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildConfigurationUri() {
            LogHelper.v(TAG, "buildConfigurationUri");
            return CONTENT_URI;
        }

    }

    /* Inner class that defines the table contents of the configuration table */
    public static final class ConfigEpisodesEntry extends ConfigEpisodesColumns {

        public static Uri buildConfigEpisodeUri(long id) {
            LogHelper.v(TAG, "buildConfigEpisodeUri");
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildConfigEpisodesUri() {
            LogHelper.v(TAG, "buildConfigEpisodesUri");
            return CONTENT_URI;
        }

    }

    /* Inner class that defines the table contents of the episodes table */
    public static final class EpisodesEntry extends EpisodesColumns {

        public static Uri buildEpisodeUri(long id) {
            LogHelper.v(TAG, "buildEpisodeUri");
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildEpisodesUri() {
            LogHelper.v(TAG, "buildEpisodesUri");
            return CONTENT_URI;
        }

    }

    /* Inner class that defines the table contents of the episodesactors table */
    public static final class EpisodesActorsEntry extends EpisodesActorsColumns {

        public static Uri buildEpisodeActorUri(long id) {
            LogHelper.v(TAG, "buildEpisodeActorUri");
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildEpisodesActorsUri() {
            LogHelper.v(TAG, "buildEpisodesActorsUri");
            return CONTENT_URI;
        }

    }

    /* Inner class that defines the table contents of the episodeswriters table */
    public static final class EpisodesWritersEntry extends EpisodesWritersColumns {

        public static Uri buildEpisodeWriterUri(long id) {
            LogHelper.v(TAG, "buildEpisodeWriterUri");
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildEpisodesWritersUri() {
            LogHelper.v(TAG, "buildEpisodesWritersUri");
            return CONTENT_URI;
        }

    }

    /* Inner class that defines the table contents of the actors table */
    public static final class ActorsEntry extends ActorsColumns {

        public static Uri buildActorUri(long id) {
            LogHelper.v(TAG, "buildActorUri");
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildActorsUri() {
            LogHelper.v(TAG, "buildActorsUri");
            return CONTENT_URI;
        }

    }

    /* Inner class that defines the table contents of the actorsepisodes table */
    public static final class ActorsEpisodesEntry extends ActorsEpisodesColumns {

        public static Uri buildActorEpisodesUri(long id) {
            LogHelper.v(TAG, "buildActorEpisodesUri");
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildActorsEpisodesUri() {
            LogHelper.v(TAG, "buildActorsEpisodesUri");
            return CONTENT_URI;
        }

    }

    /* Inner class that defines the table contents of the writers table */
    public static final class WritersEntry extends WritersColumns {

        public static Uri buildWriterUri(long id) {
            LogHelper.v(TAG, "buildWriterUri");
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildWritersUri() {
            LogHelper.v(TAG, "buildWritersUri");
            return CONTENT_URI;
        }

    }

    /* Inner class that defines the table contents of the writersepisodes table */
    public static final class WritersEpisodesEntry extends WritersEpisodesColumns {

        public static Uri buildWriterEpisodesUri(long id) {
            LogHelper.v(TAG, "buildWriterEpisodesUri");
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildWritersEpisodesUri() {
            LogHelper.v(TAG, "buildWritersEpisodesUri");
            return CONTENT_URI;
        }

    }

}
