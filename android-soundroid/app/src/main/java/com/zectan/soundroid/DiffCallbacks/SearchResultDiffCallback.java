package com.zectan.soundroid.DiffCallbacks;

import androidx.recyclerview.widget.DiffUtil;

import com.zectan.soundroid.Models.SearchResult;
import com.zectan.soundroid.Models.Song;

import java.util.List;

public class SearchResultDiffCallback extends DiffUtil.Callback {
    private final List<SearchResult> mOldResults, mNewResults;
    private final Song mCurrentSong, mPreviousSong;

    public SearchResultDiffCallback(List<SearchResult> oldResults, List<SearchResult> newResults, Song currentSong, Song previousSong) {
        mOldResults = oldResults;
        mNewResults = newResults;
        mCurrentSong = currentSong;
        mPreviousSong = previousSong;
    }

    @Override
    public int getOldListSize() {
        return mOldResults.size();
    }

    @Override
    public int getNewListSize() {
        return mNewResults.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        SearchResult oldResult = mOldResults.get(oldItemPosition);
        SearchResult newResult = mNewResults.get(newItemPosition);
        return oldResult.getId().equals(newResult.getId());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        SearchResult oldResult = mOldResults.get(oldItemPosition);
        SearchResult newResult = mNewResults.get(newItemPosition);
        return oldResult.equals(newResult)
            && (mCurrentSong == null || !mCurrentSong.getSongId().equals(oldResult.getId()))
            && (mPreviousSong == null || !mPreviousSong.getSongId().equals(oldResult.getId()));
    }
}