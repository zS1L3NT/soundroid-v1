package com.zectan.soundroid.Connections;

import com.zectan.soundroid.Classes.Request;
import com.zectan.soundroid.Models.Song;

public class SongPingRequest extends Request {

    /**
     * Ping a song on the server to check if it exists
     *
     * @param song        Song to ping
     * @param highQuality Quality of song
     * @param callback    Callback
     */
    public SongPingRequest(Song song, boolean highQuality, Callback callback) {
        super(String.format("/ping/%s/%s.mp3", highQuality ? "highest" : "lowest", song.getSongId()), callback);
        sendRequest(RequestType.GET);
    }

}
