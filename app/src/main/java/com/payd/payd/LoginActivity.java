package com.payd.payd;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.payd.payd.core.User;
import com.payd.payd.core.Util;
import com.payd.payd.util.Popups;
import com.payd.payd.util.UserSession;
import com.payd.payd.util.Utils;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLContext;

public class LoginActivity extends AppCompatActivity {

    RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        try {
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, null, new SecureRandom());
            requestQueue = Volley.newRequestQueue(getApplicationContext(), new HurlStack(null, sslContext.getSocketFactory()));
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException(e);
        }

        Button loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(v -> {
            UserSession.me = new User(((TextInputEditText) findViewById(R.id.phonenumber)).getText().toString());
            loginButton.setEnabled(false);
            requestLoginCred();
        });
    }

    void requestLoginCred() {
        UserSession.me = new User(((TextInputEditText) findViewById(R.id.phonenumber)).getText().toString());
        Map<String, String> map = new HashMap<>();
        map.put("phonenumber", ((TextInputEditText) findViewById(R.id.phonenumber)).getText().toString());
        map.put("password", ((TextInputEditText) findViewById(R.id.signup_password)).getText().toString());
        map.put("publickey", Base64.getEncoder().encodeToString(UserSession.me.getPublicKey().getEncoded()));
        String requestBody = Util.gson.toJson(map);
        Log.d("Response", "requestLoginCred: "+requestBody);
        Log.d("Request", "requestLoginCred: URL"+Utils.url + "/login");
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Utils.url + "/login", response -> {
            Log.d("Response", "requestLoginCred: "+response);
            Map<String, String> resMap = Util.gson.fromJson(response, Map.class);
            if (resMap.get("message").equals("Success")) {
                Log.d("Response", "requestLoginCred: "+resMap);
                UserSession.me = new User(((TextInputEditText) findViewById(R.id.phonenumber)).getText().toString(), Base64.getEncoder().encodeToString(UserSession.me.getPrivateKey().getEncoded()), resMap.get("signature"));
                try {
                    Util.putString(getApplicationContext(), "username", UserSession.me.username);
                    Util.putString(getApplicationContext(), "privatekey", Base64.getEncoder().encodeToString(UserSession.me.getPrivateKey().getEncoded()));
                    Util.putString(getApplicationContext(), "cert", resMap.get("signature"));
                } catch (GeneralSecurityException | IOException e) {
                    throw new RuntimeException(e);
                }
                findViewById(R.id.loginButton).setEnabled(true);
                Intent intent = new Intent(LoginActivity.this, WalletActivity.class);
                startActivity(intent);
            } else {
                Popups.snack(this, resMap.get("message"));
                findViewById(R.id.loginButton).setEnabled(true);
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
    }
}