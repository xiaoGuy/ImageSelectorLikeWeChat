package com.xiaoguy.imageselector.base;

import android.app.Application;
import android.content.Context;

/**
 * Created by XiaoGuy on 2016/10/17.
 */

public class AppApplication extends Application {

    private static Context mContext;

    @Override
    public void onCreate() {
        mContext = getApplicationContext();
    }

    public static Context getInstance() {
        return mContext;
    }
}
