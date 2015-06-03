package com.neu.strangers.activities;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.material.widget.FloatingEditText;
import com.material.widget.PaperButton;
import com.neu.strangers.R;
import com.neu.strangers.tools.ApplicationManager;
import com.neu.strangers.tools.Constants;
import com.neu.strangers.tools.DatabaseManager;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import net.sqlcipher.Cursor;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.ButterKnife;
import butterknife.InjectView;
import me.drakeet.materialdialog.MaterialDialog;

public class RegisterActivity extends AppCompatActivity {
	private final static int RESPONSE_SUCCESS = 0;
	private final static int RESPONSE_NAME_ERROR = 1;
	private final static int RESPONSE_PHONE_ERROR = 2;
	private final static int RESPONSE_EMAIL_ERROR = 3;

	private SystemBarTintManager mSystemBarTintManager;

	@InjectView(R.id.tool_bar)
	Toolbar mToolbar;
	@InjectView(R.id.register_button)
	PaperButton mRegisterButton;
	@InjectView(R.id.user_name_input)
	FloatingEditText mUserNameInput;
	@InjectView(R.id.password_input)
	FloatingEditText mPasswordInput;
	@InjectView(R.id.password_second_input)
	FloatingEditText mPasswordSecondInput;
	@InjectView(R.id.phone_input)
	FloatingEditText mPhoneInput;
	@InjectView(R.id.email_input)
	FloatingEditText mEmailInput;
	@InjectView(R.id.error_dialog)
	TextView mErrorDialog;

	private MaterialDialog mRegisterDialog;
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what){
				case 0:
					ApplicationManager.getInstance().clearOtherActivities(RegisterActivity.this);
					Intent intent = new Intent();
					intent.setClass(RegisterActivity.this, MainActivity.class);
					startActivity(intent);
					overridePendingTransition(R.anim.fade_in_in, R.anim.fade_in_out);
					finish();
					break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
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
				RegisterActivity.this.overridePendingTransition(R.anim.fade_out_in, R.anim.fade_out_out);
			}
		});

		mSystemBarTintManager = new SystemBarTintManager(this);
		mSystemBarTintManager.setStatusBarTintEnabled(true);
		mSystemBarTintManager.setTintColor(getResources().getColor(R.color.app_color_primary_dark));

		mRegisterButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if(mErrorDialog.getVisibility()==View.VISIBLE){
					mErrorDialog.setVisibility(View.GONE);
				}
				if(!mPasswordInput.getText().toString().equals(mPasswordSecondInput.getText().toString())){
					mErrorDialog.setText("两次密码不一致");
					mErrorDialog.setVisibility(View.VISIBLE);
					return;
				}

				if(!isPhoneNumber(mPhoneInput.getText().toString())){
					mErrorDialog.setText("手机号码格式不正确");
					mErrorDialog.setVisibility(View.VISIBLE);
					return;
				}

				if(!isEmail(mEmailInput.getText().toString())){
					mErrorDialog.setText("邮箱格式不正确");
					mErrorDialog.setVisibility(View.VISIBLE);
					return;
				}

				mRegisterDialog = new MaterialDialog(RegisterActivity.this)
						.setTitle("正在连接服务器")
						.setMessage("请等待...");
				mUserNameInput.setEnabled(false);
				mPasswordInput.setEnabled(false);
				mPasswordSecondInput.setEnabled(false);
				mPhoneInput.setEnabled(false);
				mEmailInput.setEnabled(false);

				mRegisterDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialogInterface) {
						mUserNameInput.setEnabled(true);
						mPasswordInput.setEnabled(true);
						mPasswordSecondInput.setEnabled(true);
						mPhoneInput.setEnabled(true);
						mEmailInput.setEnabled(true);
					}
				});
//				mRegisterDialog.setCanceledOnTouchOutside(true);
				mRegisterDialog.show();

				new DoRegister().execute(
						mUserNameInput.getText().toString(),
						mPasswordInput.getText().toString(),
						mPhoneInput.getText().toString(),
						mEmailInput.getText().toString());
			}
		});
	}

	public static boolean isEmail(String email) {
		String str = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
		Pattern p = Pattern.compile(str);
		Matcher m = p.matcher(email);
		return m.matches();
	}

	public static boolean isPhoneNumber(String phoneNumber){
		String str = "^\\d{11}$";
		Pattern p = Pattern.compile(str);
		Matcher m = p.matcher(phoneNumber);
		return m.matches();
	}

	private class DoRegister extends AsyncTask<String,Integer,JSONObject>{

		@Override
		protected JSONObject doInBackground(String... strings) {
			try {
				StringBuilder stringBuilder = new StringBuilder(
						"http://www.shiguangtravel.com:8080/CN-Soft/servlet/RegisterAction");
				stringBuilder.append("?");
				stringBuilder.append("username=" + URLEncoder.encode(strings[0], "UTF-8") + "&");
				stringBuilder.append("passwd=" + URLEncoder.encode(strings[1], "UTF-8") + "&");
				stringBuilder.append("phone=" + URLEncoder.encode(strings[2], "UTF-8") + "&");
				stringBuilder.append("email=" + URLEncoder.encode(strings[3], "UTF-8"));

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
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(JSONObject jsonObject) {
			try {
				if(jsonObject!=null){
					mRegisterDialog.dismiss();
					mRegisterDialog = new MaterialDialog(RegisterActivity.this);
					switch (jsonObject.getInt("Register")){
						case RESPONSE_SUCCESS:
							mRegisterDialog.setTitle("注册中");
							mRegisterDialog.setMessage("请再稍等一会...");

							// Todo 获得id之后获取用户信息进入MainActivity
							new GetUserInfo().execute(jsonObject.getInt("id"));

							break;
						case RESPONSE_NAME_ERROR:
							mRegisterDialog.setTitle("注册失败")
								.setMessage("用户名已被占用")
								.setPositiveButton("OK", new View.OnClickListener() {
									@Override
									public void onClick(View view) {
										mRegisterDialog.dismiss();
									}
								});
							break;
						case RESPONSE_PHONE_ERROR:
							mRegisterDialog.setTitle("注册失败")
									.setMessage("手机号码已被使用")
									.setPositiveButton("OK", new View.OnClickListener() {
										@Override
										public void onClick(View view) {
											mRegisterDialog.dismiss();
										}
									});
							break;
						case RESPONSE_EMAIL_ERROR:
							mRegisterDialog.setTitle("注册失败")
									.setMessage("邮箱地址已被使用")
									.setPositiveButton("OK", new View.OnClickListener() {
										@Override
										public void onClick(View view) {
											mRegisterDialog.dismiss();
										}
									});
							break;
					}
					mRegisterDialog.show();
				}else{
					mRegisterDialog.dismiss();
					mRegisterDialog = new MaterialDialog(RegisterActivity.this)
							.setTitle("注册失败")
							.setMessage("未知错误")
							.setPositiveButton("OK", new View.OnClickListener() {
								@Override
								public void onClick(View view) {
									// ...
									mRegisterDialog.dismiss();
								}
							});
					mRegisterDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
						@Override
						public void onDismiss(DialogInterface dialogInterface) {
							mUserNameInput.setEnabled(true);
							mPasswordInput.setEnabled(true);
							mPasswordSecondInput.setEnabled(true);
							mPhoneInput.setEnabled(true);
							mEmailInput.setEnabled(true);
						}
					});
					 mRegisterDialog.show();
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}


	private class GetUserInfo extends AsyncTask<Integer,Integer,JSONObject>{
		private int id;

		@Override
		protected JSONObject doInBackground(Integer... integers) {
			id = integers[0];
			try {
				StringBuilder stringBuilder = new StringBuilder(
						"http://www.shiguangtravel.com:8080/CN-Soft/servlet/SearchAction");
				stringBuilder.append("?");
				stringBuilder.append("id=" + URLEncoder.encode(Integer.toString(id), "UTF-8"));

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
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
		}

		@Override
		protected void onPostExecute(JSONObject jsonObject) {
			try {
				if(jsonObject!=null){

					Cursor cursor = DatabaseManager.getInstance().query("user", null, null, null, null, null, null);

					if(cursor!=null){
						DatabaseManager.getInstance().delete("user","",null);
					}

					String username = jsonObject.getString("username");
					String nickname = jsonObject.getString("nickname");
					int sex = jsonObject.getString("sex").equals("woman")?1:0;
					String picture = jsonObject.getString("picture");
					String region = jsonObject.getString("region");
					String sign = jsonObject.getString("sign");
					String email = jsonObject.getString("email");
					String background = jsonObject.getString("mybackground");

					ContentValues values = new ContentValues();
					values.put("id",id);
					values.put("password",mPasswordInput.getText().toString());
					values.put("username",username);
					values.put("nickname",nickname);
					values.put("sex",sex);
					values.put("picture",picture);
					values.put("region",region);
					values.put("sign",sign);
					values.put("email",email);
					values.put("background", background);

					DatabaseManager.getInstance().insert("user",null,values);

					// 取得用户ID并保存至本地
					SharedPreferences sharedPreferences =
							getSharedPreferences(Constants.Application.PREFERENCE_NAME,0);
					SharedPreferences.Editor editor = sharedPreferences.edit();
					editor.putBoolean(Constants.Application.IS_LOGGED_IN,true);
					editor.putInt(Constants.Application.LOGGED_IN_USER_ID, id);
					editor.apply();

					mRegisterDialog.dismiss();
					mRegisterDialog = new MaterialDialog(RegisterActivity.this);

					mRegisterDialog.setTitle("注册成功");
					mRegisterDialog.setMessage("将进入主界面...");

					mRegisterDialog.show();
					mHandler.sendEmptyMessageDelayed(0, 1000);

					cursor.close();
				}else{
					mRegisterDialog.dismiss();
					mRegisterDialog = new MaterialDialog(RegisterActivity.this)
							.setTitle("注册失败")
							.setMessage("未知错误")
							.setPositiveButton("OK", new View.OnClickListener() {
								@Override
								public void onClick(View view) {
									// ...
									mRegisterDialog.dismiss();
								}
							});
					mRegisterDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
						@Override
						public void onDismiss(DialogInterface dialogInterface) {
							mUserNameInput.setEnabled(true);
							mPasswordInput.setEnabled(true);
							mPasswordSecondInput.setEnabled(true);
							mPhoneInput.setEnabled(true);
							mEmailInput.setEnabled(true);
						}
					});
					mRegisterDialog.show();
				}
			} catch (JSONException e) {
				mRegisterDialog.dismiss();
				mRegisterDialog = new MaterialDialog(RegisterActivity.this)
						.setTitle("注册失败")
						.setMessage("无法解析用户数据。")
						.setPositiveButton("OK", new View.OnClickListener() {
							@Override
							public void onClick(View view) {
								// ...
								mRegisterDialog.dismiss();
							}
						});
				mRegisterDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialogInterface) {
						mUserNameInput.setEnabled(true);
						mPasswordInput.setEnabled(true);
						mPasswordSecondInput.setEnabled(true);
						mPhoneInput.setEnabled(true);
						mEmailInput.setEnabled(true);
					}
				});
				mRegisterDialog.show();
			}
		}
	}
}
