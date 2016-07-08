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

import java.util.Date;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.harlie.radiotheater.radiomysterytheater.data.base.AbstractCursor;

/**
 * Cursor wrapper for the {@code configuration} table.
 */
public class ConfigurationCursor extends AbstractCursor implements ConfigurationModel {
    public ConfigurationCursor(Cursor cursor) {
        super(cursor);
    }

    /**
     * Primary key.
     */
    public long getId() {
        Long res = getLongOrNull(ConfigurationColumns._ID);
        if (res == null)
            throw new NullPointerException("The value of '_id' in the database was null, which is not allowed according to the model definition");
        return res;
    }

    /**
     * Get the {@code field_user_email} value.
     * Cannot be {@code null}.
     */
    @NonNull
    public String getFieldUserEmail() {
        String res = getStringOrNull(ConfigurationColumns.FIELD_USER_EMAIL);
        if (res == null)
            throw new NullPointerException("The value of 'field_user_email' in the database was null, which is not allowed according to the model definition");
        return res;
    }

    /**
     * Get the {@code field_user_name} value.
     * Can be {@code null}.
     */
    @Nullable
    public String getFieldUserName() {
        String res = getStringOrNull(ConfigurationColumns.FIELD_USER_NAME);
        return res;
    }

    /**
     * Get the {@code field_authenticated} value.
     * Can be {@code null}.
     */
    @Nullable
    public Boolean getFieldAuthenticated() {
        Boolean res = getBooleanOrNull(ConfigurationColumns.FIELD_AUTHENTICATED);
        return res;
    }

    /**
     * Get the {@code field_device_id} value.
     * Can be {@code null}.
     */
    @Nullable
    public String getFieldDeviceId() {
        String res = getStringOrNull(ConfigurationColumns.FIELD_DEVICE_ID);
        return res;
    }

    /**
     * Get the {@code field_paid_version} value.
     */
    public boolean getFieldPaidVersion() {
        Boolean res = getBooleanOrNull(ConfigurationColumns.FIELD_PAID_VERSION);
        if (res == null)
            throw new NullPointerException("The value of 'field_paid_version' in the database was null, which is not allowed according to the model definition");
        return res;
    }

    /**
     * Get the {@code field_purchase_access} value.
     */
    public boolean getFieldPurchaseAccess() {
        Boolean res = getBooleanOrNull(ConfigurationColumns.FIELD_PURCHASE_ACCESS);
        if (res == null)
            throw new NullPointerException("The value of 'field_purchase_access' in the database was null, which is not allowed according to the model definition");
        return res;
    }

    /**
     * Get the {@code field_purchase_noads} value.
     */
    public boolean getFieldPurchaseNoads() {
        Boolean res = getBooleanOrNull(ConfigurationColumns.FIELD_PURCHASE_NOADS);
        if (res == null)
            throw new NullPointerException("The value of 'field_purchase_noads' in the database was null, which is not allowed according to the model definition");
        return res;
    }

    /**
     * Get the {@code field_total_listen_count} value.
     */
    public int getFieldTotalListenCount() {
        Integer res = getIntegerOrNull(ConfigurationColumns.FIELD_TOTAL_LISTEN_COUNT);
        if (res == null)
            throw new NullPointerException("The value of 'field_total_listen_count' in the database was null, which is not allowed according to the model definition");
        return res;
    }
}
