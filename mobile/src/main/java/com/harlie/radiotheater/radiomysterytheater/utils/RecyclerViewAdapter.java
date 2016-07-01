package com.harlie.radiotheater.radiomysterytheater.utils;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.harlie.radiotheater.radiomysterytheater.BaseActivity;
import com.harlie.radiotheater.radiomysterytheater.EpisodeDetailActivity;
import com.harlie.radiotheater.radiomysterytheater.EpisodeDetailFragment;
import com.harlie.radiotheater.radiomysterytheater.EpisodeListActivity;
import com.harlie.radiotheater.radiomysterytheater.R;
import com.harlie.radiotheater.radiomysterytheater.data.episodes.EpisodesCursor;
import com.harlie.radiotheater.radiomysterytheater.dummy.DummyContent;

import java.text.DecimalFormat;
import java.util.List;

// from: http://stackoverflow.com/questions/32992239/using-a-recyclerview-with-data-from-loadermanager
public class RecyclerViewAdapter
        extends RecyclerView.Adapter<EpisodeViewHolder> {

    private final Context mContext;
    private Cursor mDataCursor;
    private List<DummyContent.DummyItem> mValues;

    public RecyclerViewAdapter(List<DummyContent.DummyItem> items, Context context) {
        mValues = items;
        mContext = context;
    }

    public RecyclerViewAdapter(BaseActivity baseActivity, EpisodesCursor cursor) {
        mContext = baseActivity;
        mDataCursor = cursor;
    }

    @Override
    public EpisodeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.episode_list_content, parent, false);
        return new EpisodeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final EpisodeViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        DecimalFormat format = new DecimalFormat("####");
        int episodeNumber = position + 1;
        String formattedEpisodeNumber = format.format(episodeNumber);
        holder.mEpisodeNumber.setText(formattedEpisodeNumber);
        holder.mEpisodeTitle.setText(mValues.get(position).episode_title);
        holder.mEpisodeDescription.setText(mValues.get(position).episode_description);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mContext instanceof EpisodeListActivity) {
                    EpisodeListActivity episodeListActivity = (EpisodeListActivity) mContext;
                    if (episodeListActivity.isTwoPane()) {
                        Bundle arguments = new Bundle();
                        arguments.putString(EpisodeDetailFragment.ARG_ITEM_ID, holder.mItem.episode_title);
                        EpisodeDetailFragment fragment = new EpisodeDetailFragment();
                        fragment.setArguments(arguments);
                        episodeListActivity.getSupportFragmentManager().beginTransaction()
                                .replace(R.id.episode_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, EpisodeDetailActivity.class);
                        intent.putExtra(EpisodeDetailFragment.ARG_ITEM_ID, holder.mItem.episode_title);
                        Bundle playInfo = new Bundle();
                        episodeListActivity.savePlayInfoToBundle(playInfo);
                        intent.putExtras(playInfo);
                        context.startActivity(intent);
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

}
