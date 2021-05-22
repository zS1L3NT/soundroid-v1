package com.zectan.soundroid.tasks;

import android.util.Log;

import java.net.URI;

import io.socket.client.IO;
import io.socket.client.Socket;

public class SongLinkFetchThread extends Thread {
    private static final String TAG = "(SounDroid) SongLinkFetchThread";
    private static final String SocketURL = "http://soundroid.zectan.com/";
    private final String id;
    private final FinishCallback finishCallback;
    private final ErrorCallback errorCallback;
    private final ConvertingCallback convertingCallback;
    private final ProgressCallback progressCallback;
    private final ActiveState activeState;

    public SongLinkFetchThread(
            String id,
            FinishCallback finishCallback,
            ErrorCallback errorCallback,
            ConvertingCallback convertingCallback,
            ProgressCallback progressCallback,
            ActiveState activeState
    ) {
        this.id = id;
        this.finishCallback = finishCallback;
        this.errorCallback = errorCallback;
        this.convertingCallback = convertingCallback;
        this.progressCallback = progressCallback;
        this.activeState = activeState;
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
            if (!activeState.get()) {
                socket.close();
                Log.i(TAG, "(SOCKET) close");
                return;
            }
            Log.i(TAG, "(SOCKET) Connected!");
        });

        socket.on("song_converted_" + id, args -> {
            if (!activeState.get()) {
                socket.close();
                Log.i(TAG, "(SOCKET) close");
                return;
            }
            String link = (String) args[0];
            Log.i(TAG, "(SOCKET) Converted: " + link);
            finishCallback.run(link);
            convertingCallback.run(false);
            socket.close();
        });

        socket.on("song_downloading_" + id, args -> {
            if (!activeState.get()) {
                socket.close();
                Log.i(TAG, "(SOCKET) close");
                return;
            }
            Log.d(TAG, "(SOCKET) Downloading...");
            progressCallback.update(0);
            convertingCallback.run(true);
        });

        socket.on("song_download_progress_" + id, args -> {
            if (!activeState.get()) {
                socket.close();
                Log.i(TAG, "(SOCKET) close");
                return;
            }
            int percent = (int) args[0];
            Log.d(TAG, "(SOCKET) Progress: " + percent);
            progressCallback.update(percent);
        });

        socket.on("error_" + id, args -> {
            if (!activeState.get()) {
                socket.close();
                Log.i(TAG, "(SOCKET) close");
                return;
            }
            String message = (String) args[0];
            Log.e(TAG, "(SOCKET) Error: " + message);
            errorCallback.run(message);
        });

        socket.on(Socket.EVENT_DISCONNECT, args -> {
            if (!activeState.get()) {
                socket.close();
                Log.i(TAG, "(SOCKET) close");
                return;
            }
            String message = (String) args[0];
            Log.w(TAG, "(SOCKET) Disconnected: " + message);
        });

        socket.on(Socket.EVENT_CONNECT_ERROR, args -> {
            if (!activeState.get()) {
                socket.close();
                Log.i(TAG, "(SOCKET) close");
                return;
            }
            Log.e(TAG, "(SOCKET) Connect error");
            errorCallback.run("Could not connect to Server");
            socket.close();
        });
    }

    public interface FinishCallback {
        void run(String link);
    }

    public interface ErrorCallback {
        void run(String message);
    }

    public interface ConvertingCallback {
        void run(boolean converting);
    }

    public interface ProgressCallback {
        void update(int progress);
    }

    public interface ActiveState {
        boolean get();
    }

}
