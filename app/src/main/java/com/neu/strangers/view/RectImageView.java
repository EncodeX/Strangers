package com.neu.strangers.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created with IntelliJ IDEA.
 * Author: Enex Tapper
 * Date: 14-6-21
 * Project: ${PROJECT_NAME}
 * Package: ${PACKAGE_NAME}
 */
public class RectImageView extends ImageView{
	public RectImageView(Context context){
		super(context);
	}
	public RectImageView(Context context, AttributeSet attrs){
		super(context,attrs);
	}
	public RectImageView(Context context, AttributeSet attrs, int defStyle){
		super(context,attrs,defStyle);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(getDefaultSize(0, widthMeasureSpec), getDefaultSize(0, heightMeasureSpec));
		heightMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredWidth()*9/16, MeasureSpec.EXACTLY);
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
}
