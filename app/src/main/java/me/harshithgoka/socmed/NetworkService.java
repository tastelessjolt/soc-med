package me.harshithgoka.socmed;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.google.gson.JsonObject;

import org.json.JSONArray;

import java.lang.ref.WeakReference;

public class NetworkService extends Service {

    public static final String TAG = NetworkService.class.getName();

    public NetworkService() {
    }

    public Handler mHandler = null;
    private Handler tHandler = null;
    private NetworkThread thread = null;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            int what = intent.getIntExtra(Constants.WHAT, -1);
            if (what == Constants.GET_NETWORK_STATE) {
                tHandler.sendMessage(tHandler.obtainMessage(Constants.GET_NETWORK_STATE));
            }
            else if (what == Constants.GET_FEED) {
                tHandler.sendMessage(tHandler.obtainMessage(Constants.GET_FEED));
            }
            else if (what == Constants.GET_MY_POSTS) {
                tHandler.sendMessage(tHandler.obtainMessage(Constants.GET_MY_POSTS));
            }
            else if (what == Constants.WRITE_POST) {
                tHandler.sendMessage(tHandler.obtainMessage(what, intent.getStringExtra(Constants.INTENT_DATA)));
            }
            else if (what == Constants.WRITE_COMMENT) {
                tHandler.sendMessage(tHandler.obtainMessage(what, intent.getBundleExtra(Constants.INTENT_DATA)));
            }
            else if (what == Constants.GET_USER_POSTS) {
                tHandler.sendMessage(tHandler.obtainMessage(what, intent.getBundleExtra(Constants.INTENT_DATA)));
            }
            else if (what == Constants.FOLLOW) {
                tHandler.sendMessage(tHandler.obtainMessage(what, intent.getBundleExtra(Constants.INTENT_DATA)));
            }
            else if (what == Constants.UNFOLLOW) {
                tHandler.sendMessage(tHandler.obtainMessage(what, intent.getBundleExtra(Constants.INTENT_DATA)));
            }
        }

        return Service.START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Log.d(TAG, msg.toString());
                handlerMsg(msg);
            }
        };

        WeakReference<Handler> handlerWeakReference = new WeakReference<Handler>(mHandler);
        thread = new NetworkThread("networking-boo-yah", handlerWeakReference);
        thread.start();
        tHandler = new NetworkHandler(thread.getLooper(), mHandler);
    }

    void handlerMsg (Message msg) {
        if (msg.what == Constants.GET_NETWORK_STATE){
            Constants.NETWORK_STATE state = (Constants.NETWORK_STATE) msg.obj;
            if (Constants.currHandler != null) {
                Constants.currHandler.sendMessage(Constants.currHandler.obtainMessage(Constants.GET_NETWORK_STATE, state));
            }
        }
        else if (msg.what == Constants.GET_FEED) {
            JsonObject jsonObject = (JsonObject) msg.obj;
            if (jsonObject != null) {
                if (Constants.currHandler != null) {
                    Constants.currHandler.sendMessage(Constants.currHandler.obtainMessage(Constants.GET_FEED, jsonObject));
                }
            }
        }
        else if (msg.what == Constants.GET_MY_POSTS) {
            JsonObject jsonObject = (JsonObject) msg.obj;
            if (jsonObject != null) {
                if (Constants.currHandler != null) {
                    Constants.currHandler.sendMessage(Constants.currHandler.obtainMessage(Constants.GET_MY_POSTS, jsonObject));
                }
            }
        }
        else if (msg.what == Constants.WRITE_POST) {
            if (msg.arg1 == Constants.TRUE ) {
                Constants.currHandler.sendMessage(Constants.currHandler.obtainMessage(Constants.WRITE_POST, Constants.TRUE, 0, null));
            }
            else if (msg.arg1 == Constants.FALSE) {
                Constants.currHandler.sendMessage(Constants.currHandler.obtainMessage(Constants.WRITE_POST, Constants.FALSE, 0, null));
            }
            else {
                Constants.currHandler.sendMessage(Constants.currHandler.obtainMessage(Constants.GET_NETWORK_STATE, Constants.NETWORK_STATE.NOT_LOGGED_IN));
            }
        }
        else if (msg.what == Constants.WRITE_COMMENT) {
            if (msg.arg1 == Constants.TRUE ) {
                Constants.currHandler.sendMessage(Constants.currHandler.obtainMessage(Constants.WRITE_COMMENT, Constants.TRUE, 0, msg.obj));
            }
            else if (msg.arg1 == Constants.FALSE) {
                Constants.currHandler.sendMessage(Constants.currHandler.obtainMessage(Constants.WRITE_COMMENT, Constants.FALSE, 0, msg.obj));
            }
            else {
                Constants.currHandler.sendMessage(Constants.currHandler.obtainMessage(Constants.GET_NETWORK_STATE, Constants.NETWORK_STATE.NOT_LOGGED_IN));
            }
        }
        else if (msg.what == Constants.GET_USER_POSTS) {
            Constants.currHandler.sendMessage(Constants.currHandler.obtainMessage(Constants.GET_USER_POSTS, msg.obj));
        }
        else if (msg.what == Constants.FOLLOW) {
            Constants.currHandler.sendMessage(Constants.currHandler.obtainMessage(Constants.FOLLOW, msg.arg1, 0, msg.obj));
        }
        else if (msg.what == Constants.UNFOLLOW) {
            Constants.currHandler.sendMessage(Constants.currHandler.obtainMessage(Constants.UNFOLLOW, msg.arg1, 0, msg.obj));
        }

        tHandler.dispatchMessage(tHandler.obtainMessage(Constants.ACK, "Cool!"));
    }

    @Override
    public void onDestroy() {
        tHandler.getLooper().quit();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Requirement not to bind to a component
        return null;
    }



}
