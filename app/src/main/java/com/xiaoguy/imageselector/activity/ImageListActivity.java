package com.xiaoguy.imageselector.activity;

import android.Manifest.permission;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by XiaoGuy on 2016/10/8.
 */

public class ImageListActivity extends AppCompatActivity {

    private static final int READ_SDCARD = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (checkReadSDCardPermission()) {

        } else {
            ActivityCompat.requestPermissions(this, new String[]{permission.READ_EXTERNAL_STORAGE},
                    READ_SDCARD);
        }
    }

    private boolean checkReadSDCardPermission() {
        return ContextCompat.checkSelfPermission(this, permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED ? true : false;
    }
}
