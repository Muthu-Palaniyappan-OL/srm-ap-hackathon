package com.payd.payd.util;

import com.payd.payd.core.DigitalCheque;

import java.util.List;
import java.util.UUID;

public class SyncTransactionData {
    public String phonenumber;
    public List<GlobalTransaction> transactions;

    public static class GlobalTransaction {
        public UUID uuid;
        public int amount;
        public String senderName;
        public String senderPhoneNumber;
        public String senderCert;
        public String senderSign;
        public String receiverPhoneNumber;
        public String receiverName;
    }
}
