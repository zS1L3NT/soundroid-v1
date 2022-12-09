package com.zectan.soundroid.Models;

import android.content.Context;
import android.net.Uri;

import com.google.android.exoplayer2.MediaItem;
import com.zectan.soundroid.Env;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class Song {
    private static final String TAG = "(SounDroid) Song";
    private static final String SongsURL = Env.API_URL + "/play/%s/%s.mp3";
    private String songId;
    private String title;
    private String artiste;
    private String cover;
    private String colorHex;
    private String playlistId;
    private String userId;

    public Song() {
    }

    /**
     * Object containing data about a song
     *
     * @param songId     Song ID
     * @param title      Title
     * @param artiste    Artiste
     * @param cover      Cover
     * @param colorHex   Color Hex
     * @param playlistId Playlist ID
     * @param userId     User ID
     */
    public Song(
        String songId,
        String title,
        String artiste,
        String cover,
        String colorHex,
        String playlistId,
        String userId
    ) {
        this.songId = songId;
        this.title = title;
        this.artiste = artiste;
        this.cover = cover;
        this.colorHex = colorHex;
        this.playlistId = playlistId;
        this.userId = userId;
    }

    /**
     * Create an empty object for a default song
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
            ""
        );
    }

    /**
     * Create a Song Object from a JSON object
     *
     * @param object JSON Object
     * @return Song Object
     * @throws JSONException Error if object is unparsable
     */
    public static Song fromJSON(JSONObject object) throws JSONException {
        String songId = object.getString("songId");
        String title = object.getString("title");
        String artiste = object.getString("artiste");
        String cover = object.getString("cover");
        String colorHex = object.getString("colorHex");
        String playlistId = object.getString("playlistId");
        String userId = object.getString("userId");
        return new Song(songId, title, artiste, cover, colorHex, playlistId, userId);
    }

    /**
     * Create a JSON Object from a Song Object
     *
     * @return JSON Object
     */
    public JSONObject toJSON() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("songId", songId);
        object.put("title", title);
        object.put("artiste", artiste);
        object.put("cover", cover);
        object.put("colorHex", colorHex);
        object.put("playlistId", playlistId);
        object.put("userId", userId);
        return object;
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

    public void setCover(String cover) {
        this.cover = cover;
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

    /**
     * Get the media item of a song so it can be parsed by ExoPlayer
     *
     * @param context           Context
     * @param highStreamQuality Quality of stream
     * @return Media item
     */
    public MediaItem getMediaItem(Context context, boolean highStreamQuality) {
        Uri uri;

        if (isDownloaded(context)) {
            uri = Uri.fromFile(getFileDir(context));
        } else {
            uri = Uri.parse(String.format(SongsURL, highStreamQuality ? "highest" : "lowest", songId));
        }

        return new MediaItem.Builder().setUri(uri).setMediaId(songId).build();
    }

    /**
     * Check if a song is downloaded onto the local file system
     *
     * @param context Context
     * @return If a song is downloaded
     */
    public boolean isDownloaded(Context context) {
        return getFileDir(context).exists();
    }

    /**
     * Delete both the in-progress file and fully downloaded file form the system
     *
     * @param context Context
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void deleteLocally(Context context) {
        getFileDir(context).delete();
        getPartialFileDir(context).delete();
    }

    /**
     * Delete a song if it is not in any other playlists
     *
     * @param context  Context
     * @param allSongs All songs
     */
    public void deleteIfNotUsed(Context context, List<Song> allSongs) {
        List<String> ids = allSongs
            .stream()
            .map(Song::getSongId)
            .filter(songId -> songId.equals(getSongId()))
            .collect(Collectors.toList());
        if (ids.size() == 1) {
            deleteLocally(context);
        }
    }

    /**
     * Get the directory of the fully downloaded file
     *
     * @param context Context
     * @return File location
     */
    private File getFileDir(Context context) {
        return new File(context.getFilesDir(), String.format("/%s.mp3", songId));
    }

    /**
     * Get the directory of the partially downloaded file
     *
     * @param context Context
     * @return File location
     */
    private File getPartialFileDir(Context context) {
        return new File(context.getFilesDir(), String.format("/__%s.mp3", songId));
    }

    /**
     * Create a Map Object from a Song Object
     *
     * @return Map Object
     */
    public Map<String, Object> toMap() {
        Map<String, Object> object = new HashMap<>();
        object.put("songId", songId);
        object.put("title", title);
        object.put("artiste", artiste);
        object.put("cover", cover);
        object.put("colorHex", colorHex);
        object.put("playlistId", playlistId);
        object.put("userId", userId);
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