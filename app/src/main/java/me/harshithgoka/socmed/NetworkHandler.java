package me.harshithgoka.socmed;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.AbstractMap;
import java.util.ArrayList;
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
            Constants.NETWORK_STATE state;
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
                        state = Constants.NETWORK_STATE.LOGGED_IN;
                    } else {
                        state = Constants.NETWORK_STATE.NOT_LOGGED_IN;
                    }

                } catch (Exception e) {
                    Log.d(TAG, e.toString());
                    state = Constants.NETWORK_STATE.NOT_CONNECTED;
                }
                finally {
                    connection.disconnect();
                }
            }
            catch (Exception e) {
                Log.d(TAG, e.toString());
                state = Constants.NETWORK_STATE.NOT_CONNECTED;
            }
            sHandler.sendMessage(sHandler.obtainMessage(Constants.GET_NETWORK_STATE, state));
        }
        else if (msg.what == Constants.GET_FEED) {
            try {
                URL url = new URL(Constants.URL + "SeePosts");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                try {
                    connection.getHeaderFields();
                    if (!url.getHost().equals(connection.getURL().getHost())) {
                        // we were redirected! Kick the user out to the browser to sign on?
                        throw new Exception("Login to your internet provider");
                    }

                    JsonObject response = Utils.getAndParse(connection.getInputStream());
                    Log.d(TAG, response.toString());

                    if (response.get("status").getAsBoolean()) {
                        sHandler.sendMessage(sHandler.obtainMessage(Constants.GET_FEED, response));
                    } else {
                        sHandler.sendMessage(sHandler.obtainMessage(Constants.GET_NETWORK_STATE, Constants.NETWORK_STATE.NOT_LOGGED_IN));
                    }


                }
                catch (Exception e) {
                    Log.d(TAG, e.toString());
                    sHandler.sendMessage(sHandler.obtainMessage(Constants.GET_NETWORK_STATE, Constants.NETWORK_STATE.NOT_CONNECTED));
                } finally {
                    connection.disconnect();
                }

            } catch (MalformedURLException e) {
                Log.d(TAG, e.toString());
                sHandler.sendMessage(sHandler.obtainMessage(Constants.GET_NETWORK_STATE, Constants.NETWORK_STATE.NOT_CONNECTED));
            } catch (IOException e) {
                Log.d(TAG, e.toString());
                sHandler.sendMessage(sHandler.obtainMessage(Constants.GET_NETWORK_STATE, Constants.NETWORK_STATE.NOT_CONNECTED));
            }
        }
        else if (msg.what == Constants.GET_MY_POSTS) {
            try {
                URL url = new URL(Constants.URL + "SeeMyPosts");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                try {
                    connection.getHeaderFields();
                    if (!url.getHost().equals(connection.getURL().getHost())) {
                        // we were redirected! Kick the user out to the browser to sign on?
                        throw new Exception("Login to your internet provider");
                    }

                    JsonObject response = Utils.getAndParse(connection.getInputStream());
                    Log.d(TAG, response.toString());

                    if (response.get("status").getAsBoolean()) {
                        sHandler.sendMessage(sHandler.obtainMessage(Constants.GET_MY_POSTS, response));
                    } else {
                        sHandler.sendMessage(sHandler.obtainMessage(Constants.GET_NETWORK_STATE, Constants.NETWORK_STATE.NOT_LOGGED_IN));
                    }

                } catch (IOException e) {
                    Log.d(TAG, e.toString());
                    sHandler.sendMessage(sHandler.obtainMessage(Constants.GET_NETWORK_STATE, Constants.NETWORK_STATE.NOT_CONNECTED));
                } catch (Exception e) {
                    Log.d(TAG, e.toString());
                    sHandler.sendMessage(sHandler.obtainMessage(Constants.GET_NETWORK_STATE, Constants.NETWORK_STATE.NOT_CONNECTED));
                } finally {
                    connection.disconnect();
                }
            } catch (MalformedURLException e) {
                Log.d(TAG, e.toString());
                sHandler.sendMessage(sHandler.obtainMessage(Constants.GET_NETWORK_STATE, Constants.NETWORK_STATE.NOT_CONNECTED));
            } catch (IOException e) {
                Log.d(TAG, e.toString());
                sHandler.sendMessage(sHandler.obtainMessage(Constants.GET_NETWORK_STATE, Constants.NETWORK_STATE.NOT_CONNECTED));
            }
        }

        else if (msg.what == Constants.WRITE_POST) {
            try {
                URL url = new URL(Constants.URL + "CreatePost");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                try {
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);
                    connection.setDoInput(true);

                    List<AbstractMap.SimpleEntry> list = new ArrayList<AbstractMap.SimpleEntry>();
                    list.add(new AbstractMap.SimpleEntry("content", (String) msg.obj));

                    OutputStream os = connection.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(
                            new OutputStreamWriter(os, "UTF-8"));
                    writer.write(Utils.getQuery(list));
                    writer.flush();
                    writer.close();
                    os.close();

                    connection.getHeaderFields();

                    if (!url.getHost().equals(connection.getURL().getHost())) {
                        // we were redirected! Kick the user out to the browser to sign on?
                        throw new Exception("Login to your internet provider");
                    }


                    JsonObject response = Utils.getAndParse(connection.getInputStream());
                    Log.d(TAG, response.toString());

                    if (response.get("status").getAsBoolean()) {
                        sHandler.sendMessage(sHandler.obtainMessage(Constants.WRITE_POST, Constants.TRUE, 0, null));
                    } else {
                        sHandler.sendMessage(sHandler.obtainMessage(Constants.WRITE_POST, Constants.FALSE, 0, null));
                    }


                } catch (Exception e) {
                    Log.d(TAG, e.toString());
                    sHandler.sendMessage(sHandler.obtainMessage(Constants.GET_NETWORK_STATE, Constants.NETWORK_STATE.NOT_CONNECTED));
                } finally {
                    connection.disconnect();
                }

            } catch (MalformedURLException e) {
                Log.d(TAG, e.toString());
                sHandler.sendMessage(sHandler.obtainMessage(Constants.GET_NETWORK_STATE, Constants.NETWORK_STATE.NOT_CONNECTED));
            } catch (IOException e) {
                Log.d(TAG, e.toString());
                sHandler.sendMessage(sHandler.obtainMessage(Constants.GET_NETWORK_STATE, Constants.NETWORK_STATE.NOT_CONNECTED));
            }
        }
        else if (msg.what == Constants.WRITE_COMMENT) {
            Bundle bundle = (Bundle) msg.obj;
            try {
                URL url = new URL(Constants.URL + "NewComment");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                try {
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);
                    connection.setDoInput(true);

                    List<AbstractMap.SimpleEntry> list = new ArrayList<AbstractMap.SimpleEntry>();
                    list.add(new AbstractMap.SimpleEntry("postid", "" + bundle.getInt(Constants.POST_ID)));
                    list.add(new AbstractMap.SimpleEntry("content", (String) bundle.getString(Constants.COMMENT_TEXT)));

                    OutputStream os = connection.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(
                            new OutputStreamWriter(os, "UTF-8"));
                    writer.write(Utils.getQuery(list));
                    writer.flush();
                    writer.close();
                    os.close();

                    connection.getHeaderFields();

                    if (!url.getHost().equals(connection.getURL().getHost())) {
                        // we were redirected! Kick the user out to the browser to sign on?
                        throw new Exception("Login to your internet provider");
                    }


                    JsonObject response = Utils.getAndParse(connection.getInputStream());
                    Log.d(TAG, response.toString());

                    if (response.get("status").getAsBoolean()) {
                        sHandler.sendMessage(sHandler.obtainMessage(Constants.WRITE_COMMENT, Constants.TRUE, 0, bundle));
                    } else {
                        sHandler.sendMessage(sHandler.obtainMessage(Constants.WRITE_COMMENT, Constants.FALSE, 0, bundle));
                    }

                } catch (Exception e) {
                    Log.d(TAG, e.toString());
                    sHandler.sendMessage(sHandler.obtainMessage(Constants.GET_NETWORK_STATE, Constants.NETWORK_STATE.NOT_CONNECTED));
                } finally {
                    connection.disconnect();
                }

            } catch (MalformedURLException e) {
                Log.d(TAG, e.toString());
                sHandler.sendMessage(sHandler.obtainMessage(Constants.GET_NETWORK_STATE, Constants.NETWORK_STATE.NOT_CONNECTED));
            } catch (IOException e) {
                Log.d(TAG, e.toString());
                sHandler.sendMessage(sHandler.obtainMessage(Constants.GET_NETWORK_STATE, Constants.NETWORK_STATE.NOT_CONNECTED));
            }
        }
    }
}

