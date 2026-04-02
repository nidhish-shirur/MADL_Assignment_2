package com.nid.madl02_49;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText etTitle, etDescription;
    private ImageView ivPreview;
    private MaterialButton btnCapture, btnSelect, btnSave, btnView;
    private android.widget.AutoCompleteTextView spinnerPriority;

    private String currentImagePath = "";
    private android.hardware.SensorManager sensorManager;
    private float acceleration = 0f;
    private float currentAcceleration = 0f;
    private float lastAcceleration = 0f;
    private int editNoteId = -1; // -1 means new note, any other number means we are editing

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Ensure your activity_main.xml has the form layout!

        // 1. Link variables to UI
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        ivPreview = findViewById(R.id.ivPreview);
        btnCapture = findViewById(R.id.btnCapture);
        btnSelect = findViewById(R.id.btnSelect);
        btnSave = findViewById(R.id.btnSave);
        btnView = findViewById(R.id.btnView); // Restore the View Notes button

        // Set up the Priority Dropdown
        spinnerPriority = findViewById(R.id.spinnerPriority);
        String[] priorities = new String[]{"High", "Medium", "Low"};
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                priorities
        );
        spinnerPriority.setAdapter(adapter);

        // 2. Setup Gallery Button Click
        btnSelect.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(intent);
        });

        // 3. Setup Camera Button Click
        btnCapture.setOnClickListener(v -> {
            requestCameraPermissionLauncher.launch(android.Manifest.permission.CAMERA);
        });

        // 4. Setup Save/Update Button Click
        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String description = etDescription.getText().toString().trim();
            String date = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date());
            String priority = spinnerPriority.getText().toString();

            if (title.isEmpty()) {
                etTitle.setError("Please enter a title");
                return;
            }

            DatabaseHelper dbHelper = new DatabaseHelper(MainActivity.this);
            boolean isSuccess;

            if (editNoteId == -1) {
                isSuccess = dbHelper.insertNote(title, description, currentImagePath, date, priority);
            } else {
                isSuccess = dbHelper.updateNote(editNoteId, title, description, currentImagePath, priority);
            }

            if (isSuccess) {
                Toast.makeText(MainActivity.this, "Saved Successfully!", Toast.LENGTH_SHORT).show();
                if (editNoteId != -1) {
                    finish();
                } else {
                    etTitle.setText("");
                    etDescription.setText("");
                    ivPreview.setImageResource(android.R.drawable.ic_menu_gallery);
                    currentImagePath = "";
                }
            } else {
                Toast.makeText(MainActivity.this, "Error saving note.", Toast.LENGTH_SHORT).show();
            }
        });

        // 5. Setup View Notes Button (RESTORED)
        btnView.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ViewNotesActivity.class);
            startActivity(intent);
        });

        // 6. Check if we are editing
        if (getIntent().hasExtra("NOTE_ID")) {
            editNoteId = getIntent().getIntExtra("NOTE_ID", -1);
            etTitle.setText(getIntent().getStringExtra("TITLE"));
            etDescription.setText(getIntent().getStringExtra("DESC"));
            currentImagePath = getIntent().getStringExtra("IMAGE");
            spinnerPriority.setText(getIntent().getStringExtra("PRIORITY"), false);
            btnSave.setText("Update Note");

            if (currentImagePath != null && !currentImagePath.isEmpty() && !currentImagePath.equals("camera_thumbnail_bitmap")) {
                ivPreview.setImageURI(Uri.parse(currentImagePath));
            }
        }

        // 7. Sensor
        sensorManager = (android.hardware.SensorManager) getSystemService(android.content.Context.SENSOR_SERVICE);
        android.hardware.Sensor accelerometer = sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            sensorManager.registerListener(sensorListener, accelerometer, android.hardware.SensorManager.SENSOR_DELAY_NORMAL);
        }
        acceleration = 10f;
        currentAcceleration = android.hardware.SensorManager.GRAVITY_EARTH;
        lastAcceleration = android.hardware.SensorManager.GRAVITY_EARTH;

        // 8. Worker
        setupBackgroundWork();
    }

    private void setupBackgroundWork() {
        PeriodicWorkRequest noteWorkRequest =
                new PeriodicWorkRequest.Builder(NotesWorker.class, 15, TimeUnit.MINUTES)
                        .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "NoteNotificationWork",
                ExistingPeriodicWorkPolicy.KEEP,
                noteWorkRequest
        );
    }

    private final android.hardware.SensorEventListener sensorListener = new android.hardware.SensorEventListener() {
        @Override
        public void onSensorChanged(android.hardware.SensorEvent event) {
            float x = event.values[0], y = event.values[1], z = event.values[2];
            lastAcceleration = currentAcceleration;
            currentAcceleration = (float) Math.sqrt((double) (x * x + y * y + z * z));
            acceleration = acceleration * 0.9f + (currentAcceleration - lastAcceleration);

            if (acceleration > 12) {
                new com.google.android.material.dialog.MaterialAlertDialogBuilder(MainActivity.this)
                        .setTitle("Sensor Alert")
                        .setMessage("Device motion detected!")
                        .setPositiveButton("OK", null)
                        .show();
            }
        }
        @Override
        public void onAccuracyChanged(android.hardware.Sensor sensor, int accuracy) {}
    };

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        ivPreview.setImageURI(imageUri);
                        currentImagePath = imageUri.toString();
                    }
                }
            }
    );

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    if (extras != null) {
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        ivPreview.setImageBitmap(imageBitmap);
                        currentImagePath = "camera_thumbnail_bitmap";
                    }
                }
            }
    );

    private final ActivityResultLauncher<String> requestCameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    cameraLauncher.launch(intent);
                } else {
                    Toast.makeText(this, "Camera permission is required to take photos", Toast.LENGTH_SHORT).show();
                }
            });
}