package com.example.sgramps;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;

import android.os.Looper;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sgramps.adapters.PlacesAutoSuggestAdapter;
import com.example.sgramps.models.RampsDAO;
import com.example.sgramps.models.RampsModel;
import com.example.sgramps.models.UserDAO;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.GeoPoint;

import org.imaginativeworld.whynotimagecarousel.ImageCarousel;
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private GoogleMap map;
    LocationRequest locationRequest;
    LocationCallback locationCallback;
    FloatingActionButton btnLocate;
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

        // logged in user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        email = user.getEmail();

        // initialize view
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // initialize map fragment
        SupportMapFragment supportMapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.google_map);

        // initialize auto-complete
        AutoCompleteTextView autoCompleteTextView = view.findViewById(R.id.txtSearch);
        autoCompleteTextView.setAdapter(new PlacesAutoSuggestAdapter(getActivity(), android.R.layout.simple_list_item_1));

        // initialize current location
        btnLocate = view.findViewById(R.id.btnLocation);

        // async map
        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @SuppressLint("PotentialBehaviorOverride")
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;
                map.getUiSettings().setMapToolbarEnabled(false);
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(1.3433453, 103.783137), 11.0f));

                // GET USER LOCATION FUNCTION
                btnLocate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        btnLocate.setEnabled(false);
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
                        Log.d("LOG", "ADDRESS SELECTED: " + autoCompleteTextView.getText().toString());
                        LatLng latlng = getlatLng(autoCompleteTextView.getText().toString());
                        if (latlng != null) {
                            Log.d("LOG", "LATITUDE: " + latlng.latitude + " - LONGITUDE: " + latlng.longitude);
                            LatLng latLng = new LatLng(latlng.latitude, latlng.longitude);
                            moveMap(latLng, false);
                        } else {
                            Log.d("LOG", "Lat Lng not found");
                        }
                    }
                });

                // MARKER CLICKED LISTENER
                map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        try {
                            RampsModel markerRamp = (RampsModel) marker.getTag();
                            createRampDialog(markerRamp);
                        } catch (NullPointerException e) {
                            LatLng latLng = new LatLng(marker.getPosition().latitude, marker.getPosition().longitude);
                            createDialog(latLng);
                        }
                        return false;
                    }
                });
            }
        });
        return view;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            requireActivity().getSupportFragmentManager().setFragmentResultListener("requestKey", this, new FragmentResultListener() {
                @Override
                public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle bundle) {
                    String result = bundle.getString("ramp");
                    getRampByName(result);
                }
            });
        }
    }

    // get current location
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
                    btnLocate.setEnabled(true);
                    return;
                } else {
                    for (Location location : locationResult.getLocations()) {
                        if (location != null) {
                            LatLng latLng = null;
                            Double lat = location.getLatitude();
                            Double lng = location.getLongitude();
                            Log.d("LOG", "USER LATITUDE: " + lat + " - USER LONGITUDE: " + lng);
                            fusedLocationClient.removeLocationUpdates(locationCallback);
                            latLng = new LatLng(lat, lng);
                            moveMap(latLng, false);
                            generateCircle(latLng);
                            getRamps(latLng);
                        }
                    }
                    btnLocate.setEnabled(true);
                }
            }
        };

        // check for permissions
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            btnLocate.setEnabled(true);
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, 44);
        } else {
            Toast.makeText(getActivity(), "Getting location...", Toast.LENGTH_SHORT).show();
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
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
                    Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.wheelchair_icon);
                    Bitmap smallMarker = Bitmap.createScaledBitmap(b, 220, 270, false);
                    BitmapDescriptor smallMarkerIcon = BitmapDescriptorFactory.fromBitmap(smallMarker);
                    markerOptions.icon(smallMarkerIcon);
                    Marker marker = map.addMarker(markerOptions);
                    marker.setTag(ramp);
                }
            }
        });
    }

    // generate radius circle
    private void generateCircle(LatLng latlng) {
        Circle circle = map.addCircle(new CircleOptions()
                .center(latlng)
                .radius(500)
                .fillColor(Color.argb(30, 28, 130, 255))
                .strokeColor(Color.argb(200, 4, 30, 224))
                .strokeWidth(4));
    }

    // generate pin and move map
    private void moveMap(LatLng latlng, boolean zoom) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latlng);


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

    // open create popup view
    public void createDialog(LatLng latLng) {
        BottomSheetDialog dialog = new BottomSheetDialog(getActivity());
        View bottomSheetView = getLayoutInflater().inflate(R.layout.create_popup, null);
        dialog.setContentView(bottomSheetView);

        Button btnCreate = (Button) dialog.findViewById(R.id.btnCreate);
        ImageButton imgBtn = dialog.findViewById(R.id.imgBtnNavigate);

        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // pass to create page with latlng
                Bundle result = new Bundle();
                double[] locationArr = {latLng.latitude, latLng.longitude};
                result.putDoubleArray("latlng", locationArr);
                getParentFragmentManager().setFragmentResult("requestLatlng", result);
                getParentFragmentManager().beginTransaction().hide(MainActivity.active).show(MainActivity.fragmentUpload).commit();
                BottomNavigationView mBottomNavigationView = getActivity().findViewById(R.id.bottom_navigation_view);
                mBottomNavigationView.setSelectedItemId(R.id.upload_page);
                dialog.dismiss();
            }
        });

        imgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uri = "http://maps.google.com/maps?daddr=" + latLng.latitude + "," + latLng.longitude;
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                intent.setPackage("com.google.android.apps.maps");
                startActivity(intent);
            }
        });
        dialog.show();
    }

    // open ramp popup view
    public void createRampDialog(RampsModel selectedMarker) {
        LatLng latLng = new LatLng(selectedMarker.getGpoint().getLatitude(), selectedMarker.getGpoint().getLongitude());
        BottomSheetDialog dialog = new BottomSheetDialog(getActivity());
        View bottomSheetView = getLayoutInflater().inflate(R.layout.popup, null);
        dialog.setContentView(bottomSheetView);

        Button btnBookmark = (Button) dialog.findViewById(R.id.btnBookmark);
        ImageButton imgBtn = dialog.findViewById(R.id.imgBtnNavigate);
        ImageButton imgBtnReport = dialog.findViewById(R.id.imgBtnReport);

        UserDAO userDb = new UserDAO();

        // set attributes accordingly
        TextView txtTitle = dialog.findViewById(R.id.txtTitle);
        TextView txtDescription = dialog.findViewById(R.id.txtDescription);
        ImageCarousel carousel = dialog.findViewById(R.id.carousel);
        txtTitle.setText(selectedMarker.getRamp_name());
        txtDescription.setText(selectedMarker.getRamp_description());
        carousel.registerLifecycle(getViewLifecycleOwner());
        List<CarouselItem> list = new ArrayList<>();
        for (String url : selectedMarker.getImg_url()
        ) {
            list.add(new CarouselItem(url));
        }
        if (list.size() <= 1) {
            carousel.setShowNavigationButtons(false);
            carousel.setInfiniteCarousel(false);
        }
        carousel.setData(list);

        // fetch user bookmarks
        userDb.getBookmark(email, new UserDAO.BookmarkCallback() {
            @Override
            public void onCallBack(List<String> bookmarks) {
                if (bookmarks.contains(selectedMarker.getRamp_name())) {
                    // bookmarked
                    btnBookmark.setText("Remove from bookmarks");
                    btnBookmark.setSelected(false);
                    btnBookmark.setBackgroundColor(Color.rgb(255, 49, 49));
                } else {
                    // not bookmarked
                    btnBookmark.setText("Bookmark ramp");
                    btnBookmark.setBackgroundColor(Color.rgb(27, 115, 232));
                    btnBookmark.setSelected(true);
                }
            }
        });

        // bookmark button click function
        btnBookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btnBookmark.isSelected()) {
                    btnBookmark.setText("Remove from bookmarks");
                    btnBookmark.setSelected(false);
                    btnBookmark.setBackgroundColor(Color.rgb(255, 49, 49));
                    userDb.setBookmark(email, selectedMarker.getRamp_name());
                    Toast.makeText(getActivity(), "Added to bookmarks", Toast.LENGTH_SHORT).show();
                } else {
                    btnBookmark.setText("Bookmark ramp");
                    btnBookmark.setSelected(true);
                    btnBookmark.setBackgroundColor(Color.rgb(27, 115, 232));
                    userDb.deleteBookmark(email, selectedMarker.getRamp_name());
                    Toast.makeText(getActivity(), "Removed from bookmarks", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // navigate button click function
        imgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uri = "http://maps.google.com/maps?daddr=" + latLng.latitude + "," + latLng.longitude;
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                intent.setPackage("com.google.android.apps.maps");
                startActivity(intent);
            }
        });

        // report button click function
        imgBtnReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.active = MainActivity.fragmentReport;
                Bundle result = new Bundle();
                result.putString("ramp_name_report", selectedMarker.getRamp_name());
                getParentFragmentManager().setFragmentResult("requestRampReport", result);
                getParentFragmentManager().beginTransaction().hide(MainActivity.active).show(MainActivity.fragmentReport).commit();
                dialog.dismiss();
            }
        });

        // This callback will only be called when MyFragment is at least Started.
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                if (MainActivity.active == MainActivity.fragmentReport) {
                    getParentFragmentManager().beginTransaction().hide(MainActivity.active).show(MainActivity.fragmentHome).commit();
                    MainActivity.active = MainActivity.fragmentHome;
                } else {
                    requireActivity().finish();
                }
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getActivity(), callback);
        dialog.show();
    }

    // move map according to bookmarks callback
    public void getRampByName(String ramp_name) {
        RampsDAO dbRamps = new RampsDAO();
        dbRamps.getRampByName(ramp_name, new RampsDAO.SingleRampCallback() {
            @Override
            public void onCallBack(RampsModel result) {
                RampsModel ramp = result;

                LatLng latLng = new LatLng(result.getGpoint().getLatitude(), result.getGpoint().getLongitude());
                map.clear();
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                Marker marker = map.addMarker(markerOptions);
                marker.setTag(ramp);
                createRampDialog(ramp);
            }
        });
    }
}