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

import android.content.Context;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import com.harlie.radiotheater.radiomysterytheater.data.base.AbstractSelection;

/**
 * Selection for the {@code configuration} table.
 */
public class ConfigurationSelection extends AbstractSelection<ConfigurationSelection> {
    @Override
    protected Uri baseUri() {
        return ConfigurationColumns.CONTENT_URI;
    }

    /**
     * Query the given content resolver using this selection.
     *
     * @param contentResolver The content resolver to query.
     * @param projection A list of which columns to return. Passing null will return all columns, which is inefficient.
     * @return A {@code ConfigurationCursor} object, which is positioned before the first entry, or null.
     */
    public ConfigurationCursor query(ContentResolver contentResolver, String[] projection) {
        Cursor cursor = contentResolver.query(uri(), projection, sel(), args(), order());
        if (cursor == null) return null;
        return new ConfigurationCursor(cursor);
    }

    /**
     * Equivalent of calling {@code query(contentResolver, null)}.
     */
    public ConfigurationCursor query(ContentResolver contentResolver) {
        return query(contentResolver, null);
    }

    /**
     * Query the given content resolver using this selection.
     *
     * @param context The context to use for the query.
     * @param projection A list of which columns to return. Passing null will return all columns, which is inefficient.
     * @return A {@code ConfigurationCursor} object, which is positioned before the first entry, or null.
     */
    public ConfigurationCursor query(Context context, String[] projection) {
        Cursor cursor = context.getContentResolver().query(uri(), projection, sel(), args(), order());
        if (cursor == null) return null;
        return new ConfigurationCursor(cursor);
    }

    /**
     * Equivalent of calling {@code query(context, null)}.
     */
    public ConfigurationCursor query(Context context) {
        return query(context, null);
    }


    public ConfigurationSelection id(long... value) {
        addEquals("configuration." + ConfigurationColumns._ID, toObjectArray(value));
        return this;
    }

    public ConfigurationSelection idNot(long... value) {
        addNotEquals("configuration." + ConfigurationColumns._ID, toObjectArray(value));
        return this;
    }

    public ConfigurationSelection orderById(boolean desc) {
        orderBy("configuration." + ConfigurationColumns._ID, desc);
        return this;
    }

    public ConfigurationSelection orderById() {
        return orderById(false);
    }

    public ConfigurationSelection fieldUserEmail(String... value) {
        addEquals(ConfigurationColumns.FIELD_USER_EMAIL, value);
        return this;
    }

    public ConfigurationSelection fieldUserEmailNot(String... value) {
        addNotEquals(ConfigurationColumns.FIELD_USER_EMAIL, value);
        return this;
    }

    public ConfigurationSelection fieldUserEmailLike(String... value) {
        addLike(ConfigurationColumns.FIELD_USER_EMAIL, value);
        return this;
    }

    public ConfigurationSelection fieldUserEmailContains(String... value) {
        addContains(ConfigurationColumns.FIELD_USER_EMAIL, value);
        return this;
    }

    public ConfigurationSelection fieldUserEmailStartsWith(String... value) {
        addStartsWith(ConfigurationColumns.FIELD_USER_EMAIL, value);
        return this;
    }

    public ConfigurationSelection fieldUserEmailEndsWith(String... value) {
        addEndsWith(ConfigurationColumns.FIELD_USER_EMAIL, value);
        return this;
    }

    public ConfigurationSelection orderByFieldUserEmail(boolean desc) {
        orderBy(ConfigurationColumns.FIELD_USER_EMAIL, desc);
        return this;
    }

    public ConfigurationSelection orderByFieldUserEmail() {
        orderBy(ConfigurationColumns.FIELD_USER_EMAIL, false);
        return this;
    }

    public ConfigurationSelection fieldUserName(String... value) {
        addEquals(ConfigurationColumns.FIELD_USER_NAME, value);
        return this;
    }

    public ConfigurationSelection fieldUserNameNot(String... value) {
        addNotEquals(ConfigurationColumns.FIELD_USER_NAME, value);
        return this;
    }

    public ConfigurationSelection fieldUserNameLike(String... value) {
        addLike(ConfigurationColumns.FIELD_USER_NAME, value);
        return this;
    }

    public ConfigurationSelection fieldUserNameContains(String... value) {
        addContains(ConfigurationColumns.FIELD_USER_NAME, value);
        return this;
    }

    public ConfigurationSelection fieldUserNameStartsWith(String... value) {
        addStartsWith(ConfigurationColumns.FIELD_USER_NAME, value);
        return this;
    }

    public ConfigurationSelection fieldUserNameEndsWith(String... value) {
        addEndsWith(ConfigurationColumns.FIELD_USER_NAME, value);
        return this;
    }

    public ConfigurationSelection orderByFieldUserName(boolean desc) {
        orderBy(ConfigurationColumns.FIELD_USER_NAME, desc);
        return this;
    }

    public ConfigurationSelection orderByFieldUserName() {
        orderBy(ConfigurationColumns.FIELD_USER_NAME, false);
        return this;
    }

    public ConfigurationSelection fieldAuthenticated(Boolean value) {
        addEquals(ConfigurationColumns.FIELD_AUTHENTICATED, toObjectArray(value));
        return this;
    }

    public ConfigurationSelection orderByFieldAuthenticated(boolean desc) {
        orderBy(ConfigurationColumns.FIELD_AUTHENTICATED, desc);
        return this;
    }

    public ConfigurationSelection orderByFieldAuthenticated() {
        orderBy(ConfigurationColumns.FIELD_AUTHENTICATED, false);
        return this;
    }

    public ConfigurationSelection fieldDeviceId(String... value) {
        addEquals(ConfigurationColumns.FIELD_DEVICE_ID, value);
        return this;
    }

    public ConfigurationSelection fieldDeviceIdNot(String... value) {
        addNotEquals(ConfigurationColumns.FIELD_DEVICE_ID, value);
        return this;
    }

    public ConfigurationSelection fieldDeviceIdLike(String... value) {
        addLike(ConfigurationColumns.FIELD_DEVICE_ID, value);
        return this;
    }

    public ConfigurationSelection fieldDeviceIdContains(String... value) {
        addContains(ConfigurationColumns.FIELD_DEVICE_ID, value);
        return this;
    }

    public ConfigurationSelection fieldDeviceIdStartsWith(String... value) {
        addStartsWith(ConfigurationColumns.FIELD_DEVICE_ID, value);
        return this;
    }

    public ConfigurationSelection fieldDeviceIdEndsWith(String... value) {
        addEndsWith(ConfigurationColumns.FIELD_DEVICE_ID, value);
        return this;
    }

    public ConfigurationSelection orderByFieldDeviceId(boolean desc) {
        orderBy(ConfigurationColumns.FIELD_DEVICE_ID, desc);
        return this;
    }

    public ConfigurationSelection orderByFieldDeviceId() {
        orderBy(ConfigurationColumns.FIELD_DEVICE_ID, false);
        return this;
    }

    public ConfigurationSelection fieldPaidVersion(boolean value) {
        addEquals(ConfigurationColumns.FIELD_PAID_VERSION, toObjectArray(value));
        return this;
    }

    public ConfigurationSelection orderByFieldPaidVersion(boolean desc) {
        orderBy(ConfigurationColumns.FIELD_PAID_VERSION, desc);
        return this;
    }

    public ConfigurationSelection orderByFieldPaidVersion() {
        orderBy(ConfigurationColumns.FIELD_PAID_VERSION, false);
        return this;
    }

    public ConfigurationSelection fieldPurchaseAccess(boolean value) {
        addEquals(ConfigurationColumns.FIELD_PURCHASE_ACCESS, toObjectArray(value));
        return this;
    }

    public ConfigurationSelection orderByFieldPurchaseAccess(boolean desc) {
        orderBy(ConfigurationColumns.FIELD_PURCHASE_ACCESS, desc);
        return this;
    }

    public ConfigurationSelection orderByFieldPurchaseAccess() {
        orderBy(ConfigurationColumns.FIELD_PURCHASE_ACCESS, false);
        return this;
    }

    public ConfigurationSelection fieldPurchaseNoads(boolean value) {
        addEquals(ConfigurationColumns.FIELD_PURCHASE_NOADS, toObjectArray(value));
        return this;
    }

    public ConfigurationSelection orderByFieldPurchaseNoads(boolean desc) {
        orderBy(ConfigurationColumns.FIELD_PURCHASE_NOADS, desc);
        return this;
    }

    public ConfigurationSelection orderByFieldPurchaseNoads() {
        orderBy(ConfigurationColumns.FIELD_PURCHASE_NOADS, false);
        return this;
    }

    public ConfigurationSelection fieldTotalListenCount(int... value) {
        addEquals(ConfigurationColumns.FIELD_TOTAL_LISTEN_COUNT, toObjectArray(value));
        return this;
    }

    public ConfigurationSelection fieldTotalListenCountNot(int... value) {
        addNotEquals(ConfigurationColumns.FIELD_TOTAL_LISTEN_COUNT, toObjectArray(value));
        return this;
    }

    public ConfigurationSelection fieldTotalListenCountGt(int value) {
        addGreaterThan(ConfigurationColumns.FIELD_TOTAL_LISTEN_COUNT, value);
        return this;
    }

    public ConfigurationSelection fieldTotalListenCountGtEq(int value) {
        addGreaterThanOrEquals(ConfigurationColumns.FIELD_TOTAL_LISTEN_COUNT, value);
        return this;
    }

    public ConfigurationSelection fieldTotalListenCountLt(int value) {
        addLessThan(ConfigurationColumns.FIELD_TOTAL_LISTEN_COUNT, value);
        return this;
    }

    public ConfigurationSelection fieldTotalListenCountLtEq(int value) {
        addLessThanOrEquals(ConfigurationColumns.FIELD_TOTAL_LISTEN_COUNT, value);
        return this;
    }

    public ConfigurationSelection orderByFieldTotalListenCount(boolean desc) {
        orderBy(ConfigurationColumns.FIELD_TOTAL_LISTEN_COUNT, desc);
        return this;
    }

    public ConfigurationSelection orderByFieldTotalListenCount() {
        orderBy(ConfigurationColumns.FIELD_TOTAL_LISTEN_COUNT, false);
        return this;
    }
}
