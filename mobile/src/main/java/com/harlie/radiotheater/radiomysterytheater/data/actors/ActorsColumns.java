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
package com.harlie.radiotheater.radiomysterytheater.data.actors;

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
 * Radio Mystery Theater actors list.
 */
public class ActorsColumns implements BaseColumns {
    public static final String TABLE_NAME = "actors";
    public static final Uri CONTENT_URI = Uri.parse(RadioTheaterProvider.CONTENT_URI_BASE + "/" + TABLE_NAME);

    /**
     * Primary key.
     */
    public static final String _ID = BaseColumns._ID;

    public static final String FIELD_ACTOR_ID = "actors__field_actor_id";

    /**
     * actor full name
     */
    public static final String FIELD_ACTOR_NAME = "field_actor_name";

    /**
     * actor photo URL
     */
    public static final String FIELD_ACTOR_URL = "field_actor_url";

    /**
     * actor biography
     */
    public static final String FIELD_ACTOR_BIO = "field_actor_bio";


    public static final String DEFAULT_ORDER = TABLE_NAME + "." +_ID;

    // @formatter:off
    public static final String[] ALL_COLUMNS = new String[] {
            _ID,
            FIELD_ACTOR_ID,
            FIELD_ACTOR_NAME,
            FIELD_ACTOR_URL,
            FIELD_ACTOR_BIO
    };
    // @formatter:on

    public static boolean hasColumns(String[] projection) {
        if (projection == null) return true;
        for (String c : projection) {
            if (c.equals(FIELD_ACTOR_ID) || c.contains("." + FIELD_ACTOR_ID)) return true;
            if (c.equals(FIELD_ACTOR_NAME) || c.contains("." + FIELD_ACTOR_NAME)) return true;
            if (c.equals(FIELD_ACTOR_URL) || c.contains("." + FIELD_ACTOR_URL)) return true;
            if (c.equals(FIELD_ACTOR_BIO) || c.contains("." + FIELD_ACTOR_BIO)) return true;
        }
        return false;
    }

    public static final String PREFIX_ACTORS_EPISODES = TABLE_NAME + "__" + ActorsEpisodesColumns.TABLE_NAME;
}
