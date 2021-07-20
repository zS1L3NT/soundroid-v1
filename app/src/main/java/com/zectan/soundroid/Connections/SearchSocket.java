package com.zectan.soundroid.Connections;

import android.util.Log;

import com.zectan.soundroid.Models.SearchResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Socket;

public class SearchSocket {
    private static final String TAG = "(SounDroid) SearchSocket";
    private static final String SocketURL = "http://soundroid.zectan.com/";
    private final Callback callback;
    private final Socket socket;

    public SearchSocket(String query, Callback callback) {
        this.callback = callback;
        IO.Options options = IO.Options.builder().setTimeout(60_000).build();
        socket = IO.socket(URI.create(SocketURL), options).connect();

        Log.d(TAG, "Search: " + query);
        socket.emit("search", query);
        socket.on("search_result_" + query, this::onResult);
        socket.on("search_message_" + query, this::onMessage);
        socket.on("search_done_" + query, this::onDone);
        socket.on(Socket.EVENT_CONNECT, this::onConnect);
        socket.on("error_" + query, this::onError);
        socket.on(Socket.EVENT_DISCONNECT, this::onDisconnect);
        socket.on(Socket.EVENT_CONNECT_ERROR, this::onConnectError);
    }

    private void onResult(Object... args) {
        if (callback.isInactive()) {
            socket.close();
            Log.i(TAG, "(SOCKET) <close>");
            return;
        }

        try {
            JSONObject object = new JSONObject(args[0].toString());
            callback.onResult(new SearchResult(object));
        } catch (JSONException e) {
            callback.onError("Could not parse server response");
            e.printStackTrace();
        }
    }

    private void onMessage(Object... args) {
        if (callback.isInactive()) {
            socket.close();
            Log.i(TAG, "(SOCKET) <close>");
            return;
        }

        callback.onMessage(args[0].toString());
    }

    private void onDone(Object... args) {
        if (callback.isInactive()) {
            socket.close();
            Log.i(TAG, "(SOCKET) <close>");
            return;
        }

        try {
            List<SearchResult> results = new ArrayList<>();
            JSONArray objects = new JSONArray(args[0].toString());
            for (int i = 0; i < objects.length(); i++) {
                JSONObject object = objects.getJSONObject(i);
                results.add(new SearchResult(object));
            }
            callback.onDone(results);
            callback.onMessage("");
        } catch (JSONException e) {
            callback.onError("Could not parse server response");
            e.printStackTrace();
        }
        socket.close();
    }

    private void onConnect(Object... args) {
        if (callback.isInactive()) {
            socket.close();
            Log.i(TAG, "(SOCKET) <close>");
            return;
        }
        Log.i(TAG, "(SOCKET) Connected!");
    }

    private void onError(Object... args) {
        if (callback.isInactive()) {
            socket.close();
            Log.i(TAG, "(SOCKET) <close>");
            return;
        }
        String message = (String) args[0];
        Log.e(TAG, "(SOCKET) Error: " + message);
        callback.onError(message);
        callback.onMessage("");
    }

    private void onDisconnect(Object... args) {
        if (callback.isInactive()) {
            socket.close();
            Log.i(TAG, "(SOCKET) <close>");
            return;
        }
        String message = (String) args[0];
        Log.w(TAG, "(SOCKET) Disconnected: " + message);
    }

    private void onConnectError(Object... args) {
        if (callback.isInactive()) {
            socket.close();
            Log.i(TAG, "(SOCKET) <close>");
            return;
        }
        Log.e(TAG, "(SOCKET) Connect error");
        callback.onError("Could not connect to Server");
        socket.close();
    }

    public interface Callback {
        void onError(String message);

        boolean isInactive();

        void onResult(SearchResult results);

        void onMessage(String message);

        void onDone(List<SearchResult> sortedResults);
    }
}
