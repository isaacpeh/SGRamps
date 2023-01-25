package com.example.sgramps;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sgramps.adapters.RecyclerTilesItemAdapter;
import com.example.sgramps.models.UserDAO;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class BookmarksFragment extends Fragment implements RecyclerTilesItemAdapter.ItemClickListener {

    private GridLayoutManager mGridLayoutManager;
    RecyclerTilesItemAdapter adapter;
    RecyclerView mRecyclerView;
    View view;
    String email;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // logged in user
        email = "isaac@gmail.com";

        // initialize view
        view = inflater.inflate(R.layout.fragment_bookmarks, container, false);
        TextView txtTitle = view.findViewById(R.id.textView);
        // can check if came from where?
        if (MainActivity.active == MainActivity.fragmentContribution) {
            txtTitle.setText("Contributions");
            fetchContributions();
        } else {
            txtTitle.setText("Bookmarks");
            fetchBookmarks();
        }

        // This callback will only be called when MyFragment is at least Started.
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                if (MainActivity.active == MainActivity.fragmentContribution) {
                    getParentFragmentManager().beginTransaction().detach(MainActivity.fragmentProfile).commit();
                    getParentFragmentManager().beginTransaction().attach(MainActivity.fragmentProfile).commit();
                    getParentFragmentManager().beginTransaction().hide(MainActivity.active).show(MainActivity.fragmentProfile).commit();
                    MainActivity.active = MainActivity.fragmentProfile;
                } else {
                    requireActivity().finish();
                }
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getActivity(), callback);

        return view;
    }

    @Override
    public void onItemClick(View view, int position) {
        //Toast.makeText(getActivity(), "You clicked " + adapter.getItem(position) + " on item number " + position, Toast.LENGTH_SHORT).show();
        // pass selected ramp to homeFragment
        Bundle result = new Bundle();
        result.putString("ramp", adapter.getItem(position));
        getParentFragmentManager().setFragmentResult("requestKey", result);
        getParentFragmentManager().beginTransaction().hide(MainActivity.active).show(MainActivity.fragmentHome).commit();
        BottomNavigationView mBottomNavigationView = getActivity().findViewById(R.id.bottom_navigation_view);
        mBottomNavigationView.setSelectedItemId(R.id.home_page);
    }

    private void showRecyclerView(List<String> data) {
        //Reference of RecyclerView
        mRecyclerView = view.findViewById(R.id.recyclerViewTiles);
        //Linear Layout Manager
        mGridLayoutManager = new GridLayoutManager(getActivity(), 2);
        //Set Layout Manager to RecyclerView
        mRecyclerView.setLayoutManager(mGridLayoutManager);
        adapter = new RecyclerTilesItemAdapter(getActivity(), data);
        //Set adapter to RecyclerView
        adapter.setClickListener(this);
        mRecyclerView.setAdapter(adapter);
    }

    private void fetchBookmarks() {
        UserDAO userDb = new UserDAO();
        userDb.getBookmark(email, new UserDAO.BookmarkCallback() {
            @Override
            public void onCallBack(List<String> bookmarks) {
                showRecyclerView(bookmarks);
            }
        });
    }

    private void fetchContributions() {
        UserDAO userDb = new UserDAO();
        userDb.getContribution(email, new UserDAO.BookmarkCallback() {
            @Override
            public void onCallBack(List<String> contributions) {
                showRecyclerView(contributions);
            }
        });
    }
}
