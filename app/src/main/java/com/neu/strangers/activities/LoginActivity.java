package com.neu.strangers.activities;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.material.widget.PaperButton;
import com.neu.strangers.R;
import com.neu.strangers.tools.ApplicationManager;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class LoginActivity extends ActionBarActivity {

	private SystemBarTintManager mSystemBarTintManager;

	@InjectView(R.id.tool_bar)
	Toolbar mToolbar;
	@InjectView(R.id.login_button)
	PaperButton mLoginButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		ApplicationManager.getInstance().addActivity(this);

		ButterKnife.inject(this);

		// Add back button
		setSupportActionBar(mToolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
				//Todo 此处调用ApplicationManager来清除WelcomeActivity
				ApplicationManager.getInstance().clearOtherActivities(LoginActivity.this);

				Intent intent = new Intent();
				intent.setClass(LoginActivity.this, MainActivity.class);
				LoginActivity.this.startActivity(intent);
				LoginActivity.this.overridePendingTransition(R.anim.fade_in_in,R.anim.fade_in_out);
				LoginActivity.this.finish();
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
}
