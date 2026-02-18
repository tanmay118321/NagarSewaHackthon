package com.deepdefender.nagarsewahackthon;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {

    private static final int LOCATION_REQUEST_CODE = 101;

    private EditText etName, etEmail, etPhone, etCity, etPassword, etConfirmPassword;
    private AppCompatButton btnRegister;
    private TextView tvLogin;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registration);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        detectCity();

        btnRegister.setOnClickListener(v -> registerUser());

        tvLogin.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class)));
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etCity = findViewById(R.id.etCity);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
    }

    private void registerUser() {

        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String city = etCity.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirm = etConfirmPassword.getText().toString().trim();

        // FULL VALIDATION

        if (TextUtils.isEmpty(name)) {
            etName.setError("Full name required");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter valid email");
            return;
        }

        if (phone.length() != 10) {
            etPhone.setError("Enter valid 10 digit phone");
            return;
        }

        if (TextUtils.isEmpty(city)) {
            Toast.makeText(this,
                    "City not detected. Enable location.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Minimum 6 characters required");
            return;
        }

        if (!password.equals(confirm)) {
            etConfirmPassword.setError("Passwords do not match");
            return;
        }

        btnRegister.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {

                    String uid = mAuth.getCurrentUser().getUid();

                    Map<String, Object> user = new HashMap<>();
                    user.put("uid", uid);
                    user.put("name", name);
                    user.put("email", email);
                    user.put("phone", phone);
                    user.put("city", city);
                    user.put("role", "user"); // Default role

                    db.collection("Users")
                            .document(uid)
                            .set(user)
                            .addOnSuccessListener(unused -> {

                                Toast.makeText(this,
                                        "Registration Successful!",
                                        Toast.LENGTH_SHORT).show();

                                startActivity(new Intent(this,
                                        HomeActivity.class));
                                finish();
                            });

                })
                .addOnFailureListener(e -> {

                    btnRegister.setEnabled(true);

                    Toast.makeText(this,
                            e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void detectCity() {

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        Geocoder geocoder = new Geocoder(this,
                                Locale.getDefault());
                        try {
                            List<Address> addresses =
                                    geocoder.getFromLocation(
                                            location.getLatitude(),
                                            location.getLongitude(),
                                            1);

                            if (addresses != null && !addresses.isEmpty()) {
                                etCity.setText(
                                        addresses.get(0).getLocality());
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }
}
