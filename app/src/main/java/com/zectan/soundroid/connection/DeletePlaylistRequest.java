package com.zectan.soundroid.connection;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

public class DeletePlaylistRequest {
    private static final String URL = "http://soundroid.zectan.com/playlist/%s/delete";

    public DeletePlaylistRequest(String playlistId, Callback callback) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
            .url(String.format(URL, playlistId))
            .delete()
            .build();

        new Thread(() -> {
            try {
                Response response = client.newCall(request).execute();
                if (response.code() == 200) {
                    callback.onComplete();
                } else {
                    callback.onError(response.body().string());
                }
            } catch (IOException e) {
                callback.onError(e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    public interface Callback {
        void onComplete();

        void onError(String message);
    }

}
