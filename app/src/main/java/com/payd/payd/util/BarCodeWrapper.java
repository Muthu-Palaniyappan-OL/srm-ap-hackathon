package com.payd.payd.util;

import android.graphics.Bitmap;
import android.widget.ImageView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.function.Consumer;

public class BarCodeWrapper {
    public static void setBarCode(String input, ImageView view) throws WriterException {
        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
        Bitmap bitmap = barcodeEncoder.encodeBitmap(input, BarcodeFormat.QR_CODE, 400, 400);
        view.setImageBitmap(bitmap);
    }

    public static ActivityResultLauncher<ScanOptions> registerBarCodeScanner(AppCompatActivity activity, Consumer<String> func) {
        return activity.registerForActivityResult(new ScanContract(), intentResult -> {
            func.accept(intentResult.getContents());
        });
    }
    public static void barCodeLaunch(ActivityResultLauncher<ScanOptions> launcher) {
        ScanOptions scanOptions = new ScanOptions();
        scanOptions.setPrompt("Scan QR Code");
        scanOptions.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        scanOptions.setOrientationLocked(true);
        scanOptions.setBeepEnabled(false);
        launcher.launch(scanOptions);
    }
}
