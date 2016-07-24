package com.harlie.radiotheater.radiomysterytheater.data_helper;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.firebase.client.Firebase;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.harlie.radiotheater.radiomysterytheater.BaseActivity;
import com.harlie.radiotheater.radiomysterytheater.RadioTheaterApplication;
import com.harlie.radiotheater.radiomysterytheater.RadioTheaterWidgetService;
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
import com.harlie.radiotheater.radiomysterytheater.firebase.FirebaseConfigEpisode;
import com.harlie.radiotheater.radiomysterytheater.firebase.FirebaseConfiguration;
import com.harlie.radiotheater.radiomysterytheater.utils.LogHelper;
import com.harlie.radiotheater.radiomysterytheater.utils.NetworkHelper;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataHelper {
    private final static String TAG = "LEE: <" + DataHelper.class.getSimpleName() + ">";

    public static final int MAX_TRIAL_EPISODES = 19;

    protected static volatile boolean sLoadedOK;
    protected static volatile long sEpisodeNumber;
    protected static volatile int sAllListenCount;
    protected static volatile long sDuration;
    protected static volatile long sCurrentPosition;

    protected static volatile String sAdvId;
    protected static volatile String sName;
    protected static volatile String sEmail;
    protected static volatile String sPassword;
    protected static volatile String sUID;

    protected static volatile String sAirdate;
    protected static volatile String sEpisodeTitle;
    protected static volatile String sEpisodeDescription;
    protected static volatile String sEpisodeWeblinkUrl;
    protected static volatile String sEpisodeDownloadUrl;

    protected static volatile boolean sFoundFirebaseDeviceId;
    protected static volatile boolean sPurchased;
    protected static volatile boolean sNoAdsForShow;
    protected static volatile boolean sDownloaded;
    protected static volatile boolean sEpisodeHeard;

    protected static volatile FirebaseAuth sFirebaseAuth;
    protected static volatile Firebase sFirebase;
    protected static volatile DatabaseReference sDatabase;
    protected static volatile FirebaseAnalytics sFirebaseAnalytics;
    protected static volatile ConfigurationContentValues sConfiguration;

    public static ConfigEpisodesContentValues getConfigEpisodeForEpisode(String episode) {
        LogHelper.v(TAG, "SQLITE: getConfigEpisodeForEpisode: episode="+episode);
        if (Long.valueOf(episode) == 0) {
            return null;
        }
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
            LogHelper.v(TAG, "SQLITE: episode "+episode+" not found");
        }
        return record;
    }

    public static EpisodesCursor getEpisodesCursor(long episode) {
        LogHelper.v(TAG, "SQLITE: getEpisodesCursor: episode="+episode);
        if (episode == 0) {
            return null;
        }
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
        LogHelper.v(TAG, "SQLITE: getActorsCursor: actor="+actor);
        if (actor == 0) {
            return null;
        }
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
        LogHelper.v(TAG, "SQLITE: getWritersCursor: writer="+writer);
        if (writer == 0) {
            return null;
        }
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
        LogHelper.v(TAG, "SQLITE: getActorsEpisodesCursor: episode="+episode);
        if (episode == 0) {
            return null;
        }
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
        LogHelper.v(TAG, "SQLITE: getWritersEpisodesCursor: episode="+episode);
        if (episode == 0) {
            return null;
        }
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
        LogHelper.v(TAG, "SQLITE: getCursorForConfigurationDevice: deviceId="+deviceId);
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
        LogHelper.v(TAG, "SQLITE: getCursorForNextAvailableEpisode");
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

    public static ConfigurationContentValues getConfigurationContentValues(ConfigurationCursor cursor) {
        LogHelper.v(TAG, "SQLITE: getConfigurationContentValues");
        LogHelper.v(TAG, "SQLITE: getConfigurationContentValues: SQL found "+cursor.getCount()+" records");
        ConfigurationContentValues record = new ConfigurationContentValues();

        if (cursor.moveToNext()) {
            try {
                record.putFieldUserEmail(getEmail());
                record.putFieldUserName(getName() != null ? getName() : "unknown");
                record.putFieldDeviceId(getAdvertId());

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

                //noinspection ConstantConditions
                record.putFieldPaidVersion(paidVersion);
                //noinspection ConstantConditions
                record.putFieldPurchaseAccess(purchased);
                //noinspection ConstantConditions
                record.putFieldPurchaseNoads(noAdsForShow);

                int listenCount = cursor.getFieldTotalListenCount();
                record.putFieldTotalListenCount(listenCount);
            } catch (Exception e) {
                LogHelper.e(TAG, "SQLITE: RECORD NOT FOUND: Exception=" + e);
                record = null;
            }
        }
        cursor.close();
        return record;
    }

    @NonNull
    public static ConfigEpisodesContentValues getConfigEpisodesContentValues(ConfigEpisodesCursor cursor) {
        LogHelper.v(TAG, "SQLITE: getConfigEpisodesContentValues: SQL found "+cursor.getCount()+" records");
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

                //noinspection ConstantConditions
                record.putFieldPurchasedAccess(purchased);
                //noinspection ConstantConditions
                record.putFieldPurchasedNoads(noAdsForShow);

                boolean downloaded = cursor.getFieldEpisodeDownloaded();
                record.putFieldEpisodeDownloaded(downloaded);

                boolean episodeHeard = cursor.getFieldEpisodeHeard();
                record.putFieldEpisodeHeard(episodeHeard);

                int listenCount = cursor.getFieldListenCount();
                record.putFieldListenCount(listenCount);
            } catch (Exception e) {
                LogHelper.e(TAG, "SQLITE: RECORD NOT FOUND: Exception=" + e);
                record = null;
            }
        }
        return record;
    }

    @SuppressWarnings("UnusedReturnValue")
    public static Uri insertConfiguration(ContentValues configurationValues) {
        LogHelper.v(TAG, "SQLITE: insertConfigurationValues");
        if (configurationValues == null || configurationValues.size() == 0) {
            LogHelper.w(TAG, "SQLITE: unable to insertConfigurationValues! - null or empty values.");
            return null;
        }
        Uri configurationEntry = RadioTheaterContract.ConfigurationEntry.buildConfigurationUri();
        return RadioTheaterApplication.getRadioTheaterApplicationContext().getContentResolver().insert(configurationEntry, configurationValues);
    }

    // update local SQLite
    @SuppressWarnings("UnusedReturnValue")
    public static int updateConfiguration(String deviceId, ContentValues configurationValues) {
        LogHelper.v(TAG, "SQLITE: updateConfiguration");
        if (configurationValues == null || configurationValues.size() == 0) {
            LogHelper.w(TAG, "SQLITE: unable to updateConfiguration! - null or empty values.");
            return 0;
        }
        Uri configurationEntry = RadioTheaterContract.ConfigurationEntry.buildConfigurationUri();
        String whereClause = RadioTheaterContract.ConfigurationEntry.FIELD_DEVICE_ID + "=?";
        String whereCondition[] = new String[]{deviceId};
        return RadioTheaterApplication.getRadioTheaterApplicationContext().getContentResolver().update(configurationEntry, configurationValues, whereClause, whereCondition);
    }

    public static Uri insertConfigEntry(ContentValues configEntryValues) {
        LogHelper.v(TAG, "SQLITE: insertConfigEntry");
        if (configEntryValues == null || configEntryValues.size() == 0) {
            LogHelper.w(TAG, "SQLITE: unable to insertConfigEntry! - null or empty values.");
            return null;
        }
        Uri configEntry = RadioTheaterContract.ConfigEpisodesEntry.buildConfigEpisodesUri();
        return RadioTheaterApplication.getRadioTheaterApplicationContext().getContentResolver().insert(configEntry, configEntryValues);
    }

    // update local SQLite
    @SuppressWarnings("UnusedReturnValue")
    public static int updateConfigEpisodesEntry(String episode, ContentValues configEpisodesEntryValues) {
        LogHelper.v(TAG, "SQLITE: updateConfigEpisodesEntry");
        if (configEpisodesEntryValues == null || configEpisodesEntryValues.size() == 0) {
            LogHelper.w(TAG, "SQLITE: unable to updateConfigEpisodesValues! - null or empty values.");
            return 0;
        }
        Uri configEpisodesEntry = RadioTheaterContract.ConfigEpisodesEntry.buildConfigEpisodesUri();
        String whereClause = RadioTheaterContract.ConfigEpisodesEntry.FIELD_EPISODE_NUMBER + "=?";
        String whereCondition[] = new String[]{episode};
        return RadioTheaterApplication.getRadioTheaterApplicationContext().getContentResolver().update(configEpisodesEntry, configEpisodesEntryValues, whereClause, whereCondition);
    }


    public static boolean getEpisodeDataForCursor(ConfigEpisodesCursor configCursor) {
        LogHelper.v(TAG, "getEpisodeDataForCursor");
        boolean foundEpisode = false;
        if (configCursor != null && configCursor.moveToNext()) {
            // found the next episode to listen to
            sEpisodeNumber = configCursor.getFieldEpisodeNumber();
            sPurchased = configCursor.getFieldPurchasedAccess();
            sNoAdsForShow = configCursor.getFieldPurchasedNoads();
            sDownloaded = configCursor.getFieldEpisodeDownloaded();
            sEpisodeHeard = configCursor.getFieldEpisodeHeard();
            configCursor.close();
            foundEpisode = getEpisodeInfoFor(sEpisodeNumber);
        }
        return foundEpisode;
    }

    public static boolean getEpisodeInfoFor(long episodeId) {
        LogHelper.v(TAG, "getEpisodeInfoFor: "+episodeId);
        // get this episode's detail info
        boolean foundEpisode = false;
        EpisodesCursor episodesCursor = DataHelper.getEpisodesCursor(episodeId);
        if (episodesCursor != null && episodesCursor.moveToNext()) {
            sEpisodeNumber = episodesCursor.getFieldEpisodeNumber();
            sAirdate = episodesCursor.getFieldAirdate();
            sEpisodeTitle = episodesCursor.getFieldEpisodeTitle();
            sEpisodeDescription = episodesCursor.getFieldEpisodeDescription();
            sEpisodeWeblinkUrl = Uri.parse(episodesCursor.getFieldWeblinkUrl()).getEncodedPath();
            sEpisodeDownloadUrl = Uri.parse("http://" + episodesCursor.getFieldDownloadUrl()).toString();
            episodesCursor.close();
            foundEpisode = true;
        }
        return foundEpisode;
    }

    //--------------------------------------------------------------------------------
    // Getters and Setters
    //--------------------------------------------------------------------------------

    public static boolean isLoadedOK() {
        return sLoadedOK;
    }

    public static void setLoadedOK(boolean sLoadedOK) {
        DataHelper.sLoadedOK = sLoadedOK;
    }

    public static String getUID() {
        if (sUID == null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(RadioTheaterApplication.getRadioTheaterApplicationContext());
            sUID = sharedPreferences.getString("userUID", "");
            if (sUID.length() == 0) {
                sUID = null;
            }
        }
        return sUID;
    }

    public static void setUID(String uid) {
        if (uid != null && uid.length() > 0) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(RadioTheaterApplication.getRadioTheaterApplicationContext());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("userUID", uid);
            editor.apply();
        }
        sUID = uid;
    }

    public static String getName() {
        if (sName == null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(RadioTheaterApplication.getRadioTheaterApplicationContext());
            sName = sharedPreferences.getString("userName", "");
            if (sName.length() == 0) {
                sName = null;
            }
        }
        return sName;
    }

    public static void setName(String name) {
        if (name != null && name.length() > 0) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(RadioTheaterApplication.getRadioTheaterApplicationContext());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("userName", name);
            editor.apply();
        }
        sName = name;
    }

    public static String getEmail() {
        if (sEmail == null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(RadioTheaterApplication.getRadioTheaterApplicationContext());
            sEmail = sharedPreferences.getString("userEmail", "");
            if (sEmail.length() == 0) {
                sEmail = null;
            }
        }
        return sEmail;
    }

    public static void setEmail(String email) {
        if (email != null && email.length() > 0) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(RadioTheaterApplication.getRadioTheaterApplicationContext());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("userEmail", email);
            editor.apply();
        }
        sEmail = email;
    }

    public static String getPass() {
        if (sPassword == null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(RadioTheaterApplication.getRadioTheaterApplicationContext());
            sPassword = sharedPreferences.getString("userPass", "");
            if (sPassword.length() == 0) {
                sPassword = null;
            }
        }
        return sPassword;
    }

    public static void setPass(String password) {
        if (password != null && password.length() > 0) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(RadioTheaterApplication.getRadioTheaterApplicationContext());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("userPass", password);
            editor.apply();
        }
        sPassword = password;
    }

    public static String getAdvertId() {
        if (getAdvId() == null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(RadioTheaterApplication.getRadioTheaterApplicationContext());
            String advertID = sharedPreferences.getString("advertID", getAdvId()); // passing null object for default
            if (advertID != null && advertID.length() != 0) {
                setAdvId(advertID);
            }
        }
        LogHelper.v(TAG, "AdvId="+getAdvId());
        return getAdvId();
    }

    public static void setAdvertId(String advId) {
        if (advId != null && advId.length() > 0) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(RadioTheaterApplication.getRadioTheaterApplicationContext());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("advertID", advId);
            editor.apply();
        }
        LogHelper.v(TAG, "set AdvId="+advId);
        sAdvId = advId;
    }

    public static String getAdvId() {
        return sAdvId;
    }

    public static void setAdvId(String sAdvId) {
        DataHelper.sAdvId = sAdvId;
    }

    public static long getEpisodeNumber() {
        return sEpisodeNumber;
    }

    public static String getEpisodeNumberString() {
        return String.valueOf(sEpisodeNumber);
    }

    public static int getAllListenCount() {
        return sAllListenCount;
    }

    public static void setAllListenCount(int sAllListenCount) {
        DataHelper.sAllListenCount = sAllListenCount;
    }

    public static long getDuration() {
        return sDuration;
    }

    public static void setDuration(long sDuration) {
        DataHelper.sDuration = sDuration;
    }

    public static long getCurrentPosition() {
        return sCurrentPosition;
    }

    public static void setCurrentPosition(long sCurrentPosition) {
        DataHelper.sCurrentPosition = sCurrentPosition;
    }

    public static void setEpisodeNumber(long sEpisodeNumber) {
        DataHelper.sEpisodeNumber = sEpisodeNumber;
    }

    public static String getAirdate() {
        return sAirdate;
    }

    public static void setAirdate(String sAirdate) {
        DataHelper.sAirdate = sAirdate;
    }

    public static String getEpisodeTitle() {
        return sEpisodeTitle;
    }

    public static void setEpisodeTitle(String sEpisodeTitle) {
        DataHelper.sEpisodeTitle = sEpisodeTitle;
    }

    public static String getEpisodeDescription() {
        return sEpisodeDescription;
    }

    public static void setEpisodeDescription(String sEpisodeDescription) {
        DataHelper.sEpisodeDescription = sEpisodeDescription;
    }

    public static String getEpisodeWeblinkUrl() {
        return sEpisodeWeblinkUrl;
    }

    public static void setEpisodeWeblinkUrl(String sEpisodeWeblinkUrl) {
        DataHelper.sEpisodeWeblinkUrl = sEpisodeWeblinkUrl;
    }

    public static String getEpisodeDownloadUrl() {
        return sEpisodeDownloadUrl;
    }

    public static void setEpisodeDownloadUrl(String sEpisodeDownloadUrl) {
        DataHelper.sEpisodeDownloadUrl = sEpisodeDownloadUrl;
    }

    public static boolean isFoundFirebaseDeviceId() {
        return sFoundFirebaseDeviceId;
    }

    public static void setFoundFirebaseDeviceId(boolean sFoundFirebaseDeviceId) {
        DataHelper.sFoundFirebaseDeviceId = sFoundFirebaseDeviceId;
    }

    public static boolean isPurchased() {
        return sPurchased;
    }

    public static void setPurchased(boolean sPurchased) {
        DataHelper.sPurchased = sPurchased;
    }

    public static boolean isNoAdsForShow() {
        return sNoAdsForShow;
    }

    public static void setNoAdsForShow(boolean sNoAdsForShow) {
        DataHelper.sNoAdsForShow = sNoAdsForShow;
    }

    public static boolean isDownloaded() {
        return sDownloaded;
    }

    public static void setDownloaded(boolean sDownloaded) {
        DataHelper.sDownloaded = sDownloaded;
    }

    public static boolean isEpisodeHeard() {
        return sEpisodeHeard;
    }

    public static void setEpisodeHeard(boolean sEpisodeHeard) {
        DataHelper.sEpisodeHeard = sEpisodeHeard;
    }

    public static boolean isTrial() {
        boolean trial = true;
        //#IFDEF 'TRIAL'
        trial = (sAllListenCount < MAX_TRIAL_EPISODES);
        //#ENDIF
        return trial;
    }

    public static FirebaseAnalytics getFirebaseAnalytics() {
        return sFirebaseAnalytics;
    }

    public static void setFirebaseAnalytics(FirebaseAnalytics sFirebaseAnalytics) {
        DataHelper.sFirebaseAnalytics = sFirebaseAnalytics;
    }

    public static DatabaseReference getDatabase() {
        return sDatabase;
    }

    public static void setDatabase(DatabaseReference sDatabase) {
        DataHelper.sDatabase = sDatabase;
    }

    public static Firebase getFirebase() {
        return sFirebase;
    }

    public static void setFirebase(Firebase sFirebase) {
        DataHelper.sFirebase = sFirebase;
    }

    public static FirebaseAuth getFirebaseAuth() {
        return sFirebaseAuth;
    }

    public static void setFirebaseAuth(FirebaseAuth sFirebaseAuth) {
        DataHelper.sFirebaseAuth = sFirebaseAuth;
    }

    public static ConfigurationContentValues getConfiguration() {
        return sConfiguration;
    }

    public static void showCurrentInfo() {
        LogHelper.v(TAG, "===> EPISODE INFO"
                + ": AllListenCount=" + getAllListenCount()
                + ": EpisodeTitle=" + getEpisodeTitle()
                + ": EpisodeNumber=" + getEpisodeNumber()
                + ": Airdate=" + getAirdate()
                + ": EpisodeDescription=" + getEpisodeDescription()
                + ": EpisodeWeblinkUrl=" + getEpisodeWeblinkUrl()
                + ": EpisodeDownloadUrl=" + getEpisodeDownloadUrl()
                + ", Purchased=" + isPurchased()
                + ", NoAdsForShow=" + isNoAdsForShow()
                + ", Downloaded=" + isDownloaded()
                + ", EpisodeHeard=" + isEpisodeHeard());
    }

    public static void initializeFirebase() {
        Context context = RadioTheaterApplication.getRadioTheaterApplicationContext();
        Firebase.setAndroidContext(context);
        DataHelper.setFirebaseAuth(FirebaseAuth.getInstance());
        DataHelper.setFirebase(new Firebase("https://radio-mystery-theater.firebaseio.com"));
        DataHelper.setDatabase(FirebaseDatabase.getInstance().getReference());
        DataHelper.setFirebaseAnalytics(FirebaseAnalytics.getInstance(context));
    }

    public static void loadAnyExistingFirebaseConfigurationValues(final BaseActivity activity) {
        String deviceId = getAdvertId();
        LogHelper.v(TAG, "*** FIREBASE REQUEST *** - loadAnyExistingFirebaseConfigurationValues: deviceId="+deviceId);
        if (deviceId == null) {
            LogHelper.e(TAG, "ERROR: loadAnyExistingFirebaseConfigurationValues deviceId is null");
            return;
        }
        if (getEmail() == null) {
            LogHelper.e(TAG, "ERROR: loadAnyExistingFirebaseConfigurationValues email is null");
            return;
        }

        // Use a timer to determine if this deviceId is tracked inside Firebase
        setFoundFirebaseDeviceId(false);
        activity.getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                LogHelper.v(TAG, "*** CHECK THE USER CONFIGURATION ***");
                updateTheUserConfiguration();
            }
        }, 90000); // allow a minute and a half

        // Attach a listener to read the data initially
        getDatabase().child("configuration").child("device").child(deviceId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                LogHelper.v(TAG, "*** -------------------------------------------------------------------------------- ***");
                // load the dataSnapshot info
                Object configurationObject = dataSnapshot.getValue();
                if (configurationObject == null) {
                    LogHelper.e(TAG, "the Firebase User Configuration (DataSnapshot) is null! deviceId="+getAdvertId()+", email="+getEmail());
                    //updateTheUserConfiguration();
                    return;
                }
                String configurationJSON = configurationObject.toString();
                setFoundFirebaseDeviceId(true);
                LogHelper.v(TAG, "===> Firebase configurationJSON="+configurationJSON);

                //#IFDEF 'PAID'
                //boolean paidVersion = true;
                //boolean purchaseAccess = true;
                //boolean purchaseNoads = true;
                //#ENDIF

                //#IFDEF 'TRIAL'
                boolean paidVersion = false;
                boolean purchaseAccess = false;
                boolean purchaseNoads = false;
                //#ENDIF

                createConfiguration(paidVersion, purchaseAccess, purchaseNoads);

                int decodedListenCount = 0;
                //
                // since we only need a single value from the JSON use a regex pattern
                // EXAMPLE JSON: {firebase_user_name=colefklbBSTHPrRWGPt9BWYcCYS2, firebase_device_id=bf5874b5-0cd5-4457-9401-6fd384edb579, firebase_email=lee@harlie.com, firebase_authenticated=true, firebase_total_listen_count=19}
                //
                Pattern pattern = Pattern.compile(".*firebase_total_listen_count=([0-9]+).*");
                Matcher matcher = pattern.matcher((configurationJSON));
                if (matcher.find()) {
                    try {
                        decodedListenCount = Integer.parseInt(matcher.group(1));
                        LogHelper.v(TAG, "---> DECODED LISTEN_COUNT="+decodedListenCount);
                        checkUpdateWidget(activity, decodedListenCount);
                        activity.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                activity.enableButtons();
                            }
                        });
                    }
                    catch (NumberFormatException e) {
                        LogHelper.e(TAG, "*** UNABLE TO DECODE LISTEN_COUNT FROM FIREBASE *** - NumberFormatException: configuration="+configurationJSON);
                    }
                }
                sConfiguration.putFieldTotalListenCount(decodedListenCount);
                LogHelper.v(TAG, "*** -------------------------------------------------------------------------------- ***");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogHelper.v(TAG, "loadAnyExistingFirebaseConfigurationValues - onCancelled");
                activity.startAuthenticationActivity();
            }
        });

    }

    public static void checkUpdateWidget(Context context, int listenCount) {
        boolean updateWidget = true;

        //#IFDEF 'TRIAL'
        updateWidget = (listenCount < DataHelper.MAX_TRIAL_EPISODES);
        //#ENDIF

        LogHelper.v(TAG, "--> checkUpdateWidget: listenCount="+listenCount+", updateWidget="+updateWidget);
        if (updateWidget && isLoadedOK()) {
            LogHelper.v(TAG, "*** TURN ON WIDGET FUNCTIONALITY ***");
            RadioTheaterWidgetService.setPaidVersion(context, updateWidget);
        }
    }

    private static ConfigurationContentValues createConfiguration(boolean paidVersion, boolean purchaseAccess, boolean purchaseNoads) {
        LogHelper.v(TAG, "createConfiguration");
        sConfiguration = new ConfigurationContentValues();
        sConfiguration.putFieldUserEmail(getEmail());
        sConfiguration.putFieldUserName(getName() != null ? getName() : "unknown");
        sConfiguration.putFieldDeviceId(getAdvertId());

        sConfiguration.putFieldPaidVersion(paidVersion);
        sConfiguration.putFieldPurchaseAccess(purchaseAccess);
        sConfiguration.putFieldPurchaseNoads(purchaseNoads);
        return sConfiguration;
    }

    // the Configuration exists initially in SQLite, then in Firebase also.
    // but if a new install happens for the account, we need to remember LISTEN_COUNT
    // and pull it from Firebase instead of using local data.
    private static void updateTheUserConfiguration() {
        LogHelper.v(TAG, "*** updateTheUserConfiguration ***");
        ConfigurationContentValues configurationContent = null;
        ContentValues sqliteConfiguration = null;
        // 1) load local SQLite entry for deviceId
        ConfigurationCursor configurationCursor = DataHelper.getCursorForConfigurationDevice(getAdvertId());
        boolean createLocalDeviceRecord = false;
        if (configurationCursor != null) {
            LogHelper.v(TAG, "found existing SQLite user Configuration");
            configurationContent = DataHelper.getConfigurationContentValues(configurationCursor);
            sqliteConfiguration = configurationContent.values();
            configurationCursor.close();
        }
        else {
            LogHelper.v(TAG, "*** SQLITE: NO LOCAL DEVICE CONFIGURATION FOUND ***");
            createLocalDeviceRecord = true;
        }

        if (! isFoundFirebaseDeviceId()) { // deviceId not in Firebase
            LogHelper.v(TAG, "device entry for Configuration doesn't exist in Firebase yet..");
            // 2) if local SQLite entry doesn't exist, create it
            if (sqliteConfiguration == null) { // no SQLite Configuration either
                LogHelper.v(TAG, "*** INITIALIZING USER *** - create local SQLite Configuration");

                //#IFDEF 'PAID'
                //sConfiguration = createConfiguration(true, true, true);
                //RadioTheaterWidgetService.setPaidVersion(this, true);
                //#ENDIF

                //#IFDEF 'TRIAL'
                sConfiguration = createConfiguration(false, false, false);
                Context context = RadioTheaterApplication.getRadioTheaterApplicationContext();
                RadioTheaterWidgetService.setPaidVersion(context, false);
                //#ENDIF

                sConfiguration.putFieldTotalListenCount(0);
                // 3) update Firebase with the new deviceId entry
                LogHelper.v(TAG, "*** CREATING FIREBASE DEVICE ENTRY ***");
                updateFirebaseConfigurationValues(getAdvertId(), sConfiguration.values());
                return;
            }
        }

        if (createLocalDeviceRecord) {
            LogHelper.v(TAG, "*** SQLITE: CREATING LOCAL DEVICE ENTRY ***");
            DataHelper.insertConfiguration(sConfiguration.values());
        }

        ContentValues firebaseConfiguration = null;
        boolean updateFirebaseWithLocal = false;
        if (sConfiguration != null) {
            firebaseConfiguration = sConfiguration.values();
        }
        else if (sqliteConfiguration != null && configurationContent != null) {
            LogHelper.v(TAG, "have SQLite configuration, but not Firebase - so update Firebase with local");
            updateFirebaseWithLocal = true;
        }

        if (sConfiguration != null || updateFirebaseWithLocal) { // found Firebase Configuration
            LogHelper.v(TAG, "updating Firebase entry for Configuration.");
            // 4) merge and update local SQLite and Firebase
            boolean dirty = mergeConfiguratons(sqliteConfiguration, firebaseConfiguration);
            if (dirty) {
                updateTheConfiguration();
            }
        } else {
            LogHelper.v(TAG, "unable to update Firebase Configuration!");
        }
    }

    private static void updateTheConfiguration() {
        LogHelper.v(TAG, "updateTheConfiguration");
        ContentValues sqliteConfiguration;
        sqliteConfiguration = sConfiguration.values();
        LogHelper.v(TAG, "updateTheConfiguration: SQLITE: sqliteConfiguration=" + sqliteConfiguration.toString());
        DataHelper.updateConfiguration(getAdvertId(), sqliteConfiguration);
        LogHelper.v(TAG, "updateTheConfiguration: FIREBASE: UPDATE FIREBASE DEVICE ENTRY ***");
        updateFirebaseConfigurationValues(getAdvertId(), sConfiguration.values());
    }

    private static boolean mergeConfiguratons(ContentValues sqliteConfiguration, ContentValues firebaseConfiguration) {
        LogHelper.v(TAG, "mergeConfiguratons");
        boolean dirty = false;
        Boolean paidVersion = false;
        Boolean purchaseAccess = false;
        Boolean purchaseNoads = false;
        Boolean firebasePaidVersion = false;
        Boolean firebasePurchaseAccess = false;
        Boolean firebasePurchaseNoads = false;
        Long sqlite_listen_count = Long.valueOf(0);
        Long firebase_listen_count = Long.valueOf(0);

        //#IFDEF 'PAID'
        //paidVersion = true;
        //purchaseAccess = true;
        //purchaseNoads = true;
        //firebasePaidVersion = true;
        //firebasePurchaseAccess = true;
        //firebasePurchaseNoads = true;
        //RadioTheaterWidgetService.setPaidVersion(this, true);
        //#ENDIF

        if (sqliteConfiguration == null) {
            if (firebaseConfiguration == null) {
                LogHelper.w(TAG, "both SQLite and Firebase have NO CONFIGURATION! - can't update");
                return false;
            }
            LogHelper.v(TAG, "no local SQLite configuration exists on this device! - copy from Firebase");
            sqliteConfiguration = firebaseConfiguration;
            dirty = (firebaseConfiguration != null);
        }
        if (sConfiguration == null) {

            //#IFDEF 'PAID'
            //sConfiguration = createConfiguration(true, true, true);
            //RadioTheaterWidgetService.setPaidVersion(this, true);
            //#ENDIF

            //#IFDEF 'TRIAL'
            sConfiguration = createConfiguration(false, false, false);
            Context context = RadioTheaterApplication.getRadioTheaterApplicationContext();
            RadioTheaterWidgetService.setPaidVersion(context, false);
            //#ENDIF

        }
        if (firebaseConfiguration == null || firebaseConfiguration.size() == 0) {
            if (sqliteConfiguration != null) {
                LogHelper.v(TAG, "*** USING THE LOCAL SQLITE CONFIGURATION TO INITIALIZE FIREBASE ***");
                firebaseConfiguration = sqliteConfiguration;
                dirty = true;
            }
            else {
                LogHelper.w(TAG, "*** EMPTY FIREBASE CONFIGURATION ***");
                firebaseConfiguration = new ContentValues();
            }
        }
        if (sqliteConfiguration == null) {
            LogHelper.v(TAG, "*** USING THE FIREBASE CONFIGURATION INITIALIZE SQLITE ***");
            sqliteConfiguration = firebaseConfiguration;
            dirty = true;
        }

        //--------------------------------------------------------------------------------
        //#IFDEF 'TRIAL'
        if (sqliteConfiguration != null && sqliteConfiguration.size() > 0) {
            try {
                paidVersion = sqliteConfiguration.getAsBoolean(ConfigurationColumns.FIELD_PAID_VERSION);
                if (!paidVersion && firebaseConfiguration != null) {
                    firebasePaidVersion = firebaseConfiguration.getAsBoolean(ConfigurationColumns.FIELD_PAID_VERSION);
                    if (firebasePaidVersion != null) {
                        paidVersion = firebasePaidVersion;
                    }
                }
            }
            catch (Exception e) {
                LogHelper.w(TAG, "SQLite: unable to get ConfigurationColumns.FIELD_PAID_VERSION, e=" + e.getMessage());
            }
            try {
                purchaseAccess = sqliteConfiguration.getAsBoolean(ConfigurationColumns.FIELD_PURCHASE_ACCESS);
                if (!purchaseAccess && firebaseConfiguration != null) {
                    firebasePurchaseAccess = firebaseConfiguration.getAsBoolean(ConfigurationColumns.FIELD_PURCHASE_ACCESS);
                    if (firebasePurchaseAccess != null) {
                        purchaseAccess = firebasePurchaseAccess;
                    }
                }
            }
            catch (Exception e) {
                LogHelper.w(TAG, "SQLite: unable to get ConfigurationColumns.FIELD_PURCHASE_ACCESS, e=" + e.getMessage());
            }
            try {
                purchaseNoads = sqliteConfiguration.getAsBoolean(ConfigurationColumns.FIELD_PURCHASE_NOADS);
                if (!purchaseNoads && firebaseConfiguration != null) {
                    firebasePurchaseNoads = firebaseConfiguration.getAsBoolean(ConfigurationColumns.FIELD_PURCHASE_NOADS);
                    if (firebasePurchaseNoads != null) {
                        purchaseNoads = firebasePurchaseNoads;
                    }
                }
            }
            catch (Exception e) {
                LogHelper.w(TAG, "SQLite: unable to get ConfigurationColumns.FIELD_PURCHASE_NOADS, e=" + e.getMessage());
            }
        }
        //#ENDIF
        //--------------------------------------------------------------------------------

        try {
            sqlite_listen_count = sqliteConfiguration.getAsLong(ConfigurationColumns.FIELD_TOTAL_LISTEN_COUNT);
            if (sqlite_listen_count == null) {
                sqlite_listen_count = 0L;
            }
        }
        catch (Exception e) {
            LogHelper.w(TAG, "SQLite: unable to get SQLite ConfigurationColumns.FIELD_TOTAL_LISTEN_COUNT, e="+e.getMessage());
            sqlite_listen_count = 0L;
        }

        try {
            if (firebaseConfiguration != null) {
                firebase_listen_count = firebaseConfiguration.getAsLong(ConfigurationColumns.FIELD_TOTAL_LISTEN_COUNT);
                if (firebase_listen_count == null) {
                    firebase_listen_count = 0L;
                }
            }
        }
        catch (Exception e) {
            LogHelper.w(TAG, "SQLite: unable to get Firebase ConfigurationColumns.FIELD_TOTAL_LISTEN_COUNT, e="+e.getMessage());
            firebase_listen_count = 0L;
        }

        if (Objects.equals(sqlite_listen_count, firebase_listen_count)) {
            LogHelper.v(TAG, "local listen count EQUAL TO firebase listen count: AllListenCount="+ DataHelper.getAllListenCount());
            sConfiguration.putFieldTotalListenCount(DataHelper.getAllListenCount());
            dirty = true; // not really dirty, but we need to update because the record might not exist yet
        }
        else if (sqlite_listen_count > firebase_listen_count) {
            long total_listen_count = (int) sqlite_listen_count.longValue();
            if (total_listen_count > DataHelper.getAllListenCount()) {
                DataHelper.setAllListenCount((int) total_listen_count);
            }
            LogHelper.v(TAG, "local listen count GREATER THAN firebase listen count: AllListenCount="+ DataHelper.getAllListenCount());
            sConfiguration.putFieldTotalListenCount(DataHelper.getAllListenCount());
            dirty = true;
        }
        else {
            long total_listen_count = (int) firebase_listen_count.longValue();
            if (total_listen_count > DataHelper.getAllListenCount()) {
                DataHelper.setAllListenCount((int) total_listen_count);
            }
            LogHelper.v(TAG, "firebase listen count GREATER THAN local listen count: AllListenCount="+ DataHelper.getAllListenCount());
            sConfiguration.putFieldTotalListenCount(DataHelper.getAllListenCount());
            dirty = true;
        }

        if ((paidVersion != null && paidVersion)
                || (firebasePaidVersion != null && firebasePaidVersion))
        {
            sConfiguration.putFieldPaidVersion(true);
            dirty = true;
        }
        if ((purchaseAccess != null && purchaseAccess)
                || (firebasePurchaseAccess != null && firebasePurchaseAccess)
                || (paidVersion != null && paidVersion)
                || (firebasePaidVersion != null && firebasePaidVersion))
        {
            sConfiguration.putFieldPurchaseAccess(true);
            dirty = true;
        }
        if ((purchaseNoads != null && purchaseNoads)
                || (firebasePurchaseNoads != null && firebasePurchaseNoads)
                || (paidVersion != null && paidVersion)
                || (firebasePaidVersion != null && firebasePaidVersion))
        {
            sConfiguration.putFieldPurchaseNoads(true);
            dirty = true;
        }

        //#IFDEF 'TRIAL'
        boolean trialMode = (paidVersion != null && paidVersion) || (purchaseAccess != null && purchaseAccess) || DataHelper.isTrial();
        Context context = RadioTheaterApplication.getRadioTheaterApplicationContext();
        RadioTheaterWidgetService.setPaidVersion(context, trialMode);
        //#ENDIF

        return dirty;
    }

    // update Firebase User Account info
    public static void updateFirebaseConfigurationValues(String deviceId, ContentValues configurationValues) {
        LogHelper.v(TAG, "updateFirebaseConfigurationValues");
        if (getDatabase() == null) {
            initializeFirebase();
        }
        String  email = getEmail();
        Boolean authenticated = email != null;
        Long total_listen_count = configurationValues.getAsLong(ConfigurationColumns.FIELD_TOTAL_LISTEN_COUNT);

        if (total_listen_count < DataHelper.getAllListenCount()) {
            total_listen_count = Long.valueOf(DataHelper.getAllListenCount());
        }
        else {
            DataHelper.setAllListenCount(total_listen_count.intValue());
        }

        //#IFDEF 'PAID'
        //Boolean paid_version = true;
        //Boolean purchase_access = true;
        //Boolean purchase_noads = true;
        //#ENDIF

        //#IFDEF 'TRIAL'
        Boolean paid_version = configurationValues.getAsBoolean(ConfigurationColumns.FIELD_PAID_VERSION);
        Boolean purchase_access = configurationValues.getAsBoolean(ConfigurationColumns.FIELD_PURCHASE_ACCESS);
        Boolean purchase_noads = configurationValues.getAsBoolean(ConfigurationColumns.FIELD_PURCHASE_NOADS);
        //#ENDIF

        FirebaseConfiguration firebaseConfiguration = new FirebaseConfiguration(
                email,
                getUID(),
                deviceId,
                authenticated,
                paid_version,
                purchase_access,
                purchase_noads,
                total_listen_count
        );
        firebaseConfiguration.commit(getDatabase(), deviceId);
    }

    // update Firebase User Episode History and Auth
    public static void updateFirebaseConfigEntryValues(String episode_number, ContentValues configEntryValues, long duration) {
        LogHelper.v(TAG, "updateFirebaseConfigEntryValues");
        if (getDatabase() == null) {
            initializeFirebase();
        }
        String  email = getEmail();
        Boolean episode_downloaded = configEntryValues.getAsBoolean(ConfigEpisodesColumns.FIELD_EPISODE_DOWNLOADED);
        Boolean episode_heard = configEntryValues.getAsBoolean(ConfigEpisodesColumns.FIELD_EPISODE_HEARD);
        Long    episode_count = configEntryValues.getAsLong(ConfigEpisodesColumns.FIELD_LISTEN_COUNT);

        //#IFDEF 'PAID'
        //Boolean purchased_access = true;
        //Boolean purchased_noads = true;
        //#ENDIF

        //#IFDEF 'TRIAL'
        Boolean purchased_access = configEntryValues.getAsBoolean(ConfigEpisodesColumns.FIELD_PURCHASED_ACCESS);
        Boolean purchased_noads = configEntryValues.getAsBoolean(ConfigEpisodesColumns.FIELD_PURCHASED_NOADS);
        //#ENDIF

        FirebaseConfigEpisode firebaseConfigEpisode = new FirebaseConfigEpisode(
                email,
                episode_number,
                purchased_access,
                purchased_noads,
                episode_downloaded,
                episode_heard,
                episode_count,
                duration
        );
        firebaseConfigEpisode.commit(getDatabase(), getUID());
    }

    /*
    private static void deleteFirebaseDatabase() {
        LogHelper.v(TAG, "*** deleteFirebaseDatabase ***");
        sFirebase.child("radiomysterytheater/0").removeValue();
        sFirebase.child("radiomysterytheater/1").removeValue();
        sFirebase.child("radiomysterytheater/2").removeValue();
    }
    */

    public static void markEpisodeAsHeardAndIncrementPlayCount(long episodeNumber, String episodeIndex, long duration) {
        LogHelper.v(TAG, "-------------------------------------------------------------------------------- AllListenCount="+ DataHelper.getAllListenCount());
        LogHelper.v(TAG, "markEpisodeAsHeardAndIncrementPlayCount: episodeNumber="+episodeNumber+", episodeIndex="+episodeIndex+", duration="+duration);
        if (episodeNumber == 0) {
            LogHelper.w(TAG, "unable to markEpisodeAsHeardAndIncrementPlayCount - episodeNumber is zero!");
            return;
        }
        boolean matchError = false;
        if (! String.valueOf(episodeNumber).equals(episodeIndex)) {
            LogHelper.e(TAG, "markEpisodeAsHeardAndIncrementPlayCount: The episodeNumber="+episodeNumber+" and episodeIndex "+episodeIndex+" DONT MATCH");
            matchError = true;
        }

        // UPDATE SQLITE - mark SQLite config episode as "HEARD" and increment "PLAY COUNT" - Also send record to Firebase
        ConfigEpisodesContentValues existingEpisodeConfig = DataHelper.getConfigEpisodeForEpisode(episodeIndex);
        long episodeListenCount = 1;
        if (existingEpisodeConfig != null && existingEpisodeConfig.values() != null && existingEpisodeConfig.values().size() != 0) {
            ContentValues configEpisode = existingEpisodeConfig.values();
            configEpisode.put(RadioTheaterContract.ConfigEpisodesEntry.FIELD_EPISODE_HEARD, true);

            episodeListenCount = configEpisode.getAsLong(ConfigEpisodesColumns.FIELD_LISTEN_COUNT);
            ++episodeListenCount;

            configEpisode.put(RadioTheaterContract.ConfigEpisodesEntry.FIELD_LISTEN_COUNT, episodeListenCount);
            DataHelper.updateConfigEpisodesEntry(episodeIndex, configEpisode);

            LogHelper.v(TAG, "markEpisodeAsHeardAndIncrementPlayCount: *** UPDATE FIREBASE CONFIG ENTRY FOR EPISODE "+episodeNumber+" ***");
            updateFirebaseConfigEntryValues(episodeIndex, configEpisode, duration);
            LogHelper.d(TAG, "markEpisodeAsHeardAndIncrementPlayCount: new EPISODE-LISTEN-COUNT=" + episodeListenCount + " for #" + episodeIndex + (matchError ? " *** MATCH ERROR EPISODE=" + episodeNumber : ""));
        }
        else {
            LogHelper.e(TAG, "markEpisodeAsHeardAndIncrementPlayCount: *** SQLITE: - MARK HEARD FAILED - unable to getConfigEpisodeForEpisode="+episodeIndex+(matchError ? " *** MATCH ERROR EPISODE="+episodeNumber : ""));
        }

        // UPDATE SQLITE - mark SQLite configuration as "HEARD" and increment "PLAY COUNT"
        ConfigurationCursor configurationCursor = DataHelper.getCursorForConfigurationDevice(getAdvertId());
        ConfigurationContentValues existingConfiguration = null;
        if (configurationCursor != null) {
            existingConfiguration = DataHelper.getConfigurationContentValues(configurationCursor);
            configurationCursor.close();
        }
        long total_listen_count = 1;
        ContentValues configurationValues = null;
        if (existingConfiguration != null && existingConfiguration.values() != null && existingConfiguration.values().size() != 0) {
            configurationValues = existingConfiguration.values();

            total_listen_count = configurationValues.getAsLong(ConfigurationColumns.FIELD_TOTAL_LISTEN_COUNT);
            ++total_listen_count;

            if (total_listen_count < DataHelper.getAllListenCount()) {
                total_listen_count = DataHelper.getAllListenCount();
            } else {
                DataHelper.setAllListenCount((int) total_listen_count);
            }

            configurationValues.put(RadioTheaterContract.ConfigurationEntry.FIELD_TOTAL_LISTEN_COUNT, Long.valueOf(DataHelper.getAllListenCount()));
            DataHelper.updateConfiguration(getAdvertId(), configurationValues);
        }
        else {
            LogHelper.e(TAG, "markEpisodeAsHeardAndIncrementPlayCount: *** SQLITE: - INCREMENT LISTEN COUNT - no getConfigurationDevice SQL record, email="+getEmail()+", deviceId="+getAdvertId());
            if (sConfiguration == null) {
                LogHelper.w(TAG, "*** THE GLOBAL CONFIGURATION IS NULL! - INITIALIZING IT NOW. ***");

                //#IFDEF 'PAID'
                //sConfiguration = createConfiguration(true, true, true);
                //#ENDIF

                //#IFDEF 'TRIAL'
                sConfiguration = createConfiguration(false, false, false);
                //#ENDIF

            }
            configurationValues = sConfiguration.values();
            DataHelper.setAllListenCount(DataHelper.getAllListenCount() + 1);
            configurationValues.put(RadioTheaterContract.ConfigurationEntry.FIELD_TOTAL_LISTEN_COUNT, DataHelper.getAllListenCount());
            try {
                LogHelper.w(TAG, "markEpisodeAsHeardAndIncrementPlayCount: *** SQLITE: MISSING - TRY UPDATE THE LOCAL DEVICE ENTRY ***");
                DataHelper.updateConfiguration(getAdvertId(), configurationValues);
            }
            catch (Exception e1) {
                LogHelper.w(TAG, "markEpisodeAsHeardAndIncrementPlayCount: *** SQLITE: MISSING - UPDATE for SQLITE Configuration failed - try INSERT instead *** - e1="+e1.getMessage());
                try {
                    LogHelper.w(TAG, "markEpisodeAsHeardAndIncrementPlayCount: *** SQLITE: MISSING - CREATE THE LOCAL DEVICE ENTRY ***");
                    DataHelper.insertConfiguration(configurationValues);
                }
                catch (Exception e2) {
                    LogHelper.e(TAG, "markEpisodeAsHeardAndIncrementPlayCount: *** SQLITE: MISSING - UNABLE TO INSERT OR UPDATE CONFIGURATION *** - e2="+e2.getMessage());
                }
            }
        }

        if (configurationValues != null && sConfiguration != null && sConfiguration.values() != null) {
            LogHelper.v(TAG, "markEpisodeAsHeardAndIncrementPlayCount: *** FIREBASE: merge and update local SQLite and Firebase - AllListenCount="+ DataHelper.getAllListenCount());
            boolean dirty = mergeConfiguratons(configurationValues, sConfiguration.values());
            if (dirty) {
                updateTheConfiguration();
            }
        }

        // send Analytics record to Firebase for Episode+Heard+Count
        trackWithFirebaseAnalytics(episodeIndex, duration, "mark HEARD + playcount="+ episodeListenCount +" + allcount="+ total_listen_count);
        LogHelper.v(TAG, "markEpisodeAsHeardAndIncrementPlayCount: FINISH");
        LogHelper.v(TAG, "-------------------------------------------------------------------------------- AllListenCount="+ DataHelper.getAllListenCount());
    }

    public static void markEpisodeAs_NOT_Heard(long episodeNumber, String episodeIndex, long duration) {
        LogHelper.v(TAG, "markEpisodeAs_NOT_Heard: episodeNumber="+episodeNumber+", episodeIndex="+episodeIndex+", duration="+duration);
        if (episodeNumber == 0) {
            LogHelper.w(TAG, "unable to markEpisodeAs_NOT_Heard - episodeNumber is zero!");
            return;
        }
        boolean matchError = false;
        if (! String.valueOf(episodeNumber).equals(episodeIndex)) {
            LogHelper.e(TAG, "markEpisodeAs_NOT_Heard: The episodeNumber="+episodeNumber+" and episodeIndex "+episodeIndex+" DONT MATCH");
            matchError = true;
        }

        // UPDATE SQLITE - mark SQLite config episode as "NOT HEARD" - Also send record to Firebase
        ConfigEpisodesContentValues existing = DataHelper.getConfigEpisodeForEpisode(episodeIndex);
        if (existing != null && existing.values() != null && existing.values().size() != 0) {
            ContentValues configEpisode = existing.values();
            configEpisode.put(RadioTheaterContract.ConfigEpisodesEntry.FIELD_EPISODE_HEARD, false);
            DataHelper.updateConfigEpisodesEntry(episodeIndex, configEpisode);
            updateFirebaseConfigEntryValues(episodeIndex, configEpisode, duration);
            LogHelper.d(TAG, "markEpisodeAs_NOT_Heard: for Episode "+episodeIndex+(matchError ? " *** MATCH ERROR EPISODE="+episodeNumber : ""));
        }
        else {
            LogHelper.e(TAG, "*** SQLITE: - unable to getConfigEpisodeForEpisode="+episodeIndex+(matchError ? " *** MATCH ERROR EPISODE="+episodeNumber : ""));
        }

        // send Analytics record to Firebase for Episode+Heard+Count
        trackWithFirebaseAnalytics(episodeIndex, duration, "mark NOT HEARD");
    }

    public static void trackWithFirebaseAnalytics(String episodeIndex, long duration, String comment) {
        if (sFirebaseAnalytics != null && episodeIndex != null && comment != null) {
            LogHelper.v(TAG, "ANALYTICS: trackWithFirebaseAnalytics: episode="+episodeIndex+", duration="+duration+", comment="+comment);
            if (Long.valueOf(episodeIndex) == 0) {
                LogHelper.w(TAG, "unable to logToFirebase - episodeIndex is zero!");
                return;
            }
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, episodeIndex);
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, DataHelper.getEpisodeTitle());
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "audio");
            bundle.putString("episode", episodeIndex);

            //#IFDEF 'PAID'
            //bundle.putString("user_action", "PAID: "+comment);
            //#ENDIF

            //#IFDEF 'TRIAL'
            bundle.putString("user_action", "TRIAL: "+comment);
            //#ENDIF

            bundle.putLong("listen_duration", duration);
            sFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            logToFirebase(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        }
    }

    public static void trackWithFirebaseAnalytics(String event, String email, String comment) {
        if (sFirebaseAnalytics != null && event != null && email != null && comment != null) {
            LogHelper.v(TAG, "ANALYTICS: trackWithFirebaseAnalytics: event="+event+", email="+email);
            if (getEmail() == null) {
                LogHelper.w(TAG, "unable to logToFirebase - Firebase user is not logged in!");
                return;
            }
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "event");
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, event);
            bundle.putString(FirebaseAnalytics.Param.ORIGIN, email);

            //#IFDEF 'PAID'
            //bundle.putString("user_action", "PAID: "+comment);
            //#ENDIF

            //#IFDEF 'TRIAL'
            bundle.putString("user_action", "TRIAL: "+comment);
            //#ENDIF

            sFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            logToFirebase(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        }
    }

    public static void trackSignupAttemptWithFirebaseAnalytics(String signup_using) {
        if (sFirebaseAnalytics != null) {
            LogHelper.v(TAG, "ANALYTICS: trackSignupAttemptWithFirebaseAnalytics signup_using="+signup_using);
            Bundle bundle = new Bundle();
            bundle.putString("using", signup_using);
            sFirebaseAnalytics.logEvent("signup_method", bundle);
        }
    }

    public static void trackSignupWithFirebaseAnalytics() {
        if (sFirebaseAnalytics != null) {
            LogHelper.v(TAG, "ANALYTICS: trackSignupWithFirebaseAnalytics email="+getEmail());
            Bundle bundle = new Bundle();
            bundle.putString("user", getEmail());
            sFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, bundle);
            logToFirebase(FirebaseAnalytics.Event.SIGN_UP, bundle);
        }
    }

    public static void trackLoginWithFirebaseAnalytics() {
        if (sFirebaseAnalytics != null) {
            LogHelper.v(TAG, "ANALYTICS: trackLoginWithFirebaseAnalytics email="+getEmail());
            Bundle bundle = new Bundle();
            bundle.putString("user", getEmail());
            sFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle);
            logToFirebase(FirebaseAnalytics.Event.LOGIN, bundle);
        }
    }

    public static void trackSearchWithFirebaseAnalytics() {
        if (sFirebaseAnalytics != null) {
            LogHelper.v(TAG, "ANALYTICS: trackSearchWithFirebaseAnalytics email="+getEmail());
            Bundle bundle = new Bundle();
            bundle.putString("user", getEmail());
            sFirebaseAnalytics.logEvent("do_search", bundle);
            logToFirebase("do_search", bundle);
        }
    }

    public static void trackSettingsWithFirebaseAnalytics() {
        if (sFirebaseAnalytics != null) {
            LogHelper.v(TAG, "ANALYTICS: trackSettingsWithFirebaseAnalytics email="+getEmail());
            Bundle bundle = new Bundle();
            bundle.putString("user", getEmail());
            sFirebaseAnalytics.logEvent("do_settings", bundle);
            logToFirebase("do_settings", bundle);
        }
    }

    public static void trackAboutWithFirebaseAnalytics() {
        if (sFirebaseAnalytics != null) {
            LogHelper.v(TAG, "ANALYTICS: trackAboutWithFirebaseAnalytics email="+getEmail());
            Bundle bundle = new Bundle();
            bundle.putString("user", getEmail());
            sFirebaseAnalytics.logEvent("do_about", bundle);
            logToFirebase("do_about", bundle);
        }
    }

    public static void logToFirebase(String action, Bundle bundle) {
        if (getEmail() == null) { // ensure the Firebase user is logged-in
            LogHelper.w(TAG, "unable to logToFirebase - Firebase user is not logged in!");
            return;
        }
        String detail = "";
        if (bundle != null) {
            detail = "{";
            for (String key : bundle.keySet()) {
                detail += " " + key + " => " + bundle.get(key) + ";";
            }
            detail += " }";
        }
        String logValue = action + " " + detail;
        LogHelper.v(TAG, "ANALYTICS: logToFirebase - logValue="+logValue);
        final String key = date_key();
        if (getDatabase() != null && key != null) {
            LogHelper.v(TAG, "commit: key=" + key);
            getDatabase().child("log").child(getUID()).child(key).setValue(logValue, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        LogHelper.v(TAG, "logToFirebase: onComplete - databaseError=" + databaseError.getMessage());
                    }
                    if (databaseReference != null) {
                        LogHelper.v(TAG, "logToFirebase: onComplete - databaseReference key=" + databaseReference.getKey());
                    }
                    if (databaseError == null && databaseReference != null) {
                        LogHelper.v(TAG, "logToFirebase: key="+key+" - SUCCESS!");
                    }
                }
            });
        }
    }

    public static String date_key() {
        return new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault()).format(new java.util.Date());
    }
}

