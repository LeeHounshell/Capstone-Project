package com.harlie.radiotheater.radiomysterytheater.utils;

public class Episode {
    private String mTitle;
    private String mDescription;
    private int mEpisodeNumber;
    private int mRating;
    private boolean mHeard;
    private boolean mDownloaded;

    public Episode() {
    }

    public Episode(String mTitle, String mDescription, int mRating, boolean mHeard, boolean mDownloaded) {
        this.mTitle = mTitle;
        this.mDescription = mDescription;
        this.mRating = mRating;
        this.mHeard = mHeard;
        this.mDownloaded = mDownloaded;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getDescription() {
        return mDescription;
    }

    public int getEpisodeNumber() {
        return mEpisodeNumber;
    }

    public int getRating() {
        return mRating;
    }

    public boolean isHeard() {
        return mHeard;
    }

    public boolean isDownloaded() {
        return mDownloaded;
    }
}
