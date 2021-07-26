package com.zectan.soundroid.Connections;

import com.zectan.soundroid.Classes.Request;
import com.zectan.soundroid.Models.Info;

public class SavePlaylistRequest extends Request {

    public SavePlaylistRequest(Info info, Callback callback) {
        super("/playlist/save", callback);

        replaceData(info.toJSON());
        sendRequest(RequestType.PUT);
    }

}
