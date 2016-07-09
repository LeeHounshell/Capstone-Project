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

import android.support.annotation.NonNull;

import com.harlie.radiotheater.radiomysterytheater.data.base.BaseModel;

/**
 * Radio Mystery Theater actors for episode list.
 */
public interface EpisodesActorsModel extends BaseModel {

    /**
     * Get the {@code field_episode_number} value.
     */
    long getFieldEpisodeNumber();

    /**
     * Get the {@code field_actor_id} value.
     */
    long getFieldActorId();

    /**
     * Get the {@code field_actor_name} value.
     * Cannot be {@code null}.
     */
    @NonNull
    String getFieldActorName();
}
