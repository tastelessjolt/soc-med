package me.harshithgoka.socmed;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.EditText;

import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Created by harshithgoka on 03/10/17.
 */

public class MyCookieStore implements CookieStore {

    /*
    * In memory storage of cookies
    * */
    private Map<URI, List<HttpCookie>> mapCookies = new HashMap<URI, List<HttpCookie>>();

    /*
    * Instance of shared prefs
    * */
    private final SharedPreferences spePreferences;

    public MyCookieStore(Context context) {
        spePreferences = context.getSharedPreferences("CookiePrefFile", 0);
        Map <String, ?> all = spePreferences.getAll();

        for (Map.Entry<String, ?> entry : all.entrySet()) {
            for (String strCookie :  (HashSet<String>) entry.getValue() ) {
                if (!mapCookies.containsKey(entry.getKey())) {
                    List<HttpCookie> listCookies = new ArrayList<>();
                    listCookies.addAll(HttpCookie.parse(strCookie));

                    try {
                        mapCookies.put(new URI(entry.getKey()), listCookies);
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    List<HttpCookie> listCookies = mapCookies.get(entry.getKey());
                    listCookies.addAll(HttpCookie.parse(strCookie));

                    try {
                        mapCookies.put(new URI(entry.getKey()), listCookies);
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                }

                System.out.println(entry.getKey() + ": " + strCookie);
            }
        }

    }

    URI getAuthorityUri(URI uri) {
        return URI.create(uri.getScheme() + "://" + uri.getAuthority());
    }

    @Override
    public void add(URI uri, HttpCookie httpCookie) {
        uri = getAuthorityUri(uri);
        List<HttpCookie> cookies = mapCookies.get(uri);
        if (cookies == null) {
            cookies = new ArrayList<HttpCookie>();
            mapCookies.put(uri, cookies);
        }

        cookies.add(httpCookie);

        SharedPreferences.Editor editor = spePreferences.edit();
        HashSet<String> setCookies = new HashSet<>();
        setCookies.add(httpCookie.toString());
        editor.putStringSet(uri.toString(), spePreferences.getStringSet(uri.toString(), setCookies));
        editor.commit();
    }

    @Override
    public List<HttpCookie> get(URI uri) {
        uri = getAuthorityUri(uri);
        List<HttpCookie> cookies = mapCookies.get(uri);

        if (cookies == null)
            mapCookies.put(uri, new ArrayList<HttpCookie>());

        return mapCookies.get(uri);
    }

    @Override
    public List<HttpCookie> getCookies() {
        Collection<List<HttpCookie>> allCookies = mapCookies.values();
        List<HttpCookie> listCookies = new ArrayList<>();
        for (List<HttpCookie> cookies : allCookies)
            listCookies.addAll(cookies);
        return listCookies;
    }

    @Override
    public List<URI> getURIs() {
        return new ArrayList<>(mapCookies.keySet());
    }

    @Override
    public boolean remove(URI uri, HttpCookie httpCookie) {
        uri = getAuthorityUri(uri);
        List<HttpCookie> cookies = mapCookies.get(uri);

        if (cookies == null)
            return false;

        return cookies.remove(httpCookie);
    }

    @Override
    public boolean removeAll() {
        mapCookies.clear();
        return true;
    }
}
