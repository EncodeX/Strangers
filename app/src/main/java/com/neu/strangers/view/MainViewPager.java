package com.neu.strangers.view;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.astuetz.PagerSlidingTabStrip;
import com.balysv.materialripple.MaterialRippleLayout;
import com.neu.strangers.R;
import com.neu.strangers.activities.ChatActivity;
import com.neu.strangers.activities.ProfileActivity;
import com.neu.strangers.adapter.ContactAdapter;
import com.neu.strangers.adapter.ContactAdapterItem;
import com.neu.strangers.adapter.SimpleItemAdapter;
import com.neu.strangers.tools.Constants;
import com.neu.strangers.tools.DatabaseManager;
import com.pnikosis.materialishprogress.ProgressWheel;
import com.woozzu.android.widget.IndexableListView;

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
import java.util.Collections;
import java.util.List;

import me.drakeet.materialdialog.MaterialDialog;

/**
 * Created with Android Studio.
 * Author: Enex Tapper
 * Date: 15/4/22
 * Project: Strangers
 * Package: com.neu.strangers.views
 */
public class MainViewPager extends ViewPager {
	private List<View> mViewList;

	RecyclerView mRecentChatList;

	IndexableListView mContactsList;
	ContactAdapter mAdapter;
	RelativeLayout mProgressLayout;

	public MainViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void initView(final Context context){
		mViewList = new ArrayList<>();

		// Initialize recent chat view.
		View recentChatView = View.inflate(context, R.layout.page_recent_chat, null);

		// Initialize recentChatList
		mRecentChatList = (RecyclerView)recentChatView.findViewById(R.id.recent_chat_list);
		mRecentChatList.setLayoutManager(new LinearLayoutManager(context));
		mRecentChatList.setAdapter(new SimpleItemAdapter(context));
		mRecentChatList.setClipToPadding(true);

		// Initialize contacts view.
		View contactsView = View.inflate(context, R.layout.page_contacts, null);

		// Initialize contacts list
		mContactsList = (IndexableListView)contactsView.findViewById(R.id.contacts_list);
		mProgressLayout = (RelativeLayout)contactsView.findViewById(R.id.progress_layout);

		mAdapter = new ContactAdapter(context,mContactsList);
		mContactsList.setAdapter(mAdapter);
		mContactsList.setFastScrollEnabled(true);
		mContactsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				if (mAdapter.getItem(i) instanceof ContactAdapterItem) {
					Intent intent = new Intent(context, ProfileActivity.class);
					intent.putExtra(Constants.Application.PROFILE_USER_ID,
							((ContactAdapterItem) mAdapter.getItem(i)).getId());
					context.startActivity(intent);
				}
			}
		});

		mViewList.add(recentChatView);
		mViewList.add(contactsView);

		TabPagerAdapter pagerAdapter = new TabPagerAdapter(context);
		this.setAdapter(pagerAdapter);

		refreshContacts(context);
	}

	public void refreshContacts(Context context){
		mProgressLayout.setVisibility(VISIBLE);
		new GetContacts().execute(context.getSharedPreferences(Constants.Application.PREFERENCE_NAME,0)
				.getInt(Constants.Application.LOGGED_IN_USER_ID,-1));
	}

	private class TabPagerAdapter extends PagerAdapter implements PagerSlidingTabStrip.CustomTabProvider {
		private final int[] TITLES = {R.string.main_tab_recent_chat,R.string.main_tab_contacts};
		private Context mContext;

		private TabPagerAdapter(Context mContext) {
			this.mContext = mContext;
		}

		@Override
		public View getCustomTabView(ViewGroup viewGroup, int i) {
			MaterialRippleLayout rippleView =
					(MaterialRippleLayout) LayoutInflater.from(mContext)
							.inflate(R.layout.main_tab, viewGroup, false);
			((TextView)rippleView.findViewById(R.id.psts_tab_title)).setText(TITLES[i]);
			return rippleView;
		}

		@Override
		public int getCount() {
			return mViewList.size();
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			container.addView(mViewList.get(position));
			return mViewList.get(position);
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView(mViewList.get(position));
		}
	}

	private class GetContacts extends AsyncTask<Integer,Integer,JSONObject> {
		// todo 需要优化错误处理等

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected JSONObject doInBackground(Integer... integers) {
			try {
				StringBuilder stringBuilder = new StringBuilder(
						"http://www.shiguangtravel.com:8080/CN-Soft/servlet/ShowFriends");
				stringBuilder.append("?");
				stringBuilder.append("id=" + URLEncoder.encode(Integer.toString(integers[0]), "UTF-8"));

				URL url = new URL(stringBuilder.toString());
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
				}else{
//					showErrorDialog("数据获取失败","无法连接服务器。");
				}
			} catch (JSONException | IOException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(JSONObject jsonObject) {
			try {
				if(jsonObject!=null){
					Cursor cursor = DatabaseManager.getInstance().query("friends", null, null, null, null, null, null);

					if(cursor!=null){
						DatabaseManager.getInstance().delete("friends","",null);
						cursor.close();
					}

					JSONArray contactList = jsonObject.getJSONArray("ShowFriends");
					ArrayList<String> names= new ArrayList<String>();
					ArrayList<Integer> ids = new ArrayList<>();

					for(int i=0;i<contactList.length();i++){
						JSONObject contact = contactList.getJSONObject(i);

						int id = contact.getInt("id");
						String username = contact.getString("username");
						String nickname = contact.getString("nickname");
						int sex = contact.getString("sex").equals("woman")?1:0;
						String picture = contact.getString("picture");
						String region = contact.getString("region");
						String sign = contact.getString("sign");
						String email = contact.getString("email");
						String background = contact.getString("mybackground");

						// Todo 存入数据库实现 但目前状况下并没有很好的数据更新逻辑 需要考虑
						ContentValues values = new ContentValues();
						values.put("id",id);
						values.put("username",username);
						values.put("nickname",nickname);
						values.put("sex",sex);
						values.put("picture",picture);
						values.put("region",region);
						values.put("sign",sign);
						values.put("email",email);
						values.put("background",background);

						DatabaseManager.getInstance().insert("friends", null, values);

						// 刷新列表
						names.add(nickname);
						ids.add(id);
					}
					mAdapter.refreshList(names, ids);
					mProgressLayout.setVisibility(GONE);

//					dialog.dismiss();
				}else{
//					showErrorDialog("数据获取失败","未知错误。");
				}
			} catch (JSONException e) {
//				showErrorDialog("数据获取失败","无法解析用户数据。");
			}
		}
	}
}
