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
package com.harlie.radiotheater.radiomysterytheater.data.writers;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.harlie.radiotheater.radiomysterytheater.data.base.BaseModel;

/**
 * Radio Mystery Theater writers list.
 */
public interface WritersModel extends BaseModel {

    /**
     * Get the {@code field_writer_id} value.
     */
    long getFieldWriterId();

    /**
     * writer full name
     * Cannot be {@code null}.
     */
    @NonNull
    String getFieldWriterName();

    /**
     * writer photo URL
     * Cannot be {@code null}.
     */
    @NonNull
    String getFieldWriterUrl();

    /**
     * writer biography
     * Can be {@code null}.
     */
    @Nullable
    String getFieldWriterBio();
}
