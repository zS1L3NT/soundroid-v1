package com.zectan.soundroid.connection;

import com.zectan.soundroid.classes.Request;

public class VersionCheckRequest extends Request {

    public VersionCheckRequest(Callback callback) {
        super("http://soundroid.zectan.com/version", new Request.Callback() {
            @Override
            public void onComplete(String response) {
                callback.onComplete(response);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });

        sendRequest(RequestType.GET);
    }

    public interface Callback {
        void onComplete(String version);

        void onError(String message);
    }
}
