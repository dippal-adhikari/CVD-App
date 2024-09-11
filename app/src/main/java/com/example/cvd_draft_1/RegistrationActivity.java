package com.example.cvd_draft_1;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {

    EditText editTextEmail, editTextPassword, editTextUsername;
    Button buttonReg;
    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registration);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();  // Initialize Firestore

        editTextEmail = findViewById(R.id.etEmail);
        editTextPassword = findViewById(R.id.etPassword);
        editTextUsername = findViewById(R.id.etName);
        buttonReg = findViewById(R.id.btnReg);

        // Back button
        TextView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Navigate to LoginActivity
        TextView textViewLogin = findViewById(R.id.tvLog);
        textViewLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Handle user registration
        buttonReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = editTextEmail.getText().toString();
                String password = editTextPassword.getText().toString();
                String username = editTextUsername.getText().toString();  // Collect username input

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(RegistrationActivity.this, "Enter email", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(RegistrationActivity.this, "Enter password", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(username)) {
                    Toast.makeText(RegistrationActivity.this, "Enter username", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Firebase Authentication - Create user with email and password
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, get Firebase user
                                    FirebaseUser user = mAuth.getCurrentUser();

                                    // Store user data in Firestore
                                    Map<String, Object> userData = new HashMap<>();
                                    userData.put("username", username);
                                    userData.put("email", email);

                                    db.collection("users").document(user.getUid())
                                            .set(userData)
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(RegistrationActivity.this, "Registration successful.",
                                                        Toast.LENGTH_SHORT).show();

                                                // Navigate to main activity
                                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                                startActivity(intent);
                                                finish();
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(RegistrationActivity.this, "Failed to save user data.",
                                                        Toast.LENGTH_SHORT).show();
                                            });
                                } else {
                                    // If sign in fails, display a message to the user
                                    Toast.makeText(RegistrationActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }
}
