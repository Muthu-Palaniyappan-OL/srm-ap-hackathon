package com.payd.payd.core;

import java.security.*;
import java.security.spec.*;
import java.util.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class PublicKeyCertificate {
    PublicKey publicKey;
    String signature;

    PublicKeyCertificate(String certificateString) throws NoSuchAlgorithmException, InvalidKeySpecException {
        String[] strings = certificateString.split("\\.");
        if (strings.length != 2) throw new Error("Certificate Not Valid");
        this.publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(strings[0])));
        this.signature = strings[1];
    }

    public boolean caVerify() throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        return Util.verify(Util.CAPublicKey, publicKey.getEncoded(), this.signature);
    }

    @Override
    public String toString() {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded()) + "." + signature;
    }
}
