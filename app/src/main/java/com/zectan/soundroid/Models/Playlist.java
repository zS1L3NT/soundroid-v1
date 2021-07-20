package com.zectan.soundroid.Models;

import android.content.Context;

import com.zectan.soundroid.Utils.ListArrayUtils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Playlist {
    private final Info info;
    private final List<Song> songs;

    public Playlist(Info info, List<Song> songs) {
        this.info = info;
        this.songs = ListArrayUtils.sortSongs(songs, info.getOrder());
    }

    /**
     * Create an empty placeholder playlist
     *
     * @return Playlist
     */
    public static Playlist getEmpty() {
        return new Playlist(Info.getEmpty(), new ArrayList<>());
    }

    public Info getInfo() {
        return info;
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
        return String.format("Playlist { info: %s, size: %s }", info, songs.size());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Playlist)) return false;
        Playlist playlist = (Playlist) o;
        return Objects.equals(info.getId(), playlist.info.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(info);
    }
}
