package com.neu.strangers.activities;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
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

/**
 * Created by Administrator on 2015/4/23 0023.
 */
public class NearbyStrangers extends Activity{

    private SystemBarTintManager mSystemBarTintManager;
    private MapView mMapView;
    private RelativeLayout mProgressLayout;
    private BaiduMap mBaiduMap;
    private LocationClient mLocClient;
    private MyLocationListenner mListener = new MyLocationListenner();
    boolean mIsFirstLoc = true;   // if is the first time to locate


    private class MyLocationListenner implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {////


            if (location == null || mMapView == null)
                return;
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    .direction(100).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
            if (mIsFirstLoc) {
                mIsFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(),
                        location.getLongitude());
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
                mBaiduMap.animateMapStatus(u);
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

        mSystemBarTintManager = new SystemBarTintManager(this);
        mSystemBarTintManager.setStatusBarTintEnabled(true);
        mSystemBarTintManager.setTintColor(getResources().getColor(R.color.app_color_primary_dark));
        mMapView = (MapView)findViewById(R.id.bmapView);
        mProgressLayout = (RelativeLayout)findViewById(R.id.progress_layout);

        // Initialize MapView
        mMapView = (MapView) findViewById(R.id.bmapView);
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




}
