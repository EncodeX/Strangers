package com.neu.strangers.activities;

import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.material.widget.PaperButton;
import com.neu.strangers.R;
import com.neu.strangers.tools.ApplicationManager;
import com.neu.strangers.tools.StrangerInfo;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.hdodenhof.circleimageview.CircleImageView;

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
    @InjectView(R.id.stranger_info)
    FrameLayout mStrangersInfo;
    @InjectView(R.id.add_friends)
    PaperButton mAddFriend;
    @InjectView(R.id.user_name)
    TextView mUserName;
    @InjectView(R.id.distance)
    TextView mDistance;
    @InjectView(R.id.user_info)
    TextView mUserInfo;
    @InjectView(R.id.user_icon)
    CircleImageView mUserIcon;
    private BaiduMap mBaiduMap;
    private LocationClient mLocClient;
    private MyLocationListenner mListener = new MyLocationListenner();
    private boolean mIsFirstLoc = true;   //if is the first time to locate
    private boolean mIsFirstShow = true;


    private Marker marker[];
    private BitmapDescriptor bitmapDescriptor;
    private BitmapDescriptor bdGround;
  //  private PopupWindow infoPopupWindow;




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

            Log.d("Latitude",String.valueOf(location.getLatitude()));
            Log.d("Longitude",String.valueOf(location.getLongitude()));
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
	    ApplicationManager.getInstance().addActivity(this);
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

        mMapView.showZoomControls(false);
        mMapView.showScaleControl(false);
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


        MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.zoomTo(14);
        mBaiduMap.animateMapStatus(mMapStatusUpdate);
        //Just for fun :)
        new Handler().postDelayed(new Runnable() {
            public void run() {
                mMapView.setVisibility(View.VISIBLE);
                mProgressLayout.setVisibility(View.GONE);

            }
        }, 3000);


        /*         测试数据              */

        final StrangerInfo mStrangers[] = new StrangerInfo[2];
        mStrangers[0] = new StrangerInfo("张三", "info", 41.789629, 123.419895,getResources().getDrawable(R.drawable.test_avatar),14);
        mStrangers[1] = new StrangerInfo("李四", "info", 41.769999, 123.422928,getResources().getDrawable(R.drawable.test_avatar),13);

        initOverlay(mStrangers);
        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {

            @Override
            public boolean onMarkerClick(Marker m) {
                for(int i = 0; i < mStrangers.length; i++){
                    if(m == marker[i]){

                       mUserName.setText(mStrangers[i].getUserName());
                       mDistance.setText("距离：" + mStrangers[i].getDisance() + " km");
                        mUserIcon.setImageDrawable(mStrangers[i].getUserIcon());

                        if(mIsFirstShow == true) {
                            showStrangerInfo();
                        }

                        MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newLatLng(mStrangers[i].getLocation());
                        mBaiduMap.animateMapStatus(mMapStatusUpdate);

                    }
                }
                return true;
            }
        });



         //添加好友

        mAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(NearbyStrangers.this,"add",Toast.LENGTH_SHORT).show();
            }
        });


    }

    // 将marker显示在地图上
    public void initOverlay(StrangerInfo strangers[]) {
        int count = strangers.length;
        LatLng latLngs;

        bitmapDescriptor  = BitmapDescriptorFactory.fromResource(R.drawable.icon_gcoding);
        bdGround  = BitmapDescriptorFactory.fromResource(R.drawable.icon_gcoding);
        marker = new Marker[count];
        for(int i = 0; i < count; i++){
            latLngs = strangers[i].getLocation();
            OverlayOptions overlayOptions_marker = new MarkerOptions().position(latLngs).icon(bitmapDescriptor);
            marker[i] = (Marker)(mBaiduMap.addOverlay(overlayOptions_marker));
           // marker[i].setTitle("strangers");
        }

    }

    // 显示stranger信息的动画
    private void showStrangerInfo()
    {
        mStrangersInfo.setVisibility(View.VISIBLE);
        ViewHelper.setY(mStrangersInfo, mStrangersInfo.getY() + dpToPx(getResources(), 126));
        AnimatorSet slideOutAnimation =
                buildSlideAnimation(mStrangersInfo, mStrangersInfo.getY()-dpToPx(getResources(),126));
        mIsFirstShow = false;
        slideOutAnimation.start();

    }


    private AnimatorSet buildSlideAnimation(View target, float targetPosY){

        AnimatorSet slideAnimation = new AnimatorSet();
        slideAnimation.playTogether(
                ObjectAnimator.ofFloat(target, "translationY", targetPosY)
        );
        slideAnimation.setInterpolator(new DecelerateInterpolator(2.0f));

        slideAnimation.setDuration(800);
        return slideAnimation;
    }

    static float dpToPx(Resources resources, float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
    }




    @Override
    protected void onStop() {
        mLocClient.stop();
        super.onStop();
    }




}
