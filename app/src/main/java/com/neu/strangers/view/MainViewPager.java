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
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.astuetz.PagerSlidingTabStrip;
import com.balysv.materialripple.MaterialRippleLayout;
import com.neu.strangers.R;
import com.neu.strangers.adapter.SimpleItemAdapter;
import com.woozzu.android.util.StringMatcher;
import com.woozzu.android.widget.IndexableListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

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
		mItems.add("Diary of a Wimpy Kid 6: Cabin Fever");
		mItems.add("Steve Jobs");
		mItems.add("Inheritance (The Inheritance Cycle)");
		mItems.add("11/22/63: A Novel");
		mItems.add("The Hunger Games");
		mItems.add("The LEGO Ideas Book");
		mItems.add("Explosive Eighteen: A Stephanie Plum Novel");
		mItems.add("Catching Fire (The Second Book of the Hunger Games)");
		mItems.add("Elder Scrolls V: Skyrim: Prima Official Game Guide");
		mItems.add("Death Comes to Pemberley");
		mItems.add("Diary of a Wimpy Kid 6: Cabin Fever");
		mItems.add("Steve Jobs");
		mItems.add("Inheritance (The Inheritance Cycle)");
		mItems.add("11/22/63: A Novel");
		mItems.add("The Hunger Games");
		mItems.add("The LEGO Ideas Book");
		mItems.add("Explosive Eighteen: A Stephanie Plum Novel");
		mItems.add("Catching Fire (The Second Book of the Hunger Games)");
		mItems.add("Elder Scrolls V: Skyrim: Prima Official Game Guide");
		mItems.add("Death Comes to Pemberley");
		Collections.sort(mItems);

		ContentAdapter adapter = new ContentAdapter(context, android.R.layout.simple_list_item_1, mItems);

		mContactsList.setAdapter(adapter);
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

	private class ContentAdapter extends ArrayAdapter<String> implements SectionIndexer {

		private String mSections = "#ABCDEFGHIJKLMNOPQRSTUVWXYZ";

		public ContentAdapter(Context context, int textViewResourceId,
		                      List<String> objects) {
			super(context, textViewResourceId, objects);
		}

		@Override
		public int getPositionForSection(int section) {
			// If there is no item for current section, previous section will be selected
			for (int i = section; i >= 0; i--) {
				for (int j = 0; j < getCount(); j++) {
					if (i == 0) {
						// For numeric section
						for (int k = 0; k <= 9; k++) {
							if (StringMatcher.match(String.valueOf(getItem(j).charAt(0)), String.valueOf(k)))
								return j;
						}
					} else {
						if (StringMatcher.match(String.valueOf(getItem(j).charAt(0)), String.valueOf(mSections.charAt(i))))
							return j;
					}
				}
			}
			return 0;
		}

		@Override
		public int getSectionForPosition(int position) {
			return 0;
		}

		@Override
		public Object[] getSections() {
			String[] sections = new String[mSections.length()];
			for (int i = 0; i < mSections.length(); i++)
				sections[i] = String.valueOf(mSections.charAt(i));
			return sections;
		}
	}
}
