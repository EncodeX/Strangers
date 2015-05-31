package com.neu.strangers.activities;

import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
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
import com.material.widget.PaperButton;
import com.neu.strangers.R;
import com.neu.strangers.tools.ApplicationManager;
import com.neu.strangers.bean.StrangerInfo;
import com.neu.strangers.tools.Constants;
import com.neu.strangers.tools.DatabaseManager;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import net.sqlcipher.Cursor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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

    private String longitude = "";
    private String latitude = "";
    private String uid;
    private int currentInfo;


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

            latitude = String.valueOf(location.getLatitude());
            longitude = String.valueOf(location.getLongitude());

            new GetStrangers().execute(uid,longitude,latitude);
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

        mProgressLayout = (RelativeLayout) findViewById(R.id.progress_layout);

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
       // option.setScanSpan(1000);
        mLocClient.setLocOption(option);
        // start locating

        Cursor cursor = DatabaseManager.getInstance().query("user", new String[]{"id"}, null, null, null, null, null);
        if (cursor.moveToNext()) {
            uid = cursor.getString(0);
            Log.e("id",cursor.getString(0).toString());
        }
        cursor.close();


        MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.zoomTo(14);
        mBaiduMap.animateMapStatus(mMapStatusUpdate);
        mLocClient.start();


    }

    // 将marker显示在地图上
    public void initOverlay(StrangerInfo strangers[]) {
        int count = strangers.length;
        LatLng latLngs;

        bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.icon_gcoding);
        bdGround = BitmapDescriptorFactory.fromResource(R.drawable.icon_gcoding);
        marker = new Marker[count];
        for (int i = 0; i < count; i++) {
            latLngs = strangers[i].getLocation();
            OverlayOptions overlayOptions_marker = new MarkerOptions().position(latLngs).icon(bitmapDescriptor);
            marker[i] = (Marker) (mBaiduMap.addOverlay(overlayOptions_marker));
            // marker[i].setTitle("strangers");
        }

    }

    // 显示stranger信息的动画
    private void showStrangerInfo() {
        mStrangersInfo.setVisibility(View.VISIBLE);
        ViewHelper.setY(mStrangersInfo, mStrangersInfo.getY() + dpToPx(getResources(), 126));
        AnimatorSet slideOutAnimation =
                buildSlideAnimation(mStrangersInfo, mStrangersInfo.getY() - dpToPx(getResources(), 126));
        mIsFirstShow = false;
        slideOutAnimation.start();

    }


    private AnimatorSet buildSlideAnimation(View target, float targetPosY) {

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

    private class GetStrangers extends AsyncTask<String,Integer,JSONObject>{

        @Override
        protected JSONObject doInBackground(String... strings) {
            try {
                StringBuilder stringBuilder = new StringBuilder(
                        "http://www.shiguangtravel.com:8080/CN-Soft/servlet/MapAction");
                stringBuilder.append("?");
                stringBuilder.append("id=" + URLEncoder.encode(strings[0], "UTF-8") + "&");
                stringBuilder.append("longitude=" + URLEncoder.encode(strings[1], "UTF-8") + "&");
                stringBuilder.append("latitude=" + URLEncoder.encode(strings[2], "UTF-8"));
                URL url = new URL(stringBuilder.toString());
                Log.e("URL",url.toString());
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                conn.setRequestMethod("GET");

                InputStreamReader inputStreamReader = new InputStreamReader(conn.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                StringBuilder strBuffer = new StringBuilder();
                String line;

                if(conn.getResponseCode()==200){
                    while ((line = bufferedReader.readLine()) != null) {
                        strBuffer.append(line);
                    }
                    Log.e("info",strBuffer.toString());

                   return new JSONObject(strBuffer.toString());
                }
            }
            catch (JSONException | IOException e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(JSONObject jsonobject) {
            super.onPostExecute(jsonobject);
            mMapView.setVisibility(View.VISIBLE);
            mProgressLayout.setVisibility(View.GONE);
            JSONArray array = jsonobject.optJSONArray("addfriends");
            final StrangerInfo[] mStrangers = new StrangerInfo[array.length()];
             //Log.e("TT",array.toString());
           // List<StrangerInfo> strangers = new LinkedList<StrangerInfo>();


            for(int i=0;i<array.length();i++) {
                JSONObject strangerInfo = array.optJSONObject(i);
                String longitude = strangerInfo.optString("longitude");
                String latitude = strangerInfo.optString("latitude");
                String nickname = strangerInfo.optString("nickname");
                String sex = strangerInfo.optString("sex");
                String distance = strangerInfo.optString("distance");
                String id = strangerInfo.optString("id");
                mStrangers[i] = new StrangerInfo("nickname", sex, Double.valueOf(latitude), Double.valueOf(longitude), getResources().getDrawable(R.drawable.test_avatar), String.format("%.2f",Double.valueOf(distance)),id);

              }

            initOverlay(mStrangers);
            mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {

                @Override
                public boolean onMarkerClick(Marker m) {
                    for (int i = 0; i < mStrangers.length; i++) {
                        if (m == marker[i]) {
                            currentInfo = i;

                            mUserName.setText(mStrangers[i].getUserName());
                            mDistance.setText("距离：" + mStrangers[i].getDisance() + " km");
                            mUserIcon.setImageDrawable(mStrangers[i].getUserIcon());
                            mUserInfo.setText("性别:"+mStrangers[i].getUserInfo());

                            if (mIsFirstShow == true) {
                                showStrangerInfo();
                            }

                            MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newLatLng(mStrangers[i].getLocation());
                            mBaiduMap.animateMapStatus(mMapStatusUpdate);

                        }
                    }
                    return true;
                }
            });
            mProgressLayout.setVisibility(View.GONE);
            mMapView.setVisibility(View.VISIBLE);
            mAddFriend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                  //  Toast.makeText(NearbyStrangers.this, "add"+mStrangers[currentInfo].getUid(), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(NearbyStrangers.this,ProfileActivity.class);
                  //  intent.putExtra("id",mStrangers[currentInfo].getUid());
                    Bundle bundle = new Bundle();
                    bundle.putInt(Constants.Application.PROFILE_USER_ID,Integer.valueOf(mStrangers[currentInfo].getUid()));
                    intent.putExtras(bundle);
                    startActivity(intent);

                }
            });
        }
    }
}





