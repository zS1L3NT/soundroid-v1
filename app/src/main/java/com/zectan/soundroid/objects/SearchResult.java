package com.zectan.soundroid.objects;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

public class SearchResult {
    private final String mLocation;
    private Info mInfo;
    private Song mSong;

    public SearchResult(JSONObject object, Context context) throws JSONException {
        String type = object.getString("type");
        mLocation = "Server";

        if (type.equals("Song")) {
            String id = object.getString("id");
            String title = object.getString("title");
            String artiste = object.getString("artiste");
            String cover = object.getString("cover");
            String colorHex = object.getString("colorHex");

            mSong = new Song(id, title, artiste, cover, colorHex).setDirectoryWith(context);
        } else if (type.equals("Playlist")) {
            String id = object.getString("id");
            String name = object.getString("name");
            String cover = object.getString("cover");
            String colorHex = object.getString("colorHex");

            mInfo = new Info(id, name, cover, colorHex, new ArrayList<>());
        } else {
            throw new RuntimeException(String.format("Undefined data type: %s", type));
        }
    }

    public SearchResult(Song song, Context context) {
        mLocation = "Local";
        mSong = song.setDirectoryWith(context);
    }

    public SearchResult(Info info) {
        mLocation = "Local";
        mInfo = info;
    }

    public Song getSong() {
        return mSong;
    }

    public Info getPlaylistInfo() {
        return mInfo;
    }

    public String getLocation() {
        return mLocation;
    }

    public String getId() {
        if (mSong != null) return mSong.getId();
        if (mInfo != null) return mInfo.getId();
        throw new RuntimeException("Undefined data type");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SearchResult)) return false;
        SearchResult that = (SearchResult) o;
        return Objects.equals(mInfo, that.mInfo) &&
            Objects.equals(mSong, that.mSong);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mInfo, mSong);
    }
}
