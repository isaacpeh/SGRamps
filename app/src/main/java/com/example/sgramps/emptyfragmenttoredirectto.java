package com.example.sgramps;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class emptyfragmenttoredirectto extends Fragment {

    TextView textView1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_emptyfragmenttoredirectto, container, false);
        // Change textview1 text
        textView1 = view.findViewById(R.id.textView1);
        FirebaseAuth fAuth = FirebaseAuth.getInstance();

        textView1.setText(fAuth.getCurrentUser().getEmail());
        return view;
    }
}