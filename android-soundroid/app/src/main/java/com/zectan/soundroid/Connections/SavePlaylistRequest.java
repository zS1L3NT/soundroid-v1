package com.zectan.soundroid.Connections;

import com.zectan.soundroid.Classes.Request;
import com.zectan.soundroid.Models.Playlist;

public class SavePlaylistRequest extends Request {

    /**
     * Save a playlist to Firestore
     *
     * @param playlist Playlist
     * @param callback Callback
     */
    public SavePlaylistRequest(Playlist playlist, Callback callback) {
        super("/playlist/save", callback);

        replaceData(playlist.toJSON());
        sendRequest(RequestType.PUT);
    }

}
