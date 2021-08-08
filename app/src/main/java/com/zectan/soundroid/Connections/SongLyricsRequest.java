package com.zectan.soundroid.Connections;

import com.zectan.soundroid.Classes.Request;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class SongLyricsRequest extends Request {

    /**
     * Get lyrics using a query of the song details
     *
     * @param query    Song details that are URLEncoded
     * @param callback Callback
     */
    public SongLyricsRequest(String query, Callback callback) {
        super(String.format("/song/lyrics?query=%s", query), new Request.Callback() {
            @Override
            public void onComplete(String response) {
                List<String> lyrics = new ArrayList<>();
                try {
                    JSONArray array = new JSONArray(response);
                    for (int i = 0; i < array.length(); i++) lyrics.add(array.getString(i));
                    callback.onComplete(lyrics);
                } catch (JSONException e) {
                    e.printStackTrace();
                    callback.onError(e.getMessage());
                }
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
        replaceClient(
            new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()
        );
        sendRequest(RequestType.GET);
    }

    public interface Callback {
        void onComplete(List<String> lyrics);

        void onError(String message);
    }
}
