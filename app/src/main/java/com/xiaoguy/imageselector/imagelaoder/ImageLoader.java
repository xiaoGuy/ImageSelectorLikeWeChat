package com.xiaoguy.imageselector.imagelaoder;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore.Images.Media;
import android.support.annotation.UiThread;
import android.util.Log;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by XiaoGuy on 2016/10/8.
 */

public class ImageLoader {

    private static final String SELECTION  = Media.MIME_TYPE + " = ? or " + Media.MIME_TYPE + " = ?";
    private static final String[] SELECTION_ARGS = {"image/jpeg", "image/png"};
    private static final int REQUEST_SDCARD = 0;

    private static ImageLoader mImageLoader;

    private Context mContext;
    private List<ImageFolder> allImageFolders = new ArrayList<>();
    private List<String> allImagePath = new ArrayList<>();
    private Handler mHandler;

    public interface OnLoadFinishListener {

        void onLoadFinish(List<ImageFolder> allImageFolders, List<String> allImagePath);
    }

    private ImageLoader(Context context)  {
        if (! (context instanceof Activity)) {
            throw new IllegalArgumentException("Context must be instance of Activity");
        }
        mContext = context;
    }

    public static ImageLoader getInstance(Context context) {
        if (mImageLoader == null) {
            synchronized (ImageLoader.class) {
                if (mImageLoader == null) {
                    mImageLoader = new ImageLoader(context);
                }
            }
        }
        return mImageLoader;
    }

    @UiThread
    public void loadImages(final OnLoadFinishListener listener) {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                listener.onLoadFinish(allImageFolders, allImagePath);
            }
        };
        new Thread(new Runnable() {
            @Override
            public void run() {
                scanImages();
            }
        }).start();
    }

    /**
     * 扫描手机中的所有的图片
     */
    private void scanImages() {
        Set<String> imageFolderPathSet = new HashSet<>();

        Cursor cursor = mContext.getContentResolver().query(
                Media.EXTERNAL_CONTENT_URI, new String[]{
                Media.DATA, Media.DATE_MODIFIED}, SELECTION,
                SELECTION_ARGS, Media.DATE_MODIFIED + " DESC");

        while (cursor.moveToNext()) {
            final String currentImagePath = cursor.getString(cursor.getColumnIndex(Media.DATA));
            Log.d("size", currentImagePath);
            // 图片所属的目录
            File parentFile = new File(currentImagePath).getParentFile();
            if (parentFile == null) {
                continue;
            }
            ImageFolder imageFolder = null;
            // 已经遍历过该目录
            if (imageFolderPathSet.contains(parentFile.getAbsolutePath().toLowerCase())) {
                continue;
            } else {
                // 有些路径名一样只是大小写不同
                imageFolderPathSet.add(parentFile.getAbsolutePath().toLowerCase());
                imageFolder = new ImageFolder();
                imageFolder.setName(parentFile.getName());
                imageFolder.setPath(parentFile.getAbsolutePath());
                imageFolder.setFirstImagePath(currentImagePath);
            }
            final File[] children = parentFile.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    // 有些图片的后缀是大写
                    String name = pathname.getName().toLowerCase();
                    if (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg")) {
                        return true;
                    }
                    return false;
                }
            });
            List<String> imagePathList = null;
            // 无法获取到该目录下的图片，但是该目录下至少存在一张图片
            if (children == null) {
                imagePathList = new ArrayList<>(1);
                imagePathList.add(currentImagePath);
                imageFolder.setImageCount(1);
                imageFolder.setImagePaths(imagePathList);
                allImageFolders.add(imageFolder);
                allImagePath.add(currentImagePath);
                continue;
            }
            final int imageCount = children.length;
            imagePathList = new ArrayList<>(imageCount);
            for (File child : children) {
                imagePathList.add(child.getAbsolutePath());
            }
            imageFolder.setImageCount(imageCount);
            imageFolder.setImagePaths(imagePathList);
            allImageFolders.add(imageFolder);
            allImagePath.addAll(imagePathList);
        }

        cursor.close();
        mHandler.sendEmptyMessage(0);
    }

}
