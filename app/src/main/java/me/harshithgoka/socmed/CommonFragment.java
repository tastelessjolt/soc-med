package me.harshithgoka.socmed;


import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

/**
 * Created by harshithgoka on 05/10/17.
 */

public class CommonFragment extends Fragment {
    SwipeRefreshLayout swipeRefreshLayout;
    LinearLayoutManager linearLayoutManager;


    public void stopRefresh() {
        swipeRefreshLayout.setRefreshing(false);
    }

    public void WriteComment(boolean success, Bundle bundle) {
        View rootView = linearLayoutManager.findViewByPosition(bundle.getInt(Constants.POST_POS));
        ImageView imageView = (ImageView) rootView.findViewById(R.id.comment_img);
        ProgressBar progressBar = (ProgressBar) rootView.findViewById(R.id.comment_progress);
        TextInputEditText editText = (TextInputEditText) rootView.findViewById(R.id.write_comment);

        imageView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);

        if (success) {
            editText.setText("");
            Toast.makeText(getContext(), "Comment Successful", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(getContext(), "Comment unsuccessful. Please contact us on our website.", Toast.LENGTH_LONG).show();
        }
    }
}
