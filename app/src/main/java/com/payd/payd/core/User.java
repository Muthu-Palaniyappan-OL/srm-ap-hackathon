package com.payd.payd.core;

import android.util.Log;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;

public class User {
    public String username;
    PublicKeyCertificate cert;
    public String signature;
    int amount;
    String walletId;
    PublicKey publicKey;
    PrivateKey privateKey;

    ArrayList<Wallet> wallets = new ArrayList<>();

    public User(String username) {
        this.username = username;
        KeyPair keyPair = Util.generateKeyPair();
        this.publicKey = keyPair.getPublic();
        this.privateKey = keyPair.getPrivate();
        addWallet("Wallet1");
    }

    public User(String username, String privatekey, String cert) {
        this.username = username;
        try {
            this.cert = new PublicKeyCertificate(cert);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
        this.publicKey = this.cert.publicKey;
        try {
            this.privateKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec((Base64.getDecoder().decode(privatekey))));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
        addWallet("Wallet1");
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }
    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public void setCertificate(String certificateString) throws NoSuchAlgorithmException, InvalidKeySpecException {
        this.cert = new PublicKeyCertificate(certificateString);
    }

    public PublicKeyCertificate getCert() {
        return cert;
    }

    public Wallet getWallet(String walletId) {
        for (Wallet w: wallets) {
            if (w.walletId.equals(walletId)) {
                return w;
            }
        }
        throw new Error("No Such Wallet");
    }

    public void addWallet(String walletId) {
        wallets.add(new Wallet(this.username, walletId));
    }

    @Override
    public String toString() {
        return String.format("User(id=%s,amount=%d)", this.username, this.amount);
    }
}
