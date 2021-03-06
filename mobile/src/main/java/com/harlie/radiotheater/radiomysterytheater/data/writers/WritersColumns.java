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

import android.net.Uri;
import android.provider.BaseColumns;

import com.harlie.radiotheater.radiomysterytheater.data.RadioTheaterProvider;

/**
 * Radio Mystery Theater writers list.
 */
public class WritersColumns implements BaseColumns {
    public static final String TABLE_NAME = "writers";
    public static final Uri CONTENT_URI = Uri.parse(RadioTheaterProvider.CONTENT_URI_BASE + "/" + TABLE_NAME);

    /**
     * Primary key.
     */
    public static final String _ID = BaseColumns._ID;

    public static final String FIELD_WRITER_ID = "field_writer_id";

    /**
     * writer full name
     */
    public static final String FIELD_WRITER_NAME = "field_writer_name";

    /**
     * writer photo URL
     */
    public static final String FIELD_WRITER_URL = "field_writer_url";

    /**
     * writer biography
     */
    public static final String FIELD_WRITER_BIO = "field_writer_bio";


    public static final String DEFAULT_ORDER = TABLE_NAME + "." +_ID;

    // @formatter:off
    public static final String[] ALL_COLUMNS = new String[] {
            _ID,
            FIELD_WRITER_ID,
            FIELD_WRITER_NAME,
            FIELD_WRITER_URL,
            FIELD_WRITER_BIO
    };
    // @formatter:on

    public static boolean hasColumns(String[] projection) {
        if (projection == null) return true;
        for (String c : projection) {
            if (c.equals(FIELD_WRITER_ID) || c.contains("." + FIELD_WRITER_ID)) return true;
            if (c.equals(FIELD_WRITER_NAME) || c.contains("." + FIELD_WRITER_NAME)) return true;
            if (c.equals(FIELD_WRITER_URL) || c.contains("." + FIELD_WRITER_URL)) return true;
            if (c.equals(FIELD_WRITER_BIO) || c.contains("." + FIELD_WRITER_BIO)) return true;
        }
        return false;
    }

}
