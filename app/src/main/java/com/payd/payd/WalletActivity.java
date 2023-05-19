package com.payd.payd;

import static com.payd.payd.util.WiFiP2pWrapper.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.text.style.TtsSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.journeyapps.barcodescanner.ScanOptions;
import com.payd.payd.core.DigitalCheque;
import com.payd.payd.core.User;
import com.payd.payd.core.Util;
import com.payd.payd.util.BarCodeWrapper;
import com.payd.payd.util.CustomTransactionAdapter;
import com.payd.payd.util.DigitalChequeDatabase;
import com.payd.payd.util.Popups;
import com.payd.payd.util.Transaction;
import com.payd.payd.util.TransactionStorageManager;
import com.payd.payd.util.UserSession;
import com.payd.payd.util.Utils;
import com.payd.payd.util.WiFiP2pWrapper;

import org.objectweb.asm.Handle;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.net.ssl.SSLContext;

public class WalletActivity extends AppCompatActivity {
    ArrayList<Transaction> PastTransactions;
    ListView listView;
    private static CustomTransactionAdapter adapter;

    public final String TAG = "WIFI_P2P";
    public WifiP2pManager wifiP2pManager;
    public WifiP2pManager.Channel channel;
    NetworkChangeReceiver receiver;
    public Handler setBalanceHandler;
    public TransactionStorageManager transactionStorageManager;
    RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        receiver = new NetworkChangeReceiver(this);
        registerReceiver(receiver, filter);

        transactionStorageManager = new TransactionStorageManager(this);

        try {
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, null, new SecureRandom());
            requestQueue = Volley.newRequestQueue(getApplicationContext(), new HurlStack(null, sslContext.getSocketFactory()));
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException(e);
        }

        wifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = wifiP2pManager.initialize(this, getMainLooper(), null);

        initHeading(UserSession.me.username, "", 0);

        ActivityResultLauncher<ScanOptions> barCode = BarCodeWrapper.registerBarCodeScanner(this, result -> {
            if (result == null) {
                return;
            } else {
                Intent intent = new Intent(getApplicationContext(), SendRequestActivity.class);
                intent.putExtra("qrString", result);
                startActivity(intent);
            }
        });

        Button sendButton = findViewById(R.id.SendButton);
        sendButton.setOnClickListener(v -> {
            BarCodeWrapper.barCodeLaunch(barCode);
        });

        setBalanceHandler = new Handler(getMainLooper(), msg -> {
            ((TextView) findViewById(R.id.BalanceValue)).setText(msg.getData().getString("amount"));
            return true;
        });

        Button receiveButton = findViewById(R.id.RecieveButton);
        receiveButton.setOnClickListener(v -> {
            Intent intent = new Intent(WalletActivity.this, ReceiveWaitingActivity.class);
            startActivity(intent);
        });

        Button topupButton = findViewById(R.id.TopUpButton);
        topupButton.setOnClickListener(v -> {
            Intent intent = new Intent(WalletActivity.this, TopUpActivity.class);
            startActivity(intent);
        });

        Button allTransactionsButton = findViewById(R.id.ViewAllTranButton);
        allTransactionsButton.setOnClickListener(v -> {
            Intent intent = new Intent(WalletActivity.this,AllTransactionsActivity.class);
            startActivity(intent);
        });

        Button reloadButton = findViewById(R.id.reload);
        reloadButton.setOnClickListener(v -> {
            Executor executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                int amount = transactionStorageManager.getBalance();
                receiver.syncTransactions(changedAmount -> {
                    runOnUiThread(() -> {
                        int balance = amount+changedAmount;
                        Log.d(TAG, "onCreate:   "+balance);
                        this.setBalanceHandler.post(() -> {
                            Message msg = android.os.Message.obtain();
                            Bundle bundle = new Bundle();
                            bundle.putCharSequence("amount", String.valueOf(balance));
                            msg.setData(bundle);
                            this.setBalanceHandler.sendMessage(msg);
                            adapter.notifyDataSetChanged();
                        });
                    });
                });
            });
        });

        Button logoutButton = (Button) findViewById(R.id.logout);
        logoutButton.setOnClickListener(v -> {
            Log.d(TAG, "onCreate: logout button pressed");
            Map<String, String> map = new HashMap<>();
            map.put("phonenumber", UserSession.me.username);
            String requestBody = Util.gson.toJson(map);
            StringRequest stringRequest = new StringRequest(Request.Method.POST, Utils.url + "/logout", response -> {
                Log.d("Response", "requestLoginCred: "+response);
                Map<String, String> resMap = Util.gson.fromJson(response, Map.class);
                if (resMap.get("message").equals("Success")) {
                    try {
                        Util.putString(getApplicationContext(), "username", "");
                        Util.putString(getApplicationContext(), "privatekey", "");
                        Util.putString(getApplicationContext(), "cert", "");
                    } catch (GeneralSecurityException | IOException e) {
                        throw new RuntimeException(e);
                    }
                    Intent intent = new Intent(this, LandingActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                } else {
                    Popups.snack(this, "Unable to logout");
                }
            }, error -> {
                Log.d("Log", "requestLoginCred:"+error);
                Log.d("Log", "requestLoginCred:"+error.getMessage());
                Popups.snack(this, error.getMessage());
                findViewById(R.id.loginButton).setEnabled(true);
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

        try {
            initTranHistory();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void initTranHistory() throws Exception {
        this.listView=(ListView)findViewById(R.id.list);
        this.PastTransactions= new ArrayList<>();

        Executor executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            ArrayList<DigitalCheque> transactions = new ArrayList<DigitalCheque>(transactionStorageManager.getTransactions());

            Log.d(TAG, "initTranHistory: "+transactions);
            try {
                for (DigitalCheque digitalCheque: transactions) {
                    if (digitalCheque.senderId.equals(UserSession.me.username)){
                        // spent
                        this.PastTransactions.add(new Transaction(digitalCheque.receiverId, formatDate(digitalCheque.timestamp), (double) (-1*digitalCheque.amount), ""));
                    } else {
                        // received
                        this.PastTransactions.add(new Transaction(digitalCheque.senderId, formatDate(digitalCheque.timestamp), (double) (digitalCheque.amount), ""));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        });

        this.adapter=new CustomTransactionAdapter(PastTransactions,getApplicationContext());

        listView.setAdapter(adapter);
    }


    private void initHeading(String fname,String lname, double balance) {
        TextView salText=findViewById(R.id.SalutationText);
        TextView balText=findViewById(R.id.BalanceValue);
        salText.setText("Hi, "+fname+" "+lname+"!");
        balText.setText(Double.toString(balance));
        //Note this View can't be seen in the xml view, only during actual execution.
    }

    @Override
    protected void onStart() {
        super.onStart();
        TransactionStorageManager.getWalletBalance(this);
        WiFiP2pWrapper.requestGroupInfo(wifiP2pManager, channel, group -> {
            if (group != null) {
                WiFiP2pWrapper.removeGroup(wifiP2pManager, channel, () -> {
                    Popups.snack(this, "Removed From Group");
                }, () -> {
                    Popups.snackDanger(this, "Unable To Remove");
                });
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        transactionStorageManager.close();
    }

    public String formatDate(long timeInMillis) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMillis);
        return formatter.format(calendar.getTime());
    }
}