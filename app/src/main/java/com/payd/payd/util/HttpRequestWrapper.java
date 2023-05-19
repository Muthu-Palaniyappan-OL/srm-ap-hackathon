package com.payd.payd.util;

import android.app.Activity;
import android.util.Log;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.*;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

import com.android.volley.toolbox.Volley;
import com.payd.payd.core.Util;

import java.util.HashMap;
import java.util.Map;

public class HttpRequestWrapper {
    public static Response helloHandler(NanoHTTPD.IHTTPSession session) {
        UserSession.newUser(session.getRemoteIpAddress(), session.getParms().get("name"));
        Log.d("Response", "helloHandler: "+UserSession.me.username);
        return newFixedLengthResponse(UserSession.me.username);
    }
}
