package com.payd.payd;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class SendTransactionStartedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_transaction_started);

        TextView amt =findViewById(R.id.AmountText);
        amt.setOnClickListener(v -> {
            Intent intent = new Intent(SendTransactionStartedActivity.this, SendTransactionLogActivity.class);
            startActivity(intent);
        });
    }
}