package com.zectan.soundroid.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.navigation.NavDirections;
import androidx.navigation.fragment.FragmentNavigator;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zectan.soundroid.R;
import com.zectan.soundroid.adapters.HomeAdapter;
import com.zectan.soundroid.classes.Fragment;
import com.zectan.soundroid.databinding.FragmentHomeBinding;
import com.zectan.soundroid.models.Playlist;
import com.zectan.soundroid.models.Song;
import com.zectan.soundroid.utils.Anonymous;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class HomeFragment extends Fragment<FragmentHomeBinding> {
    private static final String TAG = "(SounDroid) HomeFragment";

    public HomeFragment() {
        // Required empty public constructor
    }

    private final HomeAdapter.Callback callback = new HomeAdapter.Callback() {
        @Override
        public void onSongClicked(ImageView cover, String transitionName, Playlist playlist, String songId) {
            FragmentNavigator.Extras extras = Anonymous.makeExtras(cover, transitionName);
            NavDirections action = HomeFragmentDirections.openDownloadedSong().setTransitionName(transitionName);
            NavHostFragment.findNavController(HomeFragment.this).navigate(action, extras);
            playingVM.startPlaylist(playlist, songId);
        }

        @Override
        public boolean onMenuItemClicked(Song song, MenuItem item) {
            return activity.handleMenuItemClick(null, song, item);
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
        homeVM.playlist.observe(this, homeAdapter::updatePlaylist);
        homeVM.loading.observe(this, B.swipeRefresh::setRefreshing);

        B.swipeRefresh.setOnRefreshListener(() -> homeVM.reload(activity::handleError));
        B.searchbar.setOnClickListener(this::onSearchbarClicked);

        homeVM.reload(activity::handleError);

        return B.getRoot();
    }

    private void onSearchbarClicked(View view) {
        FragmentNavigator.Extras extras = Anonymous.makeExtras(B.searchbar, getString(R.string.TRANSITION_searchbar));
        NavDirections action = HomeFragmentDirections.openSearch();
        NavHostFragment.findNavController(this).navigate(action, extras);
        searchVM.results.postValue(new ArrayList<>());
    }
}