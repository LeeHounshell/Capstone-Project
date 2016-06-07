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
package com.harlie.radiotheater.radiomysterytheater.data.configuration;

import com.harlie.radiotheater.radiomysterytheater.data.base.BaseModel;

import java.util.Date;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Radio Mystery Theater user configuration.
 */
public interface ConfigurationModel extends BaseModel {

    /**
     * Get the {@code field_user_email} value.
     * Cannot be {@code null}.
     */
    @NonNull
    String getFieldUserEmail();

    /**
     * Get the {@code field_user_name} value.
     * Can be {@code null}.
     */
    @Nullable
    String getFieldUserName();

    /**
     * Get the {@code field_authenticated} value.
     * Can be {@code null}.
     */
    @Nullable
    Boolean getFieldAuthenticated();

    /**
     * Get the {@code field_device_id} value.
     * Can be {@code null}.
     */
    @Nullable
    String getFieldDeviceId();

    /**
     * Get the {@code field_paid_version} value.
     */
    boolean getFieldPaidVersion();

    /**
     * Get the {@code field_purchase_access} value.
     */
    boolean getFieldPurchaseAccess();

    /**
     * Get the {@code field_purchase_noads} value.
     */
    boolean getFieldPurchaseNoads();

    /**
     * Get the {@code field_total_listen_count} value.
     */
    int getFieldTotalListenCount();
}
