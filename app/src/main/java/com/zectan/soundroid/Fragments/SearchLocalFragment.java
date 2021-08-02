package com.zectan.soundroid.Fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.zectan.soundroid.Adapters.SearchAdapter;
import com.zectan.soundroid.Classes.Fragment;
import com.zectan.soundroid.Models.Playable;
import com.zectan.soundroid.Models.Playlist;
import com.zectan.soundroid.Models.SearchResult;
import com.zectan.soundroid.Models.Song;
import com.zectan.soundroid.R;
import com.zectan.soundroid.databinding.FragmentSearchLocalBinding;

import java.util.Collections;
import java.util.List;

public class SearchLocalFragment extends Fragment<FragmentSearchLocalBinding> {
    private final SearchAdapter.Callback callback = new SearchAdapter.Callback() {
        @Override
        public void onSongClicked(Song song) {
            Playlist playlist = new Playlist(song.getSongId(), "Search Result", Collections.singletonList(song.getSongId()));
            Playable playable = new Playable(playlist, Collections.singletonList(song));
            mActivity.getPlayingService(service -> service.startPlayable(playable, song.getSongId(), mMainVM.myUser.getValue().getHighStreamQuality()));

            if (mMainVM.myUser.getValue().getOpenPlayingScreen()) {
                mNavController.navigate(SearchFragmentDirections.openPlaying());
            }
        }

        @Override
        public void onPlaylistClicked(Playlist playlist) {
            mPlaylistViewVM.playlistId.setValue(playlist.getId());
            mPlaylistViewVM.playlist.postValue(playlist);

            mNavController.navigate(SearchFragmentDirections.openPlaylistView());
        }

        @Override
        public boolean onMenuItemClicked(SearchResult result, MenuItem item) {
            return mActivity.handleMenuItemClick(result.getPlaylistInfo(), result.getSong(), item);
        }
    };

    private SearchAdapter mSearchAdapter;

    public SearchLocalFragment() {
        super(FLAG_IGNORE_NAVIGATOR);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        B = FragmentSearchLocalBinding.inflate(inflater, container, false);
        super.onCreateView(inflater, container, savedInstanceState);

        // Recycler View
        LinearLayoutManager layoutManager = new LinearLayoutManager(mActivity);
        mSearchAdapter = new SearchAdapter(callback);
        B.recyclerView.setLayoutManager(layoutManager);
        B.recyclerView.setAdapter(mSearchAdapter);

        // Observers
        mSearchVM.localResults.observe(this, this::onLocalResultsChange);
        mSearchVM.query.observe(this, query -> mSearchVM.searchLocal(mMainVM.myPlaylists.getValue(), mMainVM.mySongs.getValue()));
        mActivity.getPlayingService(service -> service.currentSong.observe(this, mSearchAdapter::updateCurrentSong));

        return B.getRoot();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void updateVisuals(List<SearchResult> results) {
        if (mSearchVM.query.getValue().equals("")) {
            B.responseImage.setImageDrawable(mActivity.getDrawable(R.drawable.ic_search));
            B.responseHeaderText.setText(R.string.search);
            B.responseMessageText.setText(R.string.searchbar_placeholder);
        } else {
            B.responseImage.setImageDrawable(mActivity.getDrawable(R.drawable.ic_search_no_results));
            B.responseHeaderText.setText(R.string.no_results);
            B.responseMessageText.setText(R.string.no_song_match_found);
        }

        if (results.size() == 0) {
            B.responseLayout.animate().setDuration(250).alpha(1).start();
        } else {
            B.responseLayout.animate().setDuration(250).alpha(0).start();
        }
    }

    private void onLocalResultsChange(List<SearchResult> results) {
        mSearchAdapter.updateResults(results);
        updateVisuals(results);
    }
}