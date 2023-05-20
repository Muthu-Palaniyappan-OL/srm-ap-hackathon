package com.payd.payd;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class ReceiveTransactionStartedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_transaction_started);

        TextView amt = findViewById(R.id.AmountText);
        amt.setOnClickListener(v -> {
            Intent intent = new Intent(ReceiveTransactionStartedActivity.this, ResultActivity.class);
            startActivity(intent);
        });
    }
}