package me.harshithgoka.socmed;


import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;

/**
 * Created by harshithgoka on 05/10/17.
 */

public class CommonFragment extends Fragment {
    SwipeRefreshLayout swipeRefreshLayout;

    public void stopRefresh() {
        swipeRefreshLayout.setRefreshing(false);
    }
}
