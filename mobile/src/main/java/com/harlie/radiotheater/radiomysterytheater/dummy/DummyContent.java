package com.harlie.radiotheater.radiomysterytheater.dummy;

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
    public static final List<DummyItem> ITEMS = new ArrayList<DummyItem>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, DummyItem> ITEM_MAP = new HashMap<String, DummyItem>();

    private static final int COUNT = 1399;

    static {
        // Add some sample items.
        for (int i = 1; i <= COUNT; i++) {
            addItem(createDummyItem(i));
        }
    }

    private static void addItem(DummyItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.episode_title, item);
    }

    private static DummyItem createDummyItem(int episode) {
        if (episode % 3 == 1) {
            String title = "The old ones are hard to kill";
            String airdate = RadioTheaterContract.airDate("1974-01-06");
            String description = "An old lady rents a room to a sick boarder. She runs into problems with his strange deathbed confession.";
            int rating = 3;
            return new DummyItem(
                    title,
                    airdate,
                    description,
                    episode,
                    rating,
                    false, // heard
                    false); // downloaded
        }
        else
        if (episode % 3 == 2) {
            String title = "The Return of the Moresbys";
            String airdate = RadioTheaterContract.airDate("1974-01-07");
            String description = "A husband kills his wife for donating all their money. Now, he is certain that she has been reincarnated in the form of a cat to wreak revenge on him.";
            int rating = 3;
            return new DummyItem(
                    title,
                    airdate,
                    description,
                    episode,
                    rating,
                    false, // heard
                    false); // downloaded
        }
        else {
        /* if (episode % 3 == 0) */
            String title = "The Bullet";
            String airdate = RadioTheaterContract.airDate("1974-01-08");
            String description = "An accident kills a man but he is made to return to Earth to trade places with the fated victim.";
            int rating = 3;
            return new DummyItem(
                    title,
                    airdate,
                    description,
                    episode, // episodeNumber
                    rating,
                    false, // heard
                    false); // downloaded
        }
    }

    /**
     * A dummy item representing a piece of episode_description.
     */
    public static class DummyItem {

        public String episode_title;
        public String episode_airdate;
        public String episode_description;
        public int episode_number;
        public int episode_rating;
        public boolean episode_heard;
        public boolean episode_downloaded;

        public DummyItem(String title, String airdate, String description, int episodeNumber, int rating, boolean heard, boolean downloaded) {
            setTitle(title);
            setAirdate(airdate);
            setDescription(description);
            setEpisodeNumber(episodeNumber);
            setRating(rating);
            setHeard(heard);
            setDownloaded(downloaded);
        }

        @Override
        public String toString() {
            return episode_number + " " + episode_airdate + "  " + episode_title + ": " + episode_description + " ... Rated " + episode_rating + "\n\n";
        }

        public String getDummyEpisodeDetail() {
            String dummyDetail = toString();
            return dummyDetail + dummyDetail + dummyDetail;
        }

        public String getTitle() {
            return episode_title;
        }

        public String getAirdate() {
            return episode_airdate;
        }

        public boolean isDownloaded() {
            return episode_downloaded;
        }

        public boolean isHeard() {
            return episode_heard;
        }

        public int getRating() {
            return episode_rating;
        }

        public int getEpisodeNumber() {
            return episode_number;
        }

        public String getDescription() {
            return episode_description;
        }

        public void setTitle(String episode_title) {
            this.episode_title = episode_title;
        }

        public void setAirdate(String episode_airdate) {
            this.episode_airdate = episode_airdate;
        }

        public void setDescription(String episode_description) {
            this.episode_description = episode_description;
        }

        public void setEpisodeNumber(int episode_number) {
            this.episode_number = episode_number;
        }

        public void setRating(int episode_rating) {
            this.episode_rating = episode_rating;
        }

        public void setHeard(boolean episode_heard) {
            this.episode_heard = episode_heard;
        }

        public void setDownloaded(boolean episode_downloaded) {
            this.episode_downloaded = episode_downloaded;
        }

    }

}
