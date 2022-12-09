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
    private final Callback mCallback;
    private final Socket mSocket;

    /**
     * SocketIO request to the server for search results
     *
     * @param query    Search query
     * @param callback Callback
     */
    public SearchSocket(String query, Callback callback) {
        mCallback = callback;
        IO.Options options = IO.Options.builder().setTimeout(60_000).build();
        mSocket = IO.socket(URI.create(SocketURL), options).connect();

        Log.d(TAG, "Search: " + query);
        mSocket.emit("search", query);
        mSocket.on("search_result_" + query, this::onResult);
        mSocket.on("search_message_" + query, this::onMessage);
        mSocket.on("search_done_" + query, this::onDone);
        mSocket.on(Socket.EVENT_CONNECT, this::onConnect);
        mSocket.on("error_" + query, this::onError);
        mSocket.on(Socket.EVENT_DISCONNECT, this::onDisconnect);
        mSocket.on(Socket.EVENT_CONNECT_ERROR, this::onConnectError);
    }

    /**
     * When receiving a search result
     *
     * @param args Args
     */
    private void onResult(Object... args) {
        if (mCallback.isInactive()) {
            mSocket.close();
            Log.i(TAG, "(SOCKET) <close>");
            return;
        }

        try {
            JSONObject object = new JSONObject(args[0].toString());
            mCallback.onResult(new SearchResult(object));
        } catch (JSONException e) {
            mCallback.onError("Could not parse server response");
            e.printStackTrace();
        }
    }

    /**
     * When receiving a new message
     *
     * @param args Args
     */
    private void onMessage(Object... args) {
        if (mCallback.isInactive()) {
            mSocket.close();
            Log.i(TAG, "(SOCKET) <close>");
            return;
        }

        mCallback.onMessage(args[0].toString());
    }

    /**
     * When search is done
     *
     * @param args Args
     */
    private void onDone(Object... args) {
        if (mCallback.isInactive()) {
            mSocket.close();
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
            mCallback.onDone(results);
            mCallback.onMessage("");
        } catch (JSONException e) {
            mCallback.onError("Could not parse server response");
            e.printStackTrace();
        }
        mSocket.close();
    }

    private void onConnect(Object... args) {
        if (mCallback.isInactive()) {
            mSocket.close();
            Log.i(TAG, "(SOCKET) <close>");
            return;
        }
        Log.i(TAG, "(SOCKET) Connected!");
    }

    private void onError(Object... args) {
        if (mCallback.isInactive()) {
            mSocket.close();
            Log.i(TAG, "(SOCKET) <close>");
            return;
        }
        String message = (String) args[0];
        Log.e(TAG, "(SOCKET) Error: " + message);
        mCallback.onError(message);
        mCallback.onMessage("");
    }

    private void onDisconnect(Object... args) {
        if (mCallback.isInactive()) {
            mSocket.close();
            Log.i(TAG, "(SOCKET) <close>");
            return;
        }
        String message = (String) args[0];
        Log.w(TAG, "(SOCKET) Disconnected: " + message);
    }

    private void onConnectError(Object... args) {
        if (mCallback.isInactive()) {
            mSocket.close();
            Log.i(TAG, "(SOCKET) <close>");
            return;
        }
        Log.e(TAG, "(SOCKET) Connect error");
        mCallback.onError("Could not connect to Server");
        mSocket.close();
    }

    public interface Callback {
        void onError(String message);

        boolean isInactive();

        void onResult(SearchResult results);

        void onMessage(String message);

        void onDone(List<SearchResult> sortedResults);
    }
}
