package com.deepdefender.nagarsewahackthon;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private ImageView eyeIcon, btnGoogle;
    private AppCompatButton btnLogin;
    private TextView tvForgot, tvSignUp;

    private boolean isPasswordVisible = false;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();


        initViews();
        setupListeners();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        eyeIcon = findViewById(R.id.eyeIcon);
        btnLogin = findViewById(R.id.btnLogin);
        tvForgot = findViewById(R.id.tvForgot);
        tvSignUp = findViewById(R.id.tvSignUp);
        btnGoogle = findViewById(R.id.btnGoogle);
    }

    private void setupListeners() {

        btnLogin.setOnClickListener(v -> loginUser());

        tvForgot.setOnClickListener(v -> resetPassword());

        tvSignUp.setOnClickListener(v ->
                startActivity(new Intent(this, RegistrationActivity.class)));

        if (btnGoogle != null) {
            btnGoogle.setOnClickListener(v ->
                    Toast.makeText(this,
                            "Google Login Integration Pending",
                            Toast.LENGTH_SHORT).show());
        }

        eyeIcon.setOnClickListener(v -> togglePassword());
    }

    private void togglePassword() {

        if (isPasswordVisible) {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT |
                    InputType.TYPE_TEXT_VARIATION_PASSWORD);
            eyeIcon.setImageResource(R.drawable.ic_eye);
        } else {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT |
                    InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            eyeIcon.setImageResource(R.drawable.ic_eyeoff);
        }

        isPasswordVisible = !isPasswordVisible;
        etPassword.setSelection(etPassword.getText().length());
    }

    private void loginUser() {

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter valid email");
            return;
        }

        if (TextUtils.isEmpty(password) || password.length() < 6) {
            etPassword.setError("Enter valid password");
            return;
        }

        btnLogin.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {

                    btnLogin.setEnabled(true);

                    // 🔥 ROLE CHECK
                    if (email.equals("admin@gmail.com") && password.equals("admin@123")) {

                        Toast.makeText(this,
                                "Welcome Admin",
                                Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(this,
                                com.deepdefender.nagarsewahackthon.Admin.AdminActivity.class));

                    } else if (email.equals("super11@gmail.com") && password.equals("super@123")) {

                        Toast.makeText(this,
                                "Welcome Super Admin",
                                Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(this,
                                SuperAdminDashboardActivity.class));

                    } else {

                        Toast.makeText(this,
                                "Login Successful",
                                Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(this,
                                HomeActivity.class));
                    }

                    finish();

                })
                .addOnFailureListener(e -> {

                    btnLogin.setEnabled(true);

                    Toast.makeText(this,
                            e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }


    private void resetPassword() {

        String email = etEmail.getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter registered email first");
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(unused ->
                        Toast.makeText(this,
                                "Reset email sent!",
                                Toast.LENGTH_LONG).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }
}
