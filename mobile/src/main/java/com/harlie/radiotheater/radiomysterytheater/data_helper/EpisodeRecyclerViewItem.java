package com.harlie.radiotheater.radiomysterytheater.data_helper;

import android.support.v7.widget.RecyclerView;

public class EpisodeRecyclerViewItem {

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

    public EpisodeRecyclerViewItem(String title, String airdate, String description, int episodeNumber, float rating, boolean heard, boolean downloaded,
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
