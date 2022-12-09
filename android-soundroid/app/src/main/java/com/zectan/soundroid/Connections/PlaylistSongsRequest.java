package com.zectan.soundroid.Connections;

import com.zectan.soundroid.Classes.Request;
import com.zectan.soundroid.Models.Song;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class PlaylistSongsRequest extends Request {

    /**
     * Get songs from a playlist
     *
     * @param playlistId Playlist ID
     * @param callback   Callback
     */
    public PlaylistSongsRequest(String playlistId, Callback callback) {
        super("/playlist/songs?playlistId=" + playlistId, new Request.Callback() {
            @Override
            public void onComplete(String response) {
                try {
                    // Convert response to JSON object
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
