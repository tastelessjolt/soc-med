package me.harshithgoka.socmed;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.TextInputEditText;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;
import static android.support.v4.content.FileProvider.getUriForFile;
import static me.harshithgoka.socmed.Constants.POSTS_TYPE.FEED;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MainFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
/**
 * A placeholder fragment containing a simple view.
 */
public class MainFragment extends CommonFragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */

    public static final String TAG = MainFragment.class.getName();

    private static JsonArray feed;

    ImageView addImageButton;
    ImageView destImage;
    Uri imageUri;
    TextInputEditText editText;
    ProgressBar progressBar;
    TextView postButtonText;

    public MainFragment() {

    }

    public JsonArray getFeed() {
        return feed;
    }

    public void setData(Bundle bundle) {
        int offset = bundle.getInt(Constants.OFFSET);
        JsonParser jsonParser = new JsonParser();
        feed = jsonParser.parse(bundle.getString(Constants.POSTS)).getAsJsonArray();
        if (adapter != null) {
            adapter.addToDataset(feed, offset);
        }
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static MainFragment newInstance(int sectionNumber, Context context) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);

        Intent intent = new Intent(context, NetworkService.class);
        intent.putExtra(Constants.WHAT, Constants.GET_FEED);
        context.startService(intent);

        return fragment;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler);
        if (adapter == null)
            adapter = new PostAdapter(getContext(), MainFragment.feed, FEED);

        destImage = rootView.findViewById(R.id.image_view);

        addImageButton = rootView.findViewById(R.id.add_image);
        addImageButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto, 0);
            }
        });

        linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);
        recyclerView.addOnScrollListener(new RecyclerViewScrollListener());

//        recyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(linearLayoutManager) {
//            @Override
//            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
//                adapter.loadMore(totalItemsCount);
//            }
//        });


        RelativeLayout postButton = (RelativeLayout) rootView.findViewById(R.id.post_button);
        editText = (TextInputEditText) rootView.findViewById(R.id.write_post);
        progressBar = (ProgressBar) rootView.findViewById(R.id.write_post_progress);
        postButtonText = (TextView) rootView.findViewById(R.id.post_button_text);


        postButton.setOnClickListener(new SendPostOnClickListener(editText, progressBar, postButtonText, destImage));

        swipeRefreshLayout = rootView.findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d(TAG, "Refresh received");

                adapter.refreshDataset();
            }
        });

        return rootView;
    }

    public class SendPostOnClickListener implements OnClickListener {
        TextInputEditText editText;
        ProgressBar progressBar;
        TextView textView;
        ImageView imageView;
        SendPostOnClickListener(TextInputEditText editText, ProgressBar progressBar, TextView textView, ImageView imageView) {
            this.editText = editText;
            this.progressBar = progressBar;
            this.textView = textView;
            this.imageView = imageView;
        }

        @Override
        public void onClick(View view) {
            String postText = editText.getText().toString();
            postText = postText.trim();
            if( !postText.equals("") || imageUri != null ) {
                Intent intent = new Intent(getContext(), NetworkService.class);
                intent.putExtra(Constants.WHAT, Constants.WRITE_POST);

                Bundle bundle = new Bundle();
                bundle.putString(Constants.POST_TEXT, postText);
                if (imageUri != null) {
                    bundle.putSerializable(Constants.POST_IMG, imageUri.toString());
                }

                intent.putExtra(Constants.INTENT_DATA, bundle);
                getContext().startService(intent);
                progressBar.setVisibility(View.VISIBLE);
                textView.setVisibility(View.GONE);
            }

        }
    }

    public void WritePost(boolean success) {
        progressBar.setVisibility(View.GONE);
        postButtonText.setVisibility(View.VISIBLE);

        if (success) {
            editText.setText("");
            imageUri = null;
            destImage.setImageResource(0);
            Toast.makeText(getContext(), "Post Successful, Please refresh to see your post.", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(getContext(), "Post unsuccessful. Please contact us on our website.", Toast.LENGTH_LONG).show();
        }
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
//        Log.d(TAG, "inSampleSize - " + inSampleSize + " " + width + " " + height + " " + reqWidth + " " + reqHeight);
        return inSampleSize;
    }

    public Bitmap decodeSampledBitmapFile(Uri uri, int reqWidth, int reqHeight) {

        try {
            InputStream ims = getContext().getContentResolver().openInputStream(uri);
            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(ims, null, options);

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            ims = getContext().getContentResolver().openInputStream(uri);
            return BitmapFactory.decodeStream(ims, null, options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch(requestCode) {
            case 0:
                if(resultCode == RESULT_OK){
                    Uri selectedImage = imageReturnedIntent.getData();
                    destImage.setImageBitmap(decodeSampledBitmapFile(selectedImage, destImage.getRootView().getMeasuredWidth(), 0));
                    imageUri = selectedImage;
                }
                break;
        }
    }

    class RecyclerViewScrollListener extends RecyclerView.OnScrollListener {

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            Log.d(TAG, "D Y - " + dy);

//            if (dy > 0 && linearLayoutManager != null && adapter != null) {
//                int totalNum = linearLayoutManager.getItemCount();
//                int currNum = linearLayoutManager.getChildCount();
//                int currFirstPos = linearLayoutManager.findFirstVisibleItemPosition();
//
//                if ( (currFirstPos + currNum >= totalNum) && !adapter.loading ) {
//                    adapter.loading = true;
//                    ( (PostAdapter) recyclerView.getAdapter() ).loadMore(totalNum);
//                }
//
//            }
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

            if (linearLayoutManager != null && adapter != null) {
                int totalNum = linearLayoutManager.getItemCount();
                int currNum = linearLayoutManager.getChildCount();
                int currFirstPos = linearLayoutManager.findFirstVisibleItemPosition();

                if ( (currFirstPos + currNum >= totalNum) && !adapter.loading ) {
                    adapter.loading = true;
                    ( (PostAdapter) recyclerView.getAdapter() ).loadMore(totalNum);
                }

            }

            Log.d(TAG, "ScrollState - " + newState);
            switch (newState) {
                case RecyclerView.SCROLL_STATE_SETTLING:
                    Log.d(TAG, "ScrollState - Settling");
                    break;

                case RecyclerView.SCROLL_STATE_IDLE:
                    Log.d(TAG, "ScrollState - Idle");
                    break;

                case RecyclerView.SCROLL_STATE_DRAGGING:
                    Log.d(TAG, "ScrollState - Dragging");
                    break;
            }
        }
    }


}
