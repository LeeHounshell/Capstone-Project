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

            return new DummyItem(
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

            return new DummyItem(
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

            return new DummyItem(
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

    /**
     * A dummy item representing a piece of episode_description.
     */
    public static class DummyItem {

        public String episode_title;
        public String episode_airdate;
        public String episode_description;
        public int episode_number;
        public float episode_rating;
        public boolean episode_heard;
        public boolean episode_downloaded;
        public String actor1;
        public String actor2;
        public String actor3;
        public String actor4;
        public String actor5;
        public String actor6;
        public String writer;
        public String weblink;
        public String download;

        public DummyItem(String title, String airdate, String description, int episodeNumber, float rating, boolean heard, boolean downloaded,
                         String actor1, String actor2, String actor3, String actor4, String actor5, String actor6, String writer, String weblink, String download)
        {
            setTitle(title);
            setAirdate(airdate);
            setDescription(description);
            setEpisodeNumber(episodeNumber);
            setRating(rating);
            setHeard(heard);
            setDownloaded(downloaded);
            setActor1(actor1);
            setActor2(actor2);
            setActor3(actor3);
            setActor4(actor4);
            setActor5(actor5);
            setActor6(actor6);
            setWriter(writer);
            setWeblink(weblink);
            setDownload(download);
        }

        @Override
        public String toString() {
            return episode_number + " " + episode_airdate + "  " + episode_title + ": " + episode_description + " ... Rated " + episode_rating + " stars.\n\n";
        }

        public String getDummyEpisodeDetail() {
            String dummyDetail = toString();
            return dummyDetail + dummyDetail + dummyDetail;
        }

        public String getTitle() {
            return episode_title;
        }

        public void setTitle(String episode_title) {
            this.episode_title = episode_title;
        }

        public String getAirdate() {
            return episode_airdate;
        }

        public void setAirdate(String episode_airdate) {
            this.episode_airdate = episode_airdate;
        }

        public boolean isDownloaded() {
            return episode_downloaded;
        }

        public void setDownloaded(boolean episode_downloaded) {
            this.episode_downloaded = episode_downloaded;
        }

        public boolean isHeard() {
            return episode_heard;
        }

        public void setHeard(boolean episode_heard) {
            this.episode_heard = episode_heard;
        }

        public float getRating() {
            return episode_rating;
        }

        public void setRating(float episode_rating) {
            this.episode_rating = episode_rating;
        }

        public int getEpisodeNumber() {
            return episode_number;
        }

        public void setEpisodeNumber(int episode_number) {
            this.episode_number = episode_number;
        }

        public String getDescription() {
            return episode_description;
        }

        public void setDescription(String episode_description) {
            this.episode_description = episode_description;
        }

        public String getActor1() {
            return actor1;
        }

        public void setActor1(String actor1) {
            this.actor1 = actor1;
        }

        public String getActor2() {
            return actor2;
        }

        public void setActor2(String actor2) {
            this.actor2 = actor2;
        }

        public String getActor3() {
            return actor3;
        }

        public void setActor3(String actor3) {
            this.actor3 = actor3;
        }

        public String getActor4() {
            return actor4;
        }

        public void setActor4(String actor4) {
            this.actor4 = actor4;
        }

        public String getActor5() {
            return actor5;
        }

        public void setActor5(String actor5) {
            this.actor5 = actor5;
        }

        public String getActor6() {
            return actor6;
        }

        public void setActor6(String actor6) {
            this.actor6 = actor6;
        }

        public String getWriter() {
            return writer;
        }

        public void setWriter(String writer) {
            this.writer = writer;
        }

        public String getWeblink() {
            return weblink;
        }

        public void setWeblink(String weblink) {
            this.weblink = weblink;
        }

        public String getDownload() {
            return download;
        }

        public void setDownload(String download) {
            this.download = download;
        }
    }

}
