package com.neu.strangers.activities;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.material.widget.FloatingEditText;
import com.material.widget.PaperButton;
import com.neu.strangers.R;
import com.neu.strangers.tools.ApplicationManager;
import com.neu.strangers.tools.Constants;
import com.neu.strangers.tools.DatabaseManager;
import com.neu.strangers.tools.ImageCache;
import com.neu.strangers.tools.UploadUtils;
import com.neu.strangers.view.RectImageView;
import com.nineoldandroids.view.ViewHelper;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import net.sqlcipher.Cursor;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.hdodenhof.circleimageview.CircleImageView;
import me.drakeet.materialdialog.MaterialDialog;

public class ProfileActivity extends AppCompatActivity implements OnUserInfoChangedListener {
	private final static int CHANGE_NICKNAME = 1;
	private final static int CHANGE_REGION = 2;
	private final static int CHANGE_SIGN = 3;
	private final static int CHANGE_EMAIL = 4;
	private final static int CHANGE_SEX = 5;

	private SystemBarTintManager mSystemBarTintManager;
	private int mBackgroundHeight;
	private int mAlphaToggleHeight;
	private int mBackgroundY;
	private int mProfileId;
	private MaterialDialog mDialog;
	private String mPhotoFullPath;
	private ImageCache mImageCache;
	private OnUserInfoChangedListener mOnUserInfoChangedListener;

	private boolean isInitialized = false;
	private boolean isAvatarChanged = false;

	@InjectView(R.id.tool_bar)
	Toolbar mToolbar;
	@InjectView(R.id.info_scroll_view)
	ScrollView mInfoScrollView;
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
	@InjectView(R.id.user_avatar)
	CircleImageView mUserAvatar;

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

		mImageCache = new ImageCache(this);
		mImageCache.setOnBitmapPreparedListener(new ImageCache.OnBitmapPreparedListener() {
			@Override
			public void onBitmapPrepared(Bitmap bitmap, String tag) {
				mUserAvatar.setImageBitmap(bitmap);
			}
		});

		initView();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Intent intent;
		if(resultCode == Activity.RESULT_OK){
			switch (requestCode){
				case Constants.Action.TAKE_PICTURE:
					mDialog.dismiss();
					mDialog = new MaterialDialog(ProfileActivity.this);
					mDialog.setTitle("请等待");
					mDialog.setMessage("启动裁剪程序...");
					mDialog.show();

					intent = new Intent("com.android.camera.action.CROP");
					//可以选择图片类型，如果是*表明所有类型的图片
					intent.setDataAndType(Uri.fromFile(new File(mPhotoFullPath)), "image/*");
					// 下面这个crop = true是设置在开启的Intent中设置显示的VIEW可裁剪
					intent.putExtra("crop", "true");
					// aspectX aspectY 是宽高的比例，这里设置的是正方形（长宽比为1:1）
					intent.putExtra("aspectX", 1);
					intent.putExtra("aspectY", 1);
					// outputX outputY 是裁剪图片宽高
					intent.putExtra("outputX", 480);
					intent.putExtra("outputY", 480);
					//裁剪时是否保留图片的比例，这里的比例是1:1
					intent.putExtra("scale", true);
					//是否是圆形裁剪区域，设置了也不一定有效
					//intent.putExtra("circleCrop", true);
					//设置输出的格式
					intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
					//是否将数据保留在Bitmap中返回
					intent.putExtra("return-data", true);

					intent.putExtra(
							MediaStore.EXTRA_OUTPUT,
							Uri.fromFile(new File(mPhotoFullPath.replace(".jpg","_0.jpg"))));

					startActivityForResult(intent, Constants.Action.CROP_PICTURE);
					break;
				case Constants.Action.SELECT_PICTURE:
					mDialog.dismiss();
					mDialog = new MaterialDialog(ProfileActivity.this);
					mDialog.setTitle("请等待");
					mDialog.setMessage("启动裁剪程序...");
					mDialog.show();

					Uri selectedImage = data.getData();
					String[] filePathColumns={MediaStore.Images.Media.DATA};
					android.database.Cursor c =
							this.getContentResolver().query(selectedImage, filePathColumns, null,null, null);
					c.moveToFirst();
					int columnIndex = c.getColumnIndex(filePathColumns[0]);
					String picturePath= c.getString(columnIndex);
					c.close();

					intent = new Intent("com.android.camera.action.CROP");
					intent.setDataAndType(selectedImage, "image/*");
					intent.putExtra("crop", "true");
					intent.putExtra("aspectX", 1);
					intent.putExtra("aspectY", 1);
					intent.putExtra("outputX", 480);
					intent.putExtra("outputY", 480);
					intent.putExtra("scale", true);
					intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
					intent.putExtra("return-data", true);

					Log.v("Test",selectedImage.getPath());

					String name = DateFormat.format("yyyyMMdd_hhmmss", Calendar.getInstance(Locale.CHINA))+".jpg";
					File file = getFileDir(ProfileActivity.this, "photos");
					mPhotoFullPath = file.getPath() + File.separator + name;

					intent.putExtra(
							MediaStore.EXTRA_OUTPUT,
							Uri.fromFile(new File(mPhotoFullPath.replace(".jpg","_0.jpg"))));

					startActivityForResult(intent, Constants.Action.CROP_PICTURE);

					break;
				case Constants.Action.CROP_PICTURE:
					new UploadImage(mPhotoFullPath.replace(".jpg","_0.jpg")).execute();
					break;
			}
		}else{
			mDialog.dismiss();
		}
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		switch (event.getKeyCode()){
			case KeyEvent.KEYCODE_BACK:
				if(isAvatarChanged){
					ApplicationManager.getInstance().clearOtherActivities(ProfileActivity.this);
					Intent intent = new Intent();
					intent.setClass(ProfileActivity.this, MainActivity.class);
					startActivity(intent);
					finish();
					overridePendingTransition(R.anim.fade_out_in, R.anim.fade_out_out);
					return true;
				}else{
					finish();
					overridePendingTransition(R.anim.fade_out_in, R.anim.fade_out_out);
					return true;
				}
		}

		return super.dispatchKeyEvent(event);
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
				if(isAvatarChanged){
					ApplicationManager.getInstance().clearOtherActivities(ProfileActivity.this);
					Intent intent = new Intent();
					intent.setClass(ProfileActivity.this, MainActivity.class);
					startActivity(intent);
					finish();
					overridePendingTransition(R.anim.fade_out_in, R.anim.fade_out_out);
				}else{
					finish();
					overridePendingTransition(R.anim.fade_out_in, R.anim.fade_out_out);
				}
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

			// Todo 背景未获取

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

		mUserNameLabel.setOnClickListener(new OnClickListener(CHANGE_NICKNAME));

		mUserSexLabel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (sex == 0) {
					mUserSex.setImageResource(R.drawable.ic_female);
					sex = 1;
					new ChangeInformation(CHANGE_SEX,"woman").execute();
				} else if (sex == 1) {
					mUserSex.setImageResource(R.drawable.ic_male);
					sex = 0;
					new ChangeInformation(CHANGE_SEX,"man").execute();
				}
			}
		});

		mUserRegionLabel.setOnClickListener(new OnClickListener(CHANGE_REGION));

		mUserSignLabel.setOnClickListener(new OnClickListener(CHANGE_SIGN));

		mUserEmailLabel.setOnClickListener(new OnClickListener(CHANGE_EMAIL));

		mUserAvatar.setOnClickListener(new ShowChangeMethod());

		new GetSelfAvatar().execute();

		this.setOnUserInfoChangedListener(this);
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

				// Todo 背景未获取

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

		new GetOtherAvatar().execute();
	}

	private File getFileDir(Context context, String dirName) {
		String cachePath;
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
				|| !Environment.isExternalStorageRemovable()) {
			if(context.getExternalCacheDir() != null){
				cachePath = context.getExternalCacheDir().getPath();
			}else{
				cachePath = context.getCacheDir().getPath();
			}
		} else {
			cachePath = context.getCacheDir().getPath();
		}
		return new File(cachePath + File.separator + dirName);
	}

	@Override
	public void onUserInfoChanged(int mode, String string) {
		switch (mode){
			case CHANGE_NICKNAME:
				mUserNickname.setText(string);
				break;
			case CHANGE_REGION:
				mUserRegion.setText(string);
				break;
			case CHANGE_EMAIL:
				mUserEmail.setText(string);
				break;
			case CHANGE_SIGN:
				mUserSign.setText(string);
				break;
		}
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
				showErrorDialog("数据获取失败", "无法解析用户数据。");
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
			TextView title = (TextView)dialogContent.findViewById(R.id.title);
			final FloatingEditText editText = (FloatingEditText)dialogContent.findViewById(R.id.edit_text);
			PaperButton confirmButton = (PaperButton)dialogContent.findViewById(R.id.confirm_button);
			PaperButton cancelButton = (PaperButton)dialogContent.findViewById(R.id.cancel_button);

			switch(mode){
				case CHANGE_NICKNAME:
					title.setText("修改昵称");
					break;
				case CHANGE_REGION:
					title.setText("修改地区");
					break;
				case CHANGE_SIGN:
					title.setText("修改签名");
					break;
				case CHANGE_EMAIL:
					title.setText("修改邮箱地址");
					break;
			}

			confirmButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					new ChangeInformation(mode,editText.getText().toString()).execute();
				}
			});

			cancelButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					mDialog.dismiss();
				}
			});

			mDialog = new MaterialDialog(ProfileActivity.this);

			mDialog.setContentView(dialogContent);
			mDialog.show();
		}
	}

	private class ShowChangeMethod implements View.OnClickListener{

		@Override
		public void onClick(View view) {
			LayoutInflater inflate = ProfileActivity.this.getLayoutInflater();
			View dialogContent = inflate.inflate(R.layout.dialog_change_image, null);

			TextView takePhoto = (TextView)dialogContent.findViewById(R.id.take_photo);
			TextView selectFromLibrary = (TextView)dialogContent.findViewById(R.id.select_from_library);

			takePhoto.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

					String name = DateFormat.format("yyyyMMdd_hhmmss", Calendar.getInstance(Locale.CHINA))+".jpg";
					File file = getFileDir(ProfileActivity.this, "photos");
					file.mkdirs();
					mPhotoFullPath = file.getPath() + File.separator + name;
					Uri imageUri = Uri.fromFile(new File(file.getPath(), name));
					intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

					startActivityForResult(intent, Constants.Action.TAKE_PICTURE);
				}
			});

			selectFromLibrary.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

					startActivityForResult(intent, Constants.Action.SELECT_PICTURE);
				}
			});

			mDialog = new MaterialDialog(ProfileActivity.this);

			mDialog.setContentView(dialogContent);
			mDialog.show();
		}
	}

	private class UploadImage extends AsyncTask<String,Void,JSONObject>{
		private String imagePath;

		public UploadImage(String imagePath) {
			this.imagePath = imagePath;
		}

		@Override
		protected JSONObject doInBackground(String... strings) {
			try {
				StringBuilder stringBuilder = new StringBuilder(
						"http://www.shiguangtravel.com:8080/CN-Soft/servlet/UploadFile");
				stringBuilder.append("?");
				stringBuilder.append("up=1&");
				stringBuilder.append("id=" + URLEncoder.encode(Integer.toString(mProfileId), "UTF-8"));

				File file = new File(imagePath);

				String result = UploadUtils.uploadFile(file, stringBuilder.toString());

				return new JSONObject(result);
			} catch (UnsupportedEncodingException | JSONException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(JSONObject jsonObject) {
			try {
				if(jsonObject!=null){
					String imageUrl = jsonObject.getString("upload");
					DatabaseManager.getInstance().execSQL(
							"UPDATE user SET picture='"+imageUrl+"' WHERE Id="+mProfileId+";");

					mUserAvatar.setImageDrawable(BitmapDrawable.createFromPath(imagePath));

//					String[] args = {Integer.toString(mProfileId)};
//					Cursor cursor = DatabaseManager.getInstance().query(
//							"user", null, "id = ?", args, null, null, null);
//					if (cursor != null) {
//						cursor.moveToNext();
//
//						Log.v("Upload",cursor.getString(cursor.getColumnIndex("picture")));
//
//						cursor.close();
//					}
					mDialog.dismiss();
					isAvatarChanged = true;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	private class GetSelfAvatar extends AsyncTask<String,Void,Integer>{
		@Override
		protected Integer doInBackground(String... strings) {
			// todo 今后需要优化 profile activity需要给出信号 不需要每次都刷新
			Cursor cursor = DatabaseManager.getInstance().query("user", null, null, null, null, null, null);
			if (cursor != null) {
				cursor.moveToNext();

				String picture = cursor.getString(cursor.getColumnIndex("picture"));
				mImageCache.loadImage(picture,"menu_icon");

				cursor.close();
			}
			return null;
		}
	}

	private class GetOtherAvatar extends AsyncTask<String,Void,Integer>{
		@Override
		protected Integer doInBackground(String... strings) {
			String[] args = {Integer.toString(mProfileId)};
			Cursor cursor = DatabaseManager.getInstance().query(
					"friends", null, "id = ?", args, null, null, null);
			if (cursor != null) {
				cursor.moveToNext();

				String picture = cursor.getString(cursor.getColumnIndex("picture"));
				mImageCache.loadImage(picture,picture);

				cursor.close();
			}
			return null;
		}
	}

	private class ChangeInformation extends AsyncTask<String,Void,JSONObject>{
		private String string;
		private int mode;

		public ChangeInformation(int mode, String string) {
			this.mode = mode;
			this.string = string;
		}

		@Override
		protected JSONObject doInBackground(String... strings) {
			try {

				StringBuilder stringBuilder = new StringBuilder(
						"http://www.shiguangtravel.com:8080/CN-Soft/servlet/AlterAction");
				stringBuilder.append("?");
				stringBuilder.append("id=" + URLEncoder.encode(Integer.toString(mProfileId), "UTF-8") + "&");

				switch (mode){
					case CHANGE_NICKNAME:
						mDialog.dismiss();
						stringBuilder.append("nickname=" + URLEncoder.encode(string, "UTF-8"));
						break;
					case CHANGE_REGION:
						mDialog.dismiss();
						stringBuilder.append("region=" + URLEncoder.encode(string, "UTF-8"));
						break;
					case CHANGE_SIGN:
						mDialog.dismiss();
						stringBuilder.append("sign=" + URLEncoder.encode(string, "UTF-8"));
						break;
					case CHANGE_EMAIL:
						mDialog.dismiss();
						stringBuilder.append("email=" + URLEncoder.encode(string, "UTF-8"));
						break;
					case CHANGE_SEX:
						stringBuilder.append("sex=" + URLEncoder.encode(string, "UTF-8"));
						break;
				}

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

				return null;
			} catch (JSONException | IOException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(JSONObject jsonObject) {
			try {
				if(jsonObject!=null){
					String result = jsonObject.getString("Alter");
					if(!result.equals("sucess")){
						// 修改失败
						Toast.makeText(ProfileActivity.this, "修改失败", Toast.LENGTH_SHORT).show();
					}else{
						// 修改成功
						switch (mode){
							case CHANGE_NICKNAME:
								DatabaseManager.getInstance().execSQL(
										"UPDATE user SET nickname='"+string+"' WHERE Id="+mProfileId+";");
								Toast.makeText(ProfileActivity.this, "昵称修改成功", Toast.LENGTH_SHORT).show();
								break;
							case CHANGE_REGION:
								DatabaseManager.getInstance().execSQL(
										"UPDATE user SET region='"+string+"' WHERE Id="+mProfileId+";");
								Toast.makeText(ProfileActivity.this, "地区修改成功", Toast.LENGTH_SHORT).show();
								break;
							case CHANGE_SIGN:
								DatabaseManager.getInstance().execSQL(
										"UPDATE user SET sign='" + string+"' WHERE Id="+mProfileId+";");
								Toast.makeText(ProfileActivity.this, "签名修改成功", Toast.LENGTH_SHORT).show();
								break;
							case CHANGE_EMAIL:
								DatabaseManager.getInstance().execSQL(
										"UPDATE user SET email='" +string+"' WHERE Id="+mProfileId+";");
								Toast.makeText(ProfileActivity.this, "邮箱地址修改成功", Toast.LENGTH_SHORT).show();
								break;
							case CHANGE_SEX:
								if(string.equals("woman")){
									DatabaseManager.getInstance().execSQL(
											"UPDATE user SET sex=1 WHERE Id=" + mProfileId + ";");
								}else{
									DatabaseManager.getInstance().execSQL(
											"UPDATE user SET sex=0 WHERE Id=" + mProfileId + ";");
								}
								Toast.makeText(ProfileActivity.this, "性别修改成功", Toast.LENGTH_SHORT).show();
								break;
						}
						mOnUserInfoChangedListener.onUserInfoChanged(mode,string);
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	public void setOnUserInfoChangedListener(OnUserInfoChangedListener onUserInfoChangedListener) {
		this.mOnUserInfoChangedListener = onUserInfoChangedListener;
	}
}

interface OnUserInfoChangedListener{
	void onUserInfoChanged(int mode, String string);
}