package com.neu.strangers.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.balysv.materialripple.MaterialRippleLayout;
import com.github.stuxuhai.jpinyin.PinyinHelper;
import com.neu.strangers.R;
import com.neu.strangers.tools.ImageCache;
import com.neu.strangers.activities.ChatActivity;
import com.woozzu.android.util.StringMatcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created with Android Studio.
 * Author: Enex Tapper
 * Date: 15/5/27
 * Project: Strangers
 * Package: com.neu.strangers.adapter
 */
public class ContactAdapter extends BaseAdapter implements SectionIndexer, AbsListView.OnScrollListener{
	private final static String SECTIONS = "#ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	private ArrayList<ContactAdapterItem> mContactsList;
	private Context mContext;
	private ImageCache mImageCache;
	private ListView mListView;
	private int mStart = 0, mEnd = 0;
	private boolean mFirstFlag;

	public ContactAdapter(Context context, ListView listView) {
		this.mContactsList = new ArrayList<>();
		this.mContext = context;
		this.mImageCache = new ImageCache(context);
		this.mFirstFlag = true;
		this.mListView = listView;

		mListView.setOnScrollListener(this);
		mImageCache.setOnBitmapPreparedListener(new ImageCache.OnBitmapPreparedListener() {
			@Override
			public void onBitmapPrepared(Bitmap bitmap, String tag) {
				Log.v("Scroll Image Cache","事件发生");
				CircleImageView imageView = (CircleImageView) mListView.findViewWithTag(tag);
				if (imageView != null && bitmap != null) {
					imageView.setImageBitmap(bitmap);
				}else if(imageView!=null){
					imageView.setImageResource(R.mipmap.ic_launcher);
				}else{
					Log.v("Scroll Image Cache","image view 空");
				}
			}
		});
	}

	@Override
	public int getCount() {
		return mContactsList.size();
	}

	@Override
	public Object getItem(int i) {
		return mContactsList.get(i);
	}

	@Override
	public long getItemId(int i) {
		return i;
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		LayoutInflater inflate = ((Activity) mContext).getLayoutInflater();
		final ContactAdapterItem item = mContactsList.get(i);

		ViewHolderItem viewHolderItem;
		if(view==null){
			view = inflate.inflate(R.layout.contact_item, null);
			viewHolderItem = new ViewHolderItem();
			viewHolderItem.contactName = (TextView)view.findViewById(R.id.contact_name);
			viewHolderItem.contactAvatar = (CircleImageView)view.findViewById(R.id.contact_avatar);
            viewHolderItem.contactLayout = (RelativeLayout)view.findViewById(R.id.contact_layout);

			view.setTag(viewHolderItem);

		}else{
			viewHolderItem = (ViewHolderItem)view.getTag();
		}

		viewHolderItem.contactName.setText(item.getUserName());
		viewHolderItem.contactAvatar.setTag(item.getTag());

//        viewHolderItem.contactName.setOnClickListener(new View.OnClickListener() {
//	        @Override
//	        public void onClick(View view) {
//		        Intent intent = new Intent(mContext, ChatActivity.class);
//		        intent.putExtra("username", String.valueOf(item.getId()));
//
//		        mContext.startActivity(intent);
//	        }
//        });

		return view;
	}

	@Override
	public Object[] getSections() {
		String[] sections = new String[SECTIONS.length()];
		for (int i = 0; i < SECTIONS.length(); i++)
			sections[i] = String.valueOf(SECTIONS.charAt(i));
		return sections;
	}

	@Override
	public int getPositionForSection(int section) {
		// If there is no item for current section, previous section will be selected
		for (int i = section; i >= 0; i--) {
			for (int j = 0; j < getCount(); j++) {
				if (i == 0) {
					// For numeric section
					for (int k = 0; k <= 9; k++) {
						if (StringMatcher.match(String.valueOf(
								mContactsList.get(j).getPinyin().charAt(0)).toUpperCase(), String.valueOf(k)))
							return j;
					}
				} else {
					if (StringMatcher.match(String.valueOf(
									mContactsList.get(j).getPinyin().charAt(0)).toUpperCase(),
							String.valueOf(SECTIONS.charAt(i))))
						return j;
				}
			}
		}
		return 0;
	}

	@Override
	public int getSectionForPosition(int i) {
		return 0;
	}

	@Override
	public void onScrollStateChanged(AbsListView absListView, int i) {
		if (i == SCROLL_STATE_IDLE) {
			Log.v("Scroll Image Cache","Scroll state changed to idle");
//			mImageCache.loadImages(mStart, mEnd, mContactsList);
			for(int j = mStart ; j < mEnd; j++){
				mImageCache.loadImage(
						mContactsList.get(j).getAvatarUrl(),
						mContactsList.get(j).getTag());
			}
		} else {
			mImageCache.cancelAllTasks();
		}
	}

	@Override
	public void onScroll(AbsListView absListView, int i, int i1, int i2) {
		mStart = i;
		mEnd = i + i1;
		if (mFirstFlag  && i1 > 0) {
			Log.v("Scroll Image Cache","符合条件");
//			mImageCache.loadImages(mStart, mEnd, mContactsList);
			for(int j = mStart ; j < mEnd; j++){
				Log.v("Scroll Image Cache","加载 第"+j+"个");
				mImageCache.loadImage(
						mContactsList.get(j).getAvatarUrl(),
						mContactsList.get(j).getTag());
			}
			mFirstFlag = false;
		}
	}

	public void refreshList(ArrayList<String> contactsList, ArrayList<Integer> idsList){
		this.mContactsList.clear();
		for(int i = 0;i<contactsList.size();i++){
			String item = contactsList.get(i);
			this.mContactsList.add(
					new ContactAdapterItem(idsList.get(i), item, PinyinHelper.getShortPinyin(item)));
		}

		// 在此处排序
		Collections.sort(this.mContactsList, new Comparator<ContactAdapterItem>() {
			@Override
			public int compare(ContactAdapterItem item1, ContactAdapterItem item2) {
				return item1.getPinyin().compareTo(item2.getPinyin());
			}
		});

		notifyDataSetChanged();
	}

	private static class ViewHolderItem {
		TextView contactName;
		CircleImageView contactAvatar;
        RelativeLayout contactLayout;
	}
}
