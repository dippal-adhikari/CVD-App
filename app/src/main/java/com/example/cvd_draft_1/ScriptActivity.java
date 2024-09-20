package com.example.cvd_draft_1;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cvd_draft_1.api.OpenAIRequest;
import com.example.cvd_draft_1.api.OpenAIResponse;
import com.example.cvd_draft_1.api.RetrofitClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

        // Initialize question and answer lists
        questions = new ArrayList<>();
        answers = new ArrayList<>();

        // Populate questions
        questions.add("Introduce yourself.");
        questions.add("What is your professional background?");
        questions.add("What are your key skills?");
        questions.add("Describe a project or achievement you are proud of.");
        questions.add("What are your career goals?");

        // Initialize empty answers for each question
        for (int i = 0; i < questions.size(); i++) {
            answers.add("");
        }

        updateQuestion();

        // Handle button clicks
        btnNext.setOnClickListener(v -> handleNext());
        btnPrevious.setOnClickListener(v -> handlePrevious());
        btnGenerateScript.setOnClickListener(v -> handleGenerateScript());
    }

    private void handleNext() {
        saveAnswer();
        if (currentQuestionIndex < questions.size() - 1) {
            currentQuestionIndex++;
            updateQuestion();
        }
    }

    private void handlePrevious() {
        saveAnswer();
        if (currentQuestionIndex > 0) {
            currentQuestionIndex--;
            updateQuestion();
        }
    }

    private void handleGenerateScript() {
        saveAnswer();
        generateVideoScript(answers);
    }

    private void updateQuestion() {
        questionTextView.setText(questions.get(currentQuestionIndex));
        answerEditText.setText(answers.get(currentQuestionIndex));
        btnPrevious.setEnabled(currentQuestionIndex > 0);
    }

    private void saveAnswer() {
        answers.set(currentQuestionIndex, answerEditText.getText().toString().trim());
    }

    // Generate the video script by combining all the user's answers and sending it to OpenAI
    private void generateVideoScript(ArrayList<String> answers) {
        StringBuilder prompt = new StringBuilder();
        for (int i = 0; i < questions.size(); i++) {
            prompt.append(questions.get(i)).append("\n").append(answers.get(i)).append("\n\n");
        }

        // Create the message to send to OpenAI's chat model
        List<OpenAIRequest.Message> messages = Collections.singletonList(new OpenAIRequest.Message("user", prompt.toString()));

        OpenAIRequest request = new OpenAIRequest("gpt-3.5-turbo", messages, 150);
        RetrofitClient.getService().createCompletion(request).enqueue(new Callback<OpenAIResponse>() {
            @Override
            public void onResponse(Call<OpenAIResponse> call, Response<OpenAIResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Extract the generated script from the API response
                    String script = response.body().getChoices().get(0).getMessage().getContent();
                    showScript(script);
                } else {
                    try {
                        // Log the error response from the API
                        String errorResponse = response.errorBody().string();
                        Log.e("API_ERROR", "Error generating script: " + errorResponse);
                        Toast.makeText(ScriptActivity.this, "Error generating script", Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Log.e("API_ERROR", "Error reading error body: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<OpenAIResponse> call, Throwable t) {
                Log.e("API_ERROR", "Failed to generate script: " + t.getMessage());
                Toast.makeText(ScriptActivity.this, "Failed to generate script", Toast.LENGTH_LONG).show();
            }
        });
    }

    // Navigate to ScriptDisplayActivity to show the generated script
    private void showScript(String script) {
        Intent intent = new Intent(ScriptActivity.this, ScriptDisplayActivity.class);
        intent.putExtra("GENERATED_SCRIPT", script);
        startActivity(intent);
    }
}
