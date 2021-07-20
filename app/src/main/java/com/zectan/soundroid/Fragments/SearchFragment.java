package com.zectan.soundroid.Fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionInflater;

import com.jakewharton.rxbinding.widget.RxTextView;
import com.zectan.soundroid.Adapters.SearchAdapter;
import com.zectan.soundroid.Classes.Fragment;
import com.zectan.soundroid.Models.Info;
import com.zectan.soundroid.Models.Playlist;
import com.zectan.soundroid.Models.SearchResult;
import com.zectan.soundroid.Models.Song;
import com.zectan.soundroid.R;
import com.zectan.soundroid.databinding.FragmentSearchBinding;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SearchFragment extends Fragment<FragmentSearchBinding> {
    private static final String TAG = "(SounDroid) SearchFragment";
    private final SearchAdapter.Callback callback = new SearchAdapter.Callback() {

        @Override
        public void onSongClicked(Song song) {
            Info info = new Info(song.getSongId(), "Search Result", Collections.singletonList(song.getSongId()));
            Playlist playlist = new Playlist(info, Collections.singletonList(song));
            playingVM.startPlaylist(activity, playlist, song.getSongId(), mainVM.myUser.getValue().getHighStreamQuality());

            if (mainVM.myUser.getValue().getOpenPlayingScreen()) {
                navController.navigate(SearchFragmentDirections.openSearchSong());
            }
        }

        @Override
        public void onPlaylistClicked(Info info) {
            playlistViewVM.playlistId.setValue(info.getId());
            playlistViewVM.info.postValue(info);

            navController.navigate(SearchFragmentDirections.openPlaylistView());
        }

        @Override
        public boolean onMenuItemClicked(SearchResult result, MenuItem item) {
            return activity.handleMenuItemClick(result.getPlaylistInfo(), result.getSong(), item);
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
        searchVM.results.observe(this, this::onResultsChange);
        searchVM.loading.observe(this, this::onLoadingChange);
        searchVM.message.observe(this, this::onMessageChange);
        searchVM.error.observe(this, this::onErrorChange);
        B.headerBackImage.setOnClickListener(this::onBackPressed);
        playingVM.currentSong.observe(this, searchAdapter::updateCurrentSong);

        RxTextView
            .textChanges(B.headerTextEditor)
            .debounce(250, TimeUnit.MILLISECONDS)
            .map(CharSequence::toString)
            .subscribe(this::afterSearchDebounce);
        activity.showKeyboard();

        return B.getRoot();
    }

    @Override
    public void onStop() {
        super.onStop();
        activity.hideKeyboard(requireView());
    }

    private void onBackPressed(View view) {
        activity.onBackPressed();
    }

    private void afterSearchDebounce(String text) {
        String search = searchVM.query.getValue();
        if (!search.equals(text)) {
            searchVM.query.postValue(text);
            searchVM.search(text, mainVM);
        }
    }

    private void onResultsChange(List<SearchResult> results) {
        boolean loading = searchVM.loading.getValue();
        searchAdapter.updateResults(results);
        updateVisuals(results, loading);
        // Delayed so items can reorder first
        new Handler().postDelayed(() -> B.recyclerView.smoothScrollToPosition(0), 600);
    }

    private void onLoadingChange(Boolean loading) {
        List<SearchResult> results = searchVM.results.getValue();
        updateVisuals(results, loading);
        B.headerLoadingCircle.setVisibility(loading ? View.VISIBLE : View.INVISIBLE);
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
                    B.responseMessageText.setText(R.string.searchbar_placeholder);
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

    private void onMessageChange(String message) {
        B.messageText.setText(message);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(B.parent);
        if (message.equals("")) {
            constraintSet.connect(R.id.recycler_view, ConstraintSet.TOP, R.id.header, ConstraintSet.BOTTOM);
        } else {
            constraintSet.connect(R.id.recycler_view, ConstraintSet.TOP, R.id.message_text, ConstraintSet.BOTTOM);
        }
        constraintSet.applyTo(B.parent);
    }

    private void onErrorChange(String error) {
        if (!error.equals("")) {
            Log.e(TAG, error);
            B.connectToTheInternetText.setText(R.string.connect_to_the_internet);
        } else {
            B.connectToTheInternetText.setText("");
        }
    }
}