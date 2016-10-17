package com.xiaoguy.imageselector;

import android.Manifest.permission;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore.Images.Media;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.xiaoguy.imageselector.bean.ImageFolder;
import com.xiaoguy.imageselector.ui.DividerItemDecoration;
import com.xiaoguy.imageselector.ui.ImageFolderAdapter;
import com.xiaoguy.imageselector.ui.ImageListAdapter;
import com.xiaoguy.imageselector.ui.SimpleGridItemDecoration;
import com.xiaoguy.imageselector.ui.StrongBottomSheetDialog;
import com.xiaoguy.imageselector.util.ScreenUtil;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private static final String SELECTION = Media.MIME_TYPE + " = ? or " + Media.MIME_TYPE + " = ?";
    private static final String[] SELECTION_ARGS = {"image/jpeg", "image/png"};

    private static final int REQUEST_SDCARD = 0;
    private static final int REQUEST_PERMISSION_SETTING = 1;

    private ProgressDialog mProgressDialog;
    private StrongBottomSheetDialog mBottomSheetDialog;
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


    @BindView(R.id.btn)
    Button mBtn;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.recyclerView_imageList)
    RecyclerView mRecyclerViewImageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImageFolders();
            }
        });

        if (checkReadSDCardPermission()) {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                loadImages();
            } else {
                onSDCardUnavailable();
            }
        } else {
            requestReadSDCardPermission();
        }
    }

    private void onSDCardUnavailable() {
        new Builder(this).
                setMessage(R.string.sdcard_unavailable).
                setPositiveButton(R.string.confirm, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).show();
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

    private void onLoadFinish() {
        mProgressDialog.dismiss();

        ImageListAdapter adapter = new ImageListAdapter(this, mAllImages);
        mRecyclerViewImageList.setAdapter(adapter);
        mRecyclerViewImageList.setLayoutManager(new GridLayoutManager(this, 3));
        mRecyclerViewImageList.setHasFixedSize(true);
        mRecyclerViewImageList.addItemDecoration(new SimpleGridItemDecoration(3, 8, false));

    }

    private void showImageFolders() {
        if (mBottomSheetDialog == null) {
            mBottomSheetDialog = new StrongBottomSheetDialog(this);

            RecyclerView recyclerView = (RecyclerView)
                    View.inflate(this, R.layout.bottom_dialog_layout, null);
            ImageFolderAdapter adapter = new ImageFolderAdapter(this, mImageFolders);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));

            final int height = ScreenUtil.getScreenHeight(this) / 2;
            mBottomSheetDialog.setMaxHeight(height);
            mBottomSheetDialog.setPeekHeight(height);
            mBottomSheetDialog.setContentView(recyclerView);
        }
        mBottomSheetDialog.show();
    }

    private boolean checkReadSDCardPermission() {
        return ContextCompat.checkSelfPermission(this, permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED ? true : false;
    }

    private void requestReadSDCardPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale
                (this, permission.READ_EXTERNAL_STORAGE)) {
            new Builder(this).
                    setMessage(R.string.need_storage_permission).
                    setPositiveButton(R.string.confirm, new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]
                                    {permission.READ_EXTERNAL_STORAGE}, REQUEST_SDCARD);
                        }
                    }).show();
        } else {
            ActivityCompat.requestPermissions
                    (this, new String[]{permission.READ_EXTERNAL_STORAGE}, REQUEST_SDCARD);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        // 拒绝授权
        if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
            // 勾选了不再提示
            if (!ActivityCompat.shouldShowRequestPermissionRationale
                    (this, permission.READ_EXTERNAL_STORAGE)) {
                new Builder(this).
                        setMessage("显示图片需要获取存储权限，是否设置").
                        setPositiveButton(R.string.setting, new OnClickListener() {
                            // 跳转到设置界面
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getPackageName(), null);
                                intent.setData(uri);
                                startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                            }
                        }).
                        setNegativeButton(R.string.cancel, new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        }).show();
            } else {
                requestReadSDCardPermission();
            }
            // 同意授权
        } else {
            loadImages();
        }
    }

    // 从设置界面返回
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!checkReadSDCardPermission()) {
            requestReadSDCardPermission();
        }
    }
}
