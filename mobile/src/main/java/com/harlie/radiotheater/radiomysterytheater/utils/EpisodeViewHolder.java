package com.harlie.radiotheater.radiomysterytheater.utils;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import com.harlie.radiotheater.radiomysterytheater.R;
import com.harlie.radiotheater.radiomysterytheater.RadioTheaterApplication;
import com.harlie.radiotheater.radiomysterytheater.dummy.DummyContent;

public class EpisodeViewHolder extends RecyclerView.ViewHolder {

    public final View mView;
    public TextView mEpisodeNumber;
    public TextView mEpisodeAirdate;
    public TextView mEpisodeTitle;
    public TextView mEpisodeDescription;
    public RatingBar mEpisodeRating;
    public DummyContent.DummyItem mItem;

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

    @Override
    public String toString() {
        return super.toString() + " '" + mEpisodeTitle + ": " + mEpisodeDescription.getText() + "'";
    }

}
