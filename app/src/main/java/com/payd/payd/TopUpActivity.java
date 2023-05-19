package com.payd.payd;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.payd.payd.core.DigitalCheque;
import com.payd.payd.core.Util;
import com.payd.payd.util.TransactionStorageManager;
import com.payd.payd.util.UserSession;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.DefaultGasProvider;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TopUpActivity extends AppCompatActivity {
    TransactionStorageManager transactionStorageManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_up);
        transactionStorageManager = new TransactionStorageManager(this);

        ((Button) findViewById(R.id.TopUpButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Top Up", "onClick:    top up requres");
                DigitalCheque digitalCheque = new DigitalCheque(UUID.randomUUID(), Integer.parseInt(((TextView) findViewById(R.id.TopUpAmount)).getText().toString()), System.currentTimeMillis(), "Raja", UserSession.me.username, "hjjkh", "kuhiuhi");
                transactionStorageManager.addDigitalChequeAsync(digitalCheque);
                Intent intent = new Intent(getApplicationContext(), WalletActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
//                Web3j web3j = Web3j.build(new HttpService("https://rpc-mumbai.maticvigil.com/"));
//                // payee
////            payerCredentials = org.web3j.crypto.Credentials.create("ee53d41642d16bc6951a4034ec377ff238b921fcadff587d3296932cabf02581"); // god
////            payeeCredentials = org.web3j.crypto.Credentials.create("3f425bbce5ae3b7d765f3078a43a5c6831102b8b281aaa06e89e5e1974817719"); // me
////            EthConnection.Soin soin = EthConnection.Soin.load("0xc5Fa8301b7Af4f19F2eE55D903e6995eAC4D417d",web3j,payerCredentials,GAS_PRICE, BigInteger.valueOf(6721975));
//                long chainIdOfPolygon = 80001;
//                web3j = Web3j.build(new HttpService("https://rpc-mumbai.maticvigil.com/"));
//
//                Credentials bridgeCreds = Credentials.create("de47f1008d801b6bf4b0d24933b3e514dd93bdf99a764031a9f6458eb0731520");
//                TransactionManager bridgeTokenTxManager = new RawTransactionManager(
//                        web3j, bridgeCreds, chainIdOfPolygon);
//                EthConnection.Soin bridgeToken = EthConnection.Soin.load("0xc5Fa8301b7Af4f19F2eE55D903e6995eAC4D417d", web3j, bridgeTokenTxManager, new DefaultGasProvider());
//
//                Executor executor = Executors.newSingleThreadExecutor();
//                executor.execute(() -> {
//                    try {
//                        bridgeToken.transfer("0xcD603580a0D57C2F77edC116eE5b17b689B9D2DD", BigInteger.valueOf(Integer.parseInt(((TextView) findViewById(R.id.TopUpAmount)).getText().toString()))).send();
//                    } catch (Exception e) {
//                        throw new RuntimeException(e);
//                    }
//                });
//                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}