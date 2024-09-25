package com.example.cvd_draft_1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class LauncherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        Button backButton = findViewById(R.id.btnBack);

        // Back button action
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();  // Closes this activity and goes back to the previous one
            }
        });

        // Signup button logic
        Button btnSignup = findViewById(R.id.btnSignup);
        btnSignup.setOnClickListener(view -> {
            Intent intent = new Intent(LauncherActivity.this, RegistrationActivity.class);
            startActivity(intent);
        });

        // Login button logic
        Button btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(view -> {
            Intent intent = new Intent(LauncherActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }
}
