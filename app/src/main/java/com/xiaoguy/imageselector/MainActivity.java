package com.xiaoguy.imageselector;

import android.Manifest.permission;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore.Images.Media;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.xiaoguy.imageselector.imagelaoder.ImageFolder;
import com.xiaoguy.imageselector.imagelaoder.ImageLoader;
import com.xiaoguy.imageselector.imagelaoder.ImageLoader.OnLoadFinishListener;

import java.util.List;

public class MainActivity extends AppCompatActivity implements OnLoadFinishListener {

    private static final int REQUEST_SDCARD = 0;
    private static final int REQUEST_PERMISSION_SETTING = 1;
    private static final String SELECTION = Media.MIME_TYPE + " = ? or " + Media.MIME_TYPE + " = ?";
    private static final String[] SELECTION_ARGS = {"image/jpeg", "image/png"};

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (! checkReadSDCardPermission()) {
            requestReadSDCardPermission();
        } else {
            if (isSDCardAvailable()) {
                loadImages();
            } else {
                new AlertDialog.Builder(this).setMessage("SD卡不可用").
                        setPositiveButton("确定", new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        }).show();
            }
        }
    }

    private void loadImages() {
        mProgressDialog = ProgressDialog.show(this, null, null);
        ImageLoader.getInstance(this).loadImages(this);
    }

    private boolean isSDCardAvailable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    private boolean checkReadSDCardPermission() {
        return ContextCompat.checkSelfPermission(this, permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED ? true : false;
    }

    private void requestReadSDCardPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale
                (this, permission.READ_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(this).setMessage("显示图片需要获取存储权限").
                    setPositiveButton("确定", new OnClickListener() {
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
            if (! ActivityCompat.shouldShowRequestPermissionRationale
                    (this, permission.READ_EXTERNAL_STORAGE)) {
                new AlertDialog.Builder(this).setMessage("显示图片需要获取存储权限，是否设置").
                        setPositiveButton("设置", new OnClickListener() {
                            // 跳转到设置界面
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getPackageName(), null);
                                intent.setData(uri);
                                startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                            }
                        }).
                        setNegativeButton("取消", new OnClickListener() {
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
        if (! checkReadSDCardPermission()) {
            requestReadSDCardPermission();
        }
    }

    @Override
    public void onLoadFinish(List<ImageFolder> allImageFolders, List<String> allImagePath) {
        mProgressDialog.dismiss();
    }
}
