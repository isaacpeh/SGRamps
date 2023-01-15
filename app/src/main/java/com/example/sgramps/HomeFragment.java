package com.example.sgramps;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import com.example.sgramps.adapters.PlacesAutoSuggestAdapter;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private GoogleMap map;
    private int btnToggle = 0;
    LocationRequest locationRequest;
    LocationCallback locationCallback;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // initialize view
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // initialize map fragment
        SupportMapFragment supportMapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.google_map);

        // initialize auto-complete
        AutoCompleteTextView autoCompleteTextView = view.findViewById(R.id.txtSearch);
        autoCompleteTextView.setAdapter(new PlacesAutoSuggestAdapter(getActivity(), android.R.layout.simple_list_item_1));

        // initialize current location
        FloatingActionButton btnLocate = view.findViewById(R.id.btnLocation);


        // async map
        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;

                // GET USER LOCATION FUNCTION
                btnLocate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        FusedLocationProviderClient fusedLocationClient;
                        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

                        if (btnToggle == 0){
                            btnToggle = 1;
                            locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 100)
                                    .setWaitForAccurateLocation(true)
                                    .setMinUpdateIntervalMillis(3000)
                                    .setMaxUpdateDelayMillis(100)
                                    .build();

                            locationCallback = new LocationCallback() {
                                @Override
                                public void onLocationResult(@NonNull LocationResult locationResult) {
                                    if (locationResult == null){
                                        return;
                                    } else {
                                        for (Location location : locationResult.getLocations()) {
                                            if (location != null) {
                                                Double lat = location.getLatitude();
                                                Double lng = location.getLongitude();
                                                Log.d("GPS", "Lat: " + lat + " | long: " + lng);
                                            }
                                        }
                                    }
                                }
                            };
                            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                                    ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION}, 44);
                            } else {
                                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
                            }
                        } else if (btnToggle == 1) {
                            btnToggle = 0;

                            try {
                                Task<Void> voidTask = fusedLocationClient.removeLocationUpdates(locationCallback);
                                voidTask.addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d("GPS", "stopMonitoring: removeLocationUpdates successful.");
                                        } else {
                                            Log.d("GPS", "stopMonitoring: removeLocationUpdates updates unsuccessful! " + voidTask.toString());
                                        }
                                    }
                                });
                            } catch (SecurityException exp) {
                                Log.d("GPS", "stopMonitoring: Security exception.");
                            }
                        }
                    }
                });

                // AUTO COMPLETE FUNCTION
                autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        closeKeyboard();
                        autoCompleteTextView.clearFocus();

                        Log.d("Address", autoCompleteTextView.getText().toString());
                        LatLng latlng = getlatLng(autoCompleteTextView.getText().toString());
                        if (latlng != null) {
                            Log.d("Lat Lng", " " + latlng.latitude + " " + latlng.longitude);
                            LatLng latLng = new LatLng(latlng.latitude, latlng.longitude);
                            moveMap(latLng);
                        } else {
                            Log.d("Lat Lng", "Lat Lng not found");
                        }
                    }
                });

                // MAP CLICKED FUNCTION
                googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(@NonNull LatLng latLng) {
                        // when clicked on map
                        closeKeyboard();
                        autoCompleteTextView.clearFocus();
                        moveMap(latLng);
                    }
                });
            }
        });
        return view;
    }

    // generate pin and move map
    private void moveMap(LatLng latlng){
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latlng);
        markerOptions.title(latlng.latitude + " : " + latlng.longitude);
        map.clear();
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 15));
        map.addMarker(markerOptions);
    }

    // get LatLng from selected address
    private LatLng getlatLng(String address) {
        Geocoder geocoder = new Geocoder(getActivity());
        List<Address> addressList;

        try {
            addressList = geocoder.getFromLocationName(address, 1);
            if(addressList != null) {
                Address singleaddress = addressList.get(0);
                LatLng latlng = new LatLng(singleaddress.getLatitude(), singleaddress.getLongitude());
                return latlng;
            } else {
                return null;
            }

        } catch (Exception e){
            e.printStackTrace();
            return null;
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
}