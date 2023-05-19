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

public class SignUpActivity extends AppCompatActivity {
    RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        try {
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, null, new SecureRandom());
            requestQueue = Volley.newRequestQueue(getApplicationContext(), new HurlStack(null, sslContext.getSocketFactory()));
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException(e);
        }
        TextInputEditText phonenumber = (TextInputEditText) findViewById(R.id.mobileno);
        TextInputEditText password = (TextInputEditText) findViewById(R.id.signup_password);

        Button signUpButton = findViewById(R.id.SignUpButton);
        signUpButton.setOnClickListener(v -> {
            Log.d("SignUP", "onCreate: "+phonenumber);
            UserSession.me = new User(phonenumber.getText().toString());
            requestSignup();
        });
    }

    void requestSignup() {
        UserSession.me = new User(((TextView) findViewById(R.id.mobileno)).getText().toString());
        Map<String, String> map = new HashMap<>();
        map.put("name", ((TextInputEditText) findViewById(R.id.name)).getText().toString());
        map.put("aadhar", ((TextInputEditText) findViewById(R.id.aadhar)).getText().toString());
        map.put("gst", ((TextInputEditText) findViewById(R.id.gstNumber)).getText().toString());
        map.put("phonenumber", ((TextInputEditText) findViewById(R.id.mobileno)).getText().toString());
        map.put("password", ((TextInputEditText) findViewById(R.id.signup_password)).getText().toString());
        map.put("publickey", Base64.getEncoder().encodeToString(UserSession.me.getPublicKey().getEncoded()));
        String requestBody = Util.gson.toJson(map);
        Log.d("Request", "requestSignup:  "+requestBody);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Utils.url + "/signup", response -> {
            Popups.snack(this, response);
            Map<String, String> resMap = Util.gson.fromJson(response, Map.class);
            if (resMap.get("message").equals("Success")) {
                try {
                    UserSession.me.setCertificate(resMap.get("signature"));
                    Util.putString(getApplicationContext(), "username", UserSession.me.username);
                    Util.putString(getApplicationContext(), "cert", UserSession.me.getCert().toString());
                    Util.putString(getApplicationContext(), "privatekey", Base64.getEncoder().encodeToString(UserSession.me.getPrivateKey().getEncoded()));
                } catch (GeneralSecurityException | IOException e) {
                    throw new RuntimeException(e);
                }
                Intent intent = new Intent(getApplicationContext(), WalletActivity.class);
                startActivity(intent);
            } else {
                Log.d("Request", "requestSignup: "+requestBody);
                Popups.snack(this, resMap.get("message"));
                UserSession.clearMe();
            }
        }, error -> {
            Log.d("Request", "requestSignup: "+error);
            Log.d("Request", "requestSignup: error "+error.getMessage());
            Popups.snack(this, error.getMessage());
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