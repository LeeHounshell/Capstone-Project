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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.harlie.radiotheater.radiomysterytheater.data.base.BaseModel;

/**
 * Radio Mystery Theater episodes.
 */
public interface EpisodesModel extends BaseModel {

    /**
     * Get the {@code field_episode_number} value.
     */
    long getFieldEpisodeNumber();

    /**
     * Air Date
     * Cannot be {@code null}.
     */
    @NonNull
    String getFieldAirdate();

    /**
     * the episode title
     * Cannot be {@code null}.
     */
    @NonNull
    String getFieldEpisodeTitle();

    /**
     * episode description
     * Cannot be {@code null}.
     */
    @NonNull
    String getFieldEpisodeDescription();

    /**
     * url weblink path
     * Can be {@code null}.
     */
    @Nullable
    String getFieldWeblinkUrl();

    /**
     * url download path
     * Cannot be {@code null}.
     */
    @NonNull
    String getFieldDownloadUrl();

    /**
     * episode rating
     * Can be {@code null}.
     */
    @Nullable
    Float getFieldRating();

    /**
     * episode vote count
     * Can be {@code null}.
     */
    @Nullable
    Integer getFieldVoteCount();
}
