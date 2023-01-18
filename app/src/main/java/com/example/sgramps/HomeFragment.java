package com.example.sgramps;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.example.sgramps.adapters.PlacesAutoSuggestAdapter;
import com.example.sgramps.models.RampsDAO;
import com.example.sgramps.models.RampsModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.List;

public class HomeFragment extends Fragment {

    private GoogleMap map;
    private int btnToggle = 0;
    LocationRequest locationRequest;
    LocationCallback locationCallback;
    BottomSheetDialog dialog;


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
                        createDialog();
                        FusedLocationProviderClient fusedLocationClient;
                        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
                        getLocation(fusedLocationClient);
                    }
                });

                // MAP CLICKED FUNCTION
                googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(@NonNull LatLng latLng) {
                        // when clicked on map
                        closeKeyboard();
                        autoCompleteTextView.clearFocus();
                        moveMap(latLng, true);
                        generateCircle(latLng);
                        getRamps(latLng);
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
                            moveMap(latLng, false);
                        } else {
                            Log.d("Lat Lng", "Lat Lng not found");
                        }
                    }
                });
            }
        });
        return view;
    }

    // get current location
    private void getLocation(FusedLocationProviderClient fusedLocationClient) {
        if (btnToggle == 0) {
            btnToggle = 1;
            locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 100)
                    .setWaitForAccurateLocation(true)
                    .setMinUpdateIntervalMillis(3000)
                    .setMaxUpdateDelayMillis(100)
                    .build();

            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    if (locationResult == null) {
                        return;
                    } else {
                        for (Location location : locationResult.getLocations()) {
                            if (location != null) {
                                LatLng latLng = null;
                                Double lat = location.getLatitude();
                                Double lng = location.getLongitude();
                                Log.d("GPS", "Lat: " + lat + " | long: " + lng);
                                fusedLocationClient.removeLocationUpdates(locationCallback);
                                latLng = new LatLng(lat, lng);
                                moveMap(latLng, false);
                                generateCircle(latLng);
                                getRamps(latLng);
                            }
                        }
                    }
                }
            };

            //

            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION}, 44);
            } else {
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
            }
        } else if (btnToggle == 1) {
            btnToggle = 0;
            Log.d("GPS", "TODO: Change button icon to X to remove currently selected location");
        }
    }

    // get ramps within location
    private void getRamps(LatLng latLng) {
        RampsDAO dbRamps = new RampsDAO();
        dbRamps.getRamp(latLng, new RampsDAO.FirebaseCallback() {
            @Override
            public void onCallBack(List<RampsModel> ramps) {
                for (RampsModel ramp : ramps
                ) {
                    GeoPoint gp = ramp.getGpoint();
                    LatLng latlng = new LatLng(gp.getLatitude(), gp.getLongitude());
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latlng);
                    markerOptions.title(latlng.latitude + " : " + latlng.longitude);
                    map.addMarker(markerOptions);
                }
            }
        });
    }

    // generate radius circle and pins within radius
    private void generateCircle(LatLng latlng) {
        Circle circle = map.addCircle(new CircleOptions()
                .center(latlng)
                .radius(500)
                .fillColor(Color.argb(30, 28, 130, 255))
                .strokeColor(Color.argb(200, 4, 30, 224))
                .strokeWidth(4));

        // https://developers.google.com/maps/documentation/javascript/examples/marker-accessibility
    }

    // generate pin and move map
    private void moveMap(LatLng latlng, boolean zoom) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latlng);
        markerOptions.title(latlng.latitude + " : " + latlng.longitude);
        map.clear();
        if (map.getCameraPosition().zoom > 15 && zoom) {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, map.getCameraPosition().zoom));
        } else {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 15));
        }
        map.addMarker(markerOptions);
    }

    // get LatLng from selected address
    private LatLng getlatLng(String address) {
        Geocoder geocoder = new Geocoder(getActivity());
        List<Address> addressList;

        try {
            addressList = geocoder.getFromLocationName(address, 1);
            if (addressList != null) {
                Address singleaddress = addressList.get(0);
                LatLng latlng = new LatLng(singleaddress.getLatitude(), singleaddress.getLongitude());
                return latlng;
            } else {
                return null;
            }

        } catch (Exception e) {
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

    public void createDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(getActivity());
        View bottomSheetView = getLayoutInflater().inflate(R.layout.popup, null);
        dialog.setContentView(bottomSheetView);
        dialog.show();
       /* View view = getLayoutInflater().inflate(R.layout.popup, null, false);
        dialog.setContentView(view);
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        dialog.show();*/
    }
}