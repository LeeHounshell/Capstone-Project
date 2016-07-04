package com.harlie.radiotheater.radiomysterytheater.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import com.harlie.radiotheater.radiomysterytheater.BaseActivity;
import com.harlie.radiotheater.radiomysterytheater.R;
import com.harlie.radiotheater.radiomysterytheater.RadioTheaterApplication;
import com.harlie.radiotheater.radiomysterytheater.data_helper.EpisodeRecyclerViewItem;

public class EpisodeViewHolder extends RecyclerView.ViewHolder {
    private final static String TAG = "LEE: <" + EpisodeViewHolder.class.getSimpleName() + ">";

    public final View mView;
    public TextView mEpisodeNumber;
    public TextView mEpisodeAirdate;
    public TextView mEpisodeTitle;
    public TextView mEpisodeDescription;
    public RatingBar mEpisodeRating;

    public EpisodeRecyclerViewItem mItem;

    public int mTextTitleColor;
    public int mTextDescriptionColor;
    public int mTitleBackgroundColor;
    public int mDescriptionBackgroundColor;

    public EpisodeViewHolder(View itemView) {
        super(itemView);
        Context context = RadioTheaterApplication.getRadioTheaterApplicationContext();
        mView = itemView;
        this.mEpisodeNumber = (TextView) itemView.findViewById(R.id.episode_number) ;
        this.mEpisodeAirdate = (TextView) itemView.findViewById(R.id.episode_airdate) ;
        this.mEpisodeTitle = (TextView) itemView.findViewById(R.id.episode_title) ;
        this.mEpisodeDescription = (TextView) itemView.findViewById(R.id.episode_description) ;
        this.mEpisodeRating = (RatingBar) itemView.findViewById(R.id.episode_rating);
        this.mTextTitleColor = ResourceHelper.getThemeColor(context, R.color.primary_text, 0x212121);
        this.mTitleBackgroundColor = ResourceHelper.getThemeColor(context, R.color.primary_light, 0xd1c4e9);
        this.mTextDescriptionColor = ResourceHelper.getThemeColor(context, R.color.secondary_text, 0x727272);
        this.mDescriptionBackgroundColor = ResourceHelper.getThemeColor(context, R.color.light_grey, 0xbdbdbd);
    }

    public void setSpecialColors() {
        if (mItem != null) {
            if (mView != null && mItem.isHeard()) {
                int color = 0;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    color = mView.getResources().getColor(R.color.grey, null);
                }
                else {
                    color = mView.getResources().getColor(R.color.grey);
                }
                mView.setBackgroundColor(color);
            }
            if (mEpisodeTitle != null && mItem.isDownloaded()) {
                int color = 0;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    color = mView.getResources().getColor(R.color.green, null);
                }
                else {
                    color = mView.getResources().getColor(R.color.green);
                }
                mEpisodeTitle.setTextColor(color);
            }
        }
    }

    public void setFontTypeAndSizes(BaseActivity activity) {
        //LogHelper.v(TAG, "setFontTypeAndSizes");
        FontPreferences fontPreferences = new FontPreferences(activity);
        String fontname = fontPreferences.getFontName();
        String fontsize = fontPreferences.getFontSize();
        LogHelper.v(TAG, "--> USING FONT NAME="+fontname+", SIZE="+fontsize);
        activity.getTheme().applyStyle(fontPreferences.getFontStyle().getResId(), true);

        int[] attrs = {R.attr.font_small, R.attr.font_medium, R.attr.font_large, R.attr.font_xlarge}; // The attributes to retrieve
        TypedArray ta = activity.obtainStyledAttributes(fontPreferences.getFontStyle().getResId(), attrs);
        String str;
        //noinspection ResourceType
        float titleTextSize = 0, descriptionTextSize = 0, airdateTextSize = 0, episodeNumberTextSize = 0;
        //noinspection ResourceType
        str = ta.getString(3);
        if (str != null) {
            titleTextSize = Float.valueOf(str.substring(0, str.length() - 2)); // discard the "sp" part of the style item
        }
        //noinspection ResourceType
        str = ta.getString(2);
        if (str != null) {
            descriptionTextSize = Float.valueOf(str.substring(0, str.length() - 2)); // discard the "sp" part of the style item
        }
        //noinspection ResourceType
        str = ta.getString(1);
        if (str != null) {
            airdateTextSize = Float.valueOf(str.substring(0, str.length() - 2)); // discard the "sp" part of the style item
        }
        //noinspection ResourceType
        str = ta.getString(1);
        if (str != null) {
            episodeNumberTextSize = Float.valueOf(str.substring(0, str.length() - 2)); // discard the "sp" part of the style item
        }
        LogHelper.v(TAG, "=========> titleTextSize="+titleTextSize+", descriptionTextSize="+descriptionTextSize+"," +
                " airdateTextSize="+airdateTextSize+", episodeNumberTextSize="+episodeNumberTextSize);
        ta.recycle();

        mEpisodeTitle.setTextSize(titleTextSize);
        mEpisodeDescription.setTextSize(descriptionTextSize);
        mEpisodeAirdate.setTextSize(airdateTextSize);
        mEpisodeNumber.setTextSize(episodeNumberTextSize);
    }


    @Override
    public String toString() {
        return super.toString() + " '" + mEpisodeTitle + ": " + mEpisodeDescription.getText() + "'";
    }

}
