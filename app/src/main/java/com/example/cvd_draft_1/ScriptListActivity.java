package com.example.cvd_draft_1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ScriptListActivity extends AppCompatActivity {

    private RecyclerView recyclerViewScripts;
    private ScriptAdapter scriptAdapter;
    private List<Script> scriptList;

    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_script_list);

        // Initialize Firebase Firestore and Auth
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Initialize UI elements
        recyclerViewScripts = findViewById(R.id.rvScripts);
        recyclerViewScripts.setLayoutManager(new LinearLayoutManager(this));

        TextView btnBack = findViewById(R.id.btnBack);
        TextView btnAddNew = findViewById(R.id.btnAddNew);

        // Set click listeners for navigation
        btnBack.setOnClickListener(v -> finish()); // Go back to the previous activity
        btnAddNew.setOnClickListener(v -> {
            Intent intent = new Intent(ScriptListActivity.this, ScriptActivity.class); // Navigate to script creation activity
            startActivity(intent);
        });

        // Initialize the script list and adapter
        scriptList = new ArrayList<>();
        scriptAdapter = new ScriptAdapter(this, scriptList);
        recyclerViewScripts.setAdapter(scriptAdapter);

        // Load scripts from Firestore
        loadScriptsFromFirestore();
    }

    private void loadScriptsFromFirestore() {
        if (currentUser == null) {
            Toast.makeText(ScriptListActivity.this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        // Reference to the user's scripts collection
        CollectionReference scriptsRef = db.collection("users").document(currentUser.getUid()).collection("scripts");

        // Retrieve scripts from Firestore and listen for changes
        scriptsRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot value, FirebaseFirestoreException error) {
                if (error != null) {
                    Toast.makeText(ScriptListActivity.this, "Failed to load scripts.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Clear the current list and repopulate with updated data
                scriptList.clear();
                if (value != null) {
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        Script script = doc.toObject(Script.class);
                        if (script != null) {
                            script.setId(doc.getId()); // Set the document ID to the script
                            scriptList.add(script);
                        }
                    }
                }

                // Notify the adapter of the data changes
                scriptAdapter.notifyDataSetChanged();
            }
        });
    }
}
