package com.zectan.soundroid.Connections;

import com.zectan.soundroid.Classes.Request;
import com.zectan.soundroid.Models.Info;

public class SavePlaylistRequest extends Request {

    public SavePlaylistRequest(Info info, Callback callback) {
        super("http://soundroid.zectan.com/playlist/save", new Request.Callback() {
            @Override
            public void onComplete(String response) {
                callback.onComplete();
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });

        replaceData(info.toJSON());
        sendRequest(RequestType.PUT);
    }

    public interface Callback {
        void onComplete();

        void onError(String message);
    }

}
