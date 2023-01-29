package com.example.sgramps;

import android.os.Bundle;
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

public class login_page extends Fragment {

    TextInputEditText email, password;
    Button loginButton;
    TextView registerButton;
    FirebaseAuth fAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login_page, container, false);
        loginButton = view.findViewById(R.id.loginButton);
        registerButton = view.findViewById(R.id.registerButton);
        email = view.findViewById(R.id.emailInput);
        password = view.findViewById(R.id.passwordInput);

        fAuth = FirebaseAuth.getInstance();

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailString = email.getText().toString();
                String passwordString = password.getText().toString();
                if (emailString.isEmpty()) {
                    email.setError("Email is required");
                    email.requestFocus();
                } else if (passwordString.isEmpty()) {
                    password.setError("Password is required");
                    password.requestFocus();
                } else {
                    fAuth.signInWithEmailAndPassword(emailString, passwordString).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getActivity(), "Logged in successfully", Toast.LENGTH_SHORT).show();
                                getParentFragmentManager().beginTransaction().hide(MainActivity.active).show(MainActivity.fragmentHome).commit();
                                MainActivity.active = MainActivity.fragmentHome;
                            } else {
                                Toast.makeText(getActivity(), "Error!" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().beginTransaction().hide(MainActivity.active).show(MainActivity.fragmentRegister).commit();
                MainActivity.active = MainActivity.fragmentRegister;
            }
        });
        return view;
    }
}