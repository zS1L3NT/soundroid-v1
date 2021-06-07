package com.zectan.soundroid.sockets;

import android.util.Log;

public class CoverColorSocket extends Socket {
    private static final String TAG = "(SounDroid) CoverColorSocket";
    private static final String SocketURL = "http://soundroid.zectan.com/";
    private final Callback callback;

    public CoverColorSocket(String coverUrl, Callback callback) {
        super(TAG, callback, "cover_color", coverUrl);
        this.callback = callback;

        Log.d(TAG, "(SOCKET) Getting color: " + coverUrl);
        super.on("cover_color_result", this::onCoverColorResult);
    }

    private void onCoverColorResult(Object... args) {
        if (callback.isInactive()) {
            closeSocket();
            Log.i(TAG, "(SOCKET) close");
            return;
        }
        String colorHex = (String) args[0];
        callback.onFinish(colorHex);
        Log.d(TAG, "(SOCKET) Color: " + colorHex);
    }

    public interface Callback extends Socket.Callback {
        void onFinish(String colorHex);
    }
}
