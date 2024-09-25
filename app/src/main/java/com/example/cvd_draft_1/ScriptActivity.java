package com.example.cvd_draft_1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.cvd_draft_1.api.OpenAIRequest;
import com.example.cvd_draft_1.api.OpenAIResponse;
import com.example.cvd_draft_1.api.RetrofitClient;
import java.util.ArrayList;
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
        generateVideoResume(answers);
    }

    private void updateQuestion() {
        questionTextView.setText(questions.get(currentQuestionIndex));
        answerEditText.setText(answers.get(currentQuestionIndex));
        btnPrevious.setEnabled(currentQuestionIndex > 0);


        if (currentQuestionIndex == questions.size() - 1) {
            btnNext.setVisibility(View.GONE);
        } else {
            btnNext.setVisibility(View.VISIBLE);
        }
    }

    private void saveAnswer() {
        answers.set(currentQuestionIndex, answerEditText.getText().toString().trim());
    }

    // Generate the video script resume by combining all the user's answers
    private void generateVideoResume(ArrayList<String> answers) {
        // Create a structured prompt to generate a video resume script
        StringBuilder scriptPrompt = new StringBuilder();
        scriptPrompt.append("Create a professional video resume script using the following information. Please format it as a resume introduction without including the original questions. Organize it as a narrative for the user to speak, covering the following sections:\n\n");
        scriptPrompt.append("1. Introduction\n");
        scriptPrompt.append("2. Professional Background\n");
        scriptPrompt.append("3. Key Skills\n");
        scriptPrompt.append("4. Project or Achievement\n");
        scriptPrompt.append("5. Career Goals\n\n");

        // Add answers to the prompt
        scriptPrompt.append("Introduction: ").append(answers.get(0)).append("\n");
        scriptPrompt.append("Professional Background: ").append(answers.get(1)).append("\n");
        scriptPrompt.append("Key Skills: ").append(answers.get(2)).append("\n");
        scriptPrompt.append("Project or Achievement: ").append(answers.get(3)).append("\n");
        scriptPrompt.append("Career Goals: ").append(answers.get(4)).append("\n");

        // Prepare the message list for the chat-based model
        List<OpenAIRequest.Message> messages = new ArrayList<>();
        messages.add(new OpenAIRequest.Message("system", "You are a helpful assistant that writes video resumes."));
        messages.add(new OpenAIRequest.Message("user", scriptPrompt.toString()));

        // Create the request object
        OpenAIRequest request = new OpenAIRequest("gpt-3.5-turbo", messages, 300);

        // Send the request to OpenAI and handle the response
        RetrofitClient.getService().createCompletion(request).enqueue(new Callback<OpenAIResponse>() {
            @Override
            public void onResponse(Call<OpenAIResponse> call, Response<OpenAIResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Get the generated video resume script
                    String script = response.body().getChoices().get(0).getMessage().getContent();
                    showScript(script);
                } else {
                    showScript("Error generating script: " + response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<OpenAIResponse> call, Throwable t) {
                showScript("Failed to generate script: " + t.getMessage());
            }
        });
    }

    private void showScript(String script) {
        Intent intent = new Intent(ScriptActivity.this, ScriptDisplayActivity.class);
        intent.putExtra("GENERATED_SCRIPT", script);
        startActivity(intent);
    }
}
