package com.deepdefender.nagarsewahackthon.UserFragments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.deepdefender.nagarsewahackthon.R;
import com.google.android.gms.location.*;
import com.google.firebase.firestore.FirebaseFirestore;

import org.tensorflow.lite.Interpreter;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

public class RegisterComplaintActivity extends AppCompatActivity {

    TextView btnBack, tvAddress;
    EditText etDescription;
    ImageView icCamera;
    Button btnSubmit;

    FusedLocationProviderClient fusedLocationClient;

    Bitmap selectedBitmap;

    Interpreter pipeModel, potholeModel, garbageModel;

    int inputSize = 640;

    String detectedIssues = "None";

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_complaint);

        btnBack = findViewById(R.id.btnBack);
        tvAddress = findViewById(R.id.tvAddress);
        etDescription = findViewById(R.id.etDescription);
        icCamera = findViewById(R.id.icCamera);
        btnSubmit = findViewById(R.id.btnSubmitComplaint);

        db = FirebaseFirestore.getInstance();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        btnBack.setOnClickListener(v -> finish());

        getCurrentLocation();
        loadModels();

        icCamera.setOnClickListener(v -> openGallery());

        btnSubmit.setOnClickListener(v -> submitComplaint());
    }

    // 📍 LOCATION
    private void getCurrentLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                try {
                    Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                    List<Address> addresses = geocoder.getFromLocation(
                            location.getLatitude(),
                            location.getLongitude(),
                            1
                    );

                    if (addresses != null && addresses.size() > 0) {
                        tvAddress.setText(addresses.get(0).getAddressLine(0));
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // 📷 IMAGE PICKER
    ActivityResultLauncher<Intent> imageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {

                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {

                    Uri uri = result.getData().getData();

                    try {
                        selectedBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                        icCamera.setImageBitmap(selectedBitmap);

                        new Thread(() -> runAllModels(selectedBitmap)).start();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imageLauncher.launch(intent);
    }

    // 🤖 LOAD MODELS (PARTIAL ALLOWED)
    private void loadModels() {

        try {
            pipeModel = new Interpreter(loadModelFile("PipeLeakage.tflite"));
        } catch (Exception e) { pipeModel = null; }

        try {
            potholeModel = new Interpreter(loadModelFile("pothole.tflite"));
        } catch (Exception e) { potholeModel = null; }

        try {
            garbageModel = new Interpreter(loadModelFile("garbage_model.tflite"));
        } catch (Exception e) { garbageModel = null; }

        Toast.makeText(this, "AI ready", Toast.LENGTH_SHORT).show();
    }

    private ByteBuffer loadModelFile(String modelName) throws Exception {

        InputStream is = getAssets().open(modelName);
        byte[] model = new byte[is.available()];
        is.read(model);
        is.close();

        ByteBuffer buffer = ByteBuffer.allocateDirect(model.length);
        buffer.order(ByteOrder.nativeOrder());
        buffer.put(model);
        buffer.rewind();

        return buffer;
    }

    // 🤖 RUN ALL MODELS
    private void runAllModels(Bitmap bitmap) {

        float pipeConf = runYoloModel(pipeModel, bitmap);
        float potholeConf = runYoloModel(potholeModel, bitmap);
        float garbageConf = runYoloModel(garbageModel, bitmap);

        StringBuilder result = new StringBuilder();

        if (pipeConf > 0.5)
            result.append("Water Leakage, ");

        if (potholeConf > 0.5)
            result.append("Pothole, ");

        if (garbageConf > 0.5)
            result.append("Garbage, ");

        if (result.length() > 0) {
            detectedIssues = result.substring(0, result.length() - 2);
        } else {
            detectedIssues = "None";
        }

        runOnUiThread(() ->
                Toast.makeText(this, "Detected: " + detectedIssues, Toast.LENGTH_LONG).show());
    }

    // 🤖 GENERIC YOLO RUNNER
    private float runYoloModel(Interpreter model, Bitmap bitmap) {

        if (model == null) return 0f;

        Bitmap resized = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true);

        ByteBuffer input = ByteBuffer.allocateDirect(4 * inputSize * inputSize * 3);
        input.order(ByteOrder.nativeOrder());

        for (int y = 0; y < inputSize; y++) {
            for (int x = 0; x < inputSize; x++) {

                int pixel = resized.getPixel(x, y);

                input.putFloat(((pixel >> 16) & 0xFF) / 255f);
                input.putFloat(((pixel >> 8) & 0xFF) / 255f);
                input.putFloat((pixel & 0xFF) / 255f);
            }
        }

        float[][][] output = new float[1][5][8400];
        model.run(input, output);

        float maxConf = 0f;

        for (int i = 0; i < 8400; i++) {
            float confidence = output[0][4][i];
            if (confidence > maxConf) maxConf = confidence;
        }

        return maxConf;
    }

    // 🔥 SUBMIT TO FIRESTORE
    private void submitComplaint() {

        String description = etDescription.getText().toString().trim();

        if (description.isEmpty()) {
            Toast.makeText(this, "Enter description", Toast.LENGTH_SHORT).show();
            return;
        }

        String imageBase64 = "";
        if (selectedBitmap != null) {
            imageBase64 = bitmapToBase64(selectedBitmap);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("description", description);
        map.put("address", tvAddress.getText().toString());
        map.put("issues", detectedIssues);
        map.put("status", "Pending");
        map.put("timestamp", System.currentTimeMillis());
        map.put("imageBase64", imageBase64);

        db.collection("Complaints")
                .add(map)
                .addOnSuccessListener(doc -> {
                    Toast.makeText(this, "Complaint uploaded", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                });
    }

    // 🖼 BITMAP → BASE64
    private String bitmapToBase64(Bitmap bitmap) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos);
        byte[] imageBytes = baos.toByteArray();

        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    // 🔐 PERMISSION RESULT
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == 1 && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            getCurrentLocation();
        }
    }
}
