package com.neu.strangers.activities;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.astuetz.PagerSlidingTabStrip;
import com.neu.strangers.R;
import com.neu.strangers.view.MainViewPager;
import com.readystatesoftware.systembartint.SystemBarTintManager;


public class MainActivity extends AppCompatActivity{
	private SystemBarTintManager mSystemBarTintManager;
	private SensorManager mSensorManager;
	private Sensor mSensor;

	private SensorEventListener mListener = new SensorEventListener() {
		@Override
		public void onSensorChanged(SensorEvent sensorEvent) {

			float xValue = Math.abs(sensorEvent.values[0]);
			//  float yValue = Math.abs(sensorEvent.values[1]);
			// float zValue = Math.abs(sensorEvent.values[2]);
			if(xValue > 18.5 )
			{

				startActivity(new Intent(MainActivity.this,NearbyStrangers.class));
			}

		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int i) {

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		//GitHub修改测试

//		ButterKnife.inject(this);

		// Initialize tool bar.
		Toolbar toolbar = (Toolbar)findViewById(R.id.tool_bar);
		setSupportActionBar(toolbar);

		// Change StatusBar Color for Kitkat
		mSystemBarTintManager = new SystemBarTintManager(this);
		mSystemBarTintManager.setStatusBarTintEnabled(true);
		mSystemBarTintManager.setTintColor(getResources().getColor(R.color.app_color_primary_dark));

		// Initialize ViewPager.
		MainViewPager mainViewPager = (MainViewPager)findViewById(R.id.main_view_pager);
		mainViewPager.initView(this);

		PagerSlidingTabStrip mainPagerTabs =
				(PagerSlidingTabStrip)findViewById(R.id.main_pager_tabs);
		mainPagerTabs.setViewPager(mainViewPager);

		//Initialize the Sensor
		mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
		mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mSensorManager.registerListener(mListener, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}
