package com.example.sgramps.models;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class UserDAO {
    FirebaseFirestore db;
    FirebaseStorage storage;

    public UserDAO() {
    }

    public interface UserCallback {
        void onCallBack(UserModel user);
    }

    public interface BookmarkCallback {
        void onCallBack(List<String> bookmarks);
    }

    // USER BOOKMARKS
    public void getBookmark(String email, BookmarkCallback callback) {
        db = FirebaseFirestore.getInstance();
        db.collection("bookmarks").document(email).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                List<String> strings = document.getData().values().stream()
                                        .map(object -> Objects.toString(object, null))
                                        .collect(Collectors.toList());

                                List<String> ramp = null;
                                for (int i = 0; i < strings.size(); i++) {
                                    String tempRamp = strings.get(i).replace("[", "");
                                    tempRamp = tempRamp.replace("]", "");
                                    ramp = new ArrayList<String>(Arrays.asList(tempRamp.split(", ")));
                                }
                                callback.onCallBack(ramp);
                            } else {
                                // document dont exist
                                Log.d("LOG", "FIRESTORE: User bookmarks does not exist");
                            }
                        } else {
                            // error getting document
                            Log.d("LOG", "FIRESTORE: Error occurred while fetching user bookmarks");
                        }
                    }
                });
    }

    public void deleteBookmark(String email, String ramp_name) {
        db = FirebaseFirestore.getInstance();
        DocumentReference bookmarks = db.collection("bookmarks").document(email);
        bookmarks.update("ramps", FieldValue.arrayRemove(ramp_name));
    }

    public void setBookmark(String email, String ramp_name) {
        db = FirebaseFirestore.getInstance();
        DocumentReference bookmarks = db.collection("bookmarks").document(email);
        bookmarks.update("ramps", FieldValue.arrayUnion(ramp_name));
    }

    // USER DETAILS
    public void getUser(String email, UserCallback callback) {
        db = FirebaseFirestore.getInstance();
        db.collection("user").document(email).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            UserModel user = documentSnapshot.toObject(UserModel.class);
                            callback.onCallBack(user);
                        } else {
                            Log.d("LOG", "User: " + email + " not found!");
                        }
                    }
                });
    }

    public void uploadImage(UserModel user) {
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference("profileImages/" + user.getEmail());

        // perform upload of image from uri
        if (!user.getImg_url().contains("firebasestorage.googleapis.com")) {
            Uri file = Uri.parse(user.getImg_url());
            StorageReference uploadRef = storageReference.child(file.getLastPathSegment());
            Task<UploadTask.TaskSnapshot> uploadTask = uploadRef.putFile(file);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // get image url
                    uploadRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            // perform user update
                            user.setImg_url(uri.toString());
                            updateUser(user);
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                }
            });
        } else {
            // perform firestore update
            updateUser(user);
        }
    }

    public void updateUser(UserModel user) {
        db = FirebaseFirestore.getInstance();
        if (user.getPassword() == null) {
            db.collection("user").document(user.getEmail())
                    .update("name", user.getName(),
                            "number", user.getNumber(),
                            "dob", user.getDob(),
                            "gender", user.getGender(),
                            "img_url", user.getImg_url());
        } else {
            db.collection("user").document(user.getEmail())
                    .update("name", user.getName(),
                            "number", user.getNumber(),
                            "dob", user.getDob(),
                            "gender", user.getGender(),
                            "img_url", user.getImg_url(),
                            "password", user.getPassword());
            ;
        }


    }

    // USER CONTRIBUTIONS
    public void getContribution(String email, BookmarkCallback callback) {
        db = FirebaseFirestore.getInstance();
        db.collection("points")
                .whereEqualTo("uploader", email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<String> ramp = new ArrayList<String>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                ramp.add(document.getId());
                            }
                            callback.onCallBack(ramp);
                        } else {
                            Log.d("LOG", "FIRESTORE: Error getting contributions - ", task.getException());
                        }
                    }
                });
    }
}
