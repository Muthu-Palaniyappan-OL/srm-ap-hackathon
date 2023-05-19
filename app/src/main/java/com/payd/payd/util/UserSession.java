package com.payd.payd.util;

import android.net.wifi.p2p.WifiP2pManager;
import android.system.ErrnoException;
import com.payd.payd.core.User;

import java.util.HashMap;
import java.util.Stack;

public class UserSession {
    public static String clientIp;
    public static String clientName;
    public static String clientId;
    public static long createdTimeStamp;
    public static STAGE clientStage;
    public static com.payd.payd.core.User me;

    public static enum STAGE {
        HELLO,
        TRANSACTION_SHARE,
        FINISH
    };

    public static void newUser(String ip, String clientName) {
        if (clientIp != null)
            throw new Error("Already User Exists");
        UserSession.clientIp = ip;
        UserSession.clientName = clientName;
        createdTimeStamp = System.currentTimeMillis();
        clientStage = STAGE.HELLO;
    }

    public static void clear() {
        UserSession.clientIp = null;
        UserSession.clientName = null;
        UserSession.createdTimeStamp = 0;
        UserSession.clientStage = null;
    }

    public static void clearMe() {
        UserSession.me = null;
    }
}
