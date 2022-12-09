package com.zectan.soundroid.Connections;

import com.zectan.soundroid.Classes.Request;
import com.zectan.soundroid.Models.Playlist;

import org.json.JSONArray;

import java.util.List;

public class PlaylistEditRequest extends Request {

    /**
     * Edit a playlist
     *
     * @param playlist Playlist
     * @param removed  List of removed songs
     * @param callback Callback
     */
    public PlaylistEditRequest(Playlist playlist, List<String> removed, Callback callback) {
        super("/playlist/edit", callback);

        JSONArray removedArray = new JSONArray();
        for (String songId : removed) removedArray.put(songId);
        putData("removed", removedArray);
        putData("info", playlist.toJSON());
        sendRequest(RequestType.PUT);
    }

}
