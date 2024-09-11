package com.example.cvd_draft_1;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class ScriptActivity extends AppCompatActivity {

    private TextView questionTextView;
    private EditText answerEditText;
    private Button btnNext, btnPrevious, btnGenerateScript;
    private int currentQuestionIndex = 0;
    private ArrayList<String> questions;
    private ArrayList<String> answers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_script);

        // Initialize views
        questionTextView = findViewById(R.id.questionTextView);
        answerEditText = findViewById(R.id.answerEditText);
        btnNext = findViewById(R.id.btnNext);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnGenerateScript = findViewById(R.id.btnGenerateScript);

        // Initialize questions and answers
        questions = new ArrayList<>();
        answers = new ArrayList<>();

        // Add targeted questions for video script
        questions.add("Introduce yourself.");
        questions.add("What is your professional background?");
        questions.add("What are your key skills?");
        questions.add("Describe a project or achievement you are proud of.");
        questions.add("What are your career goals?");

        // Initialize empty answers list
        for (int i = 0; i < questions.size(); i++) {
            answers.add(""); // Initially empty answers
        }

        // Display the first question
        updateQuestion();

        // Handle next button click
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Save the current answer
                saveAnswer();

                // Move to the next question if there is one
                if (currentQuestionIndex < questions.size() - 1) {
                    currentQuestionIndex++;
                    updateQuestion();
                }
            }
        });

        // Handle previous button click
        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Save the current answer
                saveAnswer();

                // Move to the previous question if possible
                if (currentQuestionIndex > 0) {
                    currentQuestionIndex--;
                    updateQuestion();
                }
            }
        });

        // Handle generate script button click
        btnGenerateScript.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Save the last answer and generate the final script
                saveAnswer();
                String generatedScript = generateVideoScript();
                // Display the script (you can use a new activity or a dialog)
                displayScript(generatedScript);
            }
        });
    }

    // Update the question and current answer
    private void updateQuestion() {
        questionTextView.setText(questions.get(currentQuestionIndex));
        answerEditText.setText(answers.get(currentQuestionIndex));
        btnPrevious.setEnabled(currentQuestionIndex > 0); // Disable the "Previous" button on the first question
    }

    // Save the current answer
    private void saveAnswer() {
        String currentAnswer = answerEditText.getText().toString().trim();
        answers.set(currentQuestionIndex, currentAnswer);
    }

    // Generate the final video script using the answers provided by the user
    private String generateVideoScript() {
        StringBuilder script = new StringBuilder();

        script.append("1. Introduction:\n");
        script.append(answers.get(0)).append("\n\n");

        script.append("2. Professional Background:\n");
        script.append(answers.get(1)).append("\n\n");

        script.append("3. Key Skills:\n");
        script.append(answers.get(2)).append("\n\n");

        script.append("4. Project or Achievement:\n");
        script.append(answers.get(3)).append("\n\n");

        script.append("5. Career Goals:\n");
        script.append(answers.get(4)).append("\n\n");

        return script.toString();
    }

    // Display the generated script (you can customize how this is shown to the user)
    private void displayScript(String script) {
        // For example, print to console or show it in a dialog for now
        System.out.println(script);
        // You can open a new activity or dialog to show the script to the user
        // Alternatively, you can save the script as a file or show it in a TextView
    }
}
