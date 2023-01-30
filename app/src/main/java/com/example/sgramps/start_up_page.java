package com.example.sgramps;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import org.imaginativeworld.whynotimagecarousel.ImageCarousel;
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem;

import java.util.ArrayList;
import java.util.List;

public class start_up_page extends Fragment {
    ImageCarousel imageCarousel;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_start_up_page, container, false);
        Button loginButton = view.findViewById(R.id.loginButton);
        Button registerButton = view.findViewById(R.id.registerButton);
        ImageCarousel imageCarousel = view.findViewById(R.id.startPageCarousel);
        imageCarousel.registerLifecycle(getLifecycle());

        List<CarouselItem> list = new ArrayList<>();

        list.add (
                new CarouselItem(
                        "./drawable/placeholder.png"
                )
        );
        list.add (
                new CarouselItem(
                        "./drawable/placeholder.png"
                )
        );
        list.add (
                new CarouselItem(
                        "./drawable/placeholder.png"
                )
        );
        imageCarousel.setData(list);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = new login_page();
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frame_layout, fragment)
                        .commit();

                StartUpActivity.active = StartUpActivity.fragmentLogin;
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = new register_page();
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frame_layout, fragment)
                        .commit();
                StartUpActivity.active = StartUpActivity.fragmentRegister;
            }
        });
        return view;
    }

}