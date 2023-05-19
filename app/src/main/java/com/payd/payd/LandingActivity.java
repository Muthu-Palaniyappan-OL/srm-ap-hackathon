package com.payd.payd;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.google.android.material.snackbar.Snackbar;
import com.payd.payd.core.DigitalCheque;
import com.payd.payd.core.User;
import com.payd.payd.core.Util;
import com.payd.payd.util.PermissionDemander;
import com.payd.payd.util.Popups;
import com.payd.payd.util.UserSession;
import com.payd.payd.util.WiFiP2pWrapper;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.UUID;

public class LandingActivity extends AppCompatActivity {
    public WifiP2pManager.Channel channel;
    public WifiP2pManager wifiP2pManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);
        wifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = wifiP2pManager.initialize(this, getMainLooper(), null);

        PermissionDemander.demandBasicPermissions(this);

        Button signupButton = findViewById(R.id.signUpButton);
        signupButton.setOnClickListener(v -> {

            Intent intent = new Intent(LandingActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

        Button loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(v -> {
            Intent intent = new Intent(LandingActivity.this, LoginActivity.class);
            startActivity(intent);
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
//            Util.putString(getApplicationContext(), "username", "");
            String username = Util.getString(getApplicationContext(), "username");
            if (username == null || username.equals(""))
                return;

            Log.d("Log", "onStart: "+Util.getString(getApplicationContext(), "cert"));
            UserSession.me = new User(Util.getString(getApplicationContext(), "username"), Util.getString(getApplicationContext(), "privatekey"), Util.getString(getApplicationContext(), "cert"));

            Intent intent = new Intent(getApplicationContext(), LandingLoggedInActivity.class);
            startActivity(intent);
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }

    }
}