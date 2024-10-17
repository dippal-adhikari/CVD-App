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
import android.widget.MediaController;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.ReturnCode;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
    private List<File> originalClips = new ArrayList<>(); // List to store original clips
    private List<File> recordedClips = new ArrayList<>();

    private int currentScrollY = 0; // Track current scroll position
    private int scrollSpeed = 5; // Adjust scroll speed as needed
    private ScrollView scriptScrollView; // ScrollView for the script

    private VideoView videoView;


    TextView speedPercentageView;

    // Add captions to videos
    String inputClip, outputClip, question;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_video);

        // Request external storage permissions (required for saving files and FFmpeg operations)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, 1);
        }

        // Initialize VideoView
        videoView = findViewById(R.id.videoView);

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
        if (recording != null) {
            Toast.makeText(this, "Another recording is in progress.", Toast.LENGTH_SHORT).show();
            return; // Prevent multiple recordings from starting simultaneously
        }

        String fileName = "clip_" + currentClipIndex + ".mp4";
        File clipFile = new File(getExternalFilesDir(null), fileName);
        originalClips.add(clipFile);  // Track the original clip
        Log.d("Recording", "Starting new recording: " + clipFile.getAbsolutePath());

        recording = videoCapture.getOutput()
                .prepareRecording(this, new FileOutputOptions.Builder(clipFile).build())
                .start(ContextCompat.getMainExecutor(this), videoRecordEvent -> {
                    if (videoRecordEvent instanceof VideoRecordEvent.Start) {
                        isRecording = true;
                        startTime = System.currentTimeMillis();
                        handler.post(updateRecordingTimeRunnable);
                        startScrollingScript();
                        Log.d("Recording", "Recording started for clip: " + clipFile.getAbsolutePath());
                    } else if (videoRecordEvent instanceof VideoRecordEvent.Finalize) {
                        isRecording = false;
                        handler.removeCallbacks(updateRecordingTimeRunnable);
                        handler.removeCallbacks(scriptScrollRunnable); // Stop scrolling when recording ends

                        if (!((VideoRecordEvent.Finalize) videoRecordEvent).hasError()) {
                            Log.d("Recording", "Recording completed for clip: " + clipFile.getAbsolutePath());

                            // Post-process the recorded clip
                            postProcessRecordedClip(clipFile);
                        } else {
                            Log.e("Recording", "Recording failed for clip: " + clipFile.getAbsolutePath());
                            Toast.makeText(this, "Recording failed for clip: " + clipFile.getName(), Toast.LENGTH_SHORT).show();
                        }

                        recording = null; // Reset the recording state
                    }
                });
    }

    // Method to handle post-processing of recorded clips
    private void postProcessRecordedClip(File clipFile) {
        // Check if the file is valid and ready for processing
        if (!clipFile.exists() || clipFile.length() == 0) {
            Log.e("FFmpeg", "Clip file does not exist or is empty: " + clipFile.getAbsolutePath());
            Toast.makeText(this, "Clip file not ready or empty: " + clipFile.getName(), Toast.LENGTH_SHORT).show();
            return; // Exit if file is not valid
        }

        // Play the recorded clip using VideoView
        videoView.setVisibility(View.VISIBLE);  // Show the VideoView
        Uri videoUri = Uri.fromFile(clipFile);
        videoView.setVideoURI(videoUri);
        videoView.start(); // Start playback automatically

        // Media controller for VideoView
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        // Completion listener to ask for user input after the video finishes playing
        videoView.setOnCompletionListener(mp -> {
            new AlertDialog.Builder(this)
                    .setTitle("Review Clip")
                    .setMessage("Do you want to redo this clip or move to the next?")
                    .setPositiveButton("Redo", (dialog, which) -> {
                        redoClip(clipFile);
                    })
                    .setNegativeButton("Next", (dialog, which) -> {
                        processClipForTextAddition(clipFile);
                    })
                    .setCancelable(false)
                    .show();
        });
//
//        Log.d("FFmpeg", "Processing recorded clip: " + clipFile.getAbsolutePath());
//        String outputFileName = "clip_" + currentClipIndex + "_with_text.mp4";
//        File modifiedClip = new File(getExternalFilesDir(null), outputFileName);
//
//        addTextToClip(clipFile.getAbsolutePath(), modifiedClip.getAbsolutePath(), questions.get(currentClipIndex), () -> {
//            Log.d("FFmpeg", "Text added successfully to clip: " + modifiedClip.getAbsolutePath());
//
//            // Replace the original clip with the modified one
//            // Use add instead of set
//            if (currentClipIndex < recordedClips.size()) {
//                recordedClips.set(currentClipIndex, modifiedClip);
//            } else {
//                recordedClips.add(modifiedClip);
//            }
//
//            // Update UI and state
//            currentClipIndex++;
//            displayNextScript();
//
//            // If all clips are recorded and processed, merge them
//            if (currentClipIndex >= questions.size()) {
//                try {
//                    String fileListPath = createFileList(getClipPaths());
//                    String mergedOutputPath = getExternalFilesDir(null) + "/final_merged_video_" + System.currentTimeMillis() + ".mp4";
//                    mergeClips(fileListPath, mergedOutputPath, () -> {
//                        Toast.makeText(this, "Final video created successfully at " + mergedOutputPath, Toast.LENGTH_LONG).show();
//                    });
//                } catch (IOException e) {
//                    Log.e("FFmpeg", "Error creating final merged video file list.", e);
//                    Toast.makeText(this, "Error creating final merged video.", Toast.LENGTH_LONG).show();
//                }
//            }
//        });
    }

    // Method to redo the current clip by deleting it and restarting the recording
    private void redoClip(File clipFile) {
        if (clipFile.exists()) {
            boolean deleted = clipFile.delete();
            if (deleted) {
                Log.d("File", "Deleted clip for redo: " + clipFile.getAbsolutePath());
                currentClipIndex--; // Move back to redo the same clip index
            } else {
                Log.e("File", "Failed to delete clip for redo: " + clipFile.getAbsolutePath());
            }
        }
        // Restart the recording for the same clip index
        startRecording();
    }


    // Method to add text to the clip after the user confirms moving to the next
    private void processClipForTextAddition(File clipFile) {
        Log.d("FFmpeg", "Processing recorded clip: " + clipFile.getAbsolutePath());
        String outputFileName = "clip_" + currentClipIndex + "_with_text.mp4";
        File modifiedClip = new File(getExternalFilesDir(null), outputFileName);

        addTextToClip(clipFile.getAbsolutePath(), modifiedClip.getAbsolutePath(), questions.get(currentClipIndex), () -> {
            Log.d("FFmpeg", "Text added successfully to clip: " + modifiedClip.getAbsolutePath());

            // Replace the original clip with the modified one
            if (currentClipIndex < recordedClips.size()) {
                recordedClips.set(currentClipIndex, modifiedClip);
            } else {
                recordedClips.add(modifiedClip);
            }

            // Update UI and state
            currentClipIndex++;
            displayNextScript();

            // If all clips are recorded and processed, merge them
            if (currentClipIndex >= questions.size()) {
                try {
                    String fileListPath = createFileList(getClipPaths());
                    String mergedOutputPath = getExternalFilesDir(null) + "/final_merged_video_" + System.currentTimeMillis() + ".mp4";
                    mergeClips(fileListPath, mergedOutputPath, () -> {
                        Toast.makeText(this, "Final video created successfully at " + mergedOutputPath, Toast.LENGTH_LONG).show();
                    });
                } catch (IOException e) {
                    Log.e("FFmpeg", "Error creating final merged video file list.", e);
                    Toast.makeText(this, "Error creating final merged video.", Toast.LENGTH_LONG).show();
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
            currentScrollY = 0; // Reset scroll position counter
            scriptScrollView.scrollTo(0, 0); // Reset ScrollView to top position
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


    private String copyFontToStorage() {
        String fontFileName = "dmsans_regular.ttf";  // Update to your font file name
        File fontFile = new File(getExternalFilesDir(null), fontFileName);

        if (!fontFile.exists()) {
            try (InputStream is = getResources().openRawResource(R.font.dmsans_regular);
                 OutputStream os = new FileOutputStream(fontFile)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
                os.flush();
                Log.d("FFmpeg", "Font file copied to: " + fontFile.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("FFmpeg", "Failed to copy font file to storage: " + e.getMessage());
                return null;
            }
        }

        return fontFile.getAbsolutePath();
    }

    private void addTextToClip(String inputClipPath, String outputClipPath, String question, Runnable onTextAdded) {
        // Copy the font file to storage and get the absolute path
        String fontFilePath = copyFontToStorage();
        if (fontFilePath == null) {
            Toast.makeText(this, "Failed to copy font file!", Toast.LENGTH_SHORT).show();
            return;
        }

        String sanitizedQuestion = question.replace("'", "\\'").replace("\"", "\\\""); // Escape single quotes in the text
        String ffmpegCommand = "-i " + inputClipPath + " -vf \"drawtext=fontfile='" + fontFilePath + "': text='" + sanitizedQuestion + "': fontcolor=white: fontsize=24: box=1: boxcolor=black@0.5: x=(w-text_w)/2: y=h-(text_h+20)\" -y " + outputClipPath;

        Log.d("FFmpeg", "Executing FFmpeg command: " + ffmpegCommand);

        FFmpegKit.executeAsync(ffmpegCommand, session -> {
            if (ReturnCode.isSuccess(session.getReturnCode())) {
                Log.d("FFmpeg", "Text added successfully to " + inputClipPath);
                if (onTextAdded != null) {
                    runOnUiThread(onTextAdded);
                }
            } else {
                Log.e("FFmpeg", "Failed to add text to " + inputClipPath + ": " + session.getFailStackTrace());
                Toast.makeText(this, "Failed to add text to the video: " + inputClipPath, Toast.LENGTH_SHORT).show();
            }
        });
    }


    // Create Filelist for merging them together
    private String createFileList(List<String> clipPaths) throws IOException {
        File fileList = new File(getExternalFilesDir(null), "file_list.txt");
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileList));

        for (String clipPath : clipPaths) {
            writer.write("file '" + clipPath + "'\n");
        }

        writer.close();
        return fileList.getAbsolutePath();
    }

    // Merging video clips together to a single clip
    private void mergeClips(String fileListPath, String outputFilePath, Runnable onMergeComplete) {
        // Read and log the contents of the file_list.txt
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileListPath));
            String line;
            Log.d("FFmpeg", "Content of file_list.txt:");
            while ((line = reader.readLine()) != null) {
                Log.d("FFmpeg", line);
            }
            reader.close();
        } catch (IOException e) {
            Log.e("FFmpeg", "Failed to read file_list.txt", e);
        }
        // Command to use re-encoding for timestamp corrections
        String mergeCommand = "-f concat -safe 0 -i " + fileListPath + " -fflags +genpts -c:v mpeg4 -preset veryfast -crf 18 " + getTempOutputFilePath();

        Log.d("FFmpeg", "Executing FFmpeg merge command: " + mergeCommand);

//        FFmpegKit.executeAsync(mergeCommand, session -> {
//            if (ReturnCode.isSuccess(session.getReturnCode())) {
//                Log.d("FFmpeg", "Clips merged successfully.");
//
//                // Delete unmerged clips
//                deleteUnmergedClips();
//                if (onMergeComplete != null) {
//                    runOnUiThread(onMergeComplete);
//                }
//             else {
//                Log.e("FFmpeg", "Failed to rename merged file.");
//            }
//
//            } else {
//                Log.e("FFmpeg", "Failed to merge clips: " + session.getFailStackTrace());
//                Toast.makeText(this, "Failed to merge video clips.", Toast.LENGTH_LONG).show();
//            }
//        });

        FFmpegKit.executeAsync(mergeCommand, session -> {
            if (ReturnCode.isSuccess(session.getReturnCode())) {
                Log.d("FFmpeg", "Clips merged successfully");

                // Rename the merged file to the next CVD file (CVD0, CVD1, etc.)
                File mergedFile = new File(getTempOutputFilePath());
                File renamedMergedFile = new File(getExternalFilesDir(null), getNextCVDFileName());
                boolean renamed = mergedFile.renameTo(renamedMergedFile);
                if (renamed) {
                    Log.d("FFmpeg", "Merged file renamed to: " + renamedMergedFile.getAbsolutePath());

                    // Re-encode the merged file before uploading
                    File reEncodedFile = new File(getExternalFilesDir(null), renamedMergedFile.getName());
                    reEncodeVideo(renamedMergedFile, reEncodedFile);  // Call re-encode method

                    // Delete unmerged clips
                    deleteUnmergedClips();
                    if (onMergeComplete != null) {
                        runOnUiThread(onMergeComplete);
                    }
                } else {
                    Log.e("FFmpeg", "Failed to rename merged file.");
                }
            } else {
                Log.e("FFmpeg", "Failed to merge clips: " + session.getFailStackTrace());
                Toast.makeText(this, "Failed to merge video clips.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void reEncodeVideo(File originalVideoFile, File outputFile) {
        String ffmpegCommand = "-i " + originalVideoFile.getAbsolutePath() + " -c:v mpeg4 -crf 23 -preset veryfast " + outputFile.getAbsolutePath();

        FFmpegKit.executeAsync(ffmpegCommand, session -> {
            if (ReturnCode.isSuccess(session.getReturnCode())) {
                Log.d("FFmpeg", "Video re-encoded successfully.");
                uploadFinalVideoToFirebase(outputFile);  // Upload re-encoded video
            } else {
                Log.e("FFmpeg", "Re-encoding failed: " + session.getFailStackTrace());
                Toast.makeText(CreateVideoActivity.this, "Failed to re-encode the video before upload.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void uploadFinalVideoToFirebase(File finalVideoFile) {
        if (finalVideoFile.exists()) {
            Uri fileUri = Uri.fromFile(finalVideoFile);

            // Get the current user's UID from Firebase Authentication
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            long currentTimeMillis = System.currentTimeMillis();
            StorageMetadata metadata = new StorageMetadata.Builder()
                    .setContentType("video/mp4")
                    .setCustomMetadata("timestamp", String.valueOf(currentTimeMillis)) // Add timestamp as custom metadata
                    .build();

            // Use the user's UID to create a unique path for each user's videos
            StorageReference userVideoRef = FirebaseStorage.getInstance().getReference()
                    .child("users/" + userId + "/videos/" + finalVideoFile.getName());

            userVideoRef.putFile(fileUri, metadata)
                    .addOnSuccessListener(taskSnapshot -> {
                        // File successfully uploaded
                        Toast.makeText(CreateVideoActivity.this, "Final video uploaded successfully!", Toast.LENGTH_SHORT).show();
                        userVideoRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            // You can retrieve the download URL here if needed
                            Log.d("Firebase", "Download URL: " + uri.toString());
                        });
                    })
                    .addOnFailureListener(e -> {
                        // Handle any errors during the upload
                        Toast.makeText(CreateVideoActivity.this, "Failed to upload final video: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Log.e("Firebase", "Final video file does not exist for upload.");
        }
    }

//    private void uploadFinalVideoToFirebase(File finalVideoFile) {
//        if (finalVideoFile.exists()) {
//            try {
//                InputStream stream = new FileInputStream(finalVideoFile);
//                StorageReference finalVideoRef = videoStorageReference.child(finalVideoFile.getName());
//
//                StorageMetadata metadata = new StorageMetadata.Builder()
//                        .setContentType("video/mp4")  // Ensure the correct MIME type is set
//                        .build();
//
//                finalVideoRef.putStream(stream, metadata)
//                        .addOnSuccessListener(taskSnapshot -> {
//                            // File successfully uploaded
//                            Toast.makeText(CreateVideoActivity.this, "Final video uploaded successfully!", Toast.LENGTH_SHORT).show();
//                            finalVideoRef.getDownloadUrl().addOnSuccessListener(uri -> {
//                                // You can retrieve the download URL here if needed
//                                Log.d("Firebase", "Download URL: " + uri.toString());
//                            });
//                        })
//                        .addOnFailureListener(e -> {
//                            // Handle any errors during the upload
//                            Toast.makeText(CreateVideoActivity.this, "Failed to upload final video: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                        });
//            } catch (FileNotFoundException e) {
//                Log.e("Upload", "File not found: " + e.getMessage());
//            }
//        } else {
//            Log.e("Firebase", "Final video file does not exist for upload.");
//        }
//    }


    private String getTempOutputFilePath() {
        return getExternalFilesDir(null) + "/temp_merged_video.mp4";
    }

    private String getNextCVDFileName() {
        File directory = getExternalFilesDir(null);
        int fileIndex = 0;

        // Iterate through files and find the next available CVD number
        for (File file : directory.listFiles()) {
            if (file.getName().startsWith("CVD") && file.getName().endsWith(".mp4")) {
                String numberPart = file.getName().replace("CVD", "").replace(".mp4", "");
                try {
                    int number = Integer.parseInt(numberPart);
                    fileIndex = Math.max(fileIndex, number + 1);
                } catch (NumberFormatException e) {
                    // Ignore files that don't have a valid number in the name
                }
            }
        }

        return "CVD" + fileIndex + ".mp4";
    }

    // Method to delete unmerged clips
    private void deleteUnmergedClips() {
        for (File clip : recordedClips) {
            if (clip.exists()) {
                boolean deleted = clip.delete();
                if (deleted) {
                    Log.d("File", "Deleted unmerged clip: " + clip.getAbsolutePath());
                } else {
                    Log.e("File", "Failed to delete unmerged clip: " + clip.getAbsolutePath());
                }
            }
        }

        for (File originalClip : originalClips) {
            if (originalClip.exists()) {
                boolean deleted = originalClip.delete();
                if (deleted) {
                    Log.d("File", "Deleted original clip: " + originalClip.getAbsolutePath());
                } else {
                    Log.e("File", "Failed to delete original clip: " + originalClip.getAbsolutePath());
                }
            }
        }
        originalClips.clear(); // Clear the list of original clips
        recordedClips.clear(); // Clear the list after deletion
    }


    // Method to get the paths of all processed clips
    private List<String> getClipPaths() {
        List<String> paths = new ArrayList<>();
        for (File clip : recordedClips) {
            paths.add(clip.getAbsolutePath());
        }
        return paths;
    }
}



//
//package com.example.cvd_draft_1;
//
//import android.Manifest;
//import android.app.AlertDialog;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.hardware.camera2.CameraAccessException;
//import android.hardware.camera2.CameraManager;
//import android.net.Uri;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Looper;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.LinearLayout;
//import android.widget.MediaController;
//import android.widget.ScrollView;
//import android.widget.SeekBar;
//import android.widget.Spinner;
//import android.widget.TextView;
//import android.widget.Toast;
//import android.widget.VideoView;
//
//import androidx.activity.result.ActivityResultLauncher;
//import androidx.activity.result.contract.ActivityResultContracts;
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.camera.core.Camera;
//import androidx.camera.core.CameraSelector;
//import androidx.camera.core.Preview;
//import androidx.camera.lifecycle.ProcessCameraProvider;
//import androidx.camera.view.PreviewView;
//import androidx.core.app.ActivityCompat;
//import androidx.core.content.ContextCompat;
//
//import com.arthenica.ffmpegkit.FFmpegKit;
//import com.arthenica.ffmpegkit.ReturnCode;
//import com.google.android.material.floatingactionbutton.FloatingActionButton;
//import com.google.android.material.progressindicator.CircularProgressIndicator;
//import com.google.common.util.concurrent.ListenableFuture;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.storage.FirebaseStorage;
//import com.google.firebase.storage.StorageMetadata;
//import com.google.firebase.storage.StorageReference;
//
//import java.io.BufferedReader;
//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.FileReader;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.ExecutionException;
//
//import androidx.camera.video.FileOutputOptions;
//import androidx.camera.video.Quality;
//import androidx.camera.video.QualitySelector;
//import androidx.camera.video.Recorder;
//import androidx.camera.video.Recording;
//import androidx.camera.video.VideoCapture;
//import androidx.camera.video.VideoRecordEvent;
//
//
//
//public class CreateVideoActivity extends AppCompatActivity {
//
//    // UI Elements
//    private TextView scriptTextView, recordingTime;
//    private CircularProgressIndicator recordingProgressBar;
//    private SeekBar zoomSeekBar, exposureSeekBar;
//    private Spinner videoQualitySpinner;
//
//    // CameraX and Recording related fields
//    private ActivityResultLauncher<String> requestPermissionLauncher;
//    private boolean isRecording = false;
//    private VideoCapture<Recorder> videoCapture;
//    private Recording recording;
//    private CameraSelector cameraSelector;
//    private Camera camera;
//    private CameraManager cameraManager;
//    private Handler handler;
//    private Runnable updateRecordingTimeRunnable, scriptScrollRunnable;
//    private long startTime;
//    private boolean isBackCamera = true;
//    private boolean isFlashlightOn = false;
//
//    // Firebase-related fields
//    private StorageReference videoStorageReference;
//
//    // Script-related data
//    private String scriptId;
//    private List<String> questions;
//    private List<String> answers;
//    private int currentClipIndex = 0;
//    private List<File> originalClips = new ArrayList<>(); // List to store original clips
//    private List<File> recordedClips = new ArrayList<>();
//
//    private int currentScrollY = 0; // Track current scroll position
//    private int scrollSpeed = 5; // Adjust scroll speed as needed
//    private ScrollView scriptScrollView; // ScrollView for the script
//
//    private VideoView videoView;
//
//
//    TextView speedPercentageView;
//
//    // Add captions to videos
//    String inputClip, outputClip, question;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_create_video);
//
//        // Request external storage permissions (required for saving files and FFmpeg operations)
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
//                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                    Manifest.permission.READ_EXTERNAL_STORAGE
//            }, 1);
//        }
//
//        // Initialize VideoView
//        videoView = findViewById(R.id.videoView);
//
//        // Initialize Firebase Storage
//        videoStorageReference = FirebaseStorage.getInstance().getReference().child("videos");
//
//        // Retrieve data passed via Intent
//        questions = getIntent().getStringArrayListExtra("QUESTIONS");
//        answers = getIntent().getStringArrayListExtra("ANSWERS");
//
//        if (questions == null || answers == null) {
//            Toast.makeText(this, "Script data not found!", Toast.LENGTH_SHORT).show();
//            finish();
//            return;
//        }
//
//        // Initialize UI elements
//
//        scriptTextView = findViewById(R.id.script_text);
//        recordingTime = findViewById(R.id.recording_time);
//        recordingProgressBar = findViewById(R.id.recording_progress_bar);
//        FloatingActionButton fabRecording = findViewById(R.id.fab_recording);
//        Button rotateButton = findViewById(R.id.rotateButton);
//        Button toggleFlashlightButton = findViewById(R.id.toggle_flashlight);
//        Button toggleStabilizationButton = findViewById(R.id.toggle_stabilization);
//        FloatingActionButton showBottomSheetButton = findViewById(R.id.show_bottom_sheet_button);
//        LinearLayout bottomSheet = findViewById(R.id.bottom_sheet);
//        bottomSheet.setVisibility(View.GONE);
//        scriptScrollView = findViewById(R.id.script_scroll_view);
//
//        // Camera Controls
//        zoomSeekBar = findViewById(R.id.zoomSeekBar);
//        exposureSeekBar = findViewById(R.id.exposure_control);
//        videoQualitySpinner = findViewById(R.id.video_quality);
//
//
//
//        // Speed control buttons and TextView
//        Button btnDecreaseSpeed = findViewById(R.id.btn_decrease_speed);
//        Button btnIncreaseSpeed = findViewById(R.id.btn_increase_speed);
//        speedPercentageView = findViewById(R.id.speed_percentage);
//
//
//
//        // Decrease speed button click listener
//        btnDecreaseSpeed.setOnClickListener(v -> {
//            if (scrollSpeed > 1) {  // Limit to 10%
//                scrollSpeed -= 1;  // Decrease by 10%
//                updateSpeedDisplay();
//            }
//        });
//
//// Increase speed button click listener
//        btnIncreaseSpeed.setOnClickListener(v -> {
//            if (scrollSpeed < 10) {  // Limit to 100%
//                scrollSpeed += 1;  // Increase by 10%
//                updateSpeedDisplay();
//            }
//        });
//
//
//
//        handler = new Handler(Looper.getMainLooper());
//        updateRecordingTimeRunnable = new Runnable() {
//            @Override
//            public void run() {
//                if (isRecording) {
//                    long elapsedTime = System.currentTimeMillis() - startTime;
//                    int seconds = (int) (elapsedTime / 1000) % 60;
//                    int minutes = (int) (elapsedTime / (1000 * 60)) % 60;
//                    int hours = (int) (elapsedTime / (1000 * 60 * 60)) % 24;
//                    recordingTime.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
//                    recordingProgressBar.setProgress((int) (elapsedTime / 1000));
//                    handler.postDelayed(this, 1000);
//                }
//            }
//        };
//
//        scriptScrollRunnable = new Runnable() {
//            @Override
//            public void run() {
//                if (isRecording) {
//                    currentScrollY += scrollSpeed;
//                    scriptScrollView.scrollTo(0, currentScrollY);
//                    handler.postDelayed(this, 100); // Adjust the delay to control scroll speed
//                }
//            }
//        };
//
//        fabRecording.setOnClickListener(v -> {
//            if (isRecording) {
//                stopRecording();
//            } else {
//                startRecording();
//            }
//        });
//
//        // Rotate Camera Button
//        rotateButton.setOnClickListener(v -> {
//            isBackCamera = !isBackCamera;
//            startCamera();
//        });
//
//        // Toggle Flashlight
//        toggleFlashlightButton.setOnClickListener(v -> toggleFlashlight());
//
//        // Toggle Stabilization
//        toggleStabilizationButton.setOnClickListener(v -> toggleStabilization());
//
//        showBottomSheetButton.setOnClickListener(v -> {
//            if (bottomSheet.getVisibility() == View.GONE) {
//                bottomSheet.setVisibility(View.VISIBLE);
//            } else {
//                bottomSheet.setVisibility(View.GONE);
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
//        handler = new Handler(Looper.getMainLooper());
//        updateRecordingTimeRunnable = new Runnable() {
//            @Override
//            public void run() {
//                if (isRecording) {
//                    long elapsedTime = System.currentTimeMillis() - startTime;
//                    int seconds = (int) (elapsedTime / 1000) % 60;
//                    int minutes = (int) (elapsedTime / (1000 * 60)) % 60;
//                    int hours = (int) (elapsedTime / (1000 * 60 * 60)) % 24;
//                    recordingTime.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
//                    recordingProgressBar.setProgress((int) (elapsedTime / 1000));
//                    handler.postDelayed(this, 1000);
//                }
//            }
//        };
//
//        displayNextScript(); // Display the first script question and answer
//    }
//
//    // Method to update the speed percentage display
//    private void updateSpeedDisplay() {
//        int percentage = (scrollSpeed * 10);  // Convert speed to percentage
//        speedPercentageView.setText(percentage + "%");  // Display the percentage
//    }
//
//    private void toggleFlashlight() {
//        if (camera != null) {
//            camera.getCameraControl().enableTorch(!isFlashlightOn);
//            isFlashlightOn = !isFlashlightOn;
//        }
//    }
//
//    private void toggleStabilization() {
//        Toast.makeText(this, "Stabilization toggled.", Toast.LENGTH_SHORT).show();
//    }
//
//    private void startRecording() {
//        if (recording != null) {
//            Toast.makeText(this, "Another recording is in progress.", Toast.LENGTH_SHORT).show();
//            return; // Prevent multiple recordings from starting simultaneously
//        }
//
//        String fileName = "clip_" + currentClipIndex + ".mp4";
//        File clipFile = new File(getExternalFilesDir(null), fileName);
//        originalClips.add(clipFile);  // Track the original clip
//        Log.d("Recording", "Starting new recording: " + clipFile.getAbsolutePath());
//
//        recording = videoCapture.getOutput()
//                .prepareRecording(this, new FileOutputOptions.Builder(clipFile).build())
//                .start(ContextCompat.getMainExecutor(this), videoRecordEvent -> {
//                    if (videoRecordEvent instanceof VideoRecordEvent.Start) {
//                        isRecording = true;
//                        startTime = System.currentTimeMillis();
//                        handler.post(updateRecordingTimeRunnable);
//                        startScrollingScript();
//                        Log.d("Recording", "Recording started for clip: " + clipFile.getAbsolutePath());
//                    } else if (videoRecordEvent instanceof VideoRecordEvent.Finalize) {
//                        isRecording = false;
//                        handler.removeCallbacks(updateRecordingTimeRunnable);
//                        handler.removeCallbacks(scriptScrollRunnable); // Stop scrolling when recording ends
//
//                        if (!((VideoRecordEvent.Finalize) videoRecordEvent).hasError()) {
//                            Log.d("Recording", "Recording completed for clip: " + clipFile.getAbsolutePath());
//
//                            // Post-process the recorded clip
//                            postProcessRecordedClip(clipFile);
//                        } else {
//                            Log.e("Recording", "Recording failed for clip: " + clipFile.getAbsolutePath());
//                            Toast.makeText(this, "Recording failed for clip: " + clipFile.getName(), Toast.LENGTH_SHORT).show();
//                        }
//
//                        recording = null; // Reset the recording state
//                    }
//                });
//    }
//
//    // Method to handle post-processing of recorded clips
//    private void postProcessRecordedClip(File clipFile) {
//        // Check if the file is valid and ready for processing
//        if (!clipFile.exists() || clipFile.length() == 0) {
//            Log.e("FFmpeg", "Clip file does not exist or is empty: " + clipFile.getAbsolutePath());
//            Toast.makeText(this, "Clip file not ready or empty: " + clipFile.getName(), Toast.LENGTH_SHORT).show();
//            return; // Exit if file is not valid
//        }
//
//        // Play the recorded clip using VideoView
//        videoView.setVisibility(View.VISIBLE);  // Show the VideoView
//        Uri videoUri = Uri.fromFile(clipFile);
//        videoView.setVideoURI(videoUri);
//        videoView.start(); // Start playback automatically
//
//        // Media controller for VideoView
//        MediaController mediaController = new MediaController(this);
//        mediaController.setAnchorView(videoView);
//        videoView.setMediaController(mediaController);
//
//        // Completion listener to ask for user input after the video finishes playing
//        videoView.setOnCompletionListener(mp -> {
//            new AlertDialog.Builder(this)
//                    .setTitle("Review Clip")
//                    .setMessage("Do you want to redo this clip or move to the next?")
//                    .setPositiveButton("Redo", (dialog, which) -> {
//                        redoClip(clipFile);
//                    })
//                    .setNegativeButton("Next", (dialog, which) -> {
//                        processClipForTextAddition(clipFile);
//                    })
//                    .setCancelable(false)
//                    .show();
//        });
////
////        Log.d("FFmpeg", "Processing recorded clip: " + clipFile.getAbsolutePath());
////        String outputFileName = "clip_" + currentClipIndex + "_with_text.mp4";
////        File modifiedClip = new File(getExternalFilesDir(null), outputFileName);
////
////        addTextToClip(clipFile.getAbsolutePath(), modifiedClip.getAbsolutePath(), questions.get(currentClipIndex), () -> {
////            Log.d("FFmpeg", "Text added successfully to clip: " + modifiedClip.getAbsolutePath());
////
////            // Replace the original clip with the modified one
////            // Use add instead of set
////            if (currentClipIndex < recordedClips.size()) {
////                recordedClips.set(currentClipIndex, modifiedClip);
////            } else {
////                recordedClips.add(modifiedClip);
////            }
////
////            // Update UI and state
////            currentClipIndex++;
////            displayNextScript();
////
////            // If all clips are recorded and processed, merge them
////            if (currentClipIndex >= questions.size()) {
////                try {
////                    String fileListPath = createFileList(getClipPaths());
////                    String mergedOutputPath = getExternalFilesDir(null) + "/final_merged_video_" + System.currentTimeMillis() + ".mp4";
////                    mergeClips(fileListPath, mergedOutputPath, () -> {
////                        Toast.makeText(this, "Final video created successfully at " + mergedOutputPath, Toast.LENGTH_LONG).show();
////                    });
////                } catch (IOException e) {
////                    Log.e("FFmpeg", "Error creating final merged video file list.", e);
////                    Toast.makeText(this, "Error creating final merged video.", Toast.LENGTH_LONG).show();
////                }
////            }
////        });
//    }
//
//    // Method to redo the current clip by deleting it and restarting the recording
//    private void redoClip(File clipFile) {
//        if (clipFile.exists()) {
//            boolean deleted = clipFile.delete();
//            if (deleted) {
//                Log.d("File", "Deleted clip for redo: " + clipFile.getAbsolutePath());
//                currentClipIndex--; // Move back to redo the same clip index
//            } else {
//                Log.e("File", "Failed to delete clip for redo: " + clipFile.getAbsolutePath());
//            }
//        }
//        // Restart the recording for the same clip index
//        startRecording();
//    }
//
//
//    // Method to add text to the clip after the user confirms moving to the next
//    private void processClipForTextAddition(File clipFile) {
//        Log.d("FFmpeg", "Processing recorded clip: " + clipFile.getAbsolutePath());
//        String outputFileName = "clip_" + currentClipIndex + "_with_text.mp4";
//        File modifiedClip = new File(getExternalFilesDir(null), outputFileName);
//
//        addTextToClip(clipFile.getAbsolutePath(), modifiedClip.getAbsolutePath(), questions.get(currentClipIndex), () -> {
//            Log.d("FFmpeg", "Text added successfully to clip: " + modifiedClip.getAbsolutePath());
//
//            // Replace the original clip with the modified one
//            if (currentClipIndex < recordedClips.size()) {
//                recordedClips.set(currentClipIndex, modifiedClip);
//            } else {
//                recordedClips.add(modifiedClip);
//            }
//
//            // Update UI and state
//            currentClipIndex++;
//            displayNextScript();
//
//            // If all clips are recorded and processed, merge them
//            if (currentClipIndex >= questions.size()) {
//                try {
//                    String fileListPath = createFileList(getClipPaths());
//                    String mergedOutputPath = getExternalFilesDir(null) + "/final_merged_video_" + System.currentTimeMillis() + ".mp4";
//                    mergeClips(fileListPath, mergedOutputPath, () -> {
//                        Toast.makeText(this, "Final video created successfully at " + mergedOutputPath, Toast.LENGTH_LONG).show();
//                    });
//                } catch (IOException e) {
//                    Log.e("FFmpeg", "Error creating final merged video file list.", e);
//                    Toast.makeText(this, "Error creating final merged video.", Toast.LENGTH_LONG).show();
//                }
//            }
//        });
//    }
//
//
//    private void stopRecording() {
//        if (recording != null) {
//            recording.stop();
//            recording = null;
//        }
//        handler.removeCallbacks(scriptScrollRunnable);
//    }
//
//    private void startScrollingScript() {
//        handler.post(scriptScrollRunnable);
//    }
//
//
//
//    private void displayNextScript() {
//        if (currentClipIndex < questions.size()) {
//            String scriptText = "Q: " + questions.get(currentClipIndex) + "\n\nA: " + answers.get(currentClipIndex);
//            scriptTextView.setText(scriptText);
//            currentScrollY = 0; // Reset scroll position counter
//            scriptScrollView.scrollTo(0, 0); // Reset ScrollView to top position
//        } else {
//            Toast.makeText(this, "All clips recorded.", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    private void startCamera() {
//        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
//        cameraProviderFuture.addListener(() -> {
//            try {
//                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
//                bindPreview(cameraProvider);
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
//
//        Recorder recorder = new Recorder.Builder()
//                .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
//                .build();
//        videoCapture = VideoCapture.withOutput(recorder);
//
//        cameraProvider.unbindAll();
//        camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, videoCapture);
//    }
//
//
//    private String copyFontToStorage() {
//        String fontFileName = "dmsans_regular.ttf";  // Update to your font file name
//        File fontFile = new File(getExternalFilesDir(null), fontFileName);
//
//        if (!fontFile.exists()) {
//            try (InputStream is = getResources().openRawResource(R.font.dmsans_regular);
//                 OutputStream os = new FileOutputStream(fontFile)) {
//                byte[] buffer = new byte[1024];
//                int length;
//                while ((length = is.read(buffer)) > 0) {
//                    os.write(buffer, 0, length);
//                }
//                os.flush();
//                Log.d("FFmpeg", "Font file copied to: " + fontFile.getAbsolutePath());
//            } catch (IOException e) {
//                e.printStackTrace();
//                Log.e("FFmpeg", "Failed to copy font file to storage: " + e.getMessage());
//                return null;
//            }
//        }
//
//        return fontFile.getAbsolutePath();
//    }
//
//    private void addTextToClip(String inputClipPath, String outputClipPath, String question, Runnable onTextAdded) {
//        // Copy the font file to storage and get the absolute path
//        String fontFilePath = copyFontToStorage();
//        if (fontFilePath == null) {
//            Toast.makeText(this, "Failed to copy font file!", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        String sanitizedQuestion = question.replace("'", "\\'").replace("\"", "\\\""); // Escape single quotes in the text
//        String ffmpegCommand = "-i " + inputClipPath + " -vf \"drawtext=fontfile='" + fontFilePath + "': text='" + sanitizedQuestion + "': fontcolor=white: fontsize=24: box=1: boxcolor=black@0.5: x=(w-text_w)/2: y=h-(text_h+20)\" -y " + outputClipPath;
//
//        Log.d("FFmpeg", "Executing FFmpeg command: " + ffmpegCommand);
//
//        FFmpegKit.executeAsync(ffmpegCommand, session -> {
//            if (ReturnCode.isSuccess(session.getReturnCode())) {
//                Log.d("FFmpeg", "Text added successfully to " + inputClipPath);
//                if (onTextAdded != null) {
//                    runOnUiThread(onTextAdded);
//                }
//            } else {
//                Log.e("FFmpeg", "Failed to add text to " + inputClipPath + ": " + session.getFailStackTrace());
//                Toast.makeText(this, "Failed to add text to the video: " + inputClipPath, Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//
//    // Create Filelist for merging them together
//    private String createFileList(List<String> clipPaths) throws IOException {
//        File fileList = new File(getExternalFilesDir(null), "file_list.txt");
//        BufferedWriter writer = new BufferedWriter(new FileWriter(fileList));
//
//        for (String clipPath : clipPaths) {
//            writer.write("file '" + clipPath + "'\n");
//        }
//
//        writer.close();
//        return fileList.getAbsolutePath();
//    }
//
//    // Merging video clips together to a single clip
//    private void mergeClips(String fileListPath, String outputFilePath, Runnable onMergeComplete) {
//        // Read and log the contents of the file_list.txt
//        try {
//            BufferedReader reader = new BufferedReader(new FileReader(fileListPath));
//            String line;
//            Log.d("FFmpeg", "Content of file_list.txt:");
//            while ((line = reader.readLine()) != null) {
//                Log.d("FFmpeg", line);
//            }
//            reader.close();
//        } catch (IOException e) {
//            Log.e("FFmpeg", "Failed to read file_list.txt", e);
//        }
//        // Command to use re-encoding for timestamp corrections
//        String mergeCommand = "-f concat -safe 0 -i " + fileListPath + " -fflags +genpts -c:v mpeg4 -preset veryfast -crf 18 " + getTempOutputFilePath();
//
//        Log.d("FFmpeg", "Executing FFmpeg merge command: " + mergeCommand);
//
////        FFmpegKit.executeAsync(mergeCommand, session -> {
////            if (ReturnCode.isSuccess(session.getReturnCode())) {
////                Log.d("FFmpeg", "Clips merged successfully.");
////
////                // Delete unmerged clips
////                deleteUnmergedClips();
////                if (onMergeComplete != null) {
////                    runOnUiThread(onMergeComplete);
////                }
////             else {
////                Log.e("FFmpeg", "Failed to rename merged file.");
////            }
////
////            } else {
////                Log.e("FFmpeg", "Failed to merge clips: " + session.getFailStackTrace());
////                Toast.makeText(this, "Failed to merge video clips.", Toast.LENGTH_LONG).show();
////            }
////        });
//
//        FFmpegKit.executeAsync(mergeCommand, session -> {
//            if (ReturnCode.isSuccess(session.getReturnCode())) {
//                Log.d("FFmpeg", "Clips merged successfully");
//
//                // Rename the merged file to the next CVD file (CVD0, CVD1, etc.)
//                File mergedFile = new File(getTempOutputFilePath());
//                File renamedMergedFile = new File(getExternalFilesDir(null), getNextCVDFileName());
//                boolean renamed = mergedFile.renameTo(renamedMergedFile);
//                if (renamed) {
//                    Log.d("FFmpeg", "Merged file renamed to: " + renamedMergedFile.getAbsolutePath());
//
//                    // Upload the final video to Firebase Storage
//                    uploadFinalVideoToFirebase(renamedMergedFile);
//
//                    // Delete unmerged clips
//                    deleteUnmergedClips();
//                    if (onMergeComplete != null) {
//                        runOnUiThread(onMergeComplete);
//                    }
//                } else {
//                    Log.e("FFmpeg", "Failed to rename merged file.");
//                }
//            } else {
//                Log.e("FFmpeg", "Failed to merge clips: " + session.getFailStackTrace());
//                Toast.makeText(this, "Failed to merge video clips.", Toast.LENGTH_LONG).show();
//            }
//        });
//    }
//
//    private void uploadFinalVideoToFirebase(File finalVideoFile) {
//        if (finalVideoFile.exists()) {
//            Uri fileUri = Uri.fromFile(finalVideoFile);
//
//            // Get the current user's UID from Firebase Authentication
//            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//
//            StorageMetadata metadata = new StorageMetadata.Builder()
//                    .setContentType("video/mp4")
//                    .build();
//
//            // Use the user's UID to create a unique path for each user's videos
//            StorageReference userVideoRef = FirebaseStorage.getInstance().getReference()
//                    .child("users/" + userId + "/videos/" + finalVideoFile.getName());
//
//            userVideoRef.putFile(fileUri, metadata)
//                    .addOnSuccessListener(taskSnapshot -> {
//                        // File successfully uploaded
//                        Toast.makeText(CreateVideoActivity.this, "Final video uploaded successfully!", Toast.LENGTH_SHORT).show();
//                        userVideoRef.getDownloadUrl().addOnSuccessListener(uri -> {
//                            // You can retrieve the download URL here if needed
//                            Log.d("Firebase", "Download URL: " + uri.toString());
//                        });
//                    })
//                    .addOnFailureListener(e -> {
//                        // Handle any errors during the upload
//                        Toast.makeText(CreateVideoActivity.this, "Failed to upload final video: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                    });
//        } else {
//            Log.e("Firebase", "Final video file does not exist for upload.");
//        }
//    }
//
////    private void uploadFinalVideoToFirebase(File finalVideoFile) {
////        if (finalVideoFile.exists()) {
////            try {
////                InputStream stream = new FileInputStream(finalVideoFile);
////                StorageReference finalVideoRef = videoStorageReference.child(finalVideoFile.getName());
////
////                StorageMetadata metadata = new StorageMetadata.Builder()
////                        .setContentType("video/mp4")  // Ensure the correct MIME type is set
////                        .build();
////
////                finalVideoRef.putStream(stream, metadata)
////                        .addOnSuccessListener(taskSnapshot -> {
////                            // File successfully uploaded
////                            Toast.makeText(CreateVideoActivity.this, "Final video uploaded successfully!", Toast.LENGTH_SHORT).show();
////                            finalVideoRef.getDownloadUrl().addOnSuccessListener(uri -> {
////                                // You can retrieve the download URL here if needed
////                                Log.d("Firebase", "Download URL: " + uri.toString());
////                            });
////                        })
////                        .addOnFailureListener(e -> {
////                            // Handle any errors during the upload
////                            Toast.makeText(CreateVideoActivity.this, "Failed to upload final video: " + e.getMessage(), Toast.LENGTH_SHORT).show();
////                        });
////            } catch (FileNotFoundException e) {
////                Log.e("Upload", "File not found: " + e.getMessage());
////            }
////        } else {
////            Log.e("Firebase", "Final video file does not exist for upload.");
////        }
////    }
//
//
//    private String getTempOutputFilePath() {
//        return getExternalFilesDir(null) + "/temp_merged_video.mp4";
//    }
//
//    private String getNextCVDFileName() {
//        File directory = getExternalFilesDir(null);
//        int fileIndex = 0;
//
//        // Iterate through files and find the next available CVD number
//        for (File file : directory.listFiles()) {
//            if (file.getName().startsWith("CVD") && file.getName().endsWith(".mp4")) {
//                String numberPart = file.getName().replace("CVD", "").replace(".mp4", "");
//                try {
//                    int number = Integer.parseInt(numberPart);
//                    fileIndex = Math.max(fileIndex, number + 1);
//                } catch (NumberFormatException e) {
//                    // Ignore files that don't have a valid number in the name
//                }
//            }
//        }
//
//        return "CVD" + fileIndex + ".mp4";
//    }
//
//    // Method to delete unmerged clips
//    private void deleteUnmergedClips() {
//        for (File clip : recordedClips) {
//            if (clip.exists()) {
//                boolean deleted = clip.delete();
//                if (deleted) {
//                    Log.d("File", "Deleted unmerged clip: " + clip.getAbsolutePath());
//                } else {
//                    Log.e("File", "Failed to delete unmerged clip: " + clip.getAbsolutePath());
//                }
//            }
//        }
//
//        for (File originalClip : originalClips) {
//            if (originalClip.exists()) {
//                boolean deleted = originalClip.delete();
//                if (deleted) {
//                    Log.d("File", "Deleted original clip: " + originalClip.getAbsolutePath());
//                } else {
//                    Log.e("File", "Failed to delete original clip: " + originalClip.getAbsolutePath());
//                }
//            }
//        }
//        originalClips.clear(); // Clear the list of original clips
//        recordedClips.clear(); // Clear the list after deletion
//    }
//
//
//    // Method to get the paths of all processed clips
//    private List<String> getClipPaths() {
//        List<String> paths = new ArrayList<>();
//        for (File clip : recordedClips) {
//            paths.add(clip.getAbsolutePath());
//        }
//        return paths;
//    }
//}
//
