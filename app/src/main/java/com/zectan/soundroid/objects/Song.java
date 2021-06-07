package com.zectan.soundroid.objects;

import android.content.Context;
import android.util.Log;

import com.zectan.soundroid.sockets.ConvertSongSocket;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

public class Song {
    private static final String TAG = "(SounDroid) Song";
    private String id;
    private String title;
    private String artiste;
    private String cover;
    private String colorHex;
    private File directory;
    private List<String> playlists;
    private List<String> users;

    public Song() {

    }

    public Song(String id, String title, String artiste, String cover, String colorHex) {
        this.id = id;
        this.title = title;
        this.artiste = artiste;
        this.cover = cover;
        this.colorHex = colorHex;
    }

    public static Song getDefault() {
        return new Song(
            "",
            "-",
            "-",
            "-",
            "#7b828b"
        );
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtiste() {
        return artiste;
    }

    public String getCover() {
        return cover;
    }

    public String getColorHex() {
        return colorHex;
    }

    public File getDirectory() {
        return this.directory;
    }

    public Song setDirectoryWith(Context context) {
        this.directory = new File(context.getFilesDir().getPath(), id + ".mp3");
        return this;
    }

    public List<String> getPlaylists() {
        return playlists;
    }

    public void getFileLocation(ConvertSongSocket.Callback callback) {
        File file = getDirectory();
        if (file.exists()) {
            Log.d(TAG, "READING_SONG");
            callback.onFinish(file.getPath());
        } else {
            Log.d(TAG, "STREAMING_SONG");
            new ConvertSongSocket(id, callback);
        }

    }

    public List<String> getUsers() {
        return users;
    }

    @Override
    public @NotNull String toString() {
        return String.format(
            "Song { id: '%s', title: '%s', artiste: '%s', cover: '%s', colorHex: '%s' }",
            id,
            title,
            artiste,
            cover,
            colorHex
        );
    }
}