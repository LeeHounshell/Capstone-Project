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

import java.util.Date;

import android.content.Context;
import android.content.ContentResolver;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.harlie.radiotheater.radiomysterytheater.data.base.AbstractContentValues;

/**
 * Content values wrapper for the {@code writers} table.
 */
public class WritersContentValues extends AbstractContentValues {
    @Override
    public Uri uri() {
        return WritersColumns.CONTENT_URI;
    }

    /**
     * Update row(s) using the values stored by this object and the given selection.
     *
     * @param contentResolver The content resolver to use.
     * @param where The selection to use (can be {@code null}).
     */
    public int update(ContentResolver contentResolver, @Nullable WritersSelection where) {
        return contentResolver.update(uri(), values(), where == null ? null : where.sel(), where == null ? null : where.args());
    }

    /**
     * Update row(s) using the values stored by this object and the given selection.
     *
     * @param contentResolver The content resolver to use.
     * @param where The selection to use (can be {@code null}).
     */
    public int update(Context context, @Nullable WritersSelection where) {
        return context.getContentResolver().update(uri(), values(), where == null ? null : where.sel(), where == null ? null : where.args());
    }

    public WritersContentValues putFieldWriterId(long value) {
        mContentValues.put(WritersColumns.FIELD_WRITER_ID, value);
        return this;
    }


    /**
     * writer full name
     */
    public WritersContentValues putFieldWriterName(@NonNull String value) {
        if (value == null) throw new IllegalArgumentException("fieldWriterName must not be null");
        mContentValues.put(WritersColumns.FIELD_WRITER_NAME, value);
        return this;
    }


    /**
     * writer photo URL
     */
    public WritersContentValues putFieldWriterUrl(@NonNull String value) {
        if (value == null) throw new IllegalArgumentException("fieldWriterUrl must not be null");
        mContentValues.put(WritersColumns.FIELD_WRITER_URL, value);
        return this;
    }


    /**
     * writer biography
     */
    public WritersContentValues putFieldWriterBio(@Nullable String value) {
        mContentValues.put(WritersColumns.FIELD_WRITER_BIO, value);
        return this;
    }

    public WritersContentValues putFieldWriterBioNull() {
        mContentValues.putNull(WritersColumns.FIELD_WRITER_BIO);
        return this;
    }
}
