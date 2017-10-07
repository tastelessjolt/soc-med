package me.harshithgoka.socmed;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import org.w3c.dom.Text;

import java.lang.reflect.Type;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SearchFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchFragment extends CommonFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final String TAG = SearchFragment.class.getName();

    // TODO: Rename and change types of parameters
    private int sectionNo;

    private OnFragmentInteractionListener mListener;

    ProfileFragment profileFragment;

    public SearchFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param i Section Number.
     * @return A new instance of fragment SearchFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SearchFragment newInstance(int i) {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, i);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            sectionNo = getArguments().getInt(ARG_SECTION_NUMBER);
        }


    }

    Handler handler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);


        AutoCompleteTextView textView = rootView.findViewById(R.id.search_bar);
        SearchAdapter searchAdapter = new SearchAdapter(getContext());
        textView.setAdapter(searchAdapter);



        return rootView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {

        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void SetPosts(Bundle obj) {
        User user = (User) obj.getSerializable(Constants.USER_DATA);
        String postsStr = obj.getString(Constants.POSTS);

//        Gson gson = new Gson();
//        Type listType = new TypeToken<List<Post>>() {}.getType();

//        List<Post> posts = gson.fromJson(postsStr, listType);


        JsonParser jsonParser = new JsonParser();
        JsonArray jsonElements = jsonParser.parse(postsStr).getAsJsonArray();

        ( (TextView) getView().getRootView().findViewById(R.id.name)).setText(user.name);
        ( (TextView) getView().getRootView().findViewById(R.id.username)).setText("@(" + user.uid + ")");


        if (recyclerView == null) {
            recyclerView = getView().getRootView().findViewById(R.id.profile_recycler);

            swipeRefreshLayout = getView().getRootView().findViewById(R.id.profileswiperefresh);
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    if (adapter != null) {
                        adapter.refreshDataset();
                        swipeRefreshLayout.setRefreshing(true);
                    }
                }
            });



            adapter = new PostAdapter(getContext(), jsonElements, Constants.POSTS_TYPE.USER_POSTS, user);

            linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);

            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setHasFixedSize(true);
            recyclerView.setAdapter(adapter);
        }
        else {
            adapter.user = user;
            adapter.setData(jsonElements);
            adapter.notifyDataSetChanged();
            swipeRefreshLayout.setRefreshing(false);
        }
}

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
