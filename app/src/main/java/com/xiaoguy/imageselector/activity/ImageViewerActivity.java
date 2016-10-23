package com.xiaoguy.imageselector.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiaoguy.imageselector.R;
import com.xiaoguy.imageselector.ui.ImageViewerFragment;
import com.xiaoguy.imageselector.util.DrawableUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by XiaoGuy on 2016/10/21.
 */

public class ImageViewerActivity extends AppCompatActivity implements OnPageChangeListener {

    private static final String POSITION = "position";
    private static final String IMAGES = "images";
    private static final String SELECTED_IMAGES = "selected_images";
    public static final int REQUEST_IMAGE_VIEWER  = 374;

    @BindView(R.id.btn_back)
    ImageView mBtnBack;
    @BindView(R.id.text_title)
    TextView mTextTitle;
    @BindView(R.id.layout_bottom_bar)
    ViewGroup mLayoutBottomBar;
    @BindView(R.id.layout_title_bar)
    ViewGroup mLayoutTitleBar;
    @BindView(R.id.btn_send)
    Button mBtnSend;
    @BindView(R.id.checkbox_select)
    AppCompatCheckBox mCheckboxSelect;
    @BindView(R.id.layout)
    RelativeLayout mLayout;
    @BindView(R.id.viewPager)
    ViewPager mViewPager;

    private List<String> mImages;
    private List<String> mSelectedImages;

    /**
     * 点击的图片在图片列表中的位置
     */
    private int mClickPosition;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        ButterKnife.bind(this);

        initData();
        initView();
    }

    private void initData() {
        Intent intent = getIntent();
        if (intent != null) {
            mClickPosition = intent.getIntExtra(POSITION, -1);
            mImages = intent.getStringArrayListExtra(IMAGES);
            mSelectedImages = intent.getStringArrayListExtra(SELECTED_IMAGES);
        }
    }

    private void initView() {
        mCheckboxSelect.setButtonDrawable(DrawableUtil.setTintList(this, R.drawable.selector_checkbox,
                R.color.selector_color_checkbox));

        mViewPager.setAdapter(new ImageViewerAdapter(getSupportFragmentManager()));
        mViewPager.addOnPageChangeListener(this);
        mViewPager.setCurrentItem(mClickPosition);


    }

    /**
     * 打开该 Activity 的便捷方法</b>
     * requestCode 为 {@link #REQUEST_IMAGE_VIEWER}
     *
     * @param activity       跳转过来的 Activity
     * @param position       图片在图片列表中的位置
     * @param images         图片列表
     * @param selectedImages 选中的图片
     */
    public static void startImageViewActivity(Activity activity, int position,
                                              ArrayList<String> images, ArrayList<String> selectedImages) {
        Intent intent = new Intent(activity, ImageViewerActivity.class);
        intent.putExtra(POSITION, position);
        intent.putStringArrayListExtra(IMAGES, images);
        intent.putStringArrayListExtra(SELECTED_IMAGES, selectedImages);
        activity.startActivityForResult(intent, REQUEST_IMAGE_VIEWER);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        mTextTitle.setText(getString(R.string.title_image_viewer, position, mImages.size()));
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    class ImageViewerAdapter extends FragmentStatePagerAdapter {

        public ImageViewerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            final String path = mImages.get(position);
            return ImageViewerFragment.newInstance(path.toString());
        }

        @Override
        public int getCount() {
            return mImages.size();
        }
    }
}
