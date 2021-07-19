package com.zectan.soundroid.connection;

import com.zectan.soundroid.classes.Request;

public class PingSongRequest extends Request {

    public PingSongRequest(String songId, boolean highQuality, Callback callback) {
        super(String.format("http://soundroid.zectan.com/song/%s/%s.mp3", highQuality ? "highest" : "lowest", songId), new Request.Callback() {
            @Override
            public void onComplete(String response) {
                callback.onCallback();
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });

        sendRequest(RequestType.GET);
    }

    public interface Callback {
        void onCallback();

        void onError(String message);
    }

}
