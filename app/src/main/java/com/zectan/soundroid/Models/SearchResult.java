package com.zectan.soundroid.Models;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class SearchResult {
    private final String mLocation;
    private Playlist mPlaylist;
    private Song mSong;

    /**
     * Object containing data about a search result
     *
     * @param object JSON Object to parse
     * @throws JSONException Error if cannot parse JSON from server
     */
    public SearchResult(JSONObject object) throws JSONException {
        String type = object.getString("type");
        mLocation = "Server";

        if (type.equals("Song")) {
            mSong = Song.fromJSON(object);
        } else if (type.equals("Playlist")) {
            mPlaylist = Playlist.fromJSON(object);
        } else {
            throw new RuntimeException(String.format("Undefined data type: %s", type));
        }
    }

    /**
     * Create a SearchResult containing data about a song
     *
     * @param song Song
     */
    public SearchResult(Song song) {
        mLocation = "Local";
        mSong = song;
    }

    /**
     * Create a SearchResult containing data about a playlist
     *
     * @param playlist Playlist
     */
    public SearchResult(Playlist playlist) {
        mLocation = "Local";
        mPlaylist = playlist;
    }

    public Song getSong() {
        return mSong;
    }

    public Playlist getPlaylistInfo() {
        return mPlaylist;
    }

    public String getLocation() {
        return mLocation;
    }

    /**
     * Fetch the ID of the Song or Playlist
     *
     * @return ID
     */
    public String getId() {
        if (mSong != null) return mSong.getSongId();
        if (mPlaylist != null) return mPlaylist.getId();
        throw new RuntimeException("Undefined data type");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SearchResult)) return false;
        SearchResult that = (SearchResult) o;
        return Objects.equals(mPlaylist, that.mPlaylist) &&
            Objects.equals(mSong, that.mSong);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mPlaylist, mSong);
    }
}
