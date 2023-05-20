package com.payd.payd;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.airbnb.lottie.LottieAnimationView;
import com.payd.payd.util.TransactionStorageManager;

public class SendTransactionLogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_transaction_log);

        Button doneButton = findViewById(R.id.DoneButton);
        doneButton.setOnClickListener(v -> {
            Intent intent = new Intent(SendTransactionLogActivity.this, WalletActivity.class);
            startActivity(intent);
        });

        Button anotherTranButton = findViewById(R.id.DoAnotherTranButton);
        anotherTranButton.setOnClickListener(v -> {
            Intent intent = new Intent(SendTransactionLogActivity.this, WalletActivity.class);
            startActivity(intent);
        });

        setTranAnimation(true);

    }

    void setTranAnimation(boolean SuccessStatus){
        LottieAnimationView TranAnim=findViewById(R.id.TranAnimation);
        if(SuccessStatus){
            TranAnim.setAnimation(R.raw.green_tick);
        }
        else{
            TranAnim.setAnimation(R.raw.failed_red);
        }
    }
}