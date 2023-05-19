package com.payd.payd.core;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class InitialMessage {
    UUID uuid;
    long timestamp;
    String senderId;
    String receiverId;
    String hash;
    String walletCert;

    public InitialMessage(UUID uuid, long timestamp, String senderId, String receiverId, String walletCert) throws NoSuchAlgorithmException {
        this.uuid = uuid;
        this.timestamp = timestamp;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.walletCert = walletCert;
        byte[] uuidBytes = uuid.toString().getBytes();
        byte[] timestampBytes = String.valueOf(timestamp).getBytes();
        byte[] senderIdBytes = senderId.getBytes();
        byte[] receiverIdBytes = receiverId.getBytes();
        ByteBuffer buffer = ByteBuffer.allocate(uuidBytes.length+timestampBytes.length+senderIdBytes.length+receiverIdBytes.length);
        buffer.put(uuidBytes);
        buffer.put(timestampBytes);
        buffer.put(senderIdBytes);
        buffer.put(receiverIdBytes);
        this.hash = Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-256").digest(buffer.array()));
    }

    public boolean verify() throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        byte[] uuidBytes = uuid.toString().getBytes();
        byte[] timestampBytes = String.valueOf(timestamp).getBytes();
        byte[] senderIdBytes = senderId.getBytes();
        byte[] receiverIdBytes = receiverId.getBytes();
        ByteBuffer buffer = ByteBuffer.allocate(uuidBytes.length+timestampBytes.length+senderIdBytes.length+receiverIdBytes.length);
        buffer.put(uuidBytes);
        buffer.put(timestampBytes);
        buffer.put(senderIdBytes);
        buffer.put(receiverIdBytes);
        String computedHash = Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-256").digest(buffer.array()));

        return (new PublicKeyCertificate(walletCert).caVerify()) && hash.equals(computedHash);
    }
}
