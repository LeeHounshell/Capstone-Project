package com.harlie.radiotheater.radiomysterytheater.dummy;

import com.harlie.radiotheater.radiomysterytheater.data_helper.EpisodeRecyclerViewItem;
import com.harlie.radiotheater.radiomysterytheater.data_helper.RadioTheaterContract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample episode_description for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: (Make private for TestCases) replace all app uses of this class before publishing.
 */
public class DummyContent {

    private String mTitle;
    private String mDescription;
    private int mEpisodeNumber;
    private int mRating;
    private boolean mHeard;
    private boolean mDownloaded;

    /**
     * An array of sample (dummy) items.
     */
    public static final List<EpisodeRecyclerViewItem> ITEMS = new ArrayList<EpisodeRecyclerViewItem>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, EpisodeRecyclerViewItem> ITEM_MAP = new HashMap<String, EpisodeRecyclerViewItem>();

    private static final int COUNT = 1399;

    static {
        // Add some sample items.
        for (int i = 1; i <= COUNT; i++) {
            addItem(createEpisodeRecyclerViewItem(i));
        }
    }

    private static void addItem(EpisodeRecyclerViewItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.episode_title, item);
    }

    private static EpisodeRecyclerViewItem createEpisodeRecyclerViewItem(int episode) {
        if (episode % 3 == 1) {
            String weblink = "www.cbsrmt.com\\/episode_name-1-the-old-ones-are-hard-to-kill.html";
            String download = "www.cbsrmt.com\\/mp3\\/CBS Radio Mystery Theater 74-01-06 e0001 The Old Ones Are Hard to Kill.mp3";
            String title = "The old ones are hard to kill";
            String airdate = RadioTheaterContract.airDateShort("1974-01-06");
            String description = "An old lady rents a room to a sick boarder. She runs into problems with his strange deathbed confession.";
            float rating = 5;
            String actor1 = "dekoven_roger";
            String actor2 = "janney_leon";
            String actor3 = "moorehead_agnes";
            String actor4 = null;
            String actor5 = null;
            String actor6 = null;
            String writer = "slesar_henry";

            return new EpisodeRecyclerViewItem(
                    title,
                    airdate,
                    description,
                    episode,
                    rating,
                    false, // heard
                    false, // downloaded
                    actor1,
                    actor2,
                    actor3,
                    actor4,
                    actor5,
                    actor6,
                    writer,
                    weblink,
                    download);
        }
        else
        if (episode % 3 == 2) {
            String weblink = "www.cbsrmt.com\\/episode_name-2-the-return-of-the-moresbys.html";
            String download = "www.cbsrmt.com\\/mp3\\/CBS Radio Mystery Theater 74-01-07 e0002 The Return of the Moresbys.mp3";
            String title = "The Return of the Moresbys";
            String airdate = RadioTheaterContract.airDateShort("1974-01-07");
            String description = "A husband kills his wife for donating all their money. Now, he is certain that she has been reincarnated in the form of a cat to wreak revenge on him.";
            float rating = 3;
            String actor1 = "oneal_patrick";
            String actor2 = "ocko_dan";
            String actor3 = "pryor_nick";
            String actor4 = "seldes_marian";
            String actor5 = null;
            String actor6 = null;
            String writer = "slesar_henry";

            return new EpisodeRecyclerViewItem(
                    title,
                    airdate,
                    description,
                    episode,
                    rating,
                    false, // heard
                    false, // downloaded
                    actor1,
                    actor2,
                    actor3,
                    actor4,
                    actor5,
                    actor6,
                    writer,
                    weblink,
                    download);
        }
        else {
        /* if (episode % 3 == 0) */
            String weblink = "www.cbsrmt.com\\/episode_name-3-the-bullet.html";
            String download = "www.cbsrmt.com\\/mp3\\/CBS Radio Mystery Theater 74-01-08 e0003 The Bullet.mp3";
            String title = "The Bullet";
            String airdate = RadioTheaterContract.airDateShort("1974-01-08");
            String description = "An accident kills a man but he is made to return to Earth to trade places with the fated victim.";
            float rating = 3;
            String actor1 = "bell_ralph";
            String actor2 = "haines_larry";
            String actor3 = "janney_leon";
            String actor4 = "juster_evie";
            String actor5 = "newman_martin";
            String actor6 = "ocko_dan";
            String writer = "dann_sam";

            return new EpisodeRecyclerViewItem(
                    title,
                    airdate,
                    description,
                    episode, // episodeNumber
                    rating,
                    false, // heard
                    false, // downloaded
                    actor1,
                    actor2,
                    actor3,
                    actor4,
                    actor5,
                    actor6,
                    writer,
                    weblink,
                    download);
        }
    }

}
