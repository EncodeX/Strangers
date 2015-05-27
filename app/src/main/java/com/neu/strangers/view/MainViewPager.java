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
		mItems.add("aback");
		mItems.add("abash");
		mItems.add("abbey");
		mItems.add("abhor");
		mItems.add("abide");
		mItems.add("abuse");
		mItems.add("candidate");
		mItems.add("capture");
		mItems.add("careful");
		mItems.add("catch");
		mItems.add("cause");
		mItems.add("celebrate");
		mItems.add("forever");
		mItems.add("fable");
		mItems.add("fidelity");
		mItems.add("fox");
		mItems.add("funny");
		mItems.add("fail");
		mItems.add("jail");
		mItems.add("jade");
		mItems.add("jailor");
		mItems.add("january");
		mItems.add("jasmine");
		mItems.add("jazz");
		mItems.add("zero");
		mItems.add("zoo");
		mItems.add("zeus");
		mItems.add("zebra");
		mItems.add("zest");
		mItems.add("zing");
		Collections.sort(mItems);

		mAdapter = new ContactAdapter(mItems,context);

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
