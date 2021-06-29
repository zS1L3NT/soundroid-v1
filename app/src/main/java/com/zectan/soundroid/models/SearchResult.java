package com.zectan.soundroid.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class SearchResult {
    private final String mLocation;
    private Info mInfo;
    private Song mSong;

    public SearchResult(JSONObject object) throws JSONException {
        String type = object.getString("type");
        mLocation = "Server";

        if (type.equals("Song")) {
            mSong = Song.fromJSON(object);
        } else if (type.equals("Playlist")) {
            mInfo = Info.fromJSON(object);
        } else {
            throw new RuntimeException(String.format("Undefined data type: %s", type));
        }
    }

    public SearchResult(Song song) {
        mLocation = "Local";
        mSong = song;
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
        if (mSong != null) return mSong.getSongId();
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
