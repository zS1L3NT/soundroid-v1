package com.zectan.soundroid.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.zectan.soundroid.MainActivity;
import com.zectan.soundroid.databinding.FragmentSettingsBinding;

import org.jetbrains.annotations.NotNull;

public class SettingsFragment extends Fragment {
    private FragmentSettingsBinding B;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        B = FragmentSettingsBinding.inflate(inflater, container, false);
        MainActivity activity = (MainActivity) getActivity();
        assert activity != null;

        // TODO Profile and Logout
        // TODO Adjust fade time
        // TODO Download quality
        // TODO Stream quality

        return B.getRoot();
    }
}