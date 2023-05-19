package com.payd.payd;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.payd.payd.util.UserSession;

public class ResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        Button doneButton = findViewById(R.id.DoneButton);
        doneButton.setOnClickListener(v -> {
            Intent intent = new Intent(ResultActivity.this, WalletActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        boolean result = getIntent().getBooleanExtra("result", false);
        String amount = getIntent().getStringExtra("amount");
        boolean send = getIntent().getBooleanExtra("send", false);
        setTranAnimation(result);
        Log.d("Log", "onStart: "+result+amount+send);
        String status;
        if (send) {
            if (result) {
                status = "Sent "+amount+" to "+ UserSession.clientName;
            } else {
                status = "Unabled to send "+amount+" to "+ UserSession.clientName;
            }
        } else {
            if (result) {
                status = "Received "+amount+" from "+ UserSession.clientName;
            } else {
                status = "Unable to Receive from "+ UserSession.clientName;
            }
        }
        ((TextView) findViewById(R.id.TranStatusMessage)).setText(status);
        ((TextView) findViewById(R.id.TranStatusHeading)).setText(result ? "Transaction Succeded!" : "Transaction Failed!");
        ((TextView) findViewById(R.id.ReceivedAmountHeading)).setText(send ? "Sent Amount" : "Received Amount");
        ((TextView) findViewById(R.id.AmountValue)).setText(amount);
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