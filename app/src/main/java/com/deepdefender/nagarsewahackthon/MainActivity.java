package com.deepdefender.nagarsewahackthon;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private int progressStatus = 0;
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash); // ⚠ load splash layout

        progressBar = findViewById(R.id.progressBar);

        startLoading();
    }

    private void startLoading() {

        new Thread(() -> {

            while (progressStatus < 100) {

                progressStatus += 2;

                try {
                    Thread.sleep(40);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                handler.post(() -> progressBar.setProgress(progressStatus));
            }

            handler.post(() -> {

                // Change HomeActivity to your next screen
                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();

            });

        }).start();
    }
}
