package com.harlie.radiotheater.radiomysterytheater.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import com.harlie.radiotheater.radiomysterytheater.BaseActivity;
import com.harlie.radiotheater.radiomysterytheater.EpisodeListActivity;
import com.harlie.radiotheater.radiomysterytheater.R;
import com.harlie.radiotheater.radiomysterytheater.RadioTheaterApplication;
import com.harlie.radiotheater.radiomysterytheater.data_helper.EpisodeRecyclerViewItem;

public class EpisodeViewHolder extends RecyclerView.ViewHolder {
    private final static String TAG = "LEE: <" + EpisodeViewHolder.class.getSimpleName() + ">";

    public final View mView;
    public final TextView mEpisodeNumber;
    public final TextView mEpisodeAirdate;
    public final TextView mEpisodeTitle;
    public final TextView mEpisodeDescription;
    public final RatingBar mEpisodeRating;

    public EpisodeRecyclerViewItem mItem;

    public final int mTextTitleColor;
    public final int mTextDescriptionColor;
    public final int mTitleBackgroundColor;
    public final int mDescriptionBackgroundColor;

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

    public void setNormalColors() {
        if (mItem != null) {
            if (mView != null && mItem.isHeard()) {
                int color;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    color = mView.getResources().getColor(R.color.primary_light, null);
                }
                else {
                    //noinspection deprecation
                    color = mView.getResources().getColor(R.color.primary_light);
                }
                mView.setBackgroundColor(color);
            }
            if (mEpisodeTitle != null && mItem.isDownloaded()) {
                int color;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    color = mView != null ? mView.getResources().getColor(R.color.list_episode_title_color, null) : 0;
                }
                else {
                    //noinspection deprecation
                    color = mView != null ? mView.getResources().getColor(R.color.list_episode_title_color) : 0;
                }
                mEpisodeTitle.setTextColor(color);
            }
        }
    }

    public void setSpecialColors() {
        if (mItem != null) {
            if (mView != null && mItem.isHeard()) {
                int color;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    color = mView.getResources().getColor(R.color.grey, null);
                }
                else {
                    //noinspection deprecation
                    color = mView.getResources().getColor(R.color.grey);
                }
                mView.setBackgroundColor(color);
            }
            if (mEpisodeTitle != null && mItem.isDownloaded()) {
                int color;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    color = mView != null ? mView.getResources().getColor(R.color.green, null) : 0;
                }
                else {
                    //noinspection deprecation
                    color = mView != null ? mView.getResources().getColor(R.color.green) : 0;
                }
                mEpisodeTitle.setTextColor(color);
            }
        }
    }

    static final int LIST_SCALE_AMT = 3;
    public void setFontTypeAndSizes(BaseActivity activity) {
        //LogHelper.v(TAG, "setFontTypeAndSizes");
        FontPreferences fontPreferences = new FontPreferences(activity);
        String fontname = fontPreferences.getFontName();
        String fontsize = fontPreferences.getFontSize();
        LogHelper.v(TAG, "--> USING FONT NAME="+fontname+", SIZE="+fontsize);
        activity.getTheme().applyStyle(fontPreferences.getFontStyle().getResId(), true);

        int[] attrs = {R.attr.font_small, R.attr.font_medium, R.attr.font_large, R.attr.font_xlarge}; // The attributes to retrieve
        TypedArray ta = activity.obtainStyledAttributes(fontPreferences.getFontStyle().getResId(), attrs);
        //noinspection ResourceType
        float titleTextSize, descriptionTextSize, airdateTextSize, episodeNumberTextSize;
        if (! EpisodeListActivity.isTwoPane()
                || activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
        {
            titleTextSize = getTextSize(ta, 2) + LIST_SCALE_AMT + 5;
            descriptionTextSize = getTextSize(ta, 1) + LIST_SCALE_AMT + 3;
            airdateTextSize = getTextSize(ta, 1) + LIST_SCALE_AMT + 1;
            episodeNumberTextSize = getTextSize(ta, 1) + LIST_SCALE_AMT + 1;
        }
        else {
            titleTextSize = getTextSize(ta, 1) + LIST_SCALE_AMT + 4;
            descriptionTextSize = getTextSize(ta, 0) + LIST_SCALE_AMT + 2;
            airdateTextSize = getTextSize(ta, 0) + LIST_SCALE_AMT;
            episodeNumberTextSize = getTextSize(ta, 0) + LIST_SCALE_AMT;
        }

        LogHelper.v(TAG, "=========> titleTextSize="+titleTextSize+", descriptionTextSize="+descriptionTextSize+"," +
                " airdateTextSize="+airdateTextSize+", episodeNumberTextSize="+episodeNumberTextSize);

        ta.recycle();

        if (mEpisodeTitle != null) {
            mEpisodeTitle.setTextSize(titleTextSize);
        }
        if (mEpisodeDescription != null) {
            mEpisodeDescription.setTextSize(descriptionTextSize);
        }
        if (mEpisodeAirdate != null) {
            mEpisodeAirdate.setTextSize(airdateTextSize);
        }
        if (mEpisodeNumber != null) {
            mEpisodeNumber.setTextSize(episodeNumberTextSize);
        }
    }

    private float getTextSize(TypedArray ta, int index) {
        float textSize = 0;
        String str;//noinspection ResourceType
        str = ta.getString(index);
        if (str != null) {
            textSize = Float.valueOf(str.substring(0, str.length() - 2)); // discard the "sp" part of the style item
        }
        return textSize;
    }

    @Override
    public String toString() {
        return super.toString() + " '" + mEpisodeTitle + ": " + mEpisodeDescription.getText() + "'";
    }

}
