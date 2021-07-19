package com.zectan.soundroid.connection;

import com.zectan.soundroid.classes.Request;
import com.zectan.soundroid.models.Song;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class PlaylistSongsRequest extends Request {
    public PlaylistSongsRequest(String playlistId, Callback callback) {
        super(String.format("http://soundroid.zectan.com/playlist/songs?playlistId=%s", playlistId), new Request.Callback() {
            @Override
            public void onComplete(String response) {
                try {
                    JSONArray objects = new JSONArray(response);
                    List<Song> songs = new ArrayList<>();
                    for (int i = 0; i < objects.length(); i++) {
                        songs.add(Song.fromJSON(objects.getJSONObject(i)));
                    }
                    callback.onComplete(songs);
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

        if (playlistId.equals("")) {
            callback.onComplete(new ArrayList<>());
            return;
        }
        sendRequest(RequestType.GET);
    }

    public interface Callback {
        void onComplete(List<Song> songs);

        void onError(String message);
    }

}
