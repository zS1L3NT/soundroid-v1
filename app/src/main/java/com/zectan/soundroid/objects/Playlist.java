package com.zectan.soundroid.objects;

import android.annotation.SuppressLint;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Playlist {
    private final String id, name;
    private final List<Song> songs;

    public Playlist(String id, String name, List<Song> songs) {
        this.id = id;
        this.name = name;
        this.songs = songs;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
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
        return String.format("Playlist { id: '%s', size: %d }", id, songs.size());
    }
}
