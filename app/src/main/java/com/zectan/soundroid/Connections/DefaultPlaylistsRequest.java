package com.zectan.soundroid.Connections;

import com.zectan.soundroid.Classes.Request;

public class DefaultPlaylistsRequest extends Request {

    public DefaultPlaylistsRequest(String userId) {
        super("/playlists/default", new Callback() {
            @Override
            public void onComplete(String response) {
            }
            @Override
            public void onError(String message) {
            }
        });

        putData("userId", userId);
        sendRequest(RequestType.POST);
    }

}
