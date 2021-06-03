package com.zectan.soundroid.tasks;

import android.util.Log;

import java.net.URI;

import io.socket.client.IO;
import io.socket.client.Socket;

public class SongLinkFetchThread extends Thread {
    private static final String TAG = "(SounDroid) SongLinkFetchThread";
    private static final String SocketURL = "http://soundroid.zectan.com/";
    private final Callback callback;
    private final String id;
    
    public SongLinkFetchThread(String id, Callback callback) {
        this.id = id;
        this.callback = callback;
    }
    
    @Override
    public void run() {
        super.run();
        IO.Options options = IO.Options
            .builder()
            .setTimeout(60_000)
                .build();
        Socket socket = IO.socket(URI.create(SocketURL), options).connect();

        Log.d(TAG, "(SOCKET) Sent: " + id);
        socket.emit("convert_song", id);

        socket.on(Socket.EVENT_CONNECT, __ -> {
            if (callback.isInactive()) {
                socket.close();
                Log.i(TAG, "(SOCKET) close");
                return;
            }
            Log.i(TAG, "(SOCKET) Connected!");
        });

        socket.on("song_converted_" + id, args -> {
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
        });

        socket.on("song_downloading_" + id, args -> {
            if (callback.isInactive()) {
                socket.close();
                Log.i(TAG, "(SOCKET) close");
                return;
            }
            Log.d(TAG, "(SOCKET) Downloading...");
            callback.onProgress(0);
            callback.isConverting(true);
        });

        socket.on("song_download_progress_" + id, args -> {
            if (callback.isInactive()) {
                socket.close();
                Log.i(TAG, "(SOCKET) close");
                return;
            }
            int percent = (int) args[0];
            Log.d(TAG, "(SOCKET) Progress: " + percent);
            callback.onProgress(percent);
        });

        socket.on("error_" + id, args -> {
            if (callback.isInactive()) {
                socket.close();
                Log.i(TAG, "(SOCKET) close");
                return;
            }
            String message = (String) args[0];
            Log.e(TAG, "(SOCKET) Error: " + message);
            callback.onError(message);
        });

        socket.on(Socket.EVENT_DISCONNECT, args -> {
            if (callback.isInactive()) {
                socket.close();
                Log.i(TAG, "(SOCKET) close");
                return;
            }
            String message = (String) args[0];
            Log.w(TAG, "(SOCKET) Disconnected: " + message);
        });

        socket.on(Socket.EVENT_CONNECT_ERROR, args -> {
            if (callback.isInactive()) {
                socket.close();
                Log.i(TAG, "(SOCKET) close");
                return;
            }
            Log.e(TAG, "(SOCKET) Connect error");
            callback.onError("Could not connect to Server");
            socket.close();
        });
    }
    
    public interface Callback {
        void onFinish(String link);
        
        void onError(String message);
        
        void isConverting(boolean converting);
        
        void onProgress(int progress);
        
        boolean isInactive();
    }
    
}
