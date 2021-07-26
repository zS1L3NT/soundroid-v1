package com.zectan.soundroid.Connections;

import com.zectan.soundroid.Classes.Request;

public class ImportPlaylistRequest extends Request {
    public ImportPlaylistRequest(String url, String userId, Callback callback) {
        super("/playlist/import", callback);

        putData("url", url);
        putData("userId", userId);
        sendRequest(RequestType.POST);
    }
}
