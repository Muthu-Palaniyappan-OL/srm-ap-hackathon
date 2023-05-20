package com.payd.payd;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

public class SendWaitingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_waiting);

        ImageView img=findViewById(R.id.WaitingForPeersImage);
        img.setOnClickListener(v -> {
            Intent intent = new Intent(SendWaitingActivity.this, SendVerifyActivity.class);
            startActivity(intent);
        });
    }
}