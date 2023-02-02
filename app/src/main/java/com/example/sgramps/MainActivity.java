package com.example.sgramps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    public static Fragment fragmentHome = new HomeFragment();
    public static Fragment fragmentBookmark = new BookmarksFragment();
    public static Fragment fragmentProfile = new ProfileFragment();
    public static Fragment fragmentEdit = new EditProfileFragment();
    public static Fragment fragmentContribution = new BookmarksFragment();
    public static Fragment fragmentUpload = new UploadFragment();
    public static Fragment fragmentReport = new ReportFragment();

    public static Fragment active;
    FragmentManager fm = getSupportFragmentManager();
    public FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        NavigationBarView bottomNav = findViewById(R.id.bottom_navigation_view);

        // AUTHENTICATION
        FirebaseAuth fAuth = FirebaseAuth.getInstance();
        user = fAuth.getCurrentUser();
        if (user == null) {
            Intent intent = new Intent(MainActivity.this, StartUpActivity.class);
            startActivity(intent);
            return;
        }

        active = fragmentHome;
        fm.beginTransaction().add(R.id.frame_layout, fragmentHome, "1")
                .hide(fragmentBookmark)
                .hide(fragmentProfile)
                .hide(fragmentContribution)
                .hide(fragmentUpload)
                .hide(fragmentEdit)
                .hide(fragmentReport)
                .commit();

        fm.beginTransaction().add(R.id.frame_layout, fragmentBookmark, "2").commit();
        fm.beginTransaction().add(R.id.frame_layout, fragmentUpload, "3").commit();
        fm.beginTransaction().add(R.id.frame_layout, fragmentProfile, "5").commit();
        fm.beginTransaction().add(R.id.frame_layout, fragmentEdit, "6").commit();
        fm.beginTransaction().add(R.id.frame_layout, fragmentContribution, "7").commit();
        fm.beginTransaction().add(R.id.frame_layout, fragmentReport, "8").commit();

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
                        fm.beginTransaction().hide(active).show(fragmentUpload).commit();
                        active = fragmentUpload;
                        return true;
                    case R.id.profile_page:
                        fm.beginTransaction().detach(fragmentProfile).commit();
                        fm.beginTransaction().attach(fragmentProfile).commit();
                        fm.beginTransaction().hide(active).show(fragmentProfile).commit();
                        active = fragmentProfile;
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
                    case R.id.profile_page:
                        return;
                }
            }
        });
    }
}