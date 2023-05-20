package com.payd.payd;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class ReceiveVerifyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_verify);

        Button verifyButton = findViewById(R.id.verifyButton);
        verifyButton.setOnClickListener(v -> {
            Intent intent = new Intent(ReceiveVerifyActivity.this, ReceiveTransactionStartedActivity.class);
            startActivity(intent);
        });

        Button cancelButton = findViewById(R.id.cancelButton);
        verifyButton.setOnClickListener(v -> {
            Intent intent = new Intent(ReceiveVerifyActivity.this, ReceiveTransactionStartedActivity.class);
            startActivity(intent);
        });
    }
}