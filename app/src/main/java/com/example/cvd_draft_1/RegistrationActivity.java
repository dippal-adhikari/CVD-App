package com.example.cvd_draft_1;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.google.firebase.firestore.DocumentSnapshot;
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
    ImageButton btnBack;  // Declare the back button



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
        //        ThemeUtils.applySavedTheme(this);
        setContentView(R.layout.activity_registration);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();  // Initialize Firestore
        editTextUsername = findViewById(R.id.etName);
        editTextEmail = findViewById(R.id.etEmail);
        editTextPassword = findViewById(R.id.etPassword);



        Button buttonReg = findViewById(R.id.btnReg);

        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(RegistrationActivity.this, options);

        // google sign in
        Button googleSignInButton = findViewById(R.id.sign_in_button);
        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = googleSignInClient.getSignInIntent();
                activityResultLauncher.launch(intent);
            }
        });

//        if (mAuth.getCurrentUser() != null) {
//            Glide.with(RegistrationActivity.this).load(Objects.requireNonNull(mAuth.getCurrentUser()).getPhotoUrl()).into(imageView);
//            name.setText(mAuth.getCurrentUser().getDisplayName());
//            mail.setText(mAuth.getCurrentUser().getEmail());
//        }

        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());


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
//                register();
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
                                    if (user != null) {
                                        // Now check if the user data exists in Firestore
                                        DocumentReference docRef = db.collection("users").document(user.getUid());
                                        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    DocumentSnapshot document = task.getResult();
                                                    if (document != null && document.exists()) {
                                                        // User data already exists, update only missing fields
                                                        Map<String, Object> updatedFields = new HashMap<>();

                                                        if (!document.contains("phone")) {
                                                            updatedFields.put("phone", "");
                                                        }

                                                        if (!document.contains("address")) {
                                                            updatedFields.put("address", "");
                                                        }

                                                        // Update only if there are fields to update
                                                        if (!updatedFields.isEmpty()) {
                                                            db.collection("users").document(user.getUid())
                                                                    .update(updatedFields)
                                                                    .addOnSuccessListener(aVoid -> {
                                                                        Toast.makeText(RegistrationActivity.this, "Fields updated successfully.", Toast.LENGTH_SHORT).show();

                                                                    })
                                                                    .addOnFailureListener(e -> {
                                                                        Toast.makeText(RegistrationActivity.this, "Failed to update fields.", Toast.LENGTH_SHORT).show();
                                                                        // Navigate to main activity
                                                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                                                        startActivity(intent);
                                                                        finish();
                                                                    });
                                                        }
                                                    } else {
                                                        // If user data doesn't exist, create a new document with defaults
                                                        Map<String, Object> userData = new HashMap<>();
                                                        userData.put("username", user.getDisplayName());
                                                        userData.put("email", user.getEmail());
                                                        userData.put("phone", "");
                                                        userData.put("address", "");
                                                        userData.put("finalScript", "");

                                                        db.collection("users").document(user.getUid())
                                                                .set(userData)
                                                                .addOnSuccessListener(aVoid -> {
                                                                    Toast.makeText(RegistrationActivity.this, "Registration successful.", Toast.LENGTH_SHORT).show();
                                                                    navigateToMain();
                                                                })
                                                                .addOnFailureListener(e -> {
                                                                    Toast.makeText(RegistrationActivity.this, "Unsuccessful registration.", Toast.LENGTH_SHORT).show();
                                                                });
                                                    }
                                                } else {
                                                    Toast.makeText(RegistrationActivity.this, "Failed to check user data.", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                } else {
                                    Toast.makeText(RegistrationActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

            }
            });
    }



    // google signup
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
                                if (user != null) {
                                    // Reference to the user document in Firestore
                                    DocumentReference docRef = db.collection("users").document(user.getUid());

                                    // Check if the user's document already exists
                                    docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot document = task.getResult();
                                                if (document.exists()) {
                                                    // Document exists, update only missing fields
                                                    Map<String, Object> updatedFields = new HashMap<>();

                                                    // Check for missing fields and add them to the map
                                                    if (!document.contains("username")) {
                                                        updatedFields.put("username", user.getDisplayName());
                                                    }
                                                    if (!document.contains("email")) {
                                                        updatedFields.put("email", user.getEmail());
                                                    }
                                                    // Add any other fields that should be updated if missing
                                                    // e.g., phone, address, finalScript, etc.

                                                    // Update only if there are fields to update
                                                    if (!updatedFields.isEmpty()) {
                                                        docRef.update(updatedFields)
                                                                .addOnSuccessListener(aVoid -> {
                                                                    Toast.makeText(RegistrationActivity.this, "Fields updated successfully.", Toast.LENGTH_SHORT).show();
                                                                    // Navigate to main activity
                                                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                                                    startActivity(intent);
                                                                    finish();
                                                                })
                                                                .addOnFailureListener(e -> {
                                                                    Toast.makeText(RegistrationActivity.this, "Failed to update user data.", Toast.LENGTH_SHORT).show();
                                                                });
                                                    } else {
                                                        // Navigate to main activity if no fields needed to be updated
                                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                } else {
                                                    // Document doesn't exist, create new data
                                                    Map<String, Object> userData = new HashMap<>();
                                                    userData.put("username", user.getDisplayName());
                                                    userData.put("email", user.getEmail());
                                                    userData.put("phone", ""); // Add default or empty values
                                                    userData.put("address", "");
                                                    userData.put("finalScript", "");

                                                    docRef.set(userData)
                                                            .addOnSuccessListener(aVoid -> {
                                                                Toast.makeText(RegistrationActivity.this, "Registration successful.", Toast.LENGTH_SHORT).show();
                                                                // Navigate to main activity
                                                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                                                startActivity(intent);
                                                                finish();
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                Toast.makeText(RegistrationActivity.this, "Failed to save user data.", Toast.LENGTH_SHORT).show();
                                                            });
                                                }
                                            } else {
                                                Toast.makeText(RegistrationActivity.this, "Failed to check user data.", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            } else {
                                // If sign-in fails, display a message to the user
                                Toast.makeText(RegistrationActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            }
                        }

                    });
                } catch (ApiException e) {
                    e.printStackTrace();
                }
            }
        }
    });

//    private void register() {
//        String email = editTextEmail.getText().toString();
//        String password = editTextPassword.getText().toString();
//
//        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
//            Toast.makeText(RegistrationActivity.this, "Enter email and password", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        mAuth.createUserWithEmailAndPassword(email, password)
//                .addOnCompleteListener(this, task -> {
//                    if (task.isSuccessful()) {
//                        // Registration success, navigate to MainActivity
//                        Toast.makeText(RegistrationActivity.this, "Registration successful.", Toast.LENGTH_SHORT).show();
//                        FirebaseUser user = mAuth.getCurrentUser();
//                        navigateToMain();
//                    } else {
//                        // Task failed, log the exception message
//                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
//                        Toast.makeText(RegistrationActivity.this, "Registration failed: " + errorMessage, Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }


    private void navigateToMain() {
        Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
