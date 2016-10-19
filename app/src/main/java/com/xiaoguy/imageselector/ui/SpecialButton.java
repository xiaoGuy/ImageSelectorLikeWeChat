package com.xiaoguy.imageselector.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

/**
 * 拦截点击事件，按下时将所有的子 View 设为 pressed 状态；松开时恢复状态
 */

public class SpecialButton extends LinearLayout {

    public SpecialButton(Context context) {
        super(context);
        setClickable(true);
    }

    public SpecialButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setClickable(true);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    @Override
    protected void dispatchSetPressed(boolean pressed) {
        int count = getChildCount();

        for (int i = 0; i < count; i ++) {
            View view = getChildAt(i);
            view.setPressed(pressed);
        }
    }
}
