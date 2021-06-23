package com.zectan.soundroid.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.navigation.NavDirections;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionInflater;

import com.jakewharton.rxbinding.widget.RxTextView;
import com.zectan.soundroid.R;
import com.zectan.soundroid.adapters.SearchAdapter;
import com.zectan.soundroid.classes.Fragment;
import com.zectan.soundroid.databinding.FragmentSearchBinding;
import com.zectan.soundroid.objects.Info;
import com.zectan.soundroid.objects.Playlist;
import com.zectan.soundroid.objects.SearchResult;
import com.zectan.soundroid.objects.Song;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

// TODO Animate search results

public class SearchFragment extends Fragment<FragmentSearchBinding> {
    private static final String TAG = "(SounDroid) SearchFragment";
    private final SearchAdapter.Callback callback = new SearchAdapter.Callback() {

        @Override
        public void onSongClicked(Song song) {
            Info info = new Info(song.getId(), "Search Result", Collections.singletonList(song.getId()));
            Playlist playlist = new Playlist(info, Collections.singletonList(song));

            NavDirections action = SearchFragmentDirections.openSearchSong();
            navController.navigate(action);
            playingVM.startPlaylist(playlist, song.getId());
            activity.hideKeyboard(SearchFragment.this.requireView());
        }

        @Override
        public void onPlaylistClicked(Info info) {
            playlistViewVM.info.postValue(info);
            playlistViewVM.songs.postValue(new ArrayList<>());

            NavDirections action = SearchFragmentDirections.openPlaylistView();
            navController.navigate(action);
            playlistViewVM.firebase = false;
        }

        @Override
        public boolean onMenuItemClicked(SearchResult result, MenuItem item) {
            return false;
        }
    };
    private SearchAdapter searchAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TransitionInflater inflater = TransitionInflater.from(requireContext());
        setSharedElementEnterTransition(inflater.inflateTransition(R.transition.shared_image));
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        B = FragmentSearchBinding.inflate(inflater, container, false);
        super.onCreateView(inflater, container, savedInstanceState);

        // Recycler View
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity);
        searchAdapter = new SearchAdapter(callback);
        B.recyclerView.setLayoutManager(layoutManager);
        B.recyclerView.setAdapter(searchAdapter);
        B.recyclerView.setHasFixedSize(true);

        // Observers
        searchVM.results.observe(activity, this::onResultsChange);
        searchVM.loading.observe(activity, this::onLoadingChange);
        searchVM.error.observe(activity, this::onErrorChange);
        B.headerBackImage.setOnClickListener(this::onBackPressed);

        RxTextView
            .textChanges(B.headerTextEditor)
            .debounce(250, TimeUnit.MILLISECONDS)
            .map(CharSequence::toString)
            .subscribe(this::afterSearchDebounce);
        activity.hideNavigator();
        activity.showKeyboard();

        return B.getRoot();
    }

    private void onBackPressed(View view) {
        activity.onBackPressed();
        activity.hideKeyboard(requireView());
    }

    private void afterSearchDebounce(String text) {
        String search = searchVM.query.getValue();
        if (!search.equals(text)) {
            searchVM.query.postValue(text);
            searchVM.search(text, getContext());
        }
    }

    private void onResultsChange(List<SearchResult> results) {
        boolean loading = searchVM.loading.getValue();
        searchAdapter.updateResults(results, loading);
        updateVisuals(results, loading);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void onLoadingChange(Boolean loading) {
        List<SearchResult> results = searchVM.results.getValue();
        searchAdapter.updateLoading(loading);
        updateVisuals(results, loading);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void updateVisuals(List<SearchResult> results, boolean loading) {
        if (results.size() == 0) {
            if (loading) {
                B.responseLayout.animate().setDuration(250).alpha(0f).start();
            } else {
                B.responseLayout.animate().setDuration(1000).alpha(1f).start();
                if (B.headerTextEditor.getText().toString().isEmpty()) {
                    B.responseImage.setImageDrawable(activity.getDrawable(R.drawable.ic_search));
                    B.responseHeaderText.setText(R.string.search);
                    B.responseMessageText.setText(R.string.search_for_some_songs);
                } else {
                    B.responseImage.setImageDrawable(activity.getDrawable(R.drawable.ic_search_no_results));
                    B.responseHeaderText.setText(R.string.no_results);
                    B.responseMessageText.setText(R.string.no_song_match_found);
                }
            }
        } else {
            B.responseLayout.animate().setDuration(250).alpha(0f).start();
        }
    }

    private void onErrorChange(String error) {
        if (error != null) {
            Log.e(TAG, error);
            B.connectToTheInternetText.setText(R.string.connect_to_the_internet);
        } else {
            B.connectToTheInternetText.setText("");
        }
    }
}