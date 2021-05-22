package com.zectan.soundroid.objects;

import android.annotation.SuppressLint;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class Playlist {
    private final PlaylistInfo info;
    private final List<Song> songs;

    public Playlist(PlaylistInfo info, List<Song> songs) {
        this.info = info;
        this.songs = songs;
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

    public Song getSong(String id) {
        return this
                .songs
                .stream()
                .filter(song -> song.getId().equals(id))
                .collect(Collectors.toList())
                .get(0);
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
