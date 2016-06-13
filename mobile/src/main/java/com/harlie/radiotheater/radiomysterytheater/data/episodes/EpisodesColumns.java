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

import android.net.Uri;
import android.provider.BaseColumns;

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

/**
 * Radio Mystery Theater episodes.
 */
public class EpisodesColumns implements BaseColumns {
    public static final String TABLE_NAME = "episodes";
    public static final Uri CONTENT_URI = Uri.parse(RadioTheaterProvider.CONTENT_URI_BASE + "/" + TABLE_NAME);

    /**
     * Primary key.
     */
    public static final String _ID = BaseColumns._ID;

    public static final String FIELD_EPISODE_NUMBER = "field_episode_number";

    /**
     * Air Date
     */
    public static final String FIELD_AIRDATE = "field_airdate";

    /**
     * the episode title
     */
    public static final String FIELD_EPISODE_TITLE = "field_episode_title";

    /**
     * episode description
     */
    public static final String FIELD_EPISODE_DESCRIPTION = "field_episode_description";

    /**
     * url weblink path
     */
    public static final String FIELD_WEBLINK_URL = "field_weblink_url";

    /**
     * url download path
     */
    public static final String FIELD_DOWNLOAD_URL = "field_download_url";

    /**
     * episode rating
     */
    public static final String FIELD_RATING = "field_rating";

    /**
     * episode vote count
     */
    public static final String FIELD_VOTE_COUNT = "field_vote_count";


    public static final String DEFAULT_ORDER = TABLE_NAME + "." +_ID;

    // @formatter:off
    public static final String[] ALL_COLUMNS = new String[] {
            _ID,
            FIELD_EPISODE_NUMBER,
            FIELD_AIRDATE,
            FIELD_EPISODE_TITLE,
            FIELD_EPISODE_DESCRIPTION,
            FIELD_WEBLINK_URL,
            FIELD_DOWNLOAD_URL,
            FIELD_RATING,
            FIELD_VOTE_COUNT
    };
    // @formatter:on

    public static boolean hasColumns(String[] projection) {
        if (projection == null) return true;
        for (String c : projection) {
            if (c.equals(FIELD_EPISODE_NUMBER) || c.contains("." + FIELD_EPISODE_NUMBER)) return true;
            if (c.equals(FIELD_AIRDATE) || c.contains("." + FIELD_AIRDATE)) return true;
            if (c.equals(FIELD_EPISODE_TITLE) || c.contains("." + FIELD_EPISODE_TITLE)) return true;
            if (c.equals(FIELD_EPISODE_DESCRIPTION) || c.contains("." + FIELD_EPISODE_DESCRIPTION)) return true;
            if (c.equals(FIELD_WEBLINK_URL) || c.contains("." + FIELD_WEBLINK_URL)) return true;
            if (c.equals(FIELD_DOWNLOAD_URL) || c.contains("." + FIELD_DOWNLOAD_URL)) return true;
            if (c.equals(FIELD_RATING) || c.contains("." + FIELD_RATING)) return true;
            if (c.equals(FIELD_VOTE_COUNT) || c.contains("." + FIELD_VOTE_COUNT)) return true;
        }
        return false;
    }

}
