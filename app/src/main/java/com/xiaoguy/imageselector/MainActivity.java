package com.xiaoguy.imageselector;

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
import android.support.annotation.StringRes;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.xiaoguy.imageselector.activity.PermissionActivity;
import com.xiaoguy.imageselector.adapter.ImageFolderListAdapter;
import com.xiaoguy.imageselector.adapter.ImageFolderListAdapter.OnImageFolderClickListener;
import com.xiaoguy.imageselector.adapter.ImageListAdapter;
import com.xiaoguy.imageselector.adapter.ImageListAdapter.OnImageOperateListener;
import com.xiaoguy.imageselector.bean.ImageFolder;
import com.xiaoguy.imageselector.ui.DividerItemDecoration;
import com.xiaoguy.imageselector.ui.SimpleGridItemDecoration;
import com.xiaoguy.imageselector.ui.SpecialButton;
import com.xiaoguy.imageselector.ui.StrongBottomSheetDialog;
import com.xiaoguy.imageselector.util.DrawableUtil;
import com.xiaoguy.imageselector.util.ScreenUtil;

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

//@RuntimePermissions
public class MainActivity extends PermissionActivity implements
        OnImageOperateListener, OnImageFolderClickListener {

    private static final String TAG = MainActivity.class.getName();

    private static final String SELECTION = Media.MIME_TYPE + " = ? or " + Media.MIME_TYPE + " = ?";
    private static final String[] SELECTION_ARGS = {"image/jpeg", "image/png"};

    private static final int REQUEST_STORAGE_SETTING = 1;
    private static final int REQUEST_CAMERA_SETTING = 2;
    private static final int REQUEST_TAKE_PHOTO = 3;

    private static final String PHOTO_REFIX = "xg_";
    private static final String PHOTO_SUFFIX = ".jpeg";
    @BindView(R.id.btn_imageFolder)
    SpecialButton mBtnImageFolder;
    @BindView(R.id.text_btnMore)
    TextView mTextBtnMore;
    @BindView(R.id.btn_send)
    Button mBtnSend;
    @BindView(R.id.image_btnMore)
    ImageView mImageBtnMore;
    @BindView(R.id.btn_preview)
    TextView mBtnPreview;
    @BindView(R.id.btn_takePhoto)
    ImageView mBtnImage;

    private int mRequestCode;

    private ProgressDialog mProgressDialog;
    private StrongBottomSheetDialog mBottomSheetDialog;
    private Toast mToast;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            onLoadFinish();
        }
    };

    /**
     * 当前要显示的图片集合
     */
    private List<String> mCurrentImages;

    /**
     * 手机中所有的图片目录
     */
    private List<ImageFolder> mImageFolders = new ArrayList<>();

    /**
     * 手机中的所有图片
     */
    private List<String> mAllImages = new ArrayList<>();

    /**
     * 拍摄的照片
     */
    private File mPhotoFile;

    /**
     * 保存拍摄的照片的目录
     */
    private File mPhotoDirecory;

    /**
     * 标记 SD 卡中是否存在 mPhotoDirecory
     */
    private boolean mIsPhotoDirectoryCreated;

    @BindView(R.id.recyclerView_imageList)
    RecyclerView mRecyclerViewImageList;

    ImageListAdapter mImageListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initView();
        requestPermission(PERMISSION_STORAGE, true);
    }

    private void initView() {
        mImageListAdapter = new ImageListAdapter(this, mAllImages);
        mImageListAdapter.setOnImageOperateListener(this);
        mRecyclerViewImageList.setAdapter(mImageListAdapter);
        mRecyclerViewImageList.setLayoutManager(new GridLayoutManager(this, 3));
        mRecyclerViewImageList.setHasFixedSize(true);
        mRecyclerViewImageList.addItemDecoration(new SimpleGridItemDecoration(3, 8, false));

        mImageBtnMore.setImageDrawable(DrawableUtil.setTintList
                (this, R.drawable.selector_btn_more, R.color.selector_color_btn_more));
        // 字体的 baseline 距离底部有一段距离，这段距离叫做 descent
        mImageBtnMore.setPadding(0, 0, 0, (int) (mTextBtnMore.getPaint().descent() / 2));
        mTextBtnMore.setText(R.string.all_image);
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
        mProgressDialog = ProgressDialog.show(this, null, null);
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
                Media.EXTERNAL_CONTENT_URI, new String[]{
                        Media.DATA, Media.DATE_MODIFIED}, SELECTION,
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
            imageFolder.setImagePaths(imagePathList);
            mImageFolders.add(imageFolder);
            mAllImages.addAll(imagePathList);
        }

        cursor.close();
        mHandler.sendEmptyMessage(0);
    }

    /**
     * 扫描图片完成时调用
     */
    private void onLoadFinish() {
        mProgressDialog.dismiss();
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
            showToast(R.string.take_photo_error);
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
            return mPhotoDirecory;
        }

        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        CharSequence appName = getPackageManager().getApplicationLabel(getApplicationInfo());
        mPhotoDirecory = new File(path + File.separator + appName);

        if (!mPhotoDirecory.exists()) {
            if (mPhotoDirecory.mkdir()) {
                mIsPhotoDirectoryCreated = true;
            }
        }

        return mPhotoDirecory;
    }

    /**
     * 显示所有的图片目录
     */
    private void showImageFolders() {
        if (mBottomSheetDialog == null) {
            mBottomSheetDialog = new StrongBottomSheetDialog(this);

            RecyclerView recyclerView = (RecyclerView)
                    View.inflate(this, R.layout.bottom_dialog_layout, null);
            ImageFolderListAdapter adapter = new ImageFolderListAdapter(mImageFolders);
            adapter.setOnImageFolderClickListener(this);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager
                    (new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            recyclerView.addItemDecoration
                    (new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));

            final int height = ScreenUtil.getScreenHeight(this) / 2;
            mBottomSheetDialog.setMaxHeight(height);
            mBottomSheetDialog.setPeekHeight(height);
            mBottomSheetDialog.setContentView(recyclerView);
        }
        mBottomSheetDialog.show();
    }

    private void showToast(@StringRes int stringResId) {
        showToast(getString(stringResId));
    }

    private void showToast(String text) {
        if (mToast == null) {
            mToast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        }
        mToast.setText(text);
        mToast.show();
    }

    // 从设置界面返回
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_STORAGE_SETTING) {
            requestPermission(PERMISSION_STORAGE, true);

        } else if (requestCode == REQUEST_CAMERA_SETTING) {
            requestPermission(permission.CAMERA, false);

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
            }
        }
    }

    //***************************** OnImageOperateListener ********************************
    @Override
    public void onTakePhoto() {
        takePhoto();
    }

    @Override
    public void onImageClick(String path) {
    }

    @Override
    public void onImageSelectedOverflow(int max) {
        showToast(getString(R.string.max_selected, max));
    }

    @Override
    public void onSelectedStateChanged(int selectedState) {
        if (selectedState == OnImageOperateListener.SELECTED) {
            mBtnSend.setEnabled(true);
        } else if (selectedState == OnImageOperateListener.CLEARED) {
            mBtnSend.setEnabled(false);
        }
    }

    //*************************** OnImageFolderClickListener *******************************
    @Override
    public void onImageFolderClick(List<String> images, String folderName) {

    }
}
