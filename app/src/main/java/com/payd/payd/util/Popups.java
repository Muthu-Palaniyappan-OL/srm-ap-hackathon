package com.payd.payd.util;

import android.app.Activity;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.payd.payd.R;

public class Popups {
    public static void snack(Activity activity, String msg) {
        Log.d("Log", "snack: "+msg);
        Snackbar.make(activity.getWindow().getDecorView().getRootView(), msg, Snackbar.LENGTH_SHORT).show();
    }
    public static void snackDanger(Activity activity, String msg) {
        Snackbar.make(activity.getWindow().getDecorView().getRootView(), msg, Snackbar.LENGTH_SHORT).setBackgroundTint(ContextCompat.getColor(activity.getApplicationContext(), R.color.payd_brown)).setTextColor(ContextCompat.getColor(activity.getApplicationContext(), R.color.white)).show();
    }
}
