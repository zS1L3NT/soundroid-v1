package com.zectan.soundroid.tasks;

import android.graphics.Color;
import android.util.Log;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.zectan.soundroid.objects.Song;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SongSearchTask extends Thread {
    private static final String TAG = "(SounDroid) SongSearchTask";
    private static final String API = "http://dl.zectan.com/api/search";
    private final String query;
    private final String folder;
    private final Callback callback;

    public SongSearchTask(String query, String folder, Callback callback) {
        this.query = query;
        this.folder = folder;
        this.callback = callback;
    }

    @Override
    public void run() {
        super.run();
        HashMap<String, String> params = new HashMap<>();
        params.put("query", query);
        String JSON = new JSONObject(params).toString();

        RequestBody body = RequestBody.create(MediaType.parse("application/json"), JSON);
        Request request = new Request.Builder().url(API).post(body).build();

        try {
            OkHttpClient client = new OkHttpClient();
            client.setReadTimeout(1, TimeUnit.MINUTES);
            client.setWriteTimeout(1, TimeUnit.MINUTES);
            client.setConnectTimeout(1, TimeUnit.MINUTES);
            Response response = client.newCall(request).execute();

            if (response.code() == 200) {
                JSONArray songs = new JSONArray(response.body().string());

                List<Song> songList = new ArrayList<>();
                for (int i = 0; i < songs.length(); i++) {
                    JSONObject song = songs.getJSONObject(i);

                    String id = song.getString("id");
                    String title = song.getString("title");
                    String artiste = song.getString("artiste");
                    String cover = song.getString("cover");
                    String colorHexString = song.getString("colorHex");

                    songList.add(new Song(folder, id, title, artiste, cover, Color.parseColor(colorHexString)));
                }

                Log.i(TAG, "API_OK: " + songList.size() + " songs");
                callback.run(songList);
            } else {
                Log.e(TAG, "API_BAD: " + response.body().string());
            }
        } catch (IOException | JSONException e) {
            Log.e(TAG, "API_CRASH: " + API);
            Log.e(TAG, e.getMessage());
        }
    }

    public interface Callback {
        void run(List<Song> songList);
    }
}
