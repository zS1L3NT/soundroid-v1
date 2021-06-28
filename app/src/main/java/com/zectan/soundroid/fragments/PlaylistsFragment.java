package com.zectan.soundroid.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zectan.soundroid.adapters.PlaylistsAdapter;
import com.zectan.soundroid.classes.Fragment;
import com.zectan.soundroid.databinding.FragmentPlaylistsBinding;
import com.zectan.soundroid.models.Info;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PlaylistsFragment extends Fragment<FragmentPlaylistsBinding> {
    private static final String TAG = "(SounDroid) PlayingFragment";
    private static final String USER_ID = "admin";

    private final PlaylistsAdapter.Callback callback = new PlaylistsAdapter.Callback() {
        @Override
        public void onPlaylistClicked(Info info) {
            NavDirections action = PlaylistsFragmentDirections.openPlaylistView();
            NavHostFragment.findNavController(PlaylistsFragment.this).navigate(action);
            playlistViewVM.info.setValue(info);
            playlistViewVM.songs.setValue(new ArrayList<>());
            playlistViewVM.firebase = true;
        }

        @Override
        public boolean onMenuItemClicked(Info info, MenuItem item) {
            return false;
        }
    };

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        B = FragmentPlaylistsBinding.inflate(inflater, container, false);
        super.onCreateView(inflater, container, savedInstanceState);

        // Recycler View
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity);
        PlaylistsAdapter playlistsAdapter = new PlaylistsAdapter(callback);
        B.recyclerView.setAdapter(playlistsAdapter);
        B.recyclerView.setLayoutManager(layoutManager);
        B.recyclerView.setHasFixedSize(true);

        // Observers
        playlistsVM.infos.observe(activity, playlistsAdapter::updateInfos);

        B.swipeRefresh.setOnRefreshListener(this::loadFromFirebase);
        if (playlistsVM.infos.getValue() == null) loadFromFirebase();

        return B.getRoot();
    }

    private void loadFromFirebase() {
        if (playlistsVM.requested) return;
        B.swipeRefresh.setRefreshing(true);
        playlistsVM.requested = true;

        repository
            .playlists(USER_ID)
            .get()
            .addOnSuccessListener(snaps -> {
                List<Info> infos = snaps.toObjects(Info.class);
                playlistsVM.infos.setValue(infos);
                B.swipeRefresh.setRefreshing(false);
                playlistsVM.requested = false;
            })
            .addOnFailureListener(mainVM.error::postValue);
    }
}