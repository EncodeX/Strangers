package com.neu.strangers.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.material.widget.PaperButton;
import com.neu.strangers.R;
import com.neu.strangers.adapter.ChatAdapter;
import com.neu.strangers.bean.ChatInfo;
import com.neu.strangers.tools.XmppTool;
import com.neu.strangers.view.DropdownListView;
import com.neu.strangers.view.ChatEditText;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Registration;
import org.jivesoftware.smack.util.StringUtils;

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
    XMPPConnection mXmppConnection;
    private String reply = "";// 模拟回复
   // TaxiChatManagerListener chatManagerListener;
   //  ChatManager chatmanager;

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

                    /*
                    Chat chat = chatmanager.createChat("user@120.24.76.184",new ChatLi());
                    org.jivesoftware.smack.packet.Message message = new org.jivesoftware.smack.packet.Message();
                    message.setBody("hello");

                    try {
                        chat.sendMessage(message);
                        //chat.addMessageListener(new ChatLi());
                    } catch (XMPPException e) {
                        e.printStackTrace();
                    }
                    */

                }
                break;

            default:
                break;
        }
    }

    /*
    class TaxiChatManagerListener implements ChatManagerListener {

        public void chatCreated(Chat chat, boolean arg1) {
            chat.addMessageListener(new MessageListener() {


                @Override
                public void processMessage(Chat chat, org.jivesoftware.smack.packet.Message message) {
                    message.getFrom();
                    //消息内容
                    String body = message.getBody();
                    Log.e("body",body);
                    Log.e("body",body);



                }
            });
        }


    }



    //MessageListener
    class ChatLi implements MessageListener
    {


        @Override
        public void processMessage(Chat chat, org.jivesoftware.smack.packet.Message message) {
            message.getFrom();
            //消息内容
            String body = message.getBody();
               Log.e("body!",body);
            Log.e("body!",body);
            Log.e("!",message.getFrom());
           // message.getTo()
        }
    }
    */



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
       // chatmanager = XmppTool.getChatManager();

      //  chatManagerListener = new TaxiChatManagerListener();
      //  chatmanager.addChatListener(chatManagerListener);




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
