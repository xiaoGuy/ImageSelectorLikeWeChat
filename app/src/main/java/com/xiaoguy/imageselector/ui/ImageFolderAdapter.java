package com.xiaoguy.imageselector.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.xiaoguy.imageselector.R;
import com.xiaoguy.imageselector.ui.ImageFolderAdapter.ImageFolderHolder;
import com.xiaoguy.imageselector.bean.ImageFolder;

import java.io.File;
import java.util.List;

/**
 * Created by XiaoGuy on 2016/10/14.
 */

public class ImageFolderAdapter extends RecyclerView.Adapter<ImageFolderHolder> {

    private List<ImageFolder> mImageFolders;
    private Context mContext;

    public ImageFolderAdapter(Context context, List<ImageFolder> imageFolders) {
        mImageFolders = imageFolders;
        mContext = context;
    }

    @Override
    public ImageFolderHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from
                (parent.getContext()).inflate(R.layout.item_image_folder, parent, false);
        return new ImageFolderHolder(view);
    }

    @Override
    public void onBindViewHolder(ImageFolderHolder holder, int position) {
        final ImageFolder imageFolder = mImageFolders.get(position);

        Glide.with(mContext).
              load(new File(imageFolder.getFirstImagePath())).
              into(holder.mImageFirst);

        holder.mTextFolderName.setText(imageFolder.getName());
        holder.mTextImageCount.setText(imageFolder.getImageCount() + "张");
    }

    @Override
    public int getItemCount() {
        return mImageFolders.size();
    }

    public class ImageFolderHolder extends RecyclerView.ViewHolder {

        public ImageView mImageFirst;
        public TextView mTextFolderName;
        public TextView mTextImageCount;

        public ImageFolderHolder(View itemView) {
            super(itemView);

            mImageFirst = (ImageView) itemView.findViewById(R.id.image_first);
            mTextFolderName = (TextView) itemView.findViewById(R.id.text_folderName);
            mTextImageCount = (TextView) itemView.findViewById(R.id.text_imageCount);
        }
    }
}
