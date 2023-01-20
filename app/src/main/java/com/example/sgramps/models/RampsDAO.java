package com.example.sgramps.models;

import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class RampsDAO {
    FirebaseFirestore db;

    public RampsDAO() {
    }

    public interface FirebaseCallback {
        void onCallBack(List<RampsModel> ramps);
    }

    public interface SingleRampCallback {
        void onCallBack(RampsModel ramp);
    }

    public void addRamp(RampsModel ramp) { // add callback or smth
        db = FirebaseFirestore.getInstance();
        CollectionReference dbRamps = db.collection("points");
        //List<String> img_arr = Arrays.asList("img_1.png");
        //RampsModel rampz = new RampsModel("CoolRamp", "coolest ramp in town", Timestamp.now(), "isaac@gmail.com", img_arr,  true, new GeoPoint(5.373982, 2.705164));

        dbRamps
                .document(ramp.getRamp_name())
                .set(ramp).
                addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("LOG", " FIRESTORE: Successfully inserted ramp " + ramp.getRamp_name());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("LOG", "FIRESTORE: Failed to insert ramp " + ramp.getRamp_name());
                    }
                });
    }

    public void getRamp(LatLng latLng, FirebaseCallback callback) {
        List<RampsModel> ramps = new ArrayList<RampsModel>();

        db = FirebaseFirestore.getInstance();
        db.collection("points").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                            for (DocumentSnapshot d : list) {
                                // getData() -> returns entire document
                                // .getId() - > returns documentID
                                // .getData().get("attribute") -> returns selected attribute
                                GeoPoint gp = d.getGeoPoint("gpoint");
                                LatLng pointLatlng = new LatLng(gp.getLatitude(), gp.getLongitude());
                                float[] result = new float[1];
                                Location.distanceBetween(latLng.latitude, latLng.longitude,
                                        pointLatlng.latitude, pointLatlng.longitude, result);
                                float distanceInMeters = result[0];
                                boolean isWithin500m = (distanceInMeters <= 500);
                                if (isWithin500m) {
                                    RampsModel tempRamp = d.toObject(RampsModel.class);
                                    ramps.add(tempRamp);
                                }
                            }
                            callback.onCallBack(ramps);
                        } else {
                            Log.d("LOG", "No ramps found within radius");
                        }
                    }
                });
    }

    public void getRampByName(String ramp_name, SingleRampCallback callback) {
        db = FirebaseFirestore.getInstance();
        db.collection("points").document(ramp_name).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            RampsModel ramp = documentSnapshot.toObject(RampsModel.class);
                            callback.onCallBack(ramp);
                        } else {
                            Log.d("LOG", ramp_name + " not found!");
                        }
                    }
                });
    }
}
