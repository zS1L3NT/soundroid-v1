package com.zectan.soundroid.sockets;

import android.util.Log;

public class ConvertSongSocket extends Socket {
    private static final String TAG = "(SounDroid) ConvertSongThread";
    private final Callback callback;

    public ConvertSongSocket(String id, Callback callback) {
        super(TAG, callback, "convert_song", id);
        this.callback = callback;

        Log.d(TAG, "Convert Song: " + id);
        super.on("convert_song", this::onSongConverted);
        super.on("convert_song_downloading", this::onSongDownloading);
        super.on("convert_song_progress", this::onSongDownloadProgress);
    }

    private void onSongConverted(Object... args) {
        if (callback.isInactive()) {
            closeSocket();
            Log.i(TAG, "(SOCKET) close");
            return;
        }
        String link = (String) args[0];
        Log.i(TAG, "(SOCKET) Converted: " + link);
        callback.onFinish(link);
        callback.isConverting(false);
        closeSocket();
    }

    private void onSongDownloading(Object... args) {
        if (callback.isInactive()) {
            closeSocket();
            Log.i(TAG, "(SOCKET) close");
            return;
        }
        Log.d(TAG, "(SOCKET) Downloading...");
        callback.onProgress(0);
        callback.isConverting(true);
    }

    private void onSongDownloadProgress(Object... args) {
        if (callback.isInactive()) {
            closeSocket();
            Log.i(TAG, "(SOCKET) close");
            return;
        }
        int percent = (int) args[0];
        Log.d(TAG, "(SOCKET) Progress: " + percent);
        callback.onProgress(percent);
    }

    public interface Callback extends Socket.Callback {
        void onFinish(String link);

        void isConverting(boolean converting);

        void onProgress(int progress);
    }

}
