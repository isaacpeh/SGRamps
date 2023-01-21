package com.example.sgramps;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class ProfileFragment extends Fragment implements View.OnClickListener {

    String email;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        Button btnEdit = view.findViewById(R.id.btnEdit);
        Button btnContributions = view.findViewById(R.id.btnContributions);
        Button btnLogoff = view.findViewById(R.id.btnLogoff);

        btnEdit.setOnClickListener(this);
        btnContributions.setOnClickListener(this);
        btnLogoff.setOnClickListener(this);

        email = "isaac@gmail.com";
        getUser(email);

        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnEdit:
                Log.d("test", "edit");
                break;
            case R.id.btnContributions:
                Log.d("test", "contribute");
                break;
            case R.id.btnLogoff:
                Log.d("test", "logoff");
                break;
        }
    }

    public void getUser(String email) {

    }
}