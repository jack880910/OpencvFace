package com.example.opencvface;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class FaceTaskResultActivity extends AppCompatActivity {
    private static final String TAG = "CSHsu::FaceRecognition";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_task_result);
        Intent intent = getIntent();
        String response = intent.getStringExtra("response");
        TextView frResponseTextView = findViewById(R.id.frResponseTextView);
        frResponseTextView.setText(response);

        Button btnOk = findViewById(R.id.btnOk);
        btnOk.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
