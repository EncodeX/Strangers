package com.neu.strangers.adapter;

import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;


import com.neu.strangers.R;
import com.neu.strangers.bean.ChatInfo;
import com.nineoldandroids.view.ViewHelper;

import de.hdodenhof.circleimageview.CircleImageView;

@SuppressLint("NewApi")
public class ChatAdapter extends BaseAdapter {
	private Context mContext;
	private List<ChatInfo> mList;
    
	// 弹出的更多选择框 
	private PopupWindow mPopupWindow;

	// 复制，删除 
	private TextView mCopy, mDelete;

	private LayoutInflater mInflater;
	
	 // 执行动画的时间
	 
	protected long mAnimationTime = 150;

	public ChatAdapter(Context mContext, List<ChatInfo> mList) {
		super();
		this.mContext = mContext;
		this.mList = mList;
		mInflater = LayoutInflater.from(mContext);
		initPopWindow();
	}

	public void setList(List<ChatInfo> mList) {
		this.mList = mList;
	}

	@Override
	public int getCount() {
		
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		
		ViewHodler hodler;
		if (convertView == null) {
			hodler = new ViewHodler();
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.chat_list_item, null);
			hodler.fromContainer = (ViewGroup) convertView
					.findViewById(R.id.chart_from_container);
			hodler.toContainer = (ViewGroup) convertView
					.findViewById(R.id.chart_to_container);
			hodler.fromContent = (TextView) convertView
					.findViewById(R.id.chatfrom_content);
			hodler.toContent = (TextView) convertView
					.findViewById(R.id.chatto_content);
			hodler.time = (TextView) convertView.findViewById(R.id.chat_time);
			convertView.setTag(hodler);
		} else {
			hodler = (ViewHodler) convertView.getTag();
		}

		if (mList.get(position).fromOrTo == 0) {
			// 收到消息 from显示
			hodler.toContainer.setVisibility(View.GONE);
			hodler.fromContainer.setVisibility(View.VISIBLE);
            hodler.fromContent.setText(mList.get(position).content);
			hodler.time.setText(mList.get(position).content);
		} else {
			// 发送消息 to显示
			hodler.toContainer.setVisibility(View.VISIBLE);
			hodler.fromContainer.setVisibility(View.GONE);
			hodler.toContent.setText(mList.get(position).content);
			hodler.time.setText(mList.get(position).time);
		}
		hodler.fromContent.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				

			}
		});
		hodler.toContent.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				

			}
		});

		// 设置+按钮点击效果
		hodler.fromContent.setOnLongClickListener(new popAction(convertView,
				position, mList.get(position).fromOrTo));
		hodler.toContent.setOnLongClickListener(new popAction(convertView,
				position, mList.get(position).fromOrTo));
		return convertView;
	}



	class ViewHodler {
		CircleImageView fromIcon, toIcon;
		TextView fromContent, toContent, time;
		ViewGroup fromContainer, toContainer;
	}

	/**
	 * 屏蔽listitem的所有事件
	 * */
	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	@Override
	public boolean isEnabled(int position) {
		return false;
	}

	/**
	 * 初始化弹出的pop
	 * */
	private void initPopWindow() {
		View popView = mInflater.inflate(R.layout.chat_item_menu,
				null);
		mCopy = (TextView) popView.findViewById(R.id.chat_copy_menu);
		mDelete = (TextView) popView.findViewById(R.id.chat_delete_menu);
		mPopupWindow = new PopupWindow(popView, LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		mPopupWindow.setBackgroundDrawable(new ColorDrawable(0));
		// 设置popwindow出现和消失动画
		// mPopupWindow.setAnimationStyle(R.style.PopMenuAnimation);
	}

	/**
	 * 显示popWindow
	 * */
	public void showPop(View parent, int x, int y, final View view,
			final int position, final int fromOrTo) {
		// 设置popwindow显示位置
		mPopupWindow.showAtLocation(parent, 0, x, y);
		// 获取popwindow焦点
		mPopupWindow.setFocusable(true);
		// 设置popwindow如果点击外面区域，便关闭。
		mPopupWindow.setOutsideTouchable(true);
		// 为按钮绑定事件
		// 复制
		mCopy.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				
				if (mPopupWindow.isShowing()) {
					mPopupWindow.dismiss();
				}
				// 获取剪贴板管理服务
				ClipboardManager cm = (ClipboardManager) mContext
						.getSystemService(Context.CLIPBOARD_SERVICE);
				// 将文本数据复制到剪贴板
				cm.setText(mList.get(position).content);
			}
		});
		// 删除
		mDelete.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				
				if (mPopupWindow.isShowing()) {
					mPopupWindow.dismiss();
				}
				if (fromOrTo == 0) {
					// from
					leftRemoveAnimation(view, position);
				} else if (fromOrTo == 1) {
					// to
					rightRemoveAnimation(view, position);
				}

				// mList.remove(position);
				// notifyDataSetChanged();
			}
		});
		mPopupWindow.update();
		if (mPopupWindow.isShowing()) {

		}
	}

	/**
	 * 每个ITEM中more按钮对应的点击动作
	 * */
	public class popAction implements OnLongClickListener {
		int position;
		View view;
		int fromOrTo;

		public popAction(View view, int position, int fromOrTo) {
			this.position = position;
			this.view = view;
			this.fromOrTo = fromOrTo;
		}

		@Override
		public boolean onLongClick(View v) {
			
			int[] arrayOfInt = new int[2];
			// 获取点击按钮的坐标
			v.getLocationOnScreen(arrayOfInt);
			int x = arrayOfInt[0];
			int y = arrayOfInt[1];
			// System.out.println("x: " + x + " y:" + y + " w: " +
			// v.getMeasuredWidth() + " h: " + v.getMeasuredHeight() );
			showPop(v, x, y, view, position, fromOrTo);
			return true;
		}
	}

	/**
	 * item删除动画
	 * */
	private void rightRemoveAnimation(final View view, final int position) {
		final Animation animation = (Animation) AnimationUtils.loadAnimation(
				mContext, R.anim.chatto_remove_anim);
		animation.setAnimationListener(new AnimationListener() {
			public void onAnimationStart(Animation animation) {
			}

			public void onAnimationRepeat(Animation animation) {
			}

			public void onAnimationEnd(Animation animation) {
				view.setAlpha(0);
				performDismiss(view, position);
				animation.cancel();
			}
		});

		view.startAnimation(animation);
	}

	/**
	 * item删除动画
	 * */
	private void leftRemoveAnimation(final View view, final int position) {
		final Animation animation = (Animation) AnimationUtils.loadAnimation(
				mContext, R.anim.chatfrom_remove_anim);
		animation.setAnimationListener(new AnimationListener() {
			public void onAnimationStart(Animation animation) {
			}

			public void onAnimationRepeat(Animation animation) {
			}

			public void onAnimationEnd(Animation animation) {
				view.setAlpha(0);
				performDismiss(view, position);
				animation.cancel();
			}
		});

		view.startAnimation(animation);
	}

	/**
	 * 在此方法中执行item删除之后，其他的item向上或者向下滚动的动画，并且将position回调到方法onDismiss()中
	 * 
	 * @param dismissView
	 * @param dismissPosition
	 */
	private void performDismiss(final View dismissView,
			final int dismissPosition) {
		final LayoutParams lp = dismissView.getLayoutParams();// 获取item的布局参数
		final int originalHeight = dismissView.getHeight();// item的高度

		ValueAnimator animator = ValueAnimator.ofInt(originalHeight, 0)
				.setDuration(mAnimationTime);
		animator.start();

		animator.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				mList.remove(dismissPosition);
				notifyDataSetChanged();
				// 这段代码很重要，因为我们并没有将item从ListView中移除，而是将item的高度设置为0
				// 所以我们在动画执行完毕之后将item设置回来
				ViewHelper.setAlpha(dismissView, 1f);
				ViewHelper.setTranslationX(dismissView, 0);
				LayoutParams lp = dismissView.getLayoutParams();
				lp.height = originalHeight;
				dismissView.setLayoutParams(lp);
			}
		});

		animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator valueAnimator) {
				// 这段代码的效果是ListView删除某item之后，其他的item向上滑动的效果
				lp.height = (Integer) valueAnimator.getAnimatedValue();
				dismissView.setLayoutParams(lp);
			}
		});

	}

}
