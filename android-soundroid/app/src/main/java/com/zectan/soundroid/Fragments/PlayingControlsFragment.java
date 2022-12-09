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
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.GestureDetectorCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.zectan.soundroid.Adapters.PlayingControlsAdapter;
import com.zectan.soundroid.Classes.Fragment;
import com.zectan.soundroid.Models.Song;
import com.zectan.soundroid.R;
import com.zectan.soundroid.Services.PlayingService;
import com.zectan.soundroid.Utils.Animations;
import com.zectan.soundroid.Utils.MenuBuilder;
import com.zectan.soundroid.Utils.Utils;
import com.zectan.soundroid.databinding.FragmentPlayingControlsBinding;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

@SuppressLint("UseCompatLoadingForDrawables")
public class PlayingControlsFragment extends Fragment<FragmentPlayingControlsBinding> {
    private static final String TAG = "(SounDroid) PlayingFragment";
    private int mFinalTouch;
    private ValueAnimator mImageAnimator;
    private PlayingControlsAdapter mPlayingControlsAdapter;
    private ItemTouchHelper mItemTouchHelper;
    private PlayingService mPlayingService;
    private final GestureDetector.SimpleOnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            double relativeX = e.getX() / B.coverImage.getMeasuredWidth();
            if (relativeX < 0.25) {
                // Backward double press
                B.backward1Icon.setAlpha(0.8f);
                B.backwardBackground.setAlpha(0.25f);
                new Handler().postDelayed(() -> B.backward2Icon.setAlpha(0.8f), 100);
                new Handler().postDelayed(() -> B.backward3Icon.setAlpha(0.8f), 200);
                B.backward1Icon.animate().alpha(0f).setStartDelay(250).setDuration(250).start();
                B.backward2Icon.animate().alpha(0f).setStartDelay(350).setDuration(250).start();
                B.backward3Icon.animate().alpha(0f).setStartDelay(450).setDuration(250).start();
                B.backwardBackground.animate().alpha(0f).setStartDelay(450).setDuration(250).start();
                mPlayingService.backwardTime(mMainVM.myUser.getValue().getSeekDuration());
                return true;
            }

            if (relativeX > 0.75) {
                // Forward double press
                B.forward1Icon.setAlpha(0.8f);
                B.forwardBackground.setAlpha(0.25f);
                new Handler().postDelayed(() -> B.forward2Icon.setAlpha(0.8f), 100);
                new Handler().postDelayed(() -> B.forward3Icon.setAlpha(0.8f), 200);
                B.forward1Icon.animate().alpha(0f).setStartDelay(250).setDuration(250).start();
                B.forward2Icon.animate().alpha(0f).setStartDelay(350).setDuration(250).start();
                B.forward3Icon.animate().alpha(0f).setStartDelay(450).setDuration(250).start();
                B.forwardBackground.animate().alpha(0f).setStartDelay(450).setDuration(250).start();
                mPlayingService.forwardTime(mMainVM.myUser.getValue().getSeekDuration());
                return true;
            }

            mNavController.navigate(PlayingControlsFragmentDirections.openLyrics());
            return true;
        }
    };
    private final PlayingControlsAdapter.Callback callback = new PlayingControlsAdapter.Callback() {
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
                // Change the last touch value
                mFinalTouch = progress;

                // Change time text
                double percent = (double) progress / 1000;
                double duration = (double) mPlayingService.duration.getValue();
                int selectedTime = (int) (percent * duration);
                B.timeText.setText(Utils.formatDuration(selectedTime));
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // Set that seekbar is being touched
            mPlayingService.touchingSeekbar.setValue(true);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // After seek done, seek to location
            mPlayingService.seekTo(mFinalTouch);
            mPlayingService.touchingSeekbar.setValue(false);
        }
    };
    private int mImageColor;
    private GestureDetectorCompat mGestureDetector;

    public PlayingControlsFragment() {
        super(FLAG_TRANSPARENT_STATUS);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        B = FragmentPlayingControlsBinding.inflate(inflater, container, false);
        super.onCreateView(inflater, container, savedInstanceState);

        // Recycler Views
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mActivity);
        mPlayingControlsAdapter = new PlayingControlsAdapter(callback);
        mItemTouchHelper = new ItemTouchHelper(new PlayingControlsAdapter.PlayingItemTouchHelper(mPlayingControlsAdapter));
        B.recyclerView.setAdapter(mPlayingControlsAdapter);
        B.recyclerView.setLayoutManager(layoutManager);
        mItemTouchHelper.attachToRecyclerView(B.recyclerView);

        if (mMainVM.playingService.getValue() == null) {
            mActivity.getPlayingService(service -> mPlayingService = service);
        } else {
            mPlayingService = mMainVM.playingService.getValue();
        }

        mGestureDetector = new GestureDetectorCompat(getContext(), gestureListener);

        // Listeners
        mPlayingService.queue.observe(this, mPlayingControlsAdapter::updateSongs);
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

        B.parent.setBackground(mPlayingService.background.getValue());
        mPlayingService.error.setValue("");
        mFinalTouch = 0;
        mImageColor = mActivity.getColor(R.color.white);

        return B.getRoot();
    }

    /**
     * Animate the image to become darker
     */
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

    /**
     * Animate the image to become brighter
     */
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

    /**
     * Toggle the playing and pausing of the song
     *
     * @param v View
     */
    public void playPauseSong(View v) {
        if (mPlayingService.isBuffering.getValue()) return;
        if (mPlayingService.isPlaying.getValue()) {
            mPlayingService.pause(true);
        } else {
            mPlayingService.play();
        }
    }

    private void onMoreImageClicked(View view) {
        MenuBuilder.MenuItems items = new MenuBuilder.MenuItems();
        items.addToPlaylist();
        items.openQueue();
        items.showLyrics();
        items.clearQueue();
        MenuBuilder.createMenu(view, items, mPlayingService.currentSong.getValue(), (song, item) -> mActivity.handleMenuItemClick(null, song, item, B.parent::transitionToEnd));
    }

    private void onCurrentSongChange(Song song) {
        String colorHex;

        if (song == null) {
            // Song is null, set it to the default
            mPlayingControlsAdapter.updateSongs(new ArrayList<>());
            B.playlistNameText.setText("-");
            B.coverImage.setImageResource(R.drawable.playing_cover_loading);
            B.titleText.setText("-");
            B.descriptionText.setText("-");
            colorHex = "#7b828b";
        } else {
            // Song is showing, display song details
            String title = song.getTitle();
            String artiste = song.getArtiste();
            String cover = song.getCover();
            colorHex = song.getColorHex();

            B.playlistNameText.setText(mPlayingService.playable.getValue().getInfo().getName());
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

        // Transition the colors in the gradient background
        GradientDrawable oldGradientDrawable = mPlayingService.background.getValue();
        int[] colors = {Color.parseColor(colorHex), mActivity.getAttributeResource(R.attr.colorSecondary)};
        GradientDrawable newGradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
        Drawable[] layers = {oldGradientDrawable, newGradientDrawable};
        TransitionDrawable transition = new TransitionDrawable(layers);
        B.parent.setBackground(transition);
        transition.startTransition(500);

        // Store the most recent background in ViewModel
        mPlayingService.background.setValue(newGradientDrawable);
    }

    private void onTimeChange(int time) {
        B.timeText.setText(Utils.formatDuration(time));
    }

    private void onDurationChange(int duration) {
        B.durationText.setText(Utils.formatDuration(duration));
    }

    private void onIsPlayingChange(boolean isPlaying) {
        B.playPauseImage.setImageResource(isPlaying
            ? R.drawable.ic_controls_pause_filled
            : R.drawable.ic_controls_play_filled);
        B.playPauseMiniImage.setImageResource(isPlaying
            ? R.drawable.ic_controls_pause
            : R.drawable.ic_controls_play);
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
        B.shuffleImage.setColorFilter(ContextCompat.getColor(
            mActivity,
            isShuffling
                ? R.color.white
                : R.color.playing_inactive
            ),
            android.graphics.PorterDuff.Mode.MULTIPLY
        );
    }

    private void onIsLoopingChange(boolean isLooping) {
        B.loopImage.setColorFilter(ContextCompat.getColor(
            mActivity,
            isLooping
                ? R.color.white
                : R.color.playing_inactive
            ),
            android.graphics.PorterDuff.Mode.MULTIPLY
        );
    }

    @SuppressLint("ClickableViewAccessibility")
    private void onErrorChange(String error) {
        if (!error.equals("")) {
            // Animate error
            B.errorText.setText(error);
            B.errorText.animate().alpha(1).setDuration(500).setStartDelay(500).start();
            B.retryText.animate().alpha(1).setDuration(500).setStartDelay(500).start();
            darkenAnimateImage();
            new Handler().postDelayed(() -> {
                B.coverImage.setOnClickListener(__ -> {
                    mPlayingService.retry();
                    mPlayingService.error.setValue("");
                });
                B.coverImage.setOnTouchListener((__, ___) -> false);
            }, 1000);
        } else {
            B.coverImage.setOnClickListener(__ -> {
            });
            B.coverImage.setOnTouchListener((v, event) -> mGestureDetector.onTouchEvent(event));
            B.errorText.setAlpha(0f);
            B.retryText.setAlpha(0f);
            brightenAnimateImage();
        }
    }

}