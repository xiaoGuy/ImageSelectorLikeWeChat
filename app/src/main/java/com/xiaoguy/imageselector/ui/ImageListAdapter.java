package com.xiaoguy.imageselector.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.xiaoguy.imageselector.R;
import com.xiaoguy.imageselector.ui.ImageListAdapter.ImageItemHolder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by XiaoGuy on 2016/10/14.
 */

public class ImageListAdapter extends RecyclerView.Adapter<ImageItemHolder> {

    /**
     * 最多可以选中的图片数量
     */
    private static final int MAX_SELECTED = 9;

    /**
     * 当前图片目录中的图片
     */
    private List<String> mImages;

    /**
     * 选中的图片
     */
    private List<String> mSelectedImages;

    /**
     * 是否显示拍照按钮
     */
    private boolean mCameraEnabled;

    private Context mContext;

    public ImageListAdapter(Context context, List<String> images) {
        mContext = context;
        mImages = images;
        mSelectedImages = new ArrayList<>(MAX_SELECTED);
    }

    public void setCameraEnabled(boolean enabled) {
        mCameraEnabled = enabled;
    }

    @Override
    public ImageItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_image_list, parent, false);
        return new ImageItemHolder(view);
    }

    @Override
    public void onBindViewHolder(final ImageItemHolder holder, final int position) {
        if (mCameraEnabled && position == 0) {
            holder.mImage.setImageResource(R.mipmap.ic_launcher);
            holder.mCheckBox.setVisibility(View.GONE);
            holder.mImage.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            return;
        }

        Glide.with(mContext).
                load(new File(mImages.get(position))).
                placeholder(R.drawable.ic_placeholder).
                error(R.mipmap.ic_launcher).
                into(holder.mImage);

        holder.mImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        holder.mCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                onImageCheckedChanged(holder.mImage, isChecked, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mImages.size();
    }

    private void onImageCheckedChanged(ImageView imageView, boolean isChecked, int position) {
        if (isChecked) {
            imageView.setAlpha(0.5f);
            mSelectedImages.add(mImages.get(position));
        } else {
            imageView.setAlpha(1.0f);
            mSelectedImages.remove(mImages.get(position));
        }
    }

    class ImageItemHolder extends RecyclerView.ViewHolder {

        public ImageView mImage;
        public CheckBox mCheckBox;

        public ImageItemHolder(View itemView) {
            super(itemView);

            mImage = (ImageView) itemView.findViewById(R.id.image);
            mCheckBox = (CheckBox) itemView.findViewById(R.id.checkbox);
        }
    }

    class ImageItemCamera extends RecyclerView.ViewHolder {

        public ImageItemCamera(View itemView) {
            super(itemView);
        }
    }
}
