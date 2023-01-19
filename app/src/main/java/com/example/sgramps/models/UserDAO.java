package com.example.sgramps.models;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class UserDAO {
    FirebaseFirestore db;

    public UserDAO() {
    }

    /*public interface FirebaseCallback {
        void onCallBack(List<UserModel> user);
    }*/

    public interface BookmarkCallback {
        void onCallBack(List<String> bookmarks);
    }

    public void getBookmark(String email, BookmarkCallback callback) {
        db = FirebaseFirestore.getInstance();
        db.collection("bookmarks").document(email).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            Log.d("LOG", "FIRESTORE: Fetching bookmarks for user - " + email);
                            if (document.exists()) {
                                // do stuff here
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

    public void deleteBookmark(String ramp) {

    }
}
