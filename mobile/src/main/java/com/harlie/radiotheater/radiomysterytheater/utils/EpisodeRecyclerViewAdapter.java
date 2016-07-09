package com.harlie.radiotheater.radiomysterytheater.utils;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.TextView;

import com.harlie.radiotheater.radiomysterytheater.BaseActivity;
import com.harlie.radiotheater.radiomysterytheater.CursorRecyclerViewAdapter;
import com.harlie.radiotheater.radiomysterytheater.EpisodeDetailActivity;
import com.harlie.radiotheater.radiomysterytheater.EpisodeDetailFragment;
import com.harlie.radiotheater.radiomysterytheater.EpisodeListActivity;
import com.harlie.radiotheater.radiomysterytheater.R;
import com.harlie.radiotheater.radiomysterytheater.data_helper.EpisodeRecyclerViewItem;

import java.text.DecimalFormat;

// from: http://stackoverflow.com/questions/32992239/using-a-recyclerview-with-data-from-loadermanager
public class EpisodeRecyclerViewAdapter
        extends CursorRecyclerViewAdapter<EpisodeViewHolder>
{
    private final static String TAG = "LEE: <" + EpisodeRecyclerViewAdapter.class.getSimpleName() + ">";

    private final int FADE_DURATION = 2;
    private final Context mContext;
    private Cursor mDataCursor;

    // --- dummy content disabled ---
//  private List<EpisodeRecyclerViewItem> mValues;
//
//  public EpisodeRecyclerViewAdapter(List<EpisodeRecyclerViewItem> items, Context context) {
//      super(context, null);
//      mValues = items;
//      mContext = context;
//  }
    // --- dummy content disabled ---

    public EpisodeRecyclerViewAdapter(BaseActivity baseActivity, Cursor cursor) {
        super(baseActivity, cursor);
        mContext = baseActivity;
        mDataCursor = cursor;
        setHasStableIds(true);
    }

    @Override
    public EpisodeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.episode_list_content, parent, false);
        TextView episode_description = (TextView) view.findViewById(R.id.episode_description);
        if (! EpisodeListActivity.isTwoPane()
                || parent.getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
        {
            episode_description.setVisibility(View.VISIBLE);
        }
        else {
            episode_description.setVisibility(View.GONE);
        }
        EpisodeViewHolder holder = new EpisodeViewHolder(view);
        holder.setIsRecyclable(false); // stop the special colors from changing during scroll
        holder.setSpecialColors();
        return holder;
    }

    @Override
    public void onBindViewHolder(EpisodeViewHolder holder, Cursor cursor) {
        holder.mItem = EpisodeRecyclerViewItem.fromCursor(cursor, mContext);
        DecimalFormat format = new DecimalFormat("####");
        int episodeNumber = holder.mItem.getEpisodeNumber();
        String formattedEpisodeNumber = format.format(episodeNumber);
        holder.mEpisodeNumber.setText(formattedEpisodeNumber);
        holder.mEpisodeTitle.setText(holder.mItem.getTitle());
        holder.mEpisodeDescription.setText(holder.mItem.getDescription());
        holder.mEpisodeRating.setRating(holder.mItem.getRating());
        BaseActivity activity = (BaseActivity) mContext;
        holder.setFontTypeAndSizes(activity);
        holder.setIsRecyclable(false); // stop the special colors from changing during scroll
        holder.setSpecialColors();
        holderSetOnClickListener(holder);
        setFadeAnimation(holder.mView);
    }

    // --- dummy content disabled ---
//    @Override
//    public void onBindViewHolder(final EpisodeViewHolder holder, int position) {
//        holder.mItem = mValues.get(position);
//        DecimalFormat format = new DecimalFormat("####");
//        int episodeNumber = position + 1;
//        String formattedEpisodeNumber = format.format(episodeNumber);
//        holder.mEpisodeNumber.setText(formattedEpisodeNumber);
//        holder.mEpisodeTitle.setText(mValues.get(position).episode_title);
//        holder.mEpisodeDescription.setText(mValues.get(position).episode_description);
//        holderSetOnClickListener(holder);
//        setFadeAnimation(holder.mItem);
//    }
    // --- dummy content disabled ---

    // animate the list items using a transition animation
    private void setFadeAnimation(View view) {
        AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(FADE_DURATION);
        view.startAnimation(anim);
    }

    private void holderSetOnClickListener(final EpisodeViewHolder holder) {
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mContext instanceof EpisodeListActivity) {
                    EpisodeListActivity episodeListActivity = (EpisodeListActivity) mContext;
                    if (! EpisodeListActivity.isTwoPane()
                            || v.getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
                    {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, EpisodeDetailActivity.class);
                        intent.putExtra(EpisodeDetailFragment.ARG_EPISODE_ID, String.valueOf(holder.mItem.getEpisodeNumber()));
                        intent.putExtra(EpisodeDetailFragment.ARG_EPISODE_PARCELABLE, holder.mItem);
                        LogHelper.v(TAG, "-NEW- ARG_EPISODE_ID="+holder.mItem.getEpisodeNumber());
                        Bundle playInfo = new Bundle();
                        episodeListActivity.savePlayInfoToBundle(playInfo);
                        intent.putExtras(playInfo);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);

                    } else {

                        Bundle arguments = new Bundle();
                        arguments.putString(EpisodeDetailFragment.ARG_EPISODE_ID, String.valueOf(holder.mItem.getEpisodeNumber()));
                        arguments.putParcelable(EpisodeDetailFragment.ARG_EPISODE_PARCELABLE, holder.mItem);
                        LogHelper.v(TAG, "-NEW- ARG_EPISODE_ID="+holder.mItem.getEpisodeNumber());
                        EpisodeDetailFragment fragment = new EpisodeDetailFragment();
                        fragment.setArguments(arguments);
                        episodeListActivity.getSupportFragmentManager().beginTransaction()
                                .replace(R.id.episode_detail_container, fragment)
                                .commit();

                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mDataCursor != null) {
            return mDataCursor.getCount();
        }
        else {
            return 0;
        }

        // --- dummy content disabled ---
        //      return mValues.size();
        // --- dummy content disabled ---
    }

    @Override
    public void onViewRecycled(EpisodeViewHolder holder) {
        LogHelper.v(TAG, "onViewRecycled");
        super.onViewRecycled(holder);
        holder.setNormalColors();
    }

}
