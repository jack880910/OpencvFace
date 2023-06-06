package com.example.opencvface;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class HomePageActivity extends AppCompatActivity {
    Button btnFace;
    Button btnQrcode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        btnFace = findViewById(R.id.btnFace);
        btnFace.setOnClickListener(v -> {

            Intent intent = new Intent(HomePageActivity.this, FaceDetectionActivity.class);
            intent.putExtra("faceLabel", User.key);
            startActivity(intent);
        });

        btnQrcode = findViewById(R.id.btnQrcode);
        btnQrcode.setOnClickListener(view -> {
            Intent intent2 = new Intent(HomePageActivity.this, QrcodeActivity.class);
            startActivity(intent2);
        });
    }
}