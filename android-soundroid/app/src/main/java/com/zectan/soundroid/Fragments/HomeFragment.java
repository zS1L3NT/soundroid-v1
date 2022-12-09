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
import com.zectan.soundroid.Models.Playable;
import com.zectan.soundroid.Models.Song;
import com.zectan.soundroid.R;
import com.zectan.soundroid.Utils.Utils;
import com.zectan.soundroid.databinding.FragmentHomeBinding;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HomeFragment extends Fragment<FragmentHomeBinding> {
    private static final String TAG = "(SounDroid) HomeFragment";

    private final HomeAdapter.Callback callback = new HomeAdapter.Callback() {
        @Override
        public void onSongClicked(Playable playable, String songId) {
            mActivity.getPlayingService(service -> service.startPlayable(playable, songId, mMainVM.myUser.getValue().getHighStreamQuality()));

            if (mMainVM.myUser.getValue().getOpenPlayingScreen()) {
                mNavController.navigate(HomeFragmentDirections.openPlaying());
            }
        }

        @Override
        public boolean onMenuItemClicked(Song song, MenuItem item) {
            return mActivity.handleMenuItemClick(null, song, item, () -> mNavController.navigate(HomeFragmentDirections.openEditSong()));
        }
    };

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        B = FragmentHomeBinding.inflate(inflater, container, false);
        super.onCreateView(inflater, container, savedInstanceState);

        // Recycler View
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mActivity);
        HomeAdapter homeAdapter = new HomeAdapter(callback);
        B.recyclerView.setLayoutManager(layoutManager);
        B.recyclerView.setAdapter(homeAdapter);

        // Listeners
        mHomeVM.songs.observe(this, homeAdapter::updateSongs);
        mHomeVM.loading.observe(this, B.swipeRefresh::setRefreshing);
        mActivity.getPlayingService(service -> service.currentSong.observe(this, homeAdapter::updateCurrentSong));
        mMainVM.mySongs.observe(this, mHomeVM.songs::setValue);
        B.searchbar.setOnClickListener(this::onSearchbarClicked);
        B.swipeRefresh.setOnRefreshListener(this::onReload);
        B.settingsImage.setOnClickListener(this::onSettingsClicked);

        return B.getRoot();
    }

    private void onSettingsClicked(View view) {
        NavDirections action = HomeFragmentDirections.openSettings();
        mNavController.navigate(action);
    }

    private void onSearchbarClicked(View view) {
        FragmentNavigator.Extras extras = Utils.makeExtras(B.searchbar, getString(R.string.TRANSITION_searchbar));
        NavDirections action = HomeFragmentDirections.openSearch();
        mNavController.navigate(action, extras);
    }

    private void onReload() {
        List<Song> songs = mMainVM.mySongs.getValue();
        mHomeVM.songs.postValue(songs);
        mHomeVM.loading.postValue(false);
        B.recyclerView.scrollToPosition(0);
    }
}