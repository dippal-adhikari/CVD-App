package com.example.cvd_draft_1;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ScriptDisplayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_script_display);

        // Retrieve the generated script from the Intent
        String generatedScript = getIntent().getStringExtra("GENERATED_SCRIPT");

        // Find the TextView and display the script
        TextView scriptTextView = findViewById(R.id.scriptTextView);
        scriptTextView.setText(generatedScript);
    }
}
