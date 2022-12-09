package com.zectan.soundroid.Fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.view.GestureDetectorCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zectan.soundroid.Adapters.PlayingLyricsAdapter;
import com.zectan.soundroid.Classes.Fragment;
import com.zectan.soundroid.Services.PlayingService;
import com.zectan.soundroid.databinding.FragmentPlayingLyricsBinding;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PlayingLyricsFragment extends Fragment<FragmentPlayingLyricsBinding> {
    private final GestureDetector.SimpleOnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            mNavController.navigate(PlayingLyricsFragmentDirections.openControls());
            return true;
        }
    };
    private PlayingService mPlayingService;
    private GestureDetectorCompat mGestureDetector;
    private PlayingLyricsAdapter mPlayingLyricsAdapter;

    public PlayingLyricsFragment() {
        super(FLAG_TRANSPARENT_STATUS);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public @Nullable View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        B = com.zectan.soundroid.databinding.FragmentPlayingLyricsBinding.inflate(inflater, container, false);
        super.onCreateView(inflater, container, savedInstanceState);

        // Recycler Views
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mActivity);
        mPlayingLyricsAdapter = new PlayingLyricsAdapter();
        B.recyclerView.setAdapter(mPlayingLyricsAdapter);
        B.recyclerView.setLayoutManager(layoutManager);

        if (mMainVM.playingService.getValue() == null) {
            mActivity.getPlayingService(service -> mPlayingService = service);
        } else {
            mPlayingService = mMainVM.playingService.getValue();
        }

        // Listeners
        B.recyclerView.setOnTouchListener((v, event) -> mGestureDetector.onTouchEvent(event));
        mPlayingService.lyrics.observe(this, this::onLyricsChange);

        B.parent.setBackground(mPlayingService.background.getValue());
        mGestureDetector = new GestureDetectorCompat(getContext(), gestureListener);

        return B.getRoot();
    }

    public void onLyricsChange(List<String> lyrics) {
        mPlayingLyricsAdapter.updateLyrics(lyrics);
        if (lyrics.size() == 0) {
            B.recyclerView.animate().alpha(0).setDuration(500).start();
            B.loadingText.animate().alpha(1).setDuration(500).start();
            B.progressbar.animate().alpha(1).setDuration(500).start();
        } else {
            B.recyclerView.animate().alpha(1).setDuration(500).start();
            B.loadingText.animate().alpha(0).setDuration(500).start();
            B.progressbar.animate().alpha(0).setDuration(500).start();
        }
    }
}
