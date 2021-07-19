package com.zectan.soundroid.connection;

import com.zectan.soundroid.classes.Request;
import com.zectan.soundroid.models.Info;

import org.json.JSONArray;

import java.util.List;

public class EditPlaylistRequest extends Request {

    public EditPlaylistRequest(Info info, List<String> removed, Callback callback) {
        super("http://soundroid.zectan.com/playlist/edit", new Request.Callback() {
            @Override
            public void onComplete(String response) {
                callback.onComplete();
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });

        JSONArray removedArray = new JSONArray();
        for (String songId : removed) removedArray.put(songId);
        putData("removed", removedArray);
        putData("info", info.toJSON());
        sendRequest(RequestType.PUT);
    }

    public interface Callback {
        void onComplete();

        void onError(String message);
    }
}
