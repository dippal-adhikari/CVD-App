package com.example.cvd_draft_1;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.camera.video.FileOutputOptions;
import androidx.camera.video.Quality;
import androidx.camera.video.QualitySelector;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.camera.video.VideoCapture;
import androidx.camera.video.VideoRecordEvent;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.segmentation.Segmentation;
import com.google.mlkit.vision.segmentation.SegmentationMask;
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions;



public class CreateVideoActivity extends AppCompatActivity {
    public static final int STABILIZATION_MODE_ON = 1;
    public static final int STABILIZATION_MODE_OFF = 0;


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
    private Quality selectedQuality = Quality.HIGHEST;
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
    FloatingActionButton fabRecording;
    FloatingActionButton showBottomSheetButton;


    TextView speedPercentageView;


    // overlay View
    private OverlayView overlayView;
    private ExecutorService cameraExecutor;
    private int selectedSegmentationStyle;
    boolean isSegmentationEnabled = false; // This should be set based on user input
    PreviewView previewView;
    private ImageView recordingImageView;


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

        recordingImageView = findViewById(R.id.recordingImageView);


        overlayView = findViewById(R.id.overlayView);
        previewView = findViewById(R.id.previewView);


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
        fabRecording = findViewById(R.id.fab_recording);
        Button rotateButton = findViewById(R.id.rotateButton);
        Button toggleFlashlightButton = findViewById(R.id.toggle_flashlight);
        Button toggleStabilizationButton = findViewById(R.id.toggle_stabilization);
        showBottomSheetButton = findViewById(R.id.show_bottom_sheet_button);
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

        // grid lines handle
        Button toggleGridButton = findViewById(R.id.toggle_grid);
        final View gridLinesView = findViewById(R.id.grid_lines_view); // Make sure to set the correct ID for your GridLinesView

        toggleGridButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gridLinesView.getVisibility() == View.VISIBLE) {
                    gridLinesView.setVisibility(View.GONE);
                } else {
                    gridLinesView.setVisibility(View.VISIBLE);
                }
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


        // segmentation spinner
        // Set up segmentation options (e.g., grayscale, colored overlay)
        Spinner segmentationOptions = findViewById(R.id.segmentationOptions);
        segmentationOptions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSegmentationStyle = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
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
        cameraExecutor = Executors.newSingleThreadExecutor();

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
        fabRecording.setVisibility(View.INVISIBLE);
        showBottomSheetButton.setVisibility(View.INVISIBLE);
        recordingProgressBar.setVisibility(View.INVISIBLE);
        recordingTime.setVisibility(View.INVISIBLE);

        Uri videoUri = Uri.fromFile(clipFile);
        videoView.setVideoURI(videoUri);
        videoView.start(); // Start playback automatically

        // Media controller for VideoView
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        mediaController.show(0);  // Show controls immediately and keep them visible

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
    }

    // Method to redo the current clip by deleting it and restarting the recording
    private void redoClip(File clipFile) {
        if (clipFile.exists()) {
            boolean deleted = clipFile.delete();
            if (deleted) {
                videoView.setVisibility(View.INVISIBLE);  // Show the VideoView
                fabRecording.setVisibility(View.VISIBLE);
                showBottomSheetButton.setVisibility(View.VISIBLE);
                recordingProgressBar.setVisibility(View.VISIBLE);
                recordingTime.setVisibility(View.VISIBLE);


                Log.d("File", "Deleted clip for redo: " + clipFile.getAbsolutePath());

            } else {
                Log.e("File", "Failed to delete clip for redo: " + clipFile.getAbsolutePath());
            }
        }
        // Restart the recording for the same clip index
        displayNextScript();
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
            videoView.setVisibility(View.INVISIBLE);  // hide the VideoView
            fabRecording.setVisibility(View.VISIBLE);
            showBottomSheetButton.setVisibility(View.VISIBLE);
            recordingProgressBar.setVisibility(View.VISIBLE);
            recordingTime.setVisibility(View.VISIBLE);

            // Reset recording time and progress bar
            recordingTime.setText("00:00:00");  // Reset the timer display
            recordingProgressBar.setProgress(0);  // Reset the progress bar

            String scriptText = "Q: " + questions.get(currentClipIndex) + "\n\nA: " + answers.get(currentClipIndex);
            scriptTextView.setText(scriptText);
            currentScrollY = 0; // Reset scroll position counter
            scriptScrollView.scrollTo(0, 0); // Reset ScrollView to top position
        } else {
            Toast.makeText(this, "All clips recorded.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(CreateVideoActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }

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

    private void startCamera() {
        recordingImageView.setVisibility(View.GONE);
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        // Conditional Segmentation Setup
        Log.d("Segmentation", "camera start");


        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                int rotation = previewView.getDisplay().getRotation();


                // Preview setup
                Preview preview = new Preview.Builder().setTargetRotation(rotation).build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());


                // Setup the camera selector (front or back camera)
                cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(isBackCamera ? CameraSelector.LENS_FACING_BACK : CameraSelector.LENS_FACING_FRONT)
                        .build();

                // Real-time Image Analysis setup for background segmentation
                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(640, 480))
                        .setTargetRotation(rotation)
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, imageProxy -> {
                    // Process each frame for segmentation
                    processImageProxy(imageProxy);
                });



                // Video Capture setup
                Recorder recorder = new Recorder.Builder()
                        .setQualitySelector(QualitySelector.from(selectedQuality))
                        .build();
                videoCapture = VideoCapture.withOutput(recorder);

                // Bind all use cases to the camera
                cameraProvider.unbindAll();
                camera = cameraProvider.bindToLifecycle(
                        this, cameraSelector, preview, imageAnalysis);
//                cameraProvider.bindToLifecycle(
//                        this, cameraSelector,videoCapture, preview, imageAnalysis // Not supported more than three bind may support in another device.
//                );

//                if (isSegmentationEnabled) {
//                    // Bind imageAnalysis to enable segmentation
//                    camera = cameraProvider.bindToLifecycle(
//                            this, cameraSelector, videoCapture, imageAnalysis);
//                } else {
//                    // Bind without imageAnalysis for normal video recording
//                    camera = cameraProvider.bindToLifecycle(
//                            this, cameraSelector, preview, imageAnalysis);
//                }

            } catch (Exception e) {
                Log.e("CameraXApp", "Use case binding failed", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @SuppressLint("UnsafeOptInUsageError")
    private void processImageProxy(ImageProxy imageProxy) {
        // Ensure the image is not null before processing
        if (imageProxy.getImage() == null) {
            imageProxy.close();
            return;
        }

        // Convert ImageProxy to Bitmap

//        Bitmap bitmap = imageProxyToBitmap(imageProxy);

        Bitmap bitmap = yuvToRgb(imageProxy);

        if (bitmap == null) {
            Log.e(TAG, "Failed to convert ImageProxy to Bitmap");
            imageProxy.close();
            return;
        }

        // Convert ImageProxy to InputImage for ML Kit segmentation
        InputImage inputImage = InputImage.fromMediaImage(
                Objects.requireNonNull(imageProxy.getImage()),
                imageProxy.getImageInfo().getRotationDegrees()
        );

        // Perform segmentation and close the imageProxy after completion
        segmentImage(inputImage, bitmap, imageProxy);
    }


    private void segmentImage(InputImage image, Bitmap bitmap, ImageProxy imageProxy) {
        SelfieSegmenterOptions options = new SelfieSegmenterOptions.Builder()
                .setDetectorMode(SelfieSegmenterOptions.STREAM_MODE)  // Real-time segmentation mode
                .enableRawSizeMask()
                .build();

        Segmentation.getClient(options).process(image)
                .addOnSuccessListener(mask -> {
                    // Process the segmentation mask using the converted Bitmap
                    processSegmentationMask(mask, bitmap);

                    // Close the imageProxy after processing
                    imageProxy.close();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Segmentation failed: " + e.getMessage(), e);
                    imageProxy.close();
                });
    }

    @SuppressLint("UnsafeOptInUsageError")
    private void processSegmentationMask(SegmentationMask mask, Bitmap cameraBitmap) {
        if (cameraBitmap == null) {
            Log.e(TAG, "Bitmap is null. Cannot process segmentation.");
            return;
        }

        // Rotate and resize the cameraBitmap to match the mask dimensions
        int currentRotation = previewView.getDisplay().getRotation();
        Bitmap rotatedCameraBitmap = rotateBitmapToPortrait(cameraBitmap, currentRotation);
        Bitmap resizedCameraBitmap = Bitmap.createScaledBitmap(rotatedCameraBitmap, mask.getWidth(), mask.getHeight(), true);

        // Load and resize the background image to match the mask dimensions (for case 1)
        Bitmap backgroundBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.step1);
        if (backgroundBitmap == null) {
            Log.e(TAG, "Background bitmap is null.");
            return;
        }
        Bitmap resizedBackgroundBitmap = Bitmap.createScaledBitmap(backgroundBitmap, mask.getWidth(), mask.getHeight(), true);


        // Create a mutable bitmap for the final composited image
        Bitmap resultBitmap = Bitmap.createBitmap(mask.getWidth(), mask.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(resultBitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        // Get the segmentation mask buffer
        ByteBuffer buffer = mask.getBuffer();
        buffer.rewind();

        int width = mask.getWidth();
        int height = mask.getHeight();
        int[] pixels = new int[width * height];
        resizedCameraBitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        if(selectedSegmentationStyle !=0){
            overlayView.setVisibility(View.VISIBLE);
        }

        // Iterate through the mask pixels and blend the camera feed or apply effects
        for (int i = 0; i < width * height; i++) {
            float confidence = buffer.getFloat();

            // Coordinates for pixel location
            int x = i % width;
            int y = i / width;

            if (confidence < 0.5f) { // Background pixel
                if (selectedSegmentationStyle == 1) {
                    // Case 1: Replace the background with the image
                    int backgroundPixel = resizedBackgroundBitmap.getPixel(x, y);
                    resultBitmap.setPixel(x, y, backgroundPixel);
                } else {
                    // Handle other cases with visual effects on the background pixels
                    int color = pixels[i];
                    int alpha = Color.alpha(color);
                    int red = Color.red(color);
                    int green = Color.green(color);
                    int blue = Color.blue(color);

                    switch (selectedSegmentationStyle) {
                        case 0:
                            overlayView.setVisibility(View.GONE);
                            isSegmentationEnabled = false;
                            break;

                        case 1: // Red overlay
                            pixels[i] = Color.argb(128, 255, 0, 0); // Semi-transparent red
                            break;

                        case 2: // Blue overlay
                            pixels[i] = Color.argb(128, 0, 0, 255); // Semi-transparent blue
                            break;

                        case 3: // Green overlay
                            pixels[i] = Color.argb(128, 0, 255, 0); // Semi-transparent green
                            break;

                        case 4: // Inverted colors
                            int invertedRed = 255 - red;
                            int invertedGreen = 255 - green;
                            int invertedBlue = 255 - blue;
                            pixels[i] = Color.argb(alpha, invertedRed, invertedGreen, invertedBlue);
                            break;

                        case 5: // Sepia tone
                            int sepiaRed = (int)(red * 0.393 + green * 0.769 + blue * 0.189);
                            int sepiaGreen = (int)(red * 0.349 + green * 0.686 + blue * 0.168);
                            int sepiaBlue = (int)(red * 0.272 + green * 0.534 + blue * 0.131);
                            sepiaRed = Math.min(255, sepiaRed);
                            sepiaGreen = Math.min(255, sepiaGreen);
                            sepiaBlue = Math.min(255, sepiaBlue);
                            pixels[i] = Color.argb(alpha, sepiaRed, sepiaGreen, sepiaBlue);
                            break;

                        case 6: // Pixelation effect
                            if (i % 10 == 0) {
                                int blockGray = (red + green + blue) / 3;
                                pixels[i] = Color.argb(alpha, blockGray, blockGray, blockGray);
                            }
                            break;

                        case 7: // Vignette effect
                            int centerX = width / 2;
                            int centerY = height / 2;
                            double radius = Math.sqrt(Math.pow(x - centerX, 2) + Math.pow(y - centerY, 2));
                            double maxRadius = Math.sqrt(Math.pow(centerX, 2) + Math.pow(centerY, 2));
                            double vignetteFactor = radius / maxRadius;
                            int vignetteGray = (int)(255 - vignetteFactor * 255);
                            pixels[i] = Color.argb(alpha, vignetteGray, vignetteGray, vignetteGray);
                            break;

                        case 8: // Black and White
                            int bw = (red + green + blue) / 3;
                            pixels[i] = Color.argb(alpha, bw, bw, bw);
                            break;

                        case 9: // Brighten
                            int brightRed = Math.min(255, red + 50);
                            int brightGreen = Math.min(255, green + 50);
                            int brightBlue = Math.min(255, blue + 50);
                            pixels[i] = Color.argb(alpha, brightRed, brightGreen, brightBlue);
                            break;

                        case 10: // Darken
                            int darkRed = Math.max(0, red - 50);
                            int darkGreen = Math.max(0, green - 50);
                            int darkBlue = Math.max(0, blue - 50);
                            pixels[i] = Color.argb(alpha, darkRed, darkGreen, darkBlue);
                            break;

                        case 11: // Red Tint
                            pixels[i] = Color.argb(128, Math.min(255, red + 100), green, blue);
                            break;

                        case 12: // Green Tint
                            pixels[i] = Color.argb(128, red, Math.min(255, green + 100), blue);
                            break;

                        case 13: // Blue Tint
                            pixels[i] = Color.argb(128, red, green, Math.min(255, blue + 100));
                            break;

                        case 14: // Warm Tone
                            int warmRed = Math.min(255, red + 50);
                            int warmYellow = Math.min(255, green + 30);
                            pixels[i] = Color.argb(alpha, warmRed, warmYellow, blue);
                            break;

                        case 15: // Cold Tone
                            int coldBlue = Math.min(255, blue + 50);
                            pixels[i] = Color.argb(alpha, red, green, coldBlue);
                            break;

                        case 16: // Increase Contrast
                            int contrastRed = (int) ((red - 128) * 1.5 + 128);
                            int contrastGreen = (int) ((green - 128) * 1.5 + 128);
                            int contrastBlue = (int) ((blue - 128) * 1.5 + 128);
                            contrastRed = Math.min(255, Math.max(0, contrastRed));
                            contrastGreen = Math.min(255, Math.max(0, contrastGreen));
                            contrastBlue = Math.min(255, Math.max(0, contrastBlue));
                            pixels[i] = Color.argb(alpha, contrastRed, contrastGreen, contrastBlue);
                            break;

                        case 17: // Blur effect (Approximation: Reduce sharpness)
                            if (i > 1 && i < width * height - 1) {
                                int avgRed = (red + Color.red(pixels[i - 1]) + Color.red(pixels[i + 1])) / 3;
                                int avgGreen = (green + Color.green(pixels[i - 1]) + Color.green(pixels[i + 1])) / 3;
                                int avgBlue = (blue + Color.blue(pixels[i - 1]) + Color.blue(pixels[i + 1])) / 3;
                                pixels[i] = Color.argb(alpha, avgRed, avgGreen, avgBlue);
                            }
                            break;

                        case 18: // Grayscale background
                            int gray = (red + green + blue) / 3;
                            pixels[i] = Color.argb(alpha, gray, gray, gray);
                            break;

                        default:
                            break;
                    }

                    resultBitmap.setPixel(x, y, pixels[i]);
                }
            } else {
                int cameraPixel = pixels[i];
                resultBitmap.setPixel(x, y, cameraPixel);
            }
        }

        runOnUiThread(() -> {
            overlayView.setOverlayBitmap(resultBitmap);
        });
    }

    private Bitmap rotateBitmapToPortrait(Bitmap bitmap, int currentRotation) {
        Matrix matrix = new Matrix();
        matrix.postRotate(-90);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private Bitmap rotateBitmap(Bitmap bitmap, int rotationDegrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(rotationDegrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private Bitmap yuvToRgb(ImageProxy imageProxy) {
        ImageProxy.PlaneProxy[] planes = imageProxy.getPlanes();
        ByteBuffer yBuffer = planes[0].getBuffer(); // Y
        ByteBuffer uBuffer = planes[1].getBuffer(); // U
        ByteBuffer vBuffer = planes[2].getBuffer(); // V

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        byte[] nv21 = new byte[ySize + uSize + vSize];

        // U and V are swapped
        yBuffer.get(nv21, 0, ySize);
        vBuffer.get(nv21, ySize, vSize);
        uBuffer.get(nv21, ySize + vSize, uSize);

        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, imageProxy.getWidth(), imageProxy.getHeight(), null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, imageProxy.getWidth(), imageProxy.getHeight()), 100, out);
        byte[] imageBytes = out.toByteArray();
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
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

        FFmpegKit.executeAsync(mergeCommand, session -> {
            if (ReturnCode.isSuccess(session.getReturnCode())) {
                Log.d("FFmpeg", "Clips merged successfully");

                // Rename the merged file to the next CVD file (CVD0, CVD1, etc.)
                File mergedFile = new File(getTempOutputFilePath());
                File renamedMergedFile = new File(getExternalFilesDir(null), getNextCVDFileName());
                boolean renamed = mergedFile.renameTo(renamedMergedFile);
                if (renamed) {
                    Log.d("FFmpeg", "Merged file renamed to: " + renamedMergedFile.getAbsolutePath());
                    uploadFinalVideoToFirebase(renamedMergedFile);
                    //
//                    // Re-encode the merged file before uploading
//                    File reEncodedFile = new File(getExternalFilesDir(null), renamedMergedFile.getName());
//                    reEncodeVideo(renamedMergedFile, reEncodedFile);  // Call re-encode method

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

//    private void reEncodeVideo(File originalVideoFile, File outputFile) {
//        String ffmpegCommand = "-i " + originalVideoFile.getAbsolutePath() + " -c:v mpeg4 -crf 23 -preset veryfast " + outputFile.getAbsolutePath();
//
//        FFmpegKit.executeAsync(ffmpegCommand, session -> {
//            if (ReturnCode.isSuccess(session.getReturnCode())) {
//                Log.d("FFmpeg", "Video re-encoded successfully.");
//                uploadFinalVideoToFirebase(outputFile);  // Upload re-encoded video
//            } else {
//                Log.e("FFmpeg", "Re-encoding failed: " + session.getFailStackTrace());
//                Toast.makeText(CreateVideoActivity.this, "Failed to re-encode the video before upload.", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }


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

