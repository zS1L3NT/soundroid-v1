package com.zectan.soundroid.Fragments;

import android.annotation.SuppressLint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.zectan.soundroid.Adapters.SearchAdapter;
import com.zectan.soundroid.Classes.Fragment;
import com.zectan.soundroid.Models.Playable;
import com.zectan.soundroid.Models.Playlist;
import com.zectan.soundroid.Models.SearchResult;
import com.zectan.soundroid.Models.Song;
import com.zectan.soundroid.R;
import com.zectan.soundroid.databinding.FragmentSearchServerBinding;

import java.util.Collections;
import java.util.List;

public class SearchServerFragment extends Fragment<FragmentSearchServerBinding> {
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

    public SearchServerFragment() {
        super(FLAG_HIDE_NAVIGATOR);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        B = FragmentSearchServerBinding.inflate(inflater, container, false);
        super.onCreateView(inflater, container, savedInstanceState);

        // Recycler View
        LinearLayoutManager layoutManager = new LinearLayoutManager(mActivity);
        mSearchAdapter = new SearchAdapter(callback);
        B.recyclerView.setLayoutManager(layoutManager);
        B.recyclerView.setAdapter(mSearchAdapter);

        // Observers
        mSearchVM.serverResults.observe(this, this::onServerResultsChange);
        mSearchVM.message.observe(this, this::onMessageChange);
        mSearchVM.query.observe(this, query -> mSearchVM.searchServer());
        mSearchVM.loading.observe(this, this::onLoadingChange);
        mActivity.getPlayingService(service -> service.currentSong.observe(this, mSearchAdapter::updateCurrentSong));

        return B.getRoot();
    }

    private boolean isOffline() {
        ConnectivityManager cm = mActivity.getSystemService(ConnectivityManager.class);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo == null) return true;
        return !networkInfo.isConnected();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void updateVisuals(List<SearchResult> results, boolean loading) {
        if (mSearchVM.query.getValue().equals("")) {
            B.responseImage.setImageDrawable(mActivity.getDrawable(R.drawable.ic_search));
            B.responseHeaderText.setText(R.string.search);
            B.responseMessageText.setText(R.string.searchbar_placeholder);
        } else {
            B.responseImage.setImageDrawable(mActivity.getDrawable(R.drawable.ic_search_no_results));
            B.responseHeaderText.setText(R.string.no_results);
            B.responseMessageText.setText(
                isOffline()
                    ? R.string.connect_to_the_internet
                    : R.string.no_song_match_found);
        }

        if (results.size() == 0) {
            if (loading) {
                B.responseLayout.animate().setDuration(250).alpha(0).start();
            } else {
                B.responseLayout.animate().setDuration(250).alpha(1).start();
            }
        } else {
            B.responseLayout.animate().setDuration(250).alpha(0).start();
        }
    }

    private void onServerResultsChange(List<SearchResult> results) {
        boolean loading = mSearchVM.loading.getValue();
        mSearchAdapter.updateResults(results);
        updateVisuals(results, loading);
    }

    private void onLoadingChange(boolean loading) {
        List<SearchResult> results = mSearchVM.serverResults.getValue();
        updateVisuals(results, loading);

        if (!loading) {
            new Handler().postDelayed(() -> B.recyclerView.scrollToPosition(0), 600);
        }
    }

    private void onMessageChange(String message) {
        B.messageText.setText(message);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(B.parent);
        if (message.equals("")) {
            constraintSet.connect(R.id.recycler_view, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
        } else {
            constraintSet.connect(R.id.recycler_view, ConstraintSet.TOP, R.id.message_text, ConstraintSet.BOTTOM);
        }
        constraintSet.applyTo(B.parent);
    }
}