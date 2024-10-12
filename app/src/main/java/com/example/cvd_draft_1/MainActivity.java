package com.example.cvd_draft_1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Apply window insets for better edge-to-edge layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize BottomNavigationView
        bottomNavigationView = findViewById(R.id.bottomNavigation);

        // Handle navigation item clicks
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull android.view.MenuItem item) {
                if (item.getItemId() == R.id.nav_home) {
                    // Stay in MainActivity
                    return true;
                } else if (item.getItemId() == R.id.nav_create) {
                    // Navigate to ScriptActivity
                    Intent intent = new Intent(MainActivity.this, ScriptActivity.class);
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });

        // View/Edit Profile button click listener
        Button btnViewEditProfile = findViewById(R.id.btnViewEditProfile);
        btnViewEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });


        // Script button click listener
        CardView CVScripts = findViewById(R.id.CVScripts);
        CVScripts.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ScriptListActivity.class);
            startActivity(intent);
        });

        // Script cardview click listener
        CardView progressSection = findViewById(R.id.progressSection);
        progressSection.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ScriptActivity.class);
            startActivity(intent);
        });

        // Videos button click listener
        LinearLayout btnVideos = findViewById(R.id.btnVideos);
        btnVideos.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, VideoActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Ensure that the BottomNavigationView is initialized before using it
        if (bottomNavigationView != null) {
            // Set Home as selected when returning to MainActivity
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }
    }
}
