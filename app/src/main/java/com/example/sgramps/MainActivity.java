package com.example.sgramps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.example.sgramps.models.RampsModel;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private ArrayList<RampsModel> rampsArrayList;
    Fragment fragmentHome = new HomeFragment();
    Fragment fragmentBookmark = new BookmarksFragment();
    ;
    FragmentManager fm = getSupportFragmentManager();
    Fragment active;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        NavigationBarView bottomNav = findViewById(R.id.bottom_navigation_view);

        active = fragmentHome;
        fm.beginTransaction().add(R.id.frame_layout, fragmentHome, "1").commit();
        fm.beginTransaction().add(R.id.frame_layout, fragmentBookmark, "2").hide(fragmentHome).commit();
        /*Fragment fragment = new HomeFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_layout, fragment)
                .commit();*/
        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home_page:
                        fm.beginTransaction().hide(active).show(fragmentHome).commit();
                        active = fragmentHome;
                        return true;
                    case R.id.bookmarks_page:
                        fm.beginTransaction().detach(fragmentBookmark).commit();
                        fm.beginTransaction().attach(fragmentBookmark).commit();
                        fm.beginTransaction().hide(active).show(fragmentBookmark).commit();
                        active = fragmentBookmark;
                        return true;
                    case R.id.upload_page:

                        return true;
                    case R.id.notification_page:

                        return true;
                    case R.id.profile_page:

                        return true;
                    default:
                        return false;
                }
            }
        });

        bottomNav.setOnItemReselectedListener(new NavigationBarView.OnItemReselectedListener() {
            @Override
            public void onNavigationItemReselected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home_page:
                        return;
                    case R.id.bookmarks_page:
                        return;
                    case R.id.upload_page:
                        return;
                    case R.id.notification_page:
                        return;
                    case R.id.profile_page:
                        return;
                }
            }
        });
    }
}