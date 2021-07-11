package com.zectan.soundroid.models;

import android.content.Context;
import android.net.Uri;

import com.google.android.exoplayer2.MediaItem;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Song {
    private static final String TAG = "(SounDroid) Song";
    private static final String SongsURL = "http://soundroid.zectan.com/song/%s/%s.mp3";
    private String songId;
    private String title;
    private String artiste;
    private String cover;
    private String colorHex;
    private String playlistId;
    private String userId;
    private List<String> queries;

    public Song() {

    }

    public Song(
        String songId,
        String title,
        String artiste,
        String cover,
        String colorHex,
        String playlistId,
        String userId,
        List<String> queries
    ) {
        this.songId = songId;
        this.title = title;
        this.artiste = artiste;
        this.cover = cover;
        this.colorHex = colorHex;
        this.playlistId = playlistId;
        this.userId = userId;
        this.queries = queries;
    }

    /**
     * Creates an empty placeholder Song
     *
     * @return Song
     */
    public static Song getEmpty() {
        return new Song(
            "",
            "-",
            "-",
            "-",
            "#7b828b",
            "",
            "",
            new ArrayList<>()
        );
    }

    public static Song fromJSON(JSONObject object) throws JSONException {
        String songId = object.getString("songId");
        String title = object.getString("title");
        String artiste = object.getString("artiste");
        String cover = object.getString("cover");
        String colorHex = object.getString("colorHex");
        String playlistId = object.getString("playlistId");
        String userId = object.getString("userId");
        JSONArray queriesArray = object.getJSONArray("queries");
        List<String> queries = new ArrayList<>();
        for (int i = 0; i < queriesArray.length(); i++)
            queries.add(queriesArray.getString(i));
        return new Song(songId, title, artiste, cover, colorHex, playlistId, userId, queries);
    }

    public String getSongId() {
        return songId;
    }

    public String getTitle() {
        return title;
    }

    public String getArtiste() {
        return artiste;
    }

    public String getCover() {
        return cover;
    }

    public String getColorHex() {
        return colorHex;
    }

    public String getPlaylistId() {
        return this.playlistId;
    }

    public void setPlaylistId(String playlistId) {
        this.playlistId = playlistId;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<String> getQueries() {
        return this.queries;
    }

    public MediaItem getMediaItem(Context context, boolean highQuality) {
        Uri uri;

        if (isDownloaded(context)) {
            uri = Uri.fromFile(getFileDir(context));
        } else {
            uri = Uri.parse(String.format(SongsURL, highQuality ? "highest" : "lowest", songId));
        }

        return new MediaItem.Builder().setUri(uri).setMediaId(songId).build();
    }

    public boolean isDownloaded(Context context) {
        return getFileDir(context).exists();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void deleteLocally(Context context) {
        getFileDir(context).delete();
    }

    private File getFileDir(Context context) {
        return new File(context.getFilesDir(), String.format("/%s%s.mp3", playlistId, songId));
    }

    public Map<String, Object> toMap() {
        Map<String, Object> object = new HashMap<>();
        object.put("songId", songId);
        object.put("title", title);
        object.put("artiste", artiste);
        object.put("cover", cover);
        object.put("colorHex", colorHex);
        object.put("playlistId", playlistId);
        object.put("userId", userId);
        object.put("queries", queries);
        return object;
    }

    @Override
    public @NotNull String toString() {
        return String.format(
            "([%s] %s by %s)",
            songId,
            title,
            artiste
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Song)) return false;
        Song song = (Song) o;
        return Objects.equals(songId, song.songId) &&
            Objects.equals(title, song.title) &&
            Objects.equals(artiste, song.artiste) &&
            Objects.equals(cover, song.cover) &&
            Objects.equals(colorHex, song.colorHex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(songId, title, artiste, cover, colorHex);
    }
}