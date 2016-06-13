package com.harlie.radiotheater.radiomysterytheater.data_helper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class TheEpisodes {

    /**
     * rating : 3.2
     * description : An old lady rents a room to a sick boarder. She runs into problems with his strange deathbed confession.
     * airdate : 1974-01-06
     * weblink : www.cbsrmt.com/episode_name-1-the-old-ones-are-hard-to-kill.html
     * episode_name : The Old Ones Are Hard to Kill
     * writers : {"photo":"slesar_henry.jpg","name":"Slesar, Henry"}
     * actors : {"photo":["dekoven_roger.jpg","janney_leon.jpg","moorehead_agnes.jpg"],"name":["DeKoven, Roger","Janney, Leon","Moorehead, Agnes"]}
     * download : www.cbsrmt.com/mp3/CBS Radio Mystery Theater 74-01-06 e0001 The Old Ones Are Hard to Kill.mp3
     * episode_number : 0001
     */

    private String rating;
    private String description;
    private String airdate;
    private String weblink;
    private String episode_name;
    /**
     * photo : slesar_henry.jpg
     * name : Slesar, Henry
     */

    private WritersBean writers;
    private ActorsBean actors;
    private String download;
    private String episode_number;

    public static List<TheEpisodes> arrayTheEpisodesFromData(String str) {

        Type listType = new TypeToken<ArrayList<TheEpisodes>>() {
        }.getType();

        return new Gson().fromJson(str, listType);
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
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

    public String getWeblink() {
        return weblink;
    }

    public void setWeblink(String weblink) {
        this.weblink = weblink;
    }

    public String getEpisode_name() {
        return episode_name;
    }

    public void setEpisode_name(String episode_name) {
        this.episode_name = episode_name;
    }

    public WritersBean getWriters() {
        return writers;
    }

    public void setWriters(WritersBean writers) {
        this.writers = writers;
    }

    public ActorsBean getActors() {
        return actors;
    }

    public void setActors(ActorsBean actors) {
        this.actors = actors;
    }

    public String getDownload() {
        return download;
    }

    public void setDownload(String download) {
        this.download = download;
    }

    public String getEpisode_number() {
        return episode_number;
    }

    public void setEpisode_number(String episode_number) {
        this.episode_number = episode_number;
    }

    public static class WritersBean {
        private String photo;
        private String name;

        public static List<WritersBean> arrayWritersBeanFromData(String str) {

            Type listType = new TypeToken<ArrayList<WritersBean>>() {
            }.getType();

            return new Gson().fromJson(str, listType);
        }

        public String getPhoto() {
            return photo;
        }

        public void setPhoto(String photo) {
            this.photo = photo;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class ActorsBean {
        private List<String> photo;
        private List<String> name;

        public static List<ActorsBean> arrayActorsBeanFromData(String str) {

            Type listType = new TypeToken<ArrayList<ActorsBean>>() {
            }.getType();

            return new Gson().fromJson(str, listType);
        }

        public List<String> getPhoto() {
            return photo;
        }

        public void setPhoto(List<String> photo) {
            this.photo = photo;
        }

        public List<String> getName() {
            return name;
        }

        public void setName(List<String> name) {
            this.name = name;
        }
    }
}
