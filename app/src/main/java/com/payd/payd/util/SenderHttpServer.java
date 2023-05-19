package com.payd.payd.util;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import com.payd.payd.R;
import com.payd.payd.SendRequestActivity;
import com.payd.payd.SendTransactionLogActivity;
import fi.iki.elonen.NanoHTTPD;

public class SenderHttpServer extends NanoHTTPD {
    Context context;
    SendRequestActivity activity;
    public SenderHttpServer(Context context, int port, SendRequestActivity activity) {
        super(port);
        this.context = context;
        this.activity = activity;
    }

    @Override
    public Response serve(IHTTPSession session) {
        Log.d("URI", "serve: "+session.getUri() +" "+session.getParms().get("name"));
        if (session.getUri().startsWith("/hello")) {
            // /hello?name=muthu
            Log.d("Empty", "serve: jumping");
            Response response = HttpRequestWrapper.helloHandler(session);
            activity.setReceiverNameHandler.post(() -> {
                Message message = Message.obtain();
                Bundle bundle = new Bundle();
                bundle.putString("name", session.getParms().get("name"));
                message.setData(bundle);
                activity.setReceiverNameHandler.sendMessage(message);
            });
            return response;
        }
        return newFixedLengthResponse("Bad Request");
    }
}
