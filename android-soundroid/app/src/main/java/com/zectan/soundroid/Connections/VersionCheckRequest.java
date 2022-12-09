package com.zectan.soundroid.Connections;

import com.zectan.soundroid.Classes.Request;

public class VersionCheckRequest extends Request {

    /**
     * Check for the latest version of the app
     *
     * @param callback Callback
     */
    public VersionCheckRequest(Callback callback) {
        super("/version", callback);

        sendRequest(RequestType.GET);
    }

}
