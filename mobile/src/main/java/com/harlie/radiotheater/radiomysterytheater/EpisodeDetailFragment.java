package com.harlie.radiotheater.radiomysterytheater;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.harlie.radiotheater.radiomysterytheater.data.actors.ActorsCursor;
import com.harlie.radiotheater.radiomysterytheater.data.actorsepisodes.ActorsEpisodesCursor;
import com.harlie.radiotheater.radiomysterytheater.data.writers.WritersCursor;
import com.harlie.radiotheater.radiomysterytheater.data.writersepisodes.WritersEpisodesCursor;
import com.harlie.radiotheater.radiomysterytheater.data_helper.EpisodeRecyclerViewItem;
import com.harlie.radiotheater.radiomysterytheater.data_helper.RadioTheaterContract;
import com.harlie.radiotheater.radiomysterytheater.data_helper.SQLiteHelper;
import com.harlie.radiotheater.radiomysterytheater.utils.BitmapHelper;
import com.harlie.radiotheater.radiomysterytheater.utils.FontPreferences;
import com.harlie.radiotheater.radiomysterytheater.utils.LogHelper;

import java.io.IOException;
import java.util.Locale;

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
    //private MediaPlayer mp;

    private AppCompatButton mPlayNow;
    private AppCompatButton mWebLink;
    private long mEpisodeId;

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
        //mp = MediaPlayer.create(this.getActivity(), R.raw.click);

        super.onCreate(savedInstanceState);

        if (getArguments() != null && getArguments().containsKey(ARG_EPISODE_ID)) {
            // Load the content specified by the fragment arguments.
            // The ARG_EPISODE_PARCELABLE contains a Parcelable EpisodeRecyclerViewItem.
            mEpisodeId = Long.valueOf(getArguments().getString(ARG_EPISODE_ID));
            BaseActivity baseActivity = (BaseActivity) getActivity();
            baseActivity.setEpisodeNumber(mEpisodeId);
            LogHelper.v(TAG, "onCreate: build EpisodeRecyclerViewItem for (RECEIVE) ARG_EPISODE_ID="+ mEpisodeId);
            mItem = getArguments().getParcelable(ARG_EPISODE_PARCELABLE);

            // Load the actors for this episode
            ActorsEpisodesCursor actorsEpisodesCursor = SQLiteHelper.getActorsEpisodesCursor(mEpisodeId);
            int actorNumber = 0;
            while (actorsEpisodesCursor != null && actorsEpisodesCursor.moveToNext()) {
                actorNumber += 1;
                long actorId = actorsEpisodesCursor.getFieldActorId();
                ActorsCursor actorsCursor = SQLiteHelper.getActorsCursor(actorId);
                if (actorsCursor != null && actorsCursor.moveToNext()) {
                    String actorName = actorsCursor.getFieldActorName();
                    String actorImage = actorsCursor.getFieldActorUrl().replace(".jpg", "");
                    LogHelper.v(TAG, "onCreate: actorName="+actorName+", actorImage="+actorImage);
                    switch (actorNumber) {
                        case 1: {
                            mItem.setActor1(actorImage);
                            break;
                        }
                        case 2: {
                            mItem.setActor2(actorImage);
                            break;
                        }
                        case 3: {
                            mItem.setActor3(actorImage);
                            break;
                        }
                        case 4: {
                            mItem.setActor4(actorImage);
                            break;
                        }
                        case 5: {
                            mItem.setActor5(actorImage);
                            break;
                        }
                        case 6: {
                            mItem.setActor6(actorImage);
                            break;
                        }
                    }
                    actorsCursor.close();
                }
            }
            if (actorsEpisodesCursor != null) {
                actorsEpisodesCursor.close();
            }

            // Load the writers for this episode
            WritersEpisodesCursor writersEpisodesCursor = SQLiteHelper.getWritersEpisodesCursor(mEpisodeId);
            while (writersEpisodesCursor != null && writersEpisodesCursor.moveToNext()) {
                long writerId = writersEpisodesCursor.getFieldWriterId();
                WritersCursor writersCursor = SQLiteHelper.getWritersCursor(writerId);
                if (writersCursor != null && writersCursor.moveToNext()) {
                    String writerName = writersCursor.getFieldWriterName();
                    String writerImage = writersCursor.getFieldWriterUrl().replace(".jpg", "");
                    LogHelper.v(TAG, "onCreate: writerName="+writerName+", writerImage="+writerImage);
                    mItem.setWriter(writerImage);
                    writersCursor.close();
                }
            }
            if (writersEpisodesCursor != null) {
                writersEpisodesCursor.close();
            }

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
        final Drawable pleaseWaitButton = ResourcesCompat.getDrawable(getResources(), R.drawable.radio_theater_please_wait_button_selector, null);
        final Drawable playNowButton = ResourcesCompat.getDrawable(getResources(), R.drawable.radio_theater_playnow_button_selector, null);

        // Show the dummy content as text in a TextView.
        if (mItem != null) {
            ((TextView) rootView.findViewById(R.id.episode_number)).setText("#"+String.valueOf(mItem.getEpisodeNumber()));
            String airdate = RadioTheaterContract.airDateShort(mItem.getAirdate());
            ((TextView) rootView.findViewById(R.id.episode_airdate)).setText(airdate);
            ((TextView) rootView.findViewById(R.id.episode_title)).setText(mItem.getTitle());
            ((TextView) rootView.findViewById(R.id.episode_description)).setText(mItem.getDescription());
            ((RatingBar) rootView.findViewById(R.id.episode_rating)).setNumStars((int) mItem.getRating());

            int height = 400;
            int width = 300;

            String[] assetList = new String[0];
            try {
                assetList = RadioTheaterApplication.getRadioTheaterApplicationContext().getAssets().list("portraits");
            } catch (IOException e) {
                LogHelper.e(TAG, "unable to read portraits assets! e="+e);
            }
            LogHelper.v(TAG, "available portraits: " + assetList.toString());

            String actor1 = mItem.getActor1();
            loadPortrait(rootView, actor1, R.id.actor1, R.id.actor1_name, height, width);
            String actor2 = mItem.getActor2();
            loadPortrait(rootView, actor2, R.id.actor2, R.id.actor2_name, height, width);
            String actor3 = mItem.getActor3();
            loadPortrait(rootView, actor3, R.id.actor3, R.id.actor3_name, height, width);
            String actor4 = mItem.getActor4();
            loadPortrait(rootView, actor4, R.id.actor4, R.id.actor4_name, height, width);
            String actor5 = mItem.getActor5();
            loadPortrait(rootView, actor5, R.id.actor5, R.id.actor5_name, height, width);
            String actor6 = mItem.getActor6();
            loadPortrait(rootView, actor6, R.id.actor6, R.id.actor6_name, height, width);
            String writer = mItem.getWriter();
            loadPortrait(rootView, writer, R.id.writer, R.id.writer_name, height, width);

            mPlayNow = (AppCompatButton) rootView.findViewById(R.id.play_now);
            mPlayNow.setBackground(playNowButton);
            mPlayNow.setEnabled(true);
            mPlayNow.playSoundEffect(SoundEffectConstants.CLICK);
            mWebLink = (AppCompatButton) rootView.findViewById(R.id.weblink);

            final BaseActivity baseActivity = (BaseActivity) this.getActivity();
            mPlayNow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mPlayNow.setEnabled(false);
                    mPlayNow.setBackground(pleaseWaitButton);
                    //mp.start();
                    Intent autoplayIntent = new Intent(baseActivity, AutoplayActivity.class);
                    // setup a shared-element transition..
                    LogHelper.v(TAG, "onClick - PLAY NOW - using shared element transition");
                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(baseActivity, mPlayNow, "PlayNow");
                    startActivity(autoplayIntent, options.toBundle());
                    // close existing activity stack regardless of what's in there and create new root
                    //autoplayIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    Bundle playInfo = options.toBundle();
                    baseActivity.savePlayInfoToBundle(playInfo);
                    autoplayIntent.putExtras(playInfo);
                    autoplayIntent.putExtra("PLAY_NOW", String.valueOf(mEpisodeId));
                    startActivity(autoplayIntent, playInfo);
                    baseActivity.overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
                    baseActivity.getHandler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mPlayNow.setBackground(playNowButton);
                            mPlayNow.setEnabled(true);
                        }
                    }, 7000);
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
                    getActivity().overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
                }
            });

        }
        setFontTypeAndSizes((BaseActivity) this.getActivity(), rootView);
        return rootView;
    }

    static final int DETAIL_SCALE_AMT = 9;
    public void setFontTypeAndSizes(BaseActivity activity, View rootView) {
        //LogHelper.v(TAG, "setFontTypeAndSizes");
        FontPreferences fontPreferences = new FontPreferences(activity);
        String fontname = fontPreferences.getFontName();
        String fontsize = fontPreferences.getFontSize();
        LogHelper.v(TAG, "--> USING FONT NAME="+fontname+", SIZE="+fontsize);
        activity.getTheme().applyStyle(fontPreferences.getFontStyle().getResId(), true);

        int[] attrs = {R.attr.font_small, R.attr.font_medium, R.attr.font_large, R.attr.font_xlarge}; // The attributes to retrieve
        TypedArray ta = activity.obtainStyledAttributes(fontPreferences.getFontStyle().getResId(), attrs);
        String str;
        //noinspection ResourceType
        float titleTextSize = 0, descriptionTextSize = 0, airdateTextSize = 0, episodeNumberTextSize = 0;
        if (! EpisodeListActivity.isTwoPane()
                || activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
        {
            titleTextSize = getTextSize(ta, 3) + DETAIL_SCALE_AMT + 5;
            descriptionTextSize = getTextSize(ta, 2) + DETAIL_SCALE_AMT + 3;
            airdateTextSize = getTextSize(ta, 2) + DETAIL_SCALE_AMT + 1;
            episodeNumberTextSize = getTextSize(ta, 2) + DETAIL_SCALE_AMT + 1;
        }
        else {
            titleTextSize = getTextSize(ta, 3) + DETAIL_SCALE_AMT + 4;
            descriptionTextSize = getTextSize(ta, 2) + DETAIL_SCALE_AMT + 2;
            airdateTextSize = getTextSize(ta, 1) + DETAIL_SCALE_AMT;
            episodeNumberTextSize = getTextSize(ta, 1) + DETAIL_SCALE_AMT;
        }

        LogHelper.v(TAG, "=========> titleTextSize="+titleTextSize+", descriptionTextSize="+descriptionTextSize+"," +
                " airdateTextSize="+airdateTextSize+", episodeNumberTextSize="+episodeNumberTextSize);

        ta.recycle();

        if (rootView.findViewById(R.id.episode_title) != null) {
            ((TextView) rootView.findViewById(R.id.episode_title)).setTextSize(titleTextSize);
        }
        if (rootView.findViewById(R.id.episode_description) != null) {
            ((TextView) rootView.findViewById(R.id.episode_description)).setTextSize(descriptionTextSize);
        }
        if (rootView.findViewById(R.id.episode_airdate) != null) {
            ((TextView) rootView.findViewById(R.id.episode_airdate)).setTextSize(airdateTextSize);
        }
        if (rootView.findViewById(R.id.episode_number) != null) {
            ((TextView) rootView.findViewById(R.id.episode_number)).setTextSize(episodeNumberTextSize);
        }
    }

    private float getTextSize(TypedArray ta, int index) {
        float textSize = 0;
        String str;//noinspection ResourceType
        str = ta.getString(index);
        if (str != null) {
            textSize = Float.valueOf(str.substring(0, str.length() - 2)); // discard the "sp" part of the style item
        }
        return textSize;
    }

    private void loadPortrait(View rootView, String person, int personImageResource, int personNameResource, int height, int width) {
        LogHelper.v(TAG, "loadPortrait: person="+person+", height="+height+", width="+width);
        Bitmap bitmapPortrait;
        String person_name = null;
        if (person != null) {
            person_name = makeFullName(person);
//          int portraitResourceId = getResources().getIdentifier("com.harlie.radiotheater.radiomysterytheater:drawable/" + person, null, null);
//          Bitmap bitmap;
//          if (portraitResourceId > 0) {
//              bitmap = BitmapFactory.decodeResource(getResources(), portraitResourceId);
//          } else {
//              bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.unknown);
//          }
            bitmapPortrait = BitmapHelper.getBitmapFromAsset("portraits/"+person+".jpg");
        }
        else {
            bitmapPortrait = BitmapFactory.decodeResource(getResources(), R.drawable.unknown);
        }
        if (person_name != null && bitmapPortrait != null) {
            ((ImageView) rootView.findViewById(personImageResource)).setImageBitmap(BitmapHelper.scaleBitmap(bitmapPortrait, width, height));
            ((ImageView) rootView.findViewById(personImageResource)).setScaleType(ImageView.ScaleType.CENTER_CROP);
            ((ImageView) rootView.findViewById(personImageResource)).setAdjustViewBounds(true);
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
            part = part.substring(0, 1).toUpperCase(Locale.getDefault()) + part.substring(1).toLowerCase(Locale.getDefault());
            fullName = fullName + " " + part;
        }
        return fullName;
    }

}
