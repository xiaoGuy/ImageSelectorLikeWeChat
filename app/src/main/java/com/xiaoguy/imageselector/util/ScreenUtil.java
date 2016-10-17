package com.xiaoguy.imageselector.util;

import android.app.Activity;
import android.util.DisplayMetrics;

/**
 * Created by XiaoGuy on 2016/10/17.
 */

public abstract class ScreenUtil {

    public static int getScreenHeight(Activity activity) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }
}
