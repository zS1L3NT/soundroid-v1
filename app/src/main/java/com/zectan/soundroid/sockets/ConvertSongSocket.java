package com.zectan.soundroid.sockets;

import android.util.Log;

import java.net.URI;
import java.util.UUID;

import io.socket.client.IO;
import io.socket.client.Socket;

public class ConvertSongSocket {
    private static final String TAG = "(SounDroid) ConvertSongThread";
    private static final String SocketURL = "http://soundroid.zectan.com/";
    private final Callback callback;
    private final Socket socket;

    public ConvertSongSocket(String id, Callback callback) {
        this.callback = callback;

        IO.Options options = IO.Options.builder().setTimeout(60_000).build();
        socket = IO.socket(URI.create(SocketURL), options).connect();
        String uuid = UUID.randomUUID().toString();

        Log.d(TAG, "(SOCKET) Convert: " + id);
        socket.emit("convert_song", uuid, id);

        socket.on("song_converted_" + id, this::onSongConverted);
        socket.on("song_downloading_" + id, this::onSongDownloading);
        socket.on("song_download_progress_" + id, this::onSongDownloadProgress);

        socket.on(Socket.EVENT_CONNECT, this::onConnect);
        socket.on("error_" + id, this::onError);
        socket.on(Socket.EVENT_DISCONNECT, this::onDisconnect);
        socket.on(Socket.EVENT_CONNECT_ERROR, this::onConnectError);
    }

    private void onSongConverted(Object... args) {
        if (callback.isInactive()) {
            socket.close();
            Log.i(TAG, "(SOCKET) close");
            return;
        }
        String link = (String) args[0];
        Log.i(TAG, "(SOCKET) Converted: " + link);
        callback.onFinish(link);
        callback.isConverting(false);
        socket.close();
    }

    private void onSongDownloading(Object... args) {
        if (callback.isInactive()) {
            socket.close();
            Log.i(TAG, "(SOCKET) close");
            return;
        }
        Log.d(TAG, "(SOCKET) Downloading...");
        callback.onProgress(0);
        callback.isConverting(true);
    }

    private void onSongDownloadProgress(Object... args) {
        if (callback.isInactive()) {
            socket.close();
            Log.i(TAG, "(SOCKET) close");
            return;
        }
        int percent = (int) args[0];
        Log.d(TAG, "(SOCKET) Progress: " + percent);
        callback.onProgress(percent);
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
        void onFinish(String link);

        void onError(String message);

        void isConverting(boolean converting);

        void onProgress(int progress);

        boolean isInactive();
    }

}
