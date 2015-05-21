package com.neu.strangers.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import com.material.widget.PaperButton;
import com.neu.strangers.R;
import com.neu.strangers.adapter.ChatAdapter;
import com.neu.strangers.bean.ChatInfo;
import com.neu.strangers.view.DropdownListView;
import com.neu.strangers.view.ChatEditText;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Administrator on 2015/5/21 0021.
 */
public class ChatActivity extends ActionBarActivity implements View.OnClickListener,
        DropdownListView.OnRefreshListenerHeader {

    @InjectView(R.id.input_sms)
    ChatEditText mInput;
    @InjectView(R.id.send_sms)
    PaperButton mSend;
    @InjectView(R.id.message_chat_listview)
    DropdownListView mListView;
    @InjectView(R.id.tool_bar)
    Toolbar mToolbar;
    
    private ChatAdapter mChatAdapter;
    private SystemBarTintManager mSystemBarTintManager;
    private LinkedList<ChatInfo> mInfos = new LinkedList<ChatInfo>();
    private SimpleDateFormat mSimpleDateFormat;

    private String reply = "";// 模拟回复

    @SuppressLint("SimpleDateFormat")
    private void initViews() {
     
        mSimpleDateFormat = new SimpleDateFormat("MM-dd HH:mm");
        // 模拟收到信息
        mInfos.add(getChatInfoFrom("hello！"));
        mInfos.add(getChatInfoFrom("world"));

        mChatAdapter = new ChatAdapter(this, mInfos);
        mListView.setAdapter(mChatAdapter);
        mInput.setOnClickListener(this);
        mSend.setOnClickListener(this);
        mListView.setOnRefreshListenerHead(this);

    }

    @Override
    public void onClick(View arg0) {
        switch (arg0.getId()) {
            case R.id.input_sms:// 输入框

                break;

            case R.id.send_sms:// 发送
                reply = mInput.getText().toString();
                if (!TextUtils.isEmpty(reply)) {
                    mInfos.add(getChatInfoTo(reply));
                    mChatAdapter.setList(mInfos);
                    mChatAdapter.notifyDataSetChanged();
                    mListView.setSelection(mInfos.size() - 1);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mInfos.add(getChatInfoFrom(reply));
                            mChatAdapter.setList(mInfos);
                            mChatAdapter.notifyDataSetChanged();
                            mListView.setSelection(mInfos.size() - 1);
                        }
                    }, 1000);
                    mInput.setText("");
                }
                break;

            default:
                break;
        }
    }


     // 发送的信息

    private ChatInfo getChatInfoTo(String message) {
        ChatInfo info = new ChatInfo();
        info.content = message;
        info.fromOrTo = 1;
        info.time = mSimpleDateFormat.format(new Date());
        return info;
    }


     //* 接收的信息

    private ChatInfo getChatInfoFrom(String message) {
        ChatInfo info = new ChatInfo();
        info.content = message;
        info.fromOrTo = 0;
        info.time = mSimpleDateFormat.format(new Date());
        return info;
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    mChatAdapter.setList(mInfos);
                    mChatAdapter.notifyDataSetChanged();
                    mListView.onRefreshCompleteHeader();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        ButterKnife.inject(this);

        // Add back button
        setSupportActionBar(mToolbar);
        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
                ChatActivity.this.overridePendingTransition(R.anim.fade_out_in, R.anim.fade_out_out);
            }
        });

        mSystemBarTintManager = new SystemBarTintManager(this);
        mSystemBarTintManager.setStatusBarTintEnabled(true);
        mSystemBarTintManager.setTintColor(getResources().getColor(R.color.app_color_primary_dark));

        initViews();
    }

    @Override
    public void onRefresh() {
        new Thread() {
            @Override
            public void run() {
                try {
                    sleep(1000);
                    Message msg = mHandler.obtainMessage(0);
                    mHandler.sendMessage(msg);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }



}
