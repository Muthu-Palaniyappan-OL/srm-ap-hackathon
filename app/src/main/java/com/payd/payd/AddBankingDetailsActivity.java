package com.payd.payd;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

public class AddBankingDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_banking_details);

        Button addBankDetailsButton = findViewById(R.id.SignUpButton);
        CheckBox isVendor= findViewById(R.id.isVendor);
        addBankDetailsButton.setOnClickListener(v -> {
            Intent intent;
            if(isVendor.isChecked()){
                intent = new Intent(AddBankingDetailsActivity.this, VendorDashboardActivity.class);
            }
            else{
                intent = new Intent(AddBankingDetailsActivity.this, WalletActivity.class);
            }
            startActivity(intent);
        });

        TextView toWallet = findViewById(R.id.addDetailsLaterMessage);
        toWallet.setOnClickListener(v -> {
            Intent intent=new Intent(AddBankingDetailsActivity.this, WalletActivity.class);
            startActivity(intent);
        });
    }
}