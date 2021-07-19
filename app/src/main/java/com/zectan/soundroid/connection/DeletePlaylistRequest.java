package com.zectan.soundroid.connection;

import com.zectan.soundroid.classes.Request;

public class DeletePlaylistRequest extends com.zectan.soundroid.classes.Request {

    public DeletePlaylistRequest(String playlistId, Callback callback) {
        super("http://soundroid.zectan.com/playlist/delete", new Request.Callback() {
            @Override
            public void onComplete(String response) {
                callback.onComplete();
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });

        putData("playlistId", playlistId);
        sendRequest(RequestType.DELETE);
    }

    public interface Callback {
        void onComplete();

        void onError(String message);
    }

}
