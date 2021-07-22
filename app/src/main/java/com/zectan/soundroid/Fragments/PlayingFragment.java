package com.zectan.soundroid.Fragments;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.SeekBar;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.zectan.soundroid.Adapters.PlayingAdapter;
import com.zectan.soundroid.Classes.Fragment;
import com.zectan.soundroid.Models.Song;
import com.zectan.soundroid.R;
import com.zectan.soundroid.Utils.Animations;
import com.zectan.soundroid.Utils.MenuBuilder;
import com.zectan.soundroid.Utils.Utils;
import com.zectan.soundroid.databinding.FragmentPlayingBinding;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

@SuppressLint("UseCompatLoadingForDrawables")
public class PlayingFragment extends Fragment<FragmentPlayingBinding> {
    private static final String TAG = "(SounDroid) PlayingFragment";
    private final PlayingAdapter.Callback callback = new PlayingAdapter.Callback() {
        @Override
        public void onSongClicked(Song song) {
            mPlayingVM.changeSong(song.getSongId());
        }

        @Override
        public void onMove(int oldPosition, int newPosition) {
            mPlayingVM.onMoveSong(oldPosition + 1, newPosition + 1);
        }

        @Override
        public void onRemove(String songId) {
            mPlayingVM.onRemoveSong(songId);
        }

        @Override
        public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
            mItemTouchHelper.startDrag(viewHolder);
        }
    };
    private int mFinalTouch;
    private final SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                mFinalTouch = progress;

                double percent = (double) progress / 1000;
                double duration = (double) mPlayingVM.duration.getValue();
                int selectedTime = (int) (percent * duration);
                B.timeText.setText(Utils.formatDuration(selectedTime));
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            mPlayingVM.touchingSeekbar = true;
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mPlayingVM.seekTo(mFinalTouch);
            mPlayingVM.touchingSeekbar = false;
        }
    };
    private PlayingAdapter mPlayingAdapter;
    private ItemTouchHelper mItemTouchHelper;

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        B = FragmentPlayingBinding.inflate(inflater, container, false);
        super.onCreateView(inflater, container, savedInstanceState);
        mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        // Recycler Views
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mActivity);
        mPlayingAdapter = new PlayingAdapter(callback);
        mItemTouchHelper = new ItemTouchHelper(new PlayingAdapter.PlayingItemTouchHelper(mPlayingAdapter));
        B.recyclerView.setAdapter(mPlayingAdapter);
        B.recyclerView.setLayoutManager(layoutManager);
        mItemTouchHelper.attachToRecyclerView(B.recyclerView);

        // Live Observers
        mPlayingVM.queue.observe(this, mPlayingAdapter::updateSongs);
        mPlayingVM.currentSong.observe(this, this::onCurrentSongChange);
        mPlayingVM.time.observe(this, this::onTimeChange);
        mPlayingVM.buffered.observe(this, B.seekbar::setSecondaryProgress);
        mPlayingVM.progress.observe(this, B.seekbar::setProgress);
        mPlayingVM.duration.observe(this, this::onDurationChange);
        mPlayingVM.isBuffering.observe(this, this::onIsBufferingChange);
        mPlayingVM.isPlaying.observe(this, this::onIsPlayingChange);
        mPlayingVM.isShuffling.observe(this, this::onIsShufflingChange);
        mPlayingVM.isLooping.observe(this, this::onIsLoopingChange);
        mPlayingVM.error.observe(this, this::onErrorChange);

        B.backNavigateImage.setOnClickListener(__ -> mActivity.onBackPressed());
        B.moreImage.setOnClickListener(this::onMoreImageClicked);
        B.playPauseImage.setOnClickListener(this::playPauseSong);
        B.playPauseImage.setOnTouchListener(Animations::animationSmallSqueeze);
        B.playPauseMiniImage.setOnClickListener(this::playPauseSong);
        B.playPauseMiniImage.setOnTouchListener(Animations::animationMediumSqueeze);
        B.shuffleImage.setOnClickListener(__ -> mPlayingVM.toggleShuffle());
        B.shuffleImage.setOnTouchListener(Animations::animationMediumSqueeze);
        B.backImage.setOnClickListener(__ -> mPlayingVM.playPreviousSong());
        B.backImage.setOnTouchListener(Animations::animationMediumSqueeze);
        B.nextImage.setOnClickListener(__ -> mPlayingVM.playNextSong());
        B.nextImage.setOnTouchListener(Animations::animationMediumSqueeze);
        B.loopImage.setOnClickListener(__ -> mPlayingVM.toggleLoop());
        B.loopImage.setOnTouchListener(Animations::animationMediumSqueeze);
        B.seekbar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        B.parent.setTransitionListener(mActivity.getTransitionListener());

        B.parent.setBackground(mPlayingVM.background.getValue());
        mPlayingVM.error.setValue("");
        mFinalTouch = 0;

        return B.getRoot();
    }

    private void onMoreImageClicked(View view) {
        MenuBuilder.MenuItems items = new MenuBuilder.MenuItems();
        items.addToPlaylist();
        items.openQueue();
        items.clearQueue();
        MenuBuilder.createMenu(view, items, mPlayingVM.currentSong.getValue(), (song, item) -> mActivity.handleMenuItemClick(null, song, item, B.parent::transitionToEnd));
    }

    public void playPauseSong(View v) {
        if (mPlayingVM.isBuffering.getValue()) return;
        if (mPlayingVM.isPlaying.getValue()) {
            mPlayingVM.pause();
        } else {
            mPlayingVM.play();
        }
    }

    private void onCurrentSongChange(Song song) {
        String colorHex;
        mPlayingVM.onCurrentSongChanged(Looper.getMainLooper(), song);

        if (song == null) {
            mPlayingAdapter.updateSongs(new ArrayList<>());
            B.playlistNameText.setText("-");
            B.coverImage.setImageDrawable(mActivity.getDrawable(R.drawable.playing_cover_loading));
            B.titleText.setText("-");
            B.descriptionText.setText("-");
            colorHex = "#7b828b";
        } else {
            String title = song.getTitle();
            String artiste = song.getArtiste();
            String cover = song.getCover();
            colorHex = song.getColorHex();

            B.playlistNameText.setText(mPlayingVM.playlist.getValue().getInfo().getName());
            B.titleText.setText(title);
            B.descriptionText.setText(artiste);
            Glide
                .with(mActivity)
                .load(cover)
                .transition(DrawableTransitionOptions.withCrossFade())
                .error(R.drawable.playing_cover_failed)
                .centerCrop()
                .into(B.coverImage);
        }

        GradientDrawable oldGD = mPlayingVM.background.getValue();
        int[] colors = {Color.parseColor(colorHex), mActivity.getAttributeResource(R.attr.colorSecondary)};
        GradientDrawable newGD = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);

        Drawable[] layers = {oldGD, newGD};
        TransitionDrawable transition = new TransitionDrawable(layers);
        B.parent.setBackground(transition);
        mPlayingVM.background.setValue(newGD);
        transition.startTransition(500);
    }

    private void onTimeChange(int time) {
        B.timeText.setText(Utils.formatDuration(time));
    }

    private void onDurationChange(int duration) {
        B.durationText.setText(Utils.formatDuration(duration));
    }

    private void onIsPlayingChange(boolean isPlaying) {
        B.playPauseImage.setImageDrawable(
            mActivity.getDrawable(isPlaying ? R.drawable.controls_pause_filled : R.drawable.controls_play_filled));
        B.playPauseMiniImage
            .setImageDrawable(mActivity.getDrawable(isPlaying ? R.drawable.controls_pause : R.drawable.controls_play));
    }

    private void onIsBufferingChange(boolean loading) {
        B.loadingCircle.setAlpha(loading ? 1 : 0);
        B.parent.requestLayout();
    }

    private void onIsShufflingChange(boolean isShuffling) {
        B.shuffleImage.setColorFilter(
            ContextCompat.getColor(mActivity,
                isShuffling ? R.color.white : R.color.playing_inactive),
            android.graphics.PorterDuff.Mode.MULTIPLY);
    }

    private void onIsLoopingChange(boolean isLooping) {
        B.loopImage.setColorFilter(
            ContextCompat.getColor(mActivity,
                isLooping ? R.color.white : R.color.playing_inactive),
            android.graphics.PorterDuff.Mode.MULTIPLY);
    }

    private void onErrorChange(String error) {
        if (!error.equals("")) {
            B.errorText.setText(error);
            ValueAnimator darkenAnimation = ValueAnimator
                .ofArgb(mActivity.getColor(R.color.white), mActivity.getColor(R.color.playing_inactive))
                .setDuration(1000);
            darkenAnimation.addUpdateListener(animation -> B.coverImage.setColorFilter(
                (int) animation.getAnimatedValue(),
                PorterDuff.Mode.MULTIPLY));
            darkenAnimation.start();
            B.errorText.animate().alpha(1).setDuration(500).setStartDelay(500).start();
            B.retryText.animate().alpha(1).setDuration(500).setStartDelay(500).start();
            new Handler().postDelayed(() -> B.coverImage.setOnClickListener(__ -> {
                mPlayingVM.retry();
                mPlayingVM.error.setValue("");
            }), 1000);
        } else {
            B.coverImage.setOnClickListener(__ -> {
            });
            B.coverImage.clearColorFilter();
            B.errorText.setAlpha(0f);
            B.retryText.setAlpha(0f);
        }
    }

}
