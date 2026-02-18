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

import org.json.JSONArray;
import org.json.JSONObject;
import org.tensorflow.lite.Interpreter;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

import okhttp3.*;

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
    String grokDetectedIssues = "";

    FirebaseFirestore db;

    private static final String GROK_API_KEY = "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";

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

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
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

                    if (addresses != null && !addresses.isEmpty()) {
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
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imageLauncher.launch(intent);
    }

    // 🤖 LOAD MODELS
    private void loadModels() {

        try { pipeModel = new Interpreter(loadModelFile("PipeLeakage.tflite")); }
        catch (Exception e) { pipeModel = null; }

        try { potholeModel = new Interpreter(loadModelFile("best_float32.tflite")); }
        catch (Exception e) { potholeModel = null; }

        try { garbageModel = new Interpreter(loadModelFile("garbage_model.tflite")); }
        catch (Exception e) { garbageModel = null; }

        Toast.makeText(this, "AI Ready", Toast.LENGTH_SHORT).show();
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

    // 🤖 YOLO DETECTION
    private void runAllModels(Bitmap bitmap) {

        float pipeConf = runYoloModel(pipeModel, bitmap);
        float potholeConf = runYoloModel(potholeModel, bitmap);
        float garbageConf = runYoloModel(garbageModel, bitmap);

        Set<String> results = new LinkedHashSet<>();

        if (pipeConf > 0.5) results.add("Water Leakage");
        if (potholeConf > 0.5) results.add("Pothole");
        if (garbageConf > 0.5) results.add("Garbage");

        detectedIssues = results.isEmpty() ? "None" : String.join(", ", results);

        runOnUiThread(() ->
                Toast.makeText(this,
                        "Image Detected: " + detectedIssues,
                        Toast.LENGTH_LONG).show());
    }

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

    // 🚀 SUBMIT
    private void submitComplaint() {

        String description = etDescription.getText().toString().trim();

        if (description.isEmpty()) {
            Toast.makeText(this, "Enter description", Toast.LENGTH_SHORT).show();
            return;
        }

        detectWithGrok(description); // will fallback if fails
    }

    // 🤖 GROK AI
    private void detectWithGrok(String description) {

        if (GROK_API_KEY.equals("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")) {
            uploadFinalComplaint(); // fallback
            return;
        }

        OkHttpClient client = new OkHttpClient();

        try {

            JSONObject json = new JSONObject();
            json.put("model", "grok-2-latest");

            JSONArray messages = new JSONArray();

            JSONObject system = new JSONObject();
            system.put("role", "system");
            system.put("content",
                    "Classify in English language (words spelling) into: Road Issue, Garbage Issue, Water Leakage, Electricity Issue, General Complaint. Return comma separated.");
            messages.put(system);

            JSONObject user = new JSONObject();
            user.put("role", "user");
            user.put("content", description);
            messages.put(user);

            json.put("messages", messages);

            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json"),
                    json.toString()
            );

            Request request = new Request.Builder()
                    .url("https://api.x.ai/v1/chat/completions")
                    .addHeader("Authorization", "Bearer " + GROK_API_KEY)
                    .post(body)
                    .build();

            new Thread(() -> {
                try {
                    Response response = client.newCall(request).execute();
                    String res = response.body().string();

                    JSONObject obj = new JSONObject(res);
                    grokDetectedIssues = obj.getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content");

                } catch (Exception e) {
                    grokDetectedIssues = "";
                }

                runOnUiThread(this::uploadFinalComplaint);
            }).start();

        } catch (Exception e) {
            uploadFinalComplaint();
        }
    }

    // 🔥 FIRESTORE UPLOAD
    private void uploadFinalComplaint() {

        Set<String> issueSet = new LinkedHashSet<>();

        if (!detectedIssues.equals("None"))
            issueSet.addAll(Arrays.asList(detectedIssues.split(", ")));

        if (grokDetectedIssues != null && !grokDetectedIssues.isEmpty())
            issueSet.addAll(Arrays.asList(grokDetectedIssues.split(", ")));

        String finalIssues = issueSet.isEmpty() ? "General Complaint"
                : String.join(", ", issueSet);

        Map<String, Object> map = new HashMap<>();
        map.put("description", etDescription.getText().toString());
        map.put("address", tvAddress.getText().toString());
        map.put("issues", finalIssues);
        map.put("status", "Pending");
        map.put("timestamp", System.currentTimeMillis());
        map.put("imageBase64",
                selectedBitmap != null ? bitmapToBase64(selectedBitmap) : "");

        db.collection("Complaints")
                .add(map)
                .addOnSuccessListener(doc -> {
                    Toast.makeText(RegisterComplaintActivity.this,
                            "Complaint Uploaded",
                            Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(RegisterComplaintActivity.this,
                            ComplaintSuccessfulActivity.class);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(RegisterComplaintActivity.this,
                            "Upload Failed",
                            Toast.LENGTH_LONG).show();
                });

    }



    private String bitmapToBase64(Bitmap bitmap) {

        // 🔹 Resize to max 800px
        int maxSize = 800;

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float ratio = Math.min(
                (float) maxSize / width,
                (float) maxSize / height
        );

        int newWidth = Math.round(width * ratio);
        int newHeight = Math.round(height * ratio);

        Bitmap resized = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);

        // 🔹 Compress to reduce size
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        resized.compress(Bitmap.CompressFormat.JPEG, 40, baos);

        byte[] bytes = baos.toByteArray();

        // 🔹 SAFETY CHECK (< 1MB)
        if (bytes.length > 900000) {  // ~0.9MB safe
            baos.reset();
            resized.compress(Bitmap.CompressFormat.JPEG, 25, baos);
            bytes = baos.toByteArray();
        }

        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            getCurrentLocation();
        }
    }
}
