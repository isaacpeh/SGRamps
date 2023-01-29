package com.example.sgramps;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashScreen extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        Thread splashThread = new Thread() {

            public void run() {
                try {
                    // sleep time in milliseconds (3000 = 3sec)
                    sleep(0); //3000
                } catch (InterruptedException e) {
                    // Trace the error
                    e.printStackTrace();
                } finally {
                    // Launch the MainActivity class
                    FirebaseAuth fAuth = FirebaseAuth.getInstance();
                    FirebaseUser user = fAuth.getCurrentUser();
                    Intent intent;

                    if (user == null) {
                        intent = new Intent(SplashScreen.this, StartUpActivity.class);
                    } else {
                        Log.d("LOG", "USER LOGGED IN: " + user.getEmail());
                        intent = new Intent(SplashScreen.this, MainActivity.class);
                    }
                    startActivity(intent);
                }
            }
        };
        // To Start the thread
        splashThread.start();
    }
}