package me.harshithgoka.socmed;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.JsonObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

import static me.harshithgoka.socmed.Storage.getContext;

/**
 * Created by harshithgoka on 09/10/17.
 */

public class DiskCache {

    static int getImage(String fileid, PostAdapter.ViewHolder holder) {
        File cacheDir = Storage.getContext().getCacheDir();
        File file = new File(cacheDir, "img" + fileid + ".png");
        if (file.exists()) {
            if (holder != null)
                holder.setImageUri(Uri.fromFile(file));
            return 0;
        }
        else {
            ImageAsyncTask asyncTask = new ImageAsyncTask(holder);
            asyncTask.execute(fileid);
            return 1;
        }
    }


    static class ImageAsyncTask extends AsyncTask<String, Void, Boolean> {

        public static final String TAG = ImageAsyncTask.class.getName();

        PostAdapter.ViewHolder holder;
        Uri uri;
        ImageAsyncTask (PostAdapter.ViewHolder holder) {
            this.holder = holder;
        }

        @Override
        protected Boolean doInBackground(String... strings) {

            boolean b = false;

            if (strings[0] != null) {

                try {
                    URL url = new URL(Constants.URL + "Image");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                    try {
                        connection.setRequestMethod("POST");
                        connection.setDoOutput(true);
                        connection.setDoInput(true);

                        List<AbstractMap.SimpleEntry> list = new ArrayList<AbstractMap.SimpleEntry>();
                        list.add(new AbstractMap.SimpleEntry("id", strings[0]));

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


                        Bitmap response = BitmapFactory.decodeStream(connection.getInputStream());
                        File cacheDir = Storage.getContext().getCacheDir();
                        if (response != null) {
                            // make a new bitmap from your file
                            File file = new File(cacheDir, "img" + strings[0] + ".png");

                            FileOutputStream outStream = new FileOutputStream(file);
                            response.compress(Bitmap.CompressFormat.PNG, 100, outStream);
                            outStream.flush();
                            outStream.close();

                            uri = Uri.fromFile(file);
                            b = true;
                        }



                    } catch (Exception e) {
                        Log.d(TAG, e.toString());
                    }


                } catch (MalformedURLException e) {
                    Log.d(TAG, e.toString());
                } catch (IOException e) {
                    Log.d(TAG, e.toString());
                }

            }

            return b;
        }

        @Override
        protected void onPostExecute(Boolean b) {
            if (b) {
                if (holder != null && uri != null)
                    holder.setImageUri(uri);
            }
        }
    }

}
