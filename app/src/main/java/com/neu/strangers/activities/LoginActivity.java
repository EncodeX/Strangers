package com.neu.strangers.activities;

import android.content.DialogInterface;
import android.content.Intent;
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
import android.view.View;

import com.material.widget.FloatingEditText;
import com.material.widget.PaperButton;
import com.neu.strangers.R;
import com.neu.strangers.tools.ApplicationManager;
import com.readystatesoftware.systembartint.SystemBarTintManager;

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

public class LoginActivity extends AppCompatActivity {

	private SystemBarTintManager mSystemBarTintManager;

	@InjectView(R.id.tool_bar)
	Toolbar mToolbar;
	@InjectView(R.id.login_button)
	PaperButton mLoginButton;
	@InjectView(R.id.user_name_input)
	FloatingEditText mUserNameInput;
	@InjectView(R.id.password_input)
	FloatingEditText mPasswordInput;

	private MaterialDialog mLoginDialog;
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what){
				case 0:
					ApplicationManager.getInstance().clearOtherActivities(LoginActivity.this);
					Intent intent = new Intent();
					intent.setClass(LoginActivity.this, MainActivity.class);
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
		setContentView(R.layout.activity_login);
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
				LoginActivity.this.overridePendingTransition(R.anim.fade_out_in, R.anim.fade_out_out);
			}
		});

		mSystemBarTintManager = new SystemBarTintManager(this);
		mSystemBarTintManager.setStatusBarTintEnabled(true);
		mSystemBarTintManager.setTintColor(getResources().getColor(R.color.app_color_primary_dark));

		mLoginButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				/* Just for testing */
				mHandler.sendEmptyMessageDelayed(0,1000);

				/* 真正使用时取消注释 */
//				mLoginDialog = new MaterialDialog(LoginActivity.this);
//
//				mUserNameInput.setEnabled(false);
//				mPasswordInput.setEnabled(false);
//				mLoginDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//					@Override
//					public void onDismiss(DialogInterface dialogInterface) {
//						mUserNameInput.setEnabled(true);
//						mPasswordInput.setEnabled(true);
//					}
//				});
//
//				if(mUserNameInput.getText().length()==0 || mPasswordInput.getText().length()==0){
//					mLoginDialog.setTitle("未输入用户名/密码")
//							.setMessage("请输入完整后再登录")
//							.setPositiveButton("OK", new View.OnClickListener() {
//								@Override
//								public void onClick(View view) {
//									mLoginDialog.dismiss();
//								}
//							});
//				}else {
//					mLoginDialog.setTitle("正在登录").setMessage("请等待...");
//					new DoLogin().execute(mUserNameInput.getText().toString(), mPasswordInput.getText().toString());
//				}
//				mLoginDialog.show();
			}
		});
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		switch (event.getKeyCode()){
			case KeyEvent.KEYCODE_BACK:
				onBackPressed();
				LoginActivity.this.overridePendingTransition(R.anim.fade_out_in, R.anim.fade_out_out);
				return true;
		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.menu_login, menu);
		return true;
	}

	private class DoLogin extends AsyncTask<String,Integer,JSONObject> {

		@Override
		protected JSONObject doInBackground(String... strings) {
			try {
				StringBuilder stringBuilder = new StringBuilder(
						"http://www.shiguangtravel.com:8080/CN-Soft/servlet/LoginAction");
				stringBuilder.append("?");
				stringBuilder.append("username=" + URLEncoder.encode(strings[0], "UTF-8") + "&");
				stringBuilder.append("passwd=" + URLEncoder.encode(strings[1], "UTF-8"));

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
					mLoginDialog.dismiss();
					mLoginDialog = new MaterialDialog(LoginActivity.this);
					if(jsonObject.getString("Login").equals("success")){
						// Todo 取得用户ID并保存至本地
						mLoginDialog.setTitle("登录成功");
						mLoginDialog.setMessage("将进入主界面...");
						mHandler.sendEmptyMessageDelayed(0,1000);
					}else if(jsonObject.getString("Login").equals("fail")){
						mLoginDialog.setTitle("登录失败")
								.setMessage("请检查用户名/密码是否正确")
								.setPositiveButton("OK", new View.OnClickListener() {
									@Override
									public void onClick(View view) {
										mLoginDialog.dismiss();
									}
								});
					}
					mLoginDialog.show();
				}else{
					mLoginDialog.dismiss();
					mLoginDialog = new MaterialDialog(LoginActivity.this)
							.setTitle("登录失败")
							.setMessage("未知错误")
							.setPositiveButton("OK", new View.OnClickListener() {
								@Override
								public void onClick(View view) {
									// ...
									mLoginDialog.dismiss();
								}
							});
					mLoginDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
						@Override
						public void onDismiss(DialogInterface dialogInterface) {
							mUserNameInput.setEnabled(true);
							mPasswordInput.setEnabled(true);
						}
					});
					mLoginDialog.show();
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
}
