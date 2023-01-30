package com.example.sgramps;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.sgramps.models.UserDAO;
import com.example.sgramps.models.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.auth.User;

import java.util.Calendar;

public class register_page extends Fragment {

    String[] genderItems = {"Male", "Female"};
    AutoCompleteTextView gender;
    ArrayAdapter<String> adapterItems;
    TextInputEditText displayName, email, password, dateOfBirth, number;
    Button registerButton;
    TextView loginButton;
    FirebaseAuth fAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_register_page, container, false);

        // Initialize all fields
        gender = view.findViewById(R.id.genderDropDownInput);
        adapterItems = new ArrayAdapter<String>(getActivity(), R.layout.dropdown_item, genderItems);
        gender.setAdapter(adapterItems);
        gender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(gender.getText().toString())) {
                    adapterItems.getFilter().filter(null);
                }
            }
        });

        dateOfBirth = view.findViewById(R.id.dateOfBirthInput);
        dateOfBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePicker();
            }
        });

        displayName = view.findViewById(R.id.displayNameInput);
        email = view.findViewById(R.id.emailInput);
        password = view.findViewById(R.id.passwordInput);
        registerButton = view.findViewById(R.id.registerButton);
        loginButton = view.findViewById(R.id.loginButton);
        number = view.findViewById(R.id.phoneNumberInput);

        fAuth = FirebaseAuth.getInstance();
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String parsedEmail = email.getText().toString();
                String parsedPassword = password.getText().toString();
                String parsedDisplayName = displayName.getText().toString();
                String parsedDateOfBirth = dateOfBirth.getText().toString();
                String parsedGender = gender.getText().toString();
                String parsedNumber = number.getText().toString();

                // Check if all fields are filled
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
                } else if (TextUtils.isEmpty(parsedGender)) {
                    gender.setError("Gender is required");
                    gender.requestFocus();
                } else {
                    // Creating user (Fire Authentication)
                    fAuth.createUserWithEmailAndPassword(parsedEmail, parsedPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                String img_url = "https://firebasestorage.googleapis.com/v0/b/sgramps.appspot.com/o/" +
                                        "profileImages%2Fdefault_profile_pic.png?alt=media&token=0bedb541-f42e-4a40-99c7-d119dd8da84f";
                                UserModel user = new UserModel(parsedDisplayName, parsedEmail, parsedPassword, img_url, parsedGender, parsedDateOfBirth, parsedNumber);
                                createUser(user);
                            } else {
                                Toast.makeText(getActivity(), "" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        // Login
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

    // Choose date of birth
    public void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        long upTo = calendar.getTimeInMillis();

        CalendarConstraints.Builder constraints = new CalendarConstraints.Builder()
                .setValidator(DateValidatorPointBackward.before(upTo))
                .setEnd(upTo);

        MaterialDatePicker<Long> datePicker = MaterialDatePicker
                .Builder
                .datePicker()
                .setCalendarConstraints(constraints.build())
                .setTitleText("Select date of birth")
                .build();

        datePicker.show(getActivity().getSupportFragmentManager(), "DATE_PICKER");
        // get the value of the date and set it to the text field
        datePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Long>() {
            @Override
            public void onPositiveButtonClick(Long selection) {
                dateOfBirth.setText(datePicker.getHeaderText());
            }
        });
    }

    // Create user (Firestore)
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