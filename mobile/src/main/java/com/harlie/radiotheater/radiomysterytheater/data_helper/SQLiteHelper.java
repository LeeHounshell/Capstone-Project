package com.harlie.radiotheater.radiomysterytheater.data_helper;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.harlie.radiotheater.radiomysterytheater.BaseActivity;
import com.harlie.radiotheater.radiomysterytheater.RadioTheaterApplication;
import com.harlie.radiotheater.radiomysterytheater.data.actors.ActorsColumns;
import com.harlie.radiotheater.radiomysterytheater.data.actors.ActorsCursor;
import com.harlie.radiotheater.radiomysterytheater.data.actors.ActorsSelection;
import com.harlie.radiotheater.radiomysterytheater.data.actorsepisodes.ActorsEpisodesColumns;
import com.harlie.radiotheater.radiomysterytheater.data.actorsepisodes.ActorsEpisodesCursor;
import com.harlie.radiotheater.radiomysterytheater.data.actorsepisodes.ActorsEpisodesSelection;
import com.harlie.radiotheater.radiomysterytheater.data.configepisodes.ConfigEpisodesColumns;
import com.harlie.radiotheater.radiomysterytheater.data.configepisodes.ConfigEpisodesContentValues;
import com.harlie.radiotheater.radiomysterytheater.data.configepisodes.ConfigEpisodesCursor;
import com.harlie.radiotheater.radiomysterytheater.data.configepisodes.ConfigEpisodesSelection;
import com.harlie.radiotheater.radiomysterytheater.data.configuration.ConfigurationColumns;
import com.harlie.radiotheater.radiomysterytheater.data.configuration.ConfigurationContentValues;
import com.harlie.radiotheater.radiomysterytheater.data.configuration.ConfigurationCursor;
import com.harlie.radiotheater.radiomysterytheater.data.configuration.ConfigurationSelection;
import com.harlie.radiotheater.radiomysterytheater.data.episodes.EpisodesColumns;
import com.harlie.radiotheater.radiomysterytheater.data.episodes.EpisodesCursor;
import com.harlie.radiotheater.radiomysterytheater.data.episodes.EpisodesSelection;
import com.harlie.radiotheater.radiomysterytheater.data.writers.WritersColumns;
import com.harlie.radiotheater.radiomysterytheater.data.writers.WritersCursor;
import com.harlie.radiotheater.radiomysterytheater.data.writers.WritersSelection;
import com.harlie.radiotheater.radiomysterytheater.data.writersepisodes.WritersEpisodesColumns;
import com.harlie.radiotheater.radiomysterytheater.data.writersepisodes.WritersEpisodesCursor;
import com.harlie.radiotheater.radiomysterytheater.data.writersepisodes.WritersEpisodesSelection;
import com.harlie.radiotheater.radiomysterytheater.data_helper.RadioTheaterContract;
import com.harlie.radiotheater.radiomysterytheater.utils.LogHelper;
import com.harlie.radiotheater.radiomysterytheater.utils.NetworkHelper;

public class SQLiteHelper {
    private final static String TAG = "LEE: <" + SQLiteHelper.class.getSimpleName() + ">";

    public static ConfigEpisodesContentValues getConfigForEpisode(String episode) {
        LogHelper.v(TAG, "getConfigForEpisode: episode="+episode);
        ConfigEpisodesContentValues record = null;
        ConfigEpisodesSelection where = new ConfigEpisodesSelection();
        where.fieldEpisodeNumber(Long.parseLong(episode));
        String order_limit = RadioTheaterContract.ConfigEpisodesEntry.FIELD_EPISODE_NUMBER + " ASC LIMIT 1";

        Cursor cursor = RadioTheaterApplication.getRadioTheaterApplicationContext().getContentResolver().query(
                ConfigEpisodesColumns.CONTENT_URI,  // the 'content://' Uri to query
                null,                               // projection String[] - leaving "columns" null just returns all the columns.
                where.sel(),                        // selection - SQL where
                where.args(),                       // selection args String[] - values for the "where" clause
                order_limit);                       // sort order and limit (String)

        if (cursor != null && cursor.getCount() > 0) {
            ConfigEpisodesCursor configEpisodesCursor = new ConfigEpisodesCursor(cursor);
            record = getConfigEpisodesContentValues(configEpisodesCursor);
            cursor.close();
        }
        else {
            LogHelper.v(TAG, "SQL: episode "+episode+" not found");
        }
        return record;
    }

    public static EpisodesCursor getEpisodesCursor(long episode) {
        LogHelper.v(TAG, "getEpisodesCursor: episode="+episode);
        EpisodesSelection where = new EpisodesSelection();
        where.fieldEpisodeNumber(episode);
        String order_limit = RadioTheaterContract.EpisodesEntry.FIELD_EPISODE_NUMBER + " ASC LIMIT 1";

        Cursor cursor = RadioTheaterApplication.getRadioTheaterApplicationContext().getContentResolver().query(
                EpisodesColumns.CONTENT_URI,        // the 'content://' Uri to query
                null,                               // projection String[] - leaving "columns" null just returns all the columns.
                where.sel(),                        // selection - SQL where
                where.args(),                       // selection args String[] - values for the "where" clause
                order_limit);                       // sort order and limit (String)

        return (cursor != null && cursor.getCount() > 0) ? new EpisodesCursor(cursor) : null;
    }

    public static ActorsCursor getActorsCursor(long actor) {
        LogHelper.v(TAG, "getActorsCursor: actor="+actor);
        ActorsSelection where = new ActorsSelection();
        where.fieldActorId(actor);
        String order_limit = RadioTheaterContract.ActorsEntry.FIELD_ACTOR_ID + " ASC LIMIT 1";

        Cursor cursor = RadioTheaterApplication.getRadioTheaterApplicationContext().getContentResolver().query(
                ActorsColumns.CONTENT_URI,          // the 'content://' Uri to query
                null,                               // projection String[] - leaving "columns" null just returns all the columns.
                where.sel(),                        // selection - SQL where
                where.args(),                       // selection args String[] - values for the "where" clause
                order_limit);                       // sort order and limit (String)

        return (cursor != null && cursor.getCount() > 0) ? new ActorsCursor(cursor) : null;
    }

    public static WritersCursor getWritersCursor(long writer) {
        LogHelper.v(TAG, "getWritersCursor: writer="+writer);
        WritersSelection where = new WritersSelection();
        where.fieldWriterId(writer);
        String order_limit = RadioTheaterContract.WritersEntry.FIELD_WRITER_ID + " ASC LIMIT 1";

        Cursor cursor = RadioTheaterApplication.getRadioTheaterApplicationContext().getContentResolver().query(
                WritersColumns.CONTENT_URI,         // the 'content://' Uri to query
                null,                               // projection String[] - leaving "columns" null just returns all the columns.
                where.sel(),                        // selection - SQL where
                where.args(),                       // selection args String[] - values for the "where" clause
                order_limit);                       // sort order and limit (String)

        return (cursor != null && cursor.getCount() > 0) ? new WritersCursor(cursor) : null;
    }

    public static ActorsEpisodesCursor getActorsEpisodesCursor(long episode) {
        LogHelper.v(TAG, "getActorsEpisodesCursor: episode="+episode);
        ActorsEpisodesSelection where = new ActorsEpisodesSelection();
        where.fieldEpisodeNumber(episode);
        String order_limit = RadioTheaterContract.ActorsEpisodesEntry.FIELD_EPISODE_NUMBER + " ASC";

        Cursor cursor = RadioTheaterApplication.getRadioTheaterApplicationContext().getContentResolver().query(
                ActorsEpisodesColumns.CONTENT_URI,  // the 'content://' Uri to query
                null,                               // projection String[] - leaving "columns" null just returns all the columns.
                where.sel(),                        // selection - SQL where
                where.args(),                       // selection args String[] - values for the "where" clause
                order_limit);                       // sort order and limit (String)

        return (cursor != null && cursor.getCount() > 0) ? new ActorsEpisodesCursor(cursor) : null;
    }

    public static WritersEpisodesCursor getWritersEpisodesCursor(long episode) {
        LogHelper.v(TAG, "getWritersEpisodesCursor: episode="+episode);
        WritersEpisodesSelection where = new WritersEpisodesSelection();
        where.fieldEpisodeNumber(episode);
        String order_limit = RadioTheaterContract.WritersEpisodesEntry.FIELD_EPISODE_NUMBER + " ASC";

        Cursor cursor = RadioTheaterApplication.getRadioTheaterApplicationContext().getContentResolver().query(
                WritersEpisodesColumns.CONTENT_URI, // the 'content://' Uri to query
                null,                               // projection String[] - leaving "columns" null just returns all the columns.
                where.sel(),                        // selection - SQL where
                where.args(),                       // selection args String[] - values for the "where" clause
                order_limit);                       // sort order and limit (String)

        return (cursor != null && cursor.getCount() > 0) ? new WritersEpisodesCursor(cursor) : null;
    }

    public static ConfigurationCursor getCursorForConfigurationDevice(String deviceId) {
        LogHelper.v(TAG, "getCursorForConfigurationDevice: deviceId="+deviceId);
        ConfigurationSelection where = new ConfigurationSelection();
        // find the specified configuration
        where.fieldDeviceId(deviceId);

        String order_limit = RadioTheaterContract.ConfigurationEntry.FIELD_DEVICE_ID + " ASC LIMIT 1";

        Cursor cursor = RadioTheaterApplication.getRadioTheaterApplicationContext().getContentResolver().query(
                ConfigurationColumns.CONTENT_URI,  // the 'content://' Uri to query
                null,                               // projection String[] - leaving "columns" null just returns all the columns.
                where.sel(),                        // selection - SQL where
                where.args(),                       // selection args String[] - values for the "where" clause
                order_limit);                       // sort order and limit (String)

        return (cursor != null) ? new ConfigurationCursor(cursor) : null;
    }

    public static ConfigEpisodesCursor getCursorForNextAvailableEpisode() {
        LogHelper.v(TAG, "getCursorForNextAvailableEpisode");
        ConfigEpisodesSelection where = new ConfigEpisodesSelection();
        // find the next unwatched episode, in airdate order
        where.fieldEpisodeHeard(false);
        if (! NetworkHelper.isOnline(RadioTheaterApplication.getRadioTheaterApplicationContext())) {
            // find the next DOWNLOADED unwatched episode, in airdate order
            where.fieldEpisodeDownloaded(true);
        }

        String order_limit = RadioTheaterContract.ConfigEpisodesEntry.FIELD_EPISODE_NUMBER + " ASC LIMIT 1";

        Cursor cursor = RadioTheaterApplication.getRadioTheaterApplicationContext().getContentResolver().query(
                ConfigEpisodesColumns.CONTENT_URI,  // the 'content://' Uri to query
                null,                               // projection String[] - leaving "columns" null just returns all the columns.
                where.sel(),                        // selection - SQL where
                where.args(),                       // selection args String[] - values for the "where" clause
                order_limit);                       // sort order and limit (String)

        return (cursor != null) ? new ConfigEpisodesCursor(cursor) : null;
    }

    @NonNull
    public static ConfigurationContentValues getConfigurationContentValues(ConfigurationCursor cursor) {
        LogHelper.v(TAG, "getConfigurationContentValues");
        LogHelper.v(TAG, "getConfigurationContentValues: SQL found "+cursor.getCount()+" records");
        ConfigurationContentValues record = new ConfigurationContentValues();

        if (cursor.moveToNext()) {
            try {
                record.putFieldUserEmail(BaseActivity.getEmail());
                record.putFieldUserName(BaseActivity.getName() != null ? BaseActivity.getName() : "unknown");
                record.putFieldDeviceId(BaseActivity.getAdvertId());

                //#IFDEF 'PAID'
                //boolean paidVersion = true;
                //boolean purchased = true;
                //boolean noAdsForShow = true;
                //#ENDIF

                //#IFDEF 'TRIAL'
                boolean paidVersion = cursor.getFieldPaidVersion();
                boolean purchased = cursor.getFieldPurchaseAccess();
                boolean noAdsForShow = cursor.getFieldPurchaseNoads();
                //#ENDIF

                record.putFieldPaidVersion(paidVersion);
                record.putFieldPurchaseAccess(purchased);
                record.putFieldPurchaseNoads(noAdsForShow);

                int listenCount = cursor.getFieldTotalListenCount();
                record.putFieldTotalListenCount(listenCount);
            } catch (Exception e) {
                LogHelper.e(TAG, "RECORD NOT FOUND: Exception=" + e);
                record = null;
            }
        }
        cursor.close();
        return record;
    }

    @NonNull
    public static ConfigEpisodesContentValues getConfigEpisodesContentValues(ConfigEpisodesCursor cursor) {
        LogHelper.v(TAG, "getConfigEpisodesContentValues: SQL found "+cursor.getCount()+" records");
        ConfigEpisodesContentValues record = new ConfigEpisodesContentValues();

        if (cursor.moveToNext()) {
            try {
                long episodeNumber = cursor.getFieldEpisodeNumber();
                record.putFieldEpisodeNumber(episodeNumber);

                //#IFDEF 'PAID'
                //boolean purchased = true;
                //boolean noAdsForShow = true;
                //#ENDIF

                //#IFDEF 'TRIAL'
                boolean purchased = cursor.getFieldPurchasedAccess();
                boolean noAdsForShow = cursor.getFieldPurchasedNoads();
                //#ENDIF

                record.putFieldPurchasedAccess(purchased);
                record.putFieldPurchasedNoads(noAdsForShow);

                boolean downloaded = cursor.getFieldEpisodeDownloaded();
                record.putFieldEpisodeDownloaded(downloaded);

                boolean episodeHeard = cursor.getFieldEpisodeHeard();
                record.putFieldEpisodeHeard(episodeHeard);

                int listenCount = cursor.getFieldListenCount();
                record.putFieldListenCount(listenCount);
            } catch (Exception e) {
                LogHelper.e(TAG, "RECORD NOT FOUND: Exception=" + e);
                record = null;
            }
        }
        return record;
    }

    public static Uri insertConfiguration(ContentValues configurationValues) {
        LogHelper.v(TAG, "insertConfigurationValues");
        if (configurationValues == null || configurationValues.size() == 0) {
            LogHelper.w(TAG, "unable to insertConfigurationValues! - null or empty values.");
            return null;
        }
        Uri configurationEntry = RadioTheaterContract.ConfigurationEntry.buildConfigurationUri();
        return RadioTheaterApplication.getRadioTheaterApplicationContext().getContentResolver().insert(configurationEntry, configurationValues);
    }

    // update local SQLite
    public static int updateConfigurationValues(String deviceId, ContentValues configurationValues) {
        LogHelper.v(TAG, "updateConfigurationValues");
        if (configurationValues == null || configurationValues.size() == 0) {
            LogHelper.w(TAG, "unable to updateConfigurationValues! - null or empty values.");
            return 0;
        }
        Uri configurationEntry = RadioTheaterContract.ConfigurationEntry.buildConfigurationUri();
        String whereClause = RadioTheaterContract.ConfigurationEntry.FIELD_DEVICE_ID + "=?";
        String whereCondition[] = new String[]{deviceId};
        int rc = RadioTheaterApplication.getRadioTheaterApplicationContext().getContentResolver().update(configurationEntry, configurationValues, whereClause, whereCondition);
        return rc;
    }

    public static Uri insertConfigEntryValues(ContentValues configEntryValues) {
        LogHelper.v(TAG, "insertConfigEntryValues");
        if (configEntryValues == null || configEntryValues.size() == 0) {
            LogHelper.w(TAG, "unable to insertConfigEntryValues! - null or empty values.");
            return null;
        }
        Uri configEntry = RadioTheaterContract.ConfigEpisodesEntry.buildConfigEpisodesUri();
        return RadioTheaterApplication.getRadioTheaterApplicationContext().getContentResolver().insert(configEntry, configEntryValues);
    }

    // update local SQLite
    public static int updateConfigEntryValues(String episode, ContentValues configEntryValues) {
        LogHelper.v(TAG, "updateConfigEntryValues");
        if (configEntryValues == null || configEntryValues.size() == 0) {
            LogHelper.w(TAG, "unable to updateConfigurationValues! - null or empty values.");
            return 0;
        }
        Uri configEntry = RadioTheaterContract.ConfigEpisodesEntry.buildConfigEpisodesUri();
        String whereClause = RadioTheaterContract.ConfigEpisodesEntry.FIELD_EPISODE_NUMBER + "=?";
        String whereCondition[] = new String[]{episode};
        int rc = RadioTheaterApplication.getRadioTheaterApplicationContext().getContentResolver().update(configEntry, configEntryValues, whereClause, whereCondition);
        return rc;
    }

}

