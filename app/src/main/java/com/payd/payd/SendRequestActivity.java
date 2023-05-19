package com.payd.payd;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.payd.payd.core.DigitalCheque;
import com.payd.payd.core.TransactionMessage;
import com.payd.payd.core.User;
import com.payd.payd.core.Util;
import com.payd.payd.util.Popups;
import com.payd.payd.util.SenderHttpServer;
import com.payd.payd.util.TransactionStorageManager;
import com.payd.payd.util.UserSession;
import com.payd.payd.util.Utils;
import com.payd.payd.util.WiFiP2pWrapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class SendRequestActivity extends AppCompatActivity {
    public final String TAG = "WIFI_P2P";
    public WifiP2pManager wifiP2pManager;
    public WifiP2pManager.Channel channel;
    Map<String, String> map;
    public RequestQueue
            requestQueue;
    public SenderHttpServer senderHttpServer;
    public WiFiDirectBroadcastReceiver wiFiDirectBroadcastReceiver;
    public Handler setReceiverNameHandler;
    TransactionStorageManager transactionStorageManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_request);
        transactionStorageManager = new TransactionStorageManager(this);
        wifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = wifiP2pManager.initialize(this, getMainLooper(), null);
        String qrCode = getIntent().getStringExtra("qrString");
        map = Util.gson.fromJson(qrCode, Map.class);
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        ((TextView) findViewById(R.id.walletInfo)).setText("Your Wallet Id: "+UserSession.me.username);
        setReceiverNameHandler = new Handler(getMainLooper(), message -> {
            ((TextView) findViewById(R.id.sendToText)).setText("Sending to: "+message.getData().getString("name"));
            return true;
        });

        Button requestButton = findViewById(R.id.RequestPaymentButton);
        requestButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), ResultActivity.class);
            DigitalCheque digitalCheque = new DigitalCheque(UUID.randomUUID(), Integer.parseInt(((TextView) findViewById(R.id.request_amount)).getText().toString()), System.currentTimeMillis(), UserSession.me.username, UserSession.clientName, UserSession.me.getCert().toString(), UserSession.me.getCert().toString());
            try {
                digitalCheque.sign(UserSession.me.getPrivateKey());
            } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException |
                     BadPaddingException | InvalidKeyException e) {
                throw new RuntimeException(e);
            }
            Executor executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                    int balance = transactionStorageManager.getBalance();
                    Log.d(TAG, "onCreate: balance"+balance);
                    int req_amt = Integer.parseInt(((TextView) findViewById(R.id.request_amount)).getText().toString());
                    String url = "http://" + UserSession.clientIp + ":8080/sendamount?currentamount="+balance+"&amount=" + req_amt;
                    Log.d(TAG, "onClick: " + url);
                    StringRequest stringRequest = new StringRequest(Request.Method.POST, url, response -> {
                        Log.d(TAG, "onClick: response " + response);
                        if (response.equals("ok") && req_amt <= balance) {
                            transactionStorageManager.addDigitalChequeAsync(digitalCheque);
                            intent.putExtra("result", true);
                            intent.putExtra("send", true);
                            intent.putExtra("amount", ((TextView) findViewById(R.id.request_amount)).getText().toString());
                            startActivity(intent);
                        } else {
                            intent.putExtra("result", false);
                            intent.putExtra("send", true);
                            intent.putExtra("amount", ((TextView) findViewById(R.id.request_amount)).getText().toString());
                            startActivity(intent);
                        }
                    }, error -> {
                        Log.d(TAG, "onCreate: "+error);
                        intent.putExtra("result", false);
                        intent.putExtra("send", true);
                        intent.putExtra("amount", ((TextView) findViewById(R.id.request_amount)).getText().toString());
                        startActivity(intent);
                    }){
                        @Override
                        public String getBodyContentType() {
                            return "application/json";
                        }
                        @Override
                        public byte[] getBody() {
                            Log.d(TAG, "getBody: "+digitalCheque.uuid);
                            try {
                                return Util.gson.toJson(digitalCheque).getBytes("utf-8");
                            } catch (UnsupportedEncodingException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    };
                    requestQueue.add(stringRequest);
                });
            });

    }

    @Override
    protected void onStart() {
        super.onStart();
        ((Button) findViewById(R.id.RequestPaymentButton)).setEnabled(false);
        senderHttpServer = new SenderHttpServer(getApplicationContext(), 8080, this);
        try {
            senderHttpServer.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        wiFiDirectBroadcastReceiver = new WiFiDirectBroadcastReceiver(wifiP2pManager, channel, this);
        registerReceiver(wiFiDirectBroadcastReceiver, intentFilter);

        WiFiP2pWrapper.discoverPeers(wifiP2pManager, channel, () -> {
            Popups.snack(this, "Discovering Peers");
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(wiFiDirectBroadcastReceiver);
        UserSession.clear();
        senderHttpServer.stop();
    }

    public static class WiFiDirectBroadcastReceiver extends BroadcastReceiver {
        WifiP2pManager wifiP2pManager;
        WifiP2pManager.Channel channel;
        SendRequestActivity activity;
        RequestQueue requestQueue;
        WiFiDirectBroadcastReceiver(WifiP2pManager wifiP2pManager, WifiP2pManager.Channel channel, SendRequestActivity activity) {
            this.wifiP2pManager = wifiP2pManager;
            this.channel = channel;
            this.activity = activity;
            requestQueue = Volley.newRequestQueue(activity.getApplicationContext());
        }
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            Log.d(activity.TAG, "onReceive: "+action);

            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                // Wi-Fi Direct is enabled or disabled
            } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                // List of available Wi-Fi Direct devices has changed
                WiFiP2pWrapper.requestPeers(wifiP2pManager, channel, peerDevices -> {
                    for(WifiP2pDevice device: peerDevices.getDeviceList()) {
                        Popups.snack(activity,"Found "+device.deviceName);
                        if (device.deviceName.equals(activity.map.get("devicename"))) {
                            Popups.snack(activity,"Gonna Connect "+device.deviceName);
                            WiFiP2pWrapper.connectionInfo(wifiP2pManager, channel, wifiP2pInfo -> {
                                if (wifiP2pInfo == null || wifiP2pInfo.groupFormed) return;

                                WiFiP2pWrapper.connect(wifiP2pManager, channel, device.deviceAddress, () -> {
                                    Popups.snack(activity,"Connection Established "+device.deviceName);
                                    ((Button) activity.findViewById(R.id.RequestPaymentButton)).setEnabled(true);
                                });
                            });
                        }
                    }
                });

            } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                // Wi-Fi Direct connection status has changed
                WiFiP2pWrapper.connectionInfo(wifiP2pManager, channel, wifiP2pInfo -> {
                    if (wifiP2pInfo == null || !wifiP2pInfo.groupFormed) {
                        return;
                    }
                    if (!wifiP2pInfo.isGroupOwner) {
                        // group client
                        String url = "http:/" + wifiP2pInfo.groupOwnerAddress.getHostAddress()+":8080/hello?name="+UserSession.me.username;
                        Popups.snack(activity, url);
                        StringRequest stringRequest = new StringRequest(url, response -> {
                            UserSession.clientName = response;
                            UserSession.clientIp = wifiP2pInfo.groupOwnerAddress.getHostAddress();
                            activity.setReceiverNameHandler.post(() -> {
                                Message message = Message.obtain();
                                Bundle bundle = new Bundle();
                                bundle.putString("name", response);
                                message.setData(bundle);
                                activity.setReceiverNameHandler.sendMessage(message);
                            });
                        }, error -> {
                            Log.d(activity.TAG, "onReceive: "+error);
                        });
                        requestQueue.add(stringRequest);
                    }
                });
            } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
                // This device's Wi-Fi Direct details have changed
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        transactionStorageManager.close();
    }
}