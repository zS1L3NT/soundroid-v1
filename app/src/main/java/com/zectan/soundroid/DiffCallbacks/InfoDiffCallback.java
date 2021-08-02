package com.zectan.soundroid.DiffCallbacks;

import androidx.recyclerview.widget.DiffUtil;

import com.zectan.soundroid.Models.Playlist;

import java.util.List;

public class InfoDiffCallback extends DiffUtil.Callback {
    private final List<Playlist> mOldPlaylists, mNewPlaylists;

    public InfoDiffCallback(List<Playlist> oldPlaylists, List<Playlist> newPlaylists) {
        mOldPlaylists = oldPlaylists;
        mNewPlaylists = newPlaylists;
    }

    @Override
    public int getOldListSize() {
        return mOldPlaylists.size();
    }

    @Override
    public int getNewListSize() {
        return mNewPlaylists.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        Playlist oldPlaylist = mOldPlaylists.get(oldItemPosition);
        Playlist newPlaylist = mNewPlaylists.get(newItemPosition);
        return oldPlaylist.getId().equals(newPlaylist.getId());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        Playlist oldPlaylist = mOldPlaylists.get(oldItemPosition);
        Playlist newPlaylist = mNewPlaylists.get(newItemPosition);
        return oldPlaylist.equals(newPlaylist);
    }
}