package com.example.sgramps;

import android.net.Uri;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.sgramps.models.UserDAO;
import com.example.sgramps.models.UserModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.auth.User;
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
    TextInputEditText editNumber, editName, editPassword, editNewPassword;
    Button btnSave;
    ImageButton btnProfile;
    ActivityResultLauncher<PickVisualMediaRequest> pickMedia;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        txtEmail = view.findViewById(R.id.txtEmail2);
        txtName = view.findViewById(R.id.txtName2);
        imgProfile = view.findViewById(R.id.imgProfilePic2);
        editNumber = view.findViewById(R.id.editNumber);
        editName = view.findViewById(R.id.editName);
        editPassword = view.findViewById(R.id.editPassword);
        editNewPassword = view.findViewById(R.id.editNewPassword);
        editDob = view.findViewById(R.id.editDob);
        btnSave = view.findViewById(R.id.btnSave);
        btnProfile = view.findViewById(R.id.imgBtnProfile);

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
                    getParentFragmentManager().beginTransaction().detach(MainActivity.fragmentProfile).commit();
                    getParentFragmentManager().beginTransaction().attach(MainActivity.fragmentProfile).commit();
                    getParentFragmentManager().beginTransaction().hide(MainActivity.active).show(MainActivity.fragmentProfile).commit();
                    MainActivity.active = MainActivity.fragmentProfile;
                } else {
                    requireActivity().finish();
                }
            }
        };

        pickMedia =
                registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                    // Callback is invoked after the user selects a media item or closes the
                    // photo picker.
                    if (uri != null) {
                        Log.d("PhotoPicker", "Selected URI: " + uri);
                        imgProfile.setImageURI(uri);
                        imgProfile.setTag(uri);
                    } else {
                        Log.d("PhotoPicker", "No media selected");
                    }
                });

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

        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChooserDialog();
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
                imgProfile.setTag(user.getImg_url());
                editNumber.setText(user.getNumber());
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
        // check pw
        // check if need update pw
        // check if need change pic
        String name = editName.getText().toString();
        String number = editNumber.getText().toString();
        String dob = editDob.getText().toString();
        String gender = autoCompleteTextView.getText().toString();
        String new_password = editNewPassword.getText().toString();
        String imgUri = imgProfile.getTag().toString();
        UserModel user;

        if (new_password.trim().length() == 0) {
            user = new UserModel(name, email, imgUri, gender, dob, number);
        } else {
            user = new UserModel(name, email, new_password, imgUri, gender, dob, number);
        }

        UserDAO userDb = new UserDAO();
        userDb.uploadImage(user);
    }

    public void showChooserDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(getActivity());
        View bottomSheetView = getLayoutInflater().inflate(R.layout.cameragallery_popup, null);
        dialog.setContentView(bottomSheetView);

        TextView txtLibrary = dialog.findViewById(R.id.txtLibrary);
        txtLibrary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickMedia.launch(new PickVisualMediaRequest.Builder()
                        // Error but works
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build());
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}