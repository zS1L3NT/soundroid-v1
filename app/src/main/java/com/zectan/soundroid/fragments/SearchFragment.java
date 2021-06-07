package com.zectan.soundroid.fragments;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.FragmentNavigator;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jakewharton.rxbinding.widget.RxTextView;
import com.zectan.soundroid.AnimatedFragment;
import com.zectan.soundroid.MainActivity;
import com.zectan.soundroid.R;
import com.zectan.soundroid.adapters.SearchAdapter;
import com.zectan.soundroid.objects.Playlist;
import com.zectan.soundroid.objects.PlaylistInfo;
import com.zectan.soundroid.objects.Song;
import com.zectan.soundroid.viewmodels.PlayingViewModel;
import com.zectan.soundroid.viewmodels.SearchViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;

public class SearchFragment extends AnimatedFragment {
    private static final String TAG = "(SounDroid) SearchFragment";
    private MainActivity activity;

    private RecyclerView recyclerView;
    private EditText searchEditText;
    private ProgressBar searchProgressbar;

    private SearchViewModel searchVM;
    private PlayingViewModel playingVM;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        activity = (MainActivity) getActivity();
        assert activity != null;
        activity.hideNavigator();

        // ViewModels
        searchVM = new ViewModelProvider(activity).get(SearchViewModel.class);
        playingVM = new ViewModelProvider(activity).get(PlayingViewModel.class);

        // Reference views
        ImageView searchBack = view.findViewById(R.id.search_back);
        searchEditText = view.findViewById(R.id.search_edit_text);
        recyclerView = view.findViewById(R.id.search_recycler_view);
        searchProgressbar = view.findViewById(R.id.search_loading);

        // Recycler View
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        // Live Observers
        searchVM.songs.observe(activity, this::onSongsChange);
        searchVM.searching.observe(activity, this::onSearchingChange);

        searchBack.setOnClickListener(this::onBackPressed);
        Observable<String> obs = RxTextView
                .textChanges(searchEditText)
                .debounce(500, TimeUnit.MILLISECONDS)
                .map(CharSequence::toString);
        obs.subscribe(this::searchOnline);
        activity.showKeyboard();
        searchEditText.setOnEditorActionListener(this::onTexChange);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        activity.showNavigator();
    }

    private boolean onTexChange(TextView textView, int actionId, KeyEvent keyEvent) {
        boolean handled = false;
        if (actionId == EditorInfo.IME_ACTION_SEND) {
            searchOnline(searchEditText.getText().toString());
            activity.hideKeyboard(this.requireView());
            handled = true;
        }
        return handled;
    }

    private void onBackPressed(View view) {
        activity.onBackPressed();
        activity.hideKeyboard(requireView());
    }

    public void searchOnline(String text) {
        Log.d(TAG, "SEARCHING: " + text);
        searchVM.searchOnline(text, getContext());
    }

    private void onSongsChange(List<Song> songs) {
        SearchAdapter songAdapter = new SearchAdapter(songs, (cover, transitionName, song, position) -> {
            PlaylistInfo info = new PlaylistInfo("spotify-results", "Spotify Results", new ArrayList<>());
            Playlist queue = new Playlist(info, Collections.singletonList(song));
            cover.setTransitionName(transitionName);

            FragmentNavigator.Extras extras = new FragmentNavigator.Extras
                    .Builder()
                    .addSharedElement(cover, transitionName)
                    .build();
            NavDirections action = SearchFragmentDirections
                    .openSearchSong()
                    .setTransitionName(transitionName);
            NavHostFragment.findNavController(this).navigate(action, extras);
            playingVM.selectSong(queue, 0);
            activity.hideKeyboard(this.requireView());
        });
        recyclerView.setAdapter(songAdapter);
    }

    private void onSearchingChange(boolean searching) {
        ValueAnimator fadeIn = ValueAnimator.ofFloat(0f, 1f).setDuration(500);
        ValueAnimator fadeOut = ValueAnimator.ofFloat(1f, 0f).setDuration(500);

        fadeIn.addUpdateListener(animation -> {
            if (searching) {
                searchProgressbar.setAlpha((float) animation.getAnimatedValue());
            } else {
                recyclerView.setAlpha((float) animation.getAnimatedValue());
            }
        });

        fadeOut.addUpdateListener(animation -> {
            if (searching) {
                recyclerView.setAlpha((float) animation.getAnimatedValue());
            } else {
                searchProgressbar.setAlpha((float) animation.getAnimatedValue());
            }
        });

        fadeOut.start();
        fadeIn.start();
    }
}