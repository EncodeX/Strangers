package com.neu.strangers.activities;

import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.material.widget.PaperButton;
import com.neu.strangers.R;
import com.neu.strangers.tools.ApplicationManager;
import com.neu.strangers.view.AdvancedScrollView;
import com.neu.strangers.view.RectImageView;
import com.nineoldandroids.view.ViewHelper;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ProfileActivity extends AppCompatActivity {

	private SystemBarTintManager mSystemBarTintManager;
	private int mBackgroundHeight;
	private int mAlphaToggleHeight;
	private int mBackgroundY;
	private boolean isInitialized = false;

    private String id;

	@InjectView(R.id.tool_bar)
	Toolbar mToolbar;
	@InjectView(R.id.info_scroll_view)
	AdvancedScrollView mInfoScrollView;
	@InjectView(R.id.user_info_background)
	RectImageView mUserInfoBackground;
	@InjectView(R.id.tool_bar_shadow)
	FrameLayout mToolbarShadow;
    @InjectView(R.id.user_name)
    TextView mUserName;
    @InjectView(R.id.add_as_friend_button)
    PaperButton addAsFriend;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_profile);
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

		mSystemBarTintManager = new SystemBarTintManager(this);
		mSystemBarTintManager.setStatusBarTintEnabled(true);
		mSystemBarTintManager.setTintColor(getResources().getColor(R.color.app_color_primary_dark));

		setToolbarAlpha(0.0);
		// todo: 注意 toolbar阴影的不透明度最大值为0.5

		mInfoScrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
			@Override
			public void onScrollChanged() {
				if(!isInitialized){
					mBackgroundHeight = mUserInfoBackground.getHeight();
					mAlphaToggleHeight = mBackgroundHeight - mToolbar.getHeight();
					mBackgroundY = (int)mUserInfoBackground.getY();
					isInitialized = true;
				}
				int scrollY = mInfoScrollView.getScrollY();

				if (scrollY < 0)
					return;

				if (scrollY < mAlphaToggleHeight) {
					double alpha = (double) scrollY / (double) mBackgroundHeight;
					setToolbarAlpha(alpha);
					mToolbarShadow.setVisibility(View.INVISIBLE);
				} else if (scrollY >= mAlphaToggleHeight) {
					setToolbarAlpha(1.0);
					mToolbarShadow.setVisibility(View.VISIBLE);
				} else {
					setToolbarAlpha(0.0);
					mToolbarShadow.setVisibility(View.INVISIBLE);
				}

				if (scrollY < mBackgroundHeight) {
					ViewHelper.setY(mUserInfoBackground, mBackgroundY - scrollY/2);
				} else if (scrollY >= mBackgroundHeight) {
					ViewHelper.setY(mUserInfoBackground, mBackgroundY - mBackgroundHeight);
				} else {
					ViewHelper.setY(mUserInfoBackground, mBackgroundY);
				}
			}
		});

        id = getIntent().getStringExtra("id");
        if(id != null){
            GetInfo info = new GetInfo();
            info.execute();

         //   addAsFriend.setOnClickListener();
        }

	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	public void setToolbarAlpha(double alpha){
		mToolbar.setBackgroundColor((int)(0xFF * alpha) * 0x1000000 + 0x9C27B0);
	}

    class GetInfo extends AsyncTask<Void,Integer,JSONObject>{


        @Override
        protected JSONObject doInBackground(Void... voids) {
            try {
                String searchUrl = "http://www.shiguangtravel.com:8080/CN-Soft/servlet/SearchAction?id="+id;

                URL url = new URL(searchUrl.toString());
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
                    return new JSONObject(strBuffer.toString());
                }
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            mUserName.setText(jsonObject.optString("username"));

        }
    }
}
