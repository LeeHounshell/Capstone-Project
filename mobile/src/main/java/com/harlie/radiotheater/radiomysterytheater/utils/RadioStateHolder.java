package com.harlie.radiotheater.radiomysterytheater.utils;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.wearable.DataMap;
import com.harlie.radiotheater.radiomysterytheater.data_helper.DataHelper;
import com.harlie.radiotheater.radiomysterytheater.playback.LocalPlayback;


// NOTE: this class needs to maintain Parcelable compatibility with the wear version.
public class RadioStateHolder implements Parcelable {
    private static final String TAG = "LEE: <" + RadioStateHolder.class.getSimpleName() + ">";

    private long theTime;
    private boolean isDirty;
    private long radioState;
    private long episodeNumber;
    private long position;
    private long duration;
    private String title;
    private String description;
    private String airdate;

    public RadioStateHolder(RadioStateHolder copy) {
        this.theTime = copy.theTime;
        this.isDirty = copy.isDirty;
        this.radioState = copy.radioState;
        this.episodeNumber = copy.episodeNumber;
        this.position = copy.position;
        this.duration = copy.duration;
        this.title = copy.title;
        this.description = copy.description;
        this.airdate = copy.airdate;
    }

    public RadioStateHolder() {
    }

    public DataMap toDataMap() {
        DataMap dmap = new DataMap();
        dmap.putLong("theTime", theTime);
        dmap.putBoolean("isDirty", isDirty());
        dmap.putLong("radioState", getRadioState());
        dmap.putLong("episodeNumber", getEpisodeNumber());
        dmap.putLong("position", getPosition());
        dmap.putLong("duration", getDuration());
        dmap.putString("title", getTitle());
        dmap.putString("description", getDescription());
        dmap.putString("airdate", getAirdate());
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
        return true;
    }

    @Override
    public int hashCode() {
        int result = (isDirty() ? 1 : 0);
        result = 31 * result + (int) getTheTime();
        result = 31 * result + (int) getRadioState();
        result = 31 * result + (int) getEpisodeNumber();
        result = 31 * result + (int) getPosition();
        result = 31 * result + (int) getDuration();
        result = 31 * result + getTitle().length();
        result = 31 * result + getDescription().length();
        result = 31 * result + getAirdate().length();
        return result;
    }

    public void reset() {
        LogHelper.v(TAG, "reset");
        setDirty(false);
        setTheTime(System.currentTimeMillis());
        setRadioState(LocalPlayback.getCurrentState());
        setEpisodeNumber(DataHelper.getEpisodeNumber());
        setTitle(DataHelper.getEpisodeTitle());
        setDescription(DataHelper.getEpisodeDescription());
        setAirdate(DataHelper.getAirdate());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.theTime);
        dest.writeValue(this.isDirty);
        dest.writeValue(this.radioState);
        dest.writeValue(this.episodeNumber);
        dest.writeValue(this.position);
        dest.writeValue(this.duration);
        dest.writeString(this.title);
        dest.writeString(this.description);
        dest.writeString(this.airdate);
    }

    protected RadioStateHolder(Parcel in) {
        this.theTime = (Long) in.readValue(Long.class.getClassLoader());
        this.isDirty = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.radioState = (Integer) in.readValue(Integer.class.getClassLoader());
        this.episodeNumber = (Integer) in.readValue(Integer.class.getClassLoader());
        this.position = (Integer) in.readValue(Integer.class.getClassLoader());
        this.duration = (Integer) in.readValue(Integer.class.getClassLoader());
        this.title = in.readString();
        this.description = in.readString();
        this.airdate = in.readString();
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

    public long getTheTime() {
        return theTime;
    }

    public void setTheTime(long theTime) {
        this.theTime = theTime;
    }

    public boolean isDirty() {
        return isDirty;
    }

    public void setDirty(boolean dirty) {
        isDirty = dirty;
    }

    public long getRadioState() {
        return radioState;
    }

    public void setRadioState(long radioState) {
        this.radioState = radioState;
    }

    public long getEpisodeNumber() {
        return episodeNumber;
    }

    public void setEpisodeNumber(long episodeNumber) {
        this.episodeNumber = episodeNumber;
    }

    public long getPosition() {
        return DataHelper.getCurrentPosition();
    }

    public long getDuration() {
        return DataHelper.getDuration();
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

}
