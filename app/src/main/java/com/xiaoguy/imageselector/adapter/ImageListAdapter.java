package com.xiaoguy.imageselector.adapter;

/**
 * Created by XiaoGuy on 2016/10/19.
 */

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.xiaoguy.imageselector.R;
import com.xiaoguy.imageselector.ui.SpecialCheckBox;
import com.xiaoguy.imageselector.util.DrawableUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;

/**
 * 图片列表适配器
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

    private Context mContext;

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

    private OnImageOperateListener mOnImageOperateListener;

    public interface OnImageOperateListener {

        /**
         * 开始选中了图片
         */
        int SELECTED = 0;

        /**
         * 取消选中所有图片
         */
        int CLEARED = 1;

        /**
         * 当点击了拍照按钮时调用
         */
        void onTakePhoto();
        void onImageClick(String path);

        /**
         * 当选择的图片超过最大数量时调用
         * @param max 可以选择的最大数量
         */
        void onImageSelectedOverflow(int max);

        /**
         * 当第一次选中了图片或者取消选中所有图片时调用（可以在该回调中设置发送按钮是否可以点击）
         * @param selectedState {@link #SELECTED} 或者 {@link #CLEARED}
         */
        void onSelectedStateChanged(int selectedState);
    }

    public ImageListAdapter(Context context, List<String> images) {
        mImages = images;
        mSelectedImages = new ArrayList<>(MAX_SELECTED);
    }

    public void setOnImageOperateListener(OnImageOperateListener onImageOperateListener) {
        mOnImageOperateListener = onImageOperateListener;
    }

    public void setCameraEnabled(boolean enabled) {
        mCameraEnabled = enabled;
    }

    public void setImages(List<String> images) {
        mImages = images;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d("size", "onCreateViewHolder");
        if (mContext == null) {
            mContext = parent.getContext();
        }

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
        Log.d("size", "onBindViewHolder");
        if (getItemViewType(position) == TYPE_ITEM_CAMERA) {
            ((ItemCameraHolder) holder).mLayoutCamera.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnImageOperateListener != null) {
                        mOnImageOperateListener.onTakePhoto();
                    }
                }
            });
            return;
        }

        final ItemImageHolder itemImageHolder = (ItemImageHolder) holder;
        final String imagePath = mImages.get(position);

        Glide.with(mContext).
                load(new File(imagePath)).
                placeholder(R.drawable.shape_placeholder).
                error(R.drawable.shape_placeholder).
                centerCrop().
                into(itemImageHolder.mImage);

        // 还原选中状态
        if (mSelectedImages.contains(imagePath)) {
            itemImageHolder.mCheckBox.setChecked(true);
            itemImageHolder.mImage.setAlpha(SELECTED_ALPHA);
        } else {
            itemImageHolder.mCheckBox.setChecked(false);
            itemImageHolder.mImage.setAlpha(UNSELECTED_ALPHA);
        }

        itemImageHolder.mImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnImageOperateListener != null) {
                    mOnImageOperateListener.onImageClick(mImages.get(position));
                }
            }
        });

        // 不使用 OnCheckedChangedListener 是避免调用 setChecked() 时触发 onCheckedChanged()
        itemImageHolder.mCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 使用的是特殊的 CheckBox ，点击以后并不会选中或取消选中，要在 OnClickListener 中
                // 调用 setChecked() 进行设置
                final boolean checked = itemImageHolder.mCheckBox.isChecked();
                // 如果选择数量到达了最大值则不能选中
                if (! checked && mSelectedImages.size() >= MAX_SELECTED) {
                    if (mOnImageOperateListener != null) {
                        mOnImageOperateListener.onImageSelectedOverflow(MAX_SELECTED);
                    }
                    return;
                }
                itemImageHolder.mCheckBox.doToggle();
                onImageCheckedChanged(itemImageHolder.mImage, ! checked, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        Log.d("size", "getItemCount " + mImages.size());
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
            // 第一张照片被选中
            if (mSelectedImages.size() == 1 && mOnImageOperateListener != null) {
                mOnImageOperateListener.onSelectedStateChanged(OnImageOperateListener.SELECTED);
            }
        } else {
            imageView.setAlpha(UNSELECTED_ALPHA);
            mSelectedImages.remove(mImages.get(position));
            // 取消选择了所有照片
            if (mSelectedImages.size() == 0 && mOnImageOperateListener != null) {
                mOnImageOperateListener.onSelectedStateChanged(OnImageOperateListener.CLEARED);
            }
        }
    }

    /**
     * 图片列表中的项
     * item_image_list.xml
     */
    class ItemImageHolder extends RecyclerView.ViewHolder {

        public ImageView mImage;
        public SpecialCheckBox mCheckBox;

        public ItemImageHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(itemView);

            mImage = (ImageView) itemView.findViewById(R.id.image);
            mCheckBox = (SpecialCheckBox) itemView.findViewById(R.id.checkbox);

            mCheckBox.setButtonDrawable(DrawableUtil.setTintList
                    (mContext, R.drawable.selector_checkbox, R.color.selector_color_checkbox));
        }
    }

    /**
     * 图片列表中的拍照按钮
     * item_camera.xml
     */
    class ItemCameraHolder extends RecyclerView.ViewHolder {

        public ViewGroup mLayoutCamera;

        public ItemCameraHolder(View itemView) {
            super(itemView);

            mLayoutCamera = (ViewGroup) itemView;
        }
    }
}