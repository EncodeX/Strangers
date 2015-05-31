package com.neu.strangers.tools;

import android.content.Context;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;

/**
 * Created with Android Studio.
 * Author: Enex Tapper
 * Date: 15/5/28
 * Project: Strangers
 * Package: com.neu.strangers.tools
 */
public class DatabaseHelper extends SQLiteOpenHelper {
	public static final String CREATE_USER_TABLE =
			"create table user(" +
					"id integer, " +
					"username text, " +
					"nickname text, " +
					"sex integer, " +
					"picture text," +
					"region text," +
					"sign text," +
					"email text," +
					"background text)";

	public static final String CREATE_FRIENDS_TABLE =
			"create table friends(" +
					"id integer, " +
					"username text, " +
					"nickname text, " +
					"sex integer, " +
					"picture text," +
					"region text," +
					"sign text," +
					"email text," +
					"background text)";

	public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase sqLiteDatabase) {
		sqLiteDatabase.execSQL(CREATE_USER_TABLE);
		sqLiteDatabase.execSQL(CREATE_FRIENDS_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {

	}
}
