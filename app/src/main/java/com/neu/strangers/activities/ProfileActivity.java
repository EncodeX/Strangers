package com.neu.strangers.activities;

import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
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
	private final static int CHANGE_NAME = 1;
	private final static int CHANGE_REGION = 2;
	private final static int CHANGE_SIGN = 3;
	private final static int CHANGE_EMAIL = 4;

	private SystemBarTintManager mSystemBarTintManager;
	private int mBackgroundHeight;
	private int mAlphaToggleHeight;
	private int mBackgroundY;
	private int mProfileId;
	private MaterialDialog mDialog;

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

    //用户的信息，方便写入数据库
    private String username;
    private String nickname;
    private int sex;
    private String picture;
    private String region;
    private String sign;
	private String email;
	private String background;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);
		ApplicationManager.getInstance().addActivity(this);

		ButterKnife.inject(this);

		if(getIntent().getExtras()!=null){
			mProfileId = getIntent().getExtras().getInt(Constants.Application.PROFILE_USER_ID,-1);
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
		mToolbar.setBackgroundColor((int) (0xFF * alpha) * 0x1000000 + 0x9C27B0);
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

			nickname = cursor.getString(cursor.getColumnIndex("nickname"));
			username = cursor.getString(cursor.getColumnIndex("username"));
			sex = cursor.getInt(cursor.getColumnIndex("sex"));
			region = cursor.getString(cursor.getColumnIndex("region"));
			sign = cursor.getString(cursor.getColumnIndex("sign"));
			email = cursor.getString(cursor.getColumnIndex("email"));

			// Todo 头像与背景未获取

			cursor.close();

			mToolbar.setTitle(nickname);
			mUserNickname.setText(nickname);
			mUserName.setText("("+username+")");
			if(sex == 0){
				mUserSex.setImageResource(R.drawable.ic_male);
			}else{
				mUserSex.setImageResource(R.drawable.ic_female);
			}
			mUserRegion.setText(region);
			mUserSign.setText(sign);
			mUserEmail.setText(email);
		}

		mAddAsFriendButton.setVisibility(View.GONE);
		mStartChattingButton.setVisibility(View.GONE);

		// todo 下面添加资料修改等功能
		mDialog = new MaterialDialog(this);

		mUserNameLabel.setOnClickListener(new OnClickListener(CHANGE_NAME));

		mUserSexLabel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if(sex == 0){
					mUserSex.setImageResource(R.drawable.ic_female);
					sex = 1;
				}else if(sex == 1){
					mUserSex.setImageResource(R.drawable.ic_male);
					sex = 0;
				}
			}
		});

		mUserRegionLabel.setOnClickListener(new OnClickListener(CHANGE_REGION));

		mUserSignLabel.setOnClickListener(new OnClickListener(CHANGE_SIGN));

		mUserEmailLabel.setOnClickListener(new OnClickListener(CHANGE_EMAIL));
	}

	private void initOthersProfile(){
		String[] args = {Integer.toString(mProfileId)};
		Cursor cursor = DatabaseManager.getInstance().query(
				"friends", null, "id = ?", args, null, null, null);
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
				mUserEmail.setText(cursor.getString(cursor.getColumnIndex("email")));

				// Todo 头像与背景未获取

				cursor.close();

                //INVISIBLE控件仍然占据原来的空间
				mAddAsFriendButton.setVisibility(View.INVISIBLE);
				mStartChattingButton.setVisibility(View.VISIBLE);

				// Todo 在此处调整删除好友按钮的显隐性
			}else{
				// 非好友
				new GetUserInfo().execute(mProfileId);

				mAddAsFriendButton.setVisibility(View.VISIBLE);
                mAddAsFriendButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new AddAsFrined().execute();

                    }
                });
				mStartChattingButton.setVisibility(View.GONE);
			}
		}
        cursor.close();
	}


    private class AddAsFrined extends AsyncTask<Void,Integer,JSONObject>{
        private MaterialDialog dialog;
        private String selfId;

        @Override
        protected void onPreExecute() {
            Cursor cursor = DatabaseManager.getInstance().query("user", new String[]{"id"}, null, null, null, null, null);
            if (cursor.moveToNext()) {
                selfId = cursor.getString(0);
            }
            cursor.close();
            Log.e("id",selfId);
            Log.e("id",selfId);

            dialog = new MaterialDialog(ProfileActivity.this);
            dialog.setTitle("添加中");
            dialog.setMessage("正在获取数据...");
            dialog.show();
        }


        @Override
        protected JSONObject doInBackground(Void... voids) {
            try {
                StringBuilder stringBuilder = new StringBuilder(
                        "http://www.shiguangtravel.com:8080/CN-Soft/servlet/AddAction");
                stringBuilder.append("?");
                stringBuilder.append("id=" + URLEncoder.encode(selfId, "UTF-8") + "&");
                stringBuilder.append("fid=" + URLEncoder.encode(String.valueOf(mProfileId),"UTF-8"));
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
                }
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            if(dialog!=null){
                dialog.dismiss();
            }
            String result = jsonObject.optString("AddFriend");
            if(result.equals("success")){
                ContentValues values = new ContentValues();
                values.put("id",mProfileId);
                values.put("username",username);
                values.put("nickname",nickname);
                values.put("sex",sex);
                values.put("picture",picture);
                values.put("region",region);
                values.put("sign",sign);
	            values.put("email",email);
	            values.put("background",background);

                DatabaseManager.getInstance().insert("friends",null,values);

                Toast.makeText(ProfileActivity.this,"添加成功！",Toast.LENGTH_LONG).show();


            }else{
                Toast.makeText(ProfileActivity.this,"添加失败！",Toast.LENGTH_LONG).show();
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
					username = jsonObject.getString("username");
					nickname = jsonObject.getString("nickname");
					sex = jsonObject.getString("sex").equals("woman")?1:0;
					picture = jsonObject.getString("picture");
					region = jsonObject.getString("region");
					sign = jsonObject.getString("sign");
					email = jsonObject.getString("email");
					background = jsonObject.getString("mybackground");

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
					mUserEmail.setText(email);

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

	private class OnClickListener implements View.OnClickListener{
		private int mode;

		public OnClickListener(int mode) {
			this.mode = mode;
		}

		@Override
		public void onClick(View view) {
			LayoutInflater inflate = ProfileActivity.this.getLayoutInflater();
			View dialogContent = inflate.inflate(R.layout.dialog_edittext, null);

			mDialog = new MaterialDialog(ProfileActivity.this);

			mDialog.setContentView(dialogContent);
			mDialog.show();
			switch(mode){
				case CHANGE_NAME:
					break;
				case CHANGE_REGION:
					break;
				case CHANGE_SIGN:
					break;
				case CHANGE_EMAIL:
					break;
			}
		}
	}
}
