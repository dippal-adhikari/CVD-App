package com.example.cvd_draft_1;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import androidx.annotation.NonNull;


import java.net.URI;

public class ProfileActivity extends AppCompatActivity {
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseFirestore db;
    EditText etEmail, etName, etAddress, etPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Handle back button click
        TextView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Closes the current activity and returns to the previous one
            }
        });

        // Initialize Firebase Auth and Firestore
        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        Button btnLogout = findViewById(R.id.btnLogout);
        EditText etEmail = findViewById(R.id.etEmail);
        EditText etName = findViewById(R.id.etName);
        EditText etAddress = findViewById(R.id.etAddress);
        EditText etPhone = findViewById(R.id.etPhone);



        // Initialize edit icons
        ImageView iconEditName = findViewById(R.id.iconEditName);
        ImageView iconEditEmail = findViewById(R.id.iconEditEmail);
        ImageView iconEditPhone = findViewById(R.id.iconEditPhone);
        ImageView iconEditAddress = findViewById(R.id.iconEditAddress);
        ImageView iconEditPassword = findViewById(R.id.iconEditPassword);

        // Set click listeners for edit icons
        iconEditName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, ProfileEditActivity.class);

                // Pass user data to the next activity
                intent.putExtra("name", etName.getText().toString());
                intent.putExtra("phone", etPhone.getText().toString());
                intent.putExtra("address", etAddress.getText().toString());

                startActivity(intent);
            }
        });

        iconEditEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, ProfileEditActivity.class);

                // Pass user data to the next activity
                intent.putExtra("name", etName.getText().toString());
                intent.putExtra("phone", etPhone.getText().toString());
                intent.putExtra("address", etAddress.getText().toString());
                startActivity(intent);
            }
        });

        iconEditPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, ProfileEditActivity.class);
                // Pass user data to the next activity
                intent.putExtra("name", etName.getText().toString());
                intent.putExtra("phone", etPhone.getText().toString());
                intent.putExtra("address", etAddress.getText().toString());
                startActivity(intent);
            }
        });

        iconEditAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, ProfileEditActivity.class);
                // Pass user data to the next activity
                intent.putExtra("name", etName.getText().toString());
                intent.putExtra("phone", etPhone.getText().toString());
                intent.putExtra("address", etAddress.getText().toString());
                startActivity(intent);
            }
        });

        // Edit password click listener
        iconEditPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPasswordResetDialog();
            }
        });



        firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser == null) {
            Log.d("ProfileActivity", "User not signed in, redirecting to Login");
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            Log.d("ProfileActivity", "User signed in: " + firebaseUser.getEmail());
            etEmail.setText(firebaseUser.getEmail());

            // Retrieve user's name from Fire store
            DocumentReference docRef = db.collection("users").document(firebaseUser.getUid());
            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String username = document.getString("username");
                        String address = document.getString("address");
                        String phone = document.getString("phone");

                        etAddress.setText(address); // Set the address to the EditText
                        etName.setText(username);  // Set the username to the EditText
                        etPhone.setText(phone); // Set the username to the EditText
                    } else {
                        Log.d("ProfileActivity", "No such document");
                        Toast.makeText(ProfileActivity.this, "User data not found.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d("ProfileActivity", "Failed with: ", task.getException());
                    Toast.makeText(ProfileActivity.this, "Failed to retrieve user data.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        TextView tvContact = findViewById(R.id.tvContact);
        tvContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://communication-open.com/en/accueil-en/#contact"));
                startActivity(browserIntent);
            }
        });

        TextView tvPrivacy = findViewById(R.id.tvPrivacy);
        tvPrivacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://curriculum-vidae.com/privacy-policy/"));
                startActivity(browserIntent);
            }
        });

        TextView tvAgreement = findViewById(R.id.tvAgreement);
        tvAgreement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://curriculum-vidae.com/end-user-license-agreement"));
                startActivity(browserIntent);
            }
        });

        // Handle Logout
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    // Method to show a confirmation dialog for resetting the password
    private void showPasswordResetDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Reset Password")
                .setMessage("Are you sure you want to reset your password? An email will be sent to reset it.")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        sendPasswordResetEmail();
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    // Method to send the password reset email
    private void sendPasswordResetEmail() {
        if (firebaseUser != null) {
            String emailAddress = firebaseUser.getEmail();
            FirebaseAuth auth = FirebaseAuth.getInstance();

            auth.sendPasswordResetEmail(emailAddress)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(ProfileActivity.this, "Password reset email sent.", Toast.LENGTH_SHORT).show();
                                FirebaseAuth.getInstance().signOut();
                                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(ProfileActivity.this, "Failed to send reset email.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            Toast.makeText(this, "User not signed in.", Toast.LENGTH_SHORT).show();
        }
    }

}