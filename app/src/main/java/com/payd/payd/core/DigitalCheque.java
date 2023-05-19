package com.payd.payd.core;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

@Entity
public class DigitalCheque {
    @PrimaryKey
    @NonNull
    public UUID uuid;
    @ColumnInfo(name = "amount")
    public int amount;
    @ColumnInfo(name = "timestamp")
    public long timestamp;
    @ColumnInfo(name = "senderId")
    public String senderId;
    public String senderName;
    @ColumnInfo(name = "receiverId")
    public String receiverId;
    public String receiverName;
    @ColumnInfo(name = "signature")
    public String signature;
    @ColumnInfo(name = "senderCert")
    public String senderCert;
    @ColumnInfo(name = "receiverCert")
    public String receiverCert;
    public String hash;
    @ColumnInfo(name = "CASignature")
    public String CASignature;

    public DigitalCheque() {}

    public DigitalCheque(UUID uuid, int amount, long timestamp, String senderId, String receiverId, String senderCert, String receiverCert) {
        this.uuid = uuid;
        this.amount = amount;
        this.timestamp = timestamp;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.senderCert = senderCert;
        this.receiverCert = receiverCert;
    }

    public void sign(PrivateKey key) throws NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        byte[] uuidBytes = uuid.toString().getBytes();
        byte[] amountBytes = String.valueOf(amount).getBytes();
        byte[] timestampBytes = String.valueOf(timestamp).getBytes();
        byte[] senderIdBytes = senderId.getBytes();
        byte[] receiverIdBytes = receiverId.getBytes();
        ByteBuffer buffer = ByteBuffer.allocate(uuidBytes.length+timestampBytes.length+amountBytes.length+senderIdBytes.length+receiverIdBytes.length);
        buffer.put(uuidBytes);
        buffer.put(amountBytes);
        buffer.put(timestampBytes);
        buffer.put(senderIdBytes);
        buffer.put(receiverIdBytes);
        this.hash = Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-256").digest(buffer.array()));
        this.signature = Util.sign(key, Base64.getDecoder().decode(hash));
    }

    public boolean verify() throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        byte[] uuidBytes = uuid.toString().getBytes();
        byte[] timestampBytes = String.valueOf(timestamp).getBytes();
        byte[] amountBytes = String.valueOf(amount).getBytes();
        byte[] senderIdBytes = senderId.getBytes();
        byte[] receiverIdBytes = receiverId.getBytes();
        ByteBuffer buffer = ByteBuffer.allocate(uuidBytes.length+timestampBytes.length+amountBytes.length+senderIdBytes.length+receiverIdBytes.length);
        buffer.put(uuidBytes);
        buffer.put(amountBytes);
        buffer.put(timestampBytes);
        buffer.put(senderIdBytes);
        buffer.put(receiverIdBytes);
        String computedHash = Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-256").digest(buffer.array()));
        return computedHash.equals(hash) && (new PublicKeyCertificate(this.senderCert)).caVerify() && (new PublicKeyCertificate(this.receiverCert)).caVerify() && Util.verify((new PublicKeyCertificate(this.senderCert)).publicKey, Base64.getDecoder().decode(hash), signature);
    }

    @NonNull
    @Override
    public String toString() {
        return String.format("(amount=%s,senderId=%s)", this.amount, this.senderId);
    }
}
