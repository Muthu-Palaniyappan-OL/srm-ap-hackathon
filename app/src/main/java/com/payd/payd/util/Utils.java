package com.payd.payd.util;

import android.content.Context;
import android.content.res.Resources;
import android.net.wifi.hotspot2.pps.Credential;

import androidx.room.Room;

import com.payd.payd.R;
import com.payd.payd.core.DigitalCheque;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import java.util.HashMap;
import java.util.Map;

public class Utils {
  public static String url = "https://payd-official.onrender.com";
    //public static String url = "http://192.168.137.1:5000";

  static DigitalChequeDatabase transactionDatabase = null;


  public static DigitalChequeDatabase getDatabase(Context context) {
    if (transactionDatabase != null) return transactionDatabase;
    transactionDatabase = Room.databaseBuilder(context, DigitalChequeDatabase.class, "transaction.db").build();
    return transactionDatabase;
  }

  public static SyncTransactionData.GlobalTransaction convert(DigitalCheque digitalCheque) {
    SyncTransactionData.GlobalTransaction res = new SyncTransactionData.GlobalTransaction();
    res.uuid = digitalCheque.uuid;
    res.amount = digitalCheque.amount;
    res.senderPhoneNumber = digitalCheque.senderId;
    res.receiverPhoneNumber = digitalCheque.receiverId;
    res.receiverName = "";
    res.senderName = "";
    res.senderCert = digitalCheque.senderCert;
    res.senderSign = digitalCheque.signature;
    return res;
  }

  public static DigitalCheque convert(SyncTransactionData.GlobalTransaction globalTransaction) {
    DigitalCheque res = new DigitalCheque();
    res.uuid = globalTransaction.uuid;
    res.amount = globalTransaction.amount;
    res.senderId = globalTransaction.senderPhoneNumber;
    res.receiverId = globalTransaction.receiverPhoneNumber;
    res.receiverName = "";
    res.senderName = "";
    res.senderCert = globalTransaction.senderCert;
    res.signature = globalTransaction.senderSign;
    return res;
  }
}
