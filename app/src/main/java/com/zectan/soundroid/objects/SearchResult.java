package com.zectan.soundroid.objects;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

public class SearchResult {
    private PlaylistInfo playlistInfo;
    private Song song;

    public SearchResult(JSONObject object, Context context) throws JSONException {
        String type = object.getString("type");

        if (type.equals("Song")) {
            String id = object.getString("id");
            String title = object.getString("title");
            String artiste = object.getString("artiste");
            String cover = object.getString("cover");
            String colorHex = object.getString("colorHex");

            song = new Song(id, title, artiste, cover, colorHex).setDirectoryWith(context);
        } else if (type.equals("Playlist")) {
            String id = object.getString("id");
            String name = object.getString("name");
            String cover = object.getString("cover");
            String colorHex = object.getString("colorHex");

            playlistInfo = new PlaylistInfo(id, name, cover, new ArrayList<>());
        } else {
            throw new RuntimeException(String.format("Undefined data type: %s", type));
        }
    }

    public Song getSong() {
        return this.song;
    }

    public PlaylistInfo getPlaylistInfo() {
        return this.playlistInfo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SearchResult)) return false;
        SearchResult that = (SearchResult) o;
        return Objects.equals(playlistInfo, that.playlistInfo) &&
            Objects.equals(song, that.song);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playlistInfo, song);
    }
}
