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
package com.harlie.radiotheater.radiomysterytheater.data.configepisodes;

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
 * Radio Mystery Theater user configuration.
 */
public class ConfigEpisodesColumns implements BaseColumns {
    public static final String TABLE_NAME = "config_episodes";
    public static final Uri CONTENT_URI = Uri.parse(RadioTheaterProvider.CONTENT_URI_BASE + "/" + TABLE_NAME);

    /**
     * Primary key.
     */
    public static final String _ID = BaseColumns._ID;

    public static final String FIELD_EPISODE_NUMBER = "field_episode_number";

    public static final String FIELD_PURCHASED_ACCESS = "field_purchased_access";

    public static final String FIELD_PURCHASED_NOADS = "field_purchased_noads";

    public static final String FIELD_EPISODE_DOWNLOADED = "field_episode_downloaded";

    public static final String FIELD_EPISODE_HEARD = "field_episode_heard";

    public static final String FIELD_LISTEN_COUNT = "field_listen_count";


    public static final String DEFAULT_ORDER = TABLE_NAME + "." +_ID;

    // @formatter:off
    public static final String[] ALL_COLUMNS = new String[] {
            _ID,
            FIELD_EPISODE_NUMBER,
            FIELD_PURCHASED_ACCESS,
            FIELD_PURCHASED_NOADS,
            FIELD_EPISODE_DOWNLOADED,
            FIELD_EPISODE_HEARD,
            FIELD_LISTEN_COUNT
    };
    // @formatter:on

    public static boolean hasColumns(String[] projection) {
        if (projection == null) return true;
        for (String c : projection) {
            if (c.equals(FIELD_EPISODE_NUMBER) || c.contains("." + FIELD_EPISODE_NUMBER)) return true;
            if (c.equals(FIELD_PURCHASED_ACCESS) || c.contains("." + FIELD_PURCHASED_ACCESS)) return true;
            if (c.equals(FIELD_PURCHASED_NOADS) || c.contains("." + FIELD_PURCHASED_NOADS)) return true;
            if (c.equals(FIELD_EPISODE_DOWNLOADED) || c.contains("." + FIELD_EPISODE_DOWNLOADED)) return true;
            if (c.equals(FIELD_EPISODE_HEARD) || c.contains("." + FIELD_EPISODE_HEARD)) return true;
            if (c.equals(FIELD_LISTEN_COUNT) || c.contains("." + FIELD_LISTEN_COUNT)) return true;
        }
        return false;
    }

}
