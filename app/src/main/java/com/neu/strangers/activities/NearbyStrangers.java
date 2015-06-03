package com.neu.strangers.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
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
import java.util.Timer;
import java.util.TimerTask;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.hdodenhof.circleimageview.CircleImageView;
import me.drakeet.materialdialog.MaterialDialog;

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

    /**
     *
     * 游戏部分
     * 今后会分离出Activity
     *
     */

    private MaterialDialog mGameDialog;
	private PaperButton[] mCards = new PaperButton[3];
	private Timer mTimer;
	private TimerTask mTimerTask;
	private Handler mHandler;
	private AnimatorSet animation;

	private int[] cardPosition = {0,1,2};
	private int[] cardNumber = {0,1,1};
	private int[] cardXPos = {0,0,0};
	private boolean[] cardOpened = {false,false,false};
	private int counter;
	private boolean isGamePlaying;
	private boolean isFirstInit = true;
	private int cardSpace;
	private int targetId;

	private Runnable swapCard = new Runnable() {
		@Override
		public void run() {
			double rd = Math.random();
			int temp;
			PaperButton tempCard;

			Log.v("random",""+rd);
			if(rd > 0.6666666666666667){
				Log.v("random","交换 0,1");
				// 交换 0,1

				animation = buildAnimation(mCards[0], getSwapDistance(0,cardPosition[1]));
				animation.playTogether(buildAnimation(mCards[1], getSwapDistance(1,cardPosition[0])));
				temp = cardPosition[0];
				cardPosition[0]=  cardPosition[1];
				cardPosition[1] = temp;
				animation.start();
			}else if(rd > 0.333333333333333){
				Log.v("random","交换 0,2");
				// 交换 0,2
				animation = buildAnimation(mCards[0], getSwapDistance(0,cardPosition[2]));
				animation.playTogether(buildAnimation(mCards[2], getSwapDistance(2,cardPosition[0])));
				temp = cardPosition[0];
				cardPosition[0]=  cardPosition[2];
				cardPosition[2] = temp;
				animation.start();
			}else{
				Log.v("random","交换 1,2");
				// 交换 1,2
				animation = buildAnimation(mCards[1], getSwapDistance(1,cardPosition[2]));
				animation.playTogether(buildAnimation(mCards[2], getSwapDistance(2,cardPosition[1])));
				temp = cardPosition[1];
				cardPosition[1]=  cardPosition[2];
				cardPosition[2] = temp;
				animation.start();
			}

			counter++;
			if(counter>10){
				// 加监听
				Toast.makeText(NearbyStrangers.this, "请选择正面为1的2张卡片来查看用户资料", Toast.LENGTH_LONG).show();
				for(int i=0;i<3;i++){
					final int finalI = i;
					mCards[i].setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							mCards[finalI].setText(Integer.toString(cardNumber[finalI]));
							cardOpened[finalI]=true;
							if(cardNumber[finalI]==0){
								// 结束游戏
								Toast.makeText(NearbyStrangers.this, "选错了", Toast.LENGTH_SHORT).show();
								isGamePlaying = false;
								mGameDialog.dismiss();
							}
							boolean isAllOpened = true;
							for(int j=1;j<3;j++){
								if(!cardOpened[j]){
									isAllOpened=false;
								}
							}
							if(isAllOpened){
								Toast.makeText(NearbyStrangers.this, "查看资料", Toast.LENGTH_SHORT).show();
								Intent intent = new Intent(NearbyStrangers.this,ProfileActivity.class);
			                    Bundle bundle = new Bundle();
			                    bundle.putInt(Constants.Application.PROFILE_USER_ID,targetId);
			                    intent.putExtras(bundle);
			                    startActivity(intent);
								isGamePlaying = false;
								mGameDialog.dismiss();
							}
						}
					});
				}
			}else{
				Log.v("Counter", "" + counter);
			}
		}
	};

	private void gameStart(Context context,int id){
		mHandler = new Handler();
		LayoutInflater inflate = ((Activity)context).getLayoutInflater();
		View dialogContent = inflate.inflate(R.layout.dialog_game, null);

		mCards[0] = (PaperButton)dialogContent.findViewById(R.id.card_1);
		mCards[1] = (PaperButton)dialogContent.findViewById(R.id.card_2);
		mCards[2] = (PaperButton)dialogContent.findViewById(R.id.card_3);

		for(int i = 0;i<3;i++){
			mCards[i].setText(Integer.toString(cardNumber[i]));
		}

		mGameDialog = new MaterialDialog(context);
		mGameDialog.setContentView(dialogContent);
		mGameDialog.show();
		targetId = id;
	}

	private int getSwapDistance(int cardIndex, int targetPosition){
		int result = 0;
		switch (cardIndex){
			case 0:
				switch (targetPosition){
					case 0:
						result = 0;
						break;
					case 1:
						result = cardSpace;
						break;
					case 2:
						result = cardSpace*2;
						break;
				}
				break;
			case 1:
				switch (targetPosition){
					case 0:
						result = -cardSpace;
						break;
					case 1:
						result = 0;
						break;
					case 2:
						result = cardSpace;
						break;
				}
				break;
			case 2:
				switch (targetPosition){
					case 0:
						result = -cardSpace*2;
						break;
					case 1:
						result = -cardSpace;
						break;
					case 2:
						result = 0;
						break;
				}
				break;
		}

		return result;
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if(!hasFocus){
			if(isFirstInit){
				cardXPos[0] = mCards[0].getLeft();
				cardXPos[1] = mCards[1].getLeft();
				cardXPos[2] = mCards[2].getLeft();
				cardSpace = cardXPos[1];
			}else{
				ViewHelper.setX(mCards[0],cardXPos[0]);
				ViewHelper.setX(mCards[1],cardXPos[1]);
				ViewHelper.setX(mCards[2], cardXPos[2]);
			}

			counter = 1;
			isGamePlaying = true;
			cardOpened[0] = false;
			cardOpened[1] = false;
			cardOpened[2] = false;

			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					for(int i=0;i<3;i++){
						mCards[i].setText(" ");
					}
					for(int i = 1;i<11;i++){
						mHandler.postDelayed(swapCard,700*i);
					}
				}
			},700);
		}
	}

	private AnimatorSet buildAnimation(View target, float targetPosX) {

		AnimatorSet animation = new AnimatorSet();
		animation.playTogether(
				ObjectAnimator.ofFloat(target, "translationX", targetPosX)
		);
		animation.setInterpolator(new DecelerateInterpolator(2.0f));

		animation.setDuration(500);
		return animation;
	}

	/**
     * 游戏部分结束
     */

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
                mStrangers[i] = new StrangerInfo("nickname", sex, Double.valueOf(latitude), Double.valueOf(longitude), getResources().getDrawable(R.drawable.default_avatar), String.format("%.2f",Double.valueOf(distance)),id);

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
	                gameStart(NearbyStrangers.this,Integer.valueOf(mStrangers[currentInfo].getUid()));

//                  //  Toast.makeText(NearbyStrangers.this, "add"+mStrangers[currentInfo].getUid(), Toast.LENGTH_SHORT).show();
//                    Intent intent = new Intent(NearbyStrangers.this,ProfileActivity.class);
//                  //  intent.putExtra("id",mStrangers[currentInfo].getUid());
//                    Bundle bundle = new Bundle();
//                    bundle.putInt(Constants.Application.PROFILE_USER_ID,Integer.valueOf(mStrangers[currentInfo].getUid()));
//                    intent.putExtras(bundle);
//                    startActivity(intent);

                }
            });
        }
    }
}





