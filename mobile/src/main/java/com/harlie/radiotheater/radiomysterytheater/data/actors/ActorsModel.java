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

import com.harlie.radiotheater.radiomysterytheater.data.base.BaseModel;

import java.util.Date;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Radio Mystery Theater actors list.
 */
public interface ActorsModel extends BaseModel {

    /**
     * Get the {@code field_actor_id} value.
     */
    long getFieldActorId();

    /**
     * actor full name
     * Cannot be {@code null}.
     */
    @NonNull
    String getFieldActorName();

    /**
     * actor photo URL
     * Cannot be {@code null}.
     */
    @NonNull
    String getFieldActorUrl();

    /**
     * actor biography
     * Can be {@code null}.
     */
    @Nullable
    String getFieldActorBio();
}
