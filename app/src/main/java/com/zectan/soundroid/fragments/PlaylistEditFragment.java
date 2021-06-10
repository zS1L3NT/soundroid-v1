package com.zectan.soundroid.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.zectan.soundroid.MainActivity;
import com.zectan.soundroid.databinding.FragmentPlaylistEditBinding;

import org.jetbrains.annotations.NotNull;

public class PlaylistEditFragment extends Fragment {
    private FragmentPlaylistEditBinding B;

    public PlaylistEditFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        B = FragmentPlaylistEditBinding.inflate(inflater, container, false);
        MainActivity activity = (MainActivity) getActivity();
        assert activity != null;

        return B.getRoot();
    }
}