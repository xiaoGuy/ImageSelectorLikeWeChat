package com.xiaoguy.imageselector.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
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

    private static final float SELECTED_ALPHA = 0.5F;
    private static final float UNSELECTED_ALPHA = 1.0F;

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
//        if (mCameraEnabled && position == 0) {
//            holder.mImage.setImageResource(R.mipmap.ic_launcher);
//            holder.mCheckBox.setVisibility(View.GONE);
//            holder.mImage.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//
//                }
//            });
//            return;
//        }

        final String imagePath = mImages.get(position);

        Glide.with(mContext).
                load(new File(imagePath)).
                placeholder(R.drawable.shape_placeholder).
                error(R.mipmap.ic_launcher).
                into(holder.mImage);

        // 还原选中状态
        if (mSelectedImages.contains(imagePath)) {
            holder.mCheckBox.setChecked(true);
        } else {
            holder.mCheckBox.setChecked(false);
        }

        holder.mImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        // 不使用 OnCheckedChangedListener 是避免调用 setChecked() 时触发 onCheckedChanged()
        holder.mCheckBox.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final boolean checked = holder.mCheckBox.isChecked();
                onImageCheckedChanged(holder.mImage, checked, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mImages.size();
    }

    private void onImageCheckedChanged(ImageView imageView, boolean isChecked, int position) {
        if (isChecked) {
            imageView.setAlpha(SELECTED_ALPHA);
            mSelectedImages.add(mImages.get(position));
        } else {
            imageView.setAlpha(UNSELECTED_ALPHA);
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

            // 改变 drawable 的颜色
            Drawable btnDrawable = ContextCompat.getDrawable
                    (mContext, R.drawable.selector_drawable_checkbox);
            ColorStateList colorStateList = ContextCompat.getColorStateList
                    (mContext, R.color.selector_color_checkbox);
            DrawableCompat.setTintList(btnDrawable, colorStateList);
            mCheckBox.setButtonDrawable(btnDrawable);
        }
    }

    class ImageItemCamera extends RecyclerView.ViewHolder {

        public ImageItemCamera(View itemView) {
            super(itemView);
        }
    }
}
