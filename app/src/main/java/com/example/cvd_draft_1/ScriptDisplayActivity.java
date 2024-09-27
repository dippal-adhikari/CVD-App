package com.example.cvd_draft_1;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

public class ScriptDisplayActivity extends AppCompatActivity {

    private EditText editTextScript;
    private Button btnSaveScript;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_script_display);

        // Initialize views
        editTextScript = findViewById(R.id.editTextScript);
        btnSaveScript = findViewById(R.id.btnSaveScript);

        // Get the generated script from the intent
        String generatedScript = getIntent().getStringExtra("GENERATED_SCRIPT");

        // Display the script in the editable EditText
        if (generatedScript != null) {
            editTextScript.setText(generatedScript);
        }

        // Handle save button click (optional: Save functionality to be added)
        btnSaveScript.setOnClickListener(v -> {
            // Here you could save the edited script, for now, we'll just show a message
            String editedScript = editTextScript.getText().toString().trim();
            if (!editedScript.isEmpty()) {
                // Save the script or perform any desired operation here
                // Example: save to local storage or send it back to a server
                finish(); // Close activity after saving
            }
        });
    }
}
