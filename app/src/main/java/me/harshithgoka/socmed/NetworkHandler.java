package me.harshithgoka.socmed;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
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
import java.util.zip.CheckedOutputStream;

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
                        Storage.setName(response.get("data").getAsString());
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
            Bundle bundle = (Bundle) msg.obj;
            int offset = bundle.getInt(Constants.OFFSET, 0);
            int limit = bundle.getInt(Constants.LIMIT, Constants.MAX_LIMIT);
            try {
                URL url = new URL(Constants.URL + "SeePosts" + "?offset=" + offset + "&limit=" + limit);
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
                        bundle.putString(Constants.POSTS, response.get("data").getAsJsonArray().toString());
                        sHandler.sendMessage(sHandler.obtainMessage(Constants.GET_FEED, bundle));
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
            Bundle bundle = (Bundle) msg.obj;
            int offset = bundle.getInt(Constants.OFFSET, 0);
            int limit = bundle.getInt(Constants.LIMIT, Constants.MAX_LIMIT);
            try {
                URL url = new URL(Constants.URL + "SeeMyPosts" + "?offset=" + offset + "&limit=" + limit);
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
                        bundle.putString(Constants.POSTS, response.get("data").getAsJsonArray().toString());
                        sHandler.sendMessage(sHandler.obtainMessage(Constants.GET_MY_POSTS, bundle));
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

        else if (msg.what == Constants.GET_USER_POSTS) {
            Bundle bundle = (Bundle) msg.obj;
            int offset = bundle.getInt(Constants.OFFSET, 0);
            int limit = bundle.getInt(Constants.LIMIT, Constants.MAX_LIMIT);
            try {
                URL url = new URL(Constants.URL + "SeeUserPosts");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                try {
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);
                    connection.setDoInput(true);

                    List<AbstractMap.SimpleEntry> list = new ArrayList<AbstractMap.SimpleEntry>();
                    list.add(new AbstractMap.SimpleEntry("uid", ( (User) bundle.getSerializable(Constants.USER_DATA)).uid ));
                    list.add(new AbstractMap.SimpleEntry("offset", "" + offset));
                    list.add(new AbstractMap.SimpleEntry("limit", "" + limit));
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
                        bundle.putString(Constants.POSTS, response.get("data").getAsJsonArray().toString());
                        sHandler.sendMessage(sHandler.obtainMessage(Constants.GET_USER_POSTS, bundle));
                    } else {
                        sHandler.sendMessage(sHandler.obtainMessage(Constants.GET_NETWORK_STATE, Constants.NETWORK_STATE.NOT_LOGGED_IN));
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

        else if (msg.what == Constants.WRITE_POST) {
            Bundle bundle = (Bundle) msg.obj;
            Bitmap bitmap = null;
            if ( !bundle.getString( Constants.POST_IMG,"").equals("") ) {
                Uri imgUri = Uri.parse(bundle.getString(Constants.POST_IMG));
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(Storage.getContext().getContentResolver(),imgUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                URL url = new URL(Constants.URL + "CreatePost");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                try {
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);
                    connection.setDoInput(true);


                    List<AbstractMap.SimpleEntry> list = new ArrayList<AbstractMap.SimpleEntry>();
                    list.add(new AbstractMap.SimpleEntry<String, String>("content", bundle.getString(Constants.POST_TEXT)));
                    String base64 = "";
                    if (bitmap != null) {
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, stream);
                        byte[] byteArray = stream.toByteArray();
                        base64 = Base64.encodeToString(byteArray, Base64.DEFAULT);
                        list.add(new AbstractMap.SimpleEntry<String, String>("image", base64));
                    }
                    OutputStream os = connection.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(
                            new OutputStreamWriter(os, "ASCII"));
                    String out = Utils.getQuery(list);
                    writer.write(out);
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
                    if (!url.getHost().equals(connection.getURL().getHost())) {
                        // we were redirected! Kick the user out to the browser to sign on?
                        throw new Exception("Login to your internet provider");
                    }


                    writer.flush();
                    writer.close();
                    os.close();

                    connection.getHeaderFields();

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
        else if (msg.what == Constants.FOLLOW) {
            Bundle bundle = (Bundle) msg.obj;
            try {
                URL url = new URL(Constants.URL + "Follow");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                try {
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);
                    connection.setDoInput(true);

                    List<AbstractMap.SimpleEntry> list = new ArrayList<AbstractMap.SimpleEntry>();
                    list.add(new AbstractMap.SimpleEntry("uid", ( (User) bundle.getSerializable(Constants.USER_DATA)).uid ));

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
                        sHandler.sendMessage(sHandler.obtainMessage(Constants.FOLLOW, Constants.TRUE, 0, bundle));
                    } else {
                        if (response.get("message").equals("could not follow")) {
                            sHandler.sendMessage(sHandler.obtainMessage(Constants.FOLLOW, Constants.FALSE, 0, bundle));
                        }
                        else if (response.get("message").getAsString().equals("Already followed")) {
                            sHandler.sendMessage(sHandler.obtainMessage(Constants.FOLLOW, Constants.ALREADY_FOLLOWED, 0, bundle));
                        }
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
        else if (msg.what == Constants.UNFOLLOW) {
            Bundle bundle = (Bundle) msg.obj;
            try {
                URL url = new URL(Constants.URL + "Unfollow");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                try {
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);
                    connection.setDoInput(true);

                    List<AbstractMap.SimpleEntry> list = new ArrayList<AbstractMap.SimpleEntry>();
                    list.add(new AbstractMap.SimpleEntry("uid", ( (User) bundle.getSerializable(Constants.USER_DATA)).uid ));

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
                        sHandler.sendMessage(sHandler.obtainMessage(Constants.UNFOLLOW, Constants.TRUE, 0, bundle));
                    } else {
                        if (response.get("message").equals("could not unfollow")) {
                            sHandler.sendMessage(sHandler.obtainMessage(Constants.UNFOLLOW, Constants.FALSE, 0, bundle));
                        }
                        else if (response.get("message").getAsString().equals("user not followed")) {
                            sHandler.sendMessage(sHandler.obtainMessage(Constants.UNFOLLOW, Constants.NOT_FOLLOWED, 0, bundle));
                        }
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

