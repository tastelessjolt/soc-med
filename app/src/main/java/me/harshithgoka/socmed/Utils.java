package me.harshithgoka.socmed;

import android.util.Log;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.AbstractMap;
import java.util.List;

/**
 * Created by harshithgoka on 03/10/17.
 */

public class Utils {
    public static JsonParser jsonParser = new JsonParser();

    public static String getQuery(List<AbstractMap.SimpleEntry> params) throws UnsupportedEncodingException
    {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (AbstractMap.SimpleEntry pair : params)
        {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode((String) pair.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode((String) pair.getValue(), "UTF-8"));
        }

        return result.toString();
    }

    public static JsonObject getAndParse(InputStream inputStream) {
        StringBuilder stringBuilder = new StringBuilder();

        try (InputStream in = new BufferedInputStream(inputStream)) {
            int nbytes;
            byte[] bytes = new byte[1024];
            while ((nbytes = in.read(bytes, 0, 1024)) != -1) {
                stringBuilder.append(new String(bytes, 0, nbytes));
            }
        } catch (IOException e) {
            Log.d("PARSING", e.toString());
        }

        return jsonParser.parse(stringBuilder.toString()).getAsJsonObject();
    }
}
