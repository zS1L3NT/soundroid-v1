package com.zectan.soundroid.objects;

import android.util.Log;

import com.zectan.soundroid.tasks.SongLinkFetchThread;

import org.jetbrains.annotations.NotNull;

import java.io.File;

public class Song {
    private static final String TAG = "(SounDroid) Song";
    private String folder;
    private String id;
    private String title;
    private String artiste;
    private String cover;
    private String colorHex;

    public Song() {

    }

    public Song(String folder, String id, String title, String artiste, String cover, String colorHex) {
        this.folder = folder;
        this.id = id;
        this.title = title;
        this.artiste = artiste;
        this.cover = cover;
        this.colorHex = colorHex;
    }

    public String getFolder() {
        return folder;
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

    public void getFileLocation(SongLinkFetchThread.Callback callback) {
        File file = new File(folder, id + ".mp3");
        if (file.exists()) {
            Log.d(TAG, "READING_SONG");
            callback.onFinish(file.getPath());
        } else {
            Log.d(TAG, "STREAMING_SONG");
            new SongLinkFetchThread(id, callback).start();
        }
    
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