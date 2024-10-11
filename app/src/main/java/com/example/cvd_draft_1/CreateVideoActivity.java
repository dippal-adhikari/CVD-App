package com.example.cvd_draft_1;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import androidx.camera.video.FileOutputOptions;
import androidx.camera.video.Quality;
import androidx.camera.video.QualitySelector;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.camera.video.VideoCapture;
import androidx.camera.video.VideoRecordEvent;



public class CreateVideoActivity extends AppCompatActivity {

    // UI Elements
    private TextView scriptTextView, recordingTime;
    private CircularProgressIndicator recordingProgressBar;
    private SeekBar zoomSeekBar, exposureSeekBar;
    private Spinner videoQualitySpinner;

    // CameraX and Recording related fields
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private boolean isRecording = false;
    private VideoCapture<Recorder> videoCapture;
    private Recording recording;
    private CameraSelector cameraSelector;
    private Camera camera;
    private CameraManager cameraManager;
    private Handler handler;
    private Runnable updateRecordingTimeRunnable, scriptScrollRunnable;
    private long startTime;
    private boolean isBackCamera = true;
    private boolean isFlashlightOn = false;

    // Firebase-related fields
    private StorageReference videoStorageReference;

    // Script-related data
    private String scriptId;
    private List<String> questions;
    private List<String> answers;
    private int currentClipIndex = 0;
    private List<File> recordedClips = new ArrayList<>();

    private int currentScrollY = 0; // Track current scroll position
    private int scrollSpeed = 5; // Adjust scroll speed as needed
    private ScrollView scriptScrollView; // ScrollView for the script


    TextView speedPercentageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_video);

        // Initialize Firebase Storage
        videoStorageReference = FirebaseStorage.getInstance().getReference().child("videos");

        // Retrieve data passed via Intent
        questions = getIntent().getStringArrayListExtra("QUESTIONS");
        answers = getIntent().getStringArrayListExtra("ANSWERS");

        if (questions == null || answers == null) {
            Toast.makeText(this, "Script data not found!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize UI elements

        scriptTextView = findViewById(R.id.script_text);
        recordingTime = findViewById(R.id.recording_time);
        recordingProgressBar = findViewById(R.id.recording_progress_bar);
        FloatingActionButton fabRecording = findViewById(R.id.fab_recording);
        Button rotateButton = findViewById(R.id.rotateButton);
        Button toggleFlashlightButton = findViewById(R.id.toggle_flashlight);
        Button toggleStabilizationButton = findViewById(R.id.toggle_stabilization);
        FloatingActionButton showBottomSheetButton = findViewById(R.id.show_bottom_sheet_button);
        LinearLayout bottomSheet = findViewById(R.id.bottom_sheet);
        bottomSheet.setVisibility(View.GONE);
        scriptScrollView = findViewById(R.id.script_scroll_view);

        // Camera Controls
        zoomSeekBar = findViewById(R.id.zoomSeekBar);
        exposureSeekBar = findViewById(R.id.exposure_control);
        videoQualitySpinner = findViewById(R.id.video_quality);


        // Speed control buttons and TextView
        Button btnDecreaseSpeed = findViewById(R.id.btn_decrease_speed);
        Button btnIncreaseSpeed = findViewById(R.id.btn_increase_speed);
        speedPercentageView = findViewById(R.id.speed_percentage);

        // Display initial speed percentage
//        speedPercentageView.setText(scrollSpeed + "%");

        // Decrease speed button click listener
        btnDecreaseSpeed.setOnClickListener(v -> {
            if (scrollSpeed > 1) {  // Limit to 10%
                scrollSpeed -= 1;  // Decrease by 10%
                updateSpeedDisplay();
            }
        });

// Increase speed button click listener
        btnIncreaseSpeed.setOnClickListener(v -> {
            if (scrollSpeed < 10) {  // Limit to 100%
                scrollSpeed += 1;  // Increase by 10%
                updateSpeedDisplay();
            }
        });



        handler = new Handler(Looper.getMainLooper());
        updateRecordingTimeRunnable = new Runnable() {
            @Override
            public void run() {
                if (isRecording) {
                    long elapsedTime = System.currentTimeMillis() - startTime;
                    int seconds = (int) (elapsedTime / 1000) % 60;
                    int minutes = (int) (elapsedTime / (1000 * 60)) % 60;
                    int hours = (int) (elapsedTime / (1000 * 60 * 60)) % 24;
                    recordingTime.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
                    recordingProgressBar.setProgress((int) (elapsedTime / 1000));
                    handler.postDelayed(this, 1000);
                }
            }
        };

        scriptScrollRunnable = new Runnable() {
            @Override
            public void run() {
                if (isRecording) {
                    currentScrollY += scrollSpeed;
                    scriptScrollView.scrollTo(0, currentScrollY);
                    handler.postDelayed(this, 100); // Adjust the delay to control scroll speed
                }
            }
        };

        fabRecording.setOnClickListener(v -> {
            if (isRecording) {
                stopRecording();
            } else {
                startRecording();
            }
        });

        // Rotate Camera Button
        rotateButton.setOnClickListener(v -> {
            isBackCamera = !isBackCamera;
            startCamera();
        });

        // Toggle Flashlight
        toggleFlashlightButton.setOnClickListener(v -> toggleFlashlight());

        // Toggle Stabilization
        toggleStabilizationButton.setOnClickListener(v -> toggleStabilization());

        showBottomSheetButton.setOnClickListener(v -> {
            if (bottomSheet.getVisibility() == View.GONE) {
                bottomSheet.setVisibility(View.VISIBLE);
            } else {
                bottomSheet.setVisibility(View.GONE);
            }
        });

        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        startCamera();
                    } else {
                        Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }

        handler = new Handler(Looper.getMainLooper());
        updateRecordingTimeRunnable = new Runnable() {
            @Override
            public void run() {
                if (isRecording) {
                    long elapsedTime = System.currentTimeMillis() - startTime;
                    int seconds = (int) (elapsedTime / 1000) % 60;
                    int minutes = (int) (elapsedTime / (1000 * 60)) % 60;
                    int hours = (int) (elapsedTime / (1000 * 60 * 60)) % 24;
                    recordingTime.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
                    recordingProgressBar.setProgress((int) (elapsedTime / 1000));
                    handler.postDelayed(this, 1000);
                }
            }
        };

        displayNextScript(); // Display the first script question and answer
    }

    // Method to update the speed percentage display
    private void updateSpeedDisplay() {
        int percentage = (scrollSpeed * 10);  // Convert speed to percentage
        speedPercentageView.setText(percentage + "%");  // Display the percentage
    }

    private void toggleFlashlight() {
        if (camera != null) {
            camera.getCameraControl().enableTorch(!isFlashlightOn);
            isFlashlightOn = !isFlashlightOn;
        }
    }

    private void toggleStabilization() {
        Toast.makeText(this, "Stabilization toggled.", Toast.LENGTH_SHORT).show();
    }

    private void startRecording() {
        String fileName = "clip_" + currentClipIndex + ".mp4";
        File clipFile = new File(getExternalFilesDir(null), fileName);
        recording = videoCapture.getOutput()
                .prepareRecording(this, new FileOutputOptions.Builder(clipFile).build())
                .start(ContextCompat.getMainExecutor(this), videoRecordEvent -> {
                    if (videoRecordEvent instanceof VideoRecordEvent.Start) {
                        isRecording = true;
                        startTime = System.currentTimeMillis();
                        handler.post(updateRecordingTimeRunnable);
                        startScrollingScript();
                    } else if (videoRecordEvent instanceof VideoRecordEvent.Finalize) {
                        isRecording = false;
                        handler.removeCallbacks(updateRecordingTimeRunnable);
                        handler.removeCallbacks(scriptScrollRunnable); // Stop scrolling when recording ends
                        if (!((VideoRecordEvent.Finalize) videoRecordEvent).hasError()) {
                            recordedClips.add(clipFile);
                            currentClipIndex++;  // Move to the next question
                            displayNextScript();
                        }
                    }
                });
    }

    private void stopRecording() {
        if (recording != null) {
            recording.stop();
            recording = null;
        }
        handler.removeCallbacks(scriptScrollRunnable);
    }

    private void startScrollingScript() {
        handler.post(scriptScrollRunnable);
    }



    private void displayNextScript() {
        if (currentClipIndex < questions.size()) {
            String scriptText = "Q: " + questions.get(currentClipIndex) + "\n\nA: " + answers.get(currentClipIndex);
            scriptTextView.setText(scriptText);
        } else {
            Toast.makeText(this, "All clips recorded.", Toast.LENGTH_SHORT).show();
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Log.e("CameraXApp", "Camera provider initialization failed.", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        PreviewView previewView = findViewById(R.id.previewView);
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(isBackCamera ? CameraSelector.LENS_FACING_BACK : CameraSelector.LENS_FACING_FRONT)
                .build();

        Recorder recorder = new Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
                .build();
        videoCapture = VideoCapture.withOutput(recorder);

        cameraProvider.unbindAll();
        camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, videoCapture);
    }
}

//
//package com.example.cvd_draft_1;
//
//import android.app.AlertDialog;
//import android.widget.EditText;
//import android.Manifest;
//import android.content.pm.PackageManager;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Looper;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//import android.widget.LinearLayout;
//import android.widget.SeekBar;
//import android.widget.Spinner;
//import android.widget.ArrayAdapter;
//import android.widget.AdapterView;
//import android.widget.Toast;
//import androidx.activity.result.ActivityResultLauncher;
//import androidx.activity.result.contract.ActivityResultContracts;
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.camera.core.Camera;
//import androidx.camera.core.CameraSelector;
//import androidx.camera.core.Preview;
//import androidx.camera.lifecycle.ProcessCameraProvider;
//import androidx.camera.view.PreviewView;
//import androidx.core.content.ContextCompat;
//import com.google.android.material.floatingactionbutton.FloatingActionButton;
//import com.google.android.material.progressindicator.CircularProgressIndicator;
//import com.google.common.util.concurrent.ListenableFuture;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.firestore.FirebaseFirestore;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.ExecutionException;
//import androidx.camera.video.Recorder;
//import androidx.camera.video.VideoCapture;
//import androidx.camera.video.Recording;
//import androidx.camera.video.VideoRecordEvent;
//import androidx.camera.video.QualitySelector;
//import androidx.camera.video.Quality;
//import androidx.camera.video.FileOutputOptions;
//import android.hardware.camera2.CameraAccessException;
//import android.hardware.camera2.CameraManager;
//import android.content.Intent;
//import android.widget.TextView;
//import com.google.firebase.firestore.DocumentReference;
//
//
//
//public class CreateVideoActivity extends AppCompatActivity {
//    public static final int STABILIZATION_MODE_ON = 1;
//    public static final int STABILIZATION_MODE_OFF = 0;
//
//    private ActivityResultLauncher<String> requestPermissionLauncher;
//    private boolean isRecording = false;
//    private VideoCapture<Recorder> videoCapture;
//    private Recording recording;
//    private CameraSelector cameraSelector;
//    private boolean isBackCamera = true;
//    private boolean isFlashlightOn = false;
//    private boolean isStabilizationOn = false;
//    private CameraManager cameraManager;
//    private String cameraId;
//    private Camera camera;
//    private SeekBar zoomSeekBar;
//    private SeekBar exposureSeekBar;
//    private Spinner videoQualitySpinner;
//    private Quality selectedQuality = Quality.HIGHEST;
//    private CircularProgressIndicator recordingProgressBar;
//    private TextView recordingTime, scriptTextView;
//    private Handler handler;
//    private Runnable updateRecordingTimeRunnable;
//    private long startTime;
//
//    // Firebase-related fields
//    private FirebaseFirestore db;
//    private FirebaseUser currentUser;
//    private String scriptId;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_create_video);
//
//        // Initialize UI components
//        Button toggleStabilizationButton = findViewById(R.id.toggle_stabilization);
//        scriptTextView = findViewById(R.id.script_text);
//
//        // Initialize Firebase
//        db = FirebaseFirestore.getInstance();
//        currentUser = FirebaseAuth.getInstance().getCurrentUser();
//
//        // Retrieve data passed via Intent
//        Intent intent = getIntent();
//        scriptId = intent.getStringExtra("SCRIPT_ID");
//        ArrayList<String> questions = intent.getStringArrayListExtra("QUESTIONS");
//        ArrayList<String> answers = intent.getStringArrayListExtra("ANSWERS");
//
//        // Check if script data is available from Intent
//        if (questions != null && answers != null) {
//            // Concatenate questions and answers into a formatted script
//            StringBuilder scriptBuilder = new StringBuilder();
//            for (int i = 0; i < questions.size(); i++) {
//                scriptBuilder.append("Q: ").append(questions.get(i)).append("\n");
//                scriptBuilder.append("A: ").append(answers.get(i)).append("\n\n");
//            }
//            scriptTextView.setText(scriptBuilder.toString()); // Set the concatenated script text
//        } else {
//            scriptTextView.setText("No script available.");
//        }
//
//
////        // Fetch and display the script content
////        loadScriptContent();
//
//        // Set up the button click listener for the video stabilization button
//        toggleStabilizationButton.setOnClickListener(v -> toggleStabilization());
//        // Set up the button click listener for the video stabilization button
//        toggleStabilizationButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                toggleStabilization();
//            }
//        });
//
//        requestPermissionLauncher = registerForActivityResult(
//                new ActivityResultContracts.RequestPermission(),
//                isGranted -> {
//                    if (isGranted) {
//                        startCamera();
//                    } else {
//                        Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
//                    }
//                }
//        );
//
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
//                == PackageManager.PERMISSION_GRANTED) {
//            startCamera();
//        } else {
//            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
//        }
//
//        FloatingActionButton fabRecording = findViewById(R.id.fab_recording);
//        FloatingActionButton showBottomSheetButton = findViewById(R.id.show_bottom_sheet_button);
//        LinearLayout bottomSheet = findViewById(R.id.bottom_sheet);
//        zoomSeekBar = findViewById(R.id.zoomSeekBar);
//        exposureSeekBar = findViewById(R.id.exposure_control);
//        videoQualitySpinner = findViewById(R.id.video_quality);
//        recordingProgressBar = findViewById(R.id.recording_progress_bar);
//        recordingTime = findViewById(R.id.recording_time);
//
//
//
//
//        List<String> supportedQualities = getSupportedQualities();
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, supportedQualities);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        videoQualitySpinner.setAdapter(adapter);
//
//        videoQualitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                String quality = parent.getItemAtPosition(position).toString();
//                switch (quality) {
//                    case "Lowest":
//                        selectedQuality = Quality.LOWEST;
//                        break;
//                    case "SD":
//                        selectedQuality = Quality.SD;
//                        break;
//                    case "HD":
//                        selectedQuality = Quality.HD;
//                        break;
//                    case "FHD":
//                        selectedQuality = Quality.FHD;
//                        break;
//                    case "UHD":
//                        selectedQuality = Quality.UHD;
//                        break;
//                    default:
//                        selectedQuality = Quality.HIGHEST;
//                        break;
//                }
//                startCamera();
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//                // Do nothing
//            }
//        });
//
//        fabRecording.setOnClickListener(v -> {
//            if (isRecording) {
//                stopRecording();
//            } else {
//                startRecording();
//            }
//        });
//
//        showBottomSheetButton.setOnClickListener(v -> {
//            if (bottomSheet.getVisibility() == View.GONE) {
//                bottomSheet.setVisibility(View.VISIBLE);
//            } else {
//                bottomSheet.setVisibility(View.GONE);
//            }
//        });
//
//        Button rotateButton = findViewById(R.id.rotateButton);
//        rotateButton.setOnClickListener(v -> {
//            isBackCamera = !isBackCamera;
//            startCamera();
//        });
//
//        Button toggleFlashlightButton = findViewById(R.id.toggle_flashlight);
//        cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
//
//        toggleFlashlightButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                toggleFlashlight();
//            }
//        });
//
//        zoomSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                if (camera != null) {
//                    float maxZoomRatio = camera.getCameraInfo().getZoomState().getValue().getMaxZoomRatio();
//                    float zoomRatio = progress / 100.0f * (maxZoomRatio - 1) + 1;
//                    camera.getCameraControl().setZoomRatio(zoomRatio);
//                }
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {}
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {}
//        });
//
//        exposureSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                if (camera != null) {
//                    float exposureCompensation = (progress - 50) / 50.0f; // Assuming the range is -1 to 1
//                    camera.getCameraControl().setExposureCompensationIndex((int) (exposureCompensation * camera.getCameraInfo().getExposureState().getExposureCompensationRange().getUpper()));
//                }
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {}
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {}
//        });
//
//        Button resetButton = findViewById(R.id.reset_button);
//        resetButton.setOnClickListener(v -> resetExposure());
//
//        Button resetZoomButton = findViewById(R.id.reset_zoom_button);
//        resetZoomButton.setOnClickListener(v -> resetZoom());
//
//        handler = new Handler(Looper.getMainLooper());
//        updateRecordingTimeRunnable = new Runnable() {
//            @Override
//            public void run() {
//                if (isRecording) {
//                    long elapsedTime = System.currentTimeMillis() - startTime;
//                    int seconds = (int) (elapsedTime / 1000) % 60;
//                    int minutes = (int) (elapsedTime / (1000 * 60)) % 60;
//                    int hours = (int) (eflapsedTime / (1000 * 60 * 60)) % 24;
//                    recordingTime.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
//                    recordingProgressBar.setProgress((int) (elapsedTime / 1000));
//                    handler.postDelayed(this, 1000);
//                }
//            }
//        };
//    }
//
//    private void loadScriptContent() {
//        if (scriptId != null && !scriptId.isEmpty() && currentUser != null) {
//            DocumentReference scriptRef = db.collection("users").document(currentUser.getUid()).collection("scripts").document(scriptId);
//            scriptRef.get().addOnSuccessListener(documentSnapshot -> {
//                if (documentSnapshot.exists()) {
//                    StringBuilder scriptDisplay = new StringBuilder();
//                    List<String> questions = (List<String>) documentSnapshot.get("questions");
//                    List<String> answers = (List<String>) documentSnapshot.get("answers");
//
//                    if (questions != null && answers != null) {
//                        for (int i = 0; i < questions.size(); i++) {
//                            scriptDisplay.append("Q").append(i + 1).append(": ").append(questions.get(i)).append("\n");
//                            scriptDisplay.append("A").append(i + 1).append(": ").append(answers.get(i)).append("\n\n");
//                        }
//                    }
//                    scriptTextView.setText(scriptDisplay.toString());
//                }
//            }).addOnFailureListener(e -> Toast.makeText(CreateVideoActivity.this, "Error loading script", Toast.LENGTH_SHORT).show());
//        }
//    }
//
//    private List<String> getSupportedQualities() {
//        List<String> supportedQualities = new ArrayList<>();
//        if (isQualitySupported(Quality.LOWEST)) supportedQualities.add("Lowest");
//        if (isQualitySupported(Quality.SD)) supportedQualities.add("SD");
//        if (isQualitySupported(Quality.HD)) supportedQualities.add("HD");
//        if (isQualitySupported(Quality.FHD)) supportedQualities.add("FHD");
//        if (isQualitySupported(Quality.UHD)) supportedQualities.add("UHD");
//        if (isQualitySupported(Quality.HIGHEST)) supportedQualities.add("Highest");
//        return supportedQualities;
//    }
//
//
//
//    private boolean isQualitySupported(Quality quality) {
//        // Check if the selected quality is supported by the device
//        // This is a placeholder implementation, you need to replace it with actual checks
//        // based on your device's capabilities
//        return true; // Replace with actual check
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        if (isRecording) {
//            stopRecording();
//        }
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        if (isRecording) {
//            stopRecording();
//        }
//    }
//
//    private void toggleFlashlight() {
//        try {
//            if (camera != null) {
//                camera.getCameraControl().enableTorch(!isFlashlightOn);
//                isFlashlightOn = !isFlashlightOn;
//            } else {
//                Toast.makeText(this, "Camera is not initialized", Toast.LENGTH_SHORT).show();
//            }
//        } catch (Exception e) {
//            Log.e("CameraXApp", "Error toggling flashlight", e);
//            Toast.makeText(this, "Error toggling flashlight: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    private void toggleStabilization() {
//        isStabilizationOn = !isStabilizationOn;
//        if (camera != null) {
//            // Enable or disable stabilization for the Video
//            Recorder recorder = new Recorder.Builder()
//                    .setQualitySelector(QualitySelector.from(selectedQuality))
//                    .build();
//            videoCapture = VideoCapture.withOutput(recorder);
//            Toast.makeText(this, "Stabilization " + (isStabilizationOn ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
//        } else {
//            Toast.makeText(this, "Camera is not initialized", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    private void startCamera() {
//        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
//        cameraProviderFuture.addListener(() -> {
//            try {
//                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
//                if (isQualitySupported(selectedQuality)) {
//                    bindPreview(cameraProvider);
//                } else {
//                    Toast.makeText(this, "Selected video quality is not supported by this device", Toast.LENGTH_SHORT).show();
//                }
//            } catch (ExecutionException | InterruptedException e) {
//                Log.e("CameraXApp", "Camera provider initialization failed.", e);
//            }
//        }, ContextCompat.getMainExecutor(this));
//    }
//
//    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
//        PreviewView previewView = findViewById(R.id.previewView);
//        Preview preview = new Preview.Builder().build();
//        preview.setSurfaceProvider(previewView.getSurfaceProvider());
//
//        cameraSelector = new CameraSelector.Builder()
//                .requireLensFacing(isBackCamera ? CameraSelector.LENS_FACING_BACK : CameraSelector.LENS_FACING_FRONT)
//                .build();
//        Recorder recorder = new Recorder.Builder()
//                .setQualitySelector(QualitySelector.from(selectedQuality))
//                .build();
//        videoCapture = VideoCapture.withOutput(recorder);
//
//        cameraProvider.unbindAll();
//        camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, videoCapture);
//    }
//
//    private void startRecording() {
//        showFileNameDialog();
//    }
//
//    private void showFileNameDialog() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("Enter file name");
//
//        final EditText input = new EditText(this);
//        builder.setView(input);
//
//        builder.setPositiveButton("OK", (dialog, which) -> {
//            String fileName = input.getText().toString();
//            if (!fileName.isEmpty()) {
//                File videoFile = new File(getExternalFilesDir(null), fileName + ".mp4");
//                try {
//                    recording = videoCapture.getOutput()
//                            .prepareRecording(this, new FileOutputOptions.Builder(videoFile).build())
//                            .start(ContextCompat.getMainExecutor(this), videoRecordEvent -> {
//                                if (videoRecordEvent instanceof VideoRecordEvent.Start) {
//                                    isRecording = true;
//                                    startTime = System.currentTimeMillis();
//                                    handler.post(updateRecordingTimeRunnable);
//                                    toggleRecordingButtons();
//                                } else if (videoRecordEvent instanceof VideoRecordEvent.Finalize) {
//                                    isRecording = false;
//                                    handler.removeCallbacks(updateRecordingTimeRunnable);
//                                    toggleRecordingButtons();
//                                    if (!((VideoRecordEvent.Finalize) videoRecordEvent).hasError()) {
//                                        Toast.makeText(this, "Recording saved: " + videoFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
//                                    } else {
//                                        Log.e("CameraXApp", "Recording error: " + ((VideoRecordEvent.Finalize) videoRecordEvent).getError());
//                                        handleRecordingError(videoFile);
//                                    }
//                                }
//                            });
//                } catch (Exception e) {
//                    Log.e("CameraXApp", "Error starting recording", e);
//                    Toast.makeText(this, "Error starting recording: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                    handleRecordingError(videoFile);
//                }
//            } else {
//                Toast.makeText(this, "File name cannot be empty", Toast.LENGTH_SHORT).show();
//            }
//        });
//        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
//
//        builder.show();
//    }
//
//    private void handleRecordingError(File videoFile) {
//        // Save the recording and reset the state
//        if (recording != null) {
//            recording.stop();
//            recording = null;
//        }
//        Toast.makeText(this, "Recording saved with errors: " + videoFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
//        resetToInitialState();
//    }
//
//    private void resetToInitialState() {
//        // Reset UI components and state variables to initial state
//        toggleRecordingButtons();
//        resetExposure();
//        resetZoom();
//        recordingTime.setText("00:00:00");
//        recordingProgressBar.setProgress(0);
//        isRecording = false;
//        isFlashlightOn = false;
//        isStabilizationOn = false;
//        startCamera();
//    }
//
//    private void stopRecording() {
//        if (recording != null) {
//            recording.stop();
//            recording = null;
//        }
//    }
//
//    private void toggleRecordingButtons() {
//        FloatingActionButton fabRecording = findViewById(R.id.fab_recording);
//        if (isRecording) {
//            fabRecording.setImageResource(R.drawable.ic_stop); // Change icon to stop
//        } else {
//            fabRecording.setImageResource(R.drawable.ic_record); // Change icon to record
//        }
//    }
//
//    private void resetExposure() {
//        if (camera != null) {
//            camera.getCameraControl().setExposureCompensationIndex(0);
//            exposureSeekBar.setProgress(50); // Reset SeekBar to the middle position
//        }
//    }
//
//    private void resetZoom() {
//        if (camera != null) {
//            camera.getCameraControl().setZoomRatio(1.0f); // Reset zoom to normal
//            zoomSeekBar.setProgress(0); // Reset SeekBar to the initial position
//        }
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if (recording != null) {
//            recording.stop();
//            recording = null;
//        }
//    }
//}