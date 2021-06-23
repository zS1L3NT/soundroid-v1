package com.zectan.soundroid.objects;

import com.zectan.soundroid.anonymous.ListArrayHandler;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Playlist {
    private Info info;
    private List<Song> songs;

    public Playlist(Info info, List<Song> songs) {
        this.info = info;
        this.songs = ListArrayHandler.sortSongs(songs, info.getOrder());
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

    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }

    public void setInfo(Info info) {
        this.info = info;
    }

    public Song getSong(int position) {
        return this.songs.get(position);
    }

    public Song getSong(String id) {
        return songs.stream().filter(song -> song.getId().equals(id)).collect(Collectors.toList()).get(0);
    }

    public int getIndexOfSong(String id) {
        return songs.indexOf(getSong(id));
    }

    public void removeSong(String id) {
        songs = songs.stream().filter(song -> !song.getId().equals(id)).collect(Collectors.toList());
    }

    public int size() {
        return songs.size();
    }

    @Override
    public @NotNull String toString() {
        return String.format("Playlist { info: %s, size: %s }", info, songs.size());
    }
}
