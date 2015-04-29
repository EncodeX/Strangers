package com.neu.strangers.activities;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RelativeLayout;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.neu.strangers.R;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Administrator on 2015/4/23 0023.
 */
public class NearbyStrangers extends AppCompatActivity {

    private SystemBarTintManager mSystemBarTintManager;
    @InjectView(R.id.bmapView)
    MapView mMapView;
    @InjectView(R.id.progress_layout)
    RelativeLayout mProgressLayout;
    @InjectView(R.id.tool_bar)
    Toolbar toolbar;
    private BaiduMap mBaiduMap;
    private LocationClient mLocClient;
    private MyLocationListenner mListener = new MyLocationListenner();
    boolean mIsFirstLoc = true;   //if is the first time to locate


    private class MyLocationListenner implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {


            if (location == null || mMapView == null)
                return;
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    .direction(100).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
            if (mIsFirstLoc) {
                mIsFirstLoc = false;
                LatLng mLatLng = new LatLng(location.getLatitude(),
                        location.getLongitude());
                MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newLatLng(mLatLng);
                mBaiduMap.animateMapStatus(mMapStatusUpdate);
            }

        }

        public void onReceivePoi(BDLocation poiLocation) {
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplication());
        setContentView(R.layout.activity_nearby_strangers);


        ButterKnife.inject(this);

        // Add back button
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        mSystemBarTintManager = new SystemBarTintManager(this);
        mSystemBarTintManager.setStatusBarTintEnabled(true);
        mSystemBarTintManager.setTintColor(getResources().getColor(R.color.app_color_primary_dark));

        mProgressLayout = (RelativeLayout)findViewById(R.id.progress_layout);

        // Initialize MapView

        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMyLocationEnabled(true);
        // Initialize LocationClint 
        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(mListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);
        option.setCoorType("bd09ll");
        option.setScanSpan(1000);
        mLocClient.setLocOption(option);
        // start locating
        mLocClient.start();


        //Just for fun :)
        new Handler().postDelayed(new Runnable() {
            public void run() {
                mMapView.setVisibility(View.VISIBLE);
                mProgressLayout.setVisibility(View.GONE);

            }
        }, 3000);


    //    Toast.makeText(NearbyStrangers.this,"Test Test Test",Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStop() {
        mLocClient.stop();
        super.onStop();
    }
}
