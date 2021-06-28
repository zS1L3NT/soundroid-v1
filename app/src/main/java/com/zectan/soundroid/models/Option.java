package com.zectan.soundroid.models;

import android.util.Log;

import com.zectan.soundroid.R;
import com.zectan.soundroid.sockets.DownloadThread;

public class Option {
    private final Callback callback;
    private final int drawable;
    private final String title;
    
    public Option(Callback callback, int drawable, String title) {
        this.callback = callback;
        this.drawable = drawable;
        this.title = title;
    }

    public static Option addToPlaylist() {
        return new Option(() -> {
        }, R.drawable.ic_add_to_playlist, "Add to Playlist");
    }

    public static Option addToQueue() {
        return new Option(() -> {
        }, R.drawable.ic_add_to_queue, "Add to Queue");
    }

    public static Option download(Song song) {
        return new Option(() -> new DownloadThread(song, new DownloadThread.Callback() {
            @Override
            public void onFinish() {
                Log.d("(SounDroid)", "Finished!");
            }

            @Override
            public void onProgress(int progress) {
                Log.d("(SounDroid)", "Progress: " + progress);
            }

            @Override
            public void onError(String message) {
                Log.d("(SounDroid)", "Error: " + message);
            }
        }).start(), R.drawable.ic_download, "Download");
    }

    public Callback getCallback() {
        return callback;
    }

    public int getDrawable() {
        return drawable;
    }
    
    public String getTitle() {
        return title;
    }
    
    public interface Callback {
        void run();
    }
    
}
