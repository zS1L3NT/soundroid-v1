package com.zectan.soundroid.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zectan.soundroid.MainActivity;
import com.zectan.soundroid.R;
import com.zectan.soundroid.adapters.MusicAdapter;
import com.zectan.soundroid.viewmodels.PlayingViewModel;

public class MusicFragment extends Fragment {

    public MusicFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music, container, false);
        MainActivity activity = (MainActivity) getActivity();
        assert activity != null;

        // ViewModels
        PlayingViewModel playingVM = new ViewModelProvider(activity).get(PlayingViewModel.class);

        // Reference views
        RecyclerView recyclerView = view.findViewById(R.id.song_recycler_view);

        // Click listeners
        view.findViewById(R.id.header_search).setOnClickListener(__ -> {
            NavDirections action = MusicFragmentDirections.openSearch();
            NavHostFragment.findNavController(this).navigate(action);
        });

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        MusicAdapter songAdapter = new MusicAdapter(activity.getStaticPlaylist(), (song, position) -> {
            NavHostFragment.findNavController(this).navigate(MusicFragmentDirections.openDownloadedSong());
            playingVM.selectSong(song, position);
        });
        recyclerView.setAdapter(songAdapter);

        return view;
    }

}