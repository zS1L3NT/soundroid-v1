package com.zectan.soundroid.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zectan.soundroid.classes.Fragment;
import com.zectan.soundroid.databinding.FragmentPlaylistEditBinding;

import org.jetbrains.annotations.NotNull;

public class PlaylistEditFragment extends Fragment<FragmentPlaylistEditBinding> {

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        B = FragmentPlaylistEditBinding.inflate(inflater, container, false);
        super.onCreateView(inflater, container, savedInstanceState);

        return B.getRoot();
    }
}