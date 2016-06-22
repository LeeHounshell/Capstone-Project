package com.harlie.radiotheater.radiomysterytheater.data_helper;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.harlie.radiotheater.radiomysterytheater.BaseActivity;
import com.harlie.radiotheater.radiomysterytheater.CircleViewHelper;
import com.harlie.radiotheater.radiomysterytheater.RadioTheaterApplication;
import com.harlie.radiotheater.radiomysterytheater.data.actors.ActorsColumns;
import com.harlie.radiotheater.radiomysterytheater.data.actors.ActorsCursor;
import com.harlie.radiotheater.radiomysterytheater.data.actors.ActorsSelection;
import com.harlie.radiotheater.radiomysterytheater.data.writers.WritersColumns;
import com.harlie.radiotheater.radiomysterytheater.data.writers.WritersCursor;
import com.harlie.radiotheater.radiomysterytheater.data.writers.WritersSelection;
import com.harlie.radiotheater.radiomysterytheater.utils.LogHelper;

import java.util.Iterator;
import java.util.List;

import at.grabner.circleprogress.CircleProgressView;

// NOTE: this AsyncTask should run 3 times: WRITERS, ACTORS, EPISODES
public class LoadRadioTheaterTablesAsyncTask extends AsyncTask<BaseActivity, Void, Boolean> {
    private final static String TAG = "LEE: <" + LoadRadioTheaterTablesAsyncTask.class.getSimpleName() + ">";

    private static final int TOTAL_NUMBER_OF_EPISODES_ACTORS_WRITERS = 1399 + 310 + 36;

    private static boolean mTesting = false;
    private static int mCount;

    public enum LoadState {
        WRITERS, ACTORS, EPISODES
    }
    private volatile LoadState mState = LoadState.WRITERS;

    private BaseActivity mActivity;
    private CircleProgressView mCircleProgressView;
    private DataSnapshot mDataSnapshot;

    public LoadRadioTheaterTablesAsyncTask(BaseActivity activity, CircleProgressView circleProgressView, DataSnapshot dataSnapshot, LoadState state) {
        LogHelper.v(TAG, "new LoadRadioTheaterTablesAsyncTask");
        this.mActivity = activity;
        this.mCircleProgressView = circleProgressView;
        this.mDataSnapshot = dataSnapshot;
        this.mState = state;
        if (mState == LoadState.WRITERS) { // writers are first up..
            mCount = 0;
            CircleViewHelper.showCircleView(mActivity, mCircleProgressView, CircleViewHelper.CircleViewType.CREATE_DATABASE);
            CircleViewHelper.setCircleViewMaximum((float) TOTAL_NUMBER_OF_EPISODES_ACTORS_WRITERS, mActivity);
            CircleViewHelper.setCircleViewValue((float) mCount, mActivity);
        }
    }

    @Override
    protected void onPreExecute() {
        LogHelper.v(TAG, "onPreExecute");
        super.onPreExecute();
        mCircleProgressView.setVisibility(View.VISIBLE);
    }

    @Override
    protected Boolean doInBackground(BaseActivity... params) {
        LogHelper.v(TAG, "doInBackground");
        Boolean rc = false;
        if (mState == LoadState.WRITERS) {
            LogHelper.v(TAG, "LOADING WRITERS..");
            rc = loadWriters();
        }
        else if (mState == LoadState.ACTORS) {
            LogHelper.v(TAG, "LOADING ACTORS..");
            rc = loadActors();
        }
        else if (mState == LoadState.EPISODES) {
            LogHelper.v(TAG, "LOADING EPISODES..");
            rc = loadEpisodes();
        }
        return rc;
    }

    private Boolean loadWriters() {
        LogHelper.v(TAG, "loadWriters");
        Object writerObject = mDataSnapshot.getValue();
        if (writerObject == null) {
            LogHelper.e(TAG, "the Writers SNAPSHOT IS null");
            return false;
        }
        String writerJSON = writerObject.toString();

        //#IFDEF 'DEBUG'
        System.out.println();
        System.out.println(writerJSON);
        System.out.println();
        if (isTesting()) {
            LogHelper.v(TAG, "LOADING TEST WRITER");
            ContentValues writerValues = new ContentValues();
            writerValues.put(RadioTheaterContract.WritersEntry.FIELD_WRITER_ID, 1);
            writerValues.put(RadioTheaterContract.WritersEntry.FIELD_WRITER_NAME, "Slesar, Henry");
            writerValues.put(RadioTheaterContract.WritersEntry.FIELD_WRITER_URL, "slesar_henry.jpg");
            try {
                Uri result = insertWriterValues(writerValues);
                LogHelper.v(TAG, "Test Writer insert result="+result);
                return true;
            }
            catch (Exception e) {
                LogHelper.e(TAG, "Test Writer insert exception="+e);
                return false;
            }
        }
        //#ENDIF

        // parse the JSON
        List<TheWriters> writers = TheWriters.arrayTheWritersFromData(writerJSON);
        if (writers != null) {
            LogHelper.v(TAG, "GOT WRITERS");
            Iterator iterator = writers.iterator();
            int i = 1;
            while (iterator.hasNext()) {
                TheWriters writer = (TheWriters) iterator.next();
                String name = writer.getName().replaceAll("^\"|\"$", "");
                String photo = writer.getPhoto().replaceAll("^\"|\"$", "");
                ContentValues writerValues = new ContentValues();
                writerValues.put(RadioTheaterContract.WritersEntry.FIELD_WRITER_ID, i);
                writerValues.put(RadioTheaterContract.WritersEntry.FIELD_WRITER_NAME, name);
                writerValues.put(RadioTheaterContract.WritersEntry.FIELD_WRITER_URL, photo);
                try {
                    Uri result = insertWriterValues(writerValues);
                    LogHelper.v(TAG, "Writer insert result="+result);
                }
                catch (Exception e) {
                    LogHelper.e(TAG, "Writer insert exception="+e);
                }
                ++i;
            }
        }
        else {
            LogHelper.e(TAG, "writers is null!");
            return false;
        }
        return true;
    }

    private Boolean loadActors() {
        LogHelper.v(TAG, "loadActors");
        Object actorObject = mDataSnapshot.getValue();
        if (actorObject == null) {
            LogHelper.e(TAG, "the Actors SNAPSHOT IS null");
            return false;
        }
        String actorJSON = actorObject.toString();

        //#IFDEF 'DEBUG'
        System.out.println();
        System.out.println(actorJSON);
        System.out.println();
        if (isTesting()) {
            LogHelper.v(TAG, "LOADING TEST ACTOR");
            ContentValues actorValues = new ContentValues();
            actorValues.put(RadioTheaterContract.ActorsEntry.FIELD_ACTOR_ID, 1);
            actorValues.put(RadioTheaterContract.ActorsEntry.FIELD_ACTOR_NAME, "DeKoven, Roger");
            actorValues.put(RadioTheaterContract.ActorsEntry.FIELD_ACTOR_URL, "dekoven_roger.jpg");
            try {
                Uri result = insertActorValues(actorValues);
                LogHelper.v(TAG, "Test Actor insert result="+result);
                return true;
            }
            catch (Exception e) {
                LogHelper.e(TAG, "Test Actor insert exception="+e);
                return false;
            }
        }
        //#ENDIF

        // parse the JSON
        List<TheActors> actors = TheActors.arrayTheActorsFromData(actorJSON);
        if (actors != null) {
            LogHelper.v(TAG, "GOT ACTORS");
            Iterator iterator = actors.iterator();
            int i = 1;
            while (iterator.hasNext()) {
                TheActors actor = (TheActors) iterator.next();
                String name = actor.getName().replaceAll("^\"|\"$", "");
                String photo = actor.getPhoto().replaceAll("^\"|\"$", "");
                ContentValues actorValues = new ContentValues();
                actorValues.put(RadioTheaterContract.ActorsEntry.FIELD_ACTOR_ID, i);
                actorValues.put(RadioTheaterContract.ActorsEntry.FIELD_ACTOR_NAME, name);
                actorValues.put(RadioTheaterContract.ActorsEntry.FIELD_ACTOR_URL, photo);
                try {
                    Uri result = insertActorValues(actorValues);
                    LogHelper.v(TAG, "Actor insert result="+result);
                }
                catch (Exception e) {
                    LogHelper.e(TAG, "Actor insert exception="+e);
                }
                ++i;
            }
        }
        else {
            LogHelper.e(TAG, "actors is null!");
            return false;
        }
        return true;
    }

    private Boolean loadEpisodes() {
        LogHelper.v(TAG, "loadEpisodes");
        Object episodeObject = mDataSnapshot.getValue();
        if (episodeObject == null) {
            LogHelper.e(TAG, "the Episodes SNAPSHOT IS null");
            return false;
        }
        String episodeJSON = episodeObject.toString();

        //#IFDEF 'DEBUG'
        System.out.println();
        System.out.println(episodeJSON);
        System.out.println();
        if (isTesting()) {
            LogHelper.v(TAG, "LOADING TEST EPISODE");
            ContentValues episodeValues = new ContentValues();
            episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_EPISODE_NUMBER, "0001");
            episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_AIRDATE, "1974-01-06");
            episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_EPISODE_TITLE, "The Old Ones Are Hard to Kill");
            episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_EPISODE_DESCRIPTION, "An old lady rents a room to a sick boarder. She runs into problems with his strange deathbed confession.");
            episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_DOWNLOAD_URL, "www.cbsrmt.com/mp3/CBS Radio Mystery Theater 74-01-06 e0001 The Old Ones Are Hard to Kill.mp3");
            episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_WEBLINK_URL, "www.cbsrmt.com/episode_name-1-the-old-ones-are-hard-to-kill.html");
            episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_RATING, 3.2);
            episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_VOTE_COUNT, 1); // true count is unknown at present
            try {
                Uri result = insertEpisodeValues(episodeValues);
                LogHelper.v(TAG, "Test Episode insert result="+result);
                return true;
            }
            catch (Exception e) {
                LogHelper.e(TAG, "Test Episode insert exception="+e);
                return false;
            }
        }
        //#ENDIF

        // parse the JSON
        List<TheEpisodes> episodes = TheEpisodes.arrayTheEpisodesFromData(episodeJSON);
        if (episodes != null) {
            LogHelper.v(TAG, "GOT EPISODES");
            Iterator iterator = episodes.iterator();
            int i = 0;
            while (iterator.hasNext()) {
                ++i;
                TheEpisodes episode = (TheEpisodes) iterator.next();
                String number = episode.getEpisode_number().replaceAll("^\"|\"$", "");
                String airdate = episode.getAirdate().replaceAll("^\"|\"$", "");
                String title = episode.getEpisode_name().replaceAll("^\"|\"$", "");
                String description = episode.getDescription().replaceAll("^\"|\"$", "");
                String weblink = episode.getWeblink().replaceAll("^\"|\"$", "");
                String download = episode.getDownload().replaceAll("^\"|\"$", "");
                String rating = episode.getRating().replaceAll("^\"|\"$", "");
                TheEpisodes.ActorsBean actors = episode.getActors();
                TheEpisodes.WritersBean writers = episode.getWriters();
                String vote_count = "1"; // true value is unknown
                ContentValues episodeValues = new ContentValues();
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_EPISODE_NUMBER, number);
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_AIRDATE, airdate);
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_EPISODE_TITLE, title);
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_EPISODE_DESCRIPTION, description);
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_WEBLINK_URL, weblink);
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_DOWNLOAD_URL, download);
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_RATING, rating);
                episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_VOTE_COUNT, vote_count);
                try {
                    Uri result = insertEpisodeValues(episodeValues);
                    LogHelper.v(TAG, "Episode insert result="+result);
                }
                catch (Exception e) {
                    LogHelper.e(TAG, "Episode insert exception="+e);
                }

                String writerName = episode.getWriters().getName().replaceAll("^\"|\"$", "");
                long writerRowId = getWriterIdForName(writerName);
                //String writerPhoto = episode.getWriters().getPhoto().replaceAll("^\"|\"$", "");

                // SQLite load WritersEpisodes Table
                ContentValues writersEpisodes = new ContentValues();
                writersEpisodes.put(RadioTheaterContract.EpisodesWritersEntry.FIELD_WRITER_ID, writerRowId);
                writersEpisodes.put(RadioTheaterContract.WritersEpisodesEntry.FIELD_WRITER_NAME, writerName);
                writersEpisodes.put(RadioTheaterContract.WritersEpisodesEntry.FIELD_EPISODE_NUMBER, number);
                try {
                    Uri result = insertWritersEpisodesValues(writersEpisodes);
                    LogHelper.v(TAG, "WritersEpisodes insert result="+result);
                }
                catch (Exception e) {
                    LogHelper.e(TAG, "WritersEpisodes insert exception="+e);
                }

                // SQLite load EpisodeWriters Table
                ContentValues episodeWriters = new ContentValues();
                episodeWriters.put(RadioTheaterContract.EpisodesWritersEntry.FIELD_EPISODE_NUMBER, number);
                episodeWriters.put(RadioTheaterContract.EpisodesWritersEntry.FIELD_WRITER_ID, writerRowId);
                episodeWriters.put(RadioTheaterContract.EpisodesWritersEntry.FIELD_WRITER_NAME, writerName);
                try {
                    Uri result = insertEpisodeWritersValues(episodeWriters);
                    LogHelper.v(TAG, "EpisodeWriters insert result="+result);
                }
                catch (Exception e) {
                    LogHelper.e(TAG, "EpisodeWriters insert exception="+e);
                }

                List<String> actorNames = episode.getActors().getName();
                List<String> actorPhotos = episode.getActors().getPhoto();

                for (int act = 0; act < actorNames.size(); ++act) {
                    String actorName = actorNames.get(act).replaceAll("^\"|\"$", "");
                    long actorRowId = getActorIdForName(actorName);
                    //String actroPhoto = actorPhotos.get(act).replaceAll("^\"|\"$", "");

                    // SQLite load ActorsEpisodes Table
                    ContentValues actorsEpisodes = new ContentValues();
                    actorsEpisodes.put(RadioTheaterContract.ActorsEpisodesEntry.FIELD_ACTOR_ID, actorRowId);
                    actorsEpisodes.put(RadioTheaterContract.ActorsEpisodesEntry.FIELD_ACTOR_NAME, actorName);
                    actorsEpisodes.put(RadioTheaterContract.ActorsEpisodesEntry.FIELD_EPISODE_NUMBER, number);
                    try {
                        Uri result = insertActorsEpisodesValues(actorsEpisodes);
                        LogHelper.v(TAG, "ActorsEpisodes insert result="+result);
                    }
                    catch (Exception e) {
                        LogHelper.e(TAG, "ActorsEpisodes insert exception="+e);
                    }

                    // SQLite load EpisodesActors Table
                    ContentValues episodeActors = new ContentValues();
                    episodeActors.put(RadioTheaterContract.EpisodesActorsEntry.FIELD_EPISODE_NUMBER, number);
                    episodeActors.put(RadioTheaterContract.EpisodesActorsEntry.FIELD_ACTOR_ID, actorRowId);
                    episodeActors.put(RadioTheaterContract.EpisodesActorsEntry.FIELD_ACTOR_NAME, actorName);
                    try {
                        Uri result = insertEpisodeActorsValues(episodeActors);
                        LogHelper.v(TAG, "EpisodeActors insert result="+result);
                    }
                    catch (Exception e) {
                        LogHelper.e(TAG, "EpisodeActors insert exception="+e);
                    }

                    // SQLite load ConfigEntry Table
                    ContentValues configEntry = new ContentValues();
                    configEntry.put(RadioTheaterContract.ConfigEpisodesEntry.FIELD_EPISODE_NUMBER, number);

                    //#IFDEF 'PAID'
                    //configEntry.put(RadioTheaterContract.ConfigEpisodesEntry.FIELD_PURCHASED_ACCESS, true);
                    //configEntry.put(RadioTheaterContract.ConfigEpisodesEntry.FIELD_PURCHASED_NOADS, true);
                    //#ENDIF

                    //#IFDEF 'FREE'
                    configEntry.put(RadioTheaterContract.ConfigEpisodesEntry.FIELD_PURCHASED_ACCESS, false); // FIXME: update from Firebase if present
                    configEntry.put(RadioTheaterContract.ConfigEpisodesEntry.FIELD_PURCHASED_NOADS, false);  // FIXME: update from Firebase if present
                    //#ENDIF

                    configEntry.put(RadioTheaterContract.ConfigEpisodesEntry.FIELD_EPISODE_HEARD, false);    // FIXME: update from Firebase if present
                    configEntry.put(RadioTheaterContract.ConfigEpisodesEntry.FIELD_LISTEN_COUNT, 0);         // FIXME: update from Firebase if present
                    configEntry.put(RadioTheaterContract.ConfigEpisodesEntry.FIELD_EPISODE_DOWNLOADED, false);
                    try {
                        Uri result = insertConfigEntryValues(configEntry);
                        LogHelper.v(TAG, "ConfigEntry insert result="+result);
                    }
                    catch (Exception e) {
                        LogHelper.e(TAG, "ConfigEntry insert exception="+e);
                    }
                }
            }
            LogHelper.v(TAG, "loaded "+i+" episodes.");
        }
        else {
            LogHelper.e(TAG, "episodes is null!");
            return false;
        }
        return true;
    }

    private long getActorIdForName(String actorName) {
        LogHelper.v(TAG, "getActorIdForName: "+actorName);
        ActorsSelection where = new ActorsSelection();
        where.fieldActorName(actorName);

        Cursor cursor = RadioTheaterApplication.getRadioTheaterApplicationContext().getContentResolver().query(
                ActorsColumns.CONTENT_URI,         // the 'content://' Uri to query
                null,                               // projection String[] - leaving "columns" null just returns all the columns.
                where.sel(),                        // selection - SQL where
                where.args(),                       // selection args String[] - values for the "where" clause
                null);                              // sort order and limit (String)

        long actor = 0;
        if (cursor != null && cursor.getCount() > 0) {
            ActorsCursor actorsCursor = new ActorsCursor(cursor);
            if (actorsCursor.moveToNext()) {
                actor = actorsCursor.getFieldActorId();
            }
            cursor.close();
        }
        return actor;
    }

    private long getWriterIdForName(String writerName) {
        LogHelper.v(TAG, "getWriterIdForName: "+writerName);
        WritersSelection where = new WritersSelection();
        where.fieldWriterName(writerName);

        Cursor cursor = RadioTheaterApplication.getRadioTheaterApplicationContext().getContentResolver().query(
                WritersColumns.CONTENT_URI,         // the 'content://' Uri to query
                null,                               // projection String[] - leaving "columns" null just returns all the columns.
                where.sel(),                        // selection - SQL where
                where.args(),                       // selection args String[] - values for the "where" clause
                null);                              // sort order and limit (String)

        long writer = 0;
        if (cursor != null && cursor.getCount() > 0) {
            WritersCursor writersCursor = new WritersCursor(cursor);
            if (writersCursor.moveToNext()) {
                writer = writersCursor.getFieldWriterId();
            }
            cursor.close();
        }
        return writer;
    }

    private Uri insertEpisodeValues(ContentValues episodeValues) {
        LogHelper.v(TAG, "insertEpisodeValues=");
        Uri episode = RadioTheaterContract.EpisodesEntry.buildEpisodesUri();
        CircleViewHelper.setCircleViewValue((float) ++mCount, mActivity);
        return mActivity.getContentResolver().insert(episode, episodeValues);
    }

    private Uri insertEpisodeActorsValues(ContentValues episodeActorsValues) {
        LogHelper.v(TAG, "insertEpisodeActorsValues=");
        Uri episodeActors = RadioTheaterContract.EpisodesActorsEntry.buildEpisodesActorsUri();
        return mActivity.getContentResolver().insert(episodeActors, episodeActorsValues);
    }

    private Uri insertEpisodeWritersValues(ContentValues episodeWritersValues) {
        LogHelper.v(TAG, "insertEpisodeWritersValues=");
        Uri episodeWriters = RadioTheaterContract.EpisodesWritersEntry.buildEpisodesWritersUri();
        return mActivity.getContentResolver().insert(episodeWriters, episodeWritersValues);
    }

    private Uri insertActorValues(ContentValues actorValues) {
        LogHelper.v(TAG, "insertActorValues");
        Uri actor = RadioTheaterContract.ActorsEntry.buildActorsUri();
        CircleViewHelper.setCircleViewValue((float) ++mCount, mActivity);
        return mActivity.getContentResolver().insert(actor, actorValues);
    }

    private Uri insertActorsEpisodesValues(ContentValues actorsEpisodesValues) {
        LogHelper.v(TAG, "insertActorsEpisodesValues");
        Uri actorsEpisodes = RadioTheaterContract.ActorsEpisodesEntry.buildActorsEpisodesUri();
        return mActivity.getContentResolver().insert(actorsEpisodes, actorsEpisodesValues);
    }

    private Uri insertWriterValues(ContentValues writerValues) {
        LogHelper.v(TAG, "insertWriterValues");
        Uri writer = RadioTheaterContract.WritersEntry.buildWritersUri();
        CircleViewHelper.setCircleViewValue((float) ++mCount, mActivity);
        return mActivity.getContentResolver().insert(writer, writerValues);
    }

    private Uri insertWritersEpisodesValues(ContentValues writersEpisodesValues) {
        LogHelper.v(TAG, "insertWritersEpisodesValues");
        Uri writersEpisodes = RadioTheaterContract.WritersEpisodesEntry.buildWritersEpisodesUri();
        return mActivity.getContentResolver().insert(writersEpisodes, writersEpisodesValues);
    }

    public Uri insertConfigurationValues(ContentValues configurationValues) {
        LogHelper.v(TAG, "insertConfigEntryValues");
        // FIXME: need to "update" Firebase record for this user's configuration
        Uri configuration = RadioTheaterContract.ConfigurationEntry.buildConfigurationUri();
        return mActivity.getContentResolver().insert(configuration, configurationValues);
    }

    public Uri insertConfigEntryValues(ContentValues configEntryValues) {
        LogHelper.v(TAG, "insertConfigEntryValues");
        // FIXME: need to "update" Firebase record for this episode and user
        Uri configEntry = RadioTheaterContract.ConfigEpisodesEntry.buildConfigEpisodesUri();
        return mActivity.getContentResolver().insert(configEntry, configEntryValues);
    }

    @Override
    protected void onPostExecute(Boolean successTablesLoaded) {
        LogHelper.v(TAG, "onPostExecute: successTablesLoaded="+successTablesLoaded);
        super.onPostExecute(successTablesLoaded);
        if (mState == LoadState.EPISODES) { // episodes are last. everything should be loaded.
            CircleViewHelper.hideCircleView(mActivity);
        }
        if (successTablesLoaded) {
            LogHelper.v(TAG, "---> SQL TABLES loaded ok.");
            mActivity.runLoadStateCallback(mState);
        } else {
            LogHelper.v(TAG, "---> SQL TABLES failed to load.");
            mActivity.startAuthenticationActivity();
        }
    }

    public static boolean isTesting() {
        return mTesting;
    }

    public static void setTesting(boolean testing) {
        mTesting = testing;
    }

}
