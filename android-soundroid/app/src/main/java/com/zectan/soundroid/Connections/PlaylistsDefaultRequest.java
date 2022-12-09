package com.zectan.soundroid.Connections;

import com.zectan.soundroid.Classes.Request;

public class PlaylistsDefaultRequest extends Request {

    /**
     * Inflate the user's account with the list of default playlists
     *
     * @param userId User ID
     */
    public PlaylistsDefaultRequest(String userId) {
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
