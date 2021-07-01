package com.zectan.soundroid.connection;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class DeletePlaylistRequest {
    private static final String URL = "http://soundroid.zectan.com/playlist/delete";

    public DeletePlaylistRequest(String playlistId, Callback callback) {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        JSONObject object = new JSONObject();
        try {
            object.put("playlistId", playlistId);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(JSON, object.toString());

        Request request = new Request.Builder()
            .url(URL)
            .delete(body)
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
