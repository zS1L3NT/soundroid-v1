package com.zectan.soundroid.Adapters.DiffCallbacks;

import androidx.recyclerview.widget.DiffUtil;

import com.zectan.soundroid.Models.Info;

import java.util.List;

public class InfoDiffCallback extends DiffUtil.Callback {
    private final List<Info> mOldInfos, mNewInfos;

    public InfoDiffCallback(List<Info> oldInfos, List<Info> newInfos) {
        mOldInfos = oldInfos;
        mNewInfos = newInfos;
    }

    @Override
    public int getOldListSize() {
        return mOldInfos.size();
    }

    @Override
    public int getNewListSize() {
        return mNewInfos.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        Info oldInfo = mOldInfos.get(oldItemPosition);
        Info newInfo = mNewInfos.get(newItemPosition);
        return oldInfo.getId().equals(newInfo.getId());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        Info oldInfo = mOldInfos.get(oldItemPosition);
        Info newInfo = mNewInfos.get(newItemPosition);
        return oldInfo.equals(newInfo);
    }
}