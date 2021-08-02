package com.zectan.soundroid.Connections;

import com.zectan.soundroid.Classes.Request;
import com.zectan.soundroid.Models.Playlist;

import org.json.JSONArray;

import java.util.List;

public class EditPlaylistRequest extends Request {

    public EditPlaylistRequest(Playlist playlist, List<String> removed, Callback callback) {
        super("/playlist/edit", callback);

        JSONArray removedArray = new JSONArray();
        for (String songId : removed) removedArray.put(songId);
        putData("removed", removedArray);
        putData("info", playlist.toJSON());
        sendRequest(RequestType.PUT);
    }
}
