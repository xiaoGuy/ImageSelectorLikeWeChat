package com.xiaoguy.imageselector.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.xiaoguy.imageselector.R;
import com.xiaoguy.imageselector.adapter.SentListAdapter.ItemSentHolder;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by XiaoGuy on 2016/10/24.
 */

public class SentListAdapter extends RecyclerView.Adapter<ItemSentHolder> {

    private Context mContext;
    private ArrayList<String> mSentImages;

    public SentListAdapter(ArrayList<String> sentImages) {
        mSentImages = sentImages;
    }

    @Override
    public ItemSentHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mContext == null) {
            mContext = parent.getContext();
        }

        View view = LayoutInflater.from(mContext).inflate(R.layout.adapter_item_sent, parent, false);
        return new ItemSentHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemSentHolder holder, int position) {
        final String path = mSentImages.get(position);

        Glide.with(mContext).
                load(new File(path)).
                centerCrop().
                placeholder(R.drawable.shape_placeholder).
                error(R.drawable.shape_placeholder).
                thumbnail(0.3f).
                into(holder.mImageView);

        holder.mTextView.setText(path);
    }

    @Override
    public int getItemCount() {
        return mSentImages.size();
    }

    class ItemSentHolder extends RecyclerView.ViewHolder {

        public ImageView mImageView;
        public TextView mTextView;

        public ItemSentHolder(View itemView) {
            super(itemView);

            mImageView = (ImageView) itemView.findViewById(R.id.image_sent);
            mTextView = (TextView) itemView.findViewById(R.id.text_sent);
        }
    }
}
