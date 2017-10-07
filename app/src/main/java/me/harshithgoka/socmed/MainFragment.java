package me.harshithgoka.socmed;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;


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
    private static final String ARG_SECTION_NUMBER = "section_number";

    public JsonArray getFeed() {
        return feed;
    }

    public void setData(JsonArray feed) {
        MainFragment.feed = feed;
        if (adapter != null) {
            adapter.changeDataset(feed);
        }
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private static JsonArray feed;

    RecyclerView recyclerView;
    PostAdapter adapter;

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
        SendPostOnClickListener(TextInputEditText editText, ProgressBar progressBar, TextView textView) {
            this.editText = editText;
            this.progressBar = progressBar;
            this.textView = textView;
        }

        @Override
        public void onClick(View view) {
            String postText = editText.getText().toString();
            postText = postText.trim();
            if( !postText.equals("") ) {
                Intent intent = new Intent(getContext(), NetworkService.class);
                intent.putExtra(Constants.WHAT, Constants.WRITE_POST);
                intent.putExtra(Constants.INTENT_DATA, postText);
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
            Toast.makeText(getContext(), "Post Successful", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(getContext(), "Post unsuccessful. Please contact us on our website.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler);
        adapter = new PostAdapter(getContext(), MainFragment.feed, Constants.POSTS_TYPE.FEED);

        recyclerView.setAdapter(adapter);
        linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);

        RelativeLayout postButton = (RelativeLayout) rootView.findViewById(R.id.post_button);
        editText = (TextInputEditText) rootView.findViewById(R.id.write_post);
        progressBar = (ProgressBar) rootView.findViewById(R.id.write_post_progress);
        postButtonText = (TextView) rootView.findViewById(R.id.post_button_text);


        postButton.setOnClickListener(new SendPostOnClickListener(editText, progressBar, postButtonText));

        swipeRefreshLayout = rootView.findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d(TAG, "Refresh received");

                adapter.refreshDataset();
            }
        });

//        adapter.refreshDataset();
        Log.d(TAG, getArguments().getInt(ARG_SECTION_NUMBER, -1) + "");
//            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
        return rootView;
    }
}
