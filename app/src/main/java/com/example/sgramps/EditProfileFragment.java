package com.example.sgramps;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import java.util.ArrayList;

public class EditProfileFragment extends Fragment implements View.OnClickListener {
    String[] gender = {"Male", "Female"};
    AutoCompleteTextView autoCompleteTextView;
    ArrayAdapter<String> arrayAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        autoCompleteTextView = view.findViewById(R.id.ddlGender);
        arrayAdapter = new ArrayAdapter<>(getActivity(), R.layout.dropdown_item, gender);
        autoCompleteTextView.setAdapter(arrayAdapter);
        
        // This callback will only be called when MyFragment is at least Started.
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                if (MainActivity.active == MainActivity.fragmentEdit) {
                    getParentFragmentManager().beginTransaction().hide(MainActivity.active).commit();
                    getParentFragmentManager().beginTransaction().detach(MainActivity.fragmentEdit).commit();
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
    public void onClick(View view) {

    }


}