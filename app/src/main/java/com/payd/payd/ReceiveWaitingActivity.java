package com.payd.payd;

import static com.payd.payd.util.WiFiP2pWrapper.discoverPeers;
import static com.payd.payd.util.WiFiP2pWrapper.requestDeviceInfo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.WriterException;
import com.payd.payd.core.Util;
import com.payd.payd.util.BarCodeWrapper;
import com.payd.payd.util.Popups;
import com.payd.payd.util.ReceiverHttpServer;
import com.payd.payd.util.TransactionStorageManager;
import com.payd.payd.util.UserSession;
import com.payd.payd.util.WiFiP2pWrapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ReceiveWaitingActivity extends AppCompatActivity {
    public WifiP2pManager wifiP2pManager;
    public static String TAG = "WIFI_P2P_SERVICE";
    public WifiP2pManager.Channel channel;
    public ReceiverHttpServer receiverServer;
    public WiFiDirectBroadcastReceiver wiFiDirectBroadcastReceiver;
    public Handler receiveAmountHandler;
    public TransactionStorageManager transactionStorageManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_waiting);
        wifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = wifiP2pManager.initialize(this, getMainLooper(), null);
        transactionStorageManager = new TransactionStorageManager(this);

        receiveAmountHandler = new Handler(getMainLooper(), (msg) -> {
            Intent intent = new Intent(getApplicationContext(), ResultActivity.class);
            intent.putExtra("result", msg.getData().get("result").toString().equals("true"));
            intent.putExtra("amount", msg.getData().get("amount").toString());
            intent.putExtra("send", msg.getData().get("send").toString().equals("true"));
            startActivity(intent);
            return true;
        });

        ImageView img = findViewById(R.id.WaitingForPeersImage);
        img.setOnClickListener(v -> {
            Intent intent = new Intent(ReceiveWaitingActivity.this, ReceiveVerifyActivity.class);
            startActivity(intent);
        });

        ((Button) findViewById(R.id.receive_retry)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                discovering();
            }
        });

        discovering();
    }

    public void discovering() {
        discoverPeers(wifiP2pManager, channel, () -> {
            requestDeviceInfo(wifiP2pManager, channel, (currentDevice) -> {
                Popups.snack(this, "Discovering Peers");
                try {
                    Map<String, String> map = new HashMap<>();
                    map.put("devicename", currentDevice.deviceName);
                    BarCodeWrapper.setBarCode(Util.gson.toJson(map), findViewById(R.id.WaitingForPeersImage));
                } catch (WriterException e) {
                    throw new RuntimeException(e);
                }
            });
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        receiverServer = new ReceiverHttpServer(getApplicationContext(), 8080, this);
        try {
            receiverServer.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        wiFiDirectBroadcastReceiver = new WiFiDirectBroadcastReceiver(wifiP2pManager, channel, this);
        registerReceiver(wiFiDirectBroadcastReceiver, intentFilter);
    }

    public static class WiFiDirectBroadcastReceiver extends BroadcastReceiver {
        WifiP2pManager wifiP2pManager;
        WifiP2pManager.Channel channel;
        ReceiveWaitingActivity activity;
        RequestQueue requestQueue;

        WiFiDirectBroadcastReceiver(WifiP2pManager wifiP2pManager, WifiP2pManager.Channel channel, ReceiveWaitingActivity activity) {
            this.wifiP2pManager = wifiP2pManager;
            this.channel = channel;
            this.activity = activity;
            requestQueue = Volley.newRequestQueue(activity.getApplicationContext());
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                // Wi-Fi Direct is enabled or disabled
            } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                // List of available Wi-Fi Direct devices has changed
                if (wifiP2pManager != null) {
                    try {
                        wifiP2pManager.requestPeers(channel, wifiP2pDeviceList -> {
                            for (WifiP2pDevice device : wifiP2pDeviceList.getDeviceList()) {
                                Popups.snack(activity, "Discovered: " + device.deviceName);
                            }
                        });
                    } catch (SecurityException e) {
                        Popups.snack(activity, "Security Problem "+e.toString());
                    }
                }
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
    protected void onStop() {
        super.onStop();
        receiverServer.stop();
        UserSession.clear();
        unregisterReceiver(wiFiDirectBroadcastReceiver);
    }
}