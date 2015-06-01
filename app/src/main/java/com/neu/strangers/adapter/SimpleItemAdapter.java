package com.neu.strangers.adapter;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.neu.strangers.R;
import com.neu.strangers.activities.ChatActivity;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created with Android Studio.
 * Author: Enex Tapper
 * Date: 15/4/22
 * Project: Strangers
 * Package: com.neu.strangers.adapter
 */
public class SimpleItemAdapter extends RecyclerView.Adapter<SimpleItemAdapter.SimpleTextViewHolder> {
	private final LayoutInflater mLayoutInflater;
    private Context mContext;
	private String[] mTitles = {"第一个测试","第二个测试","第三个测试","第四个测试",
			"第五个测试","第六个测试","第七个测试","第八个测试","第九个测试","第十个测试"};

	public SimpleItemAdapter(Context context) {
		mLayoutInflater = LayoutInflater.from(context);
        mContext = context;
	}

	@Override
	public SimpleTextViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new SimpleTextViewHolder(
				mLayoutInflater.inflate(R.layout.recent_chat_item,parent,false));
	}

	@Override
	public void onBindViewHolder(SimpleTextViewHolder holder, int position) {
		holder.mRecentChatItemTitle.setText(mTitles[position]);
        holder.mRecentChatContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            //    mContext.startActivity(new Intent(mContext, ChatActivity.class));
            }
        });

	}

	@Override
	public int getItemCount() {
		return mTitles==null?0:mTitles.length;
	}

	public static class SimpleTextViewHolder extends RecyclerView.ViewHolder{
		@InjectView(R.id.recent_chat_item_title)
		TextView mRecentChatItemTitle;
        @InjectView(R.id.recent_chat_item_container)
        FrameLayout mRecentChatContainer;

		public SimpleTextViewHolder(View itemView) {
			super(itemView);
			ButterKnife.inject(this,itemView);
		}
	}
}
