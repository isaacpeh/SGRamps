package com.example.sgramps;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.constraintlayout.widget.Constraints;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.sgramps.models.UserDAO;
import com.example.sgramps.models.UserModel;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.common.primitives.Chars;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EditProfileFragment extends Fragment {
    String[] gender = {"Male", "Female"};
    AutoCompleteTextView autoCompleteTextView;
    ArrayAdapter<String> arrayAdapter;
    TextView txtEmail, txtName, editDob;
    ImageView imgProfile;
    String email;
    TextInputEditText editEmail, editName, editPassword;
    Button btnSave;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        txtEmail = view.findViewById(R.id.txtEmail2);
        txtName = view.findViewById(R.id.txtName2);
        imgProfile = view.findViewById(R.id.imgProfilePic2);
        editEmail = view.findViewById(R.id.editEmail);
        editName = view.findViewById(R.id.editName);
        editPassword = view.findViewById(R.id.editPassword);
        editDob = view.findViewById(R.id.editDob);
        btnSave = view.findViewById(R.id.btnSave);

        email = "isaac@gmail.com";
        getUser(email);

        autoCompleteTextView = view.findViewById(R.id.ddlGender);
        arrayAdapter = new ArrayAdapter<>(getActivity(), R.layout.dropdown_item, gender);
        autoCompleteTextView.setAdapter(arrayAdapter);

        // This callback will only be called when MyFragment is at least Started.
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                if (MainActivity.active == MainActivity.fragmentEdit) {
                    getParentFragmentManager().beginTransaction().hide(MainActivity.active).show(MainActivity.fragmentProfile).commit();
                    getParentFragmentManager().beginTransaction().detach(MainActivity.fragmentEdit).commit();
                    MainActivity.active = MainActivity.fragmentProfile;
                } else {
                    requireActivity().finish();
                }
            }
        };

        autoCompleteTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(autoCompleteTextView.getText().toString())) {
                    arrayAdapter.getFilter().filter(null);
                }
            }
        });

        editDob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePicker();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveProfile();
            }
        });

        requireActivity().getOnBackPressedDispatcher().addCallback(getActivity(), callback);
        return view;
    }

    public void getUser(String email) {
        UserDAO userDb = new UserDAO();
        userDb.getUser(email, new UserDAO.UserCallback() {
            @Override
            public void onCallBack(UserModel user) {
                txtName.setText(user.getName());
                txtEmail.setText(user.getEmail());
                Picasso.get().load(user.getImg_url()).into(imgProfile);
                editEmail.setText(user.getEmail());
                editName.setText(user.getName());
                editPassword.setText(user.getPassword());
                editDob.setText(user.getDob().replace("/", "."));
                if (user.getGender().equalsIgnoreCase("Male")) {
                    autoCompleteTextView.setText(gender[0], false);
                } else {
                    autoCompleteTextView.setText(gender[1], false);
                }

            }
        });
    }

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

        datePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Long>() {
            @Override
            public void onPositiveButtonClick(Long selection) {
                // set constraints such as date over the current date and etc.
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
                editDob.setText(sdf.format(selection));
            }
        });
    }

    public void saveProfile() {
        CharSequence name = editName.getText();
        CharSequence dob = editDob.getText();
        CharSequence gender = autoCompleteTextView.getText();
        CharSequence password = editPassword.getText();
        Log.d("test", " " + name + dob + gender + password + email);
    }
}