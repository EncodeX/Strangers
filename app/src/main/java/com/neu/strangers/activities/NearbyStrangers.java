package com.neu.strangers.activities;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.MapView;
import com.neu.strangers.R;
import com.readystatesoftware.systembartint.SystemBarTintManager;

/**
 * Created by Administrator on 2015/4/23 0023.
 */
public class NearbyStrangers extends Activity{

    private SystemBarTintManager mSystemBarTintManager;
    private MapView mMapView;
    private RelativeLayout mProgressLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplication());
        setContentView(R.layout.activity_nearby_strangers);

        mSystemBarTintManager = new SystemBarTintManager(this);
        mSystemBarTintManager.setStatusBarTintEnabled(true);
        mSystemBarTintManager.setTintColor(getResources().getColor(R.color.app_color_primary_dark));
        mMapView = (MapView)findViewById(R.id.bmapView);
        mProgressLayout = (RelativeLayout)findViewById(R.id.progress_layout);


        //Just for fun :)
        new Handler().postDelayed(new Runnable() {
            public void run() {

                mMapView.setVisibility(View.VISIBLE);
                mProgressLayout.setVisibility(View.GONE);


            }
        }, 3000);

    //    Toast.makeText(NearbyStrangers.this,"Test Test Test",Toast.LENGTH_SHORT).show();
    }
}
