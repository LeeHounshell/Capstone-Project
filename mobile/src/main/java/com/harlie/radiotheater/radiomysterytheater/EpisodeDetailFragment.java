package com.harlie.radiotheater.radiomysterytheater;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.harlie.radiotheater.radiomysterytheater.data_helper.EpisodeRecyclerViewItem;
import com.harlie.radiotheater.radiomysterytheater.utils.LogHelper;

import static com.harlie.radiotheater.radiomysterytheater.data_helper.EpisodeRecyclerViewItem.CREATOR;

/**
 * A fragment representing a single Episode detail screen.
 * This fragment is either contained in a {@link EpisodeListActivity}
 * in two-pane mode (on tablets) or a {@link EpisodeDetailActivity}
 * on handsets.
 */
public class EpisodeDetailFragment extends FragmentBase {
    private final static String TAG = "LEE: <" + EpisodeDetailFragment.class.getSimpleName() + ">";

    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_EPISODE_ID = "KEY_EPISODE_ID";
    public static final String ARG_EPISODE_PARCELABLE = "KEY_EPISODE_PARCELABLE";

    private AppCompatButton mPlayNow;
    private AppCompatButton mWebLink;

    /**
     * The dummy content this fragment is presenting.
     */
    private EpisodeRecyclerViewItem mItem;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public EpisodeDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        LogHelper.v(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_EPISODE_ID)) {
            // Load the content specified by the fragment arguments.
            // The ARG_EPISODE_PARCELABLE contains a Parcelable EpisodeRecyclerViewItem.
            String episodeId = getArguments().getString(ARG_EPISODE_ID);
            LogHelper.v(TAG, "onCreate: build EpisodeRecyclerViewItem for (RECEIVE) ARG_EPISODE_ID="+ episodeId);
            mItem = getArguments().getParcelable(ARG_EPISODE_PARCELABLE);

            // --- dummy content disabled ---
//          //#IFDEF 'DEBUG'
//          mItem = DummyContent.ITEM_MAP.get(getArguments().getString(ARG_EPISODE_ID));
//          //#ENDIF
            // --- dummy content disabled ---
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LogHelper.v(TAG, "onCreateView");
        View rootView = inflater.inflate(R.layout.episode_detail, container, false);

        // Show the dummy content as text in a TextView.
        if (mItem != null) {
            ((TextView) rootView.findViewById(R.id.episode_number)).setText("#"+String.valueOf(mItem.getEpisodeNumber()));
            ((TextView) rootView.findViewById(R.id.episode_airdate)).setText(mItem.getAirdate());
            ((TextView) rootView.findViewById(R.id.episode_title)).setText(mItem.getTitle());
            ((TextView) rootView.findViewById(R.id.episode_description)).setText(mItem.getDescription());
            ((RatingBar) rootView.findViewById(R.id.episode_rating)).setNumStars((int) mItem.getRating());

            String actor1 = mItem.getActor1();
            loadPortrait(rootView, actor1, R.id.actor1, R.id.actor1_name);
            String actor2 = mItem.getActor2();
            loadPortrait(rootView, actor2, R.id.actor2, R.id.actor2_name);
            String actor3 = mItem.getActor3();
            loadPortrait(rootView, actor3, R.id.actor3, R.id.actor3_name);
            String actor4 = mItem.getActor4();
            loadPortrait(rootView, actor4, R.id.actor4, R.id.actor4_name);
            String actor5 = mItem.getActor5();
            loadPortrait(rootView, actor5, R.id.actor5, R.id.actor5_name);
            String actor6 = mItem.getActor6();
            loadPortrait(rootView, actor6, R.id.actor6, R.id.actor6_name);
            String writer = mItem.getWriter();
            loadPortrait(rootView, writer, R.id.writer, R.id.writer_name);

            mPlayNow = (AppCompatButton) rootView.findViewById(R.id.play_now);
            mWebLink = (AppCompatButton) rootView.findViewById(R.id.weblink);

            mPlayNow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    LogHelper.v(TAG, "onClick - PLAY NOW");
                    // FIXME: this 'play_now' button needs to send a message for a specific QueueItem to play.
                    // FIXME: because of time-limitations, this feature and voice-search will be built last, time permitting.
                }
            });

            mWebLink.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String url = "http://" + mItem.getWeblink();
                    url = url.replace("episode_name-", "episode-"); // FIXME: database bug for weblink
                    LogHelper.v(TAG, "onClick - WEB LINK - url="+url);
                    Intent webLinkIntent = new Intent(Intent.ACTION_VIEW);
                    webLinkIntent.setData(Uri.parse(url));
                    startActivity(webLinkIntent);
                }
            });

        }
        return rootView;
    }

    private void loadPortrait(View rootView, String person, int personImageResource, int personNameResource) {
        LogHelper.v(TAG, "loadPortrait");
        if (person != null) {
            String person_name = makeFullName(person);
            int portraitResourceId = getResources().getIdentifier("com.harlie.radiotheater.radiomysterytheater:drawable/" + person, null, null);
            Bitmap bitmap;
            if (portraitResourceId > 0) {
                bitmap = BitmapFactory.decodeResource(getResources(), portraitResourceId);
            } else {
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.unknown);
            }
            ((ImageView) rootView.findViewById(personImageResource)).setImageBitmap(bitmap);
            ((ImageView) rootView.findViewById(personImageResource)).setVisibility(View.VISIBLE);
            ((TextView) rootView.findViewById(personNameResource)).setText(person_name);
            ((TextView) rootView.findViewById(personNameResource)).setVisibility(View.VISIBLE);
        }
    }

    private String makeFullName(String staffMember) {
        LogHelper.v(TAG, "makeFullName");
        String fullName = "";
        if (staffMember.contains(".jpg") || staffMember.contains(".png")) {
            staffMember = staffMember.substring(0, staffMember.length() - 4); // drop the suffix
        }
        String[] names = staffMember.split("_");
        for (int i = names.length; i > 0; --i) {
            String part = names[i - 1];
            part = part.substring(0, 1).toUpperCase() + part.substring(1).toLowerCase();
            fullName = fullName + " " + part;
        }
        return fullName;
    }

}
