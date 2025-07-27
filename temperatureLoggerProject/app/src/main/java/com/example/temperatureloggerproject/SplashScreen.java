package com.example.temperatureloggerproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        Intent toHome = new Intent(SplashScreen.this,MainActivity.class);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(toHome);
                finish();
            }
        },4000);

        HashMap<String, Object> signal = new HashMap<>();
        signal.put("signal", true);
        signal.put("timeForDelay", Integer.parseInt("1"));
        FirebaseDatabase.getInstance().getReference().child("signal").updateChildren(signal);
    }
}