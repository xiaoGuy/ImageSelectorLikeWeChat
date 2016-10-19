package com.xiaoguy.imageselector.util;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;

/**
 * Created by XiaoGuy on 2016/10/19.
 */

public abstract class DrawableUtil {

    public static Drawable setTintList(Context context, @DrawableRes int drawableResId,
                                   @ColorRes int colorResId) {
        Drawable result = ContextCompat.getDrawable(context, drawableResId);
        ColorStateList colorStateList = ContextCompat.getColorStateList(context, colorResId);
        DrawableCompat.setTintList(result, colorStateList);
        return result;
    }

    public static Drawable setTint(Context context, @DrawableRes int drawableResId,
                                   @ColorRes int colorResId) {
        Drawable result = ContextCompat.getDrawable(context, drawableResId);
        int color = ContextCompat.getColor(context, colorResId);
        DrawableCompat.setTint(result, color);
        return result;
    }
}
