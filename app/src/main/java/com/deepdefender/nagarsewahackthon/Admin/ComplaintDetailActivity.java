package com.deepdefender.nagarsewahackthon.Admin;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.deepdefender.nagarsewahackthon.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.FirebaseFirestore;

public class ComplaintDetailActivity extends AppCompatActivity {

    ImageView ivPreview;
    TextView tvAddress, tvDescription, tvStatus, tvTime;
    ChipGroup chipGroupIssues;
    Button btnResolve, btnAssign;

    FirebaseFirestore db;
    String docId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint_detail);

        ivPreview = findViewById(R.id.ivPreview);
        tvAddress = findViewById(R.id.tvAddress);
        tvDescription = findViewById(R.id.tvDescription);
        tvStatus = findViewById(R.id.tvStatus);
        tvTime = findViewById(R.id.tvTime);
        chipGroupIssues = findViewById(R.id.chipGroupIssues);
        btnResolve = findViewById(R.id.btnResolve);
        btnAssign = findViewById(R.id.btnAssign);

        db = FirebaseFirestore.getInstance();

        docId = getIntent().getStringExtra("docId");

        String issues = getIntent().getStringExtra("issues");
        String address = getIntent().getStringExtra("address");
        String description = getIntent().getStringExtra("description");
        String status = getIntent().getStringExtra("status");
        String base64 = getIntent().getStringExtra("imageBase64");

        tvAddress.setText(address);
        tvDescription.setText(description);
        tvStatus.setText(status);
        tvTime.setText("Reported recently");

        // 🔹 Load image
        if (base64 != null && !base64.isEmpty()) {
            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            ivPreview.setImageBitmap(bitmap);
        }

        // 🔹 Load issues as chips
        if (issues != null) {
            String[] issueArray = issues.split(",");
            for (String issue : issueArray) {
                Chip chip = new Chip(this);
                chip.setText(issue.trim());
                chip.setBackgroundResource(R.drawable.bg_chip);
                chip.setTextColor(getResources().getColor(R.color.black));
                chipGroupIssues.addView(chip);
            }
        }

        btnResolve.setOnClickListener(v -> resolveComplaint());

        btnAssign.setOnClickListener(v -> assignComplaint());
    }

    private void resolveComplaint() {
        db.collection("Complaints")
                .document(docId)
                .update("status", "Resolved")
                .addOnSuccessListener(unused -> finish());
    }

    private void assignComplaint() {
        db.collection("Complaints")
                .document(docId)
                .update("status", "Assigned")
                .addOnSuccessListener(unused -> finish());
    }
}
