package com.neu.strangers.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.astuetz.PagerSlidingTabStrip;
import com.neu.strangers.R;
import com.neu.strangers.tools.ApplicationManager;
import com.neu.strangers.tools.Constants;
import com.neu.strangers.tools.DatabaseManager;
import com.neu.strangers.tools.ImageCache;
import com.neu.strangers.tools.DatabaseManager;
import com.neu.strangers.tools.RoundImage;
import com.neu.strangers.tools.XmppTool;
import com.neu.strangers.view.MainViewPager;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import net.sqlcipher.Cursor;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Application Strangers
 *
 * Encode.X's TodoList:
 *
 * Todo 进入主界面时加载最近会话列表 添加时间view
 * Todo 好友列表加载 接入详细信息等
 * Todo 删除好友
 * Todo 退出登录
 * Todo LruCache 图片缓存
 * Todo 游戏到底做啥？？？
 */

public class MainActivity extends AppCompatActivity{
    private SystemBarTintManager mSystemBarTintManager;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private MenuItem mUserItem;
	private Menu mMenu;
	private ImageCache mImageCache;

    @InjectView(R.id.tool_bar)
    Toolbar toolbar;
    @InjectView(R.id.main_view_pager)
    MainViewPager mainViewPager;
    @InjectView(R.id.main_pager_tabs)
    PagerSlidingTabStrip mainPagerTabs;

    private SensorEventListener mListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {

            float xValue = Math.abs(sensorEvent.values[0]);
            // float yValue = Math.abs(sensorEvent.values[1]);
            // float zValue = Math.abs(sensorEvent.values[2]);
            if(xValue > 18.5 )
            {
                mSensorManager.unregisterListener(mListener);
                startActivity(new Intent(MainActivity.this, NearbyStrangers.class));
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
        ApplicationManager.getInstance().addActivity(this);
	    ApplicationManager.getInstance().clearOtherActivities(this);

        ButterKnife.inject(this);

        // Initialize tool bar.
        setSupportActionBar(toolbar);

        // Change StatusBar Color for Kitkat
        mSystemBarTintManager = new SystemBarTintManager(this);
        mSystemBarTintManager.setStatusBarTintEnabled(true);
        mSystemBarTintManager.setTintColor(getResources().getColor(R.color.app_color_primary_dark));

        // Initialize ViewPager.
        mainViewPager.initView(this);
        mainPagerTabs.setViewPager(mainViewPager);

        //Initialize the Sensor
        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(mListener, mSensor, SensorManager.SENSOR_DELAY_NORMAL);

	    //Initialize cache
	    mImageCache = new ImageCache(this);
	    mImageCache.setOnBitmapPreparedListener(new ImageCache.OnBitmapPreparedListener() {
		    @Override
		    public void onBitmapPrepared(Bitmap bitmap, String tag) {
			    RoundImage roundImage = new RoundImage(bitmap);
			    mUserItem = mMenu.findItem(R.id.action_user);
			    mUserItem.setIcon(roundImage);
		    }
	    });
	    new UpdateUserAvatar().execute();
    }

    @Override
    protected void onStop() {
        if(mSensorManager != null)
            mSensorManager.unregisterListener(mListener);
        super.onStop();
    }

    @Override
    protected void onRestart() {
        mSensorManager.registerListener(mListener, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
	    mMenu = menu;
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //Test round image for user avatar
        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.test_avatar);
        RoundImage roundImage = new RoundImage(bm);

        mUserItem = mMenu.findItem(R.id.action_user);
        mUserItem.setIcon(roundImage);

        return super.onPrepareOptionsMenu(menu);
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		Intent intent;

		switch (id){
			case R.id.action_settings:
				intent = new Intent();
				intent.setClass(MainActivity.this, SettingsActivity.class);
				MainActivity.this.startActivity(intent);
				return true;
			case R.id.action_user:
				intent = new Intent();
				intent.setClass(MainActivity.this, ProfileActivity.class);
				intent.putExtra(
						Constants.Application.PROFILE_USER_ID,
						getSharedPreferences(Constants.Application.PREFERENCE_NAME, 0)
								.getInt(Constants.Application.LOGGED_IN_USER_ID, -1));
				startActivity(intent);
				overridePendingTransition(R.anim.fade_in_in, R.anim.fade_in_out);
				return true;
		}

        return super.onOptionsItemSelected(item);
    }

    private class UpdateUserAvatar extends AsyncTask<String,Void,Integer>{
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
}