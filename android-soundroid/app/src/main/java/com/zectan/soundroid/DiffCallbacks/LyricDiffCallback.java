package com.zectan.soundroid.DiffCallbacks;

import androidx.recyclerview.widget.DiffUtil;

import java.util.List;

public class LyricDiffCallback extends DiffUtil.Callback {
    private final List<String> mOldLyrics, mNewLyrics;

    public LyricDiffCallback(List<String> mOldLyrics, List<String> mNewLyrics) {
        this.mOldLyrics = mOldLyrics;
        this.mNewLyrics = mNewLyrics;
    }

    @Override
    public int getOldListSize() {
        return mOldLyrics.size();
    }

    @Override
    public int getNewListSize() {
        return mNewLyrics.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return mOldLyrics.get(oldItemPosition).equals(mNewLyrics.get(newItemPosition));
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return mOldLyrics.get(oldItemPosition).equals(mNewLyrics.get(newItemPosition));
    }
}
