package com.neu.strangers.tools;

import android.app.Activity;
import android.app.Application;

import java.util.LinkedList;
import java.util.List;

/**
 * Created with Android Studio.
 * Author: Enex Tapper
 * Date: 15/5/14
 * Project: Strangers
 * Package: com.neu.strangers.tools
 */
public class ApplicationManager extends Application {
	//对于新增和删除操作add和remove，LinedList比较占优势，因为ArrayList实现了基于动态数组的数据结构，要移动数据。LinkedList基于链表的数据结构,便于增加删除
	private List<Activity> activityList = new LinkedList<Activity>();
	private static ApplicationManager instance;
	private ApplicationManager(){ }
	//单例模式中获取唯一的MyApplication实例
	public static ApplicationManager getInstance() {
		if(null == instance) {
			instance = new ApplicationManager();
		}
		return instance;
	}
	//添加Activity到容器中
	public void addActivity(Activity activity)  {
		activityList.add(activity);
	}
	//遍历所有Activity并finish
	public void exit(){
		for(Activity activity:activityList) {
			activity.finish();
		}
		System.exit(0);
	}

	public void clearOtherActivities(Activity currentActivity){
		for(Activity activity:activityList){
			if(!activity.equals(currentActivity)){
				activity.finish();
			}
		}
	}
}