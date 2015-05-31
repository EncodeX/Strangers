package com.neu.strangers.view;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.astuetz.PagerSlidingTabStrip;
import com.neu.strangers.R;
import com.neu.strangers.adapter.ContactAdapter;
import com.neu.strangers.adapter.SimpleItemAdapter;
import com.woozzu.android.widget.IndexableListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

	public MainViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void initView(Context context){
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
		ArrayList<String> mItems= new ArrayList<String>();
		mItems.add("陈荣森");
		mItems.add("陈腾扬");
		mItems.add("陈泽华");
		mItems.add("孔伟杰");
		mItems.add("冯铄");
		mItems.add("胡少晗");
		mItems.add("田喆");
		mItems.add("杜鹤然");
		mItems.add("邹锐");
		mItems.add("郑媛心");
		mItems.add("房建猷");
		mItems.add("刘荣耀");
		mItems.add("朱志文");
		mItems.add("田安捷");
		mItems.add("郭睿");
		mItems.add("朱越鹏");
		mItems.add("周鹤达");
		mItems.add("于雪晴");
		mItems.add("张文婕");
		mItems.add("田胜");
		mItems.add("赵卓");
		mItems.add("高美妍");
		mItems.add("张文昭");
		mItems.add("赵露");
		mItems.add("汤成铃");
		mItems.add("邵彦恒");
		mItems.add("杨旭恒");
		mItems.add("王浩");
		mItems.add("李广龙");
		mItems.add("徐献博");

		mAdapter = new ContactAdapter(context);
		mAdapter.refreshList(mItems);

		mContactsList.setAdapter(mAdapter);
		mContactsList.setFastScrollEnabled(true);

		mViewList.add(recentChatView);
		mViewList.add(contactsView);

		TabPagerAdapter pagerAdapter = new TabPagerAdapter(context);
		this.setAdapter(pagerAdapter);
	}

	private class TabPagerAdapter extends PagerAdapter implements PagerSlidingTabStrip.CustomTabProvider {
		private final int[] TITLES = {R.string.main_tab_recent_chat,R.string.main_tab_contacts};
		private Context mContext;

		private TabPagerAdapter(Context mContext) {
			this.mContext = mContext;
		}

		@Override
		public View getCustomTabView(ViewGroup viewGroup, int i) {
			MyRippleLayout rippleView =
					(MyRippleLayout) LayoutInflater.from(mContext)
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
}
