package com.example.cvd_draft_1;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseFirestore db;
    EditText etEmail, etName, etAddress, etPhone;
    ImageButton btnBack;  // Declare the back button

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Ensure the locale is applied before setting the content view
//        loadLocale();

        setContentView(R.layout.activity_profile);

        // Initialize the ImageButton for the back button
       ImageButton btnBack = findViewById(R.id.btnBack);

        // Back button action
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Initialize Firebase Auth and Firestore
        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        Button btnLogout = findViewById(R.id.btnLogout);
        Button btnDeleteAccount = findViewById(R.id.btnDeleteAccount);
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

        TextView tvToggleTheme = findViewById(R.id.tvToggleTheme);
        tvToggleTheme.setOnClickListener(view -> {
            boolean isDarkModeEnabled = ThemeUtils.isDarkMode(this);
            ThemeUtils.saveThemeState(this, !isDarkModeEnabled); // Toggle and save the new theme state
            ThemeUtils.applyTheme(this); // Apply the new theme
            recreate(); // Recreate the activity to apply the new theme
        });

        TextView tvLanguage = findViewById(R.id.tvLanguage);
        tvLanguage.setOnClickListener(view -> {
            final String[] languages = {"English", "français", "deutsch", "Italiano"};
            AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
            mBuilder.setTitle("Choose Language");
            mBuilder.setSingleChoiceItems(languages, -1, (dialogInterface, i) -> {
                if (i == 0) {
                    setLocale("");
                    recreate();
                } else if (i == 1) {
                    setLocale("fr");
                    recreate();
                } else if (i == 2) {
                    setLocale("nl");
                    recreate();
                } else if (i == 3) {
                    setLocale("it");
                    recreate();
                }
            });
            mBuilder.create();
            mBuilder.show();
        });


        // Handle Logout
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), WorksActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Handle delete account
        btnDeleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show confirmation dialog for deleting the account
                new AlertDialog.Builder(ProfileActivity.this)
                        .setTitle("Delete Account")
                        .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Proceed with account deletion
                                if (firebaseUser != null) {
                                    firebaseUser.delete()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(ProfileActivity.this, "Account deleted successfully.", Toast.LENGTH_SHORT).show();
                                                        FirebaseAuth.getInstance().signOut();
                                                        Intent intent = new Intent(getApplicationContext(), WorksActivity.class);
                                                        startActivity(intent);
                                                        finish();
                                                    } else {
                                                        Toast.makeText(ProfileActivity.this, "Failed to delete account. Please log in again and try.", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                } else {
                                    Toast.makeText(ProfileActivity.this, "User not signed in.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });

    }

    private void setLocale(String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.setLocale(locale);
        getBaseContext().getResources().updateConfiguration(configuration, getBaseContext().getResources().getDisplayMetrics());
        SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
        editor.putString("Language", language);
        editor.apply();
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