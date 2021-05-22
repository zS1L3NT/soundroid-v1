package com.zectan.soundroid.fragments;

import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jakewharton.rxbinding.widget.RxTextView;
import com.zectan.soundroid.MainActivity;
import com.zectan.soundroid.R;
import com.zectan.soundroid.adapters.SearchAdapter;
import com.zectan.soundroid.objects.Playlist;
import com.zectan.soundroid.objects.Song;
import com.zectan.soundroid.viewmodels.PlayingViewModel;
import com.zectan.soundroid.viewmodels.SearchViewModel;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;

public class SearchFragment extends Fragment {
    private static final String TAG = "(SounDroid) SearchFragment";
    private MainActivity activity;
    private InputMethodManager imm;

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
        activity.hideBottomNavigator();
        imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);

        // ViewModels
        searchVM = new ViewModelProvider(activity).get(SearchViewModel.class);
        playingVM = new ViewModelProvider(activity).get(PlayingViewModel.class);

        // Reference views
        searchEditText = view.findViewById(R.id.search_edit_text);
        recyclerView = view.findViewById(R.id.search_recycler_view);
        searchProgressbar = view.findViewById(R.id.search_loading);

        // Click listeners
        view.findViewById(R.id.search_back).setOnClickListener(__ -> {
            activity.onBackPressed();
            closeKeyboard();
        });
        Observable<String> obs = RxTextView
                .textChanges(searchEditText)
                .debounce(300, TimeUnit.MILLISECONDS)
                .map(CharSequence::toString);
        obs.subscribe(this::searchOnline);

        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            boolean handled = false;
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                searchOnline(searchEditText.getText().toString());
                closeKeyboard();
                handled = true;
            }
            return handled;
        });

        searchVM.liveSongs().observe(activity, this::onSongsChange);
        searchVM.liveSearching().observe(activity, this::onSearchingChange);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        activity.showBottomNavigator();
    }

    public void searchOnline(String text) {
        Log.d(TAG, "SEARCHING: " + text);
        searchVM.searchOnline(text, activity.getFilesDir().getPath());
    }

    public void closeKeyboard() {
        View focused = activity.getCurrentFocus();
        imm.hideSoftInputFromWindow(focused.getWindowToken(), 0);
    }

    private void onSongsChange(List<Song> songs) {
        SearchAdapter songAdapter = new SearchAdapter(songs, (song, position) -> {
            Playlist queue = new Playlist(song.getId(), "Search result", Collections.singletonList(song));
            NavHostFragment.findNavController(this).navigate(SearchFragmentDirections.openSearchSong());
            playingVM.selectSong(queue, 0);
            closeKeyboard();
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