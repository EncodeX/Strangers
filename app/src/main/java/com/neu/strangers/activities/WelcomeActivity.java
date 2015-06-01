package com.neu.strangers.activities;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.material.widget.PaperButton;
import com.neu.strangers.R;
import com.neu.strangers.tools.ApplicationManager;
import com.neu.strangers.tools.Constants;
import com.neu.strangers.tools.DatabaseManager;
import com.neu.strangers.tools.XmppTool;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;

import net.sqlcipher.Cursor;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class WelcomeActivity extends AppCompatActivity {
	@InjectView(R.id.welcome_background)
	ImageView mWelcomeBackground;
	@InjectView(R.id.app_logo_area)
	RelativeLayout mAppLogoArea;
	@InjectView(R.id.button_login)
	PaperButton mButtonLogin;
	@InjectView(R.id.button_register)
	PaperButton mButtonRegister;

	private boolean mIsOnCreate;

	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			Intent intent = new Intent();
			intent.setClass(WelcomeActivity.this, MainActivity.class);
			startActivity(intent);
			overridePendingTransition(R.anim.fade_in_in, R.anim.fade_in_out);
			finish();
			super.handleMessage(msg);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);
		ApplicationManager.getInstance().addActivity(this);

		ButterKnife.inject(this);

		mIsOnCreate = true;

		// 此处初始化数据库
		DatabaseManager.setContext(getApplicationContext());
		DatabaseManager.getInstance();

//		ContentValues values = new ContentValues();
//		values.put("id", 1);
//		values.put("name", "达芬奇密码");
//		DatabaseManager.getInstance().insert("user", null, values);

//		Cursor cursor = DatabaseManager.getInstance().query("user", null, null, null, null, null, null);
//		if (cursor != null) {
//			while (cursor.moveToNext()) {
//				int id = cursor.getInt(cursor.getColumnIndex("id"));
//				String name = cursor.getString(cursor.getColumnIndex("name"));
//				Log.d("Database", "id is " + id);
//				Log.d("Database", "name is " + name);
//			}
//			cursor.close();
//		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.v("Test","On resume");

		SharedPreferences sharedPreferences = getSharedPreferences(Constants.Application.PREFERENCE_NAME,0);

		if(mIsOnCreate){
			ViewHelper.setY(mAppLogoArea,mAppLogoArea.getY()+dpToPx(getResources(),80));
		}

		if(sharedPreferences.getBoolean(Constants.Application.IS_LOGGED_IN,false)){
			// 检查用户是否已经登陆。 若登录，执行下面这条语句。
			mWelcomeBackground.post(new Runnable() {
				@Override
				public void run() {
					mHandler.sendEmptyMessageDelayed(0,2500);
					buildScaleAnimation(mWelcomeBackground, 1.15f, 1.15f).start();
				}
			});
		}else{
			// 用户未登录，滑出登录与注册按钮
			if(mIsOnCreate){
				AnimatorSet slideOutAnimation =
						buildSlideAnimation(mAppLogoArea, mAppLogoArea.getY()-dpToPx(getResources(),80));
				slideOutAnimation.setStartDelay(1000);
				slideOutAnimation.start();

				mButtonLogin.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						Intent intent = new Intent();
						intent.setClass(WelcomeActivity.this, LoginActivity.class);
						WelcomeActivity.this.startActivity(intent);
						WelcomeActivity.this.overridePendingTransition(R.anim.fade_in_in,R.anim.fade_in_out);
					}
				});

				mButtonRegister.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						Intent intent = new Intent();
						intent.setClass(WelcomeActivity.this, RegisterActivity.class);
						WelcomeActivity.this.startActivity(intent);
						WelcomeActivity.this.overridePendingTransition(R.anim.fade_in_in,R.anim.fade_in_out);
					}
				});

				mIsOnCreate=false;
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.menu_welcome, menu);
		return true;
	}

	private AnimatorSet buildScaleAnimation(View target, float scaleX , float scaleY){

		AnimatorSet scaleAnimation = new AnimatorSet();
		scaleAnimation.playTogether(
				ObjectAnimator.ofFloat(target, "scaleX", scaleX),
				ObjectAnimator.ofFloat(target, "scaleY", scaleY)
		);
		scaleAnimation.setInterpolator(new DecelerateInterpolator(1.0f));

		scaleAnimation.setDuration(2500);
		return scaleAnimation;
	}

	private AnimatorSet buildSlideAnimation(View target, float targetPosY){

		AnimatorSet slideAnimation = new AnimatorSet();
		slideAnimation.playTogether(
				ObjectAnimator.ofFloat(target, "translationY",targetPosY)
		);
		slideAnimation.setInterpolator(new DecelerateInterpolator(2.0f));

		slideAnimation.setDuration(2000);
		return slideAnimation;
	}

	static float dpToPx(Resources resources, float dp) {
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
	}
}
