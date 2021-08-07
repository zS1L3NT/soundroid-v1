package com.zectan.soundroid.Connections;

import com.zectan.soundroid.Classes.Request;
import com.zectan.soundroid.Models.Song;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class SongFullRequest extends Request {

    /**
     * Get a full song from the server
     *
     * @param song        Song
     * @param highQuality Quality of song
     * @param callback    Callback
     */
    public SongFullRequest(Song song, boolean highQuality, Callback callback) {
        super(String.format("/song/%s/%s.mp3", highQuality ? "highest" : "lowest", song.getSongId()), callback);
        replaceClient(
            new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.MINUTES)
                .writeTimeout(5, TimeUnit.MINUTES)
                .readTimeout(5, TimeUnit.MINUTES)
                .build()
        );
        sendRequest(Request.RequestType.GET);
    }

}
