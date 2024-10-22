package com.example.cvd_draft_1;

import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileEditActivity extends AppCompatActivity {
    FirebaseUser firebaseUser;
    FirebaseAuth mAuth;
    private EditText etName, etPhone, etAddress;
    private Button btnSaveChanges;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        // Initialize views
        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        firebaseUser = mAuth.getCurrentUser();

        // Retrieve data passed from ProfileActivity
        String name = getIntent().getStringExtra("name");
        String phone = getIntent().getStringExtra("phone");
        String address = getIntent().getStringExtra("address");

        // Pre-fill the EditText fields with the user data
        etName.setText(name);
        etPhone.setText(phone);
        etAddress.setText(address);

        // Handle Save button click
        btnSaveChanges.setOnClickListener(v -> saveUserDetails());

        ImageButton btnBack = findViewById(R.id.btnBack);
        // Back button action
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileEditActivity.this, ProfileActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void saveUserDetails() {
        String name = etName.getText().toString();
        String phone = etPhone.getText().toString();
        String address = etAddress.getText().toString();

        // Validate input
        if (name.isEmpty()) {
            Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save to Firestore (adjust the Firestore logic as per your requirements)
        db.collection("users").document(firebaseUser.getUid()) // Replace "user_id" with actual ID
                .update(
                        "username", name,
                        "phone", phone,
                        "address", address
                )
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(ProfileEditActivity.this, "Details updated successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ProfileEditActivity.this, ProfileActivity.class);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> Toast.makeText(ProfileEditActivity.this, "Failed to update details", Toast.LENGTH_SHORT).show());
    }
}
