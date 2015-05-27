package com.neu.strangers.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.neu.strangers.R;
import com.woozzu.android.util.StringMatcher;

import java.util.ArrayList;

/**
 * Created with Android Studio.
 * Author: Enex Tapper
 * Date: 15/5/27
 * Project: Strangers
 * Package: com.neu.strangers.adapter
 */
public class ContactAdapter extends BaseAdapter implements SectionIndexer{
	private final static String SECTIONS = "#ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	private ArrayList<String> stringArray;
	private Context context;

	public ContactAdapter(ArrayList<String> stringArray, Context context) {
		this.stringArray = stringArray;
		this.context = context;
	}

	@Override
	public int getCount() {
		return stringArray.size();
	}

	@Override
	public Object getItem(int i) {
		return stringArray.get(i);
	}

	@Override
	public long getItemId(int i) {
		return i;
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		LayoutInflater inflate = ((Activity) context).getLayoutInflater();
		String name = stringArray.get(i);

		view = inflate.inflate(R.layout.contact_item, null);

		TextView contactName = (TextView)view.findViewById(R.id.contact_name);
		contactName.setText(name);

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
								((String) getItem(j)).charAt(0)).toUpperCase(), String.valueOf(k)))
							return j;
					}
				} else {
					if (StringMatcher.match(String.valueOf(
							((String)getItem(j)).charAt(0)).toUpperCase(),
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
}
