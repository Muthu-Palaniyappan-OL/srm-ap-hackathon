package com.payd.payd.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import static androidx.core.content.ContextCompat.getSystemService;
import static androidx.core.content.ContextCompat.startActivity;

public class PermissionDemander {
    private final ArrayList<String> permissionRequestList = new ArrayList<>();
    private final AppCompatActivity activity;
    public PermissionDemander(AppCompatActivity activity) {
        this.permissionRequestList.clear();
        this.activity = activity;
    }

    public PermissionDemander add(String permission) {
        permissionRequestList.add(permission);
        return this;
    }

    public void demand() {
        if (permissionRequestList.size() > 1) {
            activity.registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), it -> {
                for(Map.Entry<String, Boolean> entry : it.entrySet()) {
                    if (!entry.getValue()) {
                        // Demand User
                        Toast.makeText(activity.getApplicationContext(), "Grant Permision For "+entry.getKey(), Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", activity.getPackageName(), null));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        activity.startActivity(intent);
                    }
                }
            }).launch(Arrays.copyOf(permissionRequestList.toArray(), permissionRequestList.size(), String[].class));
        }
    }

    public void demandLocation() {
        LocationManager locationManager = (LocationManager) activity.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isLocationEnabled()) {
            Toast.makeText(activity.getApplicationContext(), "Enable Location", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            activity.startActivity(intent);
        }
    }

    public void demandWiFi() {
        WifiManager wifiManager = (WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(activity.getApplicationContext(), "Enable WiFi", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
            activity.startActivity(intent);
        }
        if (!wifiManager.isP2pSupported()) {
            Toast.makeText(activity.getApplicationContext(), "WiFi Doesnt Support P2P", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
            activity.startActivity(intent);
        }
    }

    public static void demandBasicPermissions(AppCompatActivity activity) {
        PermissionDemander permissionDemander = new PermissionDemander(activity);
        permissionDemander.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        permissionDemander.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissionDemander.add(Manifest.permission.USE_BIOMETRIC);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionDemander.add(Manifest.permission.NEARBY_WIFI_DEVICES);
        }
        permissionDemander.demand();
        permissionDemander.demandLocation();
        permissionDemander.demandWiFi();
    }

}
