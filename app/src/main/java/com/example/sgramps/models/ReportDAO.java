package com.example.sgramps.models;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

public class ReportDAO {
    FirebaseFirestore db;
    FirebaseStorage storage;

    public ReportDAO() {
    }

    public interface UploadCallback {
        void onCallback(String result);
    }

    public interface CreateReportCallback {
        void onCallback(String msg);
    }

    public void uploadReportImages(ReportModel report, UploadCallback callback) {
        storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference("reportImages/" + report.getReported_at() + "-" + report.getReporter());
        List<String> img_urls = new ArrayList<String>();

        for (int i = 0; i < report.getImg_urls().size(); i++) {
            Uri file = Uri.parse(report.getImg_urls().get(i));
            StorageReference uploadRef = storageReference.child(file.getLastPathSegment());
            Task<UploadTask.TaskSnapshot> uploadTask = uploadRef.putFile(file);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // get image url
                    uploadRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            // perform report creation
                            img_urls.add(uri.toString());
                            if (img_urls.size() == report.getImg_urls().size()) {
                                report.setImg_urls(img_urls);
                                createReport(report, new ReportDAO.CreateReportCallback() {
                                    @Override
                                    public void onCallback(String msg) {
                                        callback.onCallback(msg);
                                    }
                                });
                            }
                        }
                    });
                }
            });

        }
    }

    public void createReport(ReportModel report, CreateReportCallback callback) {
        db = FirebaseFirestore.getInstance();
        CollectionReference dbReports = db.collection("reports");

        dbReports.document(report.getReported_at() + "-" + report.getReporter()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String error = "Report ID already exists";
                        callback.onCallback(error);
                    } else {
                        dbReports.document(report.getReported_at() + "-" + report.getReporter())
                                .set(report).
                                addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d("LOG", " FIRESTORE: Successfully reported ramp " + report.getRamp_name());
                                        String result = "Successfully reported ramp";
                                        callback.onCallback(result);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d("LOG", "FIRESTORE: Failed to report ramp " + report.getRamp_name());
                                        String error = "Failed to report ramp";
                                        callback.onCallback(error);
                                    }
                                });
                    }
                } else {
                    String error = "Unknown error";
                    callback.onCallback(error);
                }
            }
        });

    }
}
