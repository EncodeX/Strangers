package com.neu.strangers.activities;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.MapView;
import com.neu.strangers.R;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Administrator on 2015/4/23 0023.
 */
public class NearbyStrangers extends AppCompatActivity{

    private SystemBarTintManager mSystemBarTintManager;
	@InjectView(R.id.bmapView)
    MapView mMapView;
	@InjectView(R.id.progress_layout)
    RelativeLayout mProgressLayout;
	@InjectView(R.id.tool_bar)
	Toolbar toolbar;

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

	    // Change status bar color for api 19
        mSystemBarTintManager = new SystemBarTintManager(this);
        mSystemBarTintManager.setStatusBarTintEnabled(true);
        mSystemBarTintManager.setTintColor(getResources().getColor(R.color.app_color_primary_dark));

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
