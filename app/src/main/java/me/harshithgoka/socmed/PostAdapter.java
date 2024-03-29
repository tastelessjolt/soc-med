package me.harshithgoka.socmed;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by harshithgoka on 05/10/17.
 */

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    public static final String TAG = PostAdapter.class.getName();

    public String[] dummy_dataset;



    public void setData(JsonArray dataset) {
        if (dataset != null) {
            data = null;
            addToDataset(dataset, 0);
        }
    }

    public JsonArray data = null;
    Context context;
    Constants.POSTS_TYPE type;
    User user;
    ProgressBar imageLoading;


    // / Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public RecyclerView recyclerView;
        public CommentAdapter commentAdapter;
        public LinearLayout mLin;
        public boolean more;
        public Uri imageUri;
        public ViewHolder(LinearLayout v) {
            super(v);
            mLin = v;
            more = true;
            imageUri = null;
        }

        public int calculateInSampleSize(
                BitmapFactory.Options options, int reqWidth, int reqHeight) {
            // Raw height and width of image
            final int height = options.outHeight;
            final int width = options.outWidth;
            int inSampleSize = 1;

            if (height > reqHeight/2 || width > reqWidth/2) {

                final int halfHeight = height / 2;
                final int halfWidth = width / 2;

                // Calculate the largest inSampleSize value that is a power of 2 and keeps both
                // height and width larger than the requested height and width.
                while ((halfWidth / inSampleSize) >= reqWidth / 2) {
                    inSampleSize *= 2;
                }
            }

            Log.d(TAG, "inSampleSize - " + inSampleSize + " " + width + " " + height + " " + reqWidth + " " + reqHeight);

            return inSampleSize;
        }

        public Bitmap decodeSampledBitmapFile(File file, int reqWidth, int reqHeight) {

            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(file.getAbsolutePath(), options);
//        BitmapFactory.decodeResource(res, resId, options);

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        }


        public void setImageUri(Uri imageUri) {
            this.imageUri = imageUri;
            ImageView imageView;
            mLin.findViewById(R.id.image_loading).setVisibility(View.GONE);

            if (mLin != null && (imageView = mLin.findViewById(R.id.image)) != null ) {

                try {
//                Log.d(TAG,  "MaxWidth = "  + imageView.getMaxWidth() + " or " + imageView.getRootView().getMeasuredWidth());
                    imageView.setImageBitmap(decodeSampledBitmapFile(new File(imageUri.getPath()), imageView.getRootView().getMeasuredWidth(), 0));
                }
                catch (Exception e) {
                    imageView.setImageURI(imageUri);
                }
            }
        }
    }

    public void addToDataset(JsonArray dataset, int offset) {

        if (dataset == null){
            return;
        }

        Gson gson = new Gson();
        Type listType = new TypeToken<List<Post>>() {}.getType();

        List<Post> posts = gson.fromJson(dataset, listType);
        if (type == Constants.POSTS_TYPE.FEED) {
            Collections.sort(posts, new Comparator<Post>() {
                @Override
                public int compare(Post post, Post t1) {
                    return post.timestamp.compareTo(t1.timestamp);
                }
            });
        }
        else {
            Collections.sort(posts, new Comparator<Post>() {
                @Override
                public int compare(Post post, Post t1) {
                    return -post.timestamp.compareTo(t1.timestamp);
                }
            });
        }
        JsonParser jsonParser = new JsonParser();
        dataset = jsonParser.parse(gson.toJson(posts)).getAsJsonArray();

//        Log.d(TAG, type.toString());
        if (data != null) {
            JsonArray firstPart = new JsonArray();
            for (int i = 0; i != offset; i ++)
                firstPart.add(data.remove(0));


            for (int i = 0; i != dataset.size(); i++) {
                if (data.size() > 0)
                    data.remove(0);
                else
                    break;
            }

            for (int i = 0; i != dataset.size(); i++)
                firstPart.add(dataset.get(i));

            for (int i = 0; i != data.size(); i++)
                firstPart.add(data.get(i));

            data = firstPart;
        }
        else {
            data = new JsonArray();
            for (int i = 0; i != dataset.size(); i++)
                data.add(dataset.get(i));
        }
        notifyDataSetChanged();
    }

//    public void addDataToDataset (JsonArray extraData) {
//        data.addAll(extraData);
//        notifyDataSetChanged();
//    }

    public PostAdapter(Context context, JsonArray jsonElements, Constants.POSTS_TYPE userPosts, User user) {
        this.type = userPosts;
        this.context = context;
        setData(jsonElements);
        this.user = user;
    }

    PostAdapter(Context context, JsonArray argdata, Constants.POSTS_TYPE type) {
        this.type = type;
        this.context = context;
        setData(argdata);

        dummy_dataset = new String[]{"Zhang", "Hey! What's up? Ya dawg.",
                "Dude", "I'm good dawg.",
                "Zhang", "Noice! This is just me wanting to write a big passage to fill up the space, I know I could use lorem ipsum but I don't understand Latin or whatever language that is in. So bear with me. :P"};

    }

    public void loadMore(int offset) {
        Intent intent = new Intent(context, NetworkService.class);
        switch (type) {
            case FEED:
                intent.putExtra(Constants.WHAT, Constants.GET_FEED);
                break;
            case MY_POSTS:
                intent.putExtra(Constants.WHAT, Constants.GET_MY_POSTS);
                break;
            case USER_POSTS:
                Bundle bundle = new Bundle();
                bundle.putSerializable(Constants.USER_DATA, user);
                intent.putExtra(Constants.WHAT, Constants.GET_USER_POSTS);
                intent.putExtra(Constants.INTENT_DATA, bundle);
                break;
        }
        intent.putExtra(Constants.OFFSET, offset);
        intent.putExtra(Constants.LIMIT, Constants.MAX_LIMIT);
        context.startService(intent);
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
                Bundle bundle = new Bundle();
                bundle.putSerializable(Constants.USER_DATA, user);
                intent.putExtra(Constants.WHAT, Constants.GET_USER_POSTS);
                intent.putExtra(Constants.INTENT_DATA, bundle);
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

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        if (data == null) {
            ((TextView) holder.mLin.findViewById(R.id.post_name)).setText(dummy_dataset[position * 2]);
            ((TextView) holder.mLin.findViewById(R.id.post_text)).setText(dummy_dataset[position * 2 + 1]);
            ((TextView) holder.mLin.findViewById(R.id.timestamp)).setText("3h ago");
        }
        else {
            JsonObject object = (JsonObject) data.get(position);
            ((TextView) holder.mLin.findViewById(R.id.post_name)).setText(object.get("name").getAsString());
            JsonElement text;
            ((TextView) holder.mLin.findViewById(R.id.post_text)).setText( ((text = object.get("text")) != null) ? text.getAsString() : "");
            ((TextView) holder.mLin.findViewById(R.id.timestamp)).setText(Utils.convertTimestamp(object.get("timestamp").getAsString()));

            JsonElement imageid;
            ImageView image = holder.mLin.findViewById(R.id.image);
            if((imageid = object.get("imageid")) != null) {
                image.setImageResource(0);
                holder.mLin.findViewById(R.id.image_loading).setVisibility(View.VISIBLE);
                DiskCache.getImage(imageid.getAsString(), holder);
            }
            else {
                holder.mLin.findViewById(R.id.image_loading).setVisibility(View.GONE);
                image.setImageResource(0);
            }


            JsonArray comments = object.getAsJsonArray("Comment");

            LinearLayout linearLayout = holder.mLin.findViewById(R.id.comments);

            holder.recyclerView = holder.mLin.findViewById(R.id.comments_recycler);
            holder.commentAdapter = new CommentAdapter(object, comments);
            holder.commentAdapter.setMore(holder.more);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
            holder.recyclerView.setLayoutManager(linearLayoutManager);
            holder.recyclerView.setAdapter(holder.commentAdapter);
            Button button = holder.mLin.findViewById(R.id.more);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.commentAdapter.setMore(false);
                    holder.more = false;
                    view.setVisibility(View.GONE);
                }
            });

            RelativeLayout commentButton = (RelativeLayout) holder.mLin.findViewById(R.id.post_button);
            ImageView imageView = (ImageView) holder.mLin.findViewById(R.id.comment_img);
            ProgressBar progressBar = (ProgressBar) holder.mLin.findViewById(R.id.comment_progress);
            TextInputEditText editText = (TextInputEditText) holder.mLin.findViewById(R.id.write_comment);

            commentButton.setOnClickListener(new CommentClickListener(position, object.get("postid").getAsInt(), imageView, progressBar, editText));

//            Log.d(TAG, "Comments size: " + comments.size() + " More: " + holder.commentAdapter.more);

            if (comments.size() > 3 && holder.commentAdapter.more) {
                button.setVisibility(View.VISIBLE);
            }
            else {
                button.setVisibility(View.GONE);
            }

            if (comments.size() == 0) {
                ( (TextView) holder.mLin.findViewById(R.id.comments_text)).setText("No comments yet");
            }
            else {
                ( (TextView) holder.mLin.findViewById(R.id.comments_text)).setText("Comments");
            }
        }
    }

    public class CommentClickListener implements View.OnClickListener {
        int postid;
        int position;
        ImageView imageView;
        ProgressBar progressBar;
        TextInputEditText editText;


        CommentClickListener(int position, int postid, ImageView imageView, ProgressBar progressBar, TextInputEditText editText){
            this.position = position;
            this.postid = postid;
            this.imageView = imageView;
            this.progressBar = progressBar;
            this.editText = editText;
        }

        @SuppressLint("ResourceType")
        @Override
        public void onClick(View view) {
            String comment = editText.getText().toString();
            comment = comment.trim();
            if (!comment.equals("")) {
                imageView.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);

                Intent intent = new Intent(context, NetworkService.class);
                intent.putExtra(Constants.WHAT, Constants.WRITE_COMMENT);

                Bundle bundle = new Bundle();
                bundle.putSerializable(Constants.SRC_FRAGMENT, type);
                bundle.putInt(Constants.POST_ID, postid);
                bundle.putString(Constants.COMMENT_TEXT, comment);
                bundle.putInt(Constants.POST_POS, position);

                intent.putExtra(Constants.INTENT_DATA, bundle);

                context.startService(intent);
            }
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