package com.xiaoguy.imageselector.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiaoguy.imageselector.R;
import com.xiaoguy.imageselector.ui.ImageViewerFragment;
import com.xiaoguy.imageselector.ui.SpecialCheckBox;
import com.xiaoguy.imageselector.util.DrawableUtil;
import com.xiaoguy.imageselector.util.WidgetUtil;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnPageChange;
import butterknife.OnPageChange.Callback;

/**
 * Created by XiaoGuy on 2016/10/21.
 */

public class ImageViewerActivity extends AppCompatActivity {

    private static final String TAG = ImageViewerActivity.class.getSimpleName();

    public static final String SELECTED_IMAGES = "selected_images";
    public static final int REQUEST_IMAGE_VIEWER = 374;

    private static final String POSITION = "position";
    private static final String IMAGES = "images";
    private static final String MAX_SELECTED = "max_selected";
    private static final String PATH = "path";


    @BindView(R.id.btn_back)
    ImageView mBtnBack;
    @BindView(R.id.text_title)
    TextView mTextTitle;
    @BindView(R.id.layout_bottom_bar)
    ViewGroup mLayoutBottomBar;
    @BindView(R.id.layout_title_bar)
    ViewGroup mLayoutTitleBar;
    @BindView(R.id.btn_finish)
    Button mBtnSend;
    @BindView(R.id.checkbox_select)
    SpecialCheckBox mCheckboxSelect;
    @BindView(R.id.layout)
    RelativeLayout mLayout;
    @BindView(R.id.viewPager)
    ViewPager mViewPager;

    private WidgetUtil mWidgetUtil;

    private ArrayList<String> mImages;
    private ArrayList<String> mSelectedImages;
    private int mMaxSelectedSize;
    private int mCurrentPosition;

    /**
     * 拍摄的照片的路径
     */
    private String mTakenPhotoPath;

    /**
     *  是否只是预览单张图片，如果是拍完照片后的预览则为 true
     */
    private boolean mIsSingleImagePreview;

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
            mCurrentPosition = intent.getIntExtra(POSITION, -1);
            mMaxSelectedSize = intent.getIntExtra(MAX_SELECTED, -1);
            mImages = intent.getStringArrayListExtra(IMAGES);
            mSelectedImages = intent.getStringArrayListExtra(SELECTED_IMAGES);
            mTakenPhotoPath = intent.getStringExtra(PATH);

            // 当前预览的是拍摄完的照片
            if (mTakenPhotoPath != null) {
                mIsSingleImagePreview = true;
                mCurrentPosition = 0;
                mMaxSelectedSize = 1;
                mImages = new ArrayList<>(1);
                mImages.add(mTakenPhotoPath);
            }
        }
    }

    private void initView() {
        mWidgetUtil = new WidgetUtil();

        if (! mIsSingleImagePreview) {
            mCheckboxSelect.setButtonDrawable(DrawableUtil.setTintList(this, R.drawable.selector_checkbox,
                    R.color.selector_color_checkbox));
            updateSendBtnText(mSelectedImages.size(), mMaxSelectedSize);
        } else {
            mCheckboxSelect.setVisibility(View.GONE);
        }

        mViewPager.setAdapter(new ImageViewerAdapter(getSupportFragmentManager()));
        mViewPager.setCurrentItem(mCurrentPosition);

        displayCurrentImagePosition(mTextTitle, mCurrentPosition + 1, mImages.size());
        setCheckState(mCurrentPosition);
    }

    /**
     * 打开该 Activity 的便捷方法</b>
     * requestCode 为 {@link #REQUEST_IMAGE_VIEWER}
     *
     * @param activity       跳转过来的 Activity
     * @param position       图片在图片列表中的位置
     * @param maxSelected    最多可选择的数量
     * @param images         图片列表
     * @param selectedImages 选中的图片
     * @
     */
    public static void startImageViewActivity(Activity activity, int position, int maxSelected,
                                              ArrayList<String> images, ArrayList<String> selectedImages) {
        Intent intent = new Intent(activity, ImageViewerActivity.class);
        intent.putExtra(POSITION, position);
        intent.putExtra(MAX_SELECTED, maxSelected);
        intent.putStringArrayListExtra(IMAGES, images);
        intent.putStringArrayListExtra(SELECTED_IMAGES, selectedImages);
        activity.startActivityForResult(intent, REQUEST_IMAGE_VIEWER);
    }

    /**
     * 单张预览模式，即预览刚拍完的照片
     *
     * @param activity 条转过来的 Activity
     * @param path 拍摄的照片的路径
     */
    public static void startImageViewActivity(Activity activity, String path) {
        Intent intent = new Intent(activity, ImageViewerActivity.class);
        intent.putExtra(PATH, path);
        activity.startActivityForResult(intent, REQUEST_IMAGE_VIEWER);
    }

    @OnPageChange(value = R.id.viewPager, callback = Callback.PAGE_SELECTED)
    public void onPageSelected(int position) {
        mCurrentPosition = position;
        displayCurrentImagePosition(mTextTitle, mCurrentPosition + 1, mImages.size());
        setCheckState(position);
    }

    @OnClick(R.id.btn_back)
    @Override
    public void onBackPressed() {
        if (! mIsSingleImagePreview) {
            Intent intent = new Intent();
            intent.putStringArrayListExtra(SELECTED_IMAGES, mSelectedImages);
            setResult(RESULT_CANCELED, intent);
        }
        super.onBackPressed();
    }

    @OnClick(R.id.checkbox_select)
    void onCheckBoxClicked() {
        final boolean isChecked = mCheckboxSelect.isChecked();
        if (! isChecked && mSelectedImages.size() >= mMaxSelectedSize) {
            mWidgetUtil.showToast(this, getString(R.string.max_selected, mMaxSelectedSize));
            return;
        }
        mCheckboxSelect.doToggle();
        if (mCheckboxSelect.isChecked()) {
            mSelectedImages.add(mImages.get(mCurrentPosition));
        } else {
            mSelectedImages.remove(mImages.get(mCurrentPosition));
        }
        updateSendBtnText(mSelectedImages.size(), mMaxSelectedSize);
    }

    @OnClick(R.id.btn_finish)
    void sendSelectedImages() {
        Intent intent = new Intent();

        if (mIsSingleImagePreview) {
            intent.putStringArrayListExtra(SELECTED_IMAGES, mImages);
        // 点击完成时如果还没有选中任何图片则选择当前的图片
        } else if (mSelectedImages.size() == 0) {
            mSelectedImages.add(mImages.get(mCurrentPosition));
            intent.putStringArrayListExtra(SELECTED_IMAGES, mSelectedImages);
        } else {
            intent.putStringArrayListExtra(SELECTED_IMAGES, mSelectedImages);
        }

        setResult(RESULT_OK, intent);
        finish();
    }

    private void updateSendBtnText(int selectedSize, int maxSelectedSize) {
        if (selectedSize == 0) {
            mBtnSend.setText(R.string.finish);
        } else {
            mBtnSend.setText(getString(R.string.finish_2, selectedSize, maxSelectedSize));
        }
    }

    /**
     * 还原选中状态
     */
    private void setCheckState(int position) {
        if (mCheckboxSelect.getVisibility() != View.VISIBLE) {
            return;
        }
        if (mSelectedImages.contains(mImages.get(position))) {
            mCheckboxSelect.setChecked(true);
        } else {
            mCheckboxSelect.setChecked(false);
        }
    }

    /**
     * 显示当前图片在图片列表中的位置，如 1/100
     *
     * @param textView 用于显示的 View
     * @param position 当前的位置
     * @param count    图片的数量
     */
    private void displayCurrentImagePosition(TextView textView, int position, int count) {
        textView.setText(getString(R.string.title_image_viewer, position, count));
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
