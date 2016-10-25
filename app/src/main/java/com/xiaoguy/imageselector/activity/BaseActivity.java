package com.xiaoguy.imageselector.activity;

import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

/**
 * Created by XiaoGuy on 2016/10/24.
 */

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (VERSION.SDK_INT >= VERSION_CODES.KITKAT && VERSION.SDK_INT < VERSION_CODES.LOLLIPOP) {
            // 将状态栏设置为透明，副作用是布局会延伸到状态栏中
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.flags |= LayoutParams.FLAG_TRANSLUCENT_STATUS;
        }
    }
}
