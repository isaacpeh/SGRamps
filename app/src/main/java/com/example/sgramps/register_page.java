package com.example.sgramps;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link register_page#newInstance} factory method to
 * create an instance of this fragment.
 */
public class register_page extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    TextInputEditText displayName, email, password, dateOfBirth, gender;
    Button registerButton;
    Button loginButton;
    FirebaseAuth fAuth;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public register_page() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment register_page.
     */
    // TODO: Rename and change types and number of parameters
    public static register_page newInstance(String param1, String param2) {
        register_page fragment = new register_page();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_register_page, container, false);
        displayName = view.findViewById(R.id.displayNameInput);
        email = view.findViewById(R.id.emailInput);
        password = view.findViewById(R.id.passwordInput);
        dateOfBirth = view.findViewById(R.id.dateOfBirthInput);

        fAuth = FirebaseAuth.getInstance();

        registerButton = view.findViewById(R.id.registerButton);
        loginButton = view.findViewById(R.id.loginButton);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String parsedEmail = email.getText().toString();
                String parsedPassword = password.getText().toString();
                String parsedDisplayName = displayName.getText().toString();
                String parsedDateOfBirth = dateOfBirth.getText().toString();

                if (TextUtils.isEmpty(parsedEmail)) {
                    email.setError("Email is required");
                    email.requestFocus();
                } else if (TextUtils.isEmpty(parsedPassword)) {
                    password.setError("Password is required");
                    password.requestFocus();
                } else if (TextUtils.isEmpty(parsedDisplayName)) {
                    displayName.setError("Display name is required");
                    displayName.requestFocus();
                } else if (TextUtils.isEmpty(parsedDateOfBirth)) {
                    dateOfBirth.setError("Date of birth is required");
                    dateOfBirth.requestFocus();
                } else {
                    fAuth.createUserWithEmailAndPassword(parsedEmail, parsedPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getActivity(), "User registered successfully", Toast.LENGTH_SHORT).show();
                                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                                fragmentManager.beginTransaction().replace(R.id.fragmentScreen, new login_page()).commit();
                            } else {
                                Toast.makeText(getActivity(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }


            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.fragmentScreen, new login_page()).commit();
            }
        });
        return view;
    }
}