package com.zectan.soundroid.DiffCallbacks;

import androidx.recyclerview.widget.DiffUtil;

import com.zectan.soundroid.Models.Song;

import java.util.List;

public class SongsDiffCallback extends DiffUtil.Callback {
    private final List<Song> mOldSongs, mNewSongs;
    private final Song mCurrentSong, mPreviousSong;

    public SongsDiffCallback(List<Song> oldSongs, List<Song> newSongs, Song currentSong, Song previousSong) {
        mOldSongs = oldSongs;
        mNewSongs = newSongs;
        mCurrentSong = currentSong;
        mPreviousSong = previousSong;
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
        return oldSong.equals(newSong) && !oldSong.equals(mCurrentSong) && !oldSong.equals(mPreviousSong);
    }
}