package com.example.sgramps;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
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

import com.example.sgramps.models.UserDAO;
import com.example.sgramps.models.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.auth.User;

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
        //gender = view.findViewById(R.id.ddlGender);

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
                //String parsedGender = gender.getText().toString();

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
                                String img_url = "https://firebasestorage.googleapis.com/v0/b/sgramps.appspot.com/o/" +
                                        "profileImages%2Fdefault_profile_pic.png?alt=media&token=0bedb541-f42e-4a40-99c7-d119dd8da84f";
                                String number = "93834566";
                                String parsedGender = "Male";
                                UserModel user = new UserModel(parsedDisplayName, parsedEmail, parsedPassword, img_url, parsedGender, parsedDateOfBirth, number);
                                createUser(user);
                            } else {
                                Toast.makeText(getActivity(), "" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeKeyboard();
                Fragment fragment = new login_page();
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frame_layout, fragment)
                        .commit();
                StartUpActivity.active = StartUpActivity.fragmentLogin;
            }
        });

        // This callback will only be called when MyFragment is at least Started.
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                if (StartUpActivity.active == StartUpActivity.fragmentRegister) {
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
        requireActivity().getOnBackPressedDispatcher().addCallback(getActivity(), callback);
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


    private void createUser(UserModel user) {
        UserDAO userDB = new UserDAO();
        userDB.createUser(user, new UserDAO.CreateCallback() {
            @Override
            public void onCallBack(String result) {
                if (result.contains("exists")) {
                    Toast.makeText(getActivity(), "Email already taken", Toast.LENGTH_SHORT).show();
                } else if (result.contains("Failed")) {
                    Toast.makeText(getActivity(), "Failed to register. PLease try again.", Toast.LENGTH_SHORT).show();
                } else if (result.contains("Successfully")) {
                    Toast.makeText(getActivity(), "Registered successfully", Toast.LENGTH_SHORT).show();
                    Fragment fragment = new login_page();
                    getParentFragmentManager()
                            .beginTransaction()
                            .replace(R.id.frame_layout, fragment)
                            .commit();
                    StartUpActivity.active = StartUpActivity.fragmentLogin;
                }
            }
        });
    }
}