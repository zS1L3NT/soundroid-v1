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
            mActivity.getPlayingService(service -> service.startPlaylist(playlist, song.getSongId(), mMainVM.myUser.getValue().getHighStreamQuality()));

            if (mMainVM.myUser.getValue().getOpenPlayingScreen()) {
                mNavController.navigate(SearchFragmentDirections.openPlaying());
            }
        }

        @Override
        public void onPlaylistClicked(Info info) {
            mPlaylistViewVM.playlistId.setValue(info.getId());
            mPlaylistViewVM.info.postValue(info);

            mNavController.navigate(SearchFragmentDirections.openPlaylistView());
        }

        @Override
        public boolean onMenuItemClicked(SearchResult result, MenuItem item) {
            return mActivity.handleMenuItemClick(result.getPlaylistInfo(), result.getSong(), item);
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
        mActivity.updateNavigator(0);

        // Recycler View
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mActivity);
        searchAdapter = new SearchAdapter(callback);
        B.recyclerView.setLayoutManager(layoutManager);
        B.recyclerView.setAdapter(searchAdapter);
        B.recyclerView.setHasFixedSize(true);

        // Observers
        mSearchVM.results.observe(this, this::onResultsChange);
        mSearchVM.loading.observe(this, this::onLoadingChange);
        mSearchVM.message.observe(this, this::onMessageChange);
        mSearchVM.error.observe(this, this::onErrorChange);
        B.headerBackImage.setOnClickListener(this::onBackPressed);
        mActivity.getPlayingService(service -> service.currentSong.observe(this, searchAdapter::updateCurrentSong));

        B.headerTextEditor.setText(mSearchVM.query.getValue());

        RxTextView
            .textChanges(B.headerTextEditor)
            .debounce(250, TimeUnit.MILLISECONDS)
            .map(CharSequence::toString)
            .subscribe(this::afterSearchDebounce);
        mActivity.showKeyboard();

        return B.getRoot();
    }

    @Override
    public void onStop() {
        super.onStop();
        mActivity.hideKeyboard(requireView());
    }

    private void onBackPressed(View view) {
        mActivity.onBackPressed();
    }

    private void afterSearchDebounce(String text) {
        String search = mSearchVM.query.getValue();
        if (!search.equals(text)) {
            mSearchVM.query.postValue(text);
            mSearchVM.search(text, mMainVM);
        }
    }

    private void onResultsChange(List<SearchResult> results) {
        boolean loading = mSearchVM.loading.getValue();
        searchAdapter.updateResults(results);
        updateVisuals(results, loading);
    }

    private void onLoadingChange(Boolean loading) {
        List<SearchResult> results = mSearchVM.results.getValue();
        updateVisuals(results, loading);
        B.headerLoadingCircle.setVisibility(loading ? View.VISIBLE : View.INVISIBLE);
        // Delayed so items can reorder first
        if (!loading) {
            new Handler().postDelayed(() -> B.recyclerView.smoothScrollToPosition(0), 600);
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void updateVisuals(List<SearchResult> results, boolean loading) {
        if (results.size() == 0) {
            if (loading) {
                B.responseLayout.animate().setDuration(250).alpha(0f).start();
            } else {
                B.responseLayout.animate().setDuration(1000).alpha(1f).start();
                if (B.headerTextEditor.getText().toString().isEmpty()) {
                    B.responseImage.setImageDrawable(mActivity.getDrawable(R.drawable.ic_search));
                    B.responseHeaderText.setText(R.string.search);
                    B.responseMessageText.setText(R.string.searchbar_placeholder);
                } else {
                    B.responseImage.setImageDrawable(mActivity.getDrawable(R.drawable.ic_search_no_results));
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