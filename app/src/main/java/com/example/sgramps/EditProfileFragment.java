package com.example.sgramps;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.transition.TransitionInflater;
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
import android.widget.Toast;

import com.example.sgramps.models.UserDAO;
import com.example.sgramps.models.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import javax.crypto.SecretKey;

public class EditProfileFragment extends Fragment {
    String[] gender = {"Male", "Female"};
    private final String ALIAS_KEY = "sgramplogin";
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TransitionInflater inflater = TransitionInflater.from(requireContext());
        setEnterTransition(inflater.inflateTransition(R.transition.slide_right));
    }

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

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        email = user.getEmail();
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

        // gender drop down
        autoCompleteTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(autoCompleteTextView.getText().toString())) {
                    arrayAdapter.getFilter().filter(null);
                }
            }
        });

        // dob calender
        editDob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePicker();
            }
        });

        // save button
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveProfile();
            }
        });

        // profile image button
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
        String current_password = editPassword.getText().toString();
        if (current_password.trim().length() == 0) {
            Toast.makeText(getContext(), "Enter password to update", Toast.LENGTH_SHORT).show();
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(email, current_password);
        FirebaseUser loggedInUser = FirebaseAuth.getInstance().getCurrentUser();
        loggedInUser.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) { // successfully checked password
                            String name = editName.getText().toString();
                            String number = editNumber.getText().toString();
                            String dob = editDob.getText().toString();
                            String gender = autoCompleteTextView.getText().toString();
                            String new_password = editNewPassword.getText().toString();
                            String imgUri = imgProfile.getTag().toString();
                            UserModel user;

                            if (name.trim().length() == 0 || number.trim().length() == 0) {
                                Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            if (new_password.trim().length() == 0) {
                                user = new UserModel(name, email, imgUri, gender, dob, number);
                            } else {
                                loggedInUser.updatePassword(new_password);
                                user = new UserModel(name, email, new_password, imgUri, gender, dob, number);

                            }

                            UserDAO userDb = new UserDAO();
                            userDb.uploadImage(user);
                            Toast.makeText(getContext(), "Successfully updated", Toast.LENGTH_SHORT).show();

                            KeyStore keyStore = null;
                            try {
                                keyStore = KeyStore.getInstance("AndroidKeyStore");
                                keyStore.load(null);
                                keyStore.deleteEntry(ALIAS_KEY);
                            } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException e) {
                                e.printStackTrace();
                            }

                            editPassword.setText("");
                            editNewPassword.setText("");
                        } else {
                            Toast.makeText(getContext(), "Password is incorrect", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void showChooserDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(getActivity());
        View bottomSheetView = getLayoutInflater().inflate(R.layout.cameragallery_popup, null);
        dialog.setContentView(bottomSheetView);

        TextView txtLibrary = dialog.findViewById(R.id.txtLibrary);
        TextView txtRemove = dialog.findViewById(R.id.txtRemove);
        TextView txtCamera = dialog.findViewById(R.id.txtCamera);

        txtCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, 43);
                } else {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, 1);
                }
                dialog.dismiss();
            }
        });

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

        txtRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://firebasestorage.googleapis.com/v0/b/sgramps.appspot.com/o/" +
                        "profileImages%2Fdefault_profile_pic.png?alt=media&token=0bedb541-f42e-4a40-99c7-d119dd8da84f";
                Picasso.get()
                        .load(url)
                        .into(imgProfile);
                imgProfile.setTag(url);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            Uri uri = getImageUri(getActivity(), photo);
            imgProfile.setImageURI(uri);
            imgProfile.setTag(uri);
        }
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }
}