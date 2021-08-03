package com.zectan.soundroid.Connections;

import com.zectan.soundroid.Classes.Request;
import com.zectan.soundroid.Models.Song;

public class SongPingRequest extends Request {

    /**
     * Ping a song on the server to check if it exists
     *
     * @param song                Song to ping
     * @param highDownloadQuality Quality of song
     * @param callback            Callback
     */
    public SongPingRequest(Song song, boolean highDownloadQuality, Callback callback) {
        super(String.format("/ping/%s/%s.mp3", highDownloadQuality ? "highest" : "lowest", song.getSongId()), callback);
        sendRequest(RequestType.GET);
    }

}
