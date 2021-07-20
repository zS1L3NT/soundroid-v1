package com.zectan.soundroid.Connections;

import com.zectan.soundroid.Classes.Request;
import com.zectan.soundroid.Models.Song;

import org.json.JSONException;

public class SongEditRequest extends Request {

    public SongEditRequest(Song song, Callback callback) {
        super("http://soundroid.zectan.com/song/edit", new Request.Callback() {
            @Override
            public void onComplete(String response) {
                callback.onComplete();
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });

        try {
            replaceData(song.toJSON());
            sendRequest(RequestType.PUT);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public interface Callback {
        void onComplete();

        void onError(String message);
    }
}
