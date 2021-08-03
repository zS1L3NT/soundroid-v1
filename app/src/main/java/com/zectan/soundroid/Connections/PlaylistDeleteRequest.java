package com.zectan.soundroid.Connections;

import com.zectan.soundroid.Classes.Request;

public class PlaylistDeleteRequest extends Request {

    /**
     * Delete a playlist
     *
     * @param playlistId Playlist ID
     * @param callback   Callback
     */
    public PlaylistDeleteRequest(String playlistId, Callback callback) {
        super("/playlist/delete", callback);

        putData("playlistId", playlistId);
        sendRequest(RequestType.DELETE);
    }

}
