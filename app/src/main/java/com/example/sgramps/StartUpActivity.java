package com.example.sgramps;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;

public class StartUpActivity extends AppCompatActivity {
    public static Fragment fragmentLogin = new login_page();
    public static Fragment fragmentRegister = new register_page();
    public static Fragment fragmentStartup = new start_up_page();
    public static Fragment active;
    FragmentManager fm = getSupportFragmentManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_up);

        active = fragmentStartup;
        fm.beginTransaction().add(R.id.frame_layout, fragmentStartup, "1")
                .commit();
    }
}