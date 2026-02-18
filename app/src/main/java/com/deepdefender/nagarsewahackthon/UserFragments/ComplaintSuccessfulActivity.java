package com.deepdefender.nagarsewahackthon.UserFragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.deepdefender.nagarsewahackthon.R;
import com.google.android.material.button.MaterialButton;

public class ComplaintSuccessfulActivity extends AppCompatActivity {

    MaterialButton btnHome;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_complaint_successful);

        btnHome = findViewById(R.id.btnHome);

        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ComplaintSuccessfulActivity.this,RegisterComplaintActivity.class);
                startActivity(intent);

            }
        });

    }
}