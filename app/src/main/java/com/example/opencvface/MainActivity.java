package com.example.opencvface;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    public static String username;
    public static String password;
    Button btnEnter;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnEnter = findViewById(R.id.btnEnter);
        btnEnter.setOnClickListener(v -> {
            TextView tvUsername = findViewById(R.id.username);
            username = tvUsername.getText().toString().trim();
            TextView tvPassword = findViewById(R.id.password);
            password = tvPassword.getText().toString().trim();

            Intent intent = new Intent(MainActivity.this, HomePageActivity.class);
            intent.putExtra("faceLabel", username);
            User.name = username;
            startActivity(intent);

            //登錄後先查詢資料
            String key = "user1";
            new Query().execute(key);
        });

    }
}