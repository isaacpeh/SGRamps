package com.example.sgramps.models;

import android.location.Location;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

public class RampsDAO {
    FirebaseFirestore db;
    FirebaseStorage storage;

    public RampsDAO() {
    }

    public interface UploadCallback {
        void onCallBack(String msg);
    }

    public interface CreateCallback {
        void onCallBack(String result);
    }

    public interface FirebaseCallback {
        void onCallBack(List<RampsModel> ramps);
    }

    public interface SingleRampCallback {
        void onCallBack(RampsModel ramp);
    }

    public void addRamp(RampsModel ramp, CreateCallback callback) {
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference("rampImages/" + ramp.getRamp_name());
        List<String> img_urls = new ArrayList<String>();

        for (int i = 0; i < ramp.getImg_url().size(); i++) {
            Uri file = Uri.parse(ramp.getImg_url().get(i));
            StorageReference uploadRef = storageReference.child(file.getLastPathSegment());
            Task<UploadTask.TaskSnapshot> uploadTask = uploadRef.putFile(file);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // get image url
                    uploadRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            // perform ramp creation
                            img_urls.add(uri.toString());
                            if (img_urls.size() == ramp.getImg_url().size()) {
                                ramp.setImg_url(img_urls);
                                createRamp(ramp, new UploadCallback() {
                                    @Override
                                    public void onCallBack(String msg) {
                                        callback.onCallBack(msg);
                                    }
                                });
                            }
                        }
                    });
                }
            });
        }
    }

    public void createRamp(RampsModel ramp, UploadCallback callback) {
        CollectionReference dbRamps = db.collection("points");
        dbRamps.document(ramp.getRamp_name()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String error = "Ramp name already exists";
                        callback.onCallBack(error);
                    } else {
                        dbRamps.document(ramp.getRamp_name())
                                .set(ramp).
                                addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d("LOG", " FIRESTORE: Successfully inserted ramp " + ramp.getRamp_name());
                                        String result = "Successfully uploaded ramp";
                                        callback.onCallBack(result);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d("LOG", "FIRESTORE: Failed to insert ramp " + ramp.getRamp_name());
                                        String error = "Failed to upload ramp";
                                        callback.onCallBack(error);
                                    }
                                });
                    }
                } else {
                    String error = "Unknown error";
                    callback.onCallBack(error);
                }
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
