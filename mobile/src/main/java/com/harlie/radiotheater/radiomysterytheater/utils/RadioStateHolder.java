package com.harlie.radiotheater.radiomysterytheater.utils;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.wearable.DataMap;


// NOTE: this class needs to maintain Parcelable compatibility with the wear version.
public class RadioStateHolder implements Parcelable {
    private static final String TAG = "LEE: <" + RadioStateHolder.class.getSimpleName() + ">";

    private Boolean isDirty;
    private Integer radioState;
    private Integer episodeNumber;
    private String title;
    private String description;
    private String airdate;
    private Float rating;

    public RadioStateHolder(RadioStateHolder copy) {
        this.isDirty = copy.isDirty;
        this.radioState = copy.radioState;
        this.episodeNumber = copy.episodeNumber;
        this.title = copy.title;
        this.description = copy.description;
        this.airdate = copy.airdate;
        this.rating = copy.rating;
    }

    public RadioStateHolder() {
    }

    public DataMap toDataMap() {
        DataMap dmap = new DataMap();
        dmap.putBoolean("isDirty", isDirty());
        dmap.putInt("radioState", getRadioState());
        dmap.putInt("episodeNumber", getEpisodeNumber());
        dmap.putString("title", getTitle());
        dmap.putString("description", getDescription());
        dmap.putString("airdate", getAirdate());
        dmap.putFloat("rating", getRating());
        return dmap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RadioStateHolder that = (RadioStateHolder) o;

        if (isDirty() != that.isDirty()) return false;
        if (getRadioState() != that.getRadioState()) return false;
        if (getEpisodeNumber() != that.getEpisodeNumber()) return false;
        if (! getTitle().equals(that.getTitle())) return false;
        if (! getDescription().equals(that.getDescription())) return false;
        if (! getAirdate().equals(that.getAirdate())) return false;
        //noinspection RedundantIfStatement
        if (getRating() != that.getRating()) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = (isDirty() ? 1 : 0);
        result = 31 * result + getRadioState();
        result = 31 * result + getEpisodeNumber();
        result = 31 * result + getTitle().length();
        result = 31 * result + getDescription().length();
        result = 31 * result + getAirdate().hashCode();
        result = (int) (31 * result + getRating());
        return result;
    }

    public void reset() {
        LogHelper.v(TAG, "reset");
        isDirty = false;
        radioState = 0;
        episodeNumber = 0;
        title = "";
        description = "";
        airdate = "";
        rating = (float) 0.0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.isDirty);
        dest.writeValue(this.radioState);
        dest.writeValue(this.episodeNumber);
        dest.writeString(this.title);
        dest.writeString(this.description);
        dest.writeString(this.airdate);
        dest.writeValue(this.rating);
    }

    protected RadioStateHolder(Parcel in) {
        this.isDirty = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.radioState = (Integer) in.readValue(Integer.class.getClassLoader());
        this.episodeNumber = (Integer) in.readValue(Integer.class.getClassLoader());
        this.title = in.readString();
        this.description = in.readString();
        this.airdate = in.readString();
        this.rating = (Float) in.readValue(Float.class.getClassLoader());
    }

    public static final Creator<RadioStateHolder> CREATOR = new Creator<RadioStateHolder>() {
        @Override
        public RadioStateHolder createFromParcel(Parcel source) {
            return new RadioStateHolder(source);
        }

        @Override
        public RadioStateHolder[] newArray(int size) {
            return new RadioStateHolder[size];
        }
    };

    public boolean isDirty() {
        return isDirty;
    }

    public void setDirty(boolean dirty) {
        isDirty = dirty;
    }

    public int getRadioState() {
        return radioState;
    }

    public void setRadioState(int radioState) {
        this.radioState = radioState;
    }

    public int getEpisodeNumber() {
        return episodeNumber;
    }

    public void setEpisodeNumber(int episodeNumber) {
        this.episodeNumber = episodeNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAirdate() {
        return airdate;
    }

    public void setAirdate(String airdate) {
        this.airdate = airdate;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

}
