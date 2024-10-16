package com.example.cvd_draft_1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class ScriptReadyNotification extends AppCompatActivity {

    // Firebase-related fields
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private String scriptId;

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


        ImageButton btnBack = findViewById(R.id.btnBack);
        Button btnRecordNow = findViewById(R.id.btnRecordNow);
        TextView btnRecordLater = findViewById(R.id.tvRecordLater);

        Intent intent_db = getIntent();
        scriptId = intent_db.getStringExtra("SCRIPT_ID");
        ArrayList<String> questions = intent_db.getStringArrayListExtra("QUESTIONS");
        ArrayList<String> answers = intent_db.getStringArrayListExtra("ANSWERS");

        // Handle Back Button Click
        btnBack.setOnClickListener(v -> finish());  // Close current activity and go back

        btnRecordNow.setOnClickListener(v -> {
            Intent intent = new Intent(ScriptReadyNotification.this, CreateVideoActivity.class);
            intent.putExtra("SCRIPT_ID", scriptId); // Pass the script ID
            intent.putStringArrayListExtra("QUESTIONS", questions); // Use the questions array directly
            intent.putStringArrayListExtra("ANSWERS", answers); // Use the answers array directly
            startActivity(intent);
        });


        btnRecordLater.setOnClickListener(v -> {
            Toast.makeText(ScriptReadyNotification.this, "Script saved successfully!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ScriptReadyNotification.this, MainActivity.class);
            startActivity(intent);
        });


    }
}