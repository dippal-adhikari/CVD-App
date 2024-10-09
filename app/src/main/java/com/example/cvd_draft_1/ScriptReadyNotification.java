package com.example.cvd_draft_1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ScriptReadyNotification extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_script_ready_notification);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        TextView btnBack = findViewById(R.id.btnBack);
        Button btnRecordNow = findViewById(R.id.btnRecordNow);
        TextView btnRecordLater = findViewById(R.id.tvRecordLater);

        // Handle Back Button Click
        btnBack.setOnClickListener(v -> finish());  // Close current activity and go back

        btnRecordNow.setOnClickListener(v -> {
            Intent intent = new Intent(ScriptReadyNotification.this, CreateVideoActivity.class);
            startActivity(intent);
        });

        btnRecordLater.setOnClickListener(v -> {
            Toast.makeText(ScriptReadyNotification.this, "Script saved successfully!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ScriptReadyNotification.this, MainActivity.class);
            startActivity(intent);
        });


    }
}