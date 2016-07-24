package com.harlie.radiotheater.radiomysterytheater.data_helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.harlie.radiotheater.radiomysterytheater.BaseActivity;
import com.harlie.radiotheater.radiomysterytheater.data.configepisodes.ConfigEpisodesContentValues;
import com.harlie.radiotheater.radiomysterytheater.data.episodes.EpisodesCursor;
import com.harlie.radiotheater.radiomysterytheater.utils.LogHelper;


public class EpisodeRecyclerViewItem implements Parcelable {
    private final static String TAG = "LEE: <" + EpisodeRecyclerViewItem.class.getSimpleName() + ">";

    private String episode_title;
    private String episode_airdate;
    private String episode_description;
    private int episode_number;
    private float episode_rating;
    @SuppressWarnings("CanBeFinal")
    private boolean have_actor_writer_detail;
    private boolean episode_heard;
    private boolean episode_downloaded;
    private String actor1;
    private String actor2;
    private String actor3;
    private String actor4;
    private String actor5;
    private String actor6;
    private String writer;
    private String weblink;
    private String download;

    public EpisodeRecyclerViewItem(String title, String airdate, String description, int episodeNumber, float rating, boolean heard, boolean downloaded,
                     String actor1, String actor2, String actor3, String actor4, String actor5, String actor6, String writer, String weblink, String download)
    {
        setTitle(title);
        setAirdate(airdate);
        setDescription(description);
        setEpisodeNumber(episodeNumber);
        setRating(rating);
        have_actor_writer_detail = true;
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

    public EpisodeRecyclerViewItem(String title, String airdate, String description, int episodeNumber, Float rating, boolean heard, boolean downloaded, String weblink, String download) {
        setTitle(title);
        setAirdate(airdate);
        setDescription(description);
        setEpisodeNumber(episodeNumber);
        setRating(rating);
        setHeard(heard);
        setDownloaded(downloaded);
        have_actor_writer_detail = false;
        setActor1(null);
        setActor2(null);
        setActor3(null);
        setActor4(null);
        setActor5(null);
        setActor6(null);
        setWriter(null);
        setWeblink(weblink);
        setDownload(download);
    }

    public static EpisodeRecyclerViewItem fromCursor(Cursor cursor, Context context) {
        EpisodesCursor episodesCursor = new EpisodesCursor(cursor);
        int episodeNumber = (int) episodesCursor.getFieldEpisodeNumber();

        // THE 'HEARD' AND 'DOWNLOADED' FLAGS ARE USED FOR GENERATION OF COLOR-CODED LIST ITEMS
        boolean heard = false;
        boolean downloaded = false;
        if (context instanceof BaseActivity) {
            String episode = String.valueOf(episodeNumber);
            ConfigEpisodesContentValues configEpisodesContentValues = DataHelper.getConfigEpisodeForEpisode(episode);
            ContentValues contentValues = configEpisodesContentValues != null ? configEpisodesContentValues.values() : null;
            heard = contentValues != null ? contentValues.getAsBoolean(RadioTheaterContract.ConfigEpisodesEntry.FIELD_EPISODE_HEARD) : null;
            downloaded = contentValues.getAsBoolean(RadioTheaterContract.ConfigEpisodesEntry.FIELD_EPISODE_DOWNLOADED);
            //LogHelper.v(TAG, "awesome!!! found matching config for episode="+episodeNumber+", heard="+heard+", and downloaded="+downloaded);
        }
        else {
            LogHelper.w(TAG, "unable to find matching config for episode="+episodeNumber+", and downloaded="+downloaded);
        }

        return new EpisodeRecyclerViewItem(
                episodesCursor.getFieldEpisodeTitle(),
                episodesCursor.getFieldAirdate(),
                episodesCursor.getFieldEpisodeDescription(),
                episodeNumber,
                episodesCursor.getFieldRating(),
                heard,
                downloaded,
                Uri.parse(episodesCursor.getFieldWeblinkUrl()).getEncodedPath(),
                Uri.parse("http://" + episodesCursor.getFieldDownloadUrl()).toString());
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.episode_title);
        dest.writeString(this.episode_airdate);
        dest.writeString(this.episode_description);
        dest.writeInt(this.episode_number);
        dest.writeFloat(this.episode_rating);
        dest.writeByte(this.have_actor_writer_detail ? (byte) 1 : (byte) 0);
        dest.writeByte(this.episode_heard ? (byte) 1 : (byte) 0);
        dest.writeByte(this.episode_downloaded ? (byte) 1 : (byte) 0);
        dest.writeString(this.weblink);
        dest.writeString(this.download);
    }

    protected EpisodeRecyclerViewItem(Parcel in) {
        this.episode_title = in.readString();
        this.episode_airdate = in.readString();
        this.episode_description = in.readString();
        this.episode_number = in.readInt();
        this.episode_rating = in.readFloat();
        this.have_actor_writer_detail = in.readByte() != 0;
        this.episode_heard = in.readByte() != 0;
        this.episode_downloaded = in.readByte() != 0;
        this.weblink = in.readString();
        this.download = in.readString();
    }

    public static final Parcelable.Creator<EpisodeRecyclerViewItem> CREATOR = new Parcelable.Creator<EpisodeRecyclerViewItem>() {
        @Override
        public EpisodeRecyclerViewItem createFromParcel(Parcel source) {
            return new EpisodeRecyclerViewItem(source);
        }

        @Override
        public EpisodeRecyclerViewItem[] newArray(int size) {
            return new EpisodeRecyclerViewItem[size];
        }
    };

}
