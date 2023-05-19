package com.payd.payd.core;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.widget.TextView;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.google.gson.Gson;
import com.payd.payd.R;
import com.payd.payd.WalletActivity;
import com.payd.payd.util.UserSession;
import com.payd.payd.util.Utils;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class Util {
        private static final String SHARED_PREFS_FILE_NAME = "payd_secure";
    public static String sign(Key key, byte[] data) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        byte[] hash = MessageDigest.getInstance("SHA-256").digest(data);

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] sign = cipher.doFinal(hash);
        return Base64.getEncoder().encodeToString(sign);
    }

    public static boolean verify(Key key, byte[] data, String sign) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        byte[] hash = MessageDigest.getInstance("SHA-256").digest(data);

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decrypted_hash = cipher.doFinal(Base64.getDecoder().decode(sign));

        return Arrays.equals(Arrays.copyOfRange(decrypted_hash, decrypted_hash.length - hash.length, decrypted_hash.length), hash);
    }

    public static PublicKeyCertificate CASignCertificate(byte[] publicKey, PrivateKey CAPrivateKey) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidKeySpecException {
        return new PublicKeyCertificate(Base64.getEncoder().encodeToString(publicKey)+"."+sign(CAPrivateKey, publicKey));
    }

    public static DigitalCheque topUp(String username, String receiverCert, int amount) {
        return new DigitalCheque(UUID.randomUUID(), amount, System.currentTimeMillis(), "CA", username, CACertificate.toString(), receiverCert);
    }

    public static PublicKey CAPublicKey;
    public static PublicKeyCertificate CACertificate;

    public static Gson gson = new Gson();
    public static KeyPairGenerator keyPairGenerator;

    static {
        try {
            keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static KeyPair generateKeyPair() {
        keyPairGenerator.initialize(2048);
        return keyPairGenerator.generateKeyPair();
    }

    public static void setCAPublicKey(PublicKey CAPublicKey) {
        Util.CAPublicKey = CAPublicKey;
    }

    public static SharedPreferences getEncryptedSharedPreferences(Context context) throws GeneralSecurityException, IOException {
        String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
        return EncryptedSharedPreferences.create(
                SHARED_PREFS_FILE_NAME,
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );
    }

    public static void putString(Context context, String key, String value) throws GeneralSecurityException, IOException {
        SharedPreferences.Editor editor = getEncryptedSharedPreferences(context).edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getString(Context context, String key) throws GeneralSecurityException, IOException {
        return getEncryptedSharedPreferences(context).getString(key, null);
    }
}
