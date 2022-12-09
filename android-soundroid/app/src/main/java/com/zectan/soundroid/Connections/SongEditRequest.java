package com.zectan.soundroid.Connections;

import com.zectan.soundroid.Classes.Request;
import com.zectan.soundroid.Models.Song;

import org.json.JSONException;

public class SongEditRequest extends Request {

    /**
     * Edit a song
     *
     * @param song     Song
     * @param callback Callback
     */
    public SongEditRequest(Song song, Callback callback) {
        super("/song/edit", callback);

        try {
            replaceData(song.toJSON());
            sendRequest(RequestType.PUT);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
