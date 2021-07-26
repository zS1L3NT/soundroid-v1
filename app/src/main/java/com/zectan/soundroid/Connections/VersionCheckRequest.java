package com.zectan.soundroid.Connections;

import com.zectan.soundroid.Classes.Request;

public class VersionCheckRequest extends Request {

    public VersionCheckRequest(Callback callback) {
        super("/version", callback);

        sendRequest(RequestType.GET);
    }
}
