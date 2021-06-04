package com.zectan.soundroid.objects;

import android.annotation.SuppressLint;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Playlist {
    private PlaylistInfo info;
    private List<Song> songs;
    
    public Playlist(PlaylistInfo info, List<Song> songs) {
        this.info = info;
        this.songs = Functions.sortSongs(songs, info.getOrder());
    }
    
    public PlaylistInfo getInfo() {
        return info;
    }
    
    public void setInfo(PlaylistInfo info) {
        this.info = info;
    }
    
    public List<Song> getSongs() {
        return songs;
    }
    
    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }
    
    public Song getSong(int position) {
        try {
            return this.songs.get(position);
        } catch (Exception e) {
            return Song.getDefault();
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
