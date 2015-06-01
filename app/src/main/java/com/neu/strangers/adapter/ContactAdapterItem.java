package com.neu.strangers.adapter;

import com.neu.strangers.tools.DatabaseManager;

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
		}else{
			this.avatarUrl =null;
		}
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
}
