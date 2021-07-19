package com.zectan.soundroid.adapters.DiffCallbacks;

import androidx.recyclerview.widget.DiffUtil;

import com.zectan.soundroid.models.SearchResult;
import com.zectan.soundroid.models.Song;

import java.util.List;

public class SearchResultDiffCallback extends DiffUtil.Callback {

    private final List<SearchResult> oldResults, newResults;
    private final Song currentSong, previousSong;

    public SearchResultDiffCallback(List<SearchResult> oldResults, List<SearchResult> newResults, Song currentSong, Song previousSong) {
        this.oldResults = oldResults;
        this.newResults = newResults;
        this.currentSong = currentSong;
        this.previousSong = previousSong;
    }

    @Override
    public int getOldListSize() {
        return oldResults.size();
    }

    @Override
    public int getNewListSize() {
        return newResults.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        SearchResult oldResult = oldResults.get(oldItemPosition);
        SearchResult newResult = newResults.get(newItemPosition);
        return oldResult.getId().equals(newResult.getId());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        SearchResult oldResult = oldResults.get(oldItemPosition);
        SearchResult newResult = newResults.get(newItemPosition);
        return oldResult.equals(newResult)
            && (currentSong == null || !currentSong.getSongId().equals(oldResult.getId()))
            && (previousSong == null || !previousSong.getSongId().equals(oldResult.getId()));
    }
}