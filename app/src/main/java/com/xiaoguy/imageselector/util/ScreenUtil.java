package com.xiaoguy.imageselector.util;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

/**
 * Created by XiaoGuy on 2016/10/17.
 */

public abstract class ScreenUtil {

    private static final String TAG = ScreenUtil.class.getSimpleName();

    private static final int WIDTH = 0;
    private static final int HEIGHT = 1;

    public static int getScreenHeight(Context activity) {
        return getScreenLengthOfSide(activity, HEIGHT);
    }

    public static int getScreenWidth(Context activity) {
        return getScreenLengthOfSide(activity, WIDTH);
    }

    private static int getScreenLengthOfSide(Context context, int witchSide) {
        if (! check(context, "getScreenWidth/Height", "context")) {
            return -1;
        }
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(displayMetrics);
        if (witchSide == WIDTH) {
            return displayMetrics.widthPixels;
        } else {
            return displayMetrics.heightPixels;
        }
    }

    public static int getStatusBarHeight(Context context) {
        if (! check(context, "getStatusBarHeight", "context")) {
            return -1;
        }
        int id = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        return context.getResources().getDimensionPixelSize(id);
    }

    /**
     * 显示 StatusBar 跟 NavigationBar
     * @param view Visibility 为 Visible 的 View
     */
    public static void showSystemUI(View view) {
        if (! check(view, "showSystemUI", "view")) {
            return;
        }
        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN // 让布局延伸到 StatusBar
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION // 让布局延伸到 NavigationBar
        );
    }

    /**
     * 隐藏 StatusBar 跟 NavigationBar
     * @param view Visibility 为 Visible 的 View
     */
    public static void hideSystemUI(View view) {
        if (! check(view, "hideSystemUI", "view")) {
            return;
        }
        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN // 隐藏 StatusBar
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // 隐藏 NavigationBar
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN // 让布局延伸到 StatusBar
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION // 让布局延伸到 NavigationBar
        );
    }

    /**
     * 设置布局内容延伸到 StatusBar 跟 NavigationBar
     * @param view Visibility 为 Visible 的 View
     */
    public static void setContentExpandToSystemUI(View view) {
        if (! check(view, "setContentExpandToSystemUI", "view")) {
            return;
        }
        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }

    private static boolean check(Object variable, String varName, String methodName) {
        if (variable == null) {
            Log.e(TAG, methodName + "() " + varName + " == null");
            return false;
        }
        return true;
    }
}
