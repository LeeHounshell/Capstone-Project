package com.harlie.radiotheater.radiomysterytheater;

import java.util.ArrayList;
import java.util.List;

public final class EpisodeList {
    public static final String EPISODE_CATEGORY[] = {
            "Category Zero",
            "Category One",
            "Category Two",
            "Category Three",
            "Category Four",
            "Category Five",
    };

    public static List<Episode> list;

    public static List<Episode> setupEpisodes() {
        list = new ArrayList<Episode>();
        String title[] = {
                "Zeitgeist 2010_ Year in Review",
                "Google Demo Slam_ 20ft Search",
                "Introducing Gmail Blue",
                "Introducing Google Fiber to the Pole",
                "Introducing Google Nose"
        };

        String description = "Fusce id nisi turpis. Praesent viverra bibendum semper. "
                + "Donec tristique, orci sed semper lacinia, quam erat rhoncus massa, non congue tellus est "
                + "quis tellus. Sed mollis orci venenatis quam scelerisque accumsan. Curabitur a massa sit "
                + "amet mi accumsan mollis sed et magna. Vivamus sed aliquam risus. Nulla eget dolor in elit "
                + "facilisis mattis. Ut aliquet luctus lacus. Phasellus nec commodo erat. Praesent tempus id "
                + "lectus ac scelerisque. Maecenas pretium cursus lectus id volutpat.";

        String showUrl[] = {
                "http://commondatastorage.googleapis.com/android-tv/Sample%20videos/Zeitgeist/Zeitgeist%202010_%20Year%20in%20Review.mp4",
                "http://commondatastorage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%2020ft%20Search.mp4",
                "http://commondatastorage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Gmail%20Blue.mp4",
                "http://commondatastorage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Google%20Fiber%20to%20the%20Pole.mp4",
                "http://commondatastorage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Google%20Nose.mp4"
        };
        String bgImageUrl[] = {
                "http://commondatastorage.googleapis.com/android-tv/Sample%20videos/Zeitgeist/Zeitgeist%202010_%20Year%20in%20Review/bg.jpg",
                "http://commondatastorage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%2020ft%20Search/bg.jpg",
                "http://commondatastorage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Gmail%20Blue/bg.jpg",
                "http://commondatastorage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Google%20Fiber%20to%20the%20Pole/bg.jpg",
                "http://commondatastorage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Google%20Nose/bg.jpg",
        };
        String cardImageUrl[] = {
                "http://commondatastorage.googleapis.com/android-tv/Sample%20videos/Zeitgeist/Zeitgeist%202010_%20Year%20in%20Review/card.jpg",
                "http://commondatastorage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%2020ft%20Search/card.jpg",
                "http://commondatastorage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Gmail%20Blue/card.jpg",
                "http://commondatastorage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Google%20Fiber%20to%20the%20Pole/card.jpg",
                "http://commondatastorage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Google%20Nose/card.jpg"
        };

        list.add(buildEpisodeInfo("category", title[0],
                description, "Studio Zero", showUrl[0], cardImageUrl[0], bgImageUrl[0]));
        list.add(buildEpisodeInfo("category", title[1],
                description, "Studio One", showUrl[1], cardImageUrl[1], bgImageUrl[1]));
        list.add(buildEpisodeInfo("category", title[2],
                description, "Studio Two", showUrl[2], cardImageUrl[2], bgImageUrl[2]));
        list.add(buildEpisodeInfo("category", title[3],
                description, "Studio Three", showUrl[3], cardImageUrl[3], bgImageUrl[3]));
        list.add(buildEpisodeInfo("category", title[4],
                description, "Studio Four", showUrl[4], cardImageUrl[4], bgImageUrl[4]));

        return list;
    }

    private static Episode buildEpisodeInfo(String category, String title,
                                          String description, String studio, String showUrl, String cardImageUrl,
                                          String bgImageUrl) {
        Episode episode = new Episode();
        episode.setId(Episode.getCount());
        Episode.incCount();
        episode.setTitle(title);
        episode.setDescription(description);
        episode.setStudio(studio);
        episode.setCategory(category);
        episode.setCardImageUrl(cardImageUrl);
        episode.setBackgroundImageUrl(bgImageUrl);
        episode.setShowUrl(showUrl);
        return episode;
    }
}
