package com.example.sgramps;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sgramps.models.RampsDAO;
import com.example.sgramps.models.RampsModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.GeoPoint;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.TooManyListenersException;

public class UploadFragment extends Fragment implements AddImagesItemAdapter.ItemClickListener {
    AddImagesItemAdapter adapter;
    RecyclerView mRecyclerView;
    ArrayList<Uri> imagesSource = new ArrayList<>();
    View view;
    LocationRequest locationRequest;
    LocationCallback locationCallback;
    ActivityResultLauncher<PickVisualMediaRequest> pickMedia;
    Button btnLocation, btnCreate;
    TextInputEditText txtName, txtLat, txtLong, txtDes;
    String email;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TransitionInflater inflater = TransitionInflater.from(requireContext());
        setEnterTransition(inflater.inflateTransition(R.transition.fade));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        email = user.getEmail();

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_upload, container, false);
        txtLat = view.findViewById(R.id.latInput);
        txtLong = view.findViewById(R.id.longInput);
        txtName = view.findViewById(R.id.titleInput);
        txtDes = view.findViewById(R.id.desInput);
        btnLocation = view.findViewById(R.id.currentLocationBtn);
        btnCreate = view.findViewById(R.id.uploadRampBtn);

        Uri uri = Uri.parse("android.resource://com.example.sgramps/drawable/camerabtn");

        imagesSource = new ArrayList<>();
        imagesSource.add(uri);
        showRecyclerView(imagesSource);

        pickMedia =
                registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), imgUri -> {
                    // Callback is invoked after the user selects a media item or closes the
                    // photo picker.
                    if (imgUri != null) {
                        Log.d("PhotoPicker", "Selected URI: " + imgUri);
                        imagesSource.add(imgUri);
                        adapter.notifyItemInserted(imagesSource.size() - 1);
                    } else {
                        Log.d("PhotoPicker", "No media selected");
                    }
                });

        btnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnLocation.setEnabled(false);
                FusedLocationProviderClient fusedLocationClient;
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
                getLocation(fusedLocationClient);
            }
        });

        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnCreate.setEnabled(false);
                createRamp();
            }
        });

        return view;
    }

    public void showRecyclerView(ArrayList<Uri> data) {
        mRecyclerView = view.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        adapter = new AddImagesItemAdapter(getActivity(), data);
        adapter.setClickListener(this);
        mRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            requireActivity().getSupportFragmentManager().setFragmentResultListener("requestLatlng", this, new FragmentResultListener() {
                @Override
                public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle bundle) {
                    double[] result = bundle.getDoubleArray("latlng");
                    txtLat.setText(String.valueOf(result[0]));
                    txtLong.setText(String.valueOf(result[1]));
                }
            });
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        //adapter.getItem(position)
        if (position == 0) {
            BottomSheetDialog dialog = new BottomSheetDialog(getActivity());
            View bottomSheetView = getLayoutInflater().inflate(R.layout.import_image_popup, null);
            dialog.setContentView(bottomSheetView);
            TextView uploadCamera = dialog.findViewById(R.id.uploadCamera);
            TextView uploadLibrary = dialog.findViewById(R.id.uploadLibrary);

            uploadCamera.setOnClickListener(new View.OnClickListener() {
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

    private void getLocation(FusedLocationProviderClient fusedLocationClient) {
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 100)
                .setWaitForAccurateLocation(true)
                .setMinUpdateIntervalMillis(3000)
                .setMaxUpdateDelayMillis(100)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) {
                    Toast.makeText(getActivity(), "Unable to get location", Toast.LENGTH_SHORT).show();
                    btnLocation.setEnabled(true);
                    return;
                } else {
                    for (Location location : locationResult.getLocations()) {
                        if (location != null) {
                            LatLng latLng = null;
                            Double lat = location.getLatitude();
                            Double lng = location.getLongitude();
                            Log.d("LOG", "USER LATITUDE: " + lat + " - USER LONGITUDE: " + lng);
                            fusedLocationClient.removeLocationUpdates(locationCallback);
                            txtLat.setText(lat.toString());
                            txtLong.setText(lng.toString());
                        }
                    }
                    btnLocation.setEnabled(true);
                }
            }
        };

        // check for permissions
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            btnLocation.setEnabled(true);
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, 44);
        } else {
            Toast.makeText(getActivity(), "Getting location...", Toast.LENGTH_SHORT).show();
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }

    }

    private void createRamp() {
        try {
            RampsDAO dbRamps = new RampsDAO();
            String rampName = txtName.getText().toString().trim();
            String rampDescription = txtDes.getText().toString().trim();
            GeoPoint rampLocation = new GeoPoint(Double.parseDouble(txtLat.getText().toString().trim()), Double.parseDouble(txtLong.getText().toString().trim()));
            Timestamp created_at = Timestamp.now();
            String uploader = email;
            List<String> imgs = new ArrayList<>();

            if (rampName.length() == 0 || rampDescription.length() == 0) {
                throw new NumberFormatException("empty String");
            }

            for (int i = 1; i < imagesSource.size(); i++) {
                imgs.add(imagesSource.get(i).toString());
            }

            if (imgs.size() == 0) {
                Toast.makeText(getActivity(), "Please add at least 1 photo", Toast.LENGTH_SHORT).show();
                btnCreate.setEnabled(true);
                return;
            }

            RampsModel ramp = new RampsModel(rampName, rampDescription, created_at, uploader, imgs, true, rampLocation);
            dbRamps.addRamp(ramp, new RampsDAO.CreateCallback() {
                @Override
                public void onCallBack(String result) {
                    if (result.contains("exists")) {
                        Toast.makeText(getActivity(), "Ramp already exists, please choose another name", Toast.LENGTH_SHORT).show();
                    } else if (result.contains("Failed")) {
                        Toast.makeText(getActivity(), "Failed to upload ramp", Toast.LENGTH_SHORT).show();
                    } else if (result.contains("Successfully")) {
                        Toast.makeText(getActivity(), "Ramp uploaded successfully!", Toast.LENGTH_SHORT).show();

                        Bundle res = new Bundle();
                        res.putString("ramp", rampName);
                        getParentFragmentManager().setFragmentResult("requestKey", res);
                        getParentFragmentManager().beginTransaction().hide(MainActivity.active).show(MainActivity.fragmentHome).commit();
                        BottomNavigationView mBottomNavigationView = getActivity().findViewById(R.id.bottom_navigation_view);
                        mBottomNavigationView.setSelectedItemId(R.id.home_page);
                        reset();
                    }
                }
            });

        } catch (NumberFormatException ex) {
            btnCreate.setEnabled(true);
            Log.d("LOG", "ERROR: " + ex);
            if (ex.getMessage().contains("empty String")) {
                Toast.makeText(getActivity(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "Error uploading ramp", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception ex) {
            btnCreate.setEnabled(true);
            Log.d("LOG", "ERROR: " + ex);
            if (ex.getMessage().contains("Longitude") || ex.getMessage().contains("Latitude")) {
                Toast.makeText(getActivity(), "Invalid location", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "Error uploading ramp", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void reset() {
        btnCreate.setEnabled(true);
        txtDes.setText("");
        txtName.setText("");
        txtLong.setText("");
        txtLat.setText("");
        imagesSource = new ArrayList<>();
        Uri uri = Uri.parse("android.resource://com.example.sgramps/drawable/camerabtn");
        imagesSource.add(uri);
        showRecyclerView(imagesSource);
    }
}