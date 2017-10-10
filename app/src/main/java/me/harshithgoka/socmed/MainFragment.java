package me.harshithgoka.socmed;

import android.content.Context;
import android.content.Intent;
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

    ImageView addImageButton;
    ImageView destImage;
    Uri imageUri;

    public JsonArray getFeed() {
        return feed;
    }

    public void setData(Bundle bundle) {
        int offset = bundle.getInt(Constants.OFFSET);
        Gson gson = new Gson();
        Type listType = new TypeToken<List<Post>>() {}.getType();

        List<Post> posts = gson.fromJson(bundle.getString(Constants.POSTS), listType);
        Collections.sort(posts, new Comparator<Post>() {
            @Override
            public int compare(Post post, Post t1) {
                return post.timestamp.compareTo(t1.timestamp);
            }
        });

//            for (Post post:
//                 posts) {
//                System.out.println(post.timestamp);
//            }

        JsonParser jsonParser = new JsonParser();
        feed = jsonParser.parse(gson.toJson(posts)).getAsJsonArray();
        if (adapter != null) {
            adapter.addToDataset(feed, offset);
        }
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private static JsonArray feed;


    TextInputEditText editText;
    ProgressBar progressBar;
    TextView postButtonText;

    public MainFragment() {

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

    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch(requestCode) {
            case 0:
                if(resultCode == RESULT_OK){
                    if (Objects.equals(imageReturnedIntent.getAction(), MediaStore.ACTION_IMAGE_CAPTURE)) {
                        Uri uri = getCaptureImageOutputUri();
                        destImage.setImageURI(uri);
                        imageUri = uri;
                    }
                    else {
                        Uri selectedImage = imageReturnedIntent.getData();
                        destImage.setImageURI(selectedImage);
                        imageUri = selectedImage;
                    }
                }
                break;
        }
    }

    /**
     * Get URI to image received from capture by camera.
     */
    private Uri getCaptureImageOutputUri() {
        Uri outputFileUri = null;

        File getImage = getContext().getFilesDir();

        if (getImage != null) {
            File imagePath = new File(getImage, "images");
            File newFile = new File(imagePath, "temp.jpg");
            outputFileUri = getUriForFile(getContext(), "me.harshithgoka.socmed", newFile);


        }
        return outputFileUri;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler);
        if (adapter == null)
            adapter = new PostAdapter(getContext(), MainFragment.feed, FEED);
        addImageButton = rootView.findViewById(R.id.add_image);
        destImage = rootView.findViewById(R.id.image_view);

        addImageButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

//                Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                takePicture.putExtra(MediaStore.EXTRA_OUTPUT, getCaptureImageOutputUri());
//                takePicture.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
//
//                Intent chooserIntent = Intent.createChooser(takePicture, "Choose between Camera and Gallery");
//                chooserIntent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { pickPhoto });


                startActivityForResult(pickPhoto, 0);
            }
        });

        recyclerView.setAdapter(adapter);
        linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);

        recyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                adapter.loadMore(totalItemsCount);
            }
        });


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

//        adapter.refreshDataset();
        Log.d(TAG, "Which Tab? " + getArguments().getInt(ARG_SECTION_NUMBER, -1) + "");
//            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
        return rootView;
    }
}
