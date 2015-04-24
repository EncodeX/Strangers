package com.neu.strangers.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.balysv.materialripple.MaterialRippleLayout;

/**
 * Created with Android Studio.
 * Author: Enex Tapper
 * Date: 15/4/22
 * Project: Strangers
 * Package: com.neu.strangers.view
 */
public class MyRippleLayout extends MaterialRippleLayout {

	public MyRippleLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return super.onTouchEvent(event);
	}
}
