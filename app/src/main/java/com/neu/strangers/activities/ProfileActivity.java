package com.neu.strangers.activities;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import com.neu.strangers.R;
import com.neu.strangers.tools.ApplicationManager;
import com.neu.strangers.view.AdvancedScrollView;
import com.neu.strangers.view.RectImageView;
import com.nineoldandroids.view.ViewHelper;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ProfileActivity extends AppCompatActivity {

	private SystemBarTintManager mSystemBarTintManager;
	private int mBackgroundHeight;
	private int mAlphaToggleHeight;
	private int mBackgroundY;
	private boolean isInitialized = false;

	@InjectView(R.id.tool_bar)
	Toolbar mToolbar;
	@InjectView(R.id.info_scroll_view)
	AdvancedScrollView mInfoScrollView;
	@InjectView(R.id.user_info_background)
	RectImageView mUserInfoBackground;
	@InjectView(R.id.tool_bar_shadow)
	FrameLayout mToolbarShadow;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_profile);
		ApplicationManager.getInstance().addActivity(this);

		ButterKnife.inject(this);

		// Add back button
		setSupportActionBar(mToolbar);
		if(getSupportActionBar()!=null){
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}
		mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onBackPressed();
			}
		});

		mSystemBarTintManager = new SystemBarTintManager(this);
		mSystemBarTintManager.setStatusBarTintEnabled(true);
		mSystemBarTintManager.setTintColor(getResources().getColor(R.color.app_color_primary_dark));

		setToolbarAlpha(0.0);
		// todo: 注意 toolbar阴影的不透明度最大值为0.5

		mInfoScrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
			@Override
			public void onScrollChanged() {
				if(!isInitialized){
					mBackgroundHeight = mUserInfoBackground.getHeight();
					mAlphaToggleHeight = mBackgroundHeight - mToolbar.getHeight();
					mBackgroundY = (int)mUserInfoBackground.getY();
					isInitialized = true;
				}
				int scrollY = mInfoScrollView.getScrollY();

				if (scrollY < 0)
					return;

				if (scrollY < mAlphaToggleHeight) {
					double alpha = (double) scrollY / (double) mBackgroundHeight;
					setToolbarAlpha(alpha);
					mToolbarShadow.setVisibility(View.INVISIBLE);
				} else if (scrollY >= mAlphaToggleHeight) {
					setToolbarAlpha(1.0);
					mToolbarShadow.setVisibility(View.VISIBLE);
				} else {
					setToolbarAlpha(0.0);
					mToolbarShadow.setVisibility(View.INVISIBLE);
				}

				if (scrollY < mBackgroundHeight) {
					ViewHelper.setY(mUserInfoBackground, mBackgroundY - scrollY/2);
				} else if (scrollY >= mBackgroundHeight) {
					ViewHelper.setY(mUserInfoBackground, mBackgroundY - mBackgroundHeight);
				} else {
					ViewHelper.setY(mUserInfoBackground, mBackgroundY);
				}
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	public void setToolbarAlpha(double alpha){
		mToolbar.setBackgroundColor((int)(0xFF * alpha) * 0x1000000 + 0x9C27B0);
	}
}
