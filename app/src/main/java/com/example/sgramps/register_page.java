package com.example.sgramps;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;

public class register_page extends Fragment {

    String[] genderItems = {"Male", "Female"};
    AutoCompleteTextView gender;
    ArrayAdapter<String> adapterItems;
    TextInputEditText displayName, email, password, dateOfBirth;
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

        fAuth = FirebaseAuth.getInstance();
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String parsedEmail = email.getText().toString();
                String parsedPassword = password.getText().toString();
                String parsedDisplayName = displayName.getText().toString();
                String parsedDateOfBirth = dateOfBirth.getText().toString();
                String parsedGender = gender.getText().toString();

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

        // Login
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().beginTransaction().hide(MainActivity.active).show(MainActivity.fragmentLogin).commit();
                MainActivity.active = MainActivity.fragmentLogin;
            }
        });
        return view;
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
}