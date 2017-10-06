package me.harshithgoka.socmed;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Created by harshithgoka on 06/10/17.
 */

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder>{


    public static final String TAG = CommentAdapter.class.getName();
    public static final int MAX_COMMENTS = 3;
    public boolean more;
    JsonArray comments;
    CommentAdapter (JsonArray data ) {
        comments = data;
        more = true;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.comment, parent, false);
        // set the view's size, margins, paddings and layout parameters

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (comments != null) {
            JsonObject object = (JsonObject) comments.get(position);
            ((TextView) holder.mLin.findViewById(R.id.comment_name)).setText(object.get("name").getAsString());
            ((TextView) holder.mLin.findViewById(R.id.comment_text)).setText(object.get("text").getAsString());
            ((TextView) holder.mLin.findViewById(R.id.comment_timestamp)).setText(Utils.convertTimestamp(object.get("timestamp").getAsString()));
        }
        else {

        }
    }

    @Override
    public int getItemCount() {
        if (comments != null) {
            if (more)
                return Math.min (comments.size(), MAX_COMMENTS);
            else
                return comments.size();
        }
        else
            return 0;

    }

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


}
