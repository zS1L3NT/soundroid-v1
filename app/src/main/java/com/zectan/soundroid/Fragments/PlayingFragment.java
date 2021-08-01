package com.zectan.soundroid.Fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.ColorInt;
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
import com.zectan.soundroid.Services.PlayingService;
import com.zectan.soundroid.Utils.Animations;
import com.zectan.soundroid.Utils.MenuBuilder;
import com.zectan.soundroid.Utils.Utils;
import com.zectan.soundroid.databinding.FragmentPlayingBinding;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

@SuppressLint("UseCompatLoadingForDrawables")
public class PlayingFragment extends Fragment<FragmentPlayingBinding> {
    private static final String TAG = "(SounDroid) PlayingFragment";
    private @ColorInt
    int mImageColor;
    private int mFinalTouch;
    private ValueAnimator mImageAnimator;
    private PlayingAdapter mPlayingAdapter;
    private ItemTouchHelper mItemTouchHelper;
    private PlayingService mPlayingService;
    private final PlayingAdapter.Callback callback = new PlayingAdapter.Callback() {
        @Override
        public void onSongClicked(Song song) {
            mPlayingService.changeSong(song.getSongId());
            B.recyclerView.scrollToPosition(0);

            if (mMainVM.myUser.getValue().getOpenPlayingScreen()) {
                B.parent.transitionToStart();
            }
        }

        @Override
        public void onMove(int oldPosition, int newPosition) {
            mPlayingService.onMoveSong(oldPosition + 1, newPosition + 1);
        }

        @Override
        public void onRemove(String songId) {
            mPlayingService.onRemoveSong(songId);
        }

        @Override
        public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
            mItemTouchHelper.startDrag(viewHolder);
        }
    };
    private final SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                mFinalTouch = progress;

                double percent = (double) progress / 1000;
                double duration = (double) mPlayingService.duration.getValue();
                int selectedTime = (int) (percent * duration);
                B.timeText.setText(Utils.formatDuration(selectedTime));
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            mPlayingService.touchingSeekbar.setValue(true);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mPlayingService.seekTo(mFinalTouch);
            mPlayingService.touchingSeekbar.setValue(false);
        }
    };

    public PlayingFragment() {
        super(FLAG_TRANSPARENT_STATUS);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        B = FragmentPlayingBinding.inflate(inflater, container, false);
        super.onCreateView(inflater, container, savedInstanceState);

        // Recycler Views
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mActivity);
        mPlayingAdapter = new PlayingAdapter(callback);
        mItemTouchHelper = new ItemTouchHelper(new PlayingAdapter.PlayingItemTouchHelper(mPlayingAdapter));
        B.recyclerView.setAdapter(mPlayingAdapter);
        B.recyclerView.setLayoutManager(layoutManager);
        mItemTouchHelper.attachToRecyclerView(B.recyclerView);

        if (mMainVM.playingService.getValue() == null) {
            mActivity.getPlayingService(service -> mPlayingService = service);
        } else {
            mPlayingService = mMainVM.playingService.getValue();
        }

        // Live Observers
        mPlayingService.queue.observe(this, mPlayingAdapter::updateSongs);
        mPlayingService.currentSong.observe(this, this::onCurrentSongChange);
        mPlayingService.time.observe(this, this::onTimeChange);
        mPlayingService.buffered.observe(this, B.seekbar::setSecondaryProgress);
        mPlayingService.progress.observe(this, B.seekbar::setProgress);
        mPlayingService.duration.observe(this, this::onDurationChange);
        mPlayingService.isBuffering.observe(this, this::onIsBufferingChange);
        mPlayingService.isPlaying.observe(this, this::onIsPlayingChange);
        mPlayingService.isShuffling.observe(this, this::onIsShufflingChange);
        mPlayingService.isLooping.observe(this, this::onIsLoopingChange);
        mPlayingService.error.observe(this, this::onErrorChange);

        B.backNavigateImage.setOnClickListener(__ -> mActivity.onBackPressed());
        B.moreImage.setOnClickListener(this::onMoreImageClicked);
        B.playPauseImage.setOnClickListener(this::playPauseSong);
        B.playPauseImage.setOnTouchListener(Animations::animationSmallSqueeze);
        B.playPauseMiniImage.setOnClickListener(this::playPauseSong);
        B.playPauseMiniImage.setOnTouchListener(Animations::animationMediumSqueeze);
        B.shuffleImage.setOnClickListener(__ -> mPlayingService.toggleShuffle());
        B.shuffleImage.setOnTouchListener(Animations::animationMediumSqueeze);
        B.backImage.setOnClickListener(__ -> mPlayingService.playPreviousSong());
        B.backImage.setOnTouchListener(Animations::animationMediumSqueeze);
        B.nextImage.setOnClickListener(__ -> mPlayingService.playNextSong());
        B.nextImage.setOnTouchListener(Animations::animationMediumSqueeze);
        B.loopImage.setOnClickListener(__ -> mPlayingService.toggleLoop());
        B.loopImage.setOnTouchListener(Animations::animationMediumSqueeze);
        B.seekbar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        B.parent.setTransitionListener(mActivity.getTransitionListener());

        B.parent.setBackground(mPlayingService.background.getValue());
        mPlayingService.error.setValue("");
        mFinalTouch = 0;
        mImageColor = mActivity.getColor(R.color.white);

        return B.getRoot();
    }

    private void darkenAnimateImage() {
        if (mImageAnimator != null) mImageAnimator.pause();
        mImageAnimator = ValueAnimator.ofArgb(mImageColor, mActivity.getColor(R.color.playing_inactive)).setDuration(1000);
        mImageAnimator.addUpdateListener(animation -> {
            @ColorInt int color = (int) animation.getAnimatedValue();
            B.coverImage.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
            mImageColor = color;
        });
        mImageAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mImageAnimator = null;
            }
        });
        mImageAnimator.start();
    }

    private void brightenAnimateImage() {
        if (mPlayingService.error.getValue().equals("") && !mPlayingService.isBuffering.getValue()) {
            if (mImageAnimator != null) mImageAnimator.pause();
            mImageAnimator = ValueAnimator.ofArgb(mImageColor, mActivity.getColor(R.color.white)).setDuration(1000);
            mImageAnimator.addUpdateListener(animation -> {
                @ColorInt int color = (int) animation.getAnimatedValue();
                B.coverImage.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
                mImageColor = color;
            });
            mImageAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mImageAnimator = null;
                }
            });
            mImageAnimator.start();
        }
    }

    private void onMoreImageClicked(View view) {
        MenuBuilder.MenuItems items = new MenuBuilder.MenuItems();
        items.addToPlaylist();
        items.openQueue();
        items.clearQueue();
        MenuBuilder.createMenu(view, items, mPlayingService.currentSong.getValue(), (song, item) -> mActivity.handleMenuItemClick(null, song, item, B.parent::transitionToEnd));
    }

    public void playPauseSong(View v) {
        if (mPlayingService.isBuffering.getValue()) return;
        if (mPlayingService.isPlaying.getValue()) {
            mPlayingService.pause(true);
        } else {
            mPlayingService.play();
        }
    }

    private void onCurrentSongChange(Song song) {
        String colorHex;

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

            B.playlistNameText.setText(mPlayingService.playlist.getValue().getInfo().getName());
            B.titleText.setText(title);
            B.descriptionText.setText(artiste);
            Glide
                .with(mActivity)
                .load(cover)
                .transition(DrawableTransitionOptions.withCrossFade())
                .placeholder(R.drawable.playing_cover_failed)
                .error(R.drawable.playing_cover_failed)
                .centerCrop()
                .into(B.coverImage);
        }

        GradientDrawable oldGD = mPlayingService.background.getValue();
        int[] colors = {Color.parseColor(colorHex), mActivity.getAttributeResource(R.attr.colorSecondary)};
        GradientDrawable newGD = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);

        Drawable[] layers = {oldGD, newGD};
        TransitionDrawable transition = new TransitionDrawable(layers);
        B.parent.setBackground(transition);
        mPlayingService.background.setValue(newGD);
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
            mActivity.getDrawable(isPlaying ? R.drawable.ic_controls_pause_filled : R.drawable.ic_controls_play_filled));
        B.playPauseMiniImage
            .setImageDrawable(mActivity.getDrawable(isPlaying ? R.drawable.ic_controls_pause : R.drawable.ic_controls_play));
    }

    private void onIsBufferingChange(boolean loading) {
        B.loadingCircle.setAlpha(loading ? 1 : 0);
        B.parent.requestLayout();
        if (loading) {
            darkenAnimateImage();
        } else {
            brightenAnimateImage();
        }
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
            B.errorText.animate().alpha(1).setDuration(500).setStartDelay(500).start();
            B.retryText.animate().alpha(1).setDuration(500).setStartDelay(500).start();
            darkenAnimateImage();
            new Handler().postDelayed(() -> B.coverImage.setOnClickListener(__ -> {
                mPlayingService.retry();
                mPlayingService.error.setValue("");
            }), 1000);
        } else {
            B.coverImage.setOnClickListener(__ -> {
            });
            B.errorText.setAlpha(0f);
            B.retryText.setAlpha(0f);
            brightenAnimateImage();
        }
    }

}
