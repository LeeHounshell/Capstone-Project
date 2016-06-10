package com.harlie.radiotheater.radiomysterytheater.data_helper;

import android.content.ContentValues;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.harlie.radiotheater.radiomysterytheater.BaseActivity;
import com.harlie.radiotheater.radiomysterytheater.R;

import at.grabner.circleprogress.CircleProgressView;

// NOTE: this AsyncTask should run 3 times: WRITERS, ACTORS, EPISODES
public class LoadRadioTheaterTablesAsyncTask extends AsyncTask<BaseActivity, Void, Boolean> {
    private final static String TAG = "LEE: <" + LoadRadioTheaterTablesAsyncTask.class.getSimpleName() + ">";

    public enum LoadState {
        WRITERS, ACTORS, EPISODES
    }
    private volatile LoadState mState = LoadState.WRITERS;

    private BaseActivity mActivity;
    private CircleProgressView mCircleProgressView;
    private DataSnapshot mDataSnapshot;
    private boolean mTesting;

    public LoadRadioTheaterTablesAsyncTask(BaseActivity activity, CircleProgressView circleProgressView, DataSnapshot dataSnapshot, LoadState state, boolean testing) {
        Log.v(TAG, "new LoadRadioTheaterTablesAsyncTask");
        this.mActivity = activity;
        this.mCircleProgressView = circleProgressView;
        this.mDataSnapshot = dataSnapshot;
        this.mState = state;
        this.mTesting = testing;
    }

    @Override
    protected void onPreExecute() {
        Log.v(TAG, "onPreExecute");
        super.onPreExecute();
        mCircleProgressView.setVisibility(View.VISIBLE);
    }

    @Override
    protected Boolean doInBackground(BaseActivity... params) {
        Log.v(TAG, "---------> doInBackground: snapshot="+mDataSnapshot.getValue());
        if (mTesting) {
            loadSomeTestData();
        }
        SystemClock.sleep(1000); // FIXME don't pretend
        return true; // pretending
    }

    private void loadSomeTestData() {
        Log.v(TAG, "loadSomeTestData");
        ContentValues episodeValues = new ContentValues();
        episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_EPISODE_NUMBER, "0001");
        episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_AIRDATE, "1974-01-06");
        episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_EPISODE_TITLE, "The Old Ones Are Hard to Kill");
        episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_EPISODE_DESCRIPTION, "An old lady rents a room to a sick boarder. She runs into problems with his strange deathbed confession.");
        episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_DOWNLOAD_URL, "www.cbsrmt.com/mp3/CBS Radio Mystery Theater 74-01-06 e0001 The Old Ones Are Hard to Kill.mp3");
        episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_WEBLINK_URL, "www.cbsrmt.com/episode_name-1-the-old-ones-are-hard-to-kill.html");
        episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_RATING, 3.2);
        episodeValues.put(RadioTheaterContract.EpisodesEntry.FIELD_VOTE_COUNT, 1); // true count is unknown at present
        Uri result;
        try {
            result = insertEpisodeValues(episodeValues);
            Log.v(TAG, "Episode insert result="+result);
        }
        catch (Exception e) {
            Log.e(TAG, "insert exception="+e);
        }

        ContentValues actorValues = new ContentValues();
        actorValues.put(RadioTheaterContract.ActorsEntry.FIELD_ACTOR_ID, 1);
        actorValues.put(RadioTheaterContract.ActorsEntry.FIELD_ACTOR_NAME, "DeKoven, Roger");
        actorValues.put(RadioTheaterContract.ActorsEntry.FIELD_ACTOR_URL, "dekoven_roger.jpg");
        try {
            result = insertActorValues(actorValues);
            Log.v(TAG, "Actor insert result="+result);
        }
        catch (Exception e) {
            Log.e(TAG, "insert exception="+e);
        }

        ContentValues writerValues = new ContentValues();
        writerValues.put(RadioTheaterContract.WritersEntry.FIELD_WRITER_ID, 1);
        writerValues.put(RadioTheaterContract.WritersEntry.FIELD_WRITER_NAME, "Slesar, Henry");
        writerValues.put(RadioTheaterContract.WritersEntry.FIELD_WRITER_URL, "slesar_henry.jpg");
        try {
            result = insertWriterValues(writerValues);
            Log.v(TAG, "Writer insert result="+result);
        }
        catch (Exception e) {
            Log.e(TAG, "insert exception="+e);
        }
    }

    private Uri insertEpisodeValues(ContentValues episodeValues) {
        Log.v(TAG, "insertEpisodeValues=");
        Uri episode = RadioTheaterContract.EpisodesEntry.buildEpisodesUri();
        return mActivity.getContentResolver().insert(episode, episodeValues);
    }

    private Uri insertActorValues(ContentValues actorValues) {
        Log.v(TAG, "insertActorValues");
        Uri actor = RadioTheaterContract.ActorsEntry.buildActorsUri();
        return mActivity.getContentResolver().insert(actor, actorValues);
    }

    private Uri insertWriterValues(ContentValues writerValues) {
        Log.v(TAG, "insertWriterValues");
        Uri writer = RadioTheaterContract.WritersEntry.buildWritersUri();
        return mActivity.getContentResolver().insert(writer, writerValues);
    }

    @Override
    protected void onPostExecute(Boolean successTablesLoaded) {
        Log.v(TAG, "onPostExecute: successTablesLoaded="+successTablesLoaded);
        super.onPostExecute(successTablesLoaded);
        if (successTablesLoaded) {
            Log.v(TAG, "---> SQL TABLES loaded ok.");
            mActivity.runLoadStateCallback(mState);
        } else {
            Log.v(TAG, "---> SQL TABLES failed to load.");
            mActivity.startAuthenticationActivity();
        }
    }

}
