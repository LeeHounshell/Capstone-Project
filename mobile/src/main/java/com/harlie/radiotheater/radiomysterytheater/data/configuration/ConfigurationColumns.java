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

import android.net.Uri;
import android.provider.BaseColumns;

import com.harlie.radiotheater.radiomysterytheater.data.RadioTheaterProvider;
import com.harlie.radiotheater.radiomysterytheater.data.configepisodes.ConfigEpisodesColumns;

/**
 * Radio Mystery Theater user configuration.
 */
public class ConfigurationColumns implements BaseColumns {
    public static final String TABLE_NAME = "configuration";
    public static final Uri CONTENT_URI = Uri.parse(RadioTheaterProvider.CONTENT_URI_BASE + "/" + TABLE_NAME);

    /**
     * Primary key.
     */
    public static final String _ID = BaseColumns._ID;

    public static final String FIELD_USER_EMAIL = "field_user_email";

    public static final String FIELD_USER_NAME = "field_user_name";

    public static final String FIELD_AUTHENTICATED = "field_authenticated";

    public static final String FIELD_DEVICE_ID = "field_device_id";

    public static final String FIELD_PAID_VERSION = "field_paid_version";

    public static final String FIELD_PURCHASE_ACCESS = "configuration__field_purchase_access";

    public static final String FIELD_PURCHASE_NOADS = "configuration__field_purchase_noads";

    public static final String FIELD_TOTAL_LISTEN_COUNT = "field_total_listen_count";


    public static final String DEFAULT_ORDER = TABLE_NAME + "." +_ID;

    // @formatter:off
    public static final String[] ALL_COLUMNS = new String[] {
            _ID,
            FIELD_USER_EMAIL,
            FIELD_USER_NAME,
            FIELD_AUTHENTICATED,
            FIELD_DEVICE_ID,
            FIELD_PAID_VERSION,
            FIELD_PURCHASE_ACCESS,
            FIELD_PURCHASE_NOADS,
            FIELD_TOTAL_LISTEN_COUNT
    };
    // @formatter:on

    public static boolean hasColumns(String[] projection) {
        if (projection == null) return true;
        for (String c : projection) {
            if (c.equals(FIELD_USER_EMAIL) || c.contains("." + FIELD_USER_EMAIL)) return true;
            if (c.equals(FIELD_USER_NAME) || c.contains("." + FIELD_USER_NAME)) return true;
            if (c.equals(FIELD_AUTHENTICATED) || c.contains("." + FIELD_AUTHENTICATED)) return true;
            if (c.equals(FIELD_DEVICE_ID) || c.contains("." + FIELD_DEVICE_ID)) return true;
            if (c.equals(FIELD_PAID_VERSION) || c.contains("." + FIELD_PAID_VERSION)) return true;
            if (c.equals(FIELD_PURCHASE_ACCESS) || c.contains("." + FIELD_PURCHASE_ACCESS)) return true;
            if (c.equals(FIELD_PURCHASE_NOADS) || c.contains("." + FIELD_PURCHASE_NOADS)) return true;
            if (c.equals(FIELD_TOTAL_LISTEN_COUNT) || c.contains("." + FIELD_TOTAL_LISTEN_COUNT)) return true;
        }
        return false;
    }

    public static final String PREFIX_CONFIG_EPISODES = TABLE_NAME + "__" + ConfigEpisodesColumns.TABLE_NAME;
}
