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

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.harlie.radiotheater.radiomysterytheater.data.base.AbstractContentValues;

/**
 * Content values wrapper for the {@code configuration} table.
 */
public class ConfigurationContentValues extends AbstractContentValues {
    @Override
    public Uri uri() {
        return ConfigurationColumns.CONTENT_URI;
    }

    /**
     * Update row(s) using the values stored by this object and the given selection.
     *
     * @param contentResolver The content resolver to use.
     * @param where The selection to use (can be {@code null}).
     */
    public int update(ContentResolver contentResolver, @Nullable ConfigurationSelection where) {
        return contentResolver.update(uri(), values(), where == null ? null : where.sel(), where == null ? null : where.args());
    }

    /**
     * Update row(s) using the values stored by this object and the given selection.
     *
     * @param contentResolver The content resolver to use.
     * @param where The selection to use (can be {@code null}).
     */
    public int update(Context context, @Nullable ConfigurationSelection where) {
        return context.getContentResolver().update(uri(), values(), where == null ? null : where.sel(), where == null ? null : where.args());
    }

    public ConfigurationContentValues putFieldUserEmail(@NonNull String value) {
        if (value == null) throw new IllegalArgumentException("fieldUserEmail must not be null");
        mContentValues.put(ConfigurationColumns.FIELD_USER_EMAIL, value);
        return this;
    }


    public ConfigurationContentValues putFieldUserName(@Nullable String value) {
        mContentValues.put(ConfigurationColumns.FIELD_USER_NAME, value);
        return this;
    }

    public ConfigurationContentValues putFieldUserNameNull() {
        mContentValues.putNull(ConfigurationColumns.FIELD_USER_NAME);
        return this;
    }

    public ConfigurationContentValues putFieldAuthenticated(@Nullable Boolean value) {
        mContentValues.put(ConfigurationColumns.FIELD_AUTHENTICATED, value);
        return this;
    }

    public ConfigurationContentValues putFieldAuthenticatedNull() {
        mContentValues.putNull(ConfigurationColumns.FIELD_AUTHENTICATED);
        return this;
    }

    public ConfigurationContentValues putFieldDeviceId(@Nullable String value) {
        mContentValues.put(ConfigurationColumns.FIELD_DEVICE_ID, value);
        return this;
    }

    public ConfigurationContentValues putFieldDeviceIdNull() {
        mContentValues.putNull(ConfigurationColumns.FIELD_DEVICE_ID);
        return this;
    }

    public ConfigurationContentValues putFieldPaidVersion(boolean value) {
        mContentValues.put(ConfigurationColumns.FIELD_PAID_VERSION, value);
        return this;
    }


    public ConfigurationContentValues putFieldPurchaseAccess(boolean value) {
        mContentValues.put(ConfigurationColumns.FIELD_PURCHASE_ACCESS, value);
        return this;
    }


    public ConfigurationContentValues putFieldPurchaseNoads(boolean value) {
        mContentValues.put(ConfigurationColumns.FIELD_PURCHASE_NOADS, value);
        return this;
    }


    public ConfigurationContentValues putFieldTotalListenCount(int value) {
        mContentValues.put(ConfigurationColumns.FIELD_TOTAL_LISTEN_COUNT, value);
        return this;
    }

}
