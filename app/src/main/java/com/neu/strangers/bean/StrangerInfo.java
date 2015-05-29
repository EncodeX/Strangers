package com.neu.strangers.bean;

import android.graphics.drawable.Drawable;

import com.baidu.mapapi.model.LatLng;

/**
 * Created by Administrator on 2015/5/14 0014.
 */
public class StrangerInfo {

    private String mUserName;
    private String mUserInfo;    //用户的其他信息，先预留着。
    private LatLng mLocation;
    private Drawable mUserIcon;
    private String mDisance;
    private String uid;


    public StrangerInfo() {
        mUserName = "";
        mUserInfo = "";
        mLocation = null;
        mUserIcon = null;
        mDisance = "";
        uid = "";
    }
    public StrangerInfo(String mUserName, String mUserInfo, LatLng mLocation,Drawable mUserIcon,String mDisance,String uid){
        this.mUserName = mUserName;
        this.mUserInfo = mUserInfo;
        this.mLocation = mLocation;
        this.mUserIcon = mUserIcon;
        this.mDisance = mDisance;
        this.uid = uid;
    }
    public StrangerInfo(String mUserName, String mUserInfo, double latitude, double longitude,Drawable mUserIcon,String mDisance,String uid){
        this.mUserName = mUserName;
        this.mUserInfo = mUserInfo;
        this.mUserIcon = mUserIcon;
        this.mDisance = mDisance;
        this.uid = uid;
        mLocation = new LatLng(latitude, longitude);
    }

    public String getUserName() {
        return mUserName;
    }

    public void setUserName(String mUserName) {
        this.mUserName = mUserName;
    }

    public String getUserInfo() {
        return mUserInfo;
    }

    public void setUserInfo(String mUserInfo) {
        this.mUserInfo = mUserInfo;
    }

    public LatLng getLocation() {
        return mLocation;
    }

    
    public void setLocation(LatLng mLocation) {
        this.mLocation = mLocation;
    }

    public Drawable getUserIcon(){
        return this.mUserIcon;
    }

    public void setUserIcon(Drawable mIcon) {
        this.mUserIcon = mIcon;
    }

    public String getDisance(){
        return this.mDisance;
    }

    public void setDisance(String disance) {
        this.mDisance = disance;
    }

    public String getUid(){
        return uid;
    }


}
