package com.zectan.soundroid.Connections;

public class DeletePlaylistRequest extends com.zectan.soundroid.Classes.Request {

    public DeletePlaylistRequest(String playlistId, Callback callback) {
        super("/playlist/delete", callback);

        putData("playlistId", playlistId);
        sendRequest(RequestType.DELETE);
    }

}
