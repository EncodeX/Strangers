package com.neu.strangers.adapter;

import android.util.Log;

import com.neu.strangers.tools.DatabaseManager;
import com.neu.strangers.tools.ImageCache;

import net.sqlcipher.Cursor;

/**
 * Created with Android Studio.
 * Author: Enex Tapper
 * Date: 15/5/31
 * Project: Strangers
 * Package: com.neu.strangers.adapter
 */
public class ContactAdapterItem {
	private String userName;
	private String pinyin;
	private int id;
	private String avatarUrl;
	private String tag;

	public ContactAdapterItem(int id,String userName, String pinyin) {
		this.id = id;
		this.userName = userName;
		this.pinyin = pinyin;

		String[] args = {Integer.toString(id)};
		Cursor cursor = DatabaseManager.getInstance().query(
				"friends", null, "id = ?", args, null, null, null);
		if(cursor!=null) {
			if (cursor.moveToNext()) {
				this.avatarUrl = cursor.getString(cursor.getColumnIndex("picture"));
			}else{
				this.avatarUrl =null;
			}
			cursor.close();
		}else{
			this.avatarUrl =null;
		}
		this.tag = ImageCache.toMD5String(Integer.toString(this.id)+this.pinyin+avatarUrl);
		Log.v("Scroll Image Cache","id = "+this.id+" tag = "+this.tag);
	}

	public String getUserName() {
		return userName;
	}

	public String getPinyin() {
		return pinyin;
	}

	public int getId(){
		return id;
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public String getTag() {
		return tag;
	}
}
