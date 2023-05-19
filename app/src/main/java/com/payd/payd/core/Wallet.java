package com.payd.payd.core;

import com.payd.payd.util.Utils;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class Wallet {
    public String username;
    public String walletId;
    public PublicKey publicKey;
    public PrivateKey privateKey;
    public PublicKeyCertificate cert;
    public ArrayList<DigitalCheque> digitalCheques = new ArrayList<>();

    Wallet(String username, String walletId) {
        this.username = username;
        this.walletId = walletId;
        KeyPair keyPair = Util.generateKeyPair();
        publicKey = keyPair.getPublic();
        privateKey = keyPair.getPrivate();
    }

    public void setCertificate(String certificateString) throws NoSuchAlgorithmException, InvalidKeySpecException {
        this.cert = new PublicKeyCertificate(certificateString);
    }

    public void addDigitalCheque(DigitalCheque digitalCheque) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException {
//        digitalCheque.verify();
        digitalCheques.add(digitalCheque);
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public PublicKeyCertificate getCert() {
        return cert;
    }
}
