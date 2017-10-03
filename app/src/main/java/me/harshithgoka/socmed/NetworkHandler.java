package me.harshithgoka.socmed;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Created by harshithgoka on 03/10/17.
 */

public class NetworkHandler extends Handler {

    private static final String TAG = NetworkHandler.class.getName();
    private Handler sHandler;

    NetworkHandler(Looper looper, Handler handler) {
        super(looper);
        sHandler = handler;
    }

    @Override
    public void handleMessage(Message msg) {
        Log.d(TAG, msg.toString());
        if (msg.what == Constants.ACK) {

        }
        else if (msg.what == Constants.GET_NETWORK_STATE) {
            try {
                URL url = new URL(Constants.URL + "Ping");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                try {
                    connection.getHeaderFields();
                    if (!url.getHost().equals(connection.getURL().getHost())) {
                        // we were redirected! Kick the user out to the browser to sign on?
                        throw new Exception("Login to your internet provider");
                    }
                    StringBuilder stringBuilder = new StringBuilder();

                    try (InputStream in = new BufferedInputStream(connection.getInputStream())) {
                        int nbytes;
                        byte[] bytes = new byte[1024];
                        while ((nbytes = in.read(bytes, 0, 1024)) != -1) {
                            stringBuilder.append(new String(bytes, 0, nbytes));
                        }
                    }

                    JsonParser jsonParser = new JsonParser();
                    JsonObject response = jsonParser.parse(stringBuilder.toString()).getAsJsonObject();
                    Log.d(TAG, response.toString());

                    if (response.get("status").getAsBoolean()) {
                        sHandler.sendMessage(sHandler.obtainMessage(Constants.GET_NETWORK_STATE, Constants.NETWORK_STATE.LOGGED_IN));
                    } else {
                        sHandler.sendMessage(sHandler.obtainMessage(Constants.GET_NETWORK_STATE, Constants.NETWORK_STATE.NOT_LOGGED_IN));
                    }

                } catch (Exception e) {
                    Log.d(TAG, e.toString());
                    sHandler.sendMessage(sHandler.obtainMessage(Constants.GET_NETWORK_STATE, Constants.NETWORK_STATE.NOT_CONNECTED));
                }
                finally {
                    connection.disconnect();
                }
            }
            catch (Exception e) {
                Log.d(TAG, e.toString());
                sHandler.sendMessage(sHandler.obtainMessage(Constants.GET_NETWORK_STATE, Constants.NETWORK_STATE.NOT_CONNECTED));
            }
        }
    }
}

