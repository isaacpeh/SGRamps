package com.example.sgramps;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;

import com.example.sgramps.adapters.PlacesAutoSuggestAdapter;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.util.Arrays;
import java.util.List;

public class HomeFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // initialize view
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // initialize map fragment
        SupportMapFragment supportMapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.google_map);

        // async map
        supportMapFragment.getMapAsync(new OnMapReadyCallback() {

            @Override
            public void onMapReady(GoogleMap googleMap) {

                // auto-complete
                AutoCompleteTextView autoCompleteTextView = view.findViewById(R.id.txtSearch);
                autoCompleteTextView.setAdapter(new PlacesAutoSuggestAdapter(getActivity() , android.R.layout.simple_list_item_1));
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

                            // initialize marker options
                            MarkerOptions markerOptions = new MarkerOptions();
                            // set position of marker
                            markerOptions.position(latLng);
                            // set title of marker
                            markerOptions.title(latLng.latitude + " : " + latLng.longitude);
                            // remove all marker
                            googleMap.clear();
                            // animate camera to zoom
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                            // add marker
                            googleMap.addMarker(markerOptions);
                        } else {
                            Log.d("Lat Lng", "Lat Lng not found");
                        }
                    }
                });

                // when map loaded
                googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(@NonNull LatLng latLng) {
                        // when clicked on map
                        closeKeyboard();
                        autoCompleteTextView.clearFocus();

                        Log.d("clikedLatLng", " " + latLng); //lat/lng: (6.637593339651669,0.9557589143514632)
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(latLng);
                        markerOptions.title(latLng.latitude + " : " + latLng.longitude);
                        googleMap.clear();
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                        googleMap.addMarker(markerOptions);
                    }
                });
            }
        });
        return view;
    }

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

    private void closeKeyboard()
    {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager manager
                    = (InputMethodManager)
                    getActivity().getSystemService(
                            Context.INPUT_METHOD_SERVICE);
            manager
                    .hideSoftInputFromWindow(
                            view.getWindowToken(), 0);
        }
    }
}