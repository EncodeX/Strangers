package com.neu.strangers.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.kenumir.materialsettings.MaterialSettingsActivity;
import com.kenumir.materialsettings.items.CheckboxItem;
import com.kenumir.materialsettings.items.HeaderItem;
import com.kenumir.materialsettings.items.SwitcherItem;
import com.kenumir.materialsettings.items.TextItem;
import com.kenumir.materialsettings.storage.PreferencesStorageInterface;
import com.kenumir.materialsettings.storage.StorageInterface;
import com.neu.strangers.R;
import com.neu.strangers.tools.ApplicationManager;
import com.neu.strangers.tools.Constants;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class SettingsActivity extends MaterialSettingsActivity {

	private SystemBarTintManager mSystemBarTintManager;

	@InjectView(R.id.toolbar)
	Toolbar mToolbar;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
		mToolbar.setTitle("设定");

		mSystemBarTintManager = new SystemBarTintManager(this);
		mSystemBarTintManager.setStatusBarTintEnabled(true);
		mSystemBarTintManager.setTintColor(getResources().getColor(R.color.app_color_primary_dark));

		addItem(new HeaderItem(getFragment()).setTitle("Sample title 1"));
		addItem(new CheckboxItem(getFragment(), "key1").setTitle("Checkbox item").setSubtitle("Subtitle text").setOnCheckedChangeListener(new CheckboxItem.OnCheckedChangeListener() {
			@Override
			public void onCheckedChange(CheckboxItem cbi, boolean isChecked) {
				Toast.makeText(SettingsActivity.this, "CHECKED: " + isChecked, Toast.LENGTH_SHORT).show();
			}
		}));
		addItem(new SwitcherItem(getFragment(), "key2").setTitle("Switcher item").setSubtitle("Subtitle text"));
		addItem(new TextItem(getFragment(), "key4").setTitle("退出登录").setOnclick(new TextItem.OnClickListener() {
			@Override
			public void onClick(TextItem item) {
				ApplicationManager.getInstance().clearOtherActivities(SettingsActivity.this);

				SharedPreferences sharedPreferences =
						getSharedPreferences(Constants.Application.PREFERENCE_NAME,0);
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor.putBoolean(Constants.Application.IS_LOGGED_IN,false);
				editor.apply();

				SettingsActivity.this.finish();
			}
		}));
		addItem(new HeaderItem(getFragment()).setTitle("Sample title 2"));
		addItem(new SwitcherItem(getFragment(), "key5").setTitle("Switcher item").setSubtitle("Subtitle text"));
		addItem(new SwitcherItem(getFragment(), "key6").setTitle("Switcher item").setSubtitle("Subtitle text"));
	}

	@Override
	public StorageInterface initStorageInterface() {
		return new PreferencesStorageInterface(this);
	}
}
