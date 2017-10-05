package me.harshithgoka.socmed;

import android.app.Application;
import android.content.res.AssetManager;
import android.graphics.Typeface;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.Locale;

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
