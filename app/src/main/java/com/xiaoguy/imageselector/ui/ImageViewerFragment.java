package com.xiaoguy.imageselector.ui;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.xiaoguy.imageselector.R;
import com.xiaoguy.imageselector.activity.ImageViewerActivity;
import com.xiaoguy.imageselector.util.ScreenUtil;

import java.io.File;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher.OnPhotoTapListener;

/**
 * Created by XiaoGuy on 2016/10/22.
 */

public class ImageViewerFragment extends Fragment {

    private static final String PATH = "path";

    /**
     * 图片的路径
     */
    private String mPath;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPath = getArguments() != null ? getArguments().getString(PATH) : null;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image_viewer, container, false);
        final PhotoView photoView = (PhotoView) view.findViewById(R.id.photoView);

        if (mPath == null) {
            photoView.setImageResource(R.drawable.shape_placeholder);
            return view;
        }

        Glide.with(this).
                load(new File(mPath)).
                asBitmap().
                error(R.drawable.shape_placeholder).
                into(new SimpleTarget<Bitmap>(ScreenUtil.getScreenWidth(getActivity()),
                        ScreenUtil.getScreenHeight(getActivity())) {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        photoView.setImageBitmap(resource);
                    }
                });

        photoView.setOnPhotoTapListener(new OnPhotoTapListener() {
            @Override
            public void onPhotoTap(View view, float x, float y) {
                Activity activity = getActivity();
                if (activity instanceof ImageViewerActivity) {
                    ((ImageViewerActivity) activity).toggleBarVisibility();
                }
            }
        });

        return view;
    }

    public static ImageViewerFragment newInstance(String path) {
        ImageViewerFragment imageViewerFragment = new ImageViewerFragment();

        Bundle bundle = new Bundle();
        bundle.putString(PATH, path);
        imageViewerFragment.setArguments(bundle);

        return imageViewerFragment;
    }
}
