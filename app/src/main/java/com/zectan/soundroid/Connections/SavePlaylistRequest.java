package com.zectan.soundroid.Connections;

import com.zectan.soundroid.Classes.Request;
import com.zectan.soundroid.Models.Playlist;

public class SavePlaylistRequest extends Request {

    public SavePlaylistRequest(Playlist playlist, Callback callback) {
        super("/playlist/save", callback);

        replaceData(playlist.toJSON());
        sendRequest(RequestType.PUT);
    }

}
