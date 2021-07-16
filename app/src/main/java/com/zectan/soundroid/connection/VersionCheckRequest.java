package com.zectan.soundroid.connection;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

public class VersionCheckRequest {
    private static final String URL = "http://soundroid.zectan.com/version";

    public VersionCheckRequest(Callback callback) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
            .url(URL)
            .get()
            .build();

        new Thread(() -> {
            try {
                Response response = client.newCall(request).execute();
                if (response.code() == 200) {
                    callback.onComplete(response.body().string());
                } else {
                    callback.onError(String.format("Server returned error code %s", response.code()));
                }
            } catch (IOException e) {
                callback.onError(e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    public interface Callback {
        void onComplete(String version);

        void onError(String message);
    }
}
