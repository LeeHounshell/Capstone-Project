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
package com.harlie.radiotheater.radiomysterytheater.data.episodesactors;

import android.net.Uri;
import android.provider.BaseColumns;

import com.harlie.radiotheater.radiomysterytheater.data.RadioTheaterProvider;

/**
 * Radio Mystery Theater actors for episode list.
 */
public class EpisodesActorsColumns implements BaseColumns {
    public static final String TABLE_NAME = "episodes_actors";
    public static final Uri CONTENT_URI = Uri.parse(RadioTheaterProvider.CONTENT_URI_BASE + "/" + TABLE_NAME);

    /**
     * Primary key.
     */
    public static final String _ID = BaseColumns._ID;

    public static final String FIELD_EPISODE_NUMBER = "field_episode_number";

    public static final String FIELD_ACTOR_ID = "field_actor_id";


    public static final String DEFAULT_ORDER = TABLE_NAME + "." +_ID;

    // @formatter:off
    public static final String[] ALL_COLUMNS = new String[] {
            _ID,
            FIELD_EPISODE_NUMBER,
            FIELD_ACTOR_ID
    };
    // @formatter:on

    public static boolean hasColumns(String[] projection) {
        if (projection == null) return true;
        for (String c : projection) {
            if (c.equals(FIELD_EPISODE_NUMBER) || c.contains("." + FIELD_EPISODE_NUMBER)) return true;
            if (c.equals(FIELD_ACTOR_ID) || c.contains("." + FIELD_ACTOR_ID)) return true;
        }
        return false;
    }

}
