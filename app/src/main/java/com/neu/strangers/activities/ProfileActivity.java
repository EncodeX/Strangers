package com.neu.strangers.activities;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.material.widget.PaperButton;
import com.neu.strangers.R;
import com.neu.strangers.tools.ApplicationManager;
import com.neu.strangers.tools.Constants;
import com.neu.strangers.tools.DatabaseManager;
import com.neu.strangers.view.AdvancedScrollView;
import com.neu.strangers.view.RectImageView;
import com.nineoldandroids.view.ViewHelper;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import net.sqlcipher.Cursor;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import butterknife.ButterKnife;
import butterknife.InjectView;
import me.drakeet.materialdialog.MaterialDialog;

public class ProfileActivity extends AppCompatActivity {

	private SystemBarTintManager mSystemBarTintManager;
	private int mBackgroundHeight;
	private int mAlphaToggleHeight;
	private int mBackgroundY;
	private int mProfileId;

	private boolean isInitialized = false;

	@InjectView(R.id.tool_bar)
	Toolbar mToolbar;
	@InjectView(R.id.info_scroll_view)
	AdvancedScrollView mInfoScrollView;
	@InjectView(R.id.user_info_background)
	RectImageView mUserInfoBackground;
	@InjectView(R.id.tool_bar_shadow)
	FrameLayout mToolbarShadow;
	@InjectView(R.id.user_name_label)
	RelativeLayout mUserNameLabel;
	@InjectView(R.id.user_nickname)
	TextView mUserNickname;
	@InjectView(R.id.user_name)
	TextView mUserName;
	@InjectView(R.id.user_sex_label)
	RelativeLayout mUserSexLabel;
	@InjectView(R.id.user_sex)
	ImageView mUserSex;
	@InjectView(R.id.user_region_label)
	RelativeLayout mUserRegionLabel;
	@InjectView(R.id.user_region)
	TextView mUserRegion;
	@InjectView(R.id.add_as_friend_button)
	PaperButton mAddAsFriendButton;
	@InjectView(R.id.start_chatting_button)
	PaperButton mStartChattingButton;
	@InjectView(R.id.user_sign_label)
	RelativeLayout mUserSignLabel;
	@InjectView(R.id.user_sign)
	TextView mUserSign;
	@InjectView(R.id.user_email_label)
	RelativeLayout mUserEmailLabel;
	@InjectView(R.id.user_email)
	TextView mUserEmail;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);
		ApplicationManager.getInstance().addActivity(this);

		ButterKnife.inject(this);

		if(getIntent().getExtras()!=null){
			mProfileId = getIntent().getExtras().getInt(
					Constants.Application.PROFILE_USER_ID,
					getSharedPreferences(Constants.Application.PREFERENCE_NAME,0)
							.getInt(Constants.Application.LOGGED_IN_USER_ID,-1)
			);
		}else{
			Toast.makeText(ProfileActivity.this, "未读取用户信息 无法查看个人信息", Toast.LENGTH_SHORT).show();
			this.finish();
		}
		initView();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	public void setToolbarAlpha(double alpha){
		mToolbar.setBackgroundColor((int)(0xFF * alpha) * 0x1000000 + 0x9C27B0);
	}

	public void initView(){
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

		mInfoScrollView.getViewTreeObserver().
				addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
					@Override
					public void onScrollChanged() {
						if (!isInitialized) {
							mBackgroundHeight = mUserInfoBackground.getHeight();
							mAlphaToggleHeight = mBackgroundHeight - mToolbar.getHeight();
							mBackgroundY = (int) mUserInfoBackground.getY();
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
							ViewHelper.setY(mUserInfoBackground, mBackgroundY - scrollY / 2);
						} else if (scrollY >= mBackgroundHeight) {
							ViewHelper.setY(mUserInfoBackground, mBackgroundY - mBackgroundHeight);
						} else {
							ViewHelper.setY(mUserInfoBackground, mBackgroundY);
						}
					}
				});

		if(mProfileId==-1){
			Toast.makeText(ProfileActivity.this, "找不到用户", Toast.LENGTH_SHORT).show();
			this.finish();
		}else{
			if(mProfileId == getSharedPreferences(Constants.Application.PREFERENCE_NAME,0)
					.getInt(Constants.Application.LOGGED_IN_USER_ID,-1)){
				// 登录的用户 可以修改相关信息
				initSelfProfile();
				Toast.makeText(ProfileActivity.this, "可以点击资料进行修改", Toast.LENGTH_SHORT).show();
			}else{
				// 其他用户 好友列表中存在则显示会话按钮 不存在则显示添加好友
				initOthersProfile();
			}
		}
	}

	private void initSelfProfile(){
		String[] args = {Integer.toString(mProfileId)};
		Cursor cursor = DatabaseManager.getInstance().query(
				"user", null, "id = ?", args, null, null, null);
		if (cursor != null) {
			cursor.moveToNext();

			mToolbar.setTitle(cursor.getString(cursor.getColumnIndex("nickname")));
			mUserNickname.setText(cursor.getString(cursor.getColumnIndex("nickname")));
			mUserName.setText("("+cursor.getString(cursor.getColumnIndex("username"))+")");
			if(cursor.getInt(cursor.getColumnIndex("sex"))==0){
				mUserSex.setImageResource(R.drawable.ic_male);
			}else{
				mUserSex.setImageResource(R.drawable.ic_female);
			}
			mUserRegion.setText(cursor.getString(cursor.getColumnIndex("region")));
			mUserSign.setText(cursor.getString(cursor.getColumnIndex("sign")));

			// Todo 邮件地址服务器数据库未实现
//					mUserEmail.setText(cursor.getString(cursor.getColumnIndex("")));
			// Todo 头像未获取（服务器未实现）

			cursor.close();
		}

		mAddAsFriendButton.setVisibility(View.GONE);
		mStartChattingButton.setVisibility(View.GONE);
		// todo 下面添加资料修改等功能
	}

	private void initOthersProfile(){
		String[] args = {Integer.toString(mProfileId)};
		Cursor cursor = DatabaseManager.getInstance().query(
				"user", null, "id = ?", args, null, null, null);
		if(cursor!=null){
			if(cursor.moveToNext()){
				// 好友
				mToolbar.setTitle(cursor.getString(cursor.getColumnIndex("nickname")));
				mUserNickname.setText(cursor.getString(cursor.getColumnIndex("nickname")));
				mUserName.setText("("+cursor.getString(cursor.getColumnIndex("username"))+")");
				if(cursor.getInt(cursor.getColumnIndex("sex"))==0){
					mUserSex.setImageResource(R.drawable.ic_male);
				}else{
					mUserSex.setImageResource(R.drawable.ic_female);
				}
				mUserRegion.setText(cursor.getString(cursor.getColumnIndex("region")));
				mUserSign.setText(cursor.getString(cursor.getColumnIndex("sign")));
				cursor.close();

				mAddAsFriendButton.setVisibility(View.GONE);
				mStartChattingButton.setVisibility(View.VISIBLE);
			}else{
				// 非好友
				new GetUserInfo().execute(mProfileId);

				mAddAsFriendButton.setVisibility(View.VISIBLE);
				mStartChattingButton.setVisibility(View.GONE);
			}
		}
	}

	private class GetUserInfo extends AsyncTask<Integer,Integer,JSONObject> {
		private MaterialDialog dialog;

		@Override
		protected void onPreExecute() {
			dialog = new MaterialDialog(ProfileActivity.this);

			dialog.setTitle("通信中");
			dialog.setMessage("正在获取数据...");

			dialog.show();
		}

		@Override
		protected JSONObject doInBackground(Integer... integers) {
			try {
				StringBuilder stringBuilder = new StringBuilder(
						"http://www.shiguangtravel.com:8080/CN-Soft/servlet/SearchAction");
				stringBuilder.append("?");
				stringBuilder.append("id=" + URLEncoder.encode(Integer.toString(integers[0]), "UTF-8"));

				URL url = new URL(stringBuilder.toString());
				HttpURLConnection conn = (HttpURLConnection)url.openConnection();
				conn.setRequestMethod("GET");

				InputStreamReader inputStreamReader = new InputStreamReader(conn.getInputStream());
				BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
				StringBuilder strBuffer = new StringBuilder();
				String line;

				if(conn.getResponseCode()==200){
					while ((line = bufferedReader.readLine()) != null) {
						strBuffer.append(line);
					}
					return new JSONObject(strBuffer.toString());
				}else{
					showErrorDialog("数据获取失败","无法连接服务器。");
				}
			} catch (JSONException | IOException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(JSONObject jsonObject) {
			try {
				if(jsonObject!=null){
					String username = jsonObject.getString("username");
					String nickname = jsonObject.getString("nickname");
					int sex = jsonObject.getString("sex").equals("woman")?1:0;
					String picture = jsonObject.getString("picture");
					String region = jsonObject.getString("region");
					String sign = jsonObject.getString("sign");

					mToolbar.setTitle(nickname);
					mUserNickname.setText(nickname);
					mUserName.setText("("+username+")");
					if(sex==0){
						mUserSex.setImageResource(R.drawable.ic_male);
					}else{
						mUserSex.setImageResource(R.drawable.ic_female);
					}
					mUserRegion.setText(region);
					mUserSign.setText(sign);

					dialog.dismiss();
				}else{
					showErrorDialog("数据获取失败","未知错误。");
				}
			} catch (JSONException e) {
				showErrorDialog("数据获取失败","无法解析用户数据。");
			}
		}

		private void showErrorDialog(String title, String message){
			if(dialog!=null){
				dialog.dismiss();
			}
			dialog = new MaterialDialog(ProfileActivity.this)
					.setTitle(title)
					.setMessage(message)
					.setPositiveButton("OK", new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							// ...
							dialog.dismiss();
						}
					});
			dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialogInterface) {
					ProfileActivity.this.finish();
				}
			});
			dialog.show();
		}
	}
}
