package com.zectan.soundroid.fragments;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.navigation.NavDirections;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jakewharton.rxbinding.widget.RxTextView;
import com.zectan.soundroid.adapters.SearchAdapter;
import com.zectan.soundroid.databinding.FragmentSearchBinding;
import com.zectan.soundroid.objects.Playlist;
import com.zectan.soundroid.objects.PlaylistInfo;
import com.zectan.soundroid.objects.Song;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

// TODO Animate search results

public class SearchFragment extends FragmentAnimated<FragmentSearchBinding> {
    private static final String TAG = "(SounDroid) SearchFragment";

    private final SearchAdapter.Callback callback = new SearchAdapter.Callback() {
        @Override
        public void onSongClicked(Song song) {
            PlaylistInfo info = new PlaylistInfo(song.getId(), "Search Result", Collections.singletonList(song.getId()));
            Playlist playlist = new Playlist(info, Collections.singletonList(song));

            NavDirections action = SearchFragmentDirections.openSearchSong();
            navController.navigate(action);
            playingVM.selectSong(playlist, 0);
            activity.hideKeyboard(SearchFragment.this.requireView());
        }

        @Override
        public void onPlaylistClicked(PlaylistInfo info) {
            playlistViewVM.info.postValue(info);
            playlistViewVM.songs.postValue(new ArrayList<>());

            NavDirections action = SearchFragmentDirections.openPlaylistView();
            navController.navigate(action);
            playlistViewVM.firebase = false;
        }
    };

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        B = FragmentSearchBinding.inflate(inflater, container, false);
        super.onCreateView(inflater, container, savedInstanceState);

        // Recycler View
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity);
        SearchAdapter searchAdapter = new SearchAdapter(callback);
        B.recyclerView.setLayoutManager(layoutManager);
        B.recyclerView.setAdapter(searchAdapter);
        B.recyclerView.setHasFixedSize(true);

        // Observers
        searchVM.results.observe(activity, searchAdapter::updateResults);
        searchVM.error.observe(activity, System.out::println);
        B.headerBackImage.setOnClickListener(this::onBackPressed);

        RxTextView
            .textChanges(B.headerTextEditor)
            .debounce(250, TimeUnit.MILLISECONDS)
            .map(CharSequence::toString)
            .subscribe(this::afterSearchDebounce);
        activity.hideNavigator();
        activity.showKeyboard();
        B.headerTextEditor.setOnEditorActionListener(this::onTextChange);

        return B.getRoot();
    }

    private void onBackPressed(View view) {
        activity.onBackPressed();
        activity.hideKeyboard(requireView());
    }

    private boolean onTextChange(TextView textView, int actionId, KeyEvent keyEvent) {
        boolean handled = false;
        if (actionId == EditorInfo.IME_ACTION_SEND) {
            searchVM.search(B.headerTextEditor.getText().toString(), getContext());
            activity.hideKeyboard(this.requireView());
            handled = true;
        }
        return handled;
    }

    private void afterSearchDebounce(String text) {
        String search = searchVM.search.getValue();
        if (search == null || !search.equals(text)) {
            searchVM.search.postValue(text);
            searchVM.search(text, getContext());
        }
    }
}