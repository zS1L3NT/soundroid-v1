package com.zectan.soundroid.Adapters.DiffCallbacks;

import androidx.recyclerview.widget.DiffUtil;

import com.zectan.soundroid.Models.Info;

import java.util.List;

public class InfoDiffCallback extends DiffUtil.Callback {

    private final List<Info> oldInfos, newInfos;

    public InfoDiffCallback(List<Info> oldInfos, List<Info> newInfos) {
        this.oldInfos = oldInfos;
        this.newInfos = newInfos;
    }

    @Override
    public int getOldListSize() {
        return oldInfos.size();
    }

    @Override
    public int getNewListSize() {
        return newInfos.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        Info oldInfo = oldInfos.get(oldItemPosition);
        Info newInfo = newInfos.get(newItemPosition);
        return oldInfo.getId().equals(newInfo.getId());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        Info oldInfo = oldInfos.get(oldItemPosition);
        Info newInfo = newInfos.get(newItemPosition);
        return oldInfo.equals(newInfo);
    }
}