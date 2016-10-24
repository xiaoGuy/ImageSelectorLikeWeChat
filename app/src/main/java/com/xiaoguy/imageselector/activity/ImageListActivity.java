package com.xiaoguy.imageselector.activity;

import android.Manifest;
import android.Manifest.permission;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetBehavior.BottomSheetCallback;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.AdapterDataObserver;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xiaoguy.imageselector.R;
import com.xiaoguy.imageselector.adapter.ImageFolderListAdapter;
import com.xiaoguy.imageselector.adapter.ImageFolderListAdapter.OnImageFolderClickListener;
import com.xiaoguy.imageselector.adapter.ImageListAdapter;
import com.xiaoguy.imageselector.adapter.ImageListAdapter.OnImageOperateListener;
import com.xiaoguy.imageselector.bean.ImageFolder;
import com.xiaoguy.imageselector.ui.DividerItemDecoration;
import com.xiaoguy.imageselector.ui.SimpleGridItemDecoration;
import com.xiaoguy.imageselector.util.DrawableUtil;
import com.xiaoguy.imageselector.util.WidgetUtil;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ImageListActivity extends PermissionActivity implements
        OnImageOperateListener, OnImageFolderClickListener {

    private static final String TAG = ImageListActivity.class.getName();
    private static final String PHOTO_REFIX = "xg_";
    private static final String PHOTO_SUFFIX = ".jpeg";

    private static final String SELECTION = Media.MIME_TYPE + " = ? or " + Media.MIME_TYPE + " = ?";
    private static final String[] SELECTION_ARGS = {"image/jpeg", "image/png"};

    private static final String PATTERN = "(%1$d/%2$d)";
    private static final String PATTERN_2 = "(%1$d)";

    private static final int MESSAGE_SCAN_FINISH= 754;
    private static final int REQUEST_CAMERA_SETTING =472;
    private static final int REQUEST_TAKE_PHOTO =422;
    private static final int REQUEST_STORAGE_SETTING = 999;

    @BindView(R.id.btn_back)
    ImageView mBtnBack;
    @BindView(R.id.view_divider)
    View mViewDivider;
    @BindView(R.id.text_btnOtherFolder)
    TextView mTextBtnOtherFolder;
    @BindView(R.id.btn_send)
    Button mBtnSend;
    @BindView(R.id.image_btnOtherFolder)
    ImageView mImageBtnOtherFolder;
    @BindView(R.id.btn_preview)
    TextView mBtnPreview;
    @BindView(R.id.btn_takePhoto)
    ImageView mBtnTakePhoto;
    @BindView(R.id.text_title)
    TextView mTextTitleFolderName;
    @BindView(R.id.view_touchOutside)
    View mViewTouchOutside;
    @BindView(R.id.bottom_sheet)
    RelativeLayout mBottomSheet;
    @BindView(R.id.layout_imageFolder)
    CoordinatorLayout mLayoutImageFolder;
    @BindView(R.id.recyclerView_imageList)
    RecyclerView mRecyclerViewImageList;
    @BindView(R.id.recyclerView_imageFolderList)
    RecyclerView mRecyclerViewImageFolderList;
    @BindView(R.id.layout_emptyView)
    ViewGroup mLayoutEmptyView;
    private ProgressDialog mProgressDialog;

    /**
     * 充当 PupupWindow
     */
    private BottomSheetBehavior mBottomSheetBehavior;
    private Toast mToast;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MESSAGE_SCAN_FINISH) {
                onLoadFinish();
            }
        }
    };

    private WidgetUtil mWidgetUtil;

    /**
     * 手机中所有的图片目录
     */
    private List<ImageFolder> mImageFolders = new ArrayList<>();

    /**
     * 手机中的所有图片
     */
    private ArrayList<String> mAllImages = new ArrayList<>();

    /**
     * 拍摄的照片
     */
    private File mPhotoFile;

    /**
     * 保存拍摄的照片的目录
     */
    private File mPhotoDirectory;

    /**
     * 标记 SD 卡中是否存在 mPhotoDirectory
     */
    private boolean mIsPhotoDirectoryCreated;

    /**
     * 选中的图片目录在列表中的位置
     */
    private int mSelectedFolderPosition;

    private ImageListAdapter mImageListAdapter;
    private ImageFolderListAdapter mImageFolderListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_list);
        ButterKnife.bind(this);

        initView();
        requestPermission(PERMISSION_STORAGE, true);
    }

    private void initView() {
        mWidgetUtil = new WidgetUtil();

        mViewDivider.setVisibility(View.GONE);
        mBtnBack.setVisibility(View.GONE);
        mBtnSend.setEnabled(false);
        mTextTitleFolderName.setText(R.string.all_image);

        mImageListAdapter = new ImageListAdapter(this, mAllImages);
        mImageListAdapter.setOnImageOperateListener(this);
        // 设置 EmptyView
        mImageListAdapter.registerAdapterDataObserver(new AdapterDataObserver() {
            @Override
            public void onChanged() {
                if (mImageListAdapter.getItemCount() == 0) {
                    mLayoutEmptyView.setVisibility(View.VISIBLE);
                } else {
                    mLayoutEmptyView.setVisibility(View.GONE);
                }
            }
        });
        mRecyclerViewImageList.setAdapter(mImageListAdapter);
        mRecyclerViewImageList.setLayoutManager(new GridLayoutManager(this, 3));
        mRecyclerViewImageList.setHasFixedSize(true);
        mRecyclerViewImageList.addItemDecoration(new SimpleGridItemDecoration(3, 8, false));

        mImageBtnOtherFolder.setImageDrawable(DrawableUtil.setTintList
                (this, R.drawable.selector_btn_more, R.color.selector_color_btn_more));
        mTextBtnOtherFolder.setText(R.string.all_image);
        mViewTouchOutside.setAlpha(0);

        mBottomSheetBehavior = BottomSheetBehavior.from(mBottomSheet);
        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    mLayoutImageFolder.setVisibility(View.GONE);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                mViewTouchOutside.setAlpha(slideOffset);
            }
        });
        mImageFolderListAdapter = new ImageFolderListAdapter(mImageFolders);
        mImageFolderListAdapter.setOnImageFolderClickListener(this);
        mRecyclerViewImageFolderList.setAdapter(mImageFolderListAdapter);
        mRecyclerViewImageFolderList.setLayoutManager
                    (new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRecyclerViewImageFolderList.addItemDecoration
                    (new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
    }

    @Override
    protected void onPermissionGranted(String permission) {
        if (permission.equals(PERMISSION_STORAGE)) {
            start();
        } else if (permission.equals(Manifest.permission.CAMERA)) {
            doTakePhoto();
        }
    }

    @Override
    protected void onPermissionDenied(String permission) {
        boolean isRequired;
        if (permission.equals(PERMISSION_STORAGE)) {
            isRequired = true;
        } else if (permission.equals(Manifest.permission.CAMERA)) {
            isRequired = false;
        } else {
            isRequired = false;
        }
        requestPermission(permission, isRequired);
    }

    void start() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            loadImages();
        } else {
            new Builder(this).
                    setMessage(R.string.sdcard_unavailable).
                    setPositiveButton(R.string.confirm, new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).show();
        }
    }

    private void loadImages() {
//        mProgressDialog = ProgressDialog.show(this, null, null);
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

        Cursor cursor = getContentResolver().query(
                Media.EXTERNAL_CONTENT_URI, new String[]{ Media.DATA, Media.DATE_MODIFIED}, SELECTION,
                SELECTION_ARGS, Media.DATE_MODIFIED + " DESC");

        while (cursor.moveToNext()) {
            final String currentImagePath = cursor.getString(cursor.getColumnIndex(Media.DATA));
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
                imageFolder.setFirstImage(currentImagePath);
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
            ArrayList<String> imagePathList = null;
            // 无法获取到该目录下的图片，所以该目录下只有一张图片（通过该图片获取到的目录）
            if (children == null) {
                imagePathList = new ArrayList<>(1);
                imagePathList.add(currentImagePath);
                imageFolder.setImageCount(1);
                imageFolder.setImages(imagePathList);
                mImageFolders.add(imageFolder);
                mAllImages.add(currentImagePath);
                continue;
            }
            final int imageCount = children.length;
            imagePathList = new ArrayList<>(imageCount);
            for (File child : children) {
                imagePathList.add(child.getAbsolutePath());
            }
            imageFolder.setImageCount(imageCount);
            imageFolder.setImages(imagePathList);
            mImageFolders.add(imageFolder);
            mAllImages.addAll(imagePathList);
        }

        // SD 卡中没有图片
        if (mAllImages.size() != 0) {
            // 往图片目录中添加“所有图片”目录并放在第一位
            ImageFolder allImageFolder = new ImageFolder();
            allImageFolder.setFirstImage(mAllImages.get(0));
            allImageFolder.setImages(mAllImages);
            allImageFolder.setImageCount(mAllImages.size());
            allImageFolder.setName(getString(R.string.all_image));
            mImageFolders.add(0, allImageFolder);
        }

        cursor.close();
        mHandler.sendEmptyMessage(MESSAGE_SCAN_FINISH);
    }

    /**
     * 扫描图片完成时调用
     */
    private void onLoadFinish() {
//        mProgressDialog.dismiss();
        mImageListAdapter.notifyDataSetChanged();
    }

    @OnClick(R.id.btn_takePhoto)
    void takePhoto() {
        requestPermission(permission.CAMERA, false);
    }

    void doTakePhoto() {
        // 创建保存拍摄的照片的目录失败
        if (createPhotoDirectory() == null) {
            Log.w(TAG, "create photo directory failed!");
            mWidgetUtil.showToast(this, R.string.take_photo_error);
            return;
        }

        mPhotoFile = new File(createPhotoDirectory(),
                PHOTO_REFIX + System.currentTimeMillis() + PHOTO_SUFFIX);
        if (mPhotoFile.exists()) {
            mPhotoFile.delete();
        }

        try {
            mPhotoFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            Log.w(TAG, "create photo file failed! " + mPhotoFile.getAbsolutePath());
        }

        Uri uri = Uri.fromFile(mPhotoFile);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, REQUEST_TAKE_PHOTO);
    }

    /**
     * 创建保存拍摄的照片的目录
     *
     * @return 保存拍摄的照片的目录或者 null （创建目录失败）
     */
    private File createPhotoDirectory() {
        if (mIsPhotoDirectoryCreated) {
            return mPhotoDirectory;
        }

        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        CharSequence appName = getPackageManager().getApplicationLabel(getApplicationInfo());
        mPhotoDirectory = new File(path + File.separator + appName);

        if (!mPhotoDirectory.exists()) {
            if (mPhotoDirectory.mkdir()) {
                mIsPhotoDirectoryCreated = true;
            }
        }

        return mPhotoDirectory;
    }

    @OnClick(R.id.btn_preview)
    void previewSelectedImages() {
        ArrayList<String> selectedImages = mImageListAdapter.getSelectedImages();
        ImageViewerActivity.startImageViewActivity(this, 0, mImageListAdapter.getMaxSelected(),
                mImageListAdapter.getSelectedImages(), mImageListAdapter.getSelectedImages());
    }

    /**
     * 显示所有的图片目录
     */
    @OnClick(R.id.btn_otherFolder)
    void showImageFolders() {
        if (mImageFolders.size() == 0) {
            return;
        }

        mRecyclerViewImageFolderList.scrollToPosition(mSelectedFolderPosition);

        final int state = mBottomSheetBehavior.getState();
        // 完全展开
        if (state == BottomSheetBehavior.STATE_EXPANDED) {
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        // 处于 peekHeight 处（这里 peekHeight 为 0）
        } else if (state == BottomSheetBehavior.STATE_COLLAPSED) {
            mLayoutImageFolder.setVisibility(View.VISIBLE);
            // 延迟展开，否则看不到展开动画
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }, 10);
        }
    }

    /**
     * 点击 PopupWindow 外面关闭 PopupWindow
     */
    @OnClick(R.id.view_touchOutside)
    void onTouchOutside() {
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_STORAGE_SETTING) {
            requestPermission(PERMISSION_STORAGE, true);

        } else if (requestCode == REQUEST_CAMERA_SETTING) {
            requestPermission(permission.CAMERA, false);
        // 从拍照界面返回
        } else if (requestCode == REQUEST_TAKE_PHOTO) {
            if (resultCode == RESULT_CANCELED) {
                // 取消拍照后要删除创建出的文件
                if (mPhotoFile.delete()) {
                    Log.d(TAG, mPhotoFile.getAbsolutePath() + "deleted!");
                }
                mPhotoFile = null;
            } else {
                // 将拍摄的照片保存到数据库中，这样才能从 MediaStore 中读取到
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                        Uri.fromFile(mPhotoFile)));
                ImageViewerActivity.startImageViewActivity(this, mPhotoFile.getAbsolutePath());
            }
        // 从查看图片界面返回
        } else if (requestCode == ImageViewerActivity.REQUEST_IMAGE_VIEWER) {
            if (resultCode == RESULT_OK) {

            } else {
                // 如果是单张预览模式则 data 为 null
                if (data != null) {
                    ArrayList<String> selectedImages =
                            data.getStringArrayListExtra(ImageViewerActivity.SELECTED_IMAGES);
                    mImageListAdapter.updateSelectedImages(selectedImages);
                    updateBtnText(selectedImages);
                }
            }
        }
    }

    //***************************** OnImageOperateListener ********************************
    @Override
    public void onTakePhoto() {
        takePhoto();
    }

    @Override
    public void onImageClick(int position, ArrayList<String> images, ArrayList<String> selectedImages) {
        ImageViewerActivity.startImageViewActivity(this, position,
                mImageListAdapter.getMaxSelected(), images, selectedImages);
    }

    @Override
    public void onImageSelectedOverflow(int max) {
        mWidgetUtil.showToast(this, getString(R.string.max_selected, max));
    }

    @Override
    public void onImageSelected(List<String> selectedImages) {
        updateBtnText(selectedImages);
    }

    private void updateBtnText(List<String> selectedImages) {
        if (selectedImages.size() == 0) {
            mBtnSend.setEnabled(false);
            mBtnPreview.setEnabled(false);
            mBtnSend.setText(R.string.send);
            mBtnPreview.setText(R.string.preview);
        } else {
            mBtnSend.setEnabled(true);
            mBtnPreview.setEnabled(true);
            String str = String.format(PATTERN, selectedImages.size(), mImageListAdapter.getMaxSelected());
            mBtnSend.setText(getString(R.string.send) + str);
            str = String.format(PATTERN_2, selectedImages.size());
            mBtnPreview.setText(getString(R.string.preview) + str);
        }
    }

    //*************************** OnImageFolderClickListener *******************************
    @Override
    public void onImageFolderClick(ImageFolder imageFolder, int position, boolean isChecked) {
        mSelectedFolderPosition = position;
        mTextBtnOtherFolder.setText(imageFolder.getName());
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        if (! isChecked) {
            // 如果选择的是“所有图片”目录，要显示拍照按钮
            if (imageFolder.getName().equals(getString(R.string.all_image))) {
                mImageListAdapter.setCameraEnabled(true);
            } else {
                mImageListAdapter.setCameraEnabled(false);
            }
            mImageListAdapter.setImages(imageFolder.getImages());
        }
    }
}
