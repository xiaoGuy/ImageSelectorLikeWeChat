package com.xiaoguy.imageselector.util;

import android.content.Context;
import android.support.annotation.StringRes;
import android.widget.Toast;

/**
 * Created by XiaoGuy on 2016/10/23.
 */

public class WidgetUtil {

    private Toast mToast;

    public void showToast(Context context,  @StringRes int stringResId) {
        showToast(context, context.getString(stringResId));
    }

    public void showToast(Context context, String text) {
        if (mToast == null) {
            mToast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        }
        mToast.setText(text);
        mToast.show();
    }
}
