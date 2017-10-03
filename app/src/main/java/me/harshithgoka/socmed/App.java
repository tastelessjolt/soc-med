package me.harshithgoka.socmed;

import android.app.Application;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;

/**
 * Created by harshithgoka on 03/10/17.
 */

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        CookieManager cookieManager = new CookieManager(new MyCookieStore(getApplicationContext()), CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);
    }
}
