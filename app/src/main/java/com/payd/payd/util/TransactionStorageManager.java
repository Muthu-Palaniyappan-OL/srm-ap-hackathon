package com.payd.payd.util;

import android.app.Activity;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import androidx.room.Room;

import com.payd.payd.WalletActivity;
import com.payd.payd.core.DigitalCheque;
import com.payd.payd.core.User;
import com.payd.payd.core.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TransactionStorageManager {

    private DigitalChequeDatabase digitalChequeDatabase;
    private List<DigitalCheque> transactions = new ArrayList<>();

    public int amount = 0;

    public TransactionStorageManager(Activity activity) {
        digitalChequeDatabase = Room.databaseBuilder(activity.getApplicationContext(), DigitalChequeDatabase.class, "transaction.db").build();
    }

    public void addDigitalCheque(DigitalCheque digitalCheque) {
        digitalChequeDatabase.digitalChequeDao().insertAll(digitalCheque);
        transactions.add(digitalCheque);
    }

    public void addDigitalChequeAsync(DigitalCheque digitalCheque) {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            validTransactions();
            addDigitalCheque(digitalCheque);
        });
    }

    public int getBalance() {
        validTransactions();
        amount = 0;
        for (DigitalCheque digitalCheque : transactions) {
            if (UserSession.me.username.equals(digitalCheque.senderId)) {
                amount -= digitalCheque.amount;
            } else {
                amount += digitalCheque.amount;
            }
        }
        return amount;
    }

    public List<DigitalCheque> getTransactions() {
        validTransactions();
        return transactions;
    }

    public void validTransactions() {
        if (transactions.size() == 0) {
            transactions = digitalChequeDatabase.digitalChequeDao().getAll();
        }
    }

    public void close() {
        digitalChequeDatabase.close();
    }

    public static void getWalletBalance(WalletActivity activity) {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            int balance = activity.transactionStorageManager.getBalance();
            activity.setBalanceHandler.post(() -> {
                Message msg = android.os.Message.obtain();
                Bundle bundle = new Bundle();
                bundle.putCharSequence("amount", String.valueOf(balance));
                msg.setData(bundle);
                activity.setBalanceHandler.sendMessage(msg);
            });
        });
    }

    public int syncDigitalCheque(ArrayList<DigitalCheque> digitalCheques) {
        int amountChanged = 0;
        ArrayList<DigitalCheque> needToBeAdded = new ArrayList<>();
        for(DigitalCheque global : digitalCheques) {
            boolean flag = false;
            for(DigitalCheque local : this.getTransactions()) {
                if (global.uuid.equals(local.uuid)) {
                    flag = true;
                }
            }
            if (!flag) {
                needToBeAdded.add(global);
            }
        }
        for(DigitalCheque digitalCheque:needToBeAdded) {
            addDigitalCheque(digitalCheque);
            Log.d("Log", "syncDigitalCheque: "+digitalCheque);
            if (digitalCheque.senderId.equals(UserSession.me.username)) {
                amountChanged -= digitalCheque.amount;
            } else {
                amountChanged += digitalCheque.amount;
            }
        }
        return amountChanged;
    }

}
