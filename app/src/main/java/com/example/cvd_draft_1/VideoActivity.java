package com.example.cvd_draft_1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class VideoActivity extends AppCompatActivity {
    ImageButton btnBack;  // Declare the back button


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        // Initialize the ImageButton for the back button
        btnBack = findViewById(R.id.btnBack);

        // Set click listener for the back button to finish the current activity
        btnBack.setOnClickListener(v -> finish());

        // Handle add new button click
        TextView btnAddNew = findViewById(R.id.btnAddNew);
        btnAddNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VideoActivity.this, ScriptListActivity.class);
                startActivity(intent);
                finish();
            }
        });


    }
}
