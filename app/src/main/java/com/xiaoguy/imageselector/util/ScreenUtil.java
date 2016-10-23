package com.xiaoguy.imageselector.util;

import android.app.Activity;
import android.util.DisplayMetrics;

import static android.R.attr.orientation;

/**
 * Created by XiaoGuy on 2016/10/17.
 */

public abstract class ScreenUtil {

    private static final int WIDTH = 0;
    private static final int HEIGHT = 1;

    public static int getScreenHeight(Activity activity) {
        return getScreenLengthOfSide(activity, HEIGHT);
    }

    public static int getScreenWidth(Activity activity) {
        return getScreenLengthOfSide(activity, WIDTH);
    }

    private static int getScreenLengthOfSide(Activity activity, int witchSide) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        if (witchSide == WIDTH) {
            return displayMetrics.widthPixels;
        } else {
            return displayMetrics.heightPixels;
        }
    }
}
