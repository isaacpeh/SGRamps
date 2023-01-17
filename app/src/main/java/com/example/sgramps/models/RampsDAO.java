package com.example.sgramps.models;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.Arrays;
import java.util.List;

public class RampsDAO {
    FirebaseFirestore db;

    public RampsDAO() {
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
                        Log.d("FireStore: INSERT", "Successfully inserted ramp " + ramp.getRamp_name());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("FireStore: INSERT", "Failed to insert ramp " + ramp.getRamp_name());
                    }
                });
    }

    public void getRamp(LatLng latLng) {
        
    }
}
