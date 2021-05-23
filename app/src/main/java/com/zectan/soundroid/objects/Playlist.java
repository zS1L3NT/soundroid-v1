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
        this.songs = songs
                .stream()
                .sorted((song1, song2) -> {
                    List<String> order = info.getOrder();
                    return order.indexOf(song1.getId()) - order.indexOf(song2.getId());
                })
                .collect(Collectors.toList());
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
