package com.zectan.soundroid.objects;

import android.util.Log;

import com.zectan.soundroid.tasks.SongLinkFetchThread;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

public class Song {
    private static final String TAG = "(SounDroid) Song";
    private String folder;
    private String id;
    private String title;
    private String artiste;
    private String cover;
    private String colorHex;
    private List<String> playlists;
    private List<String> users;
    
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
    
    public static Song getDefault() {
        return new Song(
            "",
            "",
            "-",
            "-",
            "-",
            "#7b828b"
        );
    }
    
    public List<String> getPlaylists() {
        return playlists;
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