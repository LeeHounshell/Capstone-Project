package com.harlie.radiotheater.radiomysterytheater;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.harlie.radiotheater.radiomysterytheater.dummy.DummyContent;

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
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The dummy content this fragment is presenting.
     */
    private DummyContent.DummyItem mItem;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public EpisodeDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mItem = DummyContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
        }
        return rootView;
    }

    private void loadPortrait(View rootView, String person, int personImageResource, int personNameResource) {
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
