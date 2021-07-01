package com.zectan.soundroid.connection;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.zectan.soundroid.models.Info;

import java.io.IOException;

public class SavePlaylistRequest {
    private static final String URL = "http://soundroid.zectan.com/playlist/save";

    public SavePlaylistRequest(Info info, Callback callback) {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(JSON, info.toJSON().toString());

        Request request = new Request.Builder()
            .url(URL)
            .put(body)
            .build();

        new Thread(() -> {
            try {
                Response response = client.newCall(request).execute();
                if (response.code() == 200) {
                    callback.onComplete();
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
        void onComplete();

        void onError(String message);
    }

}
