package com.neu.strangers.tools;

import android.content.Context;

import net.sqlcipher.database.SQLiteDatabase;

/**
 * Created with Android Studio.
 * Author: Enex Tapper
 * Date: 15/5/28
 * Project: Strangers
 * Package: com.neu.strangers.tools
 */
public class DatabaseManager {

	private static SQLiteDatabase database;
	private static Context mContext;

	public static SQLiteDatabase getInstance(){
		if(database==null){
			if(mContext!=null){
				SQLiteDatabase.loadLibs(mContext);
				DatabaseHelper databaseHelper = new DatabaseHelper(mContext,"database.db",null,1);
				database = databaseHelper.getWritableDatabase("dROwSsaP");
			}
		}
		return database;
	}

	public static void setContext(Context context){
		mContext = context;
	}
}
