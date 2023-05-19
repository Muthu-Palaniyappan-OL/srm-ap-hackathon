package com.payd.payd.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.payd.payd.ReceiveVerifyActivity;
import com.payd.payd.ReceiveWaitingActivity;
import com.payd.payd.core.DigitalCheque;
import com.payd.payd.core.User;
import com.payd.payd.core.Util;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import fi.iki.elonen.NanoHTTPD;

public class ReceiverHttpServer extends NanoHTTPD {
    Context context;
    ReceiveWaitingActivity activity;

    public ReceiverHttpServer(Context context, int port, ReceiveWaitingActivity activity) {
        super(port);
        this.context = context;
        this.activity = activity;
    }

    @Override
    public Response serve(IHTTPSession session) {
        Log.d("URI", "serve: " + session.getUri() + session.getParms().get("name"));
        if (session.getUri().startsWith("/hello")) {
            //  /hello?name=muthu
            return HttpRequestWrapper.helloHandler(session);
        } else if (session.getUri().startsWith("/sendamount")) {
            Log.d("Amount Request", "serve: " + session.getUri());
            int currentAmount = Integer.parseInt(session.getParms().get("currentamount"));
            int amount = Integer.parseInt(session.getParms().get("amount"));
            Map<String, String> map = new HashMap<>();
            try {
                session.parseBody(map);
            } catch (IOException | ResponseException e) {
                throw new RuntimeException(e);
            }
            Log.d(activity.TAG, "serve: " + map);
            Log.d(activity.TAG, "serve: "+amount);
            Log.d(activity.TAG, "serve: "+currentAmount);
            Log.d(activity.TAG, "serve: postdata" + map.get("postData"));
            DigitalCheque digitalCheque = Util.gson.fromJson(map.get("postData"), DigitalCheque.class);
            if (currentAmount >= amount) {
                activity.transactionStorageManager.addDigitalCheque(digitalCheque);
                Message message = Message.obtain();
                Bundle bundle = new Bundle();
                bundle.putBoolean("result", currentAmount >= amount);
                bundle.putBoolean("send", false);
                bundle.putString("amount", session.getParms().get("amount"));
                message.setData(bundle);
                activity.receiveAmountHandler.post(() -> {
                    activity.receiveAmountHandler.sendMessage(message);
                });
                return newFixedLengthResponse("ok");
            } else  {
                Message message = Message.obtain();
                Bundle bundle = new Bundle();
                bundle.putBoolean("result", false);
                bundle.putBoolean("send", false);
                bundle.putString("amount", session.getParms().get("amount"));
                message.setData(bundle);
                activity.receiveAmountHandler.post(() -> {
                    activity.receiveAmountHandler.sendMessage(message);
                });
                return newFixedLengthResponse("Bad Request");
            }
        }
        return newFixedLengthResponse("Bad Request");
    }
}
