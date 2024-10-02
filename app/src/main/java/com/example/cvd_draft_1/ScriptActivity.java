package com.example.cvd_draft_1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cvd_draft_1.api.OpenAIRequest;
import com.example.cvd_draft_1.api.OpenAIResponse;
import com.example.cvd_draft_1.api.RetrofitClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScriptActivity extends AppCompatActivity {

    private LinearLayout questionContainer;
    private Button btnSubmitQuestions, btnNext, btnPrevious, btnGenerateScript;
    private TextView questionTextView;
    private EditText answerEditText;
    private RadioGroup radioGroupStyle;
    private RadioButton radioCasual, radioProfessional, radioFriendly;

    private ArrayList<String> selectedQuestions;
    private ArrayList<String> answers;
    private Map<String, CheckBox> questionCheckBoxes;
    private int currentQuestionIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_script);

        // Initialize views
        questionContainer = findViewById(R.id.questionContainer);
        btnSubmitQuestions = findViewById(R.id.btnSubmitQuestions);
        btnNext = findViewById(R.id.btnNext);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnGenerateScript = findViewById(R.id.btnGenerateScript);
        questionTextView = findViewById(R.id.questionTextView);
        answerEditText = findViewById(R.id.answerEditText);
        radioGroupStyle = findViewById(R.id.radioGroupStyle);
        radioCasual = findViewById(R.id.radioCasual);
        radioProfessional = findViewById(R.id.radioProfessional);
        radioFriendly = findViewById(R.id.radioFriendly);

        // Set up questions and checkboxes
        ArrayList<String> questions = new ArrayList<>();
        questions.add("Introduce yourself.");
        questions.add("What is your professional background?");
        questions.add("What are your key skills?");
        questions.add("Describe a project or achievement you are proud of.");
        questions.add("What are your career goals?");
        questions.add("Where do you see yourself in five years?");
        questions.add("What motivates you?");
        questions.add("What are your strengths and weaknesses?");
        questions.add("Why are you a good fit for this role?");
        questions.add("How do you handle pressure or stress at work?");

        questionCheckBoxes = new HashMap<>();
        for (String question : questions) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(question);
            questionContainer.addView(checkBox);
            questionCheckBoxes.put(question, checkBox);
        }

        // Button to submit selected questions
        btnSubmitQuestions.setOnClickListener(v -> {
            selectedQuestions = new ArrayList<>();
            answers = new ArrayList<>();
            for (Map.Entry<String, CheckBox> entry : questionCheckBoxes.entrySet()) {
                if (entry.getValue().isChecked()) {
                    selectedQuestions.add(entry.getKey());
                    answers.add("");  // Prepare empty slots for answers
                }
            }
            if (selectedQuestions.isEmpty()) {
                Toast.makeText(ScriptActivity.this, "Please select at least one question.", Toast.LENGTH_SHORT).show();
            } else {
                showNextQuestion();
            }
        });

        // Button to go to the next question
        btnNext.setOnClickListener(v -> {
            saveAnswer();
            if (currentQuestionIndex < selectedQuestions.size() - 1) {
                currentQuestionIndex++;
                showNextQuestion();
            } else {
                // Show style selection (Casual, Professional, Friendly)
                showStyleSelection();
            }
        });

        // Button to go to the previous question
        btnPrevious.setOnClickListener(v -> {
            saveAnswer();
            if (currentQuestionIndex > 0) {
                currentQuestionIndex--;
                showNextQuestion();
            }
        });

        // Button to generate the final script
        btnGenerateScript.setOnClickListener(v -> generateScript());
    }

    private void showNextQuestion() {
        questionContainer.setVisibility(View.GONE);
        btnSubmitQuestions.setVisibility(View.GONE);

        questionTextView.setVisibility(View.VISIBLE);
        answerEditText.setVisibility(View.VISIBLE);
        btnNext.setVisibility(View.VISIBLE);
        btnPrevious.setVisibility(View.VISIBLE);

        questionTextView.setText(selectedQuestions.get(currentQuestionIndex));
        answerEditText.setText(answers.get(currentQuestionIndex));
        btnPrevious.setEnabled(currentQuestionIndex > 0);
    }

    private void saveAnswer() {
        answers.set(currentQuestionIndex, answerEditText.getText().toString().trim());
    }

    private void showStyleSelection() {
        questionTextView.setVisibility(View.GONE);
        answerEditText.setVisibility(View.GONE);
        btnNext.setVisibility(View.GONE);
        btnPrevious.setVisibility(View.GONE);

        radioGroupStyle.setVisibility(View.VISIBLE);
        btnGenerateScript.setVisibility(View.VISIBLE);
    }

//    private void generateScript() {
//        int selectedStyleId = radioGroupStyle.getCheckedRadioButtonId();
//        String selectedStyle = "";
//
//        if (selectedStyleId == R.id.radioCasual) {
//            selectedStyle = "Casual";
//        } else if (selectedStyleId == R.id.radioProfessional) {
//            selectedStyle = "Professional";
//        } else if (selectedStyleId == R.id.radioFriendly) {
//            selectedStyle = "Friendly";
//        }
//
//        if (selectedStyle.isEmpty()) {
//            Toast.makeText(this, "Please select a script style.", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        // Build a structured script prompt based on the selected style
//        StringBuilder scriptPrompt = new StringBuilder();
//        scriptPrompt.append("Please respond in the format {\"question1\":\"answer1\",\"question2\":\"answer2\", ...} ");
//        scriptPrompt.append("Generate the answers in a " + selectedStyle.toLowerCase() + " tone.\n\n");
//
////        scriptPrompt.append("Create a video interview script in a ").append(selectedStyle.toLowerCase()).append(" tone. ");
////        scriptPrompt.append("The script should be generated as if the user is answering the questions directly to the camera, in the first person. ");
////        scriptPrompt.append("Do not include instructions on background or camera setup. Just provide a script that the user can use for recording their video interview.\n\n");
//
//
//        // Append each question and answer to the structured prompt
//        scriptPrompt.append("User's input:\n");
//        for (int i = 0; i < selectedQuestions.size(); i++) {
//            scriptPrompt.append(selectedQuestions.get(i)).append(": ").append(answers.get(i)).append("\n");
//        }
//
//        // Create the OpenAI request
//        List<OpenAIRequest.Message> messages = new ArrayList<>();
//        messages.add(new OpenAIRequest.Message("system", "You are an assistant that generates structured responses based on user input."));
//        messages.add(new OpenAIRequest.Message("user", scriptPrompt.toString()));
//
//        OpenAIRequest request = new OpenAIRequest("gpt-3.5-turbo", messages, 300);
//
//        RetrofitClient.getService().createCompletion(request).enqueue(new Callback<OpenAIResponse>() {
//            @Override
//            public void onResponse(Call<OpenAIResponse> call, Response<OpenAIResponse> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    String structuredScript = response.body().getChoices().get(0).getMessage().getContent();
//                    try {
//                        Map<String, String> scriptMap = parseResponseToMap(structuredScript);
//
//                        // Create separate ArrayLists for questions and answers
//                        ArrayList<String> questionsList = new ArrayList<>(scriptMap.keySet());
//                        ArrayList<String> answersList = new ArrayList<>(scriptMap.values());
//
//                        // Send the questions and answers to the next activity
//                        Intent intent = new Intent(ScriptActivity.this, ScriptDisplayActivity.class);
//                        intent.putStringArrayListExtra("QUESTIONS", questionsList);
//                        intent.putStringArrayListExtra("ANSWERS", answersList);
//                        startActivity(intent);
//
//                    } catch (Exception e) {
//                        Toast.makeText(ScriptActivity.this, "Error parsing the script.", Toast.LENGTH_SHORT).show();
//                    }
//                } else {
//                    Toast.makeText(ScriptActivity.this, "Error generating script.", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<OpenAIResponse> call, Throwable t) {
//                Toast.makeText(ScriptActivity.this, "Failed to generate script.", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

    private void generateScript() {
        int selectedStyleId = radioGroupStyle.getCheckedRadioButtonId();
        String selectedStyle = "";

        if (selectedStyleId == R.id.radioCasual) {
            selectedStyle = "Casual";
        } else if (selectedStyleId == R.id.radioProfessional) {
            selectedStyle = "Professional";
        } else if (selectedStyleId == R.id.radioFriendly) {
            selectedStyle = "Friendly";
        }

        if (selectedStyle.isEmpty()) {
            Toast.makeText(this, "Please select a script style.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Build a personalized script prompt based on the selected style
        StringBuilder scriptPrompt = new StringBuilder();
//        scriptPrompt.append("").append(selectedStyle.toLowerCase()).append(" tone. ");
//        scriptPrompt.append("The script should be generated as if the user is answering the questions directly to the camera, in the first person. ");
//        scriptPrompt.append("Do not include instructions on background or camera setup. Just provide a script that the user can use for recording their video interview.\n\n");

//        scriptPrompt.append("Please respond  to the prompted questions in the format {\"question1\":\"answer1\",\"question2\":\"answer2\", ...}");
//        scriptPrompt.append("Create a video interview script answers in a " + selectedStyle.toLowerCase() + " tone. ");

        scriptPrompt.append("For a Video CV app, create script like in CV for the prompted answers in the format {\"question1\":\"answer1\",\"question2\":\"answer2\", ...} in a \" + selectedStyle.toLowerCase() + \" tone. \"");
        scriptPrompt.append("Replace questions titles with actual questions (Don't change a single letter for questions) and replace answers titles with actual answer scripts. The length should be around 75 words.");

        // Add the user-provided answers to the prompt in a structured format
        scriptPrompt.append("Here is the user's input:\n");
        for (int i = 0; i < selectedQuestions.size(); i++) {
            scriptPrompt.append(selectedQuestions.get(i)).append(": ").append(answers.get(i)).append("\n");
        }

        // Create the OpenAI request
        List<OpenAIRequest.Message> messages = new ArrayList<>();
        messages.add(new OpenAIRequest.Message("system", "You are an assistant generating personalized video interview scripts based on the user's input."));
        messages.add(new OpenAIRequest.Message("user", scriptPrompt.toString()));

        OpenAIRequest request = new OpenAIRequest("gpt-3.5-turbo", messages, 300);

        RetrofitClient.getService().createCompletion(request).enqueue(new Callback<OpenAIResponse>() {
            @Override
            public void onResponse(Call<OpenAIResponse> call, Response<OpenAIResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String structuredScript = response.body().getChoices().get(0).getMessage().getContent();
                    try {
                        Map<String, String> scriptMap = parseResponseToMap(structuredScript);

                        // Create separate ArrayLists for questions and answers
                        ArrayList<String> questionsList = new ArrayList<>(scriptMap.keySet());
                        ArrayList<String> answersList = new ArrayList<>(scriptMap.values());

                        // Send the questions and answers to the next activity
                        Intent intent = new Intent(ScriptActivity.this, ScriptDisplayActivity.class);
                        intent.putStringArrayListExtra("QUESTIONS", questionsList);
                        intent.putStringArrayListExtra("ANSWERS", answersList);
                        startActivity(intent);

                    } catch (Exception e) {
                        Toast.makeText(ScriptActivity.this, "Error parsing the script.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ScriptActivity.this, "Error generating script.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<OpenAIResponse> call, Throwable t) {
                Toast.makeText(ScriptActivity.this, "Failed to generate script.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Map<String, String> parseResponseToMap(String structuredResponse) throws JSONException {
        Map<String, String> resultMap = new HashMap<>();
        JSONObject jsonObject = new JSONObject(structuredResponse);

        Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            resultMap.put(key, jsonObject.getString(key));
        }
        return resultMap;
    }

}
