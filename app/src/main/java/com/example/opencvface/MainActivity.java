package com.example.opencvface;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    public static String identity;
    public static String password;
    Button btnEnter;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnEnter = findViewById(R.id.btnEnter);
        btnEnter.setOnClickListener(v -> {
            TextView tvUsername = findViewById(R.id.identity);
            identity = tvUsername.getText().toString().trim();
            TextView tvPassword = findViewById(R.id.password);
            password = tvPassword.getText().toString().trim();

            Intent intent = new Intent(MainActivity.this, HomePageActivity.class);
            intent.putExtra("faceLabel", identity);
            User.key = identity;
            startActivity(intent);

            //if private == null, genkey then query; else, query;
            try {
                if (User.private_key == null) {
                    DigitalSignature.genKeypair(User.key);
                    new Query().execute(User.key);
                } else {
                    new Query().execute(User.key);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        });

    }
}