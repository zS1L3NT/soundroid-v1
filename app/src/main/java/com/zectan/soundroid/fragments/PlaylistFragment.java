package com.zectan.soundroid.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zectan.soundroid.MainActivity;
import com.zectan.soundroid.R;
import com.zectan.soundroid.adapters.PlaylistAdapter;
import com.zectan.soundroid.viewmodels.PlayingViewModel;

import java.util.ArrayList;

public class PlaylistFragment extends Fragment {

    public PlaylistFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playlist, container, false);
        MainActivity activity = (MainActivity) getActivity();
        assert activity != null;

        // ViewModels
        PlayingViewModel playingVM = new ViewModelProvider(activity).get(PlayingViewModel.class);

        // Reference views
        RecyclerView recyclerView = view.findViewById(R.id.playlist_recycler_view);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        PlaylistAdapter songAdapter = new PlaylistAdapter(new ArrayList<>());
        recyclerView.setAdapter(songAdapter);

        return view;
    }
}