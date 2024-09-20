package com.example.cvd_draft_1;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;

public class WorksActivity extends AppCompatActivity {
    FirebaseAuth mAuth;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable edge-to-edge layout
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        setContentView(R.layout.activity_works);

        loadLocale();

        // Initialize FirebaseAuth instance
        mAuth = FirebaseAuth.getInstance();

        // Handle system insets for edge-to-edge layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Button logic for "Looking for a Job?"
        Button btnJob = findViewById(R.id.btnJob);
        btnJob.setOnClickListener(view -> {
            Intent intent = new Intent(WorksActivity.this, LauncherActivity.class);
            startActivity(intent);
        });

        // Button logic for changing language
        Button btnChangeLanguage = findViewById(R.id.btnChangeLanguage);
        btnChangeLanguage.setOnClickListener(view -> changeLanguage());
    }

    private void changeLanguage() {
        final String languages[] = {"English", "français", "deutsch", "Italiano"};
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
    }

    private void setLocale(String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Configuration configuration = new Configuration();
        configuration.locale = locale;
        getBaseContext().getResources().updateConfiguration(configuration, getBaseContext().getResources().getDisplayMetrics());

        SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
        editor.putString("CVD-Draft-1", language);
        editor.apply();
    }

    private void loadLocale() {
        SharedPreferences preferences = getSharedPreferences("Settings", MODE_PRIVATE);
        String language = preferences.getString("CVD-Draft-1", "");
        setLocale(language);
    }
}
