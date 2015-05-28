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
		addItem(new SwitcherItem(getFragment(), "key3").setTitle("Switcher item").setSubtitle("Subtitle text"));
		addItem(new TextItem(getFragment(), "key4").setTitle("Text item"));
		addItem(new HeaderItem(getFragment()).setTitle("Sample title 2"));
		addItem(new SwitcherItem(getFragment(), "key5").setTitle("Switcher item").setSubtitle("Subtitle text"));
		addItem(new SwitcherItem(getFragment(), "key6").setTitle("Switcher item").setSubtitle("Subtitle text"));
		addItem(new SwitcherItem(getFragment(), "key7").setTitle("Switcher item").setSubtitle("Subtitle text"));
		addItem(new SwitcherItem(getFragment(), "key8").setTitle("Switcher item").setSubtitle("Subtitle text"));
		addItem(new SwitcherItem(getFragment(), "key9").setTitle("Switcher item").setSubtitle("Subtitle text"));
		addItem(new SwitcherItem(getFragment(), "key10").setTitle("Switcher item").setSubtitle("Subtitle text"));
		addItem(new SwitcherItem(getFragment(), "key11").setTitle("Switcher item").setSubtitle("Subtitle text"));

		/* Todo 暂时利用Settings Activity 取消登录状态 */
		SharedPreferences sharedPreferences =
				getSharedPreferences(Constants.Application.PREFERENCE_NAME,0);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putBoolean(Constants.Application.IS_LOGGED_IN,false);
		editor.apply();
	}

	@Override
	public StorageInterface initStorageInterface() {
		return new PreferencesStorageInterface(this);
	}
}
