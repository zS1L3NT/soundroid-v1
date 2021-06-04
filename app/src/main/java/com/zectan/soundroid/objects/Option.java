package com.zectan.soundroid.objects;

import com.zectan.soundroid.R;

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
    
    public static Option download() {
        return new Option(() -> {
        }, R.drawable.ic_download, "Download");
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
