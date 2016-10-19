package com.xiaoguy.imageselector.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.xiaoguy.imageselector.R;
import com.xiaoguy.imageselector.adapter.ImageFolderListAdapter.ImageFolderHolder;
import com.xiaoguy.imageselector.bean.ImageFolder;
import com.xiaoguy.imageselector.util.DrawableUtil;

import java.io.File;
import java.util.List;

import butterknife.ButterKnife;

/**
 * Created by XiaoGuy on 2016/10/19.
 */

public class ImageFolderListAdapter extends RecyclerView.Adapter<ImageFolderHolder> {

    private static final String PIECE = " 张";

    private Context mContext;
    private List<ImageFolder> mImageFolders;

    /**
     * 记录当前的目录
     */
    private int mCurrentFolder;

    private OnImageFolderClickListener mOnImageFolderClickListener;

    public interface OnImageFolderClickListener {
        /**
         * 当图片目录被点击时调用
         * @param images 被点击的目录下的图片
         * @param folderName 被点击的目录的名字
         */
        void onImageFolderClick(List<String> images, String folderName);
    }

    public void setOnImageFolderClickListener(OnImageFolderClickListener onImageFolderClickListener) {
        mOnImageFolderClickListener = onImageFolderClickListener;
    }

    public ImageFolderListAdapter(List<ImageFolder> imageFolders) {
        mImageFolders = imageFolders;
    }

    @Override
    public ImageFolderHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mContext == null) {
            mContext = parent.getContext();
        }

        View view = LayoutInflater.from(parent.getContext()).inflate
                (R.layout.item_image_folder, parent, false);
        return new ImageFolderHolder(view);
    }

    @Override
    public void onBindViewHolder(ImageFolderHolder holder, final int position) {
        final ImageFolder imageFolder = mImageFolders.get(position);

        Glide.with(mContext).
              load(new File(imageFolder.getFirstImagePath())).
              placeholder(R.drawable.shape_placeholder).
              error(R.drawable.shape_placeholder).
              centerCrop().
              into(holder.mImageView);

        holder.mTextFolderName.setText(imageFolder.getName());
        holder.mTextImageCount.setText(getItemCount() + PIECE);

        holder.mItem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnImageFolderClickListener != null) {
                    mOnImageFolderClickListener.
                            onImageFolderClick(imageFolder.getImagePaths(), imageFolder.getName());
                }
                mCurrentFolder = position;
            }
        });

        // 还原选中的状态
        if (mCurrentFolder == position) {
            holder.mImageSelected.setVisibility(View.VISIBLE);
        } else {
            holder.mImageSelected.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mImageFolders.size();
    }

    /**
     * layout_item_image_folder.xml
     */
    class ImageFolderHolder extends ViewHolder {

        public View mItem;
        public ImageView mImageView;
        public ImageView mImageSelected;
        public TextView mTextFolderName;
        public TextView mTextImageCount;

        public ImageFolderHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(itemView);

            mItem = itemView;
            mImageView = (ImageView) mItem.findViewById(R.id.image_first);
            mImageSelected = (ImageView) mItem.findViewById(R.id.image_selected);
            mTextFolderName = (TextView) mItem.findViewById(R.id.text_folderName);
            mTextImageCount = (TextView) mItem.findViewById(R.id.text_imageCount);

            mImageSelected.setImageDrawable(DrawableUtil.setTintList
                    (mContext, R.drawable.ic_selected, R.color.green));
        }
    }
}
