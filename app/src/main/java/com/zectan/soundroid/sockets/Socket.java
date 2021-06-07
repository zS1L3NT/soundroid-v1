package com.zectan.soundroid.sockets;

import android.util.Log;

import java.net.URI;
import java.util.UUID;

import io.socket.client.IO;
import io.socket.emitter.Emitter;

public class Socket {
    private static final String SocketURL = "http://soundroid.zectan.com/";
    private final io.socket.client.Socket socket;
    private final Callback callback;
    private final String TAG;
    private final String uuid;

    public Socket(String TAG, Callback callback, String startEvent, String startData) {
        this.TAG = TAG;
        this.callback = callback;
        IO.Options options = IO.Options.builder().setTimeout(60_000).build();
        socket = IO.socket(URI.create(SocketURL), options).connect();
        uuid = UUID.randomUUID().toString();

        socket.emit(startEvent, uuid, startData);
        socket.on(io.socket.client.Socket.EVENT_CONNECT, this::onConnect);
        socket.on("error_" + uuid, this::onError);
        socket.on(io.socket.client.Socket.EVENT_DISCONNECT, this::onDisconnect);
        socket.on(io.socket.client.Socket.EVENT_CONNECT_ERROR, this::onConnectError);
    }

    protected void on(String event, Emitter.Listener fn) {
        socket.on(String.format("%s_%s", event, uuid), fn);
    }

    protected void closeSocket() {
        socket.close();
    }

    private void onConnect(Object... args) {
        if (callback.isInactive()) {
            socket.close();
            Log.i(TAG, "(SOCKET) close");
            return;
        }
        Log.i(TAG, "(SOCKET) Connected!");
    }

    private void onError(Object... args) {
        if (callback.isInactive()) {
            socket.close();
            Log.i(TAG, "(SOCKET) close");
            return;
        }
        String message = (String) args[0];
        Log.e(TAG, "(SOCKET) Error: " + message);
        callback.onError(message);
    }

    private void onDisconnect(Object... args) {
        if (callback.isInactive()) {
            socket.close();
            Log.i(TAG, "(SOCKET) close");
            return;
        }
        String message = (String) args[0];
        Log.w(TAG, "(SOCKET) Disconnected: " + message);
    }

    private void onConnectError(Object... args) {
        if (callback.isInactive()) {
            socket.close();
            Log.i(TAG, "(SOCKET) close");
            return;
        }
        Log.e(TAG, "(SOCKET) Connect error");
        callback.onError("Could not connect to Server");
        socket.close();
    }

    public interface Callback {
        void onError(String message);

        boolean isInactive();
    }

}
