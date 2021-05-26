package com.zectan.soundroid.objects;

import android.annotation.SuppressLint;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Playlist {
    private final PlaylistInfo info;
    private final List<Song> songs;

    public Playlist(PlaylistInfo info, List<Song> songs) {
        this.info = info;
        this.songs = Functions.sortSongs(songs, info.getOrder());
    }

    public PlaylistInfo getInfo() {
        return info;
    }

    public List<Song> getSongs() {
        return songs;
    }

    public Song getSong(int position) {
        try {
            return this.songs.get(position);
        } catch (Exception e) {
            return null;
        }
    }

    public int size() {
        return songs.size();
    }

    @SuppressLint("DefaultLocale")
    @Override
    public @NotNull String toString() {
        return String.format("Playlist { info: %s, size: %d }", info, songs.size());
    }
}
