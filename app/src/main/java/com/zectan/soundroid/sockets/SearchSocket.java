package com.zectan.soundroid.sockets;

import android.content.Context;
import android.util.Log;

import com.zectan.soundroid.classes.Socket;
import com.zectan.soundroid.objects.SearchResult;

import org.json.JSONException;
import org.json.JSONObject;

public class SearchSocket extends Socket {
    private static final String TAG = "(SounDroid) SearchSocket";
    private final Callback callback;
    private final Context context;

    public SearchSocket(String query, Context context, Callback callback) {
        super(TAG, callback, "search", query);
        this.callback = callback;
        this.context = context;

        Log.d(TAG, "Search: " + query);
        super.on("search_result", this::onResult);
        super.on("search_done", this::onDone);
    }

    private void onResult(Object... args) {
        if (callback.isInactive()) {
            closeSocket();
            Log.i(TAG, "(SOCKET) <close>");
            return;
        }

        try {
            JSONObject object = new JSONObject(args[0].toString());
            callback.onResult(new SearchResult(object, context));
        } catch (JSONException e) {
            callback.onError("Could not parse server response");
            e.printStackTrace();
        }
    }

    private void onDone(Object... objects) {
        closeSocket();
        callback.onDone();
    }

    public interface Callback extends Socket.Callback {
        void onResult(SearchResult results);

        void onDone();
    }
}
