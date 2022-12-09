package com.zectan.soundroid.Models;

import android.content.Context;

import com.zectan.soundroid.Utils.ListArrayUtils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Playable {
    private final Playlist playlist;
    private final List<Song> songs;

    /**
     * Object containing Playlist and List of Songs
     * Passed into Playing Service
     *
     * @param playlist Playlist
     * @param songs    Songs
     */
    public Playable(Playlist playlist, List<Song> songs) {
        this.playlist = playlist;
        this.songs = ListArrayUtils.sortSongs(songs, playlist.getOrder());
    }

    /**
     * Create an empty placeholder playable
     *
     * @return Playlist
     */
    public static Playable getEmpty() {
        return new Playable(Playlist.getEmpty(), new ArrayList<>());
    }

    public Playlist getInfo() {
        return playlist;
    }

    public List<Song> getSongs() {
        return songs;
    }

    /**
     * Check if all songs in playable is downloaded
     *
     * @param context Context
     * @return If all songs in playable is downloaded
     */
    public boolean isDownloaded(Context context) {
        return size() > 0 && songs.stream().allMatch(song -> song.isDownloaded(context));
    }

    /**
     * Check if some songs in playable is downloaded
     *
     * @param context Context
     * @return If some songs in playable is downloaded
     */
    public boolean hasDownloaded(Context context) {
        return size() > 0 && songs.stream().anyMatch(song -> song.isDownloaded(context));
    }

    /**
     * Get number of songs
     *
     * @return Number of songs
     */
    public int size() {
        return songs.size();
    }

    @Override
    public @NotNull String toString() {
        return String.format("Playlist { info: %s, size: %s }", playlist, songs.size());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Playable)) return false;
        Playable playable = (Playable) o;
        return Objects.equals(playlist.getId(), playable.playlist.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(playlist);
    }
}
