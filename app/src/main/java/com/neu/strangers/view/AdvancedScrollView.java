package com.neu.strangers.view;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

/**
 * Created with Android Studio.
 * Author: Enex Tapper
 * Date: 15/5/21
 * Project: Strangers
 * Package: com.neu.strangers.view
 */
public class AdvancedScrollView extends ScrollView {
	private OnScrollListener onScrollListener;
	/**
	 * 主要是用在用户手指离开MyScrollView，MyScrollView还在继续滑动，我们用来保存Y的距离，然后做比较
	 */
	private int lastScrollY;

	public AdvancedScrollView(Context context) {
		this(context, null);
	}

	public AdvancedScrollView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public AdvancedScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	/**
	 * 设置滚动接口
	 * @param onScrollListener
	 */
	public void setOnScrollListener(OnScrollListener onScrollListener) {
		this.onScrollListener = onScrollListener;
	}


	/**
	 * 用于用户手指离开MyScrollView的时候获取MyScrollView滚动的Y距离，然后回调给onScroll方法中
	 */
	private Handler handler = new Handler() {

		public void handleMessage(android.os.Message msg) {
			int scrollY = AdvancedScrollView.this.getScrollY();

			//此时的距离和记录下的距离不相等，在隔5毫秒给handler发送消息
			if(lastScrollY != scrollY){
				lastScrollY = scrollY;
				handler.sendMessageDelayed(handler.obtainMessage(), 10);
			}
			if(onScrollListener != null){
				onScrollListener.onScroll(scrollY);
			}

		};

	};

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		return super.onTouchEvent(ev);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		switch(ev.getAction()){
			case MotionEvent.ACTION_UP:
				handler.sendMessageDelayed(handler.obtainMessage(), 10);
				break;
			case MotionEvent.ACTION_MOVE:
				if(onScrollListener != null){
					onScrollListener.onScroll(lastScrollY = this.getScrollY());
				}
				break;
		}
		return super.dispatchTouchEvent(ev);
	}

	/**
	 *
	 * 滚动的回调接口
	 *
	 * @author xiaanming
	 *
	 */
	public interface OnScrollListener{
		/**
		 * 回调方法， 返回MyScrollView滑动的Y方向距离
		 * @param scrollY
		 *
		 */
		public void onScroll(int scrollY);
	}



}