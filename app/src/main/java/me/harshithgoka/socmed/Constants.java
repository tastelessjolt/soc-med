package me.harshithgoka.socmed;

import android.app.Service;
import android.os.Handler;


/**
 * Created by harshithgoka on 03/10/17.
 */

public final class Constants {
    public static String URL = "http://10.4.65.62:8080/twitter-backend/";

    public static String MISCSTATE = "MiscState";
    public static String LOGINSTATE = "LoginState";

    public static String INTENT_DATA = "IntentData";
    public static String WHAT = "what";


    public static int ACK = 0;
    public static int GET_NETWORK_STATE = 1;
    public static int GET_FEED = 2;
    public static int GET_MY_POSTS = 3;
    public enum NETWORK_STATE {
        NETWORK_STATE, LOGGED_IN, NOT_LOGGED_IN, NOT_CONNECTED

    }

    public enum POSTS_TYPE {
        FEED, MY_POSTS, USER_POSTS
    }

    public static Handler currHandler = null;

}
