package com.zectan.soundroid.connection;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.zectan.soundroid.models.Song;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class PlaylistSongsRequest {
    private static final String TAG = "(SounDroid) PlaylistSongsRequest";
    private static final String URL = "http://soundroid.zectan.com/playlist/songs?playlistId=%s";

    public PlaylistSongsRequest(String playlistId, Callback callback) {
        if (playlistId.equals("")) {
            callback.onComplete(new ArrayList<>());
            return;
        }
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
            .url(String.format(URL, playlistId))
            .get()
            .build();

        new Thread(() -> {
            try {
                Response response = client.newCall(request).execute();
                if (response.code() == 200) {
                    JSONArray objects = new JSONArray(response.body().string());
                    List<Song> songs = new ArrayList<>();
                    for (int i = 0; i < objects.length(); i++) {
                        songs.add(Song.fromJSON(objects.getJSONObject(i)));
                    }
                    callback.onComplete(songs);
                } else {
                    callback.onError("Could not retrieve songs from server");
                }
            } catch (Exception e) {
                callback.onError(e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    public interface Callback {
        void onComplete(List<Song> songs);

        void onError(String message);
    }

}
