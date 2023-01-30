package com.example.sgramps;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
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
                closeKeyboard();
                view.clearFocus();
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
                                Intent intent = new Intent(getActivity(), MainActivity.class);
                                startActivity(intent);
                                getActivity().finish();
                            } else {
                                if (task.getException().getMessage().contains("no user record") ||
                                        task.getException().getMessage().contains("password is invalid")) {
                                    Toast.makeText(getActivity(), "Email or Password is incorrect", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getActivity(), "Error occurred logging in", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                }
            }
        });


        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeKeyboard();
                view.clearFocus();
                Fragment fragment = new register_page();
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frame_layout, fragment)
                        .commit();
                StartUpActivity.active = StartUpActivity.fragmentRegister;
            }
        });

        // This callback will only be called when MyFragment is at least Started.
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                if (StartUpActivity.active == StartUpActivity.fragmentLogin) {
                    email.setText("");
                    password.setText("");
                    Fragment fragment = new start_up_page();
                    getParentFragmentManager()
                            .beginTransaction()
                            .replace(R.id.frame_layout, fragment)
                            .commit();
                    StartUpActivity.active = StartUpActivity.fragmentStartup;
                } else {
                    getActivity().finish();
                }
            }
        };

        requireActivity().

                getOnBackPressedDispatcher().

                addCallback(getActivity(), callback);
        return view;
    }

    // hide soft keyboard
    private void closeKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager manager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

}