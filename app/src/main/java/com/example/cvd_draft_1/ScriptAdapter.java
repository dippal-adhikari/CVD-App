package com.example.cvd_draft_1;

import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ScriptAdapter extends RecyclerView.Adapter<ScriptAdapter.ScriptViewHolder> {
    private List<Script> scriptList;
    private Context context;
    private FirebaseFirestore db;
    private String userId;

    // Constructor for the adapter
    public ScriptAdapter(Context context, List<Script> scriptList) {
        this.context = context;
        this.scriptList = scriptList;
        this.userId = userId;  // Get userId as a parameter
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ScriptViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Use the custom script item layout
        View view = LayoutInflater.from(context).inflate(R.layout.item_script, parent, false);
        return new ScriptViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScriptViewHolder holder, int position) {
        Script script = scriptList.get(position);

        // Format the timestamp to a readable date
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm a");
        String formattedDate = sdf.format(new Date(script.getCreatedAt()));

        holder.tvCreatedAt.setText(formattedDate);

//        // Display questions
//        StringBuilder questionsText = new StringBuilder();
//        for (String question : script.getQuestions()) {
//            questionsText.append(question).append("\n");
//        }
//        holder.tvQuestions.setText(questionsText.toString());

// Clear any existing views in the question container to prevent duplication
        holder.questionContainer.removeAllViews();

        // Dynamically create TextViews with bullet points for each question
        for (String question : script.getQuestions()) {
            // Create a container for each question with a bullet and text
            LinearLayout questionLayout = new LinearLayout(context);
            questionLayout.setOrientation(LinearLayout.HORIZONTAL);
            questionLayout.setPadding(0, 8, 0, 8); // Padding for each question

            // Create the bullet image view
            ImageView bullet = new ImageView(context);
            LinearLayout.LayoutParams bulletParams = new LinearLayout.LayoutParams(16, 16); // Set bullet size
            bulletParams.setMargins(0, 0, 16, 0); // Margin between bullet and question text
            bullet.setLayoutParams(bulletParams);
            bullet.setImageResource(R.drawable.bullet_drawable); // Use your custom bullet drawable

            // Align the bullet image vertically with the text
            bullet.setScaleType(ImageView.ScaleType.CENTER_INSIDE); // Ensures alignment with the text

            // Create the TextView for question
            TextView questionText = new TextView(context);
            questionText.setText(question);
            questionText.setTextSize(14);
            questionText.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER_VERTICAL)); // Aligns the text vertically in the parent container

            // Center the text and bullet in the horizontal layout
            questionLayout.setGravity(Gravity.CENTER_VERTICAL); // Sets vertical alignment for both bullet and text

            // Add bullet and question text to the horizontal container
            questionLayout.addView(bullet);
            questionLayout.addView(questionText);

            // Add this horizontal layout to the main question container
            holder.questionContainer.addView(questionLayout);
        }


        // Set onClick listener for Record Video button
        holder.btnRecordVideo.setOnClickListener(v -> {
            Intent intent = new Intent(context, ScriptDisplayActivity.class);
            intent.putExtra("SCRIPT_ID", script.getId());
            intent.putStringArrayListExtra("QUESTIONS", new ArrayList<>(script.getQuestions()));
            intent.putStringArrayListExtra("ANSWERS", new ArrayList<>(script.getAnswers()));
            context.startActivity(intent);
        });


        holder.btnDelete.setOnClickListener(v -> {
            // Get the current user's ID dynamically
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                String userId = currentUser.getUid();

                // Delete the script from Firestore using the correct path
                db.collection("users")
                        .document(userId) // Use the authenticated user's ID
                        .collection("scripts")
                        .document(script.getId())
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(context, "Script deleted successfully!", Toast.LENGTH_SHORT).show();
                            scriptList.remove(position);
                            notifyItemRemoved(position);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(context, "Failed to delete script: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return scriptList.size();
    }

    public static class ScriptViewHolder extends RecyclerView.ViewHolder {
        TextView tvCreatedAt;
        LinearLayout questionContainer;
        Button btnRecordVideo;
        ImageView btnDelete;

        public ScriptViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);
            questionContainer = itemView.findViewById(R.id.questionContainer);
            btnRecordVideo = itemView.findViewById(R.id.btnRecordVideo);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
