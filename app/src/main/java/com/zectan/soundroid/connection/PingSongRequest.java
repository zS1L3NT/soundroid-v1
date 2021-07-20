package com.zectan.soundroid.connection;

import com.zectan.soundroid.classes.Request;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class PingSongRequest extends Request {

    public PingSongRequest(String songId, boolean highQuality, Callback callback) {
        super(String.format("http://soundroid.zectan.com/song/%s/%s.mp3", highQuality ? "highest" : "lowest", songId), new Request.Callback() {
            @Override
            public void onComplete(String response) {
                callback.cancelTimeHandler();
                if (callback.isContinued()) {
                    callback.onCallback();
                }
            }

            @Override
            public void onError(String message) {
                callback.cancelTimeHandler();
                if (callback.isContinued()) {
                    callback.onError(message);
                }
            }
        });

        replaceClient(
            new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.MINUTES)
                .writeTimeout(5, TimeUnit.MINUTES)
                .readTimeout(5, TimeUnit.MINUTES)
                .build()
        );
        sendRequest(RequestType.GET);
    }

    public interface Callback {
        void onCallback();

        void onError(String message);

        void cancelTimeHandler();

        boolean isContinued();
    }

}
