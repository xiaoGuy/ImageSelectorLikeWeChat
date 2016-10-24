package com.xiaoguy.imageselector.adapter;

/**
 * Created by XiaoGuy on 2016/10/19.
 */

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
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

    private static final String TAG = ImageListAdapter.class.getSimpleName();

    private static final float SELECTED_ALPHA = 0.5F;
    private static final float UNSELECTED_ALPHA = 1.0F;
    private static final int TYPE_ITEM_CAMERA = 649;
    private static final int TYPE_ITEM_IMAGE = 86;

    /**
     * 最多可以选中的图片数量
     */
    private static final int DEFAULT_MAX_SELECTED_SIZE = 9;

    private Context mContext;

    /**
     * 当前图片目录中的图片
     */
    private ArrayList<String> mImages;

    /**
     * 选中的图片
     */
    private ArrayList<String> mSelectedImages;

    /**
     * 是否显示拍照按钮
     */
    private boolean mCameraEnabled = true;
    private int mMaxSelectedSize = DEFAULT_MAX_SELECTED_SIZE;

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
        void onImageClick(int position, ArrayList<String> images, ArrayList<String> selectedImages);

        /**
         * 当选择的图片超过最大数量时调用
         * @param max 可以选择的最大数量
         */
        void onImageSelectedOverflow(int max);

        /**
         * 当图片被选中或者取消选中时调用
         */
        void onImageSelected(List<String> selectedImages);
    }

    public ImageListAdapter(Context context, ArrayList<String> images) {
        mImages = images;
        mSelectedImages = new ArrayList<>(mMaxSelectedSize);
    }

    public void setOnImageOperateListener(OnImageOperateListener onImageOperateListener) {
        mOnImageOperateListener = onImageOperateListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mContext == null) {
            mContext = parent.getContext();
        }

        if (viewType == TYPE_ITEM_IMAGE) {
            return new ItemImageHolder(LayoutInflater.from(mContext).inflate
                    (R.layout.adapter_item_image_list, parent, false));
        } else {
            return new ItemCameraHolder(LayoutInflater.from(mContext).inflate
                    (R.layout.adapter_item_camera, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
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

        String imagePath;
        // 拍照按钮占据了第一张图片的位置
        if (mCameraEnabled) {
            imagePath = mImages.get(position - 1);
        } else {
            imagePath = mImages.get(position);
        }
        final ItemImageHolder itemImageHolder = (ItemImageHolder) holder;

        Glide.with(mContext).
                load(new File(imagePath)).
                placeholder(R.drawable.shape_placeholder).
                error(R.drawable.shape_placeholder).
                centerCrop().
                thumbnail(0.1f).
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
                    if (mCameraEnabled) {
                        mOnImageOperateListener.onImageClick(position - 1, mImages, mSelectedImages);
                    } else {
                        mOnImageOperateListener.onImageClick(position, mImages, mSelectedImages);
                    }
                }
            }
        });

        // 不使用 OnCheckedChangedListener 是避免调用 setChecked() 时触发 onCheckedChanged()
        itemImageHolder.mCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 使用的是 SpecialCheckBox ，点击以后并不会选中或取消选中，只能通过 setChecked()
                final boolean checked = itemImageHolder.mCheckBox.isChecked();
                // 如果选择数量到达了最大值则不能选中
                if (! checked && mSelectedImages.size() >= mMaxSelectedSize) {
                    if (mOnImageOperateListener != null) {
                        mOnImageOperateListener.onImageSelectedOverflow(mMaxSelectedSize);
                    }
                    return;
                }
                itemImageHolder.mCheckBox.doToggle();
                if (mCameraEnabled) {
                    onImageCheckedChanged(itemImageHolder.mImage, ! checked, position - 1);
                } else {
                    onImageCheckedChanged(itemImageHolder.mImage, ! checked, position);   
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mCameraEnabled ? mImages.size() + 1 : mImages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (mCameraEnabled && position == 0) {
            return TYPE_ITEM_CAMERA;
        }
        return TYPE_ITEM_IMAGE;
    }

    public void setMaxSelected(int max) {
        mMaxSelectedSize = max;
    }

    public int getMaxSelected() {
        return mMaxSelectedSize;
    }

    public void setCameraEnabled(boolean enabled) {
        mCameraEnabled = enabled;
    }

    public void setImages(ArrayList<String> images) {
        mImages = images;
        notifyDataSetChanged();
    }

    public ArrayList<String> getImages() {
        return mImages;
    }

    public void updateSelectedImages(ArrayList<String> selectedImages) {
        mSelectedImages = selectedImages;
        notifyDataSetChanged();
    }

    public void clearSelectedImages() {
        mSelectedImages.clear();
        if (mOnImageOperateListener != null) {
            mOnImageOperateListener.onImageSelected(mSelectedImages);
        }
        notifyDataSetChanged();
    }

    public ArrayList<String> getSelectedImages() {
        return mSelectedImages;
    }

    private void onImageCheckedChanged(ImageView imageView, boolean isChecked, int position) {
        if (isChecked) {
            imageView.setAlpha(SELECTED_ALPHA);
            mSelectedImages.add(mImages.get(position));
        } else {
            imageView.setAlpha(UNSELECTED_ALPHA);
            mSelectedImages.remove(mImages.get(position));
        }

        if (mOnImageOperateListener != null) {
            mOnImageOperateListener.onImageSelected(mSelectedImages);
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

            mImage = (ImageView) itemView.findViewById(R.id.image_sent);
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