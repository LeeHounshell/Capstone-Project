package com.harlie.radiotheater.radiomysterytheater.data_helper;

import android.content.ContentValues;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.harlie.radiotheater.radiomysterytheater.BaseActivity;
import com.harlie.radiotheater.radiomysterytheater.CircleViewHelper;

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
        Log.v(TAG, "new LoadRadioTheaterTablesAsyncTask");
        this.mActivity = activity;
        this.mCircleProgressView = circleProgressView;
        this.mDataSnapshot = dataSnapshot;
        this.mState = state;
        if (mState == LoadState.WRITERS) { // writers are first up..
            mCount = 0;
            CircleViewHelper.showCircleView(mActivity);
            CircleViewHelper.initializeCircleViewValue((float) TOTAL_NUMBER_OF_EPISODES_ACTORS_WRITERS, mActivity);
        }
    }

    @Override
    protected void onPreExecute() {
        Log.v(TAG, "onPreExecute");
        super.onPreExecute();
        mCircleProgressView.setVisibility(View.VISIBLE);
    }

    @Override
    protected Boolean doInBackground(BaseActivity... params) {
        Log.v(TAG, "doInBackground");
        Boolean rc = false;
        if (mState == LoadState.WRITERS) {
            Log.v(TAG, "LOADING WRITERS..");
            rc = loadWriters();
        }
        else if (mState == LoadState.ACTORS) {
            Log.v(TAG, "LOADING ACTORS..");
            rc = loadActors();
        }
        else if (mState == LoadState.EPISODES) {
            Log.v(TAG, "LOADING EPISODES..");
            rc = loadEpisodes();
        }
        return rc;
    }

    private Boolean loadWriters() {
        Log.v(TAG, "loadWriters");
        Object writerObject = mDataSnapshot.getValue();
        if (writerObject == null) {
            Log.e(TAG, "the Writers SNAPSHOT IS null");
            return false;
        }
        String writerJSON = writerObject.toString();

        //#IFDEF 'DEBUG'
        System.out.println();
        System.out.println(writerJSON);
        System.out.println();
        if (isTesting()) {
            Log.v(TAG, "LOADING TEST WRITER");
            ContentValues writerValues = new ContentValues();
            writerValues.put(RadioTheaterContract.WritersEntry.FIELD_WRITER_ID, 1);
            writerValues.put(RadioTheaterContract.WritersEntry.FIELD_WRITER_NAME, "Slesar, Henry");
            writerValues.put(RadioTheaterContract.WritersEntry.FIELD_WRITER_URL, "slesar_henry.jpg");
            try {
                Uri result = insertWriterValues(writerValues);
                Log.v(TAG, "Test Writer insert result="+result);
                return true;
            }
            catch (Exception e) {
                Log.e(TAG, "Test Writer insert exception="+e);
                return false;
            }
        }
        //#ENDIF

        // parse the JSON
        List<TheWriters> writers = TheWriters.arrayTheWritersFromData(writerJSON);
        if (writers != null) {
            Log.v(TAG, "GOT WRITERS");
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
                    Log.v(TAG, "Writer insert result="+result);
                }
                catch (Exception e) {
                    Log.e(TAG, "Writer insert exception="+e);
                }
                ++i;
            }
        }
        else {
            Log.e(TAG, "writers is null!");
            return false;
        }
        return true;
    }

    private Boolean loadActors() {
        Log.v(TAG, "loadActors");
        Object actorObject = mDataSnapshot.getValue();
        if (actorObject == null) {
            Log.e(TAG, "the Actors SNAPSHOT IS null");
            return false;
        }
        String actorJSON = actorObject.toString();

        //#IFDEF 'DEBUG'
        System.out.println();
        System.out.println(actorJSON);
        System.out.println();
        if (isTesting()) {
            Log.v(TAG, "LOADING TEST ACTOR");
            ContentValues actorValues = new ContentValues();
            actorValues.put(RadioTheaterContract.ActorsEntry.FIELD_ACTOR_ID, 1);
            actorValues.put(RadioTheaterContract.ActorsEntry.FIELD_ACTOR_NAME, "DeKoven, Roger");
            actorValues.put(RadioTheaterContract.ActorsEntry.FIELD_ACTOR_URL, "dekoven_roger.jpg");
            try {
                Uri result = insertActorValues(actorValues);
                Log.v(TAG, "Test Actor insert result="+result);
                return true;
            }
            catch (Exception e) {
                Log.e(TAG, "Test Actor insert exception="+e);
                return false;
            }
        }
        //#ENDIF

        // parse the JSON
        List<TheActors> actors = TheActors.arrayTheActorsFromData(actorJSON);
        if (actors != null) {
            Log.v(TAG, "GOT ACTORS");
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
                    Log.v(TAG, "Actor insert result="+result);
                }
                catch (Exception e) {
                    Log.e(TAG, "Actor insert exception="+e);
                }
                ++i;
            }
        }
        else {
            Log.e(TAG, "actors is null!");
            return false;
        }
        return true;
    }

    private Boolean loadEpisodes() {
        Log.v(TAG, "loadEpisodes");
        Object episodeObject = mDataSnapshot.getValue();
        if (episodeObject == null) {
            Log.e(TAG, "the Episodes SNAPSHOT IS null");
            return false;
        }
        String episodeJSON = episodeObject.toString();

        //#IFDEF 'DEBUG'
        System.out.println();
        System.out.println(episodeJSON);
        System.out.println();
        if (isTesting()) {
            Log.v(TAG, "LOADING TEST EPISODE");
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
                Log.v(TAG, "Test Episode insert result="+result);
                return true;
            }
            catch (Exception e) {
                Log.e(TAG, "Test Episode insert exception="+e);
                return false;
            }
        }
        //#ENDIF

        // parse the JSON
        List<TheEpisodes> episodes = TheEpisodes.arrayTheEpisodesFromData(episodeJSON);
        if (episodes != null) {
            Log.v(TAG, "GOT EPISODES");
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
                    Log.v(TAG, "Episode insert result="+result);
                }
                catch (Exception e) {
                    Log.e(TAG, "Episode insert exception="+e);
                }

                String writerName = episode.getWriters().getName().replaceAll("^\"|\"$", "");
                //String writerPhoto = episode.getWriters().getPhoto().replaceAll("^\"|\"$", "");

                // SQLite load WritersEpisodes Table
                ContentValues writersEpisodes = new ContentValues();
                writersEpisodes.put(RadioTheaterContract.WritersEpisodesEntry.FIELD_WRITER_ID, writerName);
                writersEpisodes.put(RadioTheaterContract.WritersEpisodesEntry.FIELD_EPISODE_NUMBER, number);
                try {
                    Uri result = insertWritersEpisodesValues(writersEpisodes);
                    Log.v(TAG, "WritersEpisodes insert result="+result);
                }
                catch (Exception e) {
                    Log.e(TAG, "WritersEpisodes insert exception="+e);
                }

                // SQLite load EpisodeWriters Table
                ContentValues episodeWriters = new ContentValues();
                episodeWriters.put(RadioTheaterContract.EpisodesWritersEntry.FIELD_EPISODE_NUMBER, number);
                episodeWriters.put(RadioTheaterContract.EpisodesWritersEntry.FIELD_WRITER_ID, writerName);
                try {
                    Uri result = insertEpisodeWritersValues(episodeWriters);
                    Log.v(TAG, "EpisodeWriters insert result="+result);
                }
                catch (Exception e) {
                    Log.e(TAG, "EpisodeWriters insert exception="+e);
                }

                List<String> actorNames = episode.getActors().getName();
                List<String> actorPhotos = episode.getActors().getPhoto();

                for (int act = 0; act < actorNames.size(); ++act) {
                    String actorName = actorNames.get(act).replaceAll("^\"|\"$", "");
                    //String actroPhoto = actorPhotos.get(act).replaceAll("^\"|\"$", "");

                    // SQLite load ActorsEpisodes Table
                    ContentValues actorsEpisodes = new ContentValues();
                    actorsEpisodes.put(RadioTheaterContract.ActorsEpisodesEntry.FIELD_ACTOR_ID, actorName);
                    actorsEpisodes.put(RadioTheaterContract.ActorsEpisodesEntry.FIELD_EPISODE_NUMBER, number);
                    try {
                        Uri result = insertActorsEpisodesValues(actorsEpisodes);
                        Log.v(TAG, "ActorsEpisodes insert result="+result);
                    }
                    catch (Exception e) {
                        Log.e(TAG, "ActorsEpisodes insert exception="+e);
                    }

                    // SQLite load EpisodesActors Table
                    ContentValues episodeActors = new ContentValues();
                    episodeActors.put(RadioTheaterContract.EpisodesActorsEntry.FIELD_EPISODE_NUMBER, number);
                    episodeActors.put(RadioTheaterContract.EpisodesActorsEntry.FIELD_ACTOR_ID, actorName);
                    try {
                        Uri result = insertEpisodeActorsValues(episodeActors);
                        Log.v(TAG, "EpisodeActors insert result="+result);
                    }
                    catch (Exception e) {
                        Log.e(TAG, "EpisodeActors insert exception="+e);
                    }
                }
            }
            Log.v(TAG, "loaded "+i+" episodes.");
        }
        else {
            Log.e(TAG, "episodes is null!");
            return false;
        }
        return true;
    }

    private Uri insertEpisodeValues(ContentValues episodeValues) {
        Log.v(TAG, "insertEpisodeValues=");
        Uri episode = RadioTheaterContract.EpisodesEntry.buildEpisodesUri();
        CircleViewHelper.setCircleViewValue((float) ++mCount, mActivity);
        return mActivity.getContentResolver().insert(episode, episodeValues);
    }

    private Uri insertEpisodeActorsValues(ContentValues episodeActorsValues) {
        Log.v(TAG, "insertEpisodeActorsValues=");
        Uri episodeActors = RadioTheaterContract.EpisodesActorsEntry.buildEpisodesActorsUri();
        return mActivity.getContentResolver().insert(episodeActors, episodeActorsValues);
    }

    private Uri insertEpisodeWritersValues(ContentValues episodeWritersValues) {
        Log.v(TAG, "insertEpisodeWritersValues=");
        Uri episodeWriters = RadioTheaterContract.EpisodesWritersEntry.buildEpisodesWritersUri();
        return mActivity.getContentResolver().insert(episodeWriters, episodeWritersValues);
    }

    private Uri insertActorValues(ContentValues actorValues) {
        Log.v(TAG, "insertActorValues");
        Uri actor = RadioTheaterContract.ActorsEntry.buildActorsUri();
        CircleViewHelper.setCircleViewValue((float) ++mCount, mActivity);
        return mActivity.getContentResolver().insert(actor, actorValues);
    }

    private Uri insertActorsEpisodesValues(ContentValues actorsEpisodesValues) {
        Log.v(TAG, "insertActorsEpisodesValues");
        Uri actorsEpisodes = RadioTheaterContract.ActorsEpisodesEntry.buildActorsEpisodesUri();
        return mActivity.getContentResolver().insert(actorsEpisodes, actorsEpisodesValues);
    }

    private Uri insertWriterValues(ContentValues writerValues) {
        Log.v(TAG, "insertWriterValues");
        Uri writer = RadioTheaterContract.WritersEntry.buildWritersUri();
        CircleViewHelper.setCircleViewValue((float) ++mCount, mActivity);
        return mActivity.getContentResolver().insert(writer, writerValues);
    }

    private Uri insertWritersEpisodesValues(ContentValues writersEpisodesValues) {
        Log.v(TAG, "insertWritersEpisodesValues");
        Uri writersEpisodes = RadioTheaterContract.WritersEpisodesEntry.buildWritersEpisodesUri();
        return mActivity.getContentResolver().insert(writersEpisodes, writersEpisodesValues);
    }

    @Override
    protected void onPostExecute(Boolean successTablesLoaded) {
        Log.v(TAG, "onPostExecute: successTablesLoaded="+successTablesLoaded);
        super.onPostExecute(successTablesLoaded);
        if (mState == LoadState.EPISODES) { // episodes are last. everything should be loaded.
            CircleViewHelper.hideCircleView(mActivity);
        }
        if (successTablesLoaded) {
            Log.v(TAG, "---> SQL TABLES loaded ok.");
            mActivity.runLoadStateCallback(mState);
        } else {
            Log.v(TAG, "---> SQL TABLES failed to load.");
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
