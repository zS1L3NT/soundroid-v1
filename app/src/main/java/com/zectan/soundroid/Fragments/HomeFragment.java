package com.zectan.soundroid.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.navigation.NavDirections;
import androidx.navigation.fragment.FragmentNavigator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zectan.soundroid.Adapters.HomeAdapter;
import com.zectan.soundroid.Classes.Fragment;
import com.zectan.soundroid.Models.Playlist;
import com.zectan.soundroid.Models.Song;
import com.zectan.soundroid.R;
import com.zectan.soundroid.Utils.Utils;
import com.zectan.soundroid.databinding.FragmentHomeBinding;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment<FragmentHomeBinding> {
    private static final String TAG = "(SounDroid) HomeFragment";

    public HomeFragment() {
        // Required empty public constructor
    }

    private final HomeAdapter.Callback callback = new HomeAdapter.Callback() {
        @Override
        public void onSongClicked(Playlist playlist, String songId) {
            playingVM.startPlaylist(activity, playlist, songId, mainVM.myUser.getValue().getHighStreamQuality());

            if (mainVM.myUser.getValue().getOpenPlayingScreen()) {
                navController.navigate(HomeFragmentDirections.openDownloadedSong());
            }
        }

        @Override
        public boolean onMenuItemClicked(Song song, MenuItem item) {
            return activity.handleMenuItemClick(null, song, item, () -> navController.navigate(HomeFragmentDirections.openEditSong()));
        }
    };

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        B = FragmentHomeBinding.inflate(inflater, container, false);
        super.onCreateView(inflater, container, savedInstanceState);

        // Recycler View
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity);
        HomeAdapter homeAdapter = new HomeAdapter(callback);
        B.recyclerView.setLayoutManager(layoutManager);
        B.recyclerView.setAdapter(homeAdapter);

        // Live Observers
        homeVM.songs.observe(this, homeAdapter::updateSongs);
        homeVM.loading.observe(this, B.swipeRefresh::setRefreshing);
        playingVM.currentSong.observe(this, homeAdapter::updateCurrentSong);

        mainVM.mySongs.observe(this, homeVM.songs::setValue);
        B.searchbar.setOnClickListener(this::onSearchbarClicked);
        B.swipeRefresh.setOnRefreshListener(this::onReload);
        B.settingsImage.setOnClickListener(this::onSettingsClicked);

        return B.getRoot();
    }

    private void onSettingsClicked(View view) {
        NavDirections action = HomeFragmentDirections.openSettings();
        navController.navigate(action);
    }

    private void onSearchbarClicked(View view) {
        FragmentNavigator.Extras extras = Utils.makeExtras(B.searchbar, getString(R.string.TRANSITION_searchbar));
        NavDirections action = HomeFragmentDirections.openSearch();
        navController.navigate(action, extras);
        searchVM.results.postValue(new ArrayList<>());
    }

    private void onReload() {
        List<Song> songs = mainVM.mySongs.getValue();
        homeVM.songs.postValue(songs);
        homeVM.loading.postValue(false);
        B.recyclerView.scrollToPosition(0);
    }
}