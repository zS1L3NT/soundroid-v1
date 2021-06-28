package com.zectan.soundroid.models;

import android.content.Context;

import com.google.android.exoplayer2.MediaItem;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Song {
    private static final String TAG = "(SounDroid) Song";
    private static final String SongsURL = "http://soundroid.zectan.com/songs";
    private String id;
    private String title;
    private String artiste;
    private String cover;
    private String colorHex;
    private File directory;
    private List<String> playlists;
    private Map<String, Boolean> owners;
    private List<String> queries;

    public Song() {

    }

    public Song(String id, String title, String artiste, String cover, String colorHex) {
        this.id = id;
        this.title = title;
        this.artiste = artiste;
        this.cover = cover;
        this.colorHex = colorHex;
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
            "#7b828b"
        );
    }

    public String getId() {
        return id;
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

    public String getUrl() {
        return String.format("%s/%s.mp3", SongsURL, id);
    }

    public File getDirectory() {
        return this.directory;
    }

    public List<String> getPlaylists() {
        return playlists;
    }

    public Map<String, Boolean> getOwners() {
        return owners;
    }

    public List<String> getQueries() {
        return queries;
    }

    public MediaItem getMediaItem() {
        return new MediaItem.Builder().setUri(getUrl()).setMediaId(id).build();
    }

    public Song setDirectoryWith(Context context) {
        this.directory = new File(context.getFilesDir().getPath(), id + ".mp3");
        return this;
    }

    @Override
    public @NotNull String toString() {
        return String.format(
            "([%s] %s by %s)",
            id,
            title,
            artiste
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Song)) return false;
        Song song = (Song) o;
        return Objects.equals(id, song.id) &&
            Objects.equals(title, song.title) &&
            Objects.equals(artiste, song.artiste) &&
            Objects.equals(cover, song.cover) &&
            Objects.equals(colorHex, song.colorHex) &&
            Objects.equals(directory, song.directory) &&
            Objects.equals(playlists, song.playlists) &&
            Objects.equals(owners, song.owners);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, artiste, cover, colorHex, directory, playlists, owners);
    }
}