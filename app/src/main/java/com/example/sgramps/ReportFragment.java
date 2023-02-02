package com.example.sgramps;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sgramps.models.RampsDAO;
import com.example.sgramps.models.RampsModel;
import com.example.sgramps.models.ReportDAO;
import com.example.sgramps.models.ReportModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class ReportFragment extends Fragment implements AddImagesItemAdapter.ItemClickListener {
    AddImagesItemAdapter adapter;
    RecyclerView mRecyclerView;
    ArrayList<Uri> imagesSource = new ArrayList<>();
    View view;
    TextInputEditText desInput;
    Button btnReport;
    ActivityResultLauncher<PickVisualMediaRequest> pickMedia;
    String email, ramp_reported;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TransitionInflater inflater = TransitionInflater.from(requireContext());
        setEnterTransition(inflater.inflateTransition(R.transition.slide_up));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        email = user.getEmail();

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_report, container, false);
        desInput = view.findViewById(R.id.reportDesInput);
        btnReport = view.findViewById(R.id.submitReportBtn);
        Uri uri = Uri.parse("android.resource://com.example.sgramps/drawable/camerabtn");
        imagesSource = new ArrayList<>();
        imagesSource.add(uri);
        showRecyclerView(imagesSource);


        pickMedia =
                registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), imgUri -> {
                    // Callback is invoked after the user selects a media item or closes the
                    // photo picker.
                    if (uri != null) {
                        Log.d("PhotoPicker", "Selected URI: " + uri);
                        imagesSource.add(imgUri);
                        adapter.notifyItemInserted(imagesSource.size() - 1);
                    } else {
                        Log.d("PhotoPicker", "No media selected");
                    }
                });

        btnReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeKeyboard();
                desInput.clearFocus();
                btnReport.setEnabled(false);
                submitReport();
            }
        });

        return view;
    }

    public void submitReport() {
        ReportDAO dbReport = new ReportDAO();
        Long reported_at = Timestamp.now().getSeconds() + Timestamp.now().getNanoseconds();
        String reporter = email;
        String ramp_name = ramp_reported;
        String description = desInput.getText().toString();
        List<String> imgs = new ArrayList<>();

        if (description.trim().length() == 0) {
            Toast.makeText(getActivity(), "Please provide more information on how the ramp is inaccessible", Toast.LENGTH_SHORT).show();
            btnReport.setEnabled(true);
            return;
        }

        for (int i = 1; i < imagesSource.size(); i++) {
            imgs.add(imagesSource.get(i).toString());
        }

        if (imgs.size() == 0) {
            Toast.makeText(getActivity(), "Please add at least 1 photo", Toast.LENGTH_SHORT).show();
            btnReport.setEnabled(true);
            return;
        }

        ReportModel report = new ReportModel(reporter, description, ramp_name, reported_at, imgs);
        dbReport.uploadReportImages(report, new ReportDAO.UploadCallback() {
            @Override
            public void onCallback(String result) {
                if (result.contains("exists")) {
                    Toast.makeText(getActivity(), "Report ID already exists, please submit again", Toast.LENGTH_SHORT).show();
                } else if (result.contains("Failed")) {
                    Toast.makeText(getActivity(), "Failed to report ramp", Toast.LENGTH_SHORT).show();
                } else if (result.contains("Successfully")) {
                    Toast.makeText(getActivity(), "Ramp reported successfully!", Toast.LENGTH_SHORT).show();
                    getParentFragmentManager().beginTransaction().hide(MainActivity.active).show(MainActivity.fragmentHome).commit();
                    reset();
                }
            }
        });
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            requireActivity().getSupportFragmentManager().setFragmentResultListener("requestRampReport", this, new FragmentResultListener() {
                @Override
                public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle bundle) {
                    ramp_reported = bundle.getString("ramp_name_report");
                    desInput = view.findViewById(R.id.reportDesInput);
                    desInput.setHint("Provide more information on '" + ramp_reported + "' ramp");
                }
            });
        } else {
            try {
                reset();
            } catch (Exception e) {

            }

        }
    }

    // hide soft keyboard
    private void closeKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager manager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void showRecyclerView(ArrayList<Uri> data) {
        mRecyclerView = view.findViewById(R.id.recycler_view_report);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        adapter = new AddImagesItemAdapter(getActivity(), data);
        adapter.setClickListener(this);
        mRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(View view, int position) {
        desInput.clearFocus();
        if (position == 0) {
            BottomSheetDialog dialog = new BottomSheetDialog(getActivity());
            View bottomSheetView = getLayoutInflater().inflate(R.layout.import_image_popup, null);
            dialog.setContentView(bottomSheetView);
            TextView uploadCamera = dialog.findViewById(R.id.uploadCamera);
            TextView uploadLibrary = dialog.findViewById(R.id.uploadLibrary);

            uploadCamera.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, 1);
                    dialog.dismiss();
                }
            });

            uploadLibrary.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            Uri uri = getImageUri(getActivity(), photo);

            imagesSource.add(uri);
            adapter.notifyItemInserted(imagesSource.size() - 1);
        }
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public void reset() {
        btnReport.setEnabled(true);
        desInput.setText("");
        desInput.setHint("");
        Uri uri = Uri.parse("android.resource://com.example.sgramps/drawable/camerabtn");
        imagesSource = new ArrayList<>();
        imagesSource.add(uri);
        showRecyclerView(imagesSource);
    }
}