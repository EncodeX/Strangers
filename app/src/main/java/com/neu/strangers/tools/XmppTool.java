package com.neu.strangers.tools;

import android.os.AsyncTask;
import android.util.Log;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Registration;

/**
 * Created by Administrator on 2015/5/28 0028.
 */
public class XmppTool {

    private static XMPPConnection mXmppConnection;
    private static ChatManager mChatManager;
    private static Chat mChat;
    private static String mUserName = "";
    private static String mPassword = "";

    private static String HOST = "www.shiguangtravel.com";
    private static String XmppTool = "XmppTool";

    public static void connect() {
        new connectToServerTask().execute(0);
    }

    public static ChatManager getChatManager(){
       if(mXmppConnection != null){
           mChatManager = mXmppConnection.getChatManager();
       }
        return mChatManager;
    }

    public static void Login(String name,String password){
        mUserName = name;
        mPassword = password;
        new connectToServerTask().execute(1);

    }

    public static void Register(String name,String password){
        mUserName = name;
        mPassword = password;
        new connectToServerTask().execute(2);
    }


    private static class connectToServerTask extends AsyncTask<Integer,Void,Void>
    {


        @Override
        protected Void doInBackground(Integer... integers) {
            switch (integers[0])
            {
                case 0:
                    mXmppConnection = new XMPPConnection(HOST);
                    try {

                        mXmppConnection.connect();
                        Log.i(XmppTool, "connect success");

                    } catch (XMPPException e) {
                        e.printStackTrace();

                        Log.i(XmppTool, "error");
                    }
                    break;
                case 1:
                    if(mXmppConnection != null){
                        try {
                            mXmppConnection.login(mUserName,mPassword);
                            Log.i(XmppTool, "Login success");
                        } catch (XMPPException e) {
                            e.printStackTrace();
                            Log.i(XmppTool, "Login failed");
                        }
                    }
                    break;
                case 2:
                    if(mXmppConnection != null) {
                        Registration reg = new Registration();
                        reg.setType(IQ.Type.SET);
                        //  reg.setTo(mXmppConnection.getInstance().getServiceName());
                        reg.setUsername(mUserName);
                        reg.setPassword(mPassword);
                        mXmppConnection.sendPacket(reg);

                    }
                    break;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mUserName = "";
            mPassword = "";
        }
    }
}
