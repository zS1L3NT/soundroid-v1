package com.zectan.soundroid.models;

import android.content.Context;

import com.zectan.soundroid.utils.ListArrayUtils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

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
        return songs.stream().allMatch(song -> song.isDownloaded(context));
    }

    public boolean hasDownloaded(Context context) {
        return songs.stream().anyMatch(song -> song.isDownloaded(context));
    }

    public int size() {
        return songs.size();
    }

    @Override
    public @NotNull String toString() {
        return String.format("Playlist { info: %s, size: %s }", info, songs.size());
    }
}
