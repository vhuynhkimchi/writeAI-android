package com.example.writeai_android;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Compatibility launcher.
 * The real dashboard is now in com.example.writeai_android.ui.main.MainActivity.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startActivity(new Intent(this, com.example.writeai_android.ui.auth.LoginActivity.class));
        finish();
    }
}
