package com.zectan.soundroid.connection;

import com.zectan.soundroid.classes.Request;

public class ImportPlaylistRequest extends Request {
    public ImportPlaylistRequest(String url, String userId, Callback callback) {
        super("http://soundroid.zectan.com/playlist/import", new Request.Callback() {
            @Override
            public void onComplete(String response) {
                callback.onComplete();
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });

        putData("url", url);
        putData("userId", userId);
        sendRequest(RequestType.POST);
    }

    public interface Callback {
        void onComplete();

        void onError(String message);
    }
}
