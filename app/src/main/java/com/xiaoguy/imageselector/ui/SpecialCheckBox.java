package com.xiaoguy.imageselector.ui;

import android.content.Context;
import android.support.v7.widget.AppCompatCheckBox;
import android.util.AttributeSet;

/**
 * <p>这是一个特殊的 CheckBox </p>
 * 点击它并不会选中或者取消选中，要想实现这些效果，需要在 OnClickListener
 * 中手动调用 setChecked() 来实现
 */

public class SpecialCheckBox extends AppCompatCheckBox {

    public SpecialCheckBox(Context context) {
        super(context);
    }

    public SpecialCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void toggle() {
    }

    public void doToggle() {
        super.toggle();
    }
}
