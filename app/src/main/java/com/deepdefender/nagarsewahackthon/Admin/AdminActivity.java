package com.deepdefender.nagarsewahackthon.Admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.deepdefender.nagarsewahackthon.R;

public class AdminActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin);

        ImageView ivAnalytics = findViewById(R.id.ivAnalytics);

        ivAnalytics.setOnClickListener(v -> {
            Intent intent = new Intent(this,AnalyticsActivity.class);
            startActivity(intent);
        });



    }
}