package com.xiaoguy.imageselector.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.xiaoguy.imageselector.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by XiaoGuy on 2016/10/14.
 */

public class ImageListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final float SELECTED_ALPHA = 0.5F;
    private static final float UNSELECTED_ALPHA = 1.0F;
    private static final int TYPE_ITEM_CAMERA = 0;
    private static final int TYPE_ITEM_IMAGE  = 1;

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
    private boolean mCameraEnabled = true;

    private Context mContext;
    private Toast mToast;

    public ImageListAdapter(Context context, List<String> images) {
        mContext = context;
        mImages = images;
        mSelectedImages = new ArrayList<>(MAX_SELECTED);
    }

    public void setCameraEnabled(boolean enabled) {
        mCameraEnabled = enabled;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM_IMAGE) {
            return new ItemImageHolder(LayoutInflater.from(mContext).inflate
                    (R.layout.item_image_list, parent, false));
        } else {
            return new ItemCameraHolder(LayoutInflater.from(mContext).inflate
                    (R.layout.item_camera, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        if (getItemViewType(position) == TYPE_ITEM_CAMERA) {
            ((ItemCameraHolder) holder).mLayoutCamera.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            return;
        }

        final ItemImageHolder itemImageHolder = (ItemImageHolder) holder;
        final String imagePath = mImages.get(position);

        Glide.with(mContext).
                load(new File(imagePath)).
                placeholder(R.drawable.shape_placeholder).
                error(R.mipmap.ic_launcher).
                into(itemImageHolder.mImage);

        // 还原选中状态
        if (mSelectedImages.contains(imagePath)) {
            itemImageHolder.mCheckBox.setChecked(true);
            itemImageHolder.mImage.setAlpha(SELECTED_ALPHA);
        } else {
            itemImageHolder.mCheckBox.setChecked(false);
            itemImageHolder.mImage.setAlpha(UNSELECTED_ALPHA);
        }

        itemImageHolder.mImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        // 不使用 OnCheckedChangedListener 是避免调用 setChecked() 时触发 onCheckedChanged()
        itemImageHolder.mCheckBox.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 使用的是特殊的 CheckBox ，点击以后并不会选中或取消选中，要在 OnClickListener 中
                // 调用 setChecked() 进行设置
                final boolean checked = itemImageHolder.mCheckBox.isChecked();
                // 如果选择数量到达了最大值则不能选中
                if (! checked && mSelectedImages.size() >= MAX_SELECTED) {
                    if (mToast == null) {
                        String hint = mContext.getResources().getString
                                (R.string.max_selected, MAX_SELECTED);
                        mToast = Toast.makeText(mContext, hint, Toast.LENGTH_SHORT);
                    }
                    mToast.show();
                    return;
                }
                itemImageHolder.mCheckBox.doToggle();
                onImageCheckedChanged(itemImageHolder.mImage, ! checked, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mImages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (mCameraEnabled && position == 0) {
            return TYPE_ITEM_CAMERA;
        }
        return TYPE_ITEM_IMAGE;
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

    class ItemImageHolder extends RecyclerView.ViewHolder {

        public ImageView mImage;
        public SpecialCheckBox mCheckBox;

        public ItemImageHolder(View itemView) {
            super(itemView);

            mImage = (ImageView) itemView.findViewById(R.id.image);
            mCheckBox = (SpecialCheckBox) itemView.findViewById(R.id.checkbox);

            // 改变 drawable 的颜色
            Drawable btnDrawable = ContextCompat.getDrawable
                    (mContext, R.drawable.selector_drawable_checkbox);
            ColorStateList colorStateList = ContextCompat.getColorStateList
                    (mContext, R.color.selector_color_checkbox);
            DrawableCompat.setTintList(btnDrawable, colorStateList);
            mCheckBox.setButtonDrawable(btnDrawable);
        }
    }

    class ItemCameraHolder extends RecyclerView.ViewHolder {

        public ViewGroup mLayoutCamera;

        public ItemCameraHolder(View itemView) {
            super(itemView);

            mLayoutCamera = (ViewGroup) itemView;
        }
    }
}
