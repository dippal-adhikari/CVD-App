package com.example.cvd_draft_1;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
public class ScriptDisplayActivity extends AppCompatActivity {

    private LinearLayout scriptContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_script_display);

        scriptContainer = findViewById(R.id.scriptContainer);

        // Retrieve the passed questions and answers from the Intent
        ArrayList<String> questions = getIntent().getStringArrayListExtra("QUESTIONS");
        ArrayList<String> answers = getIntent().getStringArrayListExtra("ANSWERS");

        if (questions != null && answers != null && questions.size() == answers.size()) {
            for (int i = 0; i < questions.size(); i++) {
                String question = questions.get(i);
                String answer = answers.get(i);
                addScriptEditText(question, answer);
            }
        }
    }

    // Method to add an EditText for each question and answer
    private void addScriptEditText(String question, String answer) {
        // Create a TextView for the question label
        TextView questionLabel = new TextView(this);
        questionLabel.setText(question);  // Display the question as a label
        questionLabel.setTextSize(16);
        questionLabel.setPadding(16, 16, 16, 8);

        // Create an EditText for the answer
        EditText editText = new EditText(this);
        editText.setText(answer);  // Set the answer as the initial text
        editText.setTextSize(16);
        editText.setPadding(16, 16, 16, 16);

        // Add the question label and the editable answer field to the layout
        scriptContainer.addView(questionLabel);
        scriptContainer.addView(editText);
    }
}