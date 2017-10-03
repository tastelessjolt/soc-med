package me.harshithgoka.socmed;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.AbstractMap;
import java.util.List;

/**
 * Created by harshithgoka on 03/10/17.
 */

public class Utils {
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
}
