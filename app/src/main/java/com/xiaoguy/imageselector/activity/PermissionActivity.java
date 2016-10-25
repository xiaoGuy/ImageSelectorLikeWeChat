package com.xiaoguy.imageselector.activity;

import android.Manifest;
import android.Manifest.permission;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.xiaoguy.imageselector.R;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

/**
 * Created by XiaoGuy on 2016/10/20.
 */

@RuntimePermissions
public class PermissionActivity extends BaseActivity {

    private static final String TAG = PermissionActivity.class.getName();

    private static final int REQUEST_STORAGE_SETTING = 1;
    private static final int REQUEST_CAMERA_SETTING = 2;
    protected static final String PERMISSION_STORAGE = "permission_storage";
    private int mRequestCode;

    /**
     * 标记存储权限是否是必要的
     */
    private boolean mIsPermissionRequired;

    protected void requestPermission(String permission, boolean required) {
        if (permission.equals(PERMISSION_STORAGE)) {
            mIsPermissionRequired = required;
            PermissionActivityPermissionsDispatcher.requestStoragePermissionWithCheck(this);
        } else if (permission.equals(Manifest.permission.CAMERA)) {
            mIsPermissionRequired = required;
            PermissionActivityPermissionsDispatcher.requestCameraPermissionWithCheck(this);
        } else {
            Log.e(TAG, "unsupported permission : " + permission);
        }
    }

    protected void onPermissionGranted(String permission) {
    }

    protected void onPermissionDenied(String permission) {
    }

    @NeedsPermission({permission.READ_EXTERNAL_STORAGE,
                      permission.WRITE_EXTERNAL_STORAGE})
    void requestStoragePermission() {
        onPermissionGranted(PERMISSION_STORAGE);
    }

    @NeedsPermission(permission.CAMERA)
    void requestCameraPermission() {
        onPermissionGranted(permission.CAMERA);
    }

    @OnPermissionDenied({permission.READ_EXTERNAL_STORAGE,
                         permission.WRITE_EXTERNAL_STORAGE})
    void onStoragePermissionDenied() {
        onPermissionDenied(PERMISSION_STORAGE);
    }

    @OnPermissionDenied(permission.CAMERA)
    void onCameraPermissionDenied() {
        onPermissionDenied(permission.CAMERA);
    }

    @OnShowRationale({permission.READ_EXTERNAL_STORAGE,
                      permission.WRITE_EXTERNAL_STORAGE})
    void showRationaleForStoragePermission(PermissionRequest request) {
        showRationaleDialog(PERMISSION_STORAGE, request, mIsPermissionRequired);
    }

    @OnShowRationale(permission.CAMERA)
    void showRationaleForCameraPermission(PermissionRequest request) {
        showRationaleDialog(permission.CAMERA, request, mIsPermissionRequired);
    }

    @OnNeverAskAgain({permission.READ_EXTERNAL_STORAGE,
                      permission.WRITE_EXTERNAL_STORAGE})
    void onStoragePermissionNeverAsk() {
        onNeverAsk(PERMISSION_STORAGE, mIsPermissionRequired);
    }

    @OnNeverAskAgain(permission.CAMERA)
    void onCameraPermissionNeverAsk() {
        onNeverAsk(permission.CAMERA, mIsPermissionRequired);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        PermissionActivityPermissionsDispatcher.
                onRequestPermissionsResult(this, requestCode, grantResults);
    }

    private void onNeverAsk(String permission, final boolean isFinishOnCancel) {

        String message = null;
        if (permission.equals(PERMISSION_STORAGE)) {
            message = getString(R.string.set_storage_permission);
            mRequestCode = REQUEST_STORAGE_SETTING;
        } else if (permission.equals(Manifest.permission.CAMERA)) {
            message = getString(R.string.set_camera_permission);
            mRequestCode = REQUEST_CAMERA_SETTING;
        }

        new Builder(this).
                setMessage(message).
                setPositiveButton(R.string.setting, new OnClickListener() {
                    // 跳转到设置界面
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, mRequestCode);
                    }
                }).
                setNegativeButton(R.string.cancel, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (isFinishOnCancel) {
                            finish();
                        }
                    }
                }).show();
    }

    private void showRationaleDialog(String permission, final PermissionRequest request,
                                     final boolean isFinishOnCancel) {
        String message = null;
        if (permission.equals(PERMISSION_STORAGE)) {
            message = getString(R.string.need_storage_permission);
        } else if (permission.equals(Manifest.permission.CAMERA)) {
            message = getString(R.string.need_camera_permission);
        }

        new AlertDialog.Builder(this)
                .setPositiveButton(R.string.allow, new OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        request.proceed();
                    }
                })
                .setNegativeButton(R.string.deny, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (isFinishOnCancel) {
                            finish();
                        }
                    }
                })
                .setCancelable(false)
                .setMessage(message)
                .show();
    }
}
