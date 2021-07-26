package com.zectan.soundroid.Connections;

import com.zectan.soundroid.Classes.Request;
import com.zectan.soundroid.Models.Info;

import org.json.JSONArray;

import java.util.List;

public class EditPlaylistRequest extends Request {

    public EditPlaylistRequest(Info info, List<String> removed, Callback callback) {
        super("/playlist/edit", callback);

        JSONArray removedArray = new JSONArray();
        for (String songId : removed) removedArray.put(songId);
        putData("removed", removedArray);
        putData("info", info.toJSON());
        sendRequest(RequestType.PUT);
    }
}
