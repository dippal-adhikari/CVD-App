package com.example.cvd_draft_1;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class RegistrationActivity extends AppCompatActivity {


    EditText editTextEmail, editTextPassword, editTextUsername;
    Button buttonReg;
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    GoogleSignInClient googleSignInClient;
    ShapeableImageView imageView;
    TextView name, mail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();  // Initialize Firestore
        editTextEmail = findViewById(R.id.etEmail);
        editTextPassword = findViewById(R.id.etPassword);

        Button buttonReg = findViewById(R.id.btnReg);
        buttonReg.setOnClickListener(view -> register());

        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(RegistrationActivity.this, options);

        // google sign in
        SignInButton googleSignInButton = findViewById(R.id.sign_in_button);
        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = googleSignInClient.getSignInIntent();
                activityResultLauncher.launch(intent);
            }
        });
        if (mAuth.getCurrentUser() != null) {
            Glide.with(RegistrationActivity.this).load(Objects.requireNonNull(mAuth.getCurrentUser()).getPhotoUrl()).into(imageView);
            name.setText(mAuth.getCurrentUser().getDisplayName());
            mail.setText(mAuth.getCurrentUser().getEmail());
        }

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
                                    userData.put("phone", "");
                                    userData.put("address", "");
                                    userData.put("finalScript", "");


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




    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == RESULT_OK) {
                Task<GoogleSignInAccount> accountTask = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                try {
                    GoogleSignInAccount signInAccount = accountTask.getResult(ApiException.class);
                    AuthCredential authCredential = GoogleAuthProvider.getCredential(signInAccount.getIdToken(), null);
                    mAuth.signInWithCredential(authCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, get Firebase user
                                FirebaseUser user = mAuth.getCurrentUser();

                                // Store user data in Firestore
                                Map<String, Object> userData = new HashMap<>();
                                userData.put("username", mAuth.getCurrentUser().getDisplayName());
                                userData.put("email", mAuth.getCurrentUser().getEmail());
                                userData.put("phone", "");
                                userData.put("address", "");
                                userData.put("finalScript", "");


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
                } catch (ApiException e) {
                    e.printStackTrace();
                }
            }
        }
    });

    private void register() {
        String email = editTextEmail.getText().toString();
        String password = editTextPassword.getText().toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(RegistrationActivity.this, "Enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        navigateToMain();
                    } else {
                        Toast.makeText(RegistrationActivity.this, "Registration failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateToMain() {
        Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
