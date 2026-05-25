package com.example.writeai_android.ui.profile;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.writeai_android.R;

public class ProfileActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.profile_container, new ProfileFragment())
                    .commit();
        }
    }
}
