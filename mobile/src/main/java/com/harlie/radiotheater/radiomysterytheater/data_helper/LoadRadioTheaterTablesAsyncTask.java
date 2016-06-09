package com.harlie.radiotheater.radiomysterytheater.data_helper;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;

import com.harlie.radiotheater.radiomysterytheater.BaseActivity;
import com.harlie.radiotheater.radiomysterytheater.data.RadioTheaterHelper;

import at.grabner.circleprogress.CircleProgressView;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class LoadRadioTheaterTablesAsyncTask extends AsyncTask<BaseActivity, Void, Boolean> {
    private final static String TAG = "LEE: <" + LoadRadioTheaterTablesAsyncTask.class.getSimpleName() + ">";

    BaseActivity activity;
    CircleProgressView circleProgressView;

    public LoadRadioTheaterTablesAsyncTask(BaseActivity activity, CircleProgressView circleProgressView) {
        Log.v(TAG, "new LoadRadioTheaterTablesAsyncTask");
        this.activity = activity;
        this.circleProgressView = circleProgressView;
    }

    @Override
    protected void onPreExecute() {
        Log.v(TAG, "onPreExecute");
        super.onPreExecute();
        circleProgressView.setVisibility(View.VISIBLE);
    }

    @Override
    protected Boolean doInBackground(BaseActivity... params) {
        Log.v(TAG, "doInBackground");
        //loadSomeTestData();
        for (int i = 0; i < 10; ++i) {
            SystemClock.sleep(1000); // FIXME don't pretend
        }
        return false;
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
        return activity.getContentResolver().insert(episode, episodeValues);
    }

    private Uri insertActorValues(ContentValues actorValues) {
        Log.v(TAG, "insertActorValues");
        Uri actor = RadioTheaterContract.ActorsEntry.buildActorsUri();
        return activity.getContentResolver().insert(actor, actorValues);
    }

    private Uri insertWriterValues(ContentValues writerValues) {
        Log.v(TAG, "insertWriterValues");
        Uri writer = RadioTheaterContract.WritersEntry.buildWritersUri();
        return activity.getContentResolver().insert(writer, writerValues);
    }

    @Override
    protected void onPostExecute(Boolean successTablesLoaded) {
        Log.v(TAG, "onPostExecute");
        super.onPostExecute(successTablesLoaded);
        if (successTablesLoaded) {
            Log.v(TAG, "---> data loaded ok.");
            activity.startAutoplayActivity();
        }
        else {
            Log.v(TAG, "---> data failed to load.");
            activity.startAuthenticationActivity();
        }
    }

}
