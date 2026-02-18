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
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.deepdefender.nagarsewahackthon.R;
import com.google.android.gms.location.*;

import org.tensorflow.lite.Interpreter;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Locale;

public class RegisterComplaintActivity extends AppCompatActivity {

    TextView btnBack, tvAddress;
    EditText etDescription;
    ImageView icCamera;
    Button btnSubmit;

    FusedLocationProviderClient fusedLocationClient;

    Bitmap selectedBitmap;

    Interpreter pipeModel, potholeModel, garbageModel;

    int inputSize = 640;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_complaint);

        btnBack = findViewById(R.id.btnBack);
        tvAddress = findViewById(R.id.tvAddress);
        etDescription = findViewById(R.id.etDescription);
        icCamera = findViewById(R.id.icCamera);
        btnSubmit = findViewById(R.id.btnSubmitComplaint);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        btnBack.setOnClickListener(v -> finish());

        getCurrentLocation();

        loadModels();

        icCamera.setOnClickListener(v -> openGallery());

        btnSubmit.setOnClickListener(v ->
                Toast.makeText(this, "Complaint Submitted", Toast.LENGTH_SHORT).show());
    }

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

    // 🤖 LOAD MODELS
    private void loadModels() {
        try {
            pipeModel = new Interpreter(loadModelFile("PipeLeakage.tflite"));
        } catch (Exception e) {
            pipeModel = null;
        }

        try {
            potholeModel = new Interpreter(loadModelFile("pothole.tflite"));
        } catch (Exception e) {
            potholeModel = null;
        }

        try {
            garbageModel = new Interpreter(loadModelFile("garbage_model.tflite"));
        } catch (Exception e) {
            garbageModel = null;
        }

        Toast.makeText(this, "AI ready (partial allowed)", Toast.LENGTH_SHORT).show();
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

        runOnUiThread(() -> {

            StringBuilder result = new StringBuilder();

            if (pipeConf > 0.5) {
                result.append("Water Leakage (").append(String.format("%.2f", pipeConf)).append(")\n");
            }

            if (potholeConf > 0.5) {
                result.append("Pothole (").append(String.format("%.2f", potholeConf)).append(")\n");
            }

            if (garbageConf > 0.5) {
                result.append("Garbage (").append(String.format("%.2f", garbageConf)).append(")\n");
            }

            if (result.length() > 0) {
                Toast.makeText(this, result.toString(), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "No Issue Found", Toast.LENGTH_LONG).show();
            }
        });
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

            if (confidence > maxConf) {
                maxConf = confidence;
            }
        }

        return maxConf;
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
