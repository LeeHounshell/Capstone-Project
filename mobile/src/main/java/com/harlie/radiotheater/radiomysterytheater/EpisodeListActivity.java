package com.harlie.radiotheater.radiomysterytheater;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import com.harlie.radiotheater.radiomysterytheater.data.configepisodes.ConfigEpisodesCursor;
import com.harlie.radiotheater.radiomysterytheater.data.episodes.EpisodesColumns;
import com.harlie.radiotheater.radiomysterytheater.data_helper.RadioTheaterContract;
import com.harlie.radiotheater.radiomysterytheater.data_helper.DataHelper;
import com.harlie.radiotheater.radiomysterytheater.playback.LocalPlayback;
import com.harlie.radiotheater.radiomysterytheater.utils.EpisodeRecyclerViewAdapter;
import com.harlie.radiotheater.radiomysterytheater.utils.LogHelper;

/**
 * An activity representing a list of Episodes. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link EpisodeDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class EpisodeListActivity extends BaseActivity
    implements LoaderManager.LoaderCallbacks<Cursor>
{
    private final static String TAG = "LEE: <" + EpisodeListActivity.class.getSimpleName() + ">";

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private static boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogHelper.v(TAG, "onCreate");
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_episode_list);

        configureToolbarTitleBehavior();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayShowTitleEnabled(false);
            }
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setFocusable(true);
        final EpisodeListActivity activity = this;
        fab.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("PrivateResource")
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
                LogHelper.v(TAG, "CLICK - fab");
                Intent autoplayIntent = new Intent(activity, AutoplayActivity.class);
                // close existing activity stack regardless of what's in there and create new root
                autoplayIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                Bundle playInfo = new Bundle();
                savePlayInfoToBundle(playInfo);
                autoplayIntent.putExtras(playInfo);
                LogHelper.v(TAG, "STARTACTIVITY: AutoplayActivity.class");
                startActivity(autoplayIntent);
                overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
                if (! sHandleRotationEvent && isTwoPane() && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    FragmentManager fragmentManager = activity.getSupportFragmentManager();
                    fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                }
            }
        });

        final View recyclerView = findViewById(R.id.episode_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);

        if (findViewById(R.id.episode_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        if (!isTwoPane() || getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            // Show the Up button in the action bar. except for TwoPane + LANDSCAPE
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }

        long position = DataHelper.getEpisodeNumber();
        int lastPlaybackState = LocalPlayback.getCurrentState();
        if (position == 0 || PlaybackStateCompat.STATE_PLAYING == lastPlaybackState) {
            LogHelper.v(TAG, "LIST: using current selection in LocalPlayback");
            position = LocalPlayback.getCurrentEpisode();
            LogHelper.v(TAG, "---> use position="+position);
        }
        if (position == 0) {
            LogHelper.v(TAG, "LIST: unable to locate selection - using NEXT AVAILABLE");
            ConfigEpisodesCursor configCursor = DataHelper.getCursorForNextAvailableEpisode();
            if (configCursor == null) {
                getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        activity.problemWithSQLiteDatabase();
                    }
                });
            }
            else {
                DataHelper.getEpisodeDataForCursor(configCursor);
            }
            position = DataHelper.getEpisodeNumber();
        }
        final int activePosition = (int) position - 1;
        LogHelper.v(TAG, "===> LIST: SET INITIAL SCROLL POSITION TO: "+activePosition);
        recyclerView.invalidate();
        ((RecyclerView) recyclerView).scrollToPosition(activePosition);
        if (isTwoPane() && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    LogHelper.v(TAG, "*** AUTO CLICK TO SHOW EPISODE DETAIL ***");
                    RecyclerView.ViewHolder viewHolder = ((RecyclerView) recyclerView).findViewHolderForAdapterPosition(activePosition);
                    if (viewHolder != null && viewHolder.itemView != null) {
                        viewHolder.itemView.performClick();
                    }
                }
            }, 1000);
        }
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        LogHelper.v(TAG, "setupRecyclerView");
        recyclerView.setFocusable(true);

        String order_limit = RadioTheaterContract.EpisodesEntry.FIELD_EPISODE_NUMBER + " ASC";

        Cursor cursor = getContentResolver().query(
                EpisodesColumns.CONTENT_URI,        // the 'content://' Uri to query
                null,                               // projection String[] - leaving "columns" null just returns all the columns.
                null,                               // selection - SQL where
                null,                               // selection args String[] - values for the "where" clause
                order_limit);                       // sort order and limit (String)

        if (cursor != null) {
            EpisodeRecyclerViewAdapter adapter = new EpisodeRecyclerViewAdapter(this, cursor);
            recyclerView.setAdapter(adapter);
            LogHelper.v(TAG, "setupRecyclerView: cursor has Count=" + cursor.getCount());
        }
        else {
            LogHelper.e(TAG, "*** FAIL *** SQLite content-resolver query="+EpisodesColumns.CONTENT_URI+", order+limit="+order_limit);
        }

        // --- dummy content disabled ---
        //      //#IFDEF 'DEBUG'
        //      recyclerView.setAdapter(new EpisodeRecyclerViewAdapter(DummyContent.ITEMS, this));
        //      //#ENDIF
        // --- dummy content disabled ---
    }

    public static boolean isTwoPane() {
        return mTwoPane;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        LogHelper.v(TAG, "onOptionsItemSelected");
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            if (isTwoPane() && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                navigateUpTo(new Intent(this, AutoplayActivity.class));
            }
            else {
                onBackPressed();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        LogHelper.v(TAG, "onBackPressed");
        super.onBackPressed();
        startAutoplayActivity(false);
        overridePendingTransition(0, 0);
    }

    //--------------------------------------------------------------------------------
    // Loader callbacks
    //--------------------------------------------------------------------------------

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        LogHelper.v(TAG, "onCreateLoader");
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        LogHelper.v(TAG, "onLoadFinished");
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        LogHelper.v(TAG, "onLoaderReset");
    }

}
