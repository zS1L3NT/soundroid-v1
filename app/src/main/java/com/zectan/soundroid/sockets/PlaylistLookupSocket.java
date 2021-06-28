package com.zectan.soundroid.sockets;

import android.content.Context;
import android.util.Log;

import com.zectan.soundroid.classes.Socket;
import com.zectan.soundroid.models.Info;
import com.zectan.soundroid.models.Playlist;
import com.zectan.soundroid.models.Song;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PlaylistLookupSocket extends Socket {
    private static final String TAG = "(SounDroid) PlaylistLookupSocket";
    private final Callback mCallback;
    private final Info mInfo;
    private final Context mContext;
    private final List<Song> mSongs;

    public PlaylistLookupSocket(Info info, Context context, Callback callback) {
        super(TAG, callback, "playlist_lookup", info.getId(), info.getCover());
        mCallback = callback;
        mContext = context;
        mInfo = info;
        mSongs = new ArrayList<>();

        Log.d(TAG, "(SOCKET) Playlist Lookup: " + info.getId());
        super.on("playlist_item", this::onSong);
        super.on("playlist_lookup", this::onResult);
    }

    private void onSong(Object... args) {
        try {
            JSONObject object = new JSONObject(args[0].toString());
            String id = object.getString("id");
            String title = object.getString("title");
            String artiste = object.getString("artiste");
            String cover = object.getString("cover");
            String colorHex = object.getString("colorHex");
            Song song = new Song(id, title, artiste, cover, colorHex).setDirectoryWith(mContext);
            mSongs.add(song);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void onResult(Object... args) {
        if (mCallback.isInactive()) {
            closeSocket();
            Log.i(TAG, "(SOCKET) <close>");
            return;
        }

        try {
            JSONArray objects = new JSONArray(args[0].toString());
            List<String> order = new ArrayList<>();
            for (int i = 0; i < objects.length(); i++) {
                order.add(objects.getString(i));
            }

            mInfo.setOrder(order);
            Playlist playlist = new Playlist(mInfo, mSongs);
            mCallback.onFinish(playlist);
        } catch (JSONException err) {
            mCallback.onError("Could not parse server response");
        }

        closeSocket();
    }

    public interface Callback extends Socket.Callback {
        void onFinish(Playlist playlist);

        void onSong(Song song);
    }

}
