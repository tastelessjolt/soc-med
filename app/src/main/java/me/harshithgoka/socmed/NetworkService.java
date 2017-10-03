package me.harshithgoka.socmed;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

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
