package com.example.sgramps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {
    public static Fragment fragmentHome = new HomeFragment();
    public static Fragment fragmentBookmark = new BookmarksFragment();
    public static Fragment fragmentProfile = new ProfileFragment();
    public static Fragment fragmentEdit = new EditProfileFragment();
    FragmentManager fm = getSupportFragmentManager();
    public static Fragment active;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        NavigationBarView bottomNav = findViewById(R.id.bottom_navigation_view);

        active = fragmentHome;
        fm.beginTransaction().add(R.id.frame_layout, fragmentHome, "1")
                .hide(fragmentBookmark)
                .hide(fragmentProfile)
                .hide(fragmentEdit)
                .commit();

        fm.beginTransaction().add(R.id.frame_layout, fragmentBookmark, "2").commit();
        fm.beginTransaction().add(R.id.frame_layout, fragmentProfile, "5").commit();
        fm.beginTransaction().add(R.id.frame_layout, fragmentEdit, "5").commit();

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
                        /*fm.beginTransaction().hide(active).show(fragmentProfile).commit();
                        active = fragmentProfile;*/

                        fm.beginTransaction().hide(active).show(fragmentEdit).commit();
                        active = fragmentEdit;
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