package com.zectan.soundroid.Adapters.DiffCallbacks;

import androidx.recyclerview.widget.DiffUtil;

import com.zectan.soundroid.Models.Song;

import java.util.List;

public class SongsReorderDiffCallback extends DiffUtil.Callback {
    private final List<Song> mOldSongs, mNewSongs;

    public SongsReorderDiffCallback(List<Song> oldSongs, List<Song> newSongs) {
        mOldSongs = oldSongs;
        mNewSongs = newSongs;
    }

    @Override
    public int getOldListSize() {
        return mOldSongs.size();
    }

    @Override
    public int getNewListSize() {
        return mNewSongs.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        Song oldSong = mOldSongs.get(oldItemPosition);
        Song newSong = mNewSongs.get(newItemPosition);
        return oldSong.getSongId().equals(newSong.getSongId());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        Song oldSong = mOldSongs.get(oldItemPosition);
        Song newSong = mNewSongs.get(newItemPosition);
        return oldSong.equals(newSong);
    }
}