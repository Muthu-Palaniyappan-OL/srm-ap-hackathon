package com.payd.payd;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Message;
import android.util.Log;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.reflect.TypeToken;
import com.payd.payd.core.DigitalCheque;
import com.payd.payd.core.User;
import com.payd.payd.core.Util;
import com.payd.payd.util.Popups;
import com.payd.payd.util.SyncTransactionData;
import com.payd.payd.util.TransactionStorageManager;
import com.payd.payd.util.UserSession;
import com.payd.payd.util.Utils;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.DefaultGasProvider;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import javax.net.ssl.SSLContext;

public class NetworkChangeReceiver extends BroadcastReceiver {
    Activity activity;
    Web3j web3j;
    Credentials payerCredentials, payeeCredentials;
    RequestQueue requestQueue;
    public NetworkChangeReceiver(Activity activity) {
        this.activity = activity;
        try {
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, null, new SecureRandom());
            requestQueue = Volley.newRequestQueue(activity.getApplicationContext(), new HurlStack(null, sslContext.getSocketFactory()));
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        // do something with isConnected
        if (isConnected) {
            Popups.snack(activity, "Connected to network");
            syncTransactions(amountChanged -> {});
//            web3j = Web3j.build(new HttpService("https://rpc-mumbai.maticvigil.com/"));
            // payee
//            payerCredentials = org.web3j.crypto.Credentials.create("ee53d41642d16bc6951a4034ec377ff238b921fcadff587d3296932cabf02581"); // god
//            payeeCredentials = org.web3j.crypto.Credentials.create("3f425bbce5ae3b7d765f3078a43a5c6831102b8b281aaa06e89e5e1974817719"); // me
//            EthConnection.Soin soin = EthConnection.Soin.load("0xc5Fa8301b7Af4f19F2eE55D903e6995eAC4D417d",web3j,payerCredentials,GAS_PRICE, BigInteger.valueOf(6721975));
//            long chainIdOfPolygon = 80001;
//            Web3j web3j = Web3j.build(new HttpService("https://rpc-mumbai.maticvigil.com/"));
//
//            Credentials bridgeCreds = Credentials.create("ee53d41642d16bc6951a4034ec377ff238b921fcadff587d3296932cabf02581");
//            TransactionManager bridgeTokenTxManager = new RawTransactionManager(
//                web3j, bridgeCreds, chainIdOfPolygon);
//            EthConnection.Soin bridgeToken = EthConnection.Soin.load("0xc5Fa8301b7Af4f19F2eE55D903e6995eAC4D417d", web3j, bridgeTokenTxManager, new DefaultGasProvider());

//            activity.findViewById(R.id.sync).setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Executor executor = Executors.newSingleThreadExecutor();
//                    executor.execute(() -> {
//                        TransactionStorageManager transactionStorageManager = new TransactionStorageManager(activity);
//                        int amount = transactionStorageManager.getBalance();
//                        Log.d("Amount", "onReceive: Amount "+amount);
//                        try {
//                            bridgeToken.transfer("0x1Bf8258F51Bf52C4E3fAF55C04a9414cbC7db925", BigInteger.valueOf(amount)).send();
//                            transactionStorageManager.close();
//                        } catch (Exception e) {
//                            throw new RuntimeException(e);
//                        }
//                    });
//                }
//            });

        }

        }

        public void syncTransactions(Consumer<Integer> func) {
            Executor executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                SyncTransactionData syncTransactionData = new SyncTransactionData();
                syncTransactionData.phonenumber = UserSession.me.username;
                TransactionStorageManager transactionStorageManager = new TransactionStorageManager(activity);
                ArrayList<SyncTransactionData.GlobalTransaction> data = new ArrayList<>();
                for(DigitalCheque digitalCheque: transactionStorageManager.getTransactions()) {
                    data.add(Utils.convert(digitalCheque));
                }
                syncTransactionData.transactions = data;
                String requestBody = Util.gson.toJson(syncTransactionData);
                Log.d("Log", "syncTransactions: sending body "+requestBody);
                StringRequest stringRequest = new StringRequest(Request.Method.POST, Utils.url + "/syncTransactions", response -> {
                    Log.d("Response", "requestLoginCred: "+response);
                    Type globalTransactions = new TypeToken<ArrayList<SyncTransactionData.GlobalTransaction>>() {}.getType();
                    ArrayList<SyncTransactionData.GlobalTransaction> res = Util.gson.fromJson(response, globalTransactions);
                    ArrayList<DigitalCheque> digitalCheques = new ArrayList<>();
                    for(SyncTransactionData.GlobalTransaction globalTransaction: res) {
                        digitalCheques.add(Utils.convert(globalTransaction));
                    }
                    Executor executor1 = Executors.newSingleThreadExecutor();
                    executor1.execute(() -> {
                            int changesAmount = transactionStorageManager.syncDigitalCheque(digitalCheques);
                            if (changesAmount != 0) {
                                Log.d("Log", "syncTransactions: received transactions... "+ Arrays.toString(digitalCheques.toArray()));
                                func.accept(changesAmount);
                            }
                    });
                    Log.d("Log", "syncTransactions: "+res);
                }, error -> {
                    Log.d("Log", "requestLoginCred:"+error);
                    Log.d("Log", "requestLoginCred:"+error.getMessage());
                    Popups.snack(activity, error.getMessage());
                    Log.d("Response", "requestLoginCred: "+error.getMessage());
                }) {
                    @Override
                    public String getBodyContentType() {
                        return "application/json; charset=utf-8";
                    }

                    @Override
                    public byte[] getBody() {
                        return requestBody.getBytes();
                    }
                };
                requestQueue.add(stringRequest);
            });
//            ArrayList<SyncTransactionData.GlobalTransaction> data = new ArrayList<>();
//            Log.d("Response", "requestLoginCred: "+requestBody);
//            Log.d("Request", "requestLoginCred: URL"+ Utils.url + "/login");
        }
}
