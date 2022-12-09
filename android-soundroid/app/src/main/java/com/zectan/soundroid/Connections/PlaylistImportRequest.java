package com.zectan.soundroid.Connections;

import com.zectan.soundroid.Classes.Request;

public class PlaylistImportRequest extends Request {

    /**
     * Import a playlist for a user's account
     *
     * @param url      Playlist URL
     * @param userId   User ID
     * @param callback Callback
     */
    public PlaylistImportRequest(String url, String userId, Callback callback) {
        super("/playlist/import", callback);

        putData("url", url);
        putData("userId", userId);
        sendRequest(RequestType.POST);
    }

}
