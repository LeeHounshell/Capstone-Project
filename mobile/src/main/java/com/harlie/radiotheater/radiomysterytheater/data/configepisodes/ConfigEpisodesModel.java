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

import com.harlie.radiotheater.radiomysterytheater.data.base.BaseModel;

/**
 * Radio Mystery Theater user configuration.
 */
public interface ConfigEpisodesModel extends BaseModel {

    /**
     * Get the {@code field_episode_number} value.
     */
    long getFieldEpisodeNumber();

    /**
     * Get the {@code field_purchased_access} value.
     */
    boolean getFieldPurchasedAccess();

    /**
     * Get the {@code field_purchased_noads} value.
     */
    boolean getFieldPurchasedNoads();

    /**
     * Get the {@code field_episode_downloaded} value.
     */
    boolean getFieldEpisodeDownloaded();

    /**
     * Get the {@code field_episode_heard} value.
     */
    boolean getFieldEpisodeHeard();

    /**
     * Get the {@code field_listen_count} value.
     */
    int getFieldListenCount();
}
