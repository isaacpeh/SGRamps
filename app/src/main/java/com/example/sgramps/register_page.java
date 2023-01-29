package com.example.sgramps;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class register_page extends Fragment {

    TextInputEditText displayName, email, password, dateOfBirth, gender;
    Button registerButton;
    TextView loginButton;
    FirebaseAuth fAuth;

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
                                Toast.makeText(getActivity(), "Registered successfully", Toast.LENGTH_SHORT).show();
                                getParentFragmentManager().beginTransaction().hide(MainActivity.active).show(MainActivity.fragmentLogin).commit();
                                MainActivity.active = MainActivity.fragmentLogin;
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
                getParentFragmentManager().beginTransaction().hide(MainActivity.active).show(MainActivity.fragmentLogin).commit();
                MainActivity.active = MainActivity.fragmentLogin;
            }
        });
        return view;
    }
}