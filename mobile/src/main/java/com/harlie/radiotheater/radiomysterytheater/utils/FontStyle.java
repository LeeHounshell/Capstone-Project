package com.harlie.radiotheater.radiomysterytheater.utils;

import com.harlie.radiotheater.radiomysterytheater.R;

//from: http://stackoverflow.com/questions/4877153/android-application-wide-font-size-preference/12591991#12591991
public enum FontStyle {

    Small(R.style.FontStyle_Small, "FontStyle.Small"),
    Medium(R.style.FontStyle_Medium, "FontStyle.Medium"),
    Large(R.style.FontStyle_Large, "FontStyle.Large");

    private final int resId;
    private final String title;

    public int getResId() {
        return resId;
    }

    public String getTitle() {
        return title;
    }

    FontStyle(int resId, String title) {
        this.resId = resId;
        this.title = title;
    }

}
