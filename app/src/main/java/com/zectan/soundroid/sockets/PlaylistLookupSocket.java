package com.zectan.soundroid.sockets;

import android.content.Context;
import android.util.Log;

import com.zectan.soundroid.objects.Playlist;
import com.zectan.soundroid.objects.PlaylistInfo;
import com.zectan.soundroid.objects.Song;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PlaylistLookupSocket extends Socket {
    private static final String TAG = "(SounDroid) PlaylistLookupSocket";
    private final Callback callback;
    private final PlaylistInfo info;
    private final Context context;

    public PlaylistLookupSocket(PlaylistInfo info, Context context, Callback callback) {
        super(TAG, callback, "playlist_lookup", info.getId(), info.getCover());
        this.callback = callback;
        this.context = context;
        this.info = info;

        Log.d(TAG, "(SOCKET) Playlist Lookup: " + info.getId());
        super.on("playlist_lookup", this::onResult);
    }

    private void onResult(Object... args) {
        if (callback.isInactive()) {
            closeSocket();
            Log.i(TAG, "(SOCKET) <close>");
            return;
        }

        try {
            JSONArray objects = new JSONArray(args[0].toString());
            List<Song> songs = new ArrayList<>();
            List<String> order = new ArrayList<>();
            for (int i = 0; i < objects.length(); i++) {
                JSONObject song = objects.getJSONObject(i);
                String id = song.getString("id");
                String title = song.getString("title");
                String artiste = song.getString("artiste");
                String cover = song.getString("cover");
                String songColorHex = song.getString("colorHex");
                songs.add(new Song(id, title, artiste, cover, songColorHex).setDirectoryWith(context));
                order.add(id);
            }

            info.setOrder(order);
            Playlist playlist = new Playlist(info, songs);
            callback.onFinish(playlist);
        } catch (JSONException err) {
            callback.onError("Could not parse server response");
        }

        closeSocket();
    }

    public interface Callback extends Socket.Callback {
        void onFinish(Playlist playlist);
    }

}
