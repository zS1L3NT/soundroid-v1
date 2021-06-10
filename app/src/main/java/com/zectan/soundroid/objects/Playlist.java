package com.zectan.soundroid.objects;

import android.annotation.SuppressLint;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class Playlist {
    private PlaylistInfo info;
    private List<Song> songs;
    
    public Playlist(PlaylistInfo info, List<Song> songs) {
        this.info = info;
        this.songs = Anonymous.sortSongs(songs, info.getOrder());
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
    
    public Song getSong(String id) {
        return this.songs.stream().filter(song -> song.getId().equals(id)).collect(Collectors.toList()).get(0);
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
