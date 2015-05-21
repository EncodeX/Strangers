package com.neu.strangers.activities;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.neu.strangers.R;
import com.neu.strangers.tools.ApplicationManager;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class SettingsActivity extends AppCompatActivity {

	private SystemBarTintManager mSystemBarTintManager;

	@InjectView(R.id.tool_bar)
	Toolbar mToolbar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
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
	}
}
