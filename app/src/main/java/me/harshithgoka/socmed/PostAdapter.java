package me.harshithgoka.socmed;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.IdRes;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.Inflater;

/**
 * Created by harshithgoka on 05/10/17.
 */

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    public static final String TAG = PostAdapter.class.getName();

    public String[] dummy_dataset;

    public void setData(JsonArray data) {
        this.data = data;
    }

    public JsonArray data = null;
    Context context;
    Constants.POSTS_TYPE type;

    // / Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public LinearLayout mLin;
        public ViewHolder(LinearLayout v) {
            super(v);
            mLin = v;
        }
    }

    public String convertTimestamp (String strtimestamp) {
        String strTimestamp = strtimestamp.split("\\.")[0];
        Log.d(TAG, strTimestamp);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = format.parse(strTimestamp);
            SimpleDateFormat myFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            return myFormat.format(date);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            Log.d(TAG, e.toString());
            return "Random";
        }

    }

    public void changeDataset(JsonArray dataset) {
        data = dataset;
        notifyDataSetChanged();
    }

    PostAdapter(Context context, JsonArray argdata, Constants.POSTS_TYPE type) {
        this.type = type;
        this.context = context;
        data = argdata;
        dummy_dataset = new String[]{"Zhang", "Hey! What's up? Ya dawg.",
                "Dude", "I'm good dawg.",
                "Zhang", "Noice! This is just me wanting to write a big passage to fill up the space, I know I could use lorem ipsum but I don't understand Latin or whatever language that is in. So bear with me. :P"};

    }

    public void refreshDataset() {
        Intent intent = new Intent(context, NetworkService.class);
        switch (type) {
            case FEED:
                intent.putExtra(Constants.WHAT, Constants.GET_FEED);
                break;
            case MY_POSTS:
                intent.putExtra(Constants.WHAT, Constants.GET_MY_POSTS);
                break;
            case USER_POSTS:
                intent.putExtra(Constants.WHAT, Constants.GET_FEED);
                break;
        }
        context.startService(intent);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.post, parent, false);
        // set the view's size, margins, paddings and layout parameters

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @SuppressLint("ResourceType")
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (data == null) {
            ((TextView) holder.mLin.findViewById(R.id.post_name)).setText(dummy_dataset[position * 2]);
            ((TextView) holder.mLin.findViewById(R.id.post_text)).setText(dummy_dataset[position * 2 + 1]);
            ((TextView) holder.mLin.findViewById(R.id.timestamp)).setText("3hrs ago");
        }
        else {
            JsonObject object = (JsonObject) data.get(position);
            ((TextView) holder.mLin.findViewById(R.id.post_name)).setText(object.get("name").getAsString());
            ((TextView) holder.mLin.findViewById(R.id.post_text)).setText(object.get("text").getAsString());
            ((TextView) holder.mLin.findViewById(R.id.timestamp)).setText(convertTimestamp(object.get("timestamp").getAsString()));

            JsonArray comments = object.getAsJsonArray("Comment");
            LinearLayout linearLayout = (LinearLayout) holder.mLin.findViewById(R.id.comments);
            int nchild = linearLayout.getChildCount();
            for (int i = 1; i < nchild; i++) {
                linearLayout.removeViewAt(i);
            }

            if (comments.size() == 0) {
                ( (TextView) linearLayout.findViewById(R.id.comments_text)).setText("No comments yet");
            }
            else {
                Log.d(TAG, "Comment here " + object.toString());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                int len = Math.min(comments.size(), 3);
                for (int i = 0; i != len; i++) {
                    LinearLayout linearLayout1 = (LinearLayout) ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.comment, linearLayout, false);

                    ((TextView) linearLayout1.findViewById(R.id.comment_name)).setText(comments.get(i).getAsJsonObject().get("name").getAsString());
                    ((TextView) linearLayout1.findViewById(R.id.comment_text)).setText(comments.get(i).getAsJsonObject().get("text").getAsString());
                    ((TextView) linearLayout1.findViewById(R.id.comment_timestamp)).setText(convertTimestamp(comments.get(i).getAsJsonObject().get("timestamp").getAsString()));
                    linearLayout.addView(linearLayout1, params);
                }
                if (len < comments.size()) {
                    Button button = new Button(context);
                    button.setId(9399);
                    button.setText("More");
                    button.setOnClickListener(new MoreClickListener(comments, linearLayout));
                    linearLayout.addView(button);
                }
            }
        }
    }

    public class MoreClickListener implements View.OnClickListener {
        JsonArray comments;
        LinearLayout linearLayout;
        MoreClickListener(JsonArray comments, LinearLayout linearLayout){
            this.comments = comments;
            this.linearLayout = linearLayout;
        }

        @SuppressLint("ResourceType")
        @Override
        public void onClick(View view) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            for (int i = 3; i < comments.size(); i++) {
                LinearLayout linearLayout1 = (LinearLayout) ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.comment, linearLayout, false);

                ((TextView) linearLayout1.findViewById(R.id.comment_name)).setText(comments.get(i).getAsJsonObject().get("name").getAsString());
                ((TextView) linearLayout1.findViewById(R.id.comment_text)).setText(comments.get(i).getAsJsonObject().get("text").getAsString());
                ((TextView) linearLayout1.findViewById(R.id.comment_timestamp)).setText(convertTimestamp(comments.get(i).getAsJsonObject().get("timestamp").getAsString()));
                linearLayout.addView(linearLayout1, params);
            }
            linearLayout.removeView(linearLayout.findViewById(9399));
        }
    }

    @Override
    public int getItemCount() {
        if (data != null)
            return data.size();
        else
            return dummy_dataset.length/2;
    }


}