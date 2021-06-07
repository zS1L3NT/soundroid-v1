package com.zectan.soundroid.sockets;

import android.content.Context;
import android.util.Log;

import com.zectan.soundroid.objects.Song;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SearchAllSocket extends Socket {
    private static final String TAG = "(SounDroid) SearchAllSocket";
    private final Callback callback;
    private final Context context;

    public SearchAllSocket(String query, Context context, Callback callback) {
        super(TAG, callback, "search_all", query);
        this.callback = callback;
        this.context = context;

        Log.d(TAG, "(SOCKET) Searching: " + query);
        super.on("search_result", this::onSearchResult);
    }

    private void onSearchResult(Object... args) {
        if (callback.isInactive()) {
            closeSocket();
            Log.i(TAG, "(SOCKET) close");
            return;
        }

        try {
            JSONArray songObjects = new JSONArray(args[0].toString());
            List<Song> songs = new ArrayList<>();
            for (int i = 0; i < songObjects.length(); i++) {
                JSONObject song = songObjects.getJSONObject(i);

                String id = song.getString("id");
                String title = song.getString("title");
                String artiste = song.getString("artiste");
                String cover = song.getString("cover");
                String colorHex = song.getString("colorHex");

                songs.add(new Song(id, title, artiste, cover, colorHex).setDirectoryWith(context));
            }

            Log.i(TAG, "(SOCKET) Results: " + songs.size());
            callback.onFinish(songs);
        } catch (JSONException e) {
            callback.onError("Could not parse server response");
            e.printStackTrace();
        }
    }

    public interface Callback extends Socket.Callback {
        void onFinish(List<Song> songList);
    }
}
