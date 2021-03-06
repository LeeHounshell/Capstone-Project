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
package com.harlie.radiotheater.radiomysterytheater.data.writersepisodes;

import android.support.annotation.NonNull;

import com.harlie.radiotheater.radiomysterytheater.data.base.BaseModel;

/**
 * Radio Mystery Theater writers list.
 */
public interface WritersEpisodesModel extends BaseModel {

    /**
     * Get the {@code field_writer_id} value.
     */
    long getFieldWriterId();

    /**
     * Get the {@code field_writer_name} value.
     * Cannot be {@code null}.
     */
    @NonNull
    String getFieldWriterName();

    /**
     * Get the {@code field_episode_number} value.
     */
    long getFieldEpisodeNumber();
}
