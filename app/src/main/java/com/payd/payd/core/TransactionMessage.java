package com.payd.payd.core;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class TransactionMessage {
    String senderId;
    ArrayList<DigitalCheque> pastTransactions;
    DigitalCheque current;

    public TransactionMessage(PrivateKey key, Wallet w, DigitalCheque current) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        pastTransactions = w.digitalCheques;
        this.current = current;
        current.sign(key);
    }

    public int amountAvailable() {
        int amount = 0;
        for(DigitalCheque digitalCheque: pastTransactions) {
            if (digitalCheque.senderId.equals(this.senderId)) {
                amount -= digitalCheque.amount;
            } else {
                amount += digitalCheque.amount;
            }
            if (amount < 0) throw new Error("Transaction Invalid");
        }
        return amount;
    }

    public boolean verify() throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException {
        for(DigitalCheque digitalCheque: pastTransactions) {
            if (!digitalCheque.verify()) return false;
        }
        return true;
    }
}
