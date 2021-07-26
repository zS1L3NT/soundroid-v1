package com.zectan.soundroid.Connections;

import com.zectan.soundroid.Classes.Request;

public class DefaultPlaylistsRequest extends Request {

    public DefaultPlaylistsRequest(String userId, Callback callback) {
        super("/playlists/default", callback);

        putData("userId", userId);
        sendRequest(RequestType.POST);
    }

}
