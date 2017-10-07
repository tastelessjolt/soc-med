package me.harshithgoka.socmed;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by harshithgoka on 05/10/17.
 */

public class Storage {
    private static String name;
    private static Context context;

    public static void Init(Context context) {
        if (context != null) {
            Storage.context = context;
            SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.USERSTATE, 0);
            name = sharedPreferences.getString(Constants.NAME, "");
        }

    }

    public static void setName(String name) {
        Storage.name = name;
        if (context != null) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.USERSTATE, 0);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(Constants.NAME, name);
            editor.commit();
        }
    }
    public static String getName () {
        return name;
    }
}

