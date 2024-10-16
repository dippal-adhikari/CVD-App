package com.example.cvd_draft_1;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class VideoActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private VideoAdapter videoAdapter;
    private List<StorageReference> videoList;
    private StorageReference userVideoStorageRef;
    ImageButton btnBack;  // Declare the back button


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        // Initialize the ImageButton for the back button
        btnBack = findViewById(R.id.btnBack);

        // Set click listener for the back button to finish the current activity
        btnBack.setOnClickListener(v -> finish());

        // Handle add new button click
        TextView btnAddNew = findViewById(R.id.btnAddNew);
        btnAddNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VideoActivity.this, ScriptListActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerViewVideos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        videoList = new ArrayList<>();
        videoAdapter = new VideoAdapter(videoList, this);
        recyclerView.setAdapter(videoAdapter);

        // Get the current user's UID
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Reference to the user's video folder in Firebase Storage
        userVideoStorageRef = FirebaseStorage.getInstance().getReference().child("users/" + userId + "/videos");

        // Fetch list of videos
        fetchVideoList();
    }

    private void fetchVideoList() {
        userVideoStorageRef.listAll().addOnSuccessListener(listResult -> {
            videoList.clear();
            videoList.addAll(listResult.getItems());
            videoAdapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> {
            Log.e("Firebase", "Failed to fetch videos: " + e.getMessage());
        });
    }
}


