package com.harlie.radiotheater.radiomysterytheater;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.harlie.radiotheater.radiomysterytheater.utils.LogHelper;

import static com.harlie.radiotheater.radiomysterytheater.EpisodeListActivity.isTwoPane;

/**
 * An activity representing a single Episode detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link EpisodeListActivity}.
 */
public class EpisodeDetailActivity extends BaseActivity {
    private final static String TAG = "LEE: <" + EpisodeDetailActivity.class.getSimpleName() + ">";

    private String mArgEpisodeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogHelper.v(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_episode_detail);
        configureToolbarTitleBehavior();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayShowTitleEnabled(false);
            }
        }

        Intent intent = getIntent();

//        String action = intent.getAction();
//        Uri data = intent.getData();
//        String packageId = getApplicationContext().getPackageName();
//        System.out.println("LEE: ACTION="+action+", DATA="+data+", PACKAGE="+packageId);

        FloatingActionButton fabActionButton = (FloatingActionButton) findViewById(R.id.fab);
        if (fabActionButton != null) {
            final EpisodeDetailActivity activity = this;
            fabActionButton.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("PrivateResource")
                @Override
                public void onClick(View view) {
                    //Snackbar.make(view, "Replace with your own detail action", Snackbar.LENGTH_LONG)
                    //        .setAction("Action", null).show();
                    LogHelper.v(TAG, "CLICK - mFabActionButton");
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
        }

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            mArgEpisodeId = intent.getStringExtra(EpisodeDetailFragment.ARG_EPISODE_ID);
            Parcelable arg_episode_parcel = intent.getParcelableExtra(EpisodeDetailFragment.ARG_EPISODE_PARCELABLE);
            LogHelper.v(TAG, "(SEND) ARG_EPISODE_PARCELABLE and ARG_EPISODE_ID="+EpisodeDetailFragment.ARG_EPISODE_ID);
            arguments.putString(EpisodeDetailFragment.ARG_EPISODE_ID, mArgEpisodeId);
            arguments.putParcelable(EpisodeDetailFragment.ARG_EPISODE_PARCELABLE, arg_episode_parcel);
            EpisodeDetailFragment fragment = new EpisodeDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.episode_detail_container, fragment)
                    .addToBackStack(null)
                    .commit();
        }

        RadioControlIntentService.startActionStart(this, "DETAIL", mArgEpisodeId, null);
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
                navigateUpTo(new Intent(this, EpisodeListActivity.class));
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

}
