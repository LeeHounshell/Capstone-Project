package com.harlie.radiotheater.radiomysterytheater.data_helper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class TheWriters {

    /**
     * photo : "bester_alfred.jpg"
     * name : "Bester, Alfred"
     */

    private String photo;
    private String name;

    public static List<TheWriters> arrayTheWritersFromData(String str) {

        Type listType = new TypeToken<ArrayList<TheWriters>>() {
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

