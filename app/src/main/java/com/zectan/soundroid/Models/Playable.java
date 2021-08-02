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

    public boolean isDownloaded(Context context) {
        return size() > 0 && songs.stream().allMatch(song -> song.isDownloaded(context));
    }

    public boolean hasDownloaded(Context context) {
        return size() > 0 && songs.stream().anyMatch(song -> song.isDownloaded(context));
    }

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
